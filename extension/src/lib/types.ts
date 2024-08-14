type PasswordItem = {
  name: string,
  website: string,
  username: string,
  password: string,
}

type NoteItem = {
  name: string,
  content: string
}

type VaultContent = {
  passwords: PasswordItem[],
  notes: NoteItem[],
  history: string[]
}