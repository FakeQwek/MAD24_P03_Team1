package sg.edu.np.mad.inkwell;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class FolderViewHolder extends RecyclerView.ViewHolder {
    // FolderViewHolder for recycler view
    Button folderButton;

    ImageView chevron;

    RecyclerView recyclerView;

    ConstraintLayout constraintLayout;

    ImageView bookmark;

    public FolderViewHolder(View view) {
        super(view);

        folderButton = view.findViewById(R.id.folderButton);

        chevron = view.findViewById(R.id.chevron);

        recyclerView = view.findViewById(R.id.recyclerView);

        constraintLayout = view.findViewById(R.id.constraintLayout);

        bookmark = view.findViewById(R.id.bookmark);
    }
}
