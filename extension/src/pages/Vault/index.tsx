import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { Menu, Tabs, TextInput, Transition } from "@mantine/core";
import { useClickOutside } from "@mantine/hooks";

import GeneratorTab from "./Tabs/GeneratorTab";
import VaultTab from "./Tabs/VaultTab";
import Current from "./Tabs/Current";

import { getVaultContent, lockVault } from "@lib/firebase";
import { getCurrentActiveWebsite, autofillSetItems } from "@lib/extension";

import logo from '@assets/logo.svg';
import { generatePassword } from "@lib/generator";

export default function Vault() {

  const { vault } = useParams();

  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState<string | null>("current");

  const [passwords, setPasswords] = useState<PasswordItem[]>([]);  
  const [notes, setNotes] = useState<NoteItem[]>([]);  
  const [history, setHistory] = useState<string[]>([]);

  const [website, setWebsite] = useState("");
  const [search, setSearch] = useState("");

  const [searchOpened, setSearchOpened] = useState(false);
  const clickOutsideRef = useClickOutside(() => setSearchOpened(false));

  const [generatorOptions, setGeneratorOptions] = useState<GeneratorOptions>({length: 15, lowercase: true, uppercase: true, numbers: true, symbols: true});

  function addItem(item: PasswordItem | NoteItem) {
    if('password' in item) {
      setPasswords([...passwords, item]);
    } else {
      setNotes([...notes, item]);
    }
  }

  function editItem(oldItem: PasswordItem | NoteItem, newItem: PasswordItem | NoteItem, cb: Function) {
    if('password' in oldItem) {
      const index = passwords.findIndex((i: PasswordItem) => i.name == oldItem.name && i.website == oldItem.website && i.username == oldItem.username && i.password == oldItem.password);
      
      const arr = passwords.map((el, i) => i == index ? newItem : el) as PasswordItem[];
      setPasswords(arr);
    } else {
      const index = notes.findIndex((i: NoteItem) => i.name == oldItem.name && i.content == oldItem.content);
      
      const arr = notes.map((el, i) => i == index ? newItem : el) as NoteItem[];
      setNotes(arr);
    }

    cb();
  }

  function deleteItem(item: PasswordItem | NoteItem, cb: Function) {
    if('password' in item) {
      const arr = passwords.filter((i: PasswordItem) => i.name != item.name && i.website != item.website && i.username != item.username && i.password != item.password) as PasswordItem[];
      setPasswords(arr);
    } else {
      const arr = notes.filter((i: NoteItem) => i.name != item.name && i.content != item.content) as NoteItem[];
      setNotes(arr);
    }

    cb();
  }

  useEffect(() => {
    getVaultContent().then(content => {
      setPasswords(content.passwords);
      setNotes(content.notes);
      setHistory(content.history);
    });

    getCurrentActiveWebsite((web: string) => {
      setWebsite(web);
    });
  }, []);

  useEffect(() => {
    // Context menu items for autofilling
    autofillSetItems(passwords);
  }, [passwords]);

  return (
    <main className="vault">
      <div>
        <i className="icon-button fa-solid fa-arrow-left" style={{position: "fixed", left: "0", top: "0", margin: "8px"}} onClick={lockVault}></i>
        { activeTab == "vault" && <i className="icon-button fa-solid fa-magnifying-glass" style={{position: "fixed", left: "30px", top: "0", margin: "8px"}} onClick={() => setSearchOpened(true)}></i>}
        <Transition
          mounted={searchOpened}
          transition={{
            in: { opacity: 1, transform: 'scaleX(1)' },
            out: { opacity: 0, transform: 'scaleX(0)' },
            common: { transformOrigin: 'left' },
            transitionProperty: 'transform, opacity',
          }}
          duration={200}
          timingFunction="ease"
        >
          {(transitionStyle) => (
            <TextInput 
            onChange={e => setSearch(e.target.value)}
            style={{position: "fixed", left: "30px", top: "0", margin: "8px", width: "80%", ...transitionStyle}}
            leftSectionProps={{style: {color: "#c9c9c9"}}}
            placeholder="Search" 
            leftSection={<i className="icon-button fa-solid fa-magnifying-glass"/>}
            ref={clickOutsideRef} />
          )}
        </Transition>
        <Menu>
          <Menu.Target>
            <i className="icon-button fa-solid fa-bars" style={{position: "fixed", right: "0", top: "0", margin: "8px"}}></i>
          </Menu.Target>
          <Menu.Dropdown>
            <Menu.Item onClick={lockVault}>Lock</Menu.Item>
            <Menu.Item 
              color="red"
              leftSection={<i className="fa-solid fa-trash"></i>}
              onClick={() => navigate('/v/:vault/delete')} >
              Delete
            </Menu.Item>
          </Menu.Dropdown>
        </Menu>
      </div>
      <div className="app-logo">
        <img src={logo}></img>
      </div>
      <section className="header">
        <h3>{vault}</h3>
      </section>
      <section style={{marginTop: "5%"}}>
        <Tabs value={activeTab} onChange={setActiveTab}>
          <Tabs.List grow>
            <Tabs.Tab value="current">Current</Tabs.Tab>
            <Tabs.Tab value="vault">Vault</Tabs.Tab>
            <Tabs.Tab value="generator">Generator</Tabs.Tab>
          </Tabs.List>
          <Tabs.Panel value="current">
            <Current 
              passwords={passwords} 
              website={website} 
              editItem={editItem} 
              deleteItem={deleteItem} 
              generate={() => generatePassword(generatorOptions)} 
            />
          </Tabs.Panel>
          <Tabs.Panel value="vault">
            <VaultTab 
              passwords={passwords} 
              notes={notes} 
              search={search} 
              addItem={addItem} 
              editItem={editItem} 
              deleteItem={deleteItem} 
              generate={() => generatePassword(generatorOptions)} 
              key={search} 
            />
          </Tabs.Panel>
          <Tabs.Panel value="generator">
            <GeneratorTab _history={history} options={generatorOptions} setOptions={setGeneratorOptions} key={history[0]} />
          </Tabs.Panel>
        </Tabs>
      </section>
    </main>
  )
}