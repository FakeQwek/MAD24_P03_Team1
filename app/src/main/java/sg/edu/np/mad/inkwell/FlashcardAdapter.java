package sg.edu.np.mad.inkwell;

import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardViewHolder> {
    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Get id of current user
    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Declaration of variables
    private ArrayList<Flashcard> allFlashcards;

    private ArrayList<Flashcard> flashcardList;

    private ViewFlashcardActivity viewFlashcardActivity;

    // FlashcardAdapter constructor
    public FlashcardAdapter(ArrayList<Flashcard> allFlashcards, ArrayList<Flashcard> flashcardList, ViewFlashcardActivity viewFlashcardActivity) {
        this.allFlashcards = allFlashcards;
        this.flashcardList = flashcardList;
        this.viewFlashcardActivity = viewFlashcardActivity;
    }

    // FlashcardAdapter onCreateViewHolder
    public FlashcardViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.flashcard, viewGroup, false);
        FlashcardViewHolder holder = new FlashcardViewHolder(view);
        return holder;
    }

    // FlashcardAdapter onBindViewHolder
    public void onBindViewHolder(FlashcardViewHolder holder, int position) {
        // Get position and set text to view holder
        Flashcard flashcard = flashcardList.get(position);
        holder.question.setText(flashcard.getQuestion());
        holder.answer.setText(flashcard.getAnswer());

        RecyclerView recyclerView = viewFlashcardActivity.findViewById(R.id.recyclerView);

        // Deletes flashcard and updates firebase
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.deleteButton.setEnabled(false);

                View popupView = LayoutInflater.from(viewFlashcardActivity).inflate(R.layout.delete_confirmation_popup, null);

                PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

                Button deleteButton = popupView.findViewById(R.id.deleteButton);

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        allFlashcards.remove(flashcard);
                        flashcardList.remove(flashcard);
                        recyclerView.getAdapter().notifyItemRemoved(holder.getAdapterPosition());

                        db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).collection("flashcards").document(String.valueOf(flashcard.getId())).delete();

                        db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).update("flashcardCount", FieldValue.increment(-1));
                        db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).update("correct", 0);

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

                holder.deleteButton.setEnabled(true);
            }
        });

        // Updates firebase everytime a change is made to the texts in the flashcard
        holder.question.addTextChangedListener(new MainActivity.TextChangedListener<EditText>(holder.question) {
            @Override
            public void onTextChanged(EditText question, Editable s) {
                flashcard.setFlashcardQuestion(question.getText().toString());
                db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).collection("flashcards").document(String.valueOf(flashcard.getId())).update("question", question.getText().toString());
            }
        });

        holder.answer.addTextChangedListener(new MainActivity.TextChangedListener<EditText>(holder.answer) {
            @Override
            public void onTextChanged(EditText answer, Editable s) {
                flashcard.setFlashcardAnswer(answer.getText().toString());
                db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).collection("flashcards").document(String.valueOf(flashcard.getId())).update("answer", answer.getText().toString());
            }
        });
    }

    // Returns the size of flashcardList
    public int getItemCount() { return flashcardList.size(); }
}
