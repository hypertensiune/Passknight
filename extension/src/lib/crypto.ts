import * as Base64 from "../../public/base64.js";
import { loadKeyFromStorage, saveKeyToStorage } from "./extension.js";

export function generateSalt(): string {
  const arr = crypto.getRandomValues(new Uint8Array(32));

  return Base64.encode(arr);
}

class CryptoUtils {

  static iterations = 600000;

  static privateKeyType = { name: "AES-CBC", length: 256 };
  static hkdfKeyType = { name: "HKDF", hash: "SHA-256" }

  static pkdf2params = (salt: ArrayBuffer): Pbkdf2Params => {
    return {
      name: "PBKDF2",
      hash: "SHA-256",
      salt: salt,
      iterations: CryptoUtils.iterations
    }
  }

  static hkdfparams = (salt: ArrayBuffer): HkdfParams => {
    return {
      name: "HKDF",
      hash: "SHA-512",
      salt: salt,
      info: new ArrayBuffer(0)
    }
  }

  static string2Buf(input: string) {
    return (new TextEncoder()).encode(input);
  }

  static buf2String(input: ArrayBuffer) {
    return (new TextDecoder()).decode(input);
  }

  static buf2hex(input: ArrayBuffer) {
    return [...new Uint8Array(input)].map(x => x.toString(16).padStart(2, '0')).join('');
  }

  static buf2Base64(input: ArrayBuffer) {
    return [...new Uint8Array(input)].map(x => x.toString(64)).join('');
  }

  static concatBufs(a: ArrayBuffer, b: ArrayBuffer) {
    const c = new Uint8Array(a.byteLength + b.byteLength);
    c.set(new Uint8Array(a), 0);
    c.set(new Uint8Array(b), a.byteLength);

    return c;
  }

  /**
   * Split the input buffer in the actual data and the IV used for encryption. 
   * The IV is the first bytes in the buffer. 
   */
  static split(input: ArrayBuffer, bytes: number) {
    const arr = new Uint8Array(input);
    return [arr.slice(0, bytes), arr.slice(bytes)];
  }
}

export class CryptoProvider {
  private static cryptography: Cryptography | null = null

  /**
   * Get the {@link Cryptography} object that handles encryption and decryption
   * 
   * Make sure {@link createProvider} or {@link loadProvider} were successfully
   * called before calling this otherwise it returns null.
   */
  public static getProvider() {
    return this.cryptography
  }

  public static async getMasterPasswordHashString(email: string, password: string) {
    const master = await this.getMasterPasswordHash(email, password);
    return Base64.encode(master);
  }

  /**
   * Generates a master key by derivating the password with PBKDF2 and the email as a salt.
   * The master password hash is obtained by derivating the master key again with PBKDF2 and
   * the original password a salt
   */
  private static async getMasterPasswordHash(email: string, password: string) {
    // Import the password
    const key = await crypto.subtle.importKey("raw", CryptoUtils.string2Buf(password), "PBKDF2", false, ["deriveBits"]);
    
    // And derive a key for pbkdf2 usage
    let tmp = await crypto.subtle.deriveBits(CryptoUtils.pkdf2params(CryptoUtils.string2Buf(email)), key, 256);
    const masterKey = await crypto.subtle.importKey("raw", tmp, "PBKDF2", false, ["deriveBits"]);
  
    // To get the master password hash derive the master key with the password as a salt    
    const masterPasswordHash = await crypto.subtle.deriveBits(CryptoUtils.pkdf2params(CryptoUtils.string2Buf(password)), masterKey, 256);

    return masterPasswordHash
  }

  /**
   * Generates a master key by derivating the password with PBKDF2 and the email as a salt which is then
   * derivated again with HKDF to stretch it to 512 bits
   */
  private static async getStretchedMasterKey(email: string, password: string) {
    // Import the password
    const key = await crypto.subtle.importKey("raw", CryptoUtils.string2Buf(password), "PBKDF2", false, ["deriveBits"]);
    
    // Derive a key one for hkdf usage
    // https://stackoverflow.com/questions/65112015/why-cant-you-derive-a-hkdf-key-with-with-webcrypto
    const tmp = await crypto.subtle.deriveBits(CryptoUtils.pkdf2params(CryptoUtils.string2Buf(email)), key, 256);
    const masterKeyHKDF = await crypto.subtle.importKey("raw", tmp, "HKDF", false, ["deriveKey"]);

    // Using HKDF stretch the master key. This is used to encrypt a randomly generated symmetric password
    const stretchedMasterKey = await crypto.subtle.deriveKey(CryptoUtils.hkdfparams(CryptoUtils.string2Buf(email)), masterKeyHKDF, CryptoUtils.privateKeyType, true, ["encrypt", "decrypt"]);
  
    return stretchedMasterKey
  }

  /**
   * Creates the symmetric key used for encryption and decryption & generates the master password hash used for firebase authentication.
   * (Used when a vault is created)
   * 
   * This process is influenced by Bitwarden's security paper. For more details see 
   * {@link https://bitwarden.com/help/bitwarden-security-white-paper/}
   * 
   * @return The master password hash that is used for authentication with firebase account and the 
   * protected symmetric key that is used with encrypting and decrypting as base64 strings
   */
  public static async createProvider(email: string, password: string) {
    
    const stretchedMasterKey = await this.getStretchedMasterKey(email, password);

    // Generate a 256 bit symmetric key and a 128 bit initialization vector
    // and encrypt the symmetric key using the stretched master key
    const generatedSymmetricKey = crypto.getRandomValues(new Uint8Array(32));
    const iv = crypto.getRandomValues(new Uint8Array(16));

    const protectedSymmetricKey = await crypto.subtle.encrypt({ name: "AES-CBC", iv: iv }, stretchedMasterKey, generatedSymmetricKey);

    const masterPasswordHash = await this.getMasterPasswordHash(email, password);
    
    // Transform the generatedSymmetricKey buffer into an actual CryptoKey to be used with the Cryptography class
    const key = await crypto.subtle.importKey("raw", generatedSymmetricKey, "AES-CBC", true, ["decrypt", "encrypt"]);
    this.cryptography = new Cryptography(key);

    // Firebase will need the master password hash used for authenticating
    // and the encrypted symmetric key
    return {
      masterPasswordHash: Base64.encode(masterPasswordHash),
      protectedSymmetricKey: Base64.encode(CryptoUtils.concatBufs(iv.buffer, protectedSymmetricKey))
    };
  }

  /**
   * Decrypt the protected symmetric key to obtain the symmetric key
   * required for encryption and decryption.
   * 
   * (Used after unlocking a vault and got access to stored data)
   */
  public static async loadProviderPsk(email: string, password: string, protectedSymmetricKey: string) {
    
    const stretchedMasterKey = await this.getStretchedMasterKey(email, password);

    const [iv, data] = CryptoUtils.split(Base64.decode(protectedSymmetricKey), 16);

    const symmetricKey = await crypto.subtle.decrypt({ name: "AES-CBC", iv: iv }, stretchedMasterKey, data);

    // Transform the symmetricKey buffer into an actual CryptoKey to be used with the Cryptography class
    const key = await crypto.subtle.importKey("raw", symmetricKey, "AES-CBC", true, ["decrypt", "encrypt"]);
    this.cryptography = new Cryptography(key);
  }

  public static async loadProviderPersistance() {
    this.cryptography = new Cryptography(null);
    this.cryptography.load();
  }
}

export class Cryptography {

  private initialized = false;
  private symmetricKey: CryptoKey | null = null;

  isInitialized = () => this.initialized;

  constructor(key: CryptoKey | null) {
    this.symmetricKey = key;
    this.save();
  }

  /**
   * Wrap the encryption key and store it for later use with firebase auth persistence.
   * Uses the firebase user's UID as the key material. 
   */
  private save() {
    if(this.symmetricKey == null) {
      return;
    }

    // Make sure the raw key data is 256 bits. Pad if necessary.
    const buf = CryptoUtils.string2Buf(window.UID);
    const pad = new Uint8Array(32 - buf.length);
    pad.fill(0);

    const padded = CryptoUtils.concatBufs(buf, pad);

    crypto.subtle.importKey("raw", padded, { name: "AES-CBC" }, false, ["wrapKey"]).then((wrapKey: CryptoKey) => {
      crypto.subtle.wrapKey(
        "raw", 
        this.symmetricKey!, 
        wrapKey, 
        { 
          name: "AES-CBC", 
          iv: new Uint8Array(16) 
        }
      ).then((wrapped: ArrayBuffer) => {
        saveKeyToStorage(Base64.encode(wrapped));
      });
    });
  }

  /**
   * Unwrap the stored encryption key.
   */
  load() {
    loadKeyFromStorage().then((key: string) => {

      // Make sure the raw key data is 256 bits. Pad if necessary.
      const buf = CryptoUtils.string2Buf(window.UID);
      const pad = new Uint8Array(32 - buf.length);
      pad.fill(0);

      const padded = CryptoUtils.concatBufs(buf, pad);

      crypto.subtle.importKey("raw", padded, { name: "AES-CBC" }, false, ["unwrapKey"]).then((unwrapKey: CryptoKey) => {
        crypto.subtle.unwrapKey(
          "raw",
          Base64.decode(key),
          unwrapKey,
          {
            name: "AES-CBC", 
            iv: new Uint8Array(16) 
          },
          CryptoUtils.privateKeyType,
          true,
          ["encrypt", "decrypt"]
        ).then((unwrapped: CryptoKey) => {
          this.symmetricKey = unwrapped;
        });
      });
    });
  }

  public getKey() {
    return this.symmetricKey;
  }

  public async encrypt(input: string) {

    if (this.symmetricKey === null) {
      console.error("NULL PRIVATE KEY");
      return;
    }

    const iv = crypto.getRandomValues(new Uint8Array(16));

    const algorithmParams = {
      name: "AES-CBC",
      iv: iv,
    }

    const result = await crypto.subtle.encrypt(algorithmParams, this.symmetricKey, CryptoUtils.string2Buf(input));

    // Add the IV to the data buffer to be used for decrypting. Represented by the first 16 bytes;
    const buf = CryptoUtils.concatBufs(iv.buffer, result);

    return Base64.encode(buf);
  }

  /**
   * @param input A base64 string or an ArrayBuffer
   */
  public async decrypt(input: string | ArrayBuffer) {

    if (this.symmetricKey === null) {
      console.error("NULL PRIVATE KEY");
      return;
    }

    let iv: any, data: any;

    // Get the counter for decrypting. Represented by the first 16 bytes.
    if (typeof input === "string") {
      [iv, data] = CryptoUtils.split(Base64.decode(input), 16);
    }
    else {
      [iv, data] = CryptoUtils.split(input, 16);
    }

    const algo = {
      name: "AES-CBC",
      iv: iv,
    }

    const result = await crypto.subtle.decrypt(algo, this.symmetricKey, data);

    return CryptoUtils.buf2String(result);
  }
}