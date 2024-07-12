package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FlashcardCollectionAdapter extends RecyclerView.Adapter<FlashcardCollectionViewHolder> {
    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Get id of current user
    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Declaration of variables
    private ArrayList<FlashcardCollection> allFlashcardCollections;

    private ArrayList<FlashcardCollection> flashcardCollectionList;

    private FlashcardActivity flashcardActivity;

    // FlashcardCollectionAdapter constructor
    public FlashcardCollectionAdapter(ArrayList<FlashcardCollection> allFlashcardCollections, ArrayList<FlashcardCollection> flashcardCollectionList, FlashcardActivity flashcardActivity) {
        this.allFlashcardCollections = allFlashcardCollections;
        this.flashcardCollectionList = flashcardCollectionList;
        this.flashcardActivity = flashcardActivity;
    }

    // FlashcardCollectionAdapter onCreateViewHolder
    public FlashcardCollectionViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.flashcard_collection, viewGroup, false);
        FlashcardCollectionViewHolder holder = new FlashcardCollectionViewHolder(view);
        return holder;
    }

    // FlashcardCollectionAdapter onBindViewHolder
    public void onBindViewHolder(FlashcardCollectionViewHolder holder, int position) {
        // Get position and set text to view holder
        FlashcardCollection flashcardCollection = flashcardCollectionList.get(position);
        holder.title.setText(flashcardCollection.getTitle());
        holder.flashcardCount.setText(flashcardCollection.getCorrect() + "/" + flashcardCollection.getFlashcardCount());

        RecyclerView recyclerView = flashcardActivity.findViewById(R.id.recyclerView);

        holder.progressBar.setMax(flashcardCollection.getFlashcardCount());

        holder.progressBar.setProgress(flashcardCollection.getCorrect());

        // On clicking a flashcard collection go to ViewFlashcardActivity
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlashcardActivity.selectedFlashcardCollectionId = flashcardCollection.getId();
                Intent viewFlashcardActivity = new Intent(flashcardActivity, ViewFlashcardActivity.class);
                flashcardActivity.startActivity(viewFlashcardActivity);
            }
        });

        // On long clicking a flashcard collection bring up a menu
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(flashcardActivity);
                View view = LayoutInflater.from(flashcardActivity).inflate(R.layout.flashcard_collection_bottom_sheet, null);
                bottomSheetDialog.setContentView(view);
                bottomSheetDialog.show();

                TextInputEditText titleEditText = view.findViewById(R.id.titleEditText);

                titleEditText.setText(flashcardCollection.title);

                Button cancelButton = view.findViewById(R.id.cancelButton);

                // Cancels the process
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });

                Button doneButton = view.findViewById(R.id.doneButton);

                // Changes the data in firebase
                doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String, Object> newFlashcardCollection = new HashMap<>();
                        newFlashcardCollection.put("title", titleEditText.getText().toString());

                        db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(flashcardCollection.id)).update(newFlashcardCollection);

                        flashcardCollection.setTitle(titleEditText.getText().toString());

                        recyclerView.getAdapter().notifyDataSetChanged();

                        bottomSheetDialog.dismiss();
                    }
                });

                Button flashcardCollectionDeleteButton = view.findViewById(R.id.deleteButton);

                // Delete flashcard collection
                flashcardCollectionDeleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();

                        View popupView = LayoutInflater.from(flashcardActivity).inflate(R.layout.delete_confirmation_popup, null);

                        PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

                        Button deleteButton = popupView.findViewById(R.id.deleteButton);

                        deleteButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                allFlashcardCollections.remove(flashcardCollection);
                                flashcardCollectionList.remove(flashcardCollection);
                                recyclerView.getAdapter().notifyDataSetChanged();

                                db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections")
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

                                db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(flashcardCollection.getId())).delete();

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

                        bottomSheetDialog.dismiss();
                    }
                });

                return false;
            }
        });

    }

    // Return the size of flashcardCollectionList
    public int getItemCount() { return flashcardCollectionList.size(); }
}
