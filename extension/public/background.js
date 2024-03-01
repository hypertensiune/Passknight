chrome.runtime.onMessage.addListener((message, sender, res) => {
  if(message.action == "deleteClipboard") {
    setTimeout(() => {
      chrome.tabs.query({active: true}, (tabs) => {
        chrome.tabs.sendMessage(tabs[0].id, "deleteClipboard");
      });
    }, 10000);
  }
});