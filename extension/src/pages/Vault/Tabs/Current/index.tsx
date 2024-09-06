import { useEffect, useState } from "react";

import { useDisclosure } from "@mantine/hooks";

import { CryptoProvider } from "@lib/crypto";

import { PasswordListItem } from "../../components";
import ItemEditForm from "../../Forms/ItemEditForm";

async function passwordItemAsyncFilter(array: PasswordItem[], website: string, onFiltered: (filtered: PasswordItem[]) => void) {
  const crypto = CryptoProvider.getProvider()!;

  const filtered: PasswordItem[] = [];

  await Promise.all(array.map(async (element) => {
    const dec = await crypto.decrypt(element.website);
    if(dec == website) {
      filtered.push(element);
    }
  }));

  onFiltered(filtered);
}

export default function Current({ passwords, website, editItem, deleteItem, generate }: { 
  passwords: PasswordItem[], 
  website: string,
  editItem: (oldItem: PasswordItem | NoteItem, newItem: PasswordItem | NoteItem, cb: Function) => void,
  deleteItem: (item: PasswordItem | NoteItem, cb: Function) => void,
  generate: () => string
}) {
  
  const [currentPasswords, setCurrentPasswords] = useState<PasswordItem[]>([]);

  const [_, { close }] = useDisclosure(false);
  const [selectedPassword, setSelectedPassword] = useState(-1);

  function resetForm() {
    setSelectedPassword(-1);
    close();
  }

  useEffect(() => {
    passwordItemAsyncFilter(passwords, website, filtered => {
      setCurrentPasswords(filtered);
    });
  }, [passwords]);

  return (
    <>
      <div className="vault">
        <section>
          <div className="list">
            {currentPasswords.map((d: PasswordItem, index) => <PasswordListItem data={d} onClick={() => setSelectedPassword(index)} key={d.updated} />)}
          </div>
        </section>
      </div>
      {selectedPassword > -1 &&
        <ItemEditForm
          items={passwords}
          item={{...currentPasswords[selectedPassword]}}
          opened={true}
          close={resetForm}
          editItem={(oldItem, newItem) => editItem(oldItem, newItem, resetForm)}
          deleteItem={item => deleteItem(item, resetForm)}
          generate={generate}
        />
      }
    </>
  )
}