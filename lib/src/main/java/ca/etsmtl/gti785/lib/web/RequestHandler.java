package ca.etsmtl.gti785.lib.web;

import static ca.etsmtl.gti785.lib.web.RoutableWebServer.sendError;
import static ca.etsmtl.gti785.lib.web.RoutableWebServer.sendEvent;
import static ca.etsmtl.gti785.lib.web.RoutableWebServer.sendJSON;
import static ca.etsmtl.gti785.lib.web.RoutableWebServer.sendServerError;
import static ca.etsmtl.gti785.lib.web.RoutableWebServer.sendStream;
import static ca.etsmtl.gti785.lib.web.RoutableWebServer.sendTimeout;

import android.util.Log;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import ca.etsmtl.gti785.lib.entity.EventEntity;
import ca.etsmtl.gti785.lib.entity.FileEntity;
import ca.etsmtl.gti785.lib.entity.PeerEntity;
import ca.etsmtl.gti785.lib.peering.PeerHive;
import ca.etsmtl.gti785.lib.repository.FileRepository;
import ca.etsmtl.gti785.lib.repository.PeerRepository;
import ca.etsmtl.gti785.lib.repository.QueueRepository;

import fi.iki.elonen.NanoHTTPD.Response;

public class RequestHandler {

    private static final long REQUEST_TIMEOUT = 60;

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
        PeerEntity peer = peerRepository.get(uuid);

        if (peer != null) {
            // Try to start worker if not already running
            peerHive.spawnWorker(peer);
        }

        BlockingQueue<EventEntity> queue = queueRepository.getOrCreate(uuid);

        try {
            EventEntity event = queue.poll(REQUEST_TIMEOUT, TimeUnit.SECONDS);

            if (event != null) {
                Log.d("RequestHandler", "Handling event " + event.getEvent());

                return sendEvent(event);
            } else {
                return sendTimeout();
            }
        } catch (InterruptedException e) {
            return sendServerError("SERVER INTERNAL ERROR: InterruptedException: " + e.getMessage());
        }
    }

    public Response handleFileList() {
        return sendJSON(fileRepository.encode());
    }

    public Response handleFileRequest(UUID uuid) {
        FileEntity file = fileRepository.get(uuid);

        if (file != null) {
            return sendStream(file.getFile());
        } else {
            return sendError("Could not find file with UUID '" + uuid + "'.");
        }
    }
}
