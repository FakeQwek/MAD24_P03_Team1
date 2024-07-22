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
import android.widget.TextView;
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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FlashcardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Get id of current user
    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Declaration of variables

    // currentFlashcardCollectionId keeps track of the ids that have already been assigned
    public static int currentFlashcardCollectionId;

    // selectedFlashcardCollectionId keeps track of the flashcard collection that has been selected
    public static int selectedFlashcardCollectionId;

    private ArrayList<FlashcardCollection> flashcardCollections;

    private int flashcardCollectionCount;

    ArrayList<FlashcardCollection> allFlashcardCollections;

    TextView flashcardCollectionCounter;

    // Method to set items in the recycler view
    private void recyclerView(ArrayList<FlashcardCollection> allFlashcardCollections, ArrayList<FlashcardCollection> flashcardCollections) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FlashcardCollectionAdapter adapter = new FlashcardCollectionAdapter(allFlashcardCollections, flashcardCollections, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    // Method to filter items already in the recycler view
    private void filter(ArrayList<FlashcardCollection> allFlashcardCollections, String query) {
        ArrayList<FlashcardCollection> filterList = new ArrayList<>();
        for (FlashcardCollection flashcardCollection : allFlashcardCollections){
            if(flashcardCollection.getTitle().toLowerCase().contains(query)) {
                filterList.add(flashcardCollection);
            }
        }
        flashcardCollections = filterList;
        recyclerView(allFlashcardCollections, filterList);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard);

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

        allFlashcardCollections = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        flashcardCollectionCounter = findViewById(R.id.flashcardCollectionCounter);

        ImageButton addFlashcardCollectionButton = findViewById(R.id.addFlashcardCollectionButton);

        // Adds a flashcard collection to firebase
        addFlashcardCollectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(FlashcardActivity.this);
                View view = LayoutInflater.from(FlashcardActivity.this).inflate(R.layout.add_flashcard_collection_bottom_sheet, null);
                bottomSheetDialog.setContentView(view);
                bottomSheetDialog.show();

                TextInputEditText titleEditText = view.findViewById(R.id.titleEditText);

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
                        currentFlashcardCollectionId++;

                        Map<String, Object> flashcardCollectionData = new HashMap<>();
                        flashcardCollectionData.put("title", titleEditText.getText().toString());
                        flashcardCollectionData.put("flashcardCount", 0);
                        flashcardCollectionData.put("correct", 0);
                        flashcardCollectionData.put("uid", currentFirebaseUserUid);
                        db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(currentFlashcardCollectionId)).set(flashcardCollectionData);

                        bottomSheetDialog.dismiss();

                        Toast toast = new Toast(FlashcardActivity.this);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        LayoutInflater layoutInflater = (LayoutInflater) FlashcardActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View view = layoutInflater.inflate(R.layout.toast_added, null);
                        toast.setView(view);
                        toast.show();
                    }
                });
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
                FlashcardCollection flashcardCollection = allFlashcardCollections.get(position);
                db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(flashcardCollection.id)).delete();
                allFlashcardCollections.remove(flashcardCollection);
                flashcardCollections.remove(flashcardCollection);
                recyclerView.getAdapter().notifyItemRemoved(position);

                // Delete toast message
                Toast toast = new Toast(FlashcardActivity.this);
                toast.setDuration(Toast.LENGTH_SHORT);
                LayoutInflater layoutInflater = (LayoutInflater) FlashcardActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        // Read from firebase and create flashcard collections on create
        db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections")
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
                            String docFlashcardCollectionUid = String.valueOf(dc.getDocument().getData().get("uid"));
                            if (dc.getType() == DocumentChange.Type.ADDED && docFlashcardCollectionUid.equals(currentFirebaseUserUid)) {
                                if (Integer.parseInt(dc.getDocument().getId()) > currentFlashcardCollectionId) {
                                    currentFlashcardCollectionId = Integer.parseInt(dc.getDocument().getId());
                                }

                                flashcardCollectionCount++;
                                flashcardCollectionCounter.setText(String.format(getResources().getString(R.string.flashcard_collection_counter), flashcardCollectionCount));

                                FlashcardCollection flashcardCollection = new FlashcardCollection(dc.getDocument().getData().get("title").toString(), Integer.parseInt(dc.getDocument().getId()), Integer.parseInt(dc.getDocument().getData().get("flashcardCount").toString()), Integer.parseInt(dc.getDocument().getData().get("correct").toString()));
                                allFlashcardCollections.add(flashcardCollection);
                                filter(allFlashcardCollections, "");
                            }
                            else if (dc.getType() == DocumentChange.Type.REMOVED && docFlashcardCollectionUid.equals(currentFirebaseUserUid)) {
                                flashcardCollectionCount--;
                                flashcardCollectionCounter.setText(String.format(getResources().getString(R.string.flashcard_collection_counter), flashcardCollectionCount));
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