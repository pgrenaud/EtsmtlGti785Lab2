package ca.etsmtl.gti785.peer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pgrenaud.android.p2p.entity.LocationEntity;
import com.pgrenaud.android.p2p.entity.PeerEntity;
import com.pgrenaud.android.p2p.repository.PeerRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.etsmtl.gti785.peer.R;
import ca.etsmtl.gti785.peer.adapter.PeersRecyclerViewAdapter;
import ca.etsmtl.gti785.peer.util.DividerItemDecoration;

public class PeersFragment extends Fragment {

    private PeersFragmentListener listener;

    private List<PeerEntity> peers = new ArrayList<>();
    private LocationEntity location = new LocationEntity();
    private PeersRecyclerViewAdapter adapter;

    public static PeersFragment newInstance() {
        return new PeersFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof PeersFragmentListener) {
            listener = (PeersFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement FilesFragmentListener.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new PeersRecyclerViewAdapter(peers, location, listener);
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

            ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }
                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                    PeersRecyclerViewAdapter.ViewHolder holder = (PeersRecyclerViewAdapter.ViewHolder) viewHolder;

                    if (listener != null) {
                        listener.onPeerEntityDismiss(holder.peer);
                    }
                }
            };
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        listener = null;
    }

    public void updateDataSet(PeerRepository peerRepository) {
        peers.clear();
        peers.addAll(peerRepository.getAll());

        Collections.sort(peers);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void updateDataSet(PeerEntity peerEntity) {
        location.setLocation(peerEntity.getLocation());

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public interface PeersFragmentListener {
        void onPeerEntityClick(PeerEntity peer);
        void onPeerEntityDismiss(PeerEntity peer);
    }
}
