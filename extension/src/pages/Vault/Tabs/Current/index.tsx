import { List, PasswordListItem } from "@VaultComponents";

export default function Current({ data }: { data: PasswordItem[] }) {
  return (
    <div className="current">
      <List>
        {data.map((d: PasswordItem) => <PasswordListItem data={d} onClick={() => { }} />)}
      </List>
    </div>
  )
}