package ca.etsmtl.gti785.lib.handler;

import android.util.Log;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import ca.etsmtl.gti785.lib.entity.PeerEntity;
import ca.etsmtl.gti785.lib.repository.PeerRepository;
import ca.etsmtl.gti785.lib.runnable.PeerWorker;
import ca.etsmtl.gti785.lib.service.PeerService;

public class PeerHive {

    private static final int POOL_SIZE = 32;

    private final ExecutorService pool;
    private final ConcurrentHashMap<UUID, PeerWorker> workers;
    private final PeerService service;
    private final PeerRepository peers;

    public PeerHive(PeerService service, PeerRepository peers) {
        this.service = service;
        this.peers = peers;

        pool = Executors.newFixedThreadPool(POOL_SIZE);
        workers = new ConcurrentHashMap<>();
    }

    /**
     * Synchronizing peer hive by starting missing workers and stopping unneeded ones.
     * Call that method anytime you add or remove a peer from the repository.
     */
    public void sync() {
        // Stopping workers whom peer has been removed
        for (PeerWorker worker : workers.values()) {
            PeerEntity peer = peers.get(worker.getPeerEntity().getUUID());

            if (peer == null) {
                worker.stop();
                workers.remove(worker.getPeerEntity().getUUID());
            }
        }

        // Starting workers whom peer has been added
        for (PeerEntity peer : peers.getAll()) {
            PeerWorker worker = workers.get(peer.getUUID());

            if (worker == null) {
                try {
                    worker = new PeerWorker(this, peer);

                    workers.put(peer.getUUID(), worker);
                    pool.submit(worker);
                } catch (RejectedExecutionException e) {
                    Log.e("PeerHive", "Exception occurred while submitting new peer worker to thread pool", e);
                }
            }
        }
    }

    public void stop() {
        for (PeerWorker worker : workers.values()) {
            worker.stop();
        }

        try {
            pool.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e("PeerHive", "Exception occurred while waiting thread pool to terminate", e);
        }
    }

    public PeerService getService() {
        return service;
    }
}
