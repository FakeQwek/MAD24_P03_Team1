package sg.edu.np.mad.inkwell;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class CommunityNoteViewHolder extends RecyclerView.ViewHolder {
    ImageView profileImage;

    TextView email;

    TextView communityNoteTitle;

    CardView cardView;

    TextView dateCreated;

    ImageButton deleteButton;

    ImageButton likeButton;

    TextView likeCounter;

    public CommunityNoteViewHolder(View view) {
        super(view);

        profileImage = view.findViewById(R.id.profileImage);

        email = view.findViewById(R.id.email);

        communityNoteTitle = view.findViewById(R.id.communityNoteTitle);

        cardView = view.findViewById(R.id.cardView);

        dateCreated = view.findViewById(R.id.dateCreated);

        deleteButton = view.findViewById(R.id.deleteButton);

        likeButton = view.findViewById(R.id.likeButton);

        likeCounter = view.findViewById(R.id.likeCounter);
    }
}
