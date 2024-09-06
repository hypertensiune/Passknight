import { useEffect, useState } from 'react';

import { Tooltip } from '@mantine/core';

import { clipboardDeleteCommand } from '@lib/extension';
import { CryptoProvider } from '@lib/crypto';

import './index.scss'

async function decrypt(text: string) {
  const cryptoObject = CryptoProvider.getProvider()!!;
  return await cryptoObject.decrypt(text) || "";
}

async function copy(text: string, needsDecryption: boolean) {
  if (needsDecryption) {
    text = await decrypt(text);
  }

  navigator.clipboard.writeText(text);

  clipboardDeleteCommand();
}

export function PasswordListItem({ data, onClick }: { data: PasswordItem, onClick: () => void }) {
  
  const [decryptedData, setDecryptedData] = useState(["", ""]);

  useEffect(() => {
    (async () => {
      const web = await decrypt(data.website);
      const user = await decrypt(data.username);
      setDecryptedData([web, user]);
    })();
  }, []);
  
  return (
    <div className="list-item" onClick={onClick}>
      <img src={`https://icon.horse/icon/${decryptedData[0]}`} width="20px" />
      <div className="details" style={{ width: '70%' }}>
        <div className='name'>{data.name}</div>
        <div className='username'>{decryptedData[1]}</div>
      </div>
      <div className="actions">
        <Tooltip label="Copy username" color='gray' position='bottom'>
          <i className="fa-regular fa-user" onClick={(e: any) => { 
            e.stopPropagation();
            copy(data.username, true);
          }}></i>
        </Tooltip>
        <Tooltip label="Copy password" color='gray' position='bottom'>
          <i className="fa-solid fa-key" onClick={(e: any) => {
            e.stopPropagation();
            copy(data.password, true);
          }}></i>
        </Tooltip>
      </div>
    </div>
  )
}

export function NoteListItem({ data, onClick }: { data: NoteItem, onClick: () => void }) {
  return (
    <div className="list-item" onClick={onClick} style={{ float: 'left' }}>
      <i className="fa-regular fa-file" style={{ fontSize: '1.25rem' }}></i>
      <div className="details" style={{ width: '88%' }}>
        <div className="name" style={{ padding: '3% 0' }}>{data.name}</div>
      </div>
    </div>
  )
}