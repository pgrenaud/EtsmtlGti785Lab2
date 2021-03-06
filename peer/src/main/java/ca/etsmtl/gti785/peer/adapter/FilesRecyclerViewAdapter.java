package ca.etsmtl.gti785.peer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pgrenaud.android.p2p.entity.FileEntity;

import java.util.List;

import ca.etsmtl.gti785.peer.R;

public class FilesRecyclerViewAdapter extends RecyclerView.Adapter<FilesRecyclerViewAdapter.ViewHolder> {

    private final Context context;
    private final List<FileEntity> files;

    public FilesRecyclerViewAdapter(Context context, List<FileEntity> files) {
        this.context = context;
        this.files = files;
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
        holder.sizeText.setText(Formatter.formatFileSize(context, holder.file.getSize()));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View fileView;
        public final TextView nameText;
        public final TextView sizeText;

        public FileEntity file;

        public ViewHolder(View view) {
            super(view);

            fileView = view;
            nameText = (TextView) view.findViewById(R.id.files_name_text);
            sizeText = (TextView) view.findViewById(R.id.files_size_text);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + nameText.getText() + "'";
        }
    }
}
