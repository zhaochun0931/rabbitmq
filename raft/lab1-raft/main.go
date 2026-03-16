package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"io"
	"net"
	"net/http"
	"os"
	"path/filepath"
	"sync"
	"time"

	"github.com/hashicorp/raft"
	raftbolt "github.com/hashicorp/raft-boltdb/v2"
)

// --- 1. Database State Machine (FSM) ---
// This holds your actual data.
type DatabaseFSM struct {
	mu sync.RWMutex
	DB map[string]string
}

// Apply is called by Raft once a log is committed. We update our local DB here.
func (fsm *DatabaseFSM) Apply(l *raft.Log) interface{} {
	var payload struct {
		Key   string `json:"key"`
		Value string `json:"value"`
	}
	if err := json.Unmarshal(l.Data, &payload); err != nil {
		return err
	}

	fsm.mu.Lock()
	defer fsm.mu.Unlock()
	fsm.DB[payload.Key] = payload.Value
	fmt.Printf("[DB] Inserted: %s = %s\n", payload.Key, payload.Value)
	return nil
}

// Snapshot saves the current state of the DB to disk.
func (fsm *DatabaseFSM) Snapshot() (raft.FSMSnapshot, error) {
	fsm.mu.RLock()
	defer fsm.mu.RUnlock()
	// Copy the map to ensure we don't hold the lock while writing to disk
	dbCopy := make(map[string]string)
	for k, v := range fsm.DB {
		dbCopy[k] = v
	}
	return &dbSnapshot{dbState: dbCopy}, nil
}

// Restore loads the DB state from disk when the node restarts.
func (fsm *DatabaseFSM) Restore(rc io.ReadCloser) error {
	fsm.mu.Lock()
	defer fsm.mu.Unlock()
	if err := json.NewDecoder(rc).Decode(&fsm.DB); err != nil {
		return err
	}
	fmt.Printf("[DB] Restored %d keys from disk.\n", len(fsm.DB))
	return nil
}

type dbSnapshot struct {
	dbState map[string]string
}

func (s *dbSnapshot) Persist(sink raft.SnapshotSink) error {
	err := json.NewEncoder(sink).Encode(s.dbState)
	if err != nil {
		sink.Cancel()
		return err
	}
	return sink.Close()
}
func (s *dbSnapshot) Release() {}

// --- 2. Main Application ---
func main() {
	nodeID := flag.String("id", "node1", "Unique Node ID")
	raftPort := flag.String("raft", "7000", "Raft TCP Port")
	httpPort := flag.String("http", "8000", "REST API Port")
	isBootstrap := flag.Bool("bootstrap", false, "Start as the cluster leader")
	flag.Parse()

	// Create a dedicated folder for this node's data
	baseDir := filepath.Join("data", *nodeID)
	os.MkdirAll(baseDir, 0755)

	raftAddr := fmt.Sprintf("127.0.0.1:%s", *raftPort)
	config := raft.DefaultConfig()
	config.LocalID = raft.ServerID(*nodeID)

	// --- PERSISTENCE LAYER ---
	// BoltDB stores the Raft transaction logs
	logStore, err := raftbolt.NewBoltStore(filepath.Join(baseDir, "raft-log.db"))
	if err != nil { panic(err) }
	stableStore, err := raftbolt.NewBoltStore(filepath.Join(baseDir, "raft-stable.db"))
	if err != nil { panic(err) }
	// FileSnapshotStore stores the actual JSON map of our database
	snapshotStore, err := raft.NewFileSnapshotStore(baseDir, 3, os.Stderr)
	if err != nil { panic(err) }

	// Network Transport
	addr, _ := net.ResolveTCPAddr("tcp", raftAddr)
	transport, _ := raft.NewTCPTransport(raftAddr, addr, 3, 10*time.Second, os.Stderr)

	// Initialize our Database FSM
	fsm := &DatabaseFSM{DB: make(map[string]string)}

	// Create the Raft Node
	r, err := raft.NewRaft(config, fsm, logStore, stableStore, snapshotStore, transport)
	if err != nil { panic(err) }

	// --- BOOTSTRAP LOGIC ---
	// We check if data already exists on disk. If it does, we DO NOT bootstrap,
	// we just let Raft recover naturally from the local DB files.
	hasState, _ := raft.HasExistingState(logStore, stableStore, snapshotStore)
	if *isBootstrap && !hasState {
		fmt.Println("No existing data found. Bootstrapping new cluster...")
		r.BootstrapCluster(raft.Configuration{
			Servers: []raft.Server{{ID: config.LocalID, Address: transport.LocalAddr()}},
		})
	} else if hasState {
		fmt.Println("Found existing data on disk. Recovering state...")
	}

	// --- 3. REST API Server ---
	go func() {
		// API: Join Cluster
		http.HandleFunc("/join", func(w http.ResponseWriter, req *http.Request) {
			if r.State() != raft.Leader {
				http.Error(w, "Error: Must join via Leader", http.StatusForbidden)
				return
			}
			id, addr := req.URL.Query().Get("id"), req.URL.Query().Get("addr")
			r.AddVoter(raft.ServerID(id), raft.ServerAddress(addr), 0, 0)
			fmt.Fprintf(w, "Node %s joined successfully.\n", id)
		})

		// API: Insert/Update Data (Write)
		http.HandleFunc("/set", func(w http.ResponseWriter, req *http.Request) {
			if r.State() != raft.Leader {
				http.Error(w, "Error: Must write to Leader", http.StatusForbidden)
				return
			}
			key, val := req.URL.Query().Get("key"), req.URL.Query().Get("value")
			
			// Package the data as JSON and propose it to the Raft cluster
			payload, _ := json.Marshal(map[string]string{"key": key, "value": val})
			future := r.Apply(payload, 2*time.Second)
			
			if err := future.Error(); err != nil {
				http.Error(w, "Failed to write data", http.StatusInternalServerError)
				return
			}
			fmt.Fprintf(w, "Success! %s = %s\n", key, val)
		})

		// API: Read Data (Read)
		http.HandleFunc("/get", func(w http.ResponseWriter, req *http.Request) {
			key := req.URL.Query().Get("key")
			fsm.mu.RLock()
			val, exists := fsm.DB[key]
			fsm.mu.RUnlock()

			if !exists {
				http.Error(w, "Key not found", http.StatusNotFound)
				return
			}
			fmt.Fprintf(w, "%s\n", val)
		})

		// API: Cluster Status
		http.HandleFunc("/info", func(w http.ResponseWriter, req *http.Request) {
			future := r.GetConfiguration()
			_ = future.Error()
			fmt.Fprintf(w, "Node: %s | State: %s\nData Count: %d keys\n", *nodeID, r.State(), len(fsm.DB))
		})

		fmt.Printf("Database API running on http://localhost:%s\n", *httpPort)
		http.ListenAndServe(":"+*httpPort, nil)
	}()

	select {}
}
