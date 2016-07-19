package ca.etsmtl.gti785;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class SimpleService extends Service {

    private final IBinder binder = new SimpleServiceBinder();

    private OnStuffHappenListener listener;

    private String str;

    @Override
    public void onCreate() {
        Log.d("SimpleService", "onCreate");

        str = "Test123";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SimpleService", "onStartCommand: " + str);

        // TODO: Detect if service is already running and skip initialization

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("SimpleService", "onBind");

        new Timer().schedule(
            new TimerTask() {
                @Override
                public void run() {

                    Log.d("TimerTask", "run");

                    if (listener != null) {
                        listener.onStuffHappen();
                    }
                }
            },
            5000
        );

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
    }

    public String getStr() {
        return str;
    }

    public OnStuffHappenListener getListener() {
        return listener;
    }

    public void setListener(OnStuffHappenListener listener) {
        this.listener = listener;
    }

    public class SimpleServiceBinder extends Binder {
        SimpleService getService() {
            String bob = SimpleService.this.str;

            // Return this instance of LocalService so clients can call public methods
            return SimpleService.this;
        }
    }

    public interface OnStuffHappenListener {
        void onStuffHappen();
    }
}
