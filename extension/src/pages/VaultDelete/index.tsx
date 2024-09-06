import { CryptoProvider } from "@lib/crypto";
import { clearPersistence } from "@lib/extension";
import { deleteVault, unlockVault } from "@lib/firebase";
import { Button, PasswordInput } from "@mantine/core";
import { useState } from "react";
import { NavigateFunction, useNavigate, useParams } from "react-router-dom";

async function deleteHandler(vault: string, password: string, navigate: NavigateFunction, error: () => void) {  

  const masterPasswordHash = await CryptoProvider.getMasterPasswordHashString(`${vault}@passknight.vault`, password);
  const success = await unlockVault(vault, masterPasswordHash);

  if(success) {
    await deleteVault(vault);
    await clearPersistence();
    navigate("/");
  } else {
    error();
  }
}

export default function VaultDelete() {

  const { vault } = useParams();
  const navigate = useNavigate();

  const [error, setError] = useState("");
  const [password, setPassword] = useState("");

  return (
    <main className="delete" style={{display: "flex", alignItems: "center", flexDirection: "column"}}>
      <i className="icon-button fa-solid fa-arrow-left" style={{position: "fixed", left: "0", top: "0", margin: "8px"}} onClick={() => navigate(-1)}></i>
      <h2 style={{ textAlign: 'center' }}>Delete vault</h2>
      <div className="form" style={{justifyContent: "space-between"}}>
        <section>
          <PasswordInput
            error={error}
            onChange={e => {
              setPassword(e.target.value);
              setError("");
            }}
            description='CAUTION! Vault deletion is permanent and ireversible, all data will be lost.    Re-enter the master password to confirm'
            styles={{
              description: { color: '#ef9a9a' }
            }}
            style={{ width: '90%' }}
            label="Master password"
          />
        </section>
        <div style={{width: "90%", display: "flex", justifyContent: "space-between"}}>
          <Button onClick={() => navigate(-1)}> Cancel </Button>
          <Button color="red" onClick={() => deleteHandler(vault!, password, navigate, () => setError("Invalid master password"))}> Delete vault</Button>
        </div>
      </div>
    </main>
  )
}