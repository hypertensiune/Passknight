import { useNavigate } from "react-router-dom";

import logo from '@assets/logo.svg';
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

export default function VaultList({ vaults }: { vaults: string[] }) {

  const navigate = useNavigate();

  return (
    <main className="home" style={{ display: 'flex', alignItems: 'center', flexDirection: 'column' }}>
      <div className="app-logo">
        <img src={logo}></img>
      </div>
      <section className="vaults">
        {vaults.map(v => <VaultID id={v} key={v} onClick={() => {
          navigate(`/unlock/${v}`);
        }} />)}
      </section>
      <div>
        <div className="add-button" onClick={() => navigate('/create')}></div>
      </div>
    </main>
  )
}