package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FriendAdapter extends RecyclerView.Adapter<FriendViewHolder> {
    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Get id of current user
    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private ArrayList<Friend> friendList;

    private FriendsActivity friendsActivity;

    int friendCurrentNoteId = 1;

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

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        if (friendsActivity != null) {
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
        } else {
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Date currentDate = Calendar.getInstance().getTime();

                    String dateString = simpleDateFormat.format(currentDate);

                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("title", NotesActivity.longClickSelectedNoteTitle);
                    fileData.put("body", NotesActivity.longClickSelectedNoteBody);
                    fileData.put("type", "file");
                    fileData.put("uid", friend.getId());
                    fileData.put("dateCreated", dateString);
                    fileData.put("dateUpdated", dateString);

                    db.collection("users").document(friend.getId()).collection("notes")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            if (Integer.parseInt(document.getId()) > friendCurrentNoteId) {
                                                friendCurrentNoteId = Integer.parseInt(document.getId());
                                            }
                                        }
                                        friendCurrentNoteId += 1;
                                        db.collection("users").document(friend.getId()).collection("notes").document(String.valueOf(friendCurrentNoteId)).set(fileData);
                                    } else {
                                        Log.d("testing", "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                }
            });
        }
    }

    public int getItemCount() { return friendList.size(); }
}
