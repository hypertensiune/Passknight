import { Tooltip } from '@mantine/core';

import useCrypto from '../../../hooks/useCrypto';

import AddForm from './AddForm';
import EditForm from './EditForm';

import './index.scss'
import { clipboardDeleteCommand } from '@lib/extension';

export { AddForm, EditForm }

async function copy(text: string, decrypt: boolean) {
  if (decrypt) {
    const cryptoObject = useCrypto(null);
    text = await cryptoObject.decrypt(text) || "";
  }

  navigator.clipboard.writeText(text);

  clipboardDeleteCommand();
}

export function List({ children }: any) {
  return (
    <div className="list">
      {children}
    </div>
  )
}

export function PasswordListItem({ data, onClick }: { data: PasswordItem, onClick: () => void }) {
  return (
    <div className="list-item" onClick={onClick}>
      <img src={`https://icon.horse/icon/${data.website}`} width="20px" />
      <div className="details" style={{ width: '70%' }}>
        <div className='name'>{data.name}</div>
        <div className='username'>{data.username}</div>
      </div>
      <div className="actions">
        <Tooltip label="Copy username" color='gray' position='bottom'>
          <i className="fa-regular fa-user" onClick={(e: any) => { 
            e.stopPropagation();
            copy(data.username, false);
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