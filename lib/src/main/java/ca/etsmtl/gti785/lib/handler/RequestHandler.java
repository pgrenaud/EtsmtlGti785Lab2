package ca.etsmtl.gti785.lib.handler;

import android.util.Log;

import static ca.etsmtl.gti785.lib.web.WebServer.sendEmpty;
import static ca.etsmtl.gti785.lib.web.WebServer.sendError;
import static ca.etsmtl.gti785.lib.web.WebServer.sendEvent;
import static ca.etsmtl.gti785.lib.web.WebServer.sendJSON;
import static ca.etsmtl.gti785.lib.web.WebServer.sendServerError;
import static ca.etsmtl.gti785.lib.web.WebServer.sendStream;
import static ca.etsmtl.gti785.lib.web.WebServer.sendTimeout;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import ca.etsmtl.gti785.lib.entity.EventEntity;
import ca.etsmtl.gti785.lib.entity.FileEntity;
import ca.etsmtl.gti785.lib.entity.PeerEntity;
import ca.etsmtl.gti785.lib.hive.PeerHive;
import ca.etsmtl.gti785.lib.repository.FileRepository;
import ca.etsmtl.gti785.lib.repository.PeerRepository;
import ca.etsmtl.gti785.lib.repository.QueueRepository;
import fi.iki.elonen.NanoHTTPD.Response;

public class RequestHandler {

    private static final long REQUEST_TIMEOUT = 60; // FIXME: Change for 60

    private final QueueRepository queueRepository;
    private final FileRepository fileRepository;
    private final PeerRepository peerRepository;
    private final PeerHive peerHive;

    public RequestHandler(QueueRepository queueRepository, FileRepository fileRepository, PeerRepository peerRepository, PeerHive peerHive) {
        this.queueRepository = queueRepository;
        this.fileRepository = fileRepository;
        this.peerRepository = peerRepository;
        this.peerHive = peerHive;
    }

    public Response handlePolling(UUID uuid) {
        Log.d("RequestHandler", "handlePolling: " + uuid.toString());
        Log.d("RequestHandler", "handlePolling: " + peerRepository.encode());

        PeerEntity peer = peerRepository.get(uuid);

        if (peer != null) {
            Log.d("RequestHandler", "handlePolling: " + peer.getDisplayName());
            peerHive.spawnWorker(peer);
        }

        BlockingQueue<EventEntity> queue = queueRepository.getOrCreate(uuid);

        try {
            EventEntity event = queue.poll(REQUEST_TIMEOUT, TimeUnit.SECONDS); // FIXME

            if (event != null) {
                Log.d("RequestHandler", "handlePolling: event " + event.encode());

                return sendEvent(event);
            } else {
                return sendTimeout();
            }
        } catch (InterruptedException e) {
            return sendServerError("SERVER INTERNAL ERROR: InterruptedException: " + e.getMessage());
        }
    }

    public Response handleFileList() {
        Collection<FileEntity> files = fileRepository.getAll();

        return sendJSON(files);
    }

    public Response handleFileRequest(UUID uuid) {
        FileEntity file = fileRepository.get(uuid);

        if (file != null) {
            return sendStream(file.getFile());
        } else {
            return sendError("Could not find file with UUID '" + uuid.toString().toUpperCase() + "'.");
        }
    }
}
