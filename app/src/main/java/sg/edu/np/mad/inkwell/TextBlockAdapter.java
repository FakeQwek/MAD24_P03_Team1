package sg.edu.np.mad.inkwell;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.mlkit.vision.text.Text;

import java.util.List;

public class TextBlockAdapter extends RecyclerView.Adapter<TextBlockAdapter.TextBlockViewHolder> {

    private List<Text.TextBlock> textBlocks;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String text);
    }

    public TextBlockAdapter(List<Text.TextBlock> textBlocks, OnItemClickListener listener) {
        this.textBlocks = textBlocks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TextBlockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_block_item, parent, false);
        return new TextBlockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TextBlockViewHolder holder, int position) {
        Text.TextBlock textBlock = textBlocks.get(position);
        holder.textView.setText(textBlock.getText());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(textBlock.getText()));
    }

    @Override
    public int getItemCount() {
        return textBlocks.size();
    }

    static class TextBlockViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public TextBlockViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_block_text);
        }
    }
}
