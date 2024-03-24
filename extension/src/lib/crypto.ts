import * as Base64 from "./base64.ts";

import { saveKeyToStorage, loadKeyFromStorage } from "./extension.ts";
import { getSalt } from "./firebase.ts";

export function generateSalt(): string {
  const arr = crypto.getRandomValues(new Uint8Array(32));

  return Base64.encode(arr);
}

// TO DO: Change to AES-CBC mode.
export class Cryptography {
  private initialized = false;

  private privateKey: CryptoKey | null = null;
  private iterations = 600000;

  private enc = new TextEncoder();
  private dec = new TextDecoder();

  private privateKeyType = {
    name: "AES-CBC",
    length: 256
  };

  string2Buf(input: string) {
    return this.enc.encode(input);
  }

  buf2String(input: ArrayBuffer) {
    return this.dec.decode(input);
  }

  buf2hex(input: ArrayBuffer) {
    return [...new Uint8Array(input)].map(x => x.toString(16).padStart(2, '0')).join('');
  }

  buf2Base64(input: ArrayBuffer) {
    return [...new Uint8Array(input)].map(x => x.toString(64)).join('');
  }

  concatBufs(a: ArrayBuffer, b: ArrayBuffer) {
    const c = new Uint8Array(a.byteLength + b.byteLength);
    c.set(new Uint8Array(a), 0);
    c.set(new Uint8Array(b), a.byteLength);

    return c.buffer;
  }

  isInitialized = () => this.initialized;

  init(masterpassword: string) {
    this.importKey(this.string2Buf(masterpassword)).then(async (key: CryptoKey) => {

      // The salt is randomly generated when the vault is created and stored in the vault
      // Get it from the vault to derive the key
      const salt = await getSalt();
      this.deriveKey(key, this.string2Buf(salt)).then((key: CryptoKey) => {
        this.privateKey = key;
        this.initialized = true;
        this.save();
      });
    });
  }

  /**
   * Wrap the encryption key and store it for later use with firebase auth persistence.
   * Uses the firebase user's UID as the key material. 
   */
  private save() {

    // Make sure the raw key data is 256 bits. Pad if necessary.
    const buf = this.string2Buf(window.UID);
    const pad = new Uint8Array(32 - buf.length);
    pad.fill(0);

    const padded = this.concatBufs(buf, pad);

    crypto.subtle.importKey("raw", padded, { name: "AES-CBC" }, false, ["wrapKey"]).then((wrapKey: CryptoKey) => {
      crypto.subtle.wrapKey(
        "raw", 
        this.privateKey!, 
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
      const buf = this.string2Buf(window.UID);
      const pad = new Uint8Array(32 - buf.length);
      pad.fill(0);

      const padded = this.concatBufs(buf, pad);

      crypto.subtle.importKey("raw", padded, { name: "AES-CBC" }, false, ["unwrapKey"]).then((unwrapKey: CryptoKey) => {
        crypto.subtle.unwrapKey(
          "raw",
          Base64.decode(key),
          unwrapKey,
          {
            name: "AES-CBC", 
            iv: new Uint8Array(16) 
          },
          this.privateKeyType,
          true,
          ["encrypt", "decrypt"]
        ).then((unwrapped: CryptoKey) => {
          this.privateKey = unwrapped;
          console.log("unwrapped");
        });
      });
    });
  }

  public getKey() {
    return this.privateKey;
  }

  private async importKey(keyData: ArrayBuffer): Promise<CryptoKey> {
    return await crypto.subtle.importKey("raw", keyData, "PBKDF2", false, ["deriveKey"]);
  }

  private async deriveKey(baseKey: CryptoKey, salt: ArrayBuffer): Promise<CryptoKey> {
    const algorithmParams = {
      name: "PBKDF2",
      hash: "SHA-256",
      salt: salt,
      iterations: this.iterations
    }

    return await crypto.subtle.deriveKey(algorithmParams, baseKey, this.privateKeyType, true, ["encrypt", "decrypt"]);
  }

  /**
   * Split the input buffer in the actual data and the IV used for encryption. 
   * The IV is the first bytes in the buffer. 
   */
  private split(input: ArrayBuffer, bytes: number) {
    const arr = new Uint8Array(input);
    return [arr.slice(0, bytes), arr.slice(bytes)];
  }

  public async encrypt(input: string) {

    if (this.privateKey === null) {
      console.error("NULL PRIVATE KEY");
      return;
    }

    const iv = crypto.getRandomValues(new Uint8Array(16));

    const algorithmParams = {
      name: "AES-CBC",
      iv: iv,
    }

    const result = await crypto.subtle.encrypt(algorithmParams, this.privateKey, this.string2Buf(input));

    // Add the IV to the data buffer to be used for decrypting. Represented by the first 16 bytes;
    const buf = this.concatBufs(iv.buffer, result);

    return Base64.encode(buf);
  }

  /**
   * @param input A base64 string or an ArrayBuffer
   */
  public async decrypt(input: string | ArrayBuffer) {

    if (this.privateKey === null) {
      console.error("NULL PRIVATE KEY");
      return;
    }

    let iv: any, data: any;

    // Get the counter for decrypting. Represented by the first 16 bytes.
    if (typeof input === "string") {
      [iv, data] = this.split(Base64.decode(input), 16);
    }
    else {
      [iv, data] = this.split(input, 16);
    }

    const algo = {
      name: "AES-CBC",
      iv: iv,
    }

    const result = await crypto.subtle.decrypt(algo, this.privateKey, data);

    return this.buf2String(result);
  }
}