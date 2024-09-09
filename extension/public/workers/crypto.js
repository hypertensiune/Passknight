/**
 * The functions in this file are the same as in lib/crypto.ts
 *  to allow the background service worker to utilise the same functionalities.
 */

import * as Base64 from '../base64.js';

export class Crypto {

  #privateKey = null;

  #dec = new TextDecoder();
  #enc = new TextEncoder();

  /**
   * @param {string} keyMaterial The material used to derive a key for unwrapping the private symmetric key from storage
   */
  constructor(onLoaded) {
    this.#loadKeyFromStorage(onLoaded);
  }

  async decrypt(password) {
    if (this.#privateKey === null) {
      return;
    }
  
    const buf = Base64.decode(password);
  
    const iv = buf.slice(0, 16);
    const data = buf.slice(16);
  
    const res = await crypto.subtle.decrypt({ name: "AES-CBC", iv: iv }, this.#privateKey, data);
  
    return this.#dec.decode(res);
  }

  async #loadKeyFromStorage(onLoaded) {
    const res = await chrome.storage.session.get(["key"]);
    const key = res["key"];
  
    crypto.subtle.importKey("raw", Base64.decode(key), { name: "AES-CBC" }, true, ["encrypt", "decrypt"]).then(privateKey => {
      this.#privateKey = privateKey;
      onLoaded();
    });
  }
}