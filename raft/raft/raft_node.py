import asyncio
import json
import os
import random
import time
from datetime import datetime

NODE_ID = int(os.getenv("NODE_ID"))
PEERS = os.getenv("PEERS", "").split(",")

PORT = 5000 + NODE_ID
LOG_DIR = "logs"
os.makedirs(LOG_DIR, exist_ok=True)
LOG_FILE = os.path.join(LOG_DIR, f"node_{NODE_ID}_log.txt")

STATE_FOLLOWER = "follower"
STATE_CANDIDATE = "candidate"
STATE_LEADER = "leader"

class RaftNode:
    def __init__(self, node_id, peers):
        self.node_id = node_id
        self.peers = peers
        self.state = STATE_FOLLOWER
        self.current_term = 0
        self.voted_for = None
        self.vote_count = 0
        self.last_heartbeat = time.time()

    async def start(self):
        # Load persisted log
        self.load_log()

        # Start election timer and server
        asyncio.create_task(self.election_timer())
        asyncio.create_task(self.listen())
        print(f"[Node {self.node_id}] started on port {PORT}", flush=True)

    def load_log(self):
        if os.path.exists(LOG_FILE):
            with open(LOG_FILE, "r") as f:
                for line in f:
                    line = line.strip()
                    if line:
                        print(f"[Node {self.node_id}] Replaying command from log: {line}", flush=True)

    async def listen(self):
        server = await asyncio.start_server(self.handle_conn, "0.0.0.0", PORT)
        async with server:
            await server.serve_forever()

    async def handle_conn(self, reader, writer):
        data = await reader.read(100000)
        if not data:
            return
        msg = json.loads(data.decode())
        response = await self.handle_message(msg)
        writer.write(json.dumps(response).encode())
        await writer.drain()
        writer.close()

    async def handle_message(self, msg):
        if msg["type"] == "vote_request":
            return await self.handle_vote_request(msg)
        elif msg["type"] == "heartbeat":
            return await self.handle_heartbeat(msg)
        elif msg["type"] == "command":
            return await self.handle_command(msg)
        elif msg["type"] == "append_entries":
            return await self.handle_append_entries(msg)

    async def handle_vote_request(self, msg):
        term = msg["term"]
        candidate = msg["candidate"]
        if term > self.current_term:
            self.current_term = term
            self.voted_for = None
            self.state = STATE_FOLLOWER

        vote_granted = False
        if self.voted_for is None and term >= self.current_term:
            self.voted_for = candidate
            vote_granted = True
            self.last_heartbeat = time.time()

        return {"term": self.current_term, "vote_granted": vote_granted}

    async def handle_heartbeat(self, msg):
        if msg["term"] >= self.current_term:
            if msg["term"] > self.current_term or self.state == STATE_CANDIDATE:
                self.state = STATE_FOLLOWER
            self.current_term = msg["term"]
            self.last_heartbeat = time.time()
        return {"ok": True}

    async def handle_command(self, msg):
        if self.state != STATE_LEADER:
            return {"status": "not_leader"}
        timestamp = datetime.utcnow().isoformat()
        command_with_ts = f"{timestamp} | {msg['command']}"

        with open(LOG_FILE, "a") as f:
            f.write(command_with_ts + "\n")
        print(f"[Leader {self.node_id}] Executing command: {command_with_ts}", flush=True)

        # Replicate to followers
        tasks = [self.append_to_follower(peer, command_with_ts) for peer in self.peers]
        await asyncio.gather(*tasks)
        return {"status": "ok"}

    async def append_to_follower(self, peer, command):
        try:
            host, port = peer.split(":")
            reader, writer = await asyncio.open_connection(host, int(port))
            msg = {"type": "append_entries", "term": self.current_term, "command": command}
            writer.write(json.dumps(msg).encode())
            await writer.drain()
            await reader.read(10000)
            writer.close()
        except:
            pass

    async def handle_append_entries(self, msg):
        command = msg.get("command")
        if command:
            with open(LOG_FILE, "a") as f:
                f.write(command + "\n")
            print(f"[Follower {self.node_id}] Appended command: {command}", flush=True)
        return {"status": "ok"}

    async def election_timer(self):
        while True:
            await asyncio.sleep(0.05)
            timeout = random.uniform(0.3, 0.5)
            if time.time() - self.last_heartbeat > timeout and self.state != STATE_LEADER:
                await self.start_election()

    async def start_election(self):
        self.state = STATE_CANDIDATE
        self.current_term += 1
        self.voted_for = self.node_id
        self.vote_count = 1
        print(f"[Node {self.node_id}] Starting election (term {self.current_term})", flush=True)
        tasks = [self.request_vote(peer) for peer in self.peers]
        await asyncio.gather(*tasks)
        if self.vote_count > (len(self.peers) + 1) // 2:
            self.state = STATE_LEADER
            print(f"🔥🔥 [Node {self.node_id}] is LEADER for term {self.current_term} 🔥🔥", flush=True)
            asyncio.create_task(self.send_heartbeats())

    async def request_vote(self, peer):
        try:
            host, port = peer.split(":")
            reader, writer = await asyncio.open_connection(host, int(port))
            msg = {"type": "vote_request", "term": self.current_term, "candidate": self.node_id}
            writer.write(json.dumps(msg).encode())
            await writer.drain()
            data = await reader.read(10000)
            writer.close()
            reply = json.loads(data.decode())
            if reply.get("vote_granted"):
                self.vote_count += 1
        except:
            pass

    async def send_heartbeats(self):
        while self.state == STATE_LEADER:
            for peer in self.peers:
                asyncio.create_task(self.send_heartbeat(peer))
            await asyncio.sleep(0.15)

    async def send_heartbeat(self, peer):
        try:
            host, port = peer.split(":")
            reader, writer = await asyncio.open_connection(host, int(port))
            msg = {"type": "heartbeat", "term": self.current_term, "leader": self.node_id}
            writer.write(json.dumps(msg).encode())
            await writer.drain()
            writer.close()
        except:
            pass

async def main():
    node = RaftNode(NODE_ID, PEERS)
    await node.start()
    while True:
        await asyncio.sleep(1)

asyncio.run(main())

