package ca.etsmtl.gti785.lib.repository;

import android.util.Log;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueRepository {

    private final Map<UUID, BlockingQueue<Object>> queues; // TODO: Replace Object by Event or something

    public QueueRepository() {
        queues = new ConcurrentHashMap<>();
    }

    public BlockingQueue<Object> get(UUID uuid) {
        return queues.get(uuid);
    }

    public BlockingQueue<Object> getOrCreate(UUID uuid) {
        BlockingQueue<Object> queue = queues.get(uuid);

        if (queue == null) {
            queue = new LinkedBlockingQueue<>();
        }

        return queue;
    }

    public Collection<BlockingQueue<Object>> getAll() {
        return queues.values();
    }

    public void add(UUID uuid, BlockingQueue<Object> queue) {
        queues.put(uuid, queue);
    }

    public void putAll(Object event) {
        for (BlockingQueue<Object> queue : queues.values()) {
            try {
                queue.put(event);
            } catch (InterruptedException e) {
                Log.e("QueueRepository", "Exception occurred while appending event to queue", e);
            }
        }
    }

    public void remove(UUID uuid) {
        queues.remove(uuid);
    }

    public void removeAll() {
        queues.clear();
    }
}
