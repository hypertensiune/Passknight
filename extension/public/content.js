(function() {
  let pkctxmenu_singleField = null;
  let pkctxmenu_multiField = { user: null, pass: null };
  
  chrome.runtime.onMessage.addListener((message, sender, res) => {
    if (message.action == "deleteClipboard") {
      navigator.clipboard.writeText("");
    }
  });
  
  chrome.runtime.onMessage.addListener((message, sender, res) => {
    if (message.action == "autofill") {
      
      if(message.type == "multi") {
        
        if(pkctxmenu_multiField.user == null || pkctxmenu_multiField.pass == null) {
          alert("Passknight - no fields detected for autofill!");
          return;
        }

        pkctxmenu_multiField.user.value = message.username;
        pkctxmenu_multiField.pass.value = message.password;

      } else {
        pkctxmenu_singleField.value = message.data;
      }
    }
  });

  document.addEventListener('contextmenu', (evt) => {
  
    // If the user clicked directly on an input field autofill only that one
    if(evt.target.nodeName == "INPUT") {
      pkctxmenu_singleField = evt.target;
      return;
    }

    const inputs = document.querySelectorAll("input");

    for(const input of inputs) {
      if(input.autocomplete === 'off') {
        continue;
      }
      
      // If a input has autocomplete specified use that to determine 
      // if it should be autofilled and with what.
      // If there is no autofill check the field's type
      // and finally it's name or placeholder
      if(input.autocomplete.includes('username')) {
        pkctxmenu_multiField.user = input;
      }
      
      if(input.autocomplete.includes('password')) {
        pkctxmenu_multiField.pass = input;
      }

      if(pkctxmenu_multiField.user == null && input.type === 'username') {
        pkctxmenu_multiField.user = input;
      }

      if(pkctxmenu_multiField.user == null && input.type === 'password') {
        pkctxmenu_multiField.user = input;
      }

      if(pkctxmenu_multiField.user == null && (input.name.includes('username') || input.placeholder.includes('username'))) {
        pkctxmenu_multiField.user = input;
      }

      if(pkctxmenu_multiField.pass == null && (input.name.includes('password') || input.placeholder.includes('password'))) {
        pkctxmenu_multiField.pass = input;
      }
    }
  });

})();