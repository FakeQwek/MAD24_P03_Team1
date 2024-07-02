package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;

public class FriendAdapter extends RecyclerView.Adapter<FriendViewHolder> {

    private ArrayList<Friend> friendList;

    private FriendsActivity friendsActivity;

    public FriendAdapter(ArrayList<Friend> friendList, FriendsActivity friendsActivity) {
        this.friendList = friendList;
        this.friendsActivity = friendsActivity;
    }

    public FriendViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend, viewGroup, false);
        FriendViewHolder holder = new FriendViewHolder(view);
        return holder;
    }

    public void onBindViewHolder(FriendViewHolder holder, int position) {
        Friend friend = friendList.get(position);
        holder.friendEmail.setText(friend.getEmail());
        holder.friendProfileImage.setImageBitmap(friend.bitmap);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendsActivity.selectedFriendUid = friend.uid;
                FriendsActivity.selectedFriendEmail = friend.email;

                FriendsActivity.selectedFriendId = friend.getId();
                Intent chatActivity = new Intent(friendsActivity, ChatActivity.class);
                friendsActivity.startActivity(chatActivity);
            }
        });
    }

    public int getItemCount() { return friendList.size(); }
}
