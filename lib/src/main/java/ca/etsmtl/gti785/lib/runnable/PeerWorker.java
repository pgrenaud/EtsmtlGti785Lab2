package ca.etsmtl.gti785.lib.runnable;

import android.util.Log;

import java.io.IOException;

import ca.etsmtl.gti785.lib.entity.PeerEntity;
import ca.etsmtl.gti785.lib.hive.PeerHive;
import ca.etsmtl.gti785.lib.web.HttpClientWrapper;
import ca.etsmtl.gti785.lib.web.HttpClientWrapper.HttpResponseCallback;
import cz.msebera.android.httpclient.conn.HttpHostConnectException;

public class PeerWorker implements Runnable {

    private final PeerHive hive;
    private final PeerEntity peer;
    private final HttpClientWrapper client;

    private final String pingUrl;
    private final String pollingUrl;

    private volatile boolean running;
    private boolean available = false;

    public PeerWorker(PeerHive hive, PeerEntity peer) {
        this.hive = hive;
        this.peer = peer;

        client = new HttpClientWrapper(peer.getHost());

        pingUrl = "/api/v1/ping";
        pollingUrl = "/api/v1/polling/" + hive.getService().getSelfPeerEntity().getUUID().toString();

        running = false;
        available = false;
    }

    @Override
    public void run() {
        running = true;

        Log.d("PeerWorker", "starting worker: " + peer.getDisplayName());
        Log.d("PeerWorker", "starting worker: " + peer.getUUID().toString());

        try {
            // TODO
            client.performHttpGet(pingUrl, new HttpResponseCallback() {
                @Override
                public void onHttpResponse(int status, String content) {
                    Log.d("PeerWorker", "ping: onHttpResponse: " + status);

                    if (status == 200) {
                        Log.d("PeerWorker", peer.getDisplayName() + " is online");

                        available = true;
                        peer.setOnline(true);
                        notifyListener();
                    }
                }
                @Override
                public void onException(Exception exception) {
                    if (exception instanceof HttpHostConnectException) {
                        Log.d("PeerWorker", "ping: " + exception.getMessage());
                    } else {
                        Log.d("PeerWorker", "ping: onException", exception);
                    }
                }
            });

            while (running && available) {
                client.performHttpGet(pollingUrl, new HttpResponseCallback() {
                    @Override
                    public void onHttpResponse(int status, String content) {
                        Log.d("PeerWorker", "polling");

                        if (status == 408) {
                            // Polling timeout
                        } else if (status == 200) {
                            // TODO: Handle event
                        } else {
                            Log.d("PeerWorker", "polling: onHttpResponse: " + status);

                            available = false;
                            peer.setOnline(false);
                            notifyListener();
                        }
                    }
                    @Override
                    public void onException(Exception exception) {
                        if (exception instanceof HttpHostConnectException) {
                            Log.d("PeerWorker", "polling: " + exception.getMessage());
                        } else {
                            Log.d("PeerWorker", "polling: onException", exception);
                        }

                        available = false;
                        peer.setOnline(false);
                        notifyListener();
                    }
                });
            }
        } finally {
            stop();
        }
    }

    public void stop() {
        if (!running) {
            return; // Already stopped, nothing to do.
        }

        Log.d("PeerWorker", "stopping worker: " + peer.getDisplayName());

        running = false;

        if (available) {
            Log.d("PeerWorker", "worker was not stopped properly");

            available = false;
            peer.setOnline(false);
            notifyListener();
        }

        // TODO: Cleanup

        try {
            client.close();
        } catch (IOException e) {
            Log.e("PeerWorker", "Exception occurred while closing http client", e);
        }
    }

    public PeerEntity getPeerEntity() {
        return peer;
    }

    public boolean isRunning() {
        return running;
    }

    private void notifyListener() {
        if (hive.getService().getListener() != null) {
            hive.getService().getListener().onPeerConnection(hive.getService().getPeerRepository(), peer);
        }
    }
}
