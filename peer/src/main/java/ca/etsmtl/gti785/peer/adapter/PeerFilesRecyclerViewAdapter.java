package ca.etsmtl.gti785.peer.adapter;

import static android.text.format.Formatter.formatFileSize;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pgrenaud.android.p2p.entity.FileEntity;
import ca.etsmtl.gti785.peer.R;
import ca.etsmtl.gti785.peer.fragment.PeerFilesFragment.PeerFilesFragmentListener;

import java.util.List;

public class PeerFilesRecyclerViewAdapter extends RecyclerView.Adapter<PeerFilesRecyclerViewAdapter.ViewHolder> {

    private final Context context;
    private final List<FileEntity> files;
    private final PeerFilesFragmentListener listener;
    private final String host;

    public PeerFilesRecyclerViewAdapter(Context context, List<FileEntity> files, PeerFilesFragmentListener listener, String host) {
        this.context = context;
        this.files = files;
        this.listener = listener;
        this.host = host;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_files, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.file = files.get(position);
        holder.nameText.setText(holder.file.getName());
//        holder.sizeText.setText(Long.toString(files.get(position).getSize()));
        holder.sizeText.setText(formatFileSize(context, holder.file.getSize()));
        holder.downloadImage.setVisibility(View.VISIBLE);

        holder.downloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDownloadImageClick(holder.file, host);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View fileView;
        public final TextView nameText;
        public final TextView sizeText;
        public final ImageView downloadImage;

        public FileEntity file;

        public ViewHolder(View view) {
            super(view);

            fileView = view;
            nameText = (TextView) view.findViewById(R.id.files_name_text);
            sizeText = (TextView) view.findViewById(R.id.files_size_text);
            downloadImage = (ImageView) view.findViewById(R.id.files_download_image);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + nameText.getText() + "'";
        }
    }
}
