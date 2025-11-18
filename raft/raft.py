# Minimal Raft implementation (asyncio) + 3-node demo
# This code will:
# 1. Start 3 Raft nodes (each listens on localhost different ports)
# 2. Run leader election
# 3. Show which node became leader
# 4. Send a client "append" command to the leader
# 5. Wait for replication and show logs/committed index on each node
#
# This is a *minimal, educational* Raft — many production features are omitted.
# Run in the notebook; it will execute the demo automatically.
import asyncio
import json
import random
import time
from typing import Dict, List, Optional

# ----- Configuration -----
NODES = [
    ("127.0.0.1", 9001),
    ("127.0.0.1", 9002),
    ("127.0.0.1", 9003),
]

ELECTION_TIMEOUT_RANGE = (0.15, 0.3)  # seconds (short for demo)
HEARTBEAT_INTERVAL = 0.05  # seconds


# ----- Raft Node -----
class RaftNode:
    def __init__(self, name: str, host: str, port: int, peers: List[tuple]):
        self.name = name
        self.host = host
        self.port = port
        self.peers = peers  # list of (host, port)
        # Persistent state
        self.current_term = 0
        self.voted_for: Optional[str] = None
        self.log: List[Dict] = []  # entries: {'term': t, 'command': cmd}
        # Volatile state
        self.commit_index = -1
        self.last_applied = -1
        # Leader state (only valid when leader)
        self.next_index: Dict[str, int] = {}
        self.match_index: Dict[str, int] = {}
        # Role & election
        self.role = "follower"
        self.votes_received = set()

        # Concurrency
        self._server_task = None
        self._tasks = []
        self._election_timer_handle = None
        self._lock = asyncio.Lock()

        # For stopping
        self._stopping = False

    # Networking helpers: send JSON and receive JSON response
    async def rpc_call(self, peer, message, timeout=1.0):
        reader = writer = None
        try:
            reader, writer = await asyncio.wait_for(asyncio.open_connection(peer[0], peer[1]), timeout=timeout)
            data = (json.dumps(message) + "\n").encode()
            writer.write(data)
            await writer.drain()
            resp_line = await asyncio.wait_for(reader.readline(), timeout=timeout)
            if not resp_line:
                return None
            return json.loads(resp_line.decode())
        except Exception as e:
            # print(f"{self.name} rpc_call to {peer} failed: {e}")
            return None
        finally:
            if writer:
                writer.close()
                await writer.wait_closed()

    async def handle_connection(self, reader: asyncio.StreamReader, writer: asyncio.StreamWriter):
        try:
            line = await reader.readline()
            if not line:
                writer.close()
                await writer.wait_closed()
                return
            msg = json.loads(line.decode())
            typ = msg.get("type")
            if typ == "RequestVote":
                resp = await self.on_request_vote(msg)
            elif typ == "AppendEntries":
                resp = await self.on_append_entries(msg)
            elif typ == "ClientAppend":
                resp = await self.on_client_append(msg)
            elif typ == "GetState":
                resp = await self.on_get_state(msg)
            else:
                resp = {"ok": False, "error": "unknown"}
            writer.write((json.dumps(resp) + "\n").encode())
            await writer.drain()
            writer.close()
            await writer.wait_closed()
        except Exception as e:
            # ignore for demo
            try:
                writer.close()
                await writer.wait_closed()
            except:
                pass

    async def start_server(self):
        server = await asyncio.start_server(self.handle_connection, self.host, self.port)
        self._server_task = asyncio.create_task(server.serve_forever())
        return server

    # ---- RPC Handlers ----
    async def on_request_vote(self, msg):
        async with self._lock:
            term = msg["term"]
            candidate = msg["candidate"]
            last_log_index = msg["last_log_index"]
            last_log_term = msg["last_log_term"]

            if term < self.current_term:
                return {"term": self.current_term, "vote_granted": False}

            if term > self.current_term:
                # step down if term higher
                self.current_term = term
                self.voted_for = None
                self.role = "follower"

            can_vote = (self.voted_for is None) or (self.voted_for == candidate)
            # candidate's log up-to-date check
            my_last_term = self.log[-1]["term"] if self.log else 0
            my_last_index = len(self.log) - 1
            up_to_date = (last_log_term > my_last_term) or (last_log_term == my_last_term and last_log_index >= my_last_index)

            if can_vote and up_to_date:
                self.voted_for = candidate
                # reset election timer
                asyncio.create_task(self.reset_election_timer())
                return {"term": self.current_term, "vote_granted": True}
            else:
                return {"term": self.current_term, "vote_granted": False}

    async def on_append_entries(self, msg):
        async with self._lock:
            term = msg["term"]
            leader = msg["leader"]
            prev_log_index = msg["prev_log_index"]
            prev_log_term = msg["prev_log_term"]
            entries = msg.get("entries", [])
            leader_commit = msg.get("leader_commit", -1)

            # Reply false if term < currentTerm
            if term < self.current_term:
                return {"term": self.current_term, "success": False}

            # We see a valid leader
            self.current_term = term
            self.role = "follower"
            # reset election timer
            asyncio.create_task(self.reset_election_timer())

            # Check if log contains entry at prev_log_index with prev_log_term
            if prev_log_index >= 0:
                if prev_log_index >= len(self.log):
                    return {"term": self.current_term, "success": False}
                if self.log[prev_log_index]["term"] != prev_log_term:
                    # conflict -> delete from conflict onward
                    self.log = self.log[:prev_log_index]
                    return {"term": self.current_term, "success": False}

            # Append any new entries not already in the log
            for i, e in enumerate(entries):
                idx = prev_log_index + 1 + i
                if idx < len(self.log):
                    if self.log[idx]["term"] != e["term"]:
                        # delete the existing entry and all that follow it
                        self.log = self.log[:idx]
                        self.log.append(e)
                else:
                    self.log.append(e)

            # Update commit index
            if leader_commit > self.commit_index:
                self.commit_index = min(leader_commit, len(self.log)-1)

            return {"term": self.current_term, "success": True, "match_index": len(self.log)-1}

    async def on_client_append(self, msg):
        # clients send to leader. If not leader, reply with redirect info
        async with self._lock:
            if self.role != "leader":
                return {"ok": False, "leader": getattr(self, "leader_id", None)}
            # append to leader log
            entry = {"term": self.current_term, "command": msg["command"]}
            self.log.append(entry)
            # initialize match/next index for new entry for leader state
            # we'll replicate asynchronously here
            # return index assigned
            idx = len(self.log)-1
            # start replication task
            asyncio.create_task(self.replicate_log())
            return {"ok": True, "index": idx}

    async def on_get_state(self, msg):
        async with self._lock:
            return {
                "term": self.current_term,
                "role": self.role,
                "commit_index": self.commit_index,
                "log": self.log,
                "leader": getattr(self, "leader_id", None),
            }

    # ---- Election / Heartbeat / Replication ----
    async def reset_election_timer(self):
        # cancel previous
        if self._election_timer_handle:
            self._election_timer_handle.cancel()
        timeout = random.uniform(*ELECTION_TIMEOUT_RANGE)
        loop = asyncio.get_running_loop()
        self._election_timer_handle = loop.call_later(timeout, lambda: asyncio.create_task(self.start_election()))

    async def start_election(self):
        async with self._lock:
            if self.role == "leader" or self._stopping:
                return
            self.role = "candidate"
            self.current_term += 1
            self.voted_for = self.name
            self.votes_received = {self.name}
            term_at_start = self.current_term
            last_log_index = len(self.log) - 1
            last_log_term = self.log[-1]["term"] if self.log else 0

        # send RequestVote RPCs to peers
        tasks = []
        for p in self.peers:
            msg = {
                "type": "RequestVote",
                "term": term_at_start,
                "candidate": self.name,
                "last_log_index": last_log_index,
                "last_log_term": last_log_term,
            }
            tasks.append(asyncio.create_task(self.rpc_call(p, msg)))
        # wait a bit for replies
        done = await asyncio.gather(*tasks, return_exceptions=True)
        async with self._lock:
            for resp in done:
                if not resp or not isinstance(resp, dict):
                    continue
                if resp.get("term", 0) > self.current_term:
                    self.current_term = resp["term"]
                    self.role = "follower"
                    self.voted_for = None
                elif resp.get("vote_granted"):
                    self.votes_received.add(resp.get("voter", "unknown"))  # note: peers don't include voter name here
                    # we can infer vote by counting True replies
                    # (we'll simply count successes below)

            # Count votes as number of True responses + self
            votes = 1 + sum(1 for r in done if isinstance(r, dict) and r.get("vote_granted"))
            if self.role == "candidate" and votes > (len(self.peers) + 1) // 2:
                # become leader
                self.role = "leader"
                self.leader_id = self.name
                # initialize leader state
                next_idx = len(self.log)
                for p in self.peers:
                    self.next_index[f"{p[0]}:{p[1]}"] = next_idx
                    self.match_index[f"{p[0]}:{p[1]}"] = -1
                # start heartbeat task
                t = asyncio.create_task(self.leader_heartbeat_loop())
                self._tasks.append(t)
            else:
                # remain follower/candidate; reset election timer
                asyncio.create_task(self.reset_election_timer())

    async def leader_heartbeat_loop(self):
        while not self._stopping and self.role == "leader":
            await self.send_heartbeats()
            await asyncio.sleep(HEARTBEAT_INTERVAL)

    async def send_heartbeats(self):
        # leader sends AppendEntries with no entries as heartbeats
        async with self._lock:
            term = self.current_term
            leader = self.name
        tasks = []
        for p in self.peers:
            prev_index = len(self.log) - 1
            prev_term = self.log[prev_index]["term"] if prev_index >= 0 else 0
            msg = {
                "type": "AppendEntries",
                "term": term,
                "leader": leader,
                "prev_log_index": prev_index,
                "prev_log_term": prev_term,
                "entries": [],
                "leader_commit": self.commit_index,
            }
            tasks.append(asyncio.create_task(self.rpc_call(p, msg)))
        # collect replies and update match index if needed (heartbeats don't carry new entries)
        results = await asyncio.gather(*tasks, return_exceptions=True)
        # no extra handling for heartbeats for demo

    async def replicate_log(self):
        # simplistic replication: send full AppendEntries with entries not yet present on followers
        async with self._lock:
            if self.role != "leader":
                return
            term = self.current_term
            leader = self.name
            full_log = list(self.log)  # snapshot
        tasks = []
        for p in self.peers:
            prev_index = -1
            prev_term = 0
            # for simplicity send full log as entries to keep followers in sync
            msg = {
                "type": "AppendEntries",
                "term": term,
                "leader": leader,
                "prev_log_index": prev_index,
                "prev_log_term": prev_term,
                "entries": full_log,
                "leader_commit": self.commit_index,
            }
            tasks.append(asyncio.create_task(self.rpc_call(p, msg)))
        results = await asyncio.gather(*tasks, return_exceptions=True)
        # count successful matches
        match_count = 1  # leader itself
        for res in results:
            if isinstance(res, dict) and res.get("success"):
                match_count += 1
        # if majority replicated, commit the last entry
        async with self._lock:
            if len(self.log) > 0 and match_count > (len(self.peers)+1)//2:
                self.commit_index = len(self.log)-1

    # ----- lifecycle -----
    async def start(self):
        await self.start_server()
        await self.reset_election_timer()

    async def stop(self):
        self._stopping = True
        if self._election_timer_handle:
            self._election_timer_handle.cancel()
        for t in self._tasks:
            t.cancel()
        if self._server_task:
            self._server_task.cancel()

# ----- Demo Orchestration -----
async def demo():
    # create nodes
    nodes = []
    for i, (h, p) in enumerate(NODES):
        peers = [x for x in NODES if x != (h, p)]
        node = RaftNode(f"node{i+1}", h, p, peers)
        nodes.append(node)

    # start nodes
    servers = []
    for n in nodes:
        await n.start()
    # give them time to elect
    await asyncio.sleep(1.0)

    # query states
    states = []
    for (h, p) in NODES:
        try:
            reader, writer = await asyncio.open_connection(h, p)
            writer.write((json.dumps({"type": "GetState"}) + "\n").encode())
            await writer.drain()
            line = await reader.readline()
            st = json.loads(line.decode())
            states.append(((h, p), st))
            writer.close(); await writer.wait_closed()
        except:
            states.append(((h,p), None))

    print("=== Node states after startup/election ===")
    for (addr, st) in states:
        print(addr, "->", st["role"], "term", st["term"], "commit", st["commit_index"])

    # find leader
    leader = None
    for addr, st in states:
        if st and st["role"] == "leader":
            leader = addr
            break
    if not leader:
        print("No leader found (retrying shortly)...")
        await asyncio.sleep(1.0)
        # query again
        for (h, p) in NODES:
            try:
                reader, writer = await asyncio.open_connection(h, p)
                writer.write((json.dumps({"type": "GetState"}) + "\n").encode())
                await writer.drain()
                line = await reader.readline()
                st = json.loads(line.decode())
                states.append(((h, p), st))
                writer.close(); await writer.wait_closed()
                if st["role"] == "leader":
                    leader = (h, p)
                    break
            except:
                pass
    if not leader:
        print("Still no leader — quitting demo.")
        for n in nodes:
            await n.stop()
        return

    print("Leader elected at:", leader)

    # send a client append to leader
    print("Sending client append -> 'set x=1' to leader...")
    reader, writer = await asyncio.open_connection(leader[0], leader[1])
    writer.write((json.dumps({"type": "ClientAppend", "command": "set x=1"}) + "\n").encode())
    await writer.drain()
    line = await reader.readline()
    resp = json.loads(line.decode())
    writer.close(); await writer.wait_closed()
    print("Client append response:", resp)

    await asyncio.sleep(0.8)  # wait for replication

    # show logs on each node
    print("\n=== Logs and commit_index on each node ===")
    for (h, p) in NODES:
        try:
            reader, writer = await asyncio.open_connection(h, p)
            writer.write((json.dumps({"type": "GetState"}) + "\n").encode())
            await writer.drain()
            line = await reader.readline()
            st = json.loads(line.decode())
            writer.close(); await writer.wait_closed()
            print(f"{(h,p)}: role={st['role']} term={st['term']} commit={st['commit_index']} log={st['log']}")
        except Exception as e:
            print(f"{(h,p)}: error {e}")

    # stop nodes
    for n in nodes:
        await n.stop()

# Run the demo
asyncio.run(demo())
