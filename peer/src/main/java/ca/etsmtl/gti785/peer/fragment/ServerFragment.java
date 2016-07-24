package ca.etsmtl.gti785.peer.fragment;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pgrenaud.android.p2p.entity.PeerEntity;

import ca.etsmtl.gti785.peer.R;
import ca.etsmtl.gti785.peer.util.QRCodeUtil;

public class ServerFragment extends Fragment {

    private PeerEntity peerEntity;

    private TextView nameText;
    private TextView statusText;
    private ImageView qrImage;
    private Bitmap qrBitmap;

    public static ServerFragment newInstance() {
        return new ServerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_server, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();

        if (view != null) {
            nameText = (TextView) view.findViewById(R.id.server_name_text);
            statusText = (TextView) view.findViewById(R.id.server_status_text);
            qrImage = (ImageView) view.findViewById(R.id.server_qr_image);


            if (peerEntity != null) {
                updateDataSet(peerEntity);
            }

            // FIXME
//            if (qrBitmap != null) {
//                qrImage.setImageBitmap(qrBitmap);
//            } else {
//                new BitmapAsyncTask().execute();
//            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // TODO: Clear all UI references
        nameText = null;
        statusText = null;
        qrImage = null;
    }

    public void updateDataSet(PeerEntity peerEntity) {
        this.peerEntity = peerEntity;

        if (isAdded()) {
            nameText.setText(getString(R.string.server_peer_name, peerEntity.getDisplayName()));
            statusText.setText(getString(R.string.server_peer_status, peerEntity.getIpAddress(), peerEntity.getPort()));

            qrImage.setImageDrawable(null);

            // FIXME: Remove accessedAt date from peer entity
            new BitmapAsyncTask().execute(peerEntity.encode());
        }
    }

    // See: https://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
    // See: https://developer.android.com/training/displaying-bitmaps/process-bitmap.html#BitmapWorkerTask
    private class BitmapAsyncTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            if (strings.length == 1) {
                Log.d("BitmapAsyncTask", "generating image");

                Bitmap bitmap = QRCodeUtil.generateBitmap(strings[0]);

                Log.d("BitmapAsyncTask", "image generated");

                return bitmap;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.d("BitmapAsyncTask", "setting image");

            qrBitmap = bitmap;

            if (qrImage != null) {
                qrImage.setImageBitmap(bitmap);
            }

            Log.d("BitmapAsyncTask", "image set");
        }

    }
}
