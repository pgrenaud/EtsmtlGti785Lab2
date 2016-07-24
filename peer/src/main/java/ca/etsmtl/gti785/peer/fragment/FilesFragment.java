package ca.etsmtl.gti785.peer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pgrenaud.android.p2p.entity.FileEntity;
import com.pgrenaud.android.p2p.repository.FileRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.etsmtl.gti785.peer.adapter.FilesRecyclerViewAdapter;
import ca.etsmtl.gti785.peer.R;
import ca.etsmtl.gti785.peer.util.DividerItemDecoration;

public class FilesFragment extends Fragment {

    private List<FileEntity> files = new ArrayList<>();
    private FilesRecyclerViewAdapter adapter;

    public static FilesFragment newInstance() {
        return new FilesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new FilesRecyclerViewAdapter(getContext(), files);
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

    public void updateDataSet(FileRepository fileRepository) {
        files.clear();
        files.addAll(fileRepository.getAll());

        Collections.sort(files);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
