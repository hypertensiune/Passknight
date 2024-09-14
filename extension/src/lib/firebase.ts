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

import * as Converters from "./itemUtils.js";

import firebaseConfig from "./firebaseConfig.js";

const app = initializeApp(firebaseConfig);
const db = getFirestore();
const auth = getAuth(app);

let vaultsInfo: Map<string, string> = new Map();
let unlockedVaultID: string | undefined = undefined;

let currentUser: User | null = null;

// Load firebase persistence info from extension's session storage.
// This has to be done explicitly before the onAuthStateChange is called so firebase
// can correctly detect the current state
export async function firebaseInit(callback: (data: any) => void) {
  await loadPersistence(`firebase:authUser:${firebaseConfig.apiKey}:[DEFAULT]`);

  onAuthStateChanged(auth, async (user) => {
    // get all the names of the available vaults
    const res = await getDoc(doc(db, "vaults", "ids"));
    const resObj = res.data() as Object;

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
    callback([data, ...options]);
  });
}

export function isVaultUnlocked(): boolean {
  return unlockedVaultID !== undefined;
}

export async function createVault(name: string, password: string, symmetricKey: string): Promise<boolean> {
  const auth = getAuth(app);

  try {
    const user = await createUserWithEmailAndPassword(auth, `${name}@passknight.vault`, password);

    await setDoc(doc(db, "vaults", "ids"), { [name.toLowerCase()]: user.user.uid }, { merge: true });

    await setDoc(doc(db, user.user.uid, "psk"), { "psk": symmetricKey });
    await setDoc(doc(db, user.user.uid, "passwords"), {});
    await setDoc(doc(db, user.user.uid, "notes"), {});
    await setDoc(doc(db, user.user.uid, "history"), { "history": [] });

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
    await updateDoc(doc(db, "vaults", "ids"), { [vault]: deleteField() });

    await deleteDoc(doc(db, unlockedVaultID!, "psk"));
    await deleteDoc(doc(db, unlockedVaultID!, "passwords"));
    await deleteDoc(doc(db, unlockedVaultID!, "notes"));
    await deleteDoc(doc(db, unlockedVaultID!, "history"));

    await deleteUser(currentUser);
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

  await clearPersistence();

  window.location.reload();
}

export async function getVaultContent(): Promise<VaultContent> {
  if (unlockedVaultID === undefined) {
    return { passwords: [], notes: [], history: [] };
  }

  const data = await getDocs(query(collection(db, unlockedVaultID)));
  
  let passwords: PasswordItem[] = [], notes: NoteItem[] = [], history: string[] = [];
  
  data.forEach(doc => {
    const docdata = doc.data();
    switch(doc.id) {
      case "passwords": 
        passwords = Object.keys(docdata).map(key => Converters.firebaseToPasswordItem(key, docdata[key]));
        break;
      case "notes": 
        notes = Object.keys(docdata).map(key => Converters.firebaseToNoteItem(key, docdata[key]));
        break;
      case "history":
        history = docdata["history"]
        break;
    }
  });

  return { passwords: passwords, notes: notes, history: history };
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
    await updateDoc(doc(db, unlockedVaultID, "passwords"), Converters.passwordItemToFirebase(item));
  }
  else {
    await updateDoc(doc(db, unlockedVaultID, "notes"), Converters.noteItemToFirebase(item));
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
  if(passwords.length == 0) {
    return false;
  }

  await updateDoc(doc(db, unlockedVaultID, "history"), { history: passwords });

  return true;
}