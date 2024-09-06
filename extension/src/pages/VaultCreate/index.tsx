import { NavigateFunction, useNavigate } from "react-router-dom";

import { Button, PasswordInput, TextInput } from "@mantine/core";
import { useForm } from "@mantine/form";

import { createVault } from "@lib/firebase";
import { CryptoProvider } from "@lib/crypto";

async function submitHandler(data: { name: string, password: string, confirm: string }, navigate: NavigateFunction) {

  const keys = await CryptoProvider.createProvider(`${data.name}@passknight.vault`, data.password)
  console.log(keys);
  const result = await createVault(data.name, keys.masterPasswordHash, keys.protectedSymmetricKey)
  
  if(result) {
    navigate(`/v/${data.name}`);
  }
}

export default function VaultCreate({vaults }: { vaults: string[] }) {

  const navigate = useNavigate();

  const form = useForm({
    initialValues: { password: '', confirm: '', name: '' },

    validate: {
      name: (value) => (value.length == 0 ? 'Vault name is required' : vaults?.includes(value) ? 'Vault already exists' : null),
      password: (value) => (value.length < 6 ? 'Passwords should be at least 15 characters' : null),
      confirm: (value, values) => (value !== values.password ? 'Passwords did not match' : null)
    }
  });

  return (
    <main className="vault-create">
      <i className="icon-button fa-solid fa-arrow-left" style={{position: "fixed", left: "0", top: "0", margin: "8px"}} onClick={() => navigate(-1)}></i>
      <form onSubmit={form.onSubmit(data => submitHandler(data, navigate))}>
        <h2 style={{ textAlign: 'center' }}>Vault setup</h2>
        <div className="form">
          <section>
            <TextInput
              label="Vault name"
              style={{ width: '90%' }}
              {...form.getInputProps('name')}
            />
          </section>
          <section style={{marginBottom: "40%"}}>
            <PasswordInput
              label="Master password"
              description='The master password cannot be recovered if you forget it! 15 characters minimum'
              styles={{
                description: { color: '#ef9a9a' }
              }}
              style={{ width: '90%' }}
              {...form.getInputProps('password')}
            />
            <PasswordInput
              label="Confirm master password"
              style={{ width: '90%' }}
              {...form.getInputProps('confirm')}
            />
          </section>
          <Button type='submit'>Create</Button>
        </div>
      </form>
    </main>
  )
}