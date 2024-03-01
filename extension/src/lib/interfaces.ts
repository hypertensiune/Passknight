interface PasswordItem {
  name: string,
  website: string,
  username: string,
  password: string,
}

interface NoteItem {
  name: string,
  content: string
}

interface VaultContent {
  passwords: PasswordItem[],
  notes: NoteItem[],
  history: string[]
}