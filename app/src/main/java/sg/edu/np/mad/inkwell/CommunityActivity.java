package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ViewAnimator;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CommunityActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private int noteCount;

    public static boolean selectedNote = false;

    private ArrayList<CommunityNote> communityNotes = new ArrayList<>();

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());

    public static boolean manageNotes = false;

    private void recyclerView(ArrayList<CommunityNote> communityNoteList, ArrayList<CommunityNote> communityNotes) {
        RecyclerView recyclerView = findViewById(R.id.communityRecyclerView);
        CommunityNoteAdapter adapter = new CommunityNoteAdapter(communityNoteList, communityNotes, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void filter(ArrayList<CommunityNote> communityNoteList, String query) {
        ArrayList<CommunityNote> filterList = new ArrayList<>();
        for (CommunityNote communityNote : communityNoteList){
            if(communityNote.getTitle().toLowerCase().contains(query)) {
                filterList.add(communityNote);
            }
        }
        communityNotes = filterList;
        recyclerView(communityNoteList, filterList);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

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

        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReference();

        ArrayList<CommunityNote> communityNoteList = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.communityRecyclerView);

        manageNotes = false;

        db.collection("community")
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
                                StorageReference imageRef = storageRef.child("users/" + dc.getDocument().getData().get("uid").toString() + "/profile.jpg");

                                long ONE_MEGABYTE = 1024 * 1024;

                                imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        CommunityNote communityNote = new CommunityNote(dc.getDocument().getId(), dc.getDocument().getData().get("title").toString(), dc.getDocument().getData().get("body").toString(), dc.getDocument().getData().get("email").toString(), dc.getDocument().getData().get("uid").toString(), bitmap, dc.getDocument().getData().get("dateCreated").toString());
                                        communityNoteList.add(communityNote);
                                        filter(communityNoteList, "");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        CommunityNote communityNote = new CommunityNote(dc.getDocument().getId(), dc.getDocument().getData().get("title").toString(), dc.getDocument().getData().get("body").toString(), dc.getDocument().getData().get("email").toString(), dc.getDocument().getData().get("uid").toString(), null, dc.getDocument().getData().get("dateCreated").toString());
                                        communityNoteList.add(communityNote);
                                        filter(communityNoteList, "");
                                    }
                                });
                            }
                            else if (dc.getType() == DocumentChange.Type.REMOVED) {

                            }
                        }
                    }
                });

        db.collection("users").document(currentFirebaseUserUid).collection("notes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (Integer.parseInt(document.getId()) > noteCount) {
                                    noteCount = Integer.parseInt(document.getId());
                                }
                            }
                        } else {
                            Log.d("testing", "Error getting documents: ", task.getException());
                        }
                    }
                });

        ViewAnimator viewAnimator = findViewById(R.id.viewAnimator);

        ImageButton swapButton = findViewById(R.id.swapButton);

        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewAnimator.getDisplayedChild() == 0) {
                    viewAnimator.setDisplayedChild(1);
                } else {
                    viewAnimator.setDisplayedChild(0);
                }
            }
        });

        ImageButton downloadButton = findViewById(R.id.downloadButton);

        TextView noteTitle = findViewById(R.id.noteTitle);

        TextView noteBody = findViewById(R.id.noteBody);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedNote) {
                    noteCount++;

                    Date currentDate = Calendar.getInstance().getTime();

                    String dateString = simpleDateFormat.format(currentDate);

                    Map<String, Object> newNoteData = new HashMap<>();
                    newNoteData.put("title", noteTitle.getText().toString());
                    newNoteData.put("body", noteBody.getText().toString());
                    newNoteData.put("type", "file");
                    newNoteData.put("uid", currentFirebaseUserUid);
                    newNoteData.put("dateCreated", dateString);
                    newNoteData.put("dateUpdated", dateString);

                    db.collection("users").document(currentFirebaseUserUid).collection("notes").document(String.valueOf(noteCount)).set(newNoteData);
                }
            }
        });

        SearchView searchView = findViewById(R.id.searchView);

        // Search the items in recycler view
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(communityNoteList, query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(communityNoteList, newText);
                return false;
            }
        });

        ImageButton manageButton = findViewById(R.id.manageButton);

        manageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (manageNotes) {
                    manageNotes = false;

                    communityNoteList.clear();
                    communityNotes.clear();
                    recyclerView.getAdapter().notifyDataSetChanged();

                    db.collection("community")
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
                                            StorageReference imageRef = storageRef.child("users/" + dc.getDocument().getData().get("uid").toString() + "/profile.jpg");

                                            long ONE_MEGABYTE = 1024 * 1024;

                                            imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                @Override
                                                public void onSuccess(byte[] bytes) {
                                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                    CommunityNote communityNote = new CommunityNote(dc.getDocument().getId(), dc.getDocument().getData().get("title").toString(), dc.getDocument().getData().get("body").toString(), dc.getDocument().getData().get("email").toString(), dc.getDocument().getData().get("uid").toString(), bitmap, dc.getDocument().getData().get("dateCreated").toString());
                                                    communityNoteList.add(communityNote);
                                                    filter(communityNoteList, "");
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    CommunityNote communityNote = new CommunityNote(dc.getDocument().getId(), dc.getDocument().getData().get("title").toString(), dc.getDocument().getData().get("body").toString(), dc.getDocument().getData().get("email").toString(), dc.getDocument().getData().get("uid").toString(), null, dc.getDocument().getData().get("dateCreated").toString());
                                                    communityNoteList.add(communityNote);
                                                    filter(communityNoteList, "");
                                                }
                                            });
                                        }
                                        else if (dc.getType() == DocumentChange.Type.REMOVED) {

                                        }
                                    }
                                }
                            });
                } else {
                    manageNotes = true;

                    communityNoteList.clear();
                    communityNotes.clear();
                    recyclerView.getAdapter().notifyDataSetChanged();


                    db.collection("community")
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
                                            if (dc.getDocument().getData().get("uid").toString().equals(currentFirebaseUserUid)) {
                                                StorageReference imageRef = storageRef.child("users/" + dc.getDocument().getData().get("uid").toString() + "/profile.jpg");

                                                long ONE_MEGABYTE = 1024 * 1024;

                                                imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                    @Override
                                                    public void onSuccess(byte[] bytes) {
                                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                        CommunityNote communityNote = new CommunityNote(dc.getDocument().getId(), dc.getDocument().getData().get("title").toString(), dc.getDocument().getData().get("body").toString(), dc.getDocument().getData().get("email").toString(), dc.getDocument().getData().get("uid").toString(), bitmap, dc.getDocument().getData().get("dateCreated").toString());
                                                        communityNoteList.add(communityNote);
                                                        filter(communityNoteList, "");
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception exception) {
                                                        CommunityNote communityNote = new CommunityNote(dc.getDocument().getId(), dc.getDocument().getData().get("title").toString(), dc.getDocument().getData().get("body").toString(), dc.getDocument().getData().get("email").toString(), dc.getDocument().getData().get("uid").toString(), null, dc.getDocument().getData().get("dateCreated").toString());
                                                        communityNoteList.add(communityNote);
                                                        filter(communityNoteList, "");
                                                    }
                                                });
                                            }
                                        }
                                        else if (dc.getType() == DocumentChange.Type.REMOVED) {

                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

    //Allows movement between activities upon clicking
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {


        int id = menuItem.getItemId();
        Navbar navbar = new Navbar(this);
        Intent newActivity = navbar.redirect(id);
        startActivity(newActivity);

        return true;
    }
}