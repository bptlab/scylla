package de.hpi.bpt.scylla.plugin.batch;

// This class holds the state a batch cluster passes during its lifecycle
enum BatchClusterState {
    INIT, READY, MAXLOADED, RUNNING, TERMINATED;
}
