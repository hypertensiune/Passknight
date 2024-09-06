import { useState } from "react";
import { NavigateFunction, useNavigate, useParams } from "react-router-dom";

import { Button, PasswordInput } from "@mantine/core";

import { unlockVault, getVaultPsk } from "@lib/firebase";
import { CryptoProvider } from "@lib/crypto";

import logo from '@assets/logo.svg';

async function unlockHandler(vault: string, password: string, navigate: NavigateFunction, error: () => void) {
  const masterPasswordHash = await CryptoProvider.getMasterPasswordHashString(`${vault}@passknight.vault`, password);
  
  const success = await unlockVault(vault, masterPasswordHash);
  if (success) {
    const psk = await getVaultPsk();
    await CryptoProvider.loadProviderPsk(`${vault}@passknight.vault`, password, psk);
    navigate(`/v/${vault}`);
  }
  else {
    error();
  }
}

export default function VaultUnlock() {
  const { vault } = useParams();

  const navigate = useNavigate();

  const [error, setError] = useState("");
  const [password, setPassword] = useState("");

  return (
    <main className="unlock" style={{ display: 'flex', alignItems: 'center', flexDirection: 'column' }}>
      <i className="icon-button fa-solid fa-arrow-left" style={{position: "fixed", left: "0", top: "0", margin: "8px"}} onClick={() => navigate(-1)}></i>
      <div className="app-logo">
        <img src={logo}></img>
      </div>
      <section className="header">
        <h3>{vault}</h3>
      </section>
      <div className="form" style={{marginTop: "5%", justifyContent: "space-between"}}>
        <PasswordInput
          error={error}
          onChange={e => {
            setPassword(e.target.value);
            setError("");
          }}
          style={{ width: '90%' }}
          label="Master password"
        />
        <Button
          style={{ width: '35%' }}
          onClick={() => unlockHandler(vault!, password, navigate, () => setError("Invalid master password"))}>
          Unlock vault
        </Button>
      </div>
    </main>
  )
}