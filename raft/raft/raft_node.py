import asyncio
import json
import os
import random
import time

NODE_ID = int(os.getenv("NODE_ID"))
PEERS = os.getenv("PEERS", "").split(",")

PORT = 5000 + NODE_ID

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
        asyncio.create_task(self.election_timer())
        asyncio.create_task(self.listen())
        print(f"[Node {self.node_id}] started on port {PORT}")

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

        return {
            "term": self.current_term,
            "vote_granted": vote_granted
        }

    async def handle_heartbeat(self, msg):
        if msg["term"] >= self.current_term:
            self.current_term = msg["term"]
            self.state = STATE_FOLLOWER
            self.last_heartbeat = time.time()
        return {"ok": True}

    async def election_timer(self):
        while True:
            await asyncio.sleep(0.15)
            timeout = 0.5 + random.random()

            if time.time() - self.last_heartbeat > timeout and self.state != STATE_LEADER:
                await self.start_election()

    async def start_election(self):
        self.state = STATE_CANDIDATE
        self.current_term += 1
        self.voted_for = self.node_id
        self.vote_count = 1
        print(f"[Node {self.node_id}] Starting election (term {self.current_term})")

        for peer in self.peers:
            asyncio.create_task(self.request_vote(peer))

        # check election result
        await asyncio.sleep(0.3)
        if self.state == STATE_CANDIDATE and self.vote_count > (len(self.peers) + 1) // 2:
            self.state = STATE_LEADER
            print(f"🔥🔥 [Node {self.node_id}] is LEADER for term {self.current_term} 🔥🔥")
            asyncio.create_task(self.send_heartbeats())

    async def request_vote(self, peer):
        try:
            host, port = peer.split(":")
            reader, writer = await asyncio.open_connection(host, int(port))
            msg = {
                "type": "vote_request",
                "term": self.current_term,
                "candidate": self.node_id
            }
            writer.write(json.dumps(msg).encode())
            await writer.drain()

            data = await reader.read(10000)
            writer.close()

            reply = json.loads(data.decode())
            if reply["vote_granted"]:
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
            msg = {
                "type": "heartbeat",
                "term": self.current_term,
                "leader": self.node_id
            }
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

