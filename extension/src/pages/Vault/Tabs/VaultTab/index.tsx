import { useState } from 'react';

import { useDisclosure, useToggle } from '@mantine/hooks'
import { Collapse } from '@mantine/core';

import { PasswordListItem, NoteListItem } from '../../components';
import ItemAddForm from '../../Forms/ItemAddForm';
import ItemEditForm from '../../Forms/ItemEditForm';


export default function Vault({ passwords, notes, search, addItem, editItem, deleteItem, generate }: { 
  passwords: PasswordItem[], 
  notes: NoteItem[], 
  search: string,
  addItem:  (item: PasswordItem | NoteItem) => void,
  editItem: (oldItem: PasswordItem | NoteItem, newItem: PasswordItem | NoteItem, cb: Function) => void,
  deleteItem: (item: PasswordItem | NoteItem, cb: Function) => void,
  generate: () => string
}) {

  const [addFormOpened, addFormHandlers] = useDisclosure(false);

  const [passwordsOpened, togglePasswords] = useToggle([true, false]);
  const [notesOpened, toggleNotes] = useToggle([true, false]);

  const [selectedPassword, setSelectedPassword] = useState(-1);
  const [selectedNote, setSelectedNote] = useState(-1);

  const editFormHandlers = useDisclosure(true)[1];

  const searchedPasswords = search != "" ? passwords.filter(item => item.name.toLowerCase().includes(search.toLowerCase())) : passwords;
  const searchedNotes = search != "" ? notes.filter(item => item.name.toLowerCase().includes(search.toLowerCase())) : notes;

  function resetPasswordEditForm() {
    setSelectedPassword(-1);
    editFormHandlers.close();
  }

  function resetNoteEditForm() {
    setSelectedNote(-1);
    editFormHandlers.close();
  }

  return (
    <>
      <div className="vault">
        <section>
          <div className="list-toggle" onClick={() => togglePasswords()}>
            {passwordsOpened && <i className="fa-solid fa-chevron-up" style={{fontSize: "10px", marginRight: "5px"}}></i>}
            {!passwordsOpened && <i className="fa-solid fa-chevron-down" style={{fontSize: "10px", marginRight: "5px"}}></i>}
            Passwords {searchedPasswords.length}
          </div>
          <Collapse in={passwordsOpened}>
            <div className="list">
              {searchedPasswords.map((d: PasswordItem, index: number) => <PasswordListItem data={d} onClick={() => setSelectedPassword(index)} key={d.updated} />)}
            </div>
          </Collapse>
        </section>
        <section>
          <div className="list-toggle" onClick={() => toggleNotes()}>
            {notesOpened && <i className="fa-solid fa-chevron-up" style={{fontSize: "10px", marginRight: "5px"}}></i>}
            {!notesOpened && <i className="fa-solid fa-chevron-down" style={{fontSize: "10px", marginRight: "5px"}}></i>}
            Notes {searchedNotes.length}
          </div>
          <Collapse in={notesOpened}>
            <div className="list">
              {searchedNotes.map((n: NoteItem, index: number) => <NoteListItem data={n} onClick={() => setSelectedNote(index)} key={n.updated} />)}
            </div>
          </Collapse>
        </section>
        <ItemAddForm 
          opened={addFormOpened} 
          close={addFormHandlers.close}
          passwordItems={passwords}
          noteItems={notes}
          addNewItem={addItem}
          generate={generate}
        />
        {selectedPassword > -1 && 
          <ItemEditForm
            items={passwords}
            item={{...searchedPasswords[selectedPassword]}}
            opened={true}
            close={resetPasswordEditForm}
            editItem={(oldItem, newItem) => editItem(oldItem, newItem, resetPasswordEditForm)}
            deleteItem={item => deleteItem(item, resetPasswordEditForm)}
            generate={generate}
          />
        }
        {selectedNote > -1 &&
          <ItemEditForm
            items={notes}
            item={{...searchedNotes[selectedNote]}}
            opened={selectedNote > -1}
            close={resetNoteEditForm}
            editItem={(oldItem, newItem) => editItem(oldItem, newItem, resetNoteEditForm)}
            deleteItem={item => deleteItem(item, resetNoteEditForm)}
            generate={generate}
          />
        }
      </div>
      <div>
        <div className="add-button" onClick={addFormHandlers.open}></div>
      </div>
    </>
  )
}