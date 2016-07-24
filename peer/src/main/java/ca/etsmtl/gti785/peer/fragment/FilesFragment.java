package ca.etsmtl.gti785.peer.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.pgrenaud.android.p2p.entity.FileEntity;
import com.pgrenaud.android.p2p.repository.FileRepository;
import com.pgrenaud.android.p2p.service.PeerService;
import ca.etsmtl.gti785.peer.activity.MainActivity;
import ca.etsmtl.gti785.peer.adapter.FilesRecyclerViewAdapter;
import ca.etsmtl.gti785.peer.R;
import ca.etsmtl.gti785.peer.util.DividerItemDecoration;

public class FilesFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    private FilesFragmentListener listener;

    private List<FileEntity> files = new ArrayList<>();
    private FilesRecyclerViewAdapter adapter;

//    public FilesFragment() {
//    }

    // TODO: Customize parameter initialization
    public static FilesFragment newInstance() {
        FilesFragment fragment = new FilesFragment();

//        Bundle args = new Bundle();
//        args.putInt(ARG_COLUMN_COUNT, columnCount);
//        fragment.setArguments(args);

        return fragment;
    }

    // TODO: Use this to get the MainActivity instance and access fields/methods (and also send event)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FilesFragmentListener) {
            listener = (FilesFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement FilesFragmentListener.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getArguments() != null) {
//            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
//        }

        adapter = new FilesRecyclerViewAdapter(getContext(), files, listener);

//        reloadFiles();
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
    public void onDetach() {
        super.onDetach();

        listener = null;
    }

    public void updateDataSet(FileRepository fileRepository) {
        files.clear();
        files.addAll(fileRepository.getAll());

        Collections.sort(files);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

//    public void reloadFiles() {
//        // TODO: Use arguments to get path from activity
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String path = prefs.getString(getString(R.string.pref_server_directory_key), Environment.getExternalStorageDirectory().getPath());
//
//        File f = new File(path);
//        File[] files = f.listFiles();
//
//        this.files.clear();
//
//        for (File file : files) {
//            if (file.isFile()) {
//                this.files.add(file);
//            }
//        }
//
//        adapter.notifyDataSetChanged();
//    }

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
    public interface FilesFragmentListener {
        // TODO: Update argument type and name
//        void onListFragmentInteraction(File file);
//        void onFileRepositoryUpdate(FileRepository fileRepository);
//        PeerService getPeerService();
    }
}
