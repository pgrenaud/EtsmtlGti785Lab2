package ca.etsmtl.gti785.lib.repository;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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
        peers.put(peer.getUUID(), peer);
    }

    /**
     *
     * @param peer
     * @return Returns true if the PeerEntity was added to the repository or false if it was updated.
     */
    public boolean addOrUpdate(PeerEntity peer) {
        PeerEntity localPeer = peers.get(peer.getUUID());

        if (localPeer == null) {
            peers.put(peer.getUUID(), peer);

            return true;
        } else {
            localPeer.setDisplayName(peer.getDisplayName());

            return false;
        }
    }

    public void addAll(Collection<PeerEntity> peerEntities) {
        for (PeerEntity peer : peerEntities) {
            add(peer);
        }
    }

    public void remove(PeerEntity peer) {
        peers.remove(peer.getUUID());
    }

    public void removeAll() {
        peers.clear();
    }

    public String encode() {
        Gson gson = new Gson();

        return gson.toJson(getAll());
    }

    public static Collection<PeerEntity> decode(String json) throws JsonSyntaxException {
        Gson gson = new Gson();
        Type type = new TypeToken<Collection<PeerEntity>>(){}.getType();

        return gson.fromJson(json, type);
    }
}
