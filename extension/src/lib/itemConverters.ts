export function PasswordItemToFirebase(item: PasswordItem) {
  return {
    [item.name]: {
      username: item.username, 
      website: item.website,
      password: item.password
    } 
  }
}

export function FirebaseToPasswordItem(key: string, data: any) {
  return {
    name: key,
    website: data.website,
    username: data.username, 
    password: data.password
  } as PasswordItem;
}

export function NoteItemToFirebase(item: NoteItem) {
  return {
    [item.name]: {
      content: item.content, 
    } 
  }
}

export function FirebaseToNoteItem(key: string, data: any) {
  return {
    name: key,
    content: data,
  } as NoteItem;
}