/**
 * 
 * The functions in this file are the same as in lib/crypto.ts
 * Copied to allow the background service worker to utilise the same functionalities.
 * 
 */

import { decode } from "../base64.js";

let privateKey = null;

const dec = new TextDecoder();
const enc = new TextEncoder();

export async function loadKeyFromStorage(base) {
  const res = await chrome.storage.session.get(["key"]);

  const key = res["key"];

  // Make sure the raw key data is 256 bits. Pad if necessary
  const buf = enc.encode(base);
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
      decode(key),
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
      privateKey = unwrapped;
    });
  });
}

export async function decrypt(password) {
  if (privateKey === null) {
    return;
  }

  const buf = decode(password);

  const iv = buf.slice(0, 16);
  const data = buf.slice(16);

  const res = await crypto.subtle.decrypt({ name: "AES-CBC", iv: iv }, privateKey, data);

  return dec.decode(res);
}