import { Button, Drawer } from "@mantine/core";

export default function ConfirmDelete({string, size, opened, close, action}: {string: String, size: string, opened: boolean, close: () => void, action: () => void}) {
  function yes() {
    action();
    close();
  };

  return (
    <Drawer opened={opened} onClose={close} position="bottom" size={size}>
      <div style={{display: "flex", flexDirection: "column"}}>
        <h2 style={{textAlign: "center"}}>Are you sure you want to delete this {string}?</h2>
        <div style={{display: "flex", marginTop: "15%", justifyContent: "space-around"}}>
          <Button color="red" onClick={close}>No</Button>
          <Button color="green" onClick={yes}>Yes</Button>
        </div>
      </div>
    </Drawer>
  )
}