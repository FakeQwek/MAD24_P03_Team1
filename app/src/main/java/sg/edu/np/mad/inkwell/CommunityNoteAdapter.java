package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ViewAnimator;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CommunityNoteAdapter extends RecyclerView.Adapter<CommunityNoteViewHolder> {

    private ArrayList<CommunityNote> communityNoteList;

    private CommunityActivity communityActivity;

    public CommunityNoteAdapter(ArrayList<CommunityNote> communityNoteList, CommunityActivity communityActivity) {
        this.communityNoteList = communityNoteList;
        this.communityActivity = communityActivity;
    }

    public CommunityNoteViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.community_note, viewGroup, false);
        CommunityNoteViewHolder holder = new CommunityNoteViewHolder(view);
        return holder;
    }

    public void onBindViewHolder(CommunityNoteViewHolder holder, int position) {
        CommunityNote communityNote = communityNoteList.get(position);
        holder.communityNoteTitle.setText(communityNote.getTitle());
        holder.email.setText(communityNote.getEmail());
        holder.profileImage.setImageBitmap(communityNote.getBitmap());

        ViewAnimator viewAnimator = communityActivity.findViewById(R.id.viewAnimator);

        EditText noteTitle = communityActivity.findViewById(R.id.noteTitle);

        EditText noteBody = communityActivity.findViewById(R.id.noteBody);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                communityActivity.selectedNote = true;

                viewAnimator.setDisplayedChild(1);
                noteTitle.setText(communityNote.getTitle());
                noteBody.setText(communityNote.getBody());
            }
        });
    }

    public int getItemCount() { return communityNoteList.size(); }
}
