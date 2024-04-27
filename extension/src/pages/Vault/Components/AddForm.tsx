import { useEffect, useState } from "react";

import { Button, Drawer, NativeSelect, PasswordInput, TextInput, Textarea } from "@mantine/core";
import { useForm } from "@mantine/form";

import { getCurrentActiveWebsite } from "@lib/extension";
import { addItemToVault } from "@lib/firebase";

async function submitNew(item: PasswordItem | NoteItem, addNewItem: (item: PasswordItem | NoteItem) => void) {
  const res = await addItemToVault(item);
  if (res) {
    addNewItem(item);
  }
}

export default function AddForm({ opened, close, addNewItem }: { opened: boolean, close: () => void, addNewItem: (item: PasswordItem | NoteItem) => void }) {

  const [itemType, setItemType] = useState("Password");

  const passForm = useForm({
    initialValues: { name: '', website: '', username: '', password: '' },
  });

  const noteForm = useForm({
    initialValues: { name: '', content: '' },
  });

  const getActiveForm = () => itemType === 'Password' ? passForm : noteForm;

  useEffect(() => {
    getCurrentActiveWebsite((web: string) => passForm.setValues({website: web}));
  }, []);

  return (
    <Drawer opened={opened} onClose={close} position='bottom' size="100%">
      <form onSubmit={getActiveForm().onSubmit((data: PasswordItem | NoteItem) => {
        console.log(data);
        submitNew(data, addNewItem);
        passForm.reset();
        noteForm.reset();
        close();
      })}>
        <h2 style={{ textAlign: 'center' }}>Add Item</h2>
        <NativeSelect value={itemType} data={['Password', 'Note']} style={{ marginBottom: '5%' }} onChange={(event) => setItemType(event.target.value)} />
        {itemType === "Password" ? (
          <>
            <TextInput label="Name" {...passForm.getInputProps('name')} />
            <TextInput label="Website" placeholder={passForm.getTransformedValues().website} {...passForm.getInputProps('website')} />
            <TextInput label="Username" placeholder='Your username' {...passForm.getInputProps('username')} />
            <PasswordInput label="Password" placeholder='Your password' {...passForm.getInputProps('password')} />
          </>
        ) : (
          <>
            <TextInput label="Name" {...noteForm.getInputProps('name')} />
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