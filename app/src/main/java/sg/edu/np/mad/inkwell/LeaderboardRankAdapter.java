package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LeaderboardRankAdapter extends RecyclerView.Adapter<LeaderboardRankViewHolder> {
    private ArrayList<LeaderboardRank> leaderboardRankList;

    private LeaderboardActivity leaderboardActivity;

    public LeaderboardRankAdapter(ArrayList<LeaderboardRank> leaderboardRankList, LeaderboardActivity leaderboardActivity) {
        this.leaderboardRankList = leaderboardRankList;
        this.leaderboardActivity = leaderboardActivity;
    }

    public LeaderboardRankViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.leaderboard_rank, viewGroup, false);
        LeaderboardRankViewHolder holder = new LeaderboardRankViewHolder(view);
        return holder;
    }

    public void onBindViewHolder(LeaderboardRankViewHolder holder, int position) {
        LeaderboardRank leaderboardRank = leaderboardRankList.get(position);
        holder.email.setText(leaderboardRank.getEmail());
        holder.points.setText(leaderboardRank.points + "/10");
        holder.profileImage.setImageBitmap(leaderboardRank.bitmap);

    }

    public int getItemCount() { return leaderboardRankList.size(); }
}
