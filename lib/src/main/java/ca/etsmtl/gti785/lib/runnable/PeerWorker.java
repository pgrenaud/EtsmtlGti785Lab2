package ca.etsmtl.gti785.lib.runnable;

import android.util.Log;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import ca.etsmtl.gti785.lib.entity.PeerEntity;
import ca.etsmtl.gti785.lib.handler.PeerHive;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.util.EntityUtils;

public class PeerWorker implements Runnable {

    private final PeerHive hive;
    private final PeerEntity peer;
    private final CloseableHttpClient client;

    private final String pingUrl;
    private final String pollingUrl;

    private volatile boolean running;
    private boolean available = false;

    public PeerWorker(PeerHive hive, PeerEntity peer) {
        this.hive = hive;
        this.peer = peer;

        client = HttpClients.createDefault();

        pingUrl = "/api/v1/ping";
        pollingUrl = "/api/v1/polling/" + peer.getUUID().toString();
    }

    @Override
    public void run() {
        running = true;

        Log.d("PeerWorker", "starting worker: " + peer.getDisplayName());

        try {
            // TODO
            performHttpGetJson(pingUrl, new HttpResponseCallback() {
                @Override
                public void onHttpResponse(int status, String content) {
                    if (status == 200) {
                        Log.d("PeerWorker", peer.getDisplayName() + " is online");

                        peer.setOnline(true);
                        available = true;

                        if (hive.getService().getListener() != null) {
                            hive.getService().getListener().onPeerConnection(peer);
                        }
                    }
                }
                @Override
                public void onIOException(IOException exception) {
                    available = false;
                }
            });

//            while (running && available) {
//                performHttpGetJson(pollingUrl, new HttpResponseCallback() {
//                    @Override
//                    public void onHttpResponse(int status, String content) {
//                        if (status == 418) {
//                            // Polling timeout
//                        } else if (status == 200) {
//                            // Handle event
//                        } else {
//                            available = false;
//                        }
//                    }
//
//                    @Override
//                    public void onIOException(IOException exception) {
//                        available = false;
//                    }
//                });
//            }
        } catch (URISyntaxException e) {
            Log.e("PeerWorker", "Exception occurred while building URI", e);
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

        // TODO: Cleanup
        try {
            client.close();
        } catch (IOException e) {
            Log.e("PeerWorker", "Exception occurred while closing http client", e);
        }
    }

    public void performHttpGetJson(String path, HttpResponseCallback callback) throws URISyntaxException {
        try {
            URI uri = new URIBuilder()
                .setScheme("http")
                .setHost(peer.getHost())
                .setPath(path)
                .build();

            HttpGet get = new HttpGet(uri);
            CloseableHttpResponse response = client.execute(get);

            int status = response.getStatusLine().getStatusCode();

            try {
                HttpEntity entity = response.getEntity();
                String json = EntityUtils.toString(entity);
                EntityUtils.consume(entity);

                callback.onHttpResponse(status, json);
            } finally {
                response.close();
            }
        } catch (IOException e) {
            callback.onIOException(e);
        }
    }

    public PeerEntity getPeerEntity() {
        return peer;
    }

    public interface HttpResponseCallback {
        void onHttpResponse(int status, String content);
        void onIOException(IOException exception);
    }
}
