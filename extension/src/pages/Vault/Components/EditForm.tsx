import { useEffect } from "react";

import { Button, Drawer, PasswordInput, TextInput, Textarea } from "@mantine/core";
import { useForm } from "@mantine/form";

import { deleteItemFromVault, editItemInVault } from "@lib/firebase";
import ConfirmDelete from "./ConfirmDelete";
import { useDisclosure } from "@mantine/hooks";
import { CryptoProvider } from "@lib/crypto";

async function submitChange(oldItem: PasswordItem | NoteItem, newItem: PasswordItem | NoteItem, changeItem: (item: PasswordItem | NoteItem) => void) {
  const res = await editItemInVault(oldItem, newItem);
  if (res) {
    changeItem(newItem);
  }
}

async function submitDeletion(item: PasswordItem | NoteItem, deleteItem: () => void) {
  const res = await deleteItemFromVault(item);
  if (res) {
    deleteItem();
  }
}

export default function EditForm({ opened, close, item, changeItem, deleteItem }: { opened: boolean, close: () => void, item: PasswordItem | NoteItem, changeItem: (item: PasswordItem | NoteItem) => void, deleteItem: () => void }) {

  const isPassword = 'password' in item;

  const crypto = CryptoProvider.getProvider()!!;

  const passForm = useForm({
    initialValues: { name: item.name, website: (item as PasswordItem).website, username: (item as PasswordItem).username, password: '' }
  });

  const noteForm = useForm({
    initialValues: { name: item.name, content: '' },
  });

  useEffect(() => {
    crypto.decrypt(isPassword ? item.password : item.content).then((dec: string | undefined) => {
      isPassword && passForm.setFieldValue('password', dec!);
      !isPassword && noteForm.setFieldValue('content', dec!);
    });
  }, []);

  const getActiveForm = () => isPassword ? passForm : noteForm;

  const [deleteDrawerOpened, deleteDrawerHandlers] = useDisclosure(false);

  return (
    <>
      <Drawer.Root opened={opened} onClose={close}>
        <Drawer.Overlay />
        <Drawer.Content>
          <Drawer.Header>
            <div className='trash-btn' onClick={() => {
              deleteDrawerHandlers.open();
            }}>
              <i className="fa-solid fa-trash" style={{ color: 'var(--mantine-color-red-filled)' }}></i>
            </div>
            <Drawer.CloseButton />
          </Drawer.Header>
          <Drawer.Body>
            <form onSubmit={getActiveForm().onSubmit((data: PasswordItem | NoteItem) => {
              submitChange(item, data, changeItem);
              passForm.reset();
              noteForm.reset();
              close();
            })}>
              {isPassword ? (
                <>
                  <h2 style={{ textAlign: 'center' }}>Edit Password</h2>
                  <TextInput label="Name" {...passForm.getInputProps('name')} />
                  <TextInput label="Website" placeholder={'website'} {...passForm.getInputProps('website')} />
                  <TextInput label="Username" placeholder='Your username' {...passForm.getInputProps('username')} />
                  <PasswordInput label="Password" placeholder='Your password' {...passForm.getInputProps('password')} />
                </>
              ) : (
                <>
                  <h2 style={{ textAlign: 'center' }}>Edit Note</h2>
                  <TextInput label="Name" {...noteForm.getInputProps('name')} />
                  <Textarea autosize minRows={4} maxRows={7} label="Content" {...noteForm.getInputProps('content')} />
                </>
              )}
              <div style={{ display: 'flex', justifyContent: 'center', marginTop: '15%' }}>
                <Button type="submit" color='green'>Save Changes</Button>
              </div>
            </form>
          </Drawer.Body>
        </Drawer.Content>
      </Drawer.Root>
      <ConfirmDelete 
        size="100%"
        string="item" 
        opened={deleteDrawerOpened} 
        close={deleteDrawerHandlers.close} 
        action={() => {
          submitDeletion(item, deleteItem);
          passForm.reset();
          noteForm.reset();
          close();
        }}/>
    </>
  )
}