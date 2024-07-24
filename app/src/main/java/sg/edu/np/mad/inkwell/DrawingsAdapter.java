package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

public class DrawingsAdapter extends RecyclerView.Adapter<DrawingsAdapter.ViewHolder> {

    private List<Map<String, Object>> drawingList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Map<String, Object> drawing);
    }

    public DrawingsAdapter(List<Map<String, Object>> drawingList, OnItemClickListener onItemClickListener) {
        this.drawingList = drawingList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drawing, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> drawing = drawingList.get(position);

        String imageUrl = (String) drawing.get("imageUrl");
        String title = (String) drawing.get("title");
        String name = (String) drawing.get("name");

        holder.titleTextView.setText(title);
        holder.nameTextView.setText(name);

        // Load the image from Firebase Storage
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            holder.imageView.setImageBitmap(bitmap);
        }).addOnFailureListener(exception -> {
            // Handle any errors
        });

        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(drawing));
    }

    @Override
    public int getItemCount() {
        return drawingList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView nameTextView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
        }
    }
}