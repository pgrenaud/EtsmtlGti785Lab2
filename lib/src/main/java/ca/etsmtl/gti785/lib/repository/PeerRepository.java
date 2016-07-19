package ca.etsmtl.gti785.lib.repository;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import ca.etsmtl.gti785.lib.entity.PeerEntity;

public class PeerRepository {

    private final Map<UUID, PeerEntity> peers;

    public PeerRepository() {
        peers = new ConcurrentHashMap<>();
    }


    public PeerEntity get(UUID uuid) {
        return peers.get(uuid);
    }

    public Collection<PeerEntity> getAll() {
        return peers.values();
    }

    public void add(PeerEntity peer) {
        peers.put(peer.getUuid(), peer);
    }

    public void remove(PeerEntity peer) {
        peers.remove(peer.getUuid());
    }

    public void removeAll() {
        peers.clear();
    }
}
