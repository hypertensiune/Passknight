import { useEffect, useState } from 'react';
import { Route, Routes, useNavigate } from 'react-router-dom'

import { MantineProvider } from '@mantine/core';
import '@mantine/core/styles.css'

import { CryptoProvider } from '@lib/crypto';
import { subscribeForVaultsInfo } from '@lib/firebase';

import VaultList from './pages/VaultList';
import Vault from './pages/Vault';

import './App.scss'
import VaultUnlock from './pages/VaultUnlock';
import VaultCreate from './pages/VaultCreate';
import VaultDelete from './pages/VaultDelete';

function App() {
  const navigate = useNavigate();

  const [vaults, setVaults] = useState([]);

  useEffect(() => {
    subscribeForVaultsInfo((data: any) => {
      if (data[1]) {
        CryptoProvider.loadProviderPersistance();
        navigate(`/v/${data[2]}`);
      } else {
        setVaults(data[0])
      }
    });
  }, [])

  return (
    <MantineProvider defaultColorScheme='dark'>
      <Routes>
        <Route path='/' element={<VaultList vaults={vaults} />} />
        <Route path='/unlock/:vault' element={<VaultUnlock/>}/>
        <Route path='/create' element={<VaultCreate vaults={vaults}/>}/>
        <Route path='/v/:vault' element={<Vault />} />
        <Route path='/v/:vault/delete' element={<VaultDelete />} />
      </Routes>
    </MantineProvider>
  )
}

export default App
