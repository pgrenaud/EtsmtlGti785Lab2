package ca.etsmtl.gti785;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

public class SecondActivity extends AppCompatActivity {

    private Button startButton;
    private ImageView qrImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        Log.d("SecondActivity", "onCreate");

        startButton = (Button) findViewById(R.id.start_button);
        qrImage = (ImageView) findViewById(R.id.qr_image);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

//        qrImage.setImageBitmap(generateQRCodeBitmap("Some potatoes!", 1000, 1000));

        new BitmapAsyncTask().execute();

        Intent intent = new Intent(this, SimpleService.class);
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d("SecondActivity", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("SecondActivity", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d("SecondActivity", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d("SecondActivity", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d("SecondActivity", "onDestroy");

        Intent intent = new Intent(this, SimpleService.class);
        stopService(intent);
    }

    protected Bitmap generateQRCodeBitmap(String contents, int width, int height) {
        BitMatrix matrix;

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
//        hints.put(EncodeHintType.MARGIN, DEFAULT_MARGIN);
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        try {
            matrix = new QRCodeWriter().encode(contents, BarcodeFormat.QR_CODE, width, height, hints);

            int w = matrix.getWidth();
            int h = matrix.getHeight();

            Log.d("SecondActivity", "width: " + w + " height:" + h);

            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

            int colorBg = Color.parseColor("#FAFAFA"); // Android background color

            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    bitmap.setPixel(i, j, matrix.get(i, j) ? Color.BLACK : colorBg);
                }
            }

            return bitmap;
        } catch (IllegalArgumentException | WriterException e) {
            e.printStackTrace();
        }

        return null;
    }

    private class BitmapAsyncTask extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... voids) {
            Log.d("BitmapAsyncTask", "generating image");

            Bitmap bitmap = generateQRCodeBitmap("Yo Nadeau1!!", 250, 250);

            Log.d("BitmapAsyncTask", "image generated");

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.d("BitmapAsyncTask", "setting image");

            qrImage.setImageBitmap(bitmap);

            Log.d("BitmapAsyncTask", "image set");
        }

    }
}
