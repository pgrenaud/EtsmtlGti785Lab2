package ca.etsmtl.gti785.peer.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import ca.etsmtl.gti785.lib.entity.FileEntity;
import ca.etsmtl.gti785.lib.entity.PeerEntity;
import ca.etsmtl.gti785.lib.repository.FileRepository;
import ca.etsmtl.gti785.lib.web.HttpClientWrapper;
import ca.etsmtl.gti785.lib.web.HttpClientWrapper.HttpResponseCallback;
import ca.etsmtl.gti785.lib.web.HttpClientWrapper.BinaryHttpResponseCallback;
import ca.etsmtl.gti785.peer.R;
import ca.etsmtl.gti785.peer.adapter.PeerFilesRecyclerViewAdapter;
import ca.etsmtl.gti785.peer.util.DividerItemDecoration;
import cz.msebera.android.httpclient.conn.HttpHostConnectException;

public class PeerFilesFragment extends Fragment {

    private static final String ARG_PEER_HOST = "peer_host";
    private static final String ARG_PEER_UUID = "peer_uuid";

    private PeerFilesFragmentListener listener;
    private PeerFilesRecyclerViewAdapter adapter;
    private ProgressBar toolbarProgressbar;

    private List<FileEntity> files = new ArrayList<>();
    private String host;
//    private UUID uuid;


    public static PeerFilesFragment newInstance(PeerEntity peer) {
        PeerFilesFragment fragment = new PeerFilesFragment();

        Bundle args = new Bundle();
        args.putString(ARG_PEER_HOST, peer.getHost());
//        args.putString(ARG_PEER_UUID, peer.getUUID().toString());
        fragment.setArguments(args);

        return fragment;
    }

    // TODO: Use this to get the MainActivity instance and access fields/methods (and also send event)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof PeerFilesFragmentListener) {
            listener = (PeerFilesFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement FilesFragmentListener.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            host = getArguments().getString(ARG_PEER_HOST);
//            uuid = UUID.fromString(getArguments().getString(ARG_PEER_UUID));
        }

        adapter = new PeerFilesRecyclerViewAdapter(getContext(), files, listener, host);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.addItemDecoration(new DividerItemDecoration(context));
            recyclerView.setAdapter(adapter);
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();

        if (view != null) {
            toolbarProgressbar = (ProgressBar) getActivity().findViewById(R.id.toolbar_progressbar);

            if (host != null) {
                new ListFileAsyncTask().execute(host);
            }
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // TODO: Clear all UI references
        toolbarProgressbar = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        listener = null;
    }

    public void updateDataSet(Collection<FileEntity> fileEntities) {
        files.clear();
        files.addAll(fileEntities);

        Collections.sort(files);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

//    public void onDownloadImageClick(FileEntity file) {
//        new DownloadFileAsyncTask().execute(host, file.getUuid().toString(), file.getName());
//    }

    private class ListFileAsyncTask extends AsyncTask<String, Void, Void> {

        private HttpClientWrapper client;
        private Collection<FileEntity> results;

        @Override
        protected void onPreExecute() {
            Log.d("ListFileAsyncTask", "onPreExecute");

            toolbarProgressbar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... strings) {
            String filesUrl = "/api/v1/files";

            if (strings.length == 1) {
                client = new HttpClientWrapper(strings[0]);

                client.performHttpGet(filesUrl, new HttpResponseCallback() {
                    @Override
                    public void onHttpResponse(int status, String content) {
                        Log.d("ListFileAsyncTask", "onHttpResponse: " + status);

                        if (status == 200) {
                            Log.d("ListFileAsyncTask", "onHttpResponse: " + content);

                            try {
                                results = FileRepository.decode(content);
                            } catch (JsonSyntaxException e) {
                                Log.e("ListFileAsyncTask", "onHttpResponse: unknown results", e);
                            }
                        }
                    }
                    @Override
                    public void onException(Exception exception) {
                        if (exception instanceof HttpHostConnectException) {
                            Log.d("ListFileAsyncTask", "onException: " + exception.getMessage());
                        } else {
                            Log.d("ListFileAsyncTask", "onException", exception);
                        }
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("ListFileAsyncTask", "onPostExecute");

            toolbarProgressbar.setVisibility(View.GONE);

            if (results != null) {
                updateDataSet(results);
            }
        }
    }

    private class DownloadFileAsyncTask extends AsyncTask<String, Void, Void> {

        private HttpClientWrapper client;
        private Collection<FileEntity> results;

        @Override
        protected void onPreExecute() {
            Log.d("ListFileAsyncTask", "onPreExecute");

            toolbarProgressbar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(final String... strings) {
            if (strings.length == 3) {
                String filesUrl = "/api/v1/file/" + strings[1];

                client = new HttpClientWrapper(strings[0]);

                client.performBinaryHttpGet(filesUrl, new BinaryHttpResponseCallback() {
                    @Override
                    public void onHttpResponse(int status, InputStream input) {
                        Log.d("ListFileAsyncTask", "onHttpResponse: " + status);

                        if (status == 200) {
                            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), strings[2]); // FIXME

                            try {
                                FileOutputStream output = new FileOutputStream(file);

                                byte[] buffer = new byte[1024 * 4];
                                int len;

                                while ((len = input.read(buffer)) != -1) {
                                    output.write(buffer, 0, len);
                                }

                                input.close();
                                output.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onException(Exception exception) {
                        if (exception instanceof HttpHostConnectException) {
                            Log.d("ListFileAsyncTask", "onException: " + exception.getMessage());
                        } else {
                            Log.d("ListFileAsyncTask", "onException", exception);
                        }
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("ListFileAsyncTask", "onPostExecute");

            toolbarProgressbar.setVisibility(View.GONE);

            if (results != null) {
                updateDataSet(results);
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface PeerFilesFragmentListener {
        // TODO: Update argument type and name
//        void onListFragmentInteraction(File file);
//        void onFileRepositoryUpdate(FileRepository fileRepository);
//        PeerService getPeerService();
        void onDownloadImageClick(FileEntity file, String host);
    }
}
