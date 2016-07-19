package ca.etsmtl.gti785.peer.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.InetAddress;

import ca.etsmtl.gti785.peer.R;
import ca.etsmtl.gti785.peer.util.QRCodeUtil;

public class ServerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView nameText;
    private TextView statusText;
    private ImageView qrImage;
    private Bitmap qrBitmap;

//    public ServerFragment() {
//    }

    // TODO: Rename and change types and number of parameters
    public static ServerFragment newInstance() {
        ServerFragment fragment = new ServerFragment();

//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
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
            nameText = (TextView) getView().findViewById(R.id.server_name_text);
            statusText = (TextView) getView().findViewById(R.id.server_status_text);
            qrImage = (ImageView) getView().findViewById(R.id.server_qr_image);

            reloadStatus();

            if (qrBitmap != null) {
                qrImage.setImageBitmap(qrBitmap);
            } else {
                new BitmapAsyncTask().execute();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // TODO: Clear all UI references
        qrImage = null;
    }

    public void reloadStatus() {
        // TODO: Invalidate bitmap and redraw

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String name = prefs.getString(getString(R.string.pref_server_name_key), getString(R.string.pref_server_name_default));

        WifiManager wifiMgr = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        String serverAddr = Formatter.formatIpAddress(ip);

//        InetAddress myInetIP = InetAddress.getByAddress(myIPAddress);

        nameText.setText(getString(R.string.server_peer_name, name));
        statusText.setText(getString(R.string.server_peer_status, serverAddr, "8099")); // TODO: Bind values
    }

    // See: https://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
    // See: https://developer.android.com/training/displaying-bitmaps/process-bitmap.html#BitmapWorkerTask
    private class BitmapAsyncTask extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... voids) {
            Log.d("BitmapAsyncTask", "generating image");

            Bitmap bitmap = QRCodeUtil.generateBitmap("Test123");

            Log.d("BitmapAsyncTask", "image generated");

            return bitmap;
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
