type PasswordItem = {
  name: string,
  website: string,
  username: string,
  password: string,
  created: string,
  updated: string
}

type NoteItem = {
  name: string,
  content: string,
  created: string,
  updated: string
}

type VaultContent = {
  passwords: PasswordItem[],
  notes: NoteItem[],
  history: string[]
}