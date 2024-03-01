import { useState } from "react";
import { NavigateFunction, useNavigate } from "react-router-dom";

import { Button, Drawer, PasswordInput } from "@mantine/core";

import { unlockVault } from "@lib/firebase";
import useCrypto from "@hooks/useCrypto";

async function unlockHandler(vault: string, password: string, navigate: NavigateFunction, error: () => void) {
  const success = await unlockVault(vault, password);
  if (success) {
    useCrypto(password);
    navigate(`/v/${vault}`);
  }
  else {
    error();
  }
}

export default function UnlockVaultDrawer({ opened, close, vault }: { opened: boolean, close: () => void, vault: string }) {
  const navigate = useNavigate();

  const [error, setError] = useState("");
  const [password, setPassword] = useState("");

  return (
    <Drawer opened={opened} onClose={close} title="" position='bottom' size="100%">
      <div className="form">
        <h2 style={{ textAlign: 'center' }}>Unlock {vault}</h2>
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
          style={{ width: '35%', marginTop: '15%' }}
          onClick={() => unlockHandler(vault, password, navigate, () => setError("Invalid master password"))}>
          Unlock vault
        </Button>
      </div>
    </Drawer>
  )
}