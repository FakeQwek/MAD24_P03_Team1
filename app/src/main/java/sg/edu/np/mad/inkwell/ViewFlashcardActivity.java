package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewFlashcardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Get id of current user
    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Declaration of variables

    // currentFlashcardId keeps track of the ids that have already been assigned
    private int currentFlashcardId;

    private ArrayList<Flashcard> flashcards;

    ArrayList<Flashcard> allFlashcards;

    // Method to set items in the recycler view
    private void recyclerView(ArrayList<Flashcard> allFlashcards, ArrayList<Flashcard> flashcards) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FlashcardAdapter adapter = new FlashcardAdapter(allFlashcards, flashcards, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    // Method to filter items already in the recycler view
    private void filter(ArrayList<Flashcard> allFlashcards, String query) {
        ArrayList<Flashcard> filterList = new ArrayList<>();
        for (Flashcard flashcard : allFlashcards){
            if(flashcard.getQuestion().toLowerCase().contains(query)) {
                filterList.add(flashcard);
            }
        }
        flashcards = filterList;
        recyclerView(allFlashcards, filterList);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_flashcard);

        //Sets toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Finds drawer and nav view before setting listener
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);

        allFlashcards = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        ImageButton addFlashcardButton = findViewById(R.id.addFlashcardButton);

        // Adds a flashcard to firebase
        addFlashcardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> flashcardData = new HashMap<>();
                flashcardData.put("question", "New flashcard");
                flashcardData.put("answer", "answer");
                db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).collection("flashcards").document(String.valueOf(currentFlashcardId + 1)).set(flashcardData);

                db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).update("flashcardCount", FieldValue.increment(1));

                Toast toast = new Toast(ViewFlashcardActivity.this);
                toast.setDuration(Toast.LENGTH_SHORT);
                LayoutInflater layoutInflater = (LayoutInflater) ViewFlashcardActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = layoutInflater.inflate(R.layout.toast_added, null);
                toast.setView(view);
                toast.show();
            }
        });

        Button quizButton = findViewById(R.id.quizButton);

        // Go to QuizFlashcardActivity
        quizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!allFlashcards.isEmpty()) {
                    Intent quiz = new Intent(ViewFlashcardActivity.this, QuizFlashcardActivity.class);
                    startActivity(quiz);
                } else {

                }
            }
        });

        // Allows for recycler view items to be swiped
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Removes the item from the recycler view and deletes its data from firebase
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                Flashcard flashcard = allFlashcards.get(position);
                db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).collection("flashcards").document(String.valueOf(flashcard.id)).delete();
                db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).update("flashcardCount", FieldValue.increment(-1));
                allFlashcards.remove(flashcard);
                flashcards.remove(flashcard);
                recyclerView.getAdapter().notifyItemRemoved(position);
                Toast toast = new Toast(ViewFlashcardActivity.this);
                toast.setDuration(Toast.LENGTH_SHORT);
                LayoutInflater layoutInflater = (LayoutInflater) ViewFlashcardActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = layoutInflater.inflate(R.layout.toast_deleted, null);
                toast.setView(view);
                toast.show();
            }

            // Only swipes the item away if 80% of it is off the screen
            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.80f;
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Read from firebase and create flashcards on create
        db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).collection("flashcards")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("testing", "listen:error", e);
                            return;
                        }

                        // Adds items to recycler view on create and everytime new data is added to firebase
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                if (Integer.parseInt(dc.getDocument().getId()) > currentFlashcardId) {
                                    currentFlashcardId = Integer.parseInt(dc.getDocument().getId());
                                }
                                Flashcard flashcard = new Flashcard(dc.getDocument().getData().get("question").toString(), dc.getDocument().getData().get("answer").toString(), Integer.parseInt(dc.getDocument().getId()));
                                allFlashcards.add(flashcard);
                                filter(allFlashcards, "");
                            }
                        }
                    }
                });
    }

    //Allows movement between activities upon clicking
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        Navbar navbar = new Navbar(this);
        Intent newActivity = navbar.redirect(id);
        startActivity(newActivity);
        return true;
    }
}