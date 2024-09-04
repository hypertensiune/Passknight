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

// Display the default context menu items when the browser is started
chrome.runtime.onStartup.addListener(() => {
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
});

async function sortPasswordItems(passwordItems) {

  // Group the items by the website property
  let grouped = {};
  for(const item of passwordItems) {
    const website = await decrypt(item.website);
    if(grouped.hasOwnProperty(website)) {
      grouped[website].push(item);
    }
    else {
      grouped[website] = [item];
    }
  }

  // Sort the grouped items by website in alphabetical order
  // https://stackoverflow.com/a/51725400
  const sorted = Object.keys(grouped).sort().reduce((a, c) => (a[c] = grouped[c], a), {});

  return sorted;
}

async function renderContextMenuItems(items) {

  const sorted = await sortPasswordItems(items);

  chrome.contextMenus.removeAll();
  chrome.contextMenus.create({
    title: "Passknight",
    contexts: ["editable"],
    id: "PK_ROOT",
  });

  for(const [key, value] of Object.entries(sorted)) {

    chrome.contextMenus.create({
      title: key,
      parentId: "PK_ROOT",
      contexts: ["editable"],
      id: `PK_${key}`
    });

    // If there is only one password saved for this website show it directly
    if(value.length == 1) {
      chrome.contextMenus.create({
        title: "Fill username",
        contexts: ["editable"],
        parentId: `PK_${key}`,
        id: `PK_${key}_0_U`,
      });
  
      chrome.contextMenus.create({
        title: "Fill password",
        contexts: ["editable"],
        parentId: `PK_${key}`,
        id: `PK_${key}_0_P`,
      });
    }
    else {
      // Show all saved passwords under the this website's menu
      for(const [index, item] of value.entries()) {
        chrome.contextMenus.create({
          title: item.name,
          contexts: ["editable"],
          parentId: `PK_${key}`,
          id: `PK_${key}_${index}`,
        });
    
        chrome.contextMenus.create({
          title: "Fill username",
          contexts: ["editable"],
          parentId: `PK_${key}_${index}`,
          id: `PK_${key}_${index}_U`,
        });
    
        chrome.contextMenus.create({
          title: "Fill password",
          contexts: ["editable"],
          parentId: `PK_${key}_${index}`,
          id: `PK_${key}_${index}_P`,
        });
      }
    }
  }

  chrome.contextMenus.onClicked.addListener(async (info, tab) => {
    if(!info.menuItemId.startsWith("PK", 0)) {
      return;
    }

    const split = info.menuItemId.split("_");

    let url = split[1];
    let index = parseInt(split[2]);
    let type = split[3];


    if(type == "U") {
      const dec = await decrypt(sorted[url][index].username);
      autofill(dec);
    }
    else if(type == "P") {
      const dec = await decrypt(sorted[url][index].password);
      autofill(dec);
    }
  }); 
}