package ca.etsmtl.gti785.peer.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pgrenaud.android.p2p.entity.PeerEntity;

import ca.etsmtl.gti785.peer.R;
import ca.etsmtl.gti785.peer.fragment.PeersFragment.PeersFragmentListener;

import java.util.List;

public class PeersRecyclerViewAdapter extends RecyclerView.Adapter<PeersRecyclerViewAdapter.ViewHolder> {

    private final List<PeerEntity> peers;
    private final PeersFragmentListener listener;

    public PeersRecyclerViewAdapter(List<PeerEntity> peers, PeersFragmentListener listener) {
        this.peers = peers;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_peers, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.peer = peers.get(position);
        holder.titleText.setText(holder.peer.getDisplayName());

        if (holder.peer.getAccessedAt() == null) {
            holder.dateText.setText("Never accessed"); // FIXME
        } else {
            holder.dateText.setText(holder.peer.getFormatedAccessedAt());
        }

        holder.addressText.setText(holder.peer.getHost());
        holder.distanceText.setText("0m"); // FIXME

        if (holder.peer.isOnline()) {
            holder.statusImage.setImageResource(R.drawable.ic_brightness_1_green_500_18dp);
        } else {
            holder.statusImage.setImageResource(R.drawable.ic_brightness_1_red_500_18dp);
        }

        holder.peerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null && holder.peer.isOnline()) {
                    listener.onPeerEntityClick(holder.peer);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return peers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View peerView;
        public final TextView titleText;
        public final TextView dateText;
        public final TextView addressText;
        public final TextView distanceText;
        public final ImageView statusImage;

        public PeerEntity peer;

        public ViewHolder(View view) {
            super(view);

            peerView = view;
            titleText = (TextView) view.findViewById(R.id.peers_title_text);
            dateText = (TextView) view.findViewById(R.id.peers_date_text);
            addressText = (TextView) view.findViewById(R.id.peers_address_text);
            distanceText = (TextView) view.findViewById(R.id.peers_distance_text);
            statusImage = (ImageView) view.findViewById(R.id.peers_status_image);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + titleText.getText() + "'";
        }
    }
}
