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
import java.util.Collection;

import ca.etsmtl.gti785.lib.R;
import ca.etsmtl.gti785.lib.entity.PeerEntity;
import ca.etsmtl.gti785.lib.handler.RequestHandler;
import ca.etsmtl.gti785.lib.hive.PeerHive;
import ca.etsmtl.gti785.lib.repository.FileRepository;
import ca.etsmtl.gti785.lib.repository.PeerRepository;
import ca.etsmtl.gti785.lib.repository.QueueRepository;
import ca.etsmtl.gti785.lib.web.WebServer;

public class PeerService extends Service {

    public static final String EXTRA_DIRECTORY_PATH = "ca.etsmtl.gti785.lib.service.EXTRA_DIRECTORY_PATH"; // FIXME: Fix package
    public static final String EXTRA_PEER_NAME = "ca.etsmtl.gti785.lib.service.EXTRA_PEER_NAME"; // FIXME: Fix package
    public static final String EXTRA_SERVER_PORT = "ca.etsmtl.gti785.lib.service.EXTRA_SERVER_PORT"; // FIXME: Fix package

    public static final String DEFAULT_PEER_NAME = "Default peer name";
    public static final int DEFAULT_SERVER_PORT = 8099;

    private final IBinder binder = new PeerServiceBinder();
    private final QueueRepository queueRepository = new QueueRepository();
    private final FileRepository fileRepository = new FileRepository();
    private final PeerRepository peerRepository = new PeerRepository();
    private final PeerHive peerHive = new PeerHive(this, peerRepository);

    private PeerServiceListener listener;
    private PeerEntity selfPeer;
    private WebServer server;

    private boolean running = false;

    @Override
    public void onCreate() {
        Log.d("PeerService", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("PeerService", "onStartCommand");

        // Skip initialization if already running
        if (!running) {
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getService());
//            String path = prefs.getString("server_directory", Environment.getExternalStorageDirectory().getPath()); // FIXME: Get from intent

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getService());
            String json = prefs.getString(getString(R.string.pref_json_peer_list_key), null);

            if (json != null) {
                Log.d("PeerService", "onStartCommand: loading " + json);
                peerRepository.addAll(PeerRepository.decode(json));
            }

            String path = intent.getStringExtra(EXTRA_DIRECTORY_PATH);
            if (path != null) {
                fileRepository.addAll(path);
            }

            String peerName = intent.getStringExtra(EXTRA_PEER_NAME);
            if (path == null) {
                peerName = DEFAULT_PEER_NAME;
            }

            int serverPort = intent.getIntExtra(EXTRA_SERVER_PORT, DEFAULT_SERVER_PORT);

            // TODO: Move to a proper place
            WifiManager wifiMgr = (WifiManager) getService().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();
            String serverAddress = Formatter.formatIpAddress(ip);

            selfPeer = new PeerEntity(peerName, serverAddress, serverPort);
            Log.d("PeerService", "onStartCommand: self " + selfPeer.getUUID().toString());

            // TODO: Load peers from persistent storage

            RequestHandler requestHandler = new RequestHandler(queueRepository, fileRepository, peerRepository, peerHive);

            server = new WebServer(serverPort, requestHandler);

            try {
                server.start();
            } catch (IOException e) {
                Log.e("PeerService", "start server failed", e); // TODO: Send error to client
            }

            running = true;
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("PeerService", "onBind");

        // FIXME: Throw exception if the service is not running
        if (!running) {
            throw new IllegalStateException("Service must be started before it can be bonded.");
        }

//        new Timer().schedule(
//                new TimerTask() {
//                    @Override
//                    public void run() {
//
//                        Log.d("TimerTask", "run");
//
//                        if (listener != null) {
//                            listener.onStuffHappen();
//                        }
//                    }
//                },
//                5000
//        );

        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("PeerService", "onRebind");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("PeerService", "onUnbind");

        return false;
    }

    @Override
    public void onDestroy() {
        Log.e("PeerService", "onDestroy");

        if (running) {
            peerHive.stop();
            server.stop();

            // TODO: Save peers to persistent storage
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getService());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.pref_json_peer_list_key), peerRepository.encode());
            editor.apply();
            Log.d("PeerService", "onDestroy: saved");
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

//    public int getServerPort() {
//        return serverPort;
//    }


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
        void onServerStart(PeerEntity peerEntity); // From server
        void onServerError(String message); // From server // TODO: Define proper arguments
        void onPeerConnection(PeerRepository peerRepository, PeerEntity peerEntity); // From long-polling
        void onPeerDisplayNameUpdate(PeerEntity peerEntity); // From long-polling
        void onPeerLocationUpdate(PeerEntity peerEntity); // From long-polling
        // or
        void onPeerUpdate(PeerEntity peerEntity); // From long-polling
    }
}
