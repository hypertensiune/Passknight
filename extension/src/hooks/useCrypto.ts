import { Cryptography } from "@lib/crypto";

const cryptoObject: Cryptography = new Cryptography();

/**
 * string for using the master password to initialiaze.
 * null for getting the instance.
 * undefined for loading the private key from db.
 */
export default function useCrypto(master: string | null | undefined): Cryptography {
  if (!cryptoObject.isInitialized() && master) {
    cryptoObject.init(master as string);
  }

  if (master === undefined) {
    cryptoObject.load();
  }

  return cryptoObject;
}