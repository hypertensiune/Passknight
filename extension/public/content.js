chrome.runtime.onMessage.addListener((message, sender, res) => {
  if (message.action == "deleteClipboard") {
    navigator.clipboard.writeText("");
  }
});

chrome.runtime.onMessage.addListener((message, sender, res) => {
  if (message.action == "autofill") {
    console.log("autofill", message.data);
    pk_contextmenuTarget.value = message.data;
  }
});

let pk_contextmenuTarget = null;

document.addEventListener('contextmenu', (evt) => {
  pk_contextmenuTarget = evt.target;
});