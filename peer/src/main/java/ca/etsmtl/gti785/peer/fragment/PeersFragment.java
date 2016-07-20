package ca.etsmtl.gti785.peer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.etsmtl.gti785.lib.entity.PeerEntity;
import ca.etsmtl.gti785.lib.repository.PeerRepository;
import ca.etsmtl.gti785.peer.R;
import ca.etsmtl.gti785.peer.adapter.PeersRecyclerViewAdapter;
import ca.etsmtl.gti785.peer.util.DividerItemDecoration;

public class PeersFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    private OnListFragmentInteractionListener listener;

    private List<PeerEntity> peers = new ArrayList<>();
    private PeersRecyclerViewAdapter adapter;

//    public PeersFragment() {
//    }

    // TODO: Customize parameter initialization
    public static PeersFragment newInstance() {
        PeersFragment fragment = new PeersFragment();

//        Bundle args = new Bundle();
//        args.putInt(ARG_COLUMN_COUNT, columnCount);
//        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getArguments() != null) {
//            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
//        }

        adapter = new PeersRecyclerViewAdapter(peers, listener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_peers_list, container, false);

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

    public void updateDataSet(PeerRepository peerRepository) {
        peers.clear();
        peers.addAll(peerRepository.getAll());

        Collections.sort(peers);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }


//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnListFragmentInteractionListener) {
//            listener = (OnListFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnListFragmentInteractionListener");
//        }
//    }

//    @Override
//    public void onDetach() {
//        super.onDetach();
//        listener = null;
//    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
//        void onListFragmentInteraction(DummyItem item);
    }
}
