import { useEffect, useState } from 'react'

import { Button, Checkbox, Drawer, Modal, Slider } from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';

import { setGeneratorHistory } from '@lib/firebase';
import { generatePassword } from '@lib/generator';

import history_png from '@assets/history.png';

import './generator.scss';

let timeoutHandle: NodeJS.Timeout | null = null;

const split = (password: string) => [...password.matchAll(/(\d+)|([a-zA-Z]+)|([!@#$%^&*]+)/g)];

function printPassword(password: string) {
  let data: Array<React.JSX.Element> = [];

  split(password).map(arr => {
    if (arr[1] != undefined)
      data.push(<span className="number">{arr[1]}</span>);
    else if (arr[2] != undefined)
      data.push(<span className="letter">{arr[2]}</span>);
    else if (arr[3] != undefined)
      data.push(<span className="symbol">{arr[3]}</span>);
  });

  return data;
}

function HistoryDrawer({ history, clear, opened, close }: { history: string[], clear: () => void, opened: boolean, close: any }) {
  
  const [modal, modalHandlers] = useDisclosure(false);

  return (
    <>
      <Drawer.Root transitionProps={{duration: 0}} opened={opened} onClose={close} size="100%">
        <Drawer.Overlay />
        <Drawer.Content>
          <Drawer.Header>
            <div className='trash-btn' onClick={modalHandlers.open}>
              <i className="fa-solid fa-trash" style={{ color: 'var(--mantine-color-red-filled)' }}></i>
            </div>
            <Drawer.CloseButton />
          </Drawer.Header>
          <Drawer.Body>
            <div>
              <h2 style={{ textAlign: 'center', marginTop: "0" }}>History</h2>
              {history.map(prev => (
                <div className='pass-container' key={prev}>
                  {printPassword(prev)}
                  <div className='actions' style={{ display: 'inline' }}>
                    <span className='action' onClick={() => navigator.clipboard.writeText(prev)}><i className="fa-regular fa-copy"></i></span>
                  </div>
                </div>
              ))}
            </div>
          </Drawer.Body>
        </Drawer.Content>
      </Drawer.Root>
      <Modal radius="lg" overlayProps={{backgroundOpacity: 0.6, blur: "2"}} opened={modal} onClose={modalHandlers.close} withCloseButton={false} centered> 
        <div>
          <span>Are you sure you want to clear the password history?</span>
          <div style={{display: "flex", width: "100%", justifyContent: "flex-end", marginTop: "10%"}}>
            <Button style={{marginRight: "4%"}} onClick={modalHandlers.close}>No</Button>
            <Button onClick={() => {
              clear();
              modalHandlers.close();
            }}>Yes</Button>
          </div>
        </div>
      </Modal>
    </>
  )
}

export default function Generator({ _history, options, setOptions }: { _history: string[], options: GeneratorOptions, setOptions: Function }) {
  
  const [opened, { open, close }] = useDisclosure(false);

  const [regenerate, setRegenerate] = useState(true);

  const [history, setHistory] = useState<string[]>(_history);

  const [password, setPassword] = useState(generatePassword(options));

  useEffect(() => {

    const generated = generatePassword(options);
    setPassword(generated);

    if (timeoutHandle != null) {
      clearInterval(timeoutHandle);
    }
    timeoutHandle = setTimeout(() => {
      const newarr = [...history];
      if (history.length > 15) {
        newarr.pop()
      }
      newarr.unshift(generated);

      setHistory(newarr);
      setGeneratorHistory(newarr);
    }, 1000);

  }, [options]);

  return (
    <div className="generator">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '90%', margin: '0 auto', marginTop: '10%' }}>
        <label>Length</label>
        <Slider style={{ width: '80%' }} defaultValue={15} min={5} max={128} labelAlwaysOn onChange={value => setOptions({...options, length: value})} />
      </div>
      <div className='pass-container' id='pass-container'>
        <div id="pass" className="result">{printPassword(password)}</div>
        <div className='actions'>
          <span className='action' onClick={() => navigator.clipboard.writeText(password)}><i className="fa-regular fa-copy"></i></span>
          <span className='action' onClick={() => setRegenerate(!regenerate)}><i className="fa-solid fa-repeat"></i></span>
        </div>
      </div>
      <div style={{ marginTop: '5%' }}>
        <div className='options'>
          <Checkbox defaultChecked={true} onChange={e => setOptions({...options, lowercase: e.target.checked})} label="abc" />
          <Checkbox defaultChecked={true} onChange={e => setOptions({...options, uppercase: e.target.checked})} label="ABC" />
          <Checkbox defaultChecked={true} onChange={e => setOptions({...options, numbers: e.target.checked})} label="123" />
          <Checkbox defaultChecked={true} onChange={e => setOptions({...options, symbols: e.target.checked})} label="#$@" />
        </div>
      </div>
      <div>
        <div onClick={open} style={{ cursor: 'pointer', margin: '0 auto', width: '24px' }}><img className="history-btn" src={history_png} width='24'></img></div>
      </div>
      <HistoryDrawer history={history} opened={opened} close={close} clear={() => {
        setHistory([]);
        setGeneratorHistory([]);
      }} />
    </div>
  )
}