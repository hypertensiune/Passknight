export function getCurrentActiveWebsite(callback: Function) {
  chrome.tabs.query({ active: true, currentWindow: true }, (tabs: any) => {
    const url = tabs[0].url as string;
    const website = url.match(/https?:\/\/([^\/]*)/)?.at(1);
    callback(website);
  });

  //callback("Netflix");
}

export function clipboardDeleteCommand() {
  chrome.runtime.sendMessage({action: "deleteClipboard"});
}

export function savePersistence(data: any) {
  chrome.storage.session.set({firebase: data});
}

export async function loadPersistence(key: string) {
  const res = await chrome.storage.session.get(["firebase"]);
  
  if(res["firebase"])  
    window.sessionStorage.setItem(key, res["firebase"]);
}

export function clearPersistence() {
  chrome.storage.session.remove(["firebase", "key"]);
}

export function saveKeyToStorage(key: string) {
  chrome.storage.session.set({"key": key});
}

export async function loadKeyFromStorage() {
  const res = await chrome.storage.session.get(["key"]);
  return res["key"] || "";
}