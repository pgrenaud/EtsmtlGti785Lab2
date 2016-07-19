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

import ca.etsmtl.gti785.lib.R;
import ca.etsmtl.gti785.lib.entity.PeerEntity;
import ca.etsmtl.gti785.lib.handler.RequestHandler;
import ca.etsmtl.gti785.lib.repository.FileRepository;
import ca.etsmtl.gti785.lib.web.WebServer;

public class PeerService extends Service {

    private final IBinder binder = new PeerServiceBinder();

    private WebServer server;
    private PeerServiceListener listener;

    private boolean running = false;

    @Override
    public void onCreate() {
        Log.d("SimpleService", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SimpleService", "onStartCommand");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getService());
        String path = prefs.getString("server_directory", Environment.getExternalStorageDirectory().getPath()); // FIXME: Get from intent

        FileRepository fileRepository = new FileRepository();
        fileRepository.addAll(path);

        RequestHandler requestHandler = new RequestHandler(fileRepository);

        server = new WebServer(8099, requestHandler);

        // Skip initialization if already running
        if (!running) {
            try {
                server.start();
            } catch (IOException e) {
                Log.d("PeerService", "start server failed", e);
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
            server.stop();
        }
    }

    public PeerServiceListener getListener() {
        return listener;
    }

    public void setListener(PeerServiceListener listener) {
        this.listener = listener;
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
