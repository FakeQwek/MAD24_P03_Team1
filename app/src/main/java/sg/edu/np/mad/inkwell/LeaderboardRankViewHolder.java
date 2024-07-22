package sg.edu.np.mad.inkwell;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class LeaderboardRankViewHolder extends RecyclerView.ViewHolder {
    ImageView profileImage;

    CardView cardView;

    TextView email;

    TextView points;

    public LeaderboardRankViewHolder(View view) {
        super(view);

        profileImage = view.findViewById(R.id.profileImage);

        cardView = view.findViewById(R.id.cardView);

        email = view.findViewById(R.id.email);

        points = view.findViewById(R.id.points);
    }
}
