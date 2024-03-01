chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message === "deleteClipboard") {
        navigator.clipboard.writeText("");
    }
});