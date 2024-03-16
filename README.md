<h1 align="center">
    <img center src="extension/src/assets/logo.svg">
</h1>

<h3 align="center">Open source self hosted password manager.</h3>

## Description

<p>Passknight is a cross-platform, self hosted password manager.</p> 

<p>It supports multiple users / vaults, making it easy to organize your passwords and notes.</p>

<p>Easy to setup a secure environment for your whole family.</p>

### Current supported platforms
- Browser ( extension ) - Chromium based browsers, Firefox
- Windows - **WIP**


## Security

All of your passwords are encrypted before being stored in firebase. 

A Passknight vault is represented by a user in firebase. The password used to authentificate in firebase and get the corresponding vault is the vault's masterpassword. 

#### Private key

The **masterpassword** is then used to derive a private key unique to that vault that will encrypt and decrypt the vault content.

The derivation proccess is done using **600,000** iterations of **PBKDF2** with **SHA-256** and a randomly generated salt.

#### Encryption & Decryption

To encrypt and decrypt your passwords, Passknigth uses the **AES-CTR** algorithm with a randomly generated 16 bytes counter.

The first 16 bytes of the stored buffer is represented by the counter.

#### Auth persistence - only in browser extension

To be able to use firebase's auth persistence Passknight encrypts the private key (using [crypto.subtle.wrapKey](https://developer.mozilla.org/en-US/docs/Web/API/SubtleCrypto/wrapKey)) and stores it in session storage.

The key used for this encryption is imported from the firebase user UID (which is guaranteed to be unique for each application) using the **AES-CTR** algorithm.

#### Cryptography libraries

- Browser extension - [WebCrypto API](https://developer.mozilla.org/en-US/docs/Web/API/Web_Crypto_API)


## Setup

### If you want to build the Windows app yourself see [...]

### Firebase setup is required for the browser extension and optional for the desktop application.

- Login to [firebase](firebase.com) and create a new project.
- Register a **web app**. 
- Go to **authentification** and add the <u>Email/Password</u> provider.
- Enable **Firestore Database** and add the following rules in the ```rules``` tab:
```
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    match /vaults/ids {
    	allow write, read;
    }
    
    match /vaults/{vault} {
    	allow write, read: if request.auth != null && request.auth.uid == vault
    }
  }
}
```
<br><br>

### Browser extension

To initialize the Firebase create a file called ```firebaseConfig.js``` in the extension's folder that should look like this:
```
const firebaseConfig = {
    apiKey: "",
    authDomain: "",
    projectId: "",
    storageBucket: "",
    messagingSenderId: "",
    appId: ""
};
window.firebaseConfig = firebaseConfig;
```
Copy your config from **Project settings > General**.

<br>

### Desktop application - WIP