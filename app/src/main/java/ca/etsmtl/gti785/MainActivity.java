package ca.etsmtl.gti785;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {

    private Button startButton;
    private Button scanButton;
    private TextView responseText;

    private SimpleService service;
    private boolean bound;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            Log.d("MainActivity", "onServiceConnected");

            SimpleService.SimpleServiceBinder binder = (SimpleService.SimpleServiceBinder) iBinder;
            service = binder.getService();

            bound = true;
            service.setListener(listener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            Log.d("MainActivity", "onServiceDisconnected");

            bound = false;
            service.setListener(null);
        }
    };

    private SimpleService.OnStuffHappenListener listener = new SimpleService.OnStuffHappenListener() {
        @Override
        public void onStuffHappen() {

            Log.d("MainActivity", "onStuffHappen");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    responseText.setText(service.getStr());
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "onCreate");

        startButton = (Button) findViewById(R.id.start_button);
        scanButton = (Button) findViewById(R.id.scan_button);
        responseText = (TextView) findViewById(R.id.response_text);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
//                Intent intent = new Intent(getApplicationContext(), DrawerActivity.class);
                startActivity(intent);
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
            }
        });

        Intent intent = new Intent(this, SimpleService.class);
        startService(intent);

        Log.d("MainActivity", "download: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            responseText.setText(scanResult.getContents());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d("MainActivity", "onStart");

        Intent intent = new Intent(this, SimpleService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("MainActivity", "onResume");

        if (bound) {
            Log.d("MainActivity", "str: " + service.getStr());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("MainActivity", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d("MainActivity", "onStop");

        if (bound) {
            unbindService(connection);
            bound = false;
            service.setListener(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("MainActivity", "onDestroy");

        Intent intent = new Intent(this, SimpleService.class);
        stopService(intent);
    }

    private Activity getActivity() {
        return this;
    }
}
