package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FlashcardCollectionAdapter extends RecyclerView.Adapter<FlashcardCollectionViewHolder> {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ArrayList<FlashcardCollection> allFlashcardCollections;

    private ArrayList<FlashcardCollection> flashcardCollectionList;

    private FlashcardActivity flashcardActivity;

    public FlashcardCollectionAdapter(ArrayList<FlashcardCollection> allFlashcardCollections, ArrayList<FlashcardCollection> flashcardCollectionList, FlashcardActivity flashcardActivity) {
        this.allFlashcardCollections = allFlashcardCollections;
        this.flashcardCollectionList = flashcardCollectionList;
        this.flashcardActivity = flashcardActivity;
    }

    public FlashcardCollectionViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.flashcard_collection, viewGroup, false);
        FlashcardCollectionViewHolder holder = new FlashcardCollectionViewHolder(view);
        return holder;
    }

    public void onBindViewHolder(FlashcardCollectionViewHolder holder, int position) {
        FlashcardCollection flashcardCollection = flashcardCollectionList.get(position);
        holder.title.setText(flashcardCollection.getTitle());
        holder.flashcardCount.setText(flashcardCollection.getCorrect() + "/" + flashcardCollection.getFlashcardCount());

        RecyclerView recyclerView = flashcardActivity.findViewById(R.id.recyclerView);

        holder.progressBar.setMax(flashcardCollection.getFlashcardCount());

        holder.progressBar.setProgress(flashcardCollection.getCorrect());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewFlashcardActivity = new Intent(flashcardActivity, ViewFlashcardActivity.class);
                flashcardActivity.startActivity(viewFlashcardActivity);
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(flashcardActivity);
                View view = LayoutInflater.from(flashcardActivity).inflate(R.layout.flashcard_collection_bottom_sheet, null);
                bottomSheetDialog.setContentView(view);
                bottomSheetDialog.show();

                Button flashcardCollectionRenameButton = view.findViewById(R.id.flashcardCollectionRenameButton);
                flashcardCollectionRenameButton.setText("Rename");

                flashcardCollectionRenameButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View renamePopupView = LayoutInflater.from(flashcardActivity).inflate(R.layout.rename_popup, null);

                        PopupWindow renamePopupWindow = new PopupWindow(renamePopupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

                        TextInputEditText renameEditText = renamePopupView.findViewById(R.id.renameEditText);

                        Button renameButton = renamePopupView.findViewById(R.id.renameButton);

                        renameButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String newTitle = renameEditText.getText().toString();

                                Map<String, Object> newFlashcardCollection = new HashMap<>();
                                newFlashcardCollection.put("title", newTitle);

                                db.collection("flashcardCollections").document(String.valueOf(flashcardCollection.getId())).update(newFlashcardCollection);

                                flashcardCollection.setTitle(newTitle);

                                recyclerView.getAdapter().notifyDataSetChanged();

                                renamePopupWindow.dismiss();
                            }
                        });

                        renamePopupWindow.showAtLocation(renamePopupView, Gravity.CENTER, 0, 0);

                        bottomSheetDialog.dismiss();
                    }
                });

                Button flashcardCollectionDeleteButton = view.findViewById(R.id.flashcardCollectionDeleteButton);
                flashcardCollectionDeleteButton.setText("Delete");

                flashcardCollectionDeleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        allFlashcardCollections.remove(flashcardCollection);
                        flashcardCollectionList.remove(flashcardCollection);
                        recyclerView.getAdapter().notifyDataSetChanged();

                        db.collection("flashcardCollections")
                                .document(String.valueOf(flashcardCollection.getId()))
                                .collection("flashcards")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                document.getReference().delete();
                                            }
                                        } else {
                                            Log.d("tester", "Error getting documents: ", task.getException());
                                        }
                                    }
                                });

                        db.collection("flashcardCollections").document(String.valueOf(flashcardCollection.getId())).delete();

                        bottomSheetDialog.dismiss();
                    }
                });

                return false;
            }
        });

    }

    public int getItemCount() { return flashcardCollectionList.size(); }
}
