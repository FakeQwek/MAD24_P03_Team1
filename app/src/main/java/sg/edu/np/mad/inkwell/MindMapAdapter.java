package sg.edu.np.mad.inkwell;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MindMapAdapter extends RecyclerView.Adapter<MindMapAdapter.ViewHolder> {
    private List<String> titleNodes;

    public MindMapAdapter(List<String> titleNodes) {
        this.titleNodes = titleNodes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mindmap, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String titleNode = titleNodes.get(position);
        holder.textView.setText(titleNode);
    }

    @Override
    public int getItemCount() {
        return titleNodes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.mindmapButton);
        }
    }
}
