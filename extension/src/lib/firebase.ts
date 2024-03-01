import { initializeApp } from "firebase/app";
import { getFirestore, setDoc, doc, getDoc, updateDoc, arrayUnion, arrayRemove } from "firebase/firestore";
import { UserCredential, browserSessionPersistence, createUserWithEmailAndPassword, getAuth, onAuthStateChanged, setPersistence, signInWithEmailAndPassword, signOut } from 'firebase/auth';

import useCrypto from "../hooks/useCrypto";

import { generateSalt } from "./crypto";
import { clearPersistence, loadPersistence, savePersistence } from "./extension";

import firebaseConfig from "./firebaseConfig.js";

const app = initializeApp(firebaseConfig);
const db = getFirestore();
const auth = getAuth(app);

let vaultsInfo: Map<string, string> = new Map();
let unlockedVaultID: string | undefined = undefined;

let subscriber = (_: any) => { };

declare global {
  interface Window {
    UID: string
  }
}

// Load firebase persistence info from extension's session storage.
// This has to be done explicitly before the onAuthStateChange is called so firebase
// can correctly detect the current state
(async () => {
  await loadPersistence(`firebase:authUser:${firebaseConfig.apiKey}:[DEFAULT]`);

  onAuthStateChanged(auth, async (user) => {  
    // get all the names of the available vaults
    const res = await getDoc(doc(db, "vaults", "ids"));
    const resObj = res.data() as Object;

    console.log("USER", user);

    window.UID = user?.uid || "";
    
    vaultsInfo = new Map(Object.entries(resObj));
  
    const data = Object.keys(resObj);
  
    data.sort();
  
    let options: any[] = [false];

    // if we are already logged in and there is no unlocked vault
    // get the unlocked vault from the user's email and set it to unlocked
    if (user && unlockedVaultID === undefined) {
      const name = user.email!.split('@')[0];
      unlockedVaultID = vaultsInfo.get(name);
      options = [true, name];
    }
  
    subscriber([data, ...options]);
  });
})();


export function subscribeForVaultsInfo(callback: (data: any) => void) {
  subscriber = callback;
}

export function isVaultUnlocked(): boolean {
  return unlockedVaultID !== undefined;
}

export function createVault(name: string, password: string, callback: Function) {
  const auth = getAuth(app);
  createUserWithEmailAndPassword(auth, `${name}@passknight.vault`, password).then(async (user: UserCredential) => {
    const salt = generateSalt();
    await setDoc(doc(db, "vaults", "ids"), { [name]: user.user.uid }, { merge: true });
    await setDoc(doc(db, "vaults", user.user.uid), { "salt": salt, "passwords": [], "notes": [], "history": [] });

    callback();
  });
}

export async function unlockVault(vault: string, password: string) {
  const auth = getAuth(app);

  let success = true;

  await setPersistence(auth, browserSessionPersistence);

  await signInWithEmailAndPassword(auth, `${vault}@passknight.vault`, password).catch(error => {
    if (error) {
      success = false;
    }
  });

  if(success) {
    unlockedVaultID = vaultsInfo?.get(vault);
    const data = window.sessionStorage.getItem(`firebase:authUser:${firebaseConfig.apiKey}:[DEFAULT]`);
    savePersistence(data);
  }

  return success;
}

export async function lockVault() {
  const auth = getAuth();
  await signOut(auth);

  clearPersistence();

  window.location.reload();
}

export async function getVaultContent(): Promise<VaultContent> {
  if (unlockedVaultID === undefined) {
    return { passwords: [], notes: [], history: [] };
  }

  const data = (await getDoc(doc(db, "vaults", unlockedVaultID))).data();
  return data as VaultContent;
}

export async function getSalt(): Promise<string> {
  if (unlockedVaultID === undefined) {
    return ""; 
  }

  const data = (await getDoc(doc(db, "vaults", unlockedVaultID))).data();
  return data?.salt;
}

function isPasswordItem(item: PasswordItem | NoteItem): item is PasswordItem {
  return 'password' in item;
}

export async function addItemToVault(item: PasswordItem | NoteItem): Promise<boolean> {
  if (unlockedVaultID === undefined) {
    return false;
  }

  const crypto = useCrypto(null);

  if (isPasswordItem(item)) {
    const encrypted = await crypto.encrypt(item.password);
    item.password = encrypted!;

    await updateDoc(doc(db, "vaults", unlockedVaultID), { passwords: arrayUnion(item) });
  }
  else {
    const encrypted = await crypto.encrypt(item.content);
    item.content = encrypted!;

    await updateDoc(doc(db, "vaults", unlockedVaultID), { notes: arrayUnion(item) });
  }

  return true;
}

export async function editItemInVault(oldItem: PasswordItem | NoteItem, newItem: PasswordItem | NoteItem): Promise<boolean> {
  if (unlockedVaultID === undefined) {
    return false;
  }

  const crypto = useCrypto(null);

  if (isPasswordItem(newItem) && isPasswordItem(oldItem)) {
    await updateDoc(doc(db, "vaults", unlockedVaultID), { passwords: arrayRemove(oldItem) });

    const encrypted = await crypto.encrypt(newItem.password);
    newItem.password = encrypted!;

    await updateDoc(doc(db, "vaults", unlockedVaultID), { passwords: arrayUnion(newItem) });
  }
  else if (!isPasswordItem(newItem) && !isPasswordItem(oldItem)) {
    await updateDoc(doc(db, "vaults", unlockedVaultID), { notes: arrayRemove(oldItem) });

    const encrypted = await crypto.encrypt(newItem.content);
    newItem.content = encrypted!;

    await updateDoc(doc(db, "vaults", unlockedVaultID), { notes: arrayUnion(newItem) });
  }

  return true;
}

export async function deleteItemFromVault(item: PasswordItem | NoteItem): Promise<boolean> {
  if (unlockedVaultID === undefined) {
    return false;
  }

  if (isPasswordItem(item)) {
    await updateDoc(doc(db, "vaults", unlockedVaultID), { passwords: arrayRemove(item) });
  }
  else {
    await updateDoc(doc(db, "vaults", unlockedVaultID), { notes: arrayRemove(item) });
  }

  return true;
}

export async function setGeneratorHistory(passwords: string[]) {
  if (unlockedVaultID === undefined) {
    return false;
  }

  await updateDoc(doc(db, "vaults", unlockedVaultID), { history: passwords });

  return true;
}