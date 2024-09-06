import { useEffect, useState } from "react";

import { Button, Drawer, NativeSelect, PasswordInput, TextInput, Textarea } from "@mantine/core";
import { useForm } from "@mantine/form";

import { getCurrentActiveWebsite } from "@lib/extension";
import { addItemToVault } from "@lib/firebase";
import { CryptoProvider } from "@lib/crypto";
import { encryptPasswordItem, encryptNoteItem } from "@lib/itemUtils";
import { getTimestamp } from "@lib/time";

async function submitNew(item: PasswordItem | NoteItem, addNewItem: (item: PasswordItem | NoteItem) => void) {
  const crypto = CryptoProvider.getProvider()!!;
  
  item.created = getTimestamp();
  item.updated = getTimestamp();
  
  if('password' in item) {
    await encryptPasswordItem(item as PasswordItem, (input: string) => crypto.encrypt(input));
  } else {
    await encryptNoteItem(item as NoteItem, (input: string) => crypto.encrypt(input));
  }

  const res = await addItemToVault(item);
  if (res) {
    addNewItem(item);
  }
}

export default function ItemAddForm({ opened, close, passwordItems, noteItems, addNewItem, generate }: { 
  opened: boolean, 
  close: () => void, 
  passwordItems: PasswordItem[],
  noteItems: NoteItem[],
  addNewItem: (item: PasswordItem | NoteItem) => void,
  generate: () => string
}) {

  const [itemType, setItemType] = useState("Password");

  const passForm = useForm({
    initialValues: { name: '', website: '', username: '', password: '', created: '', updated: '' },
    validate: {
      name: (value) => (value == "" ? "Name must not be empty!" : passwordItems.find(i => i.name == value) != undefined ? "There is already an item with this name!" : null),
    }
  });

  const noteForm = useForm({
    initialValues: { name: '', content: '', created: '', updated: '' },
    validate: {
      name: (value) => (value == "" ? "Name must not be empty!" : noteItems.find((i: NoteItem) => i.name == value) != undefined ? "There is already an item with this name!" : null),
    }
  });

  const getActiveForm = () => itemType === 'Password' ? passForm : noteForm;

  useEffect(() => {
    getCurrentActiveWebsite((web: string) => passForm.setValues({website: web}));
  }, []);

  return (
    <Drawer transitionProps={{duration: 0}} position='bottom' size="100%" opened={opened} onClose={close}>
      <form onSubmit={getActiveForm().onSubmit((data: PasswordItem | NoteItem) => {
        submitNew({...data}, addNewItem);
        passForm.reset();
        noteForm.reset();
        close();
      })}>
        <h2 style={{ textAlign: 'center', marginTop: "0" }}>Create Item</h2>
        <NativeSelect value={itemType} data={['Password', 'Note']} style={{ marginBottom: '5%' }} onChange={(event) => setItemType(event.target.value)} />
        {itemType === "Password" ? (
          <>
            <div>
              <TextInput label="Name" withAsterisk={true} {...passForm.getInputProps('name')} />
              <TextInput label="Website" placeholder={passForm.getTransformedValues().website} {...passForm.getInputProps('website')} />
              <TextInput label="Username" placeholder='Your username' {...passForm.getInputProps('username')} />
            </div>
            <div style={{display: "flex", position: "relative"}}>
              <PasswordInput style={{width: "100%"}} label="Password" placeholder='Your password' {...passForm.getInputProps('password')} />
              <i className="icon-button infield fa-solid fa-repeat" style={{position: "absolute", right: 35, bottom: 3}} onClick={() => {
                passForm.setFieldValue("password", generate());
              }}></i>
            </div>
          </>
        ) : (
          <>
            <TextInput label="Name" withAsterisk={true} {...noteForm.getInputProps('name')} />
            <Textarea autosize minRows={4} maxRows={7} label="Content" {...noteForm.getInputProps('content')} />
          </>
        )}
        <div style={{ display: 'flex', justifyContent: 'center', marginTop: '15%' }}>
          <Button type="submit" style={{ margin: '0 auto' }} color='green'>Add Item</Button>
        </div>
      </form>
    </Drawer>
  )
}