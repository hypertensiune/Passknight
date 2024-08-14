import { initializeApp } from "firebase/app";
import {
  getFirestore,
  setDoc,
  doc,
  getDoc,
  updateDoc,
  deleteDoc,
  deleteField,
  getDocs,
  query,
  collection
} from "firebase/firestore";

import {
  User,
  UserCredential,
  browserSessionPersistence, 
  createUserWithEmailAndPassword, 
  deleteUser, getAuth, 
  onAuthStateChanged, 
  setPersistence, 
  signInWithEmailAndPassword, 
  signOut
} from 'firebase/auth';

import { clearPersistence, loadPersistence, savePersistence } from "./extension";

import * as Converters from "./itemConverters.js";

import firebaseConfig from "./firebaseConfig.js";

const app = initializeApp(firebaseConfig);
const db = getFirestore();
const auth = getAuth(app);

let vaultsInfo: Map<string, string> = new Map();
let unlockedVaultID: string | undefined = undefined;

let subscriber = (_: any) => { };

let currentUser: User | null = null;

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

export async function createVault(name: string, password: string, symmetricKey: string): Promise<boolean> {
  const auth = getAuth(app);

  try {
    const user = await createUserWithEmailAndPassword(auth, `${name}@passknight.vault`, password);

    // await setDoc(doc(db, "vaults", "ids"), { [name.toLowerCase()]: user.user.uid }, { merge: true });
    // await setDoc(doc(db, "vaults", user.user.uid), { "psk": symmetricKey, "passwords": [], "notes": [], "history": [] });

    await setDoc(doc(db, "vaults", "ids"), { [name.toLowerCase()]: user.user.uid }, { merge: true });

    await setDoc(doc(db, user.user.uid, "psk"), { "psk": symmetricKey });
    await setDoc(doc(db, user.user.uid, "passwords"), {});
    await setDoc(doc(db, user.user.uid, "notes"), {});

    currentUser = user.user;
    unlockedVaultID = user.user.uid;

    const data = window.sessionStorage.getItem(`firebase:authUser:${firebaseConfig.apiKey}:[DEFAULT]`);
    savePersistence(data);

    return true

  } catch (exception) {
    return false
  }
}

/**
 * Delete the currently unlocked vault.
 */
export async function deleteVault(vault: string) {
  if (currentUser != null) {
    await deleteDoc(doc(db, "vaults", unlockedVaultID!));
    await deleteUser(currentUser);
    await updateDoc(doc(db, "vaults", "ids"), { [vault]: deleteField() });
  }
}

export async function unlockVault(vault: string, password: string) {
  const auth = getAuth(app);

  let success = true;

  await setPersistence(auth, browserSessionPersistence);

  await signInWithEmailAndPassword(auth, `${vault}@passknight.vault`, password)
    .then((user: UserCredential) => currentUser = user.user)
    .catch(error => {
      if (error) {
        success = false;
      }
    });

  if (success) {
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
  // const data = (await getDoc(doc(db, "vaults", unlockedVaultID))).data();
  // return data as VaultContent;

  const data = await getDocs(query(collection(db, unlockedVaultID)));
  
  let passwords: PasswordItem[] = [], notes: NoteItem[] = [];
  
  data.forEach(doc => {
    const docdata = doc.data();
    switch(doc.id) {
      case "passwords": 
        passwords = Object.keys(docdata).map(key => Converters.FirebaseToPasswordItem(key, docdata[key]));
        break;
      case "notes": 
        notes = Object.keys(docdata).map(key => Converters.FirebaseToNoteItem(key, docdata[key]));
        break;
    }
  });
  console.log(passwords);
  return { passwords: passwords, notes: notes, history: [] };
}

export async function getVaultPsk(): Promise<string> {
  if (unlockedVaultID === undefined) {
    return "";
  }

  const data = (await getDoc(doc(db, unlockedVaultID, "psk"))).data();
  return data?.psk;
}

function isPasswordItem(item: PasswordItem | NoteItem): item is PasswordItem {
  return 'password' in item;
}

export async function addItemToVault(item: PasswordItem | NoteItem): Promise<boolean> {
  if (unlockedVaultID === undefined) {
    return false;
  }

  if (isPasswordItem(item)) {
    await updateDoc(doc(db, unlockedVaultID, "passwords"), Converters.PasswordItemToFirebase(item));
  }
  else {
    await updateDoc(doc(db, "vaults", unlockedVaultID), Converters.NoteItemToFirebase(item));
  }

  return true;
}

export async function editItemInVault(oldItem: PasswordItem | NoteItem, newItem: PasswordItem | NoteItem): Promise<boolean> {
  if (unlockedVaultID === undefined) {
    return false;
  }

  if (isPasswordItem(newItem) && isPasswordItem(oldItem)) {
    addItemToVault(newItem);
    if(newItem.name != oldItem.name) {
      deleteItemFromVault(oldItem);
    }
  }
  else if (!isPasswordItem(newItem) && !isPasswordItem(oldItem)) {
    addItemToVault(newItem);
    if(newItem.name != oldItem.name) {
      deleteItemFromVault(oldItem);
    }
  }

  return true;
}

export async function deleteItemFromVault(item: PasswordItem | NoteItem): Promise<boolean> {
  if (unlockedVaultID === undefined) {
    return false;
  }

  if (isPasswordItem(item)) {
    await updateDoc(doc(db, unlockedVaultID, "passwords"), { [item.name]: deleteField() });
  }
  else {
    await updateDoc(doc(db, unlockedVaultID, "notes"), { [item.name]: deleteField() });
  }

  return true;
}

export async function setGeneratorHistory(passwords: string[]) {
  if (unlockedVaultID === undefined) {
    return false;
  }
  if(passwords.length > 0) {
    return false;
  }

  await updateDoc(doc(db, unlockedVaultID, "notes"), { history: passwords });

  return true;
}