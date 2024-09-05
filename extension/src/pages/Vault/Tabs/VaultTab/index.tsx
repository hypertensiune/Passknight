import { useState } from 'react';
import { useDisclosure } from '@mantine/hooks'

import { List, PasswordListItem, EditForm, AddForm, NoteListItem } from '@VaultComponents';

function changeItem<T>(newItem: T, itemList: T[], index: number, setItemList: React.Dispatch<React.SetStateAction<T[]>>) {
  const newItemList = [...itemList];
  newItemList[index] = newItem;
  setItemList(newItemList);
}

function deleteItem<T>(itemList: T[], index: number, setItemList: React.Dispatch<React.SetStateAction<T[]>>) {
  const newItemList = [...itemList];
  newItemList.splice(index, 1);
  setItemList(newItemList);
}

export default function Vault({ data }: { data: VaultContent }) {

  const [addFormOpened, addFormHandlers] = useDisclosure(false);

  const [passwordList, setPasswordList] = useState<PasswordItem[]>(data.passwords);
  const [noteList, setNoteList] = useState<NoteItem[]>(data.notes);

  const [selectedPassword, setSelectedPassword] = useState(-1);
  const [selectedNote, setSelectedNote] = useState(-1);

  const editFormHandlers = useDisclosure(true)[1];

  return (
    <>
      <div className="vault" style={{ height: '55vh', overflow: 'scroll', maskImage: 'linear-gradient(to bottom, black calc(100% - 48px), transparent 100%)', paddingBottom: '5vh' }}>
        <List>
          {passwordList.map((d: PasswordItem, index: number) => <PasswordListItem data={d} onClick={() => setSelectedPassword(index)} />)}
        </List>
        <List>
          {noteList.map((n: NoteItem, index: number) => <NoteListItem data={n} onClick={() => setSelectedNote(index)} />)}
        </List>
        <AddForm 
          opened={addFormOpened} 
          close={addFormHandlers.close}
          passwordItems={passwordList}
          noteItems={noteList}
          addNewItem={(newItem: PasswordItem | NoteItem) => {
            if ("password" in newItem) {
              setPasswordList([...passwordList, newItem]);
            } else {
              setNoteList([...noteList, newItem]);
            }
          }} 
        />
        {selectedPassword > -1 &&
          <EditForm
            item={{...passwordList[selectedPassword]}}
            items={passwordList}
            opened={true}
            close={() => {
              setSelectedPassword(-1);
              editFormHandlers.close();
            }}
            changeItem={(newItem: PasswordItem | NoteItem) => changeItem<PasswordItem>(newItem as PasswordItem, passwordList, selectedPassword, setPasswordList)}
            deleteItem={() => deleteItem<PasswordItem>(passwordList, selectedPassword, setPasswordList)}
          />
        }
        {selectedNote > -1 &&
          <EditForm
            item={{...noteList[selectedNote]}}
            items={noteList}
            opened={true}
            close={() => {
              setSelectedNote(-1);
              editFormHandlers.close();
            }}
            changeItem={(newItem: PasswordItem | NoteItem) => changeItem<NoteItem>(newItem as NoteItem, noteList, selectedNote, setNoteList)}
            deleteItem={() => deleteItem<NoteItem>(noteList, selectedNote, setNoteList)}
          />
        }
      </div>
      <div>
        <div className="add-button" onClick={addFormHandlers.open}></div>
      </div>
    </>
  )
}