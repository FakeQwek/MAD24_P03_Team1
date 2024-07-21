package sg.edu.np.mad.inkwell;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class FriendViewHolder extends RecyclerView.ViewHolder {

    ImageView friendProfileImage;

    TextView friendEmail;

    CardView cardView;

    public FriendViewHolder(View view) {
        super(view);

        friendProfileImage = view.findViewById(R.id.friendProfileImage);

        friendEmail = view.findViewById(R.id.friendEmail);

        cardView = view.findViewById(R.id.cardView);
    }
}
