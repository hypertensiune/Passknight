import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

import { LoadingOverlay, Tabs, Tooltip } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";

import { deleteVault, getVaultContent, lockVault } from "@lib/firebase";

import GeneratorTab from "./Tabs/GeneratorTab";
import VaultTab from "./Tabs/VaultTab";
import Current from "./Tabs/Current";

import './vault.scss';
import ConfirmDelete from "./Components/ConfirmDelete";

export default function Vault() {
  const { vault } = useParams();

  const [data, setData] = useState<VaultContent>({ passwords: [], notes: [], history: [] });

  const [visible, { close }] = useDisclosure(true);
  const [opened, drawerHandlers] = useDisclosure(false);

  useEffect(() => {
    getVaultContent().then(content => {
      setData(content);
      close();
    });
  }, []);

  return (
    <>
      <div className="delete" onClick={drawerHandlers.open}>
        <Tooltip label="Delete vault" color="gray" position="bottom">
          <div className="trash-btn">
            <i className="fa-solid fa-trash"></i>
          </div>
        </Tooltip>
      </div>
      <LoadingOverlay visible={visible} zIndex={1000} overlayProps={{ radius: "sm", blur: 100, }} />
      <section className="header">
        <h3>{vault}</h3>
        <i className="fa-solid fa-arrow-right-from-bracket" onClick={() => lockVault()}></i>
      </section>
      <main className="vault">
        <section>
          <Tabs defaultValue="current">
            <Tabs.List grow>
              <Tabs.Tab value="current">Current</Tabs.Tab>
              <Tabs.Tab value="vault">Vault</Tabs.Tab>
              <Tabs.Tab value="generator">Generator</Tabs.Tab>
            </Tabs.List>
            <Tabs.Panel value="current">
              <Current data={data} />
            </Tabs.Panel>
            <Tabs.Panel value="vault">
              <VaultTab data={data} key={data.passwords[0]?.password} />
            </Tabs.Panel>
            <Tabs.Panel value="generator">
              <GeneratorTab _history={data.history} key={data.history[0]} />
            </Tabs.Panel>
          </Tabs>
        </section>
      </main>
      <ConfirmDelete size="75%" string="vault" opened={opened} close={drawerHandlers.close} action={() => deleteVault(vault!)}/>
    </>
  )
}