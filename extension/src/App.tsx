import { useEffect, useState } from 'react';
import { Route, Routes, useNavigate } from 'react-router-dom'

import { MantineProvider } from '@mantine/core';
import '@mantine/core/styles.css'

import { subscribeForVaultsInfo } from '@lib/firebase';

import Home from './pages/Home'
import Vault from './pages/Vault';

import logo from '@assets/logo.svg';

import './App.scss'
import { CryptoProvider } from '@lib/crypto';

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
      <div className="app-logo">
        <img src={logo}></img>
      </div>
      <Routes>
        <Route path='/' element={<Home vaults={vaults} />} />
        <Route path='/v/:vault' element={<Vault />} />
      </Routes>
    </MantineProvider>
  )
}

export default App
