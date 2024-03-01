import { Button, Drawer, PasswordInput, TextInput } from "@mantine/core";
import { useForm } from "@mantine/form";

import { createVault } from "@lib/firebase";

function submitHandler(data: { name: string, password: string, confirm: string }) {
  createVault(data.name, data.password, () => window.location.reload());
}

export default function NewVaultDrawer({ opened, close, vaults }: { opened: boolean, close: () => void, vaults: string[] }) {

  const form = useForm({
    initialValues: { password: '', confirm: '', name: '' },

    validate: {
      name: (value) => (value.length == 0 ? 'Vault name is required' : vaults?.includes(value) ? 'Vault already exists' : null),
      password: (value) => (value.length < 6 ? 'Passwords should be at least 15 characters' : null),
      confirm: (value, values) => (value !== values.password ? 'Passwords did not match' : null)
    }
  });

  return (
    <Drawer opened={opened} onClose={close} title="" position='bottom' size="100%">
      <form onSubmit={form.onSubmit(submitHandler)}>
        <h2 style={{ textAlign: 'center' }}>Vault setup</h2>
        <div className="form">
          <section>
            <TextInput
              label="Vault name"
              style={{ width: '90%' }}
              {...form.getInputProps('name')}
            />
          </section>
          <section>
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
          <Button type='submit'>Confirm</Button>
        </div>
      </form>
    </Drawer>
  )
}