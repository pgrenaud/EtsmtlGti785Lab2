package ca.etsmtl.gti785.lib.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import ca.etsmtl.gti785.lib.entity.PeerEntity;
import ca.etsmtl.gti785.lib.handler.PeerHive;
import ca.etsmtl.gti785.lib.handler.RequestHandler;
import ca.etsmtl.gti785.lib.repository.FileRepository;
import ca.etsmtl.gti785.lib.repository.PeerRepository;
import ca.etsmtl.gti785.lib.repository.QueueRepository;
import ca.etsmtl.gti785.lib.web.WebServer;

public class PeerService extends Service {

    public static final String EXTRA_DIRECTORY_PATH = "ca.etsmtl.gti785.lib.service.EXTRA_DIRECTORY_PATH"; // FIXME: Fix package
    public static final String EXTRA_SERVER_PORT = "ca.etsmtl.gti785.lib.service.EXTRA_SERVER_PORT"; // FIXME: Fix package

    public static final int DEFAULT_SERVER_PORT = 8099;

    private final IBinder binder = new PeerServiceBinder();
    private final QueueRepository queueRepository = new QueueRepository();
    private final FileRepository fileRepository = new FileRepository();
    private final PeerRepository peerRepository = new PeerRepository();
    private final PeerHive peerHive = new PeerHive(this, peerRepository);

    private PeerServiceListener listener;
    private WebServer server;
    private int serverPort;

    private boolean running = false;

    @Override
    public void onCreate() {
        Log.d("SimpleService", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SimpleService", "onStartCommand");

        // Skip initialization if already running
        if (!running) {
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getService());
//            String path = prefs.getString("server_directory", Environment.getExternalStorageDirectory().getPath()); // FIXME: Get from intent

            String path = intent.getStringExtra(EXTRA_DIRECTORY_PATH);
            if (path != null) {
                fileRepository.addAll(path);
            }

            serverPort = intent.getIntExtra(EXTRA_SERVER_PORT, DEFAULT_SERVER_PORT);

            // TODO: Load peers from persistent storage

            RequestHandler requestHandler = new RequestHandler(queueRepository, fileRepository);

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
        Log.d("SimpleService", "onBind");

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
        Log.d("SimpleService", "onRebind");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("SimpleService", "onUnbind");

        return false;
    }

    @Override
    public void onDestroy() {
        Log.e("SimpleService", "onDestroy");

        if (running) {
            // TODO: Save peers to persistent storage

            peerHive.stop();
            server.stop();
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

    public int getServerPort() {
        return serverPort;
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
        void onPeerConnection(PeerEntity peerEntity); // From long-polling
        void onPeerDisplayNameUpdate(PeerEntity peerEntity); // From long-polling
        void onPeerLocationUpdate(PeerEntity peerEntity); // From long-polling
        // or
        void onPeerUpdate(PeerEntity peerEntity); // From long-polling
    }
}
