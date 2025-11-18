import asyncio
import json

NODES = [
    "raft-node1:5001",
    "raft-node2:5002",
    "raft-node3:5003"
]

commands = ["set x=42", "set y=100", "delete x"]

async def send_command(command):
    for node in NODES:
        try:
            host, port = node.split(":")
            reader, writer = await asyncio.open_connection(host, int(port))
            msg = {"type": "command", "command": command}
            writer.write(json.dumps(msg).encode())
            await writer.drain()
            data = await reader.read(10000)
            writer.close()
            reply = json.loads(data.decode())
            if reply.get("status") == "ok":
                print(f"[Client] Command '{command}' accepted by leader {node}")
                return
        except:
            continue
    print("[Client] No leader found, retrying...")
    await asyncio.sleep(0.5)
    await send_command(command)

async def main():
    for cmd in commands:
        await send_command(cmd)
        await asyncio.sleep(0.2)

asyncio.run(main())

