import { useEffect, useState } from 'react'

import { Checkbox, Drawer, Slider } from '@mantine/core';
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

function HistoryDrawer({ history, opened, close }: { history: string[], opened: boolean, close: any }) {
  return (
    <Drawer opened={opened} onClose={close} title="History" position='bottom'>
      <div>
        {history.map(prev => (
          <div className='pass-container' key={prev}>
            {printPassword(prev)}
            <div className='actions' style={{ display: 'inline' }}>
              <span className='action' onClick={() => navigator.clipboard.writeText(prev)}><i className="fa-regular fa-copy"></i></span>
            </div>
          </div>
        ))}
      </div>
    </Drawer>
  )
}

export default function Generator({ _history }: { _history: string[] }) {
  const [opened, { open, close }] = useDisclosure(false);

  const [length, setLength] = useState(10);
  const [lowercase, setLowercase] = useState(true);
  const [uppercase, setUppercase] = useState(true);
  const [numbers, setNumbers] = useState(true);
  const [symbols, setSymbols] = useState(true);

  const [regenerate, setRegenerate] = useState(true);

  const [history, setHistory] = useState<string[]>(_history);

  const [password, setPassword] = useState("");

  useEffect(() => {
    const pass = generatePassword(length, {
      lowercase: lowercase,
      uppercase: uppercase,
      numbers: numbers,
      symbols: symbols
    });

    setPassword(pass);

    if (timeoutHandle != null) {
      clearInterval(timeoutHandle);
    }
    timeoutHandle = setTimeout(() => {
      console.log("saving...");

      const newarr = [...history];
      if (history.length > 15) {
        newarr.pop()
      }
      newarr.unshift(pass);

      setHistory(newarr);
      setGeneratorHistory(newarr);
    }, 1000);

  }, [length, lowercase, uppercase, numbers, symbols, regenerate]);

  return (
    <div className="generator">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '90%', margin: '0 auto', marginTop: '10%' }}>
        <label>Length</label>
        <Slider style={{ width: '80%' }} defaultValue={length} min={5} max={128} labelAlwaysOn onChange={setLength} />
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
          <Checkbox defaultChecked={lowercase} onChange={e => setLowercase(e.target.checked)} label="abc" />
          <Checkbox defaultChecked={uppercase} onChange={e => setUppercase(e.target.checked)} label="ABC" />
          <Checkbox defaultChecked={numbers} onChange={e => setNumbers(e.target.checked)} label="123" />
          <Checkbox defaultChecked={symbols} onChange={e => setSymbols(e.target.checked)} label="#$@" />
        </div>
      </div>
      <div>
        <div onClick={open} style={{ cursor: 'pointer', margin: '0 auto', width: '24px' }}><img src={history_png} width='24'></img></div>
      </div>
      <HistoryDrawer history={history} opened={opened} close={close} />
    </div>
  )
}