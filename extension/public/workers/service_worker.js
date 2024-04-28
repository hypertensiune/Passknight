import { decrypt, loadKeyFromStorage } from './crypto.js';

chrome.runtime.onMessage.addListener((message, sender, res) => {
  if(message.action == "deleteClipboard") {
    setTimeout(() => {
      chrome.tabs.query({active: true}, (tabs) => {
        chrome.tabs.sendMessage(tabs[0].id, {action: "deleteClipboard"});
      });
    }, 10000);
  }
});

chrome.runtime.onMessage.addListener((message, sender, res) => {
  if(message.action == "renderContextMenuItems") {
    renderContextMenuItems(message.data);
  }
});

chrome.runtime.onMessage.addListener((message, sender, res) => {
  if(message.action == "loadKeyFromStorage") {
    loadKeyFromStorage(message.UID);
  }
});

function autofill(data) {
  chrome.tabs.query({active: true}, async (tabs) => {
    chrome.tabs.sendMessage(tabs[0].id, {action: "autofill", data: data});
  });
}

chrome.contextMenus.create({
  title: "Passknight",
  contexts: ["editable"],
  id: "PK_ROOT",
  onclick: autofill("data")
});

function renderContextMenuItems(passwordItems) {
  chrome.contextMenus.removeAll();

  chrome.contextMenus.create({
    title: "Passknight",
    contexts: ["editable"],
    id: "PK_ROOT",
  });

  for(const [index, item] of passwordItems.entries()) {

    chrome.contextMenus.create({
      title: item.name,
      contexts: ["editable"],
      parentId: "PK_ROOT",
      id: `PK_${index}`,
    });

    chrome.contextMenus.create({
      title: "Fill username",
      contexts: ["editable"],
      parentId: `PK_${index}`,
      id: `PK_${index}_U`,
    });

    chrome.contextMenus.create({
      title: "Fill password",
      contexts: ["editable"],
      parentId: `PK_${index}`,
      id: `PK_${index}_P`,
    });
  }

  chrome.contextMenus.onClicked.addListener(async (info, tab) => {
    if(!info.menuItemId.startsWith("PK", 0)) {
      return;
    }

    const split = info.menuItemId.split("_");

    let index = parseInt(split[1]);
    let type = split[2];

    if(type == "U") {
      autofill(passwordItems[index].username);
    }
    else if(type == "P") {
      const dec = await decrypt(passwordItems[index].password);
      autofill(dec);
    }
  }); 
}