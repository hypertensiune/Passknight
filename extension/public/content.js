let pk_contextmenuTarget = null;

chrome.runtime.onMessage.addListener((message, sender, res) => {
  if (message.action == "deleteClipboard") {
    navigator.clipboard.writeText("");
  }
});

chrome.runtime.onMessage.addListener((message, sender, res) => {
  if (message.action == "autofill" && pk_contextmenuTarget != null) {
    pk_contextmenuTarget.value = message.data;
  }
});

document.addEventListener('contextmenu', (evt) => {
  pk_contextmenuTarget = evt.target;
});