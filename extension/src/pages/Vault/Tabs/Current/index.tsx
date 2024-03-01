import { useEffect, useState } from "react";

import { List, PasswordListItem } from "@VaultComponents";

import { getCurrentActiveWebsite } from "@lib/extension";

export default function Current({ data }: { data: VaultContent }) {

  const [website, setWebsite] = useState("");

  useEffect(() => {
    getCurrentActiveWebsite((web: string) => setWebsite(web));
  }, []);

  const passwordList = data.passwords.filter(pass => pass.website === website);

  return (
    <div className="current">
      <List>
        {passwordList.map((d: PasswordItem) => <PasswordListItem data={d} onClick={() => { }} />)}
      </List>
    </div>
  )
}