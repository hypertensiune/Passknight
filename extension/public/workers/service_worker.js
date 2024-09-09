import { Crypto } from './crypto.js';

let currentContextMenuListener = null;

// Display the default context menu items when the browser is started
chrome.runtime.onStartup.addListener(() => {
  chrome.contextMenus.removeAll();
  chrome.contextMenus.create({
    title: "Passknight",
    contexts: ["all"],
    id: "PK_ROOT",
  });

  chrome.contextMenus.create({
    title: "Unlock a vault",
    contexts: ["all"],
    parentId: "PK_ROOT",
    id: "PK_1"
  });
});

chrome.runtime.onMessage.addListener(async (message, sender, res) => {
  if (message.action == "autofillServiceLoaded") {
    const tabs = await chrome.tabs.query({active: true, currentWindow: true});
    
    // Return if the message was not sent from the active tab
    if(!tabs || !tabs[0] || tabs[0].id != sender.tab.id) {
      return;
    }

    autofillInit(tabs[0], message.isAutofillPossible);
  }
});

async function autofillInit(tab, autofill) {
  const storage = await chrome.storage.session.get(["items"]);

  if("items" in storage) {
    const currentWebsite = tab.url.match(/https?:\/\/([^\/]*)/)?.at(1);
    
    const crypto = new Crypto(async () => {
      
      const filtered = await filterPasswordItems(storage["items"], currentWebsite, crypto);
      renderContextMenu(filtered, autofill, crypto);
      setMenuClickListener(filtered, crypto);

    });
  }
}

/**
 * @param {"multi" | "single"} type The type of the autofill
 * @param {string[]} fields Username and password in this order
 */
function sendAutofillRequest(type, ...fields) {
  chrome.tabs.query({active: true, currentWindow: true}, tabs => {
    if(type == "multi") {
      chrome.tabs.sendMessage(tabs[0].id, {action: "autofill", type: "multi", username: fields[0], password: fields[1]});
    } else if(type == "single") {
      chrome.tabs.sendMessage(tabs[0].id, {action: "autofill", type: "single", data: fields[0]});
    }
  });
}

async function renderContextMenu(items, autofill, crypto) {
  if (crypto == null) {
    console.warn("NULL CRYPTO");
    return
  }
  
  chrome.contextMenus.removeAll();
  chrome.contextMenus.create({
    title: "Passknight",
    contexts: ["all"],
    id: "PK_ROOT",
  });

  for (const index in items) {
    chrome.contextMenus.create({
      title: items[index].name,
      parentId: "PK_ROOT",
      contexts: ["all"],
      id: `PK_${index}`,
    });

    if(autofill) {
      chrome.contextMenus.create({
        title: "Autofill",
        contexts: ["all"],
        parentId: `PK_${index}`,
        id: `PK_${index}_A`
      })
    }

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
}

async function filterPasswordItems(passwordItems, currentWebsite, crypto) {

  // Return only the password items for the current web url
  let filtered = [];
  for (const item of passwordItems) {
    const website = await crypto.decrypt(item.website);
    if (website == currentWebsite) {
      filtered.push(item);
    }
  }

  filtered.sort((a, b) => a.name < b.name);

  return filtered;
}

function createContextMenuListener(filtered, crypto) {
  return async function(info, tab) {
    
    if(!info.menuItemId.startsWith("PK", 0)) {
      return;
    } 

    const split = info.menuItemId.split("_");
    const index = split[1];
    const type = split[2];

    const user = await crypto.decrypt(filtered[index].username);
    const pass = await crypto.decrypt(filtered[index].password);

    if(type == "A") {
      sendAutofillRequest("multi", user, pass);
    }
    else if(type == "U") {
      sendAutofillRequest("single", user);
    } else {
      sendAutofillRequest("single", pass);
    }
  }
}

async function setMenuClickListener(filtered, crypto) {
  chrome.contextMenus.onClicked.removeListener(currentContextMenuListener);
  currentContextMenuListener = createContextMenuListener(filtered, crypto);
  chrome.contextMenus.onClicked.addListener(currentContextMenuListener);
}