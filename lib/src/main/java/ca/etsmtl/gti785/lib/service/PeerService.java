package ca.etsmtl.gti785.lib.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;

import ca.etsmtl.gti785.lib.R;
import ca.etsmtl.gti785.lib.entity.PeerEntity;
import ca.etsmtl.gti785.lib.web.RequestHandler;
import ca.etsmtl.gti785.lib.peering.PeerHive;
import ca.etsmtl.gti785.lib.repository.FileRepository;
import ca.etsmtl.gti785.lib.repository.PeerRepository;
import ca.etsmtl.gti785.lib.repository.QueueRepository;
import ca.etsmtl.gti785.lib.web.RoutableWebServer;

public class PeerService extends Service {

    public static final String EXTRA_DIRECTORY_PATH = "ca.etsmtl.gti785.lib.service.EXTRA_DIRECTORY_PATH"; // FIXME: Fix package
    public static final String EXTRA_PEER_NAME = "ca.etsmtl.gti785.lib.service.EXTRA_PEER_NAME"; // FIXME: Fix package
    public static final String EXTRA_SERVER_PORT = "ca.etsmtl.gti785.lib.service.EXTRA_SERVER_PORT"; // FIXME: Fix package

    public static final int DEFAULT_SERVER_PORT = 8099;

    private final IBinder binder = new PeerServiceBinder();
    private final QueueRepository queueRepository = new QueueRepository();
    private final FileRepository fileRepository = new FileRepository();
    private final PeerRepository peerRepository = new PeerRepository();
    private final PeerHive peerHive = new PeerHive(this, peerRepository);

    private PeerServiceListener listener;
    private PeerEntity selfPeer;
    private RoutableWebServer server;

    private boolean running = false;

    @Override
    public void onCreate() {
        Log.d("PeerService", "Creating PeerService");

        // Loading peers from persistent storage
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getService());
        String json = prefs.getString(getString(R.string.pref_json_peer_list_key), null);

        if (json != null) {
            peerRepository.addAll(PeerRepository.decode(json));
        }

        Log.d("PeerService", "PeerService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Skip initialization if already running
        if (!running) {
            Log.d("PeerService", "Starting PeerService");

            String path = intent.getStringExtra(EXTRA_DIRECTORY_PATH);
            if (path != null) {
                fileRepository.addAll(path);
            }

            String peerName = intent.getStringExtra(EXTRA_PEER_NAME);
            if (path == null) {
                peerName = getString(R.string.pref_peer_name_default);
            }

            int serverPort = intent.getIntExtra(EXTRA_SERVER_PORT, DEFAULT_SERVER_PORT);

            // TODO: Move to a proper place
            WifiManager wifiMgr = (WifiManager) getService().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();
            String serverAddress = Formatter.formatIpAddress(ip);

            selfPeer = new PeerEntity(peerName, serverAddress, serverPort);
            Log.d("PeerService", "Self peer is " + selfPeer);

            RequestHandler requestHandler = new RequestHandler(queueRepository, fileRepository, peerRepository, peerHive);

            server = new RoutableWebServer(serverPort, requestHandler);

            try {
                server.start();
            } catch (IOException e) {
                Log.e("PeerService", "Failed to start RoutableWebServer", e); // TODO: Send error to client
            }

            running = true;

            Log.d("PeerService", "PeerService started");
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("PeerService", "PeerService bound");

        if (!running) {
            throw new IllegalStateException("Service must be started before it can be bonded.");
        }

        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("PeerService", "PeerService unbound");

        return false;
    }

    @Override
    public void onDestroy() {
        if (running) {
            Log.d("PeerService", "Destroying PeerService");

            peerHive.stop();
            server.stop();

            // Saving peers to persistent storage
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getService());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.pref_json_peer_list_key), peerRepository.encode());
            editor.apply();

            Log.d("PeerService", "PeerService destroyed");
        }
    }

    public PeerServiceListener getListener() {
        return listener;
    }

    public void setListener(PeerServiceListener listener) {
        this.listener = listener;
    }

    public QueueRepository getQueueRepository() {
        return queueRepository;
    }

    public FileRepository getFileRepository() {
        return fileRepository;
    }

    public PeerRepository getPeerRepository() {
        return peerRepository;
    }

    public PeerHive getPeerHive() {
        return peerHive;
    }

    public PeerEntity getSelfPeerEntity() {
        return selfPeer;
    }

    public Service getService() {
        return this;
    }

    public class PeerServiceBinder extends Binder {
        public PeerService getService() {
            return PeerService.this;
        }
    }

    public interface PeerServiceListener {
        void onPeerConnection(PeerEntity peerEntity);
        void onPeerDisplayNameUpdate(PeerEntity peerEntity);
        void onPeerLocationUpdate(PeerEntity peerEntity);
    }
}
