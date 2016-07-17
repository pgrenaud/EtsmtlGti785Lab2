package ca.etsmtl.gti785.peer.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.etsmtl.gti785.peer.R;
import ca.etsmtl.gti785.peer.dummy.DummyContent.DummyItem;
import ca.etsmtl.gti785.peer.fragment.FilesFragment;

import java.util.List;

public class FilesRecyclerViewAdapter extends RecyclerView.Adapter<FilesRecyclerViewAdapter.ViewHolder> {

    private final List<DummyItem> files;
    private final FilesFragment.OnListFragmentInteractionListener listener;

    public FilesRecyclerViewAdapter(List<DummyItem> files, FilesFragment.OnListFragmentInteractionListener listener) {
        this.files = files;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_files, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.file = files.get(position);
        holder.nameText.setText(files.get(position).content);

        holder.fileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    listener.onListFragmentInteraction(holder.file);
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

        public DummyItem file;

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
