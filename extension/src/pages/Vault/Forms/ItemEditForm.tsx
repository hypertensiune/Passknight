import { useEffect } from "react";

import { Button, Drawer, Modal, PasswordInput, TextInput, Textarea } from "@mantine/core";
import { useForm } from "@mantine/form";
import { useDisclosure } from "@mantine/hooks";

import { deleteItemFromVault, editItemInVault } from "@lib/firebase";
import { CryptoProvider } from "@lib/crypto";
import { encryptNoteItem, encryptPasswordItem } from "@lib/itemUtils";
import { getDateFromTimestamp, getTimestamp } from "@lib/time";

function nameValidation(value: string, item: PasswordItem | NoteItem, items: PasswordItem[] | NoteItem[]) {
  if(value == "") {
    return "Name must not be empty!";
  }

  const found = items.find(i => i.name == value);
  if(found != undefined && value != item.name) {
    return "There is already an item with this name!";
  }
}

export default function ItemEditForm({ opened, close, item, items, editItem, deleteItem, generate }: { 
  opened: boolean, 
  close: () => void, 
  item: PasswordItem | NoteItem, 
  items: PasswordItem[] | NoteItem[], 
  editItem: (oldItem: PasswordItem | NoteItem, newItem: PasswordItem | NoteItem) => void,
  deleteItem: (item: PasswordItem | NoteItem) => void,
  generate: () => string
}) {

  const isPassword = 'password' in item;

  const crypto = CryptoProvider.getProvider()!!;

  const passForm = useForm({
    initialValues: { name: item.name, website: '', username: '', password: '', created: item.created, updated: item.updated },
    validate: {
      name: value => nameValidation(value, item, items)
    }
  });

  const noteForm = useForm({
    initialValues: { name: item.name, content: '', created: item.created, updated: item.updated },
    validate: {
      name: value => nameValidation(value, item, items)
    }
  });

  const [modal, modalHandlers] = useDisclosure(false);

  useEffect(() => {
    (async () => {
      // Decrypt item fields before putting them in the form
      // Not modifying the item because we need the original one to check for changes
      if(isPassword) {
        const website = await crypto.decrypt(item.website);
        const username = await crypto.decrypt(item.username);
        const password = await crypto.decrypt(item.password);
        passForm.setValues({website: website, username: username, password: password});
      } else {
        const content = await crypto.decrypt(item.content);
        noteForm.setValues({content: content});
      }
    })();
  }, []);

  const getActiveForm = () => isPassword ? passForm : noteForm;

  async function submitDeletion(item: PasswordItem | NoteItem) {
    const res = await deleteItemFromVault(item);
    if (res) {
      deleteItem(item);
    }
  }

  async function submitChange(oldItem: PasswordItem | NoteItem, newItem: PasswordItem | NoteItem) {
    const crypto = CryptoProvider.getProvider()!!;
    
    newItem.updated = getTimestamp();
   
    if('password' in newItem) {
      await encryptPasswordItem(newItem as PasswordItem, (input: string) => crypto.encrypt(input));
    } else {
      await encryptNoteItem(newItem as NoteItem, (input: string) => crypto.encrypt(input));
    }
    
    const res = await editItemInVault(oldItem, newItem);
    if (res) {
      editItem(oldItem, newItem);
    }
  }

  return (
    <>
      <Drawer.Root size="100%" position="bottom" opened={opened} onClose={close}>
        <Drawer.Overlay />
        <Drawer.Content>
          <Drawer.Header>
            <div className='trash-btn' onClick={modalHandlers.open}>
              <i className="fa-solid fa-trash" style={{ color: 'var(--mantine-color-red-filled)' }}></i>
            </div>
            <Drawer.CloseButton />
          </Drawer.Header>
          <Drawer.Body>
            <form onSubmit={getActiveForm().onSubmit((data: PasswordItem | NoteItem) => {
              submitChange(item, data);
              passForm.reset();
              noteForm.reset();
              close();
            })}>
              {isPassword ? (
                <>
                  <h2 style={{ textAlign: 'center', marginTop: "0" }}>Edit Password</h2>
                  <TextInput label="Name" {...passForm.getInputProps('name')} />
                  <TextInput label="Website" placeholder={'website'} {...passForm.getInputProps('website')} />
                  <TextInput label="Username" placeholder='Your username' {...passForm.getInputProps('username')} />
                  <div style={{display: "flex", position: "relative"}}>
                    <PasswordInput style={{width: "100%"}} label="Password" placeholder='Your password' {...passForm.getInputProps('password')} />
                    <i className="icon-button infield fa-solid fa-repeat" style={{position: "absolute", right: 35, bottom: 3}} onClick={() => {
                      passForm.setFieldValue("password", generate());
                    }}></i>
                  </div>
                </>
              ) : (
                <>
                  <h2 style={{ textAlign: 'center' }}>Edit Note</h2>
                  <TextInput label="Name" {...noteForm.getInputProps('name')} />
                  <Textarea autosize minRows={4} maxRows={7} label="Content" {...noteForm.getInputProps('content')} />
                </>
              )}
              <div style={{marginTop: '20px'}}>
                <div style={{fontSize: '14px'}}>Updated: {getDateFromTimestamp(item.updated)}</div>
                <div style={{fontSize: '14px'}}>Created: {getDateFromTimestamp(item.created)}</div>
              </div>
              <div style={{ display: 'flex', justifyContent: 'center', marginTop: '15%' }}>
                <Button type="submit" color='green'>Save Changes</Button>
              </div>
            </form>
          </Drawer.Body>
        </Drawer.Content>
      </Drawer.Root>
      <Modal radius="lg" overlayProps={{backgroundOpacity: 0.6, blur: "2"}} opened={modal} onClose={modalHandlers.close} withCloseButton={false} centered> 
        <div>
          <span>Are you sure you want to permanently delete this item? Action is not reversible.</span>
          <div style={{display: "flex", width: "100%", justifyContent: "flex-end", marginTop: "10%"}}>
            <Button style={{marginRight: "4%"}} onClick={modalHandlers.close}>No</Button>
            <Button onClick={() => submitDeletion(item)}>Yes</Button>
          </div>
        </div>
      </Modal>
    </>
  )
}