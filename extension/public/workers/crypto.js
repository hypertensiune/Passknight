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
  constructor(keyMaterial, onLoaded) {
    this.#loadKeyFromStorage(keyMaterial, onLoaded);
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

  async #loadKeyFromStorage(keyMaterial, onLoaded) {
    const res = await chrome.storage.session.get(["key"]);
  
    const key = res["key"];
  
    // Make sure the raw key data is 256 bits. Pad if necessary
    const buf = this.#enc.encode(keyMaterial);
    const pad = new Uint8Array(32 - buf.length);
    pad.fill(0);
  
    // concat the buffers
    const tmp = new Uint8Array(buf.byteLength + pad.byteLength);
    tmp.set(new Uint8Array(buf), 0);
    tmp.set(new Uint8Array(pad), buf.byteLength);
  
    const padded = tmp.buffer;
  
    crypto.subtle.importKey("raw", padded, { name: "AES-CBC" }, false, ["unwrapKey"]).then(unwrapKey => {
      crypto.subtle.unwrapKey(
        "raw",
        Base64.decode(key),
        unwrapKey,
        {
          name: "AES-CBC",
          iv: new Uint8Array(16)
        },
        {
          name: "AES-CBC",
          length: 256
        },
        true,
        ["encrypt", "decrypt"]
      ).then(unwrapped => {
        this.#privateKey = unwrapped;
        onLoaded();
      });
    });
  }
}