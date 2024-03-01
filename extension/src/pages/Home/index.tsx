import { useState } from "react";

import { useDisclosure } from "@mantine/hooks";

import NewVaultDrawer from "./Components/NewVaultDrawer";
import UnlockVaultDrawer from "./Components/UnlockVaultDrawer";

import './home.scss';

function VaultID({ id, onClick }: any) {
  return (
    <div className="item" onClick={onClick}>
      <span style={{ textTransform: 'capitalize' }}>{id}</span>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <span className="lock"></span>
      </div>
    </div>
  )
}

export default function Home({ vaults }: { vaults: string[] }) {
  const [d1opened, d1func] = useDisclosure(false);
  const [d2opened, d2func] = useDisclosure(false);

  const [vaultToUnlock, setVaultToUnlock] = useState("");

  return (
    <main className="home" style={{ display: 'flex', alignItems: 'center', flexDirection: 'column' }}>
      <section className="vaults">
        {vaults.map(v => <VaultID id={v} key={v} onClick={() => {
          setVaultToUnlock(v);
          d1func.open();
        }} />)}
      </section>
      <div>
        <div className="add-button" onClick={d2func.open}></div>
      </div>
      <section className="drawers">
        <NewVaultDrawer opened={d2opened} close={d2func.close} vaults={vaults || []} />
        <UnlockVaultDrawer opened={d1opened} close={d1func.close} vault={vaultToUnlock} />
      </section>
    </main>
  )
}