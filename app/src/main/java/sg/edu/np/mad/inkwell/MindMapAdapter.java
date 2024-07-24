package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MindMapAdapter extends RecyclerView.Adapter<MindMapAdapter.ViewHolder> {

    private final Context context;
    private final List<NodeView> nodes;
    private final OnNodeClickListener listener;

    public MindMapAdapter(Context context, List<NodeView> nodes, OnNodeClickListener listener) {
        this.context = context;
        this.nodes = nodes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mindmap, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NodeView node = nodes.get(position);
        holder.textView.setText(node.getText());
        holder.itemView.setOnClickListener(v -> listener.onNodeClick(node));
    }

    @Override
    public int getItemCount() {
        return nodes.size();
    }

    public interface OnNodeClickListener {
        void onNodeClick(NodeView node);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.mindmapButton);
        }
    }
}
