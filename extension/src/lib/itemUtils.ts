export function passwordItemToFirebase(item: PasswordItem) {
  return {
    [item.name]: {
      username: item.username, 
      website: item.website,
      password: item.password
    } 
  }
}

export function firebaseToPasswordItem(key: string, data: any) {
  return {
    name: key,
    website: data.website,
    username: data.username, 
    password: data.password
  } as PasswordItem;
}

export function noteItemToFirebase(item: NoteItem) {
  return {
    [item.name]: {
      content: item.content, 
    } 
  }
}

export function firebaseToNoteItem(key: string, data: any) {
  return {
    name: key,
    content: data,
  } as NoteItem;
}

export async function encryptPasswordItem(item: PasswordItem, encryptor: Function) {
  item.website = await encryptor(item.website);
  item.username = await encryptor(item.username);
  item.password = await encryptor(item.password);
}

export async function encryptNoteItem(item: NoteItem, encryptor: Function) {
  item.content = await encryptor(item.content);
}

export async function decryptPasswordItem(item: PasswordItem, decryptor: Function) {
  item.website = await decryptor(item.website);
  item.username = await decryptor(item.username);
  item.password = await decryptor(item.password);
}

export async function decryptNoteItem(item: NoteItem, decryptor: Function) {
  item.content = await decryptor(item.content);
}