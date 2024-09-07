export function getCurrentActiveWebsite(callback: Function) {
  chrome.tabs.query({ active: true, currentWindow: true }, (tabs: any) => {
    const url = tabs[0].url as string;
    const website = url.match(/https?:\/\/([^\/]*)/)?.at(1);
    callback(website);
  });
}

export function clipboardDeleteCommand() {
  chrome.runtime.sendMessage({action: "deleteClipboard"});
}

export function savePersistence(data: any) {
  chrome.storage.session.set({firebase: data});
}

export async function loadPersistence(key: string) {
  const res = await chrome.storage.session.get(["firebase"]);
  
  if(res["firebase"]) {
    window.sessionStorage.setItem(key, res["firebase"]);
  }
}

export async function clearPersistence() {
  await chrome.storage.session.remove(["firebase", "key", "items"]);

  // When a user is signed out and the firebase persistence is cleared, remove all the context menu items
  removeContextMenus();
}

export function saveKeyToStorage(key: string) {
  chrome.storage.session.set({"key": key});
}

export async function loadKeyFromStorage() {
  const res = await chrome.storage.session.get(["key"]);
  return res["key"] || "";
}

export async function autofillInit(items: PasswordItem[]) {
  await chrome.storage.session.set({items: items, keyMaterial: window.UID});
  chrome.runtime.sendMessage({action: "autofillInit"});
}

function removeContextMenus() {
  chrome.contextMenus.removeAll();
  chrome.contextMenus.create({
    title: "Passknight",
    contexts: ["editable"],
    id: "PK_ROOT",
  });
  
  chrome.contextMenus.create({
    title: "Unlock a vault",
    contexts: ["editable"],
    parentId: "PK_ROOT",
    id: "PK_1"
  });
}