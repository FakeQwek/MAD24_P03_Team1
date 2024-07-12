package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FriendAdapter extends RecyclerView.Adapter<FriendViewHolder> {
    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Get id of current user
    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

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

        RecyclerView recyclerView = friendsActivity.findViewById(R.id.friendRecyclerView);

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

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(friendsActivity);
                View view = LayoutInflater.from(friendsActivity).inflate(R.layout.friend_bottom_sheet, null);
                bottomSheetDialog.setContentView(view);
                bottomSheetDialog.show();

                Button deleteButton = view.findViewById(R.id.deleteButton);

                deleteButton.setText("Delete");

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.collection("users").document(currentFirebaseUserUid).collection("friends").document(String.valueOf(friend.getId())).delete();

                        friendList.remove(friend);

                        recyclerView.getAdapter().notifyItemRemoved(holder.getAdapterPosition());

                        bottomSheetDialog.dismiss();
                    }
                });

                return false;
            }
        });
    }

    public int getItemCount() { return friendList.size(); }
}
