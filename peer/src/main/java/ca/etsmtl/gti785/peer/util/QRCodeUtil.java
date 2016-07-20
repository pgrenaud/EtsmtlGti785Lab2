package ca.etsmtl.gti785.peer.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

public class QRCodeUtil {

    private static final int QR_WIDTH = 300;
    private static final int QR_HEIGHT = 300;

    public static Bitmap generateBitmap(String contents) {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
//        hints.put(EncodeHintType.MARGIN, DEFAULT_MARGIN);
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);

        try {
            BitMatrix matrix = new QRCodeWriter().encode(contents, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);

            int w = matrix.getWidth();
            int h = matrix.getHeight();

            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    bitmap.setPixel(i, j, matrix.get(i, j) ? Color.BLACK : Color.TRANSPARENT);
                }
            }

            return bitmap;
        } catch (IllegalArgumentException | WriterException e) {
            Log.e("QRCodeUtil", "generateBitmap", e);
        }

        return null;
    }
}
