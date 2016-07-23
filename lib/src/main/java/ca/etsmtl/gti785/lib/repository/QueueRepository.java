package ca.etsmtl.gti785.lib.repository;

import android.util.Log;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import ca.etsmtl.gti785.lib.entity.EventEntity;

public class QueueRepository {

    private final Map<UUID, BlockingQueue<EventEntity>> queues; // TODO: Replace Object by Event or something

    public QueueRepository() {
        queues = new ConcurrentHashMap<>();
    }

    public BlockingQueue<EventEntity> get(UUID uuid) {
        return queues.get(uuid);
    }

    public BlockingQueue<EventEntity> getOrCreate(UUID uuid) {
        BlockingQueue<EventEntity> queue = get(uuid);

        if (queue == null) {
            queue = new LinkedBlockingQueue<>();

            add(uuid, queue);
        }

        return queue;
    }

    public Collection<BlockingQueue<EventEntity>> getAll() {
        return queues.values();
    }

    public void add(UUID uuid, BlockingQueue<EventEntity> queue) {
        queues.put(uuid, queue);
    }

    public void putAll(EventEntity event) {
        Log.d("QueueRepository", "putAll: event " + event.encode());
        Log.d("QueueRepository", "putAll: queues " + queues.size());

        for (BlockingQueue<EventEntity> queue : queues.values()) {
            try {
                queue.put(event);
                Log.d("QueueRepository", "putAll: queue");
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
