package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ViewAnimator;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CommunityNoteAdapter extends RecyclerView.Adapter<CommunityNoteViewHolder> {

    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<CommunityNote> communityNoteList;

    private ArrayList<CommunityNote> communityNotes;

    private CommunityActivity communityActivity;

    public CommunityNoteAdapter(ArrayList<CommunityNote> communityNoteList, ArrayList<CommunityNote> communityNotes, CommunityActivity communityActivity) {
        this.communityNoteList = communityNoteList;
        this.communityNotes = communityNotes;
        this.communityActivity = communityActivity;
    }

    public CommunityNoteViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.community_note, viewGroup, false);
        CommunityNoteViewHolder holder = new CommunityNoteViewHolder(view);
        return holder;
    }

    public void onBindViewHolder(CommunityNoteViewHolder holder, int position) {
        CommunityNote communityNote = communityNotes.get(position);
        holder.communityNoteTitle.setText(communityNote.getTitle());
        holder.email.setText(communityNote.getEmail());
        holder.profileImage.setImageBitmap(communityNote.getBitmap());
        holder.dateCreated.setText(communityNote.getDateCreated());

        ViewAnimator viewAnimator = communityActivity.findViewById(R.id.viewAnimator);

        EditText noteTitle = communityActivity.findViewById(R.id.noteTitle);

        EditText noteBody = communityActivity.findViewById(R.id.noteBody);

        RecyclerView recyclerView = communityActivity.findViewById(R.id.communityRecyclerView);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                communityActivity.selectedNote = true;

                viewAnimator.setDisplayedChild(1);
                noteTitle.setText(communityNote.getTitle());
                noteBody.setText(communityNote.getBody());
            }
        });

        if (CommunityActivity.manageNotes) {
            holder.deleteButton.setVisibility(View.VISIBLE);
        }

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View popupView = LayoutInflater.from(communityActivity).inflate(R.layout.delete_confirmation_popup, null);

                PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

                Button popupDeleteButton = popupView.findViewById(R.id.deleteButton);

                popupDeleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.collection("community").document(communityNote.getId()).delete();

                        communityNoteList.remove(communityNote);
                        communityNotes.remove(communityNote);
                        recyclerView.getAdapter().notifyItemRemoved(holder.getAdapterPosition());

                        popupWindow.dismiss();
                    }
                });

                Button cancelButton = popupView.findViewById(R.id.cancelButton);

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });

                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
            }
        });
    }

    public int getItemCount() { return communityNotes.size(); }
}
