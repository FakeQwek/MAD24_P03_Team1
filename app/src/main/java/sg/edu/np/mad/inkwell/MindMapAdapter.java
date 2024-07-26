package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MindMapAdapter extends RecyclerView.Adapter<MindMapAdapter.ViewHolder> {

    private final Context context;
    private final List<String> titles;
    private final List<String> ids;
    private final OnItemClickListener itemClickListener;
    private final OnItemLongClickListener itemLongClickListener;

    public MindMapAdapter(Context context, List<String> titles, List<String> ids,
                          OnItemClickListener itemClickListener,
                          OnItemLongClickListener itemLongClickListener) {
        this.context = context;
        this.titles = titles;
        this.ids = ids;
        this.itemClickListener = itemClickListener;
        this.itemLongClickListener = itemLongClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mindmap, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String title = titles.get(position);
        String id = ids.get(position);

        holder.titleTextView.setText(title);

        holder.itemView.setOnClickListener(v -> itemClickListener.onItemClick(id));
        holder.itemView.setOnLongClickListener(v -> {
            itemLongClickListener.onItemLongClick(id);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String id);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(String id);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView titleTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.mindmapButton);
        }
    }
}
