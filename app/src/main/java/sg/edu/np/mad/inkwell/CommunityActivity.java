package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
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

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    FirebaseStorage storage = FirebaseStorage.getInstance();

    StorageReference storageRef = storage.getReference();

    private int noteCount;

    public static CommunityNote selectedNote;

    private ArrayList<CommunityNote> communityNotes = new ArrayList<>();

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public static boolean manageNotes = false;

    ArrayList<CommunityNote> communityNoteList;

    private String stringURLEndPoint = "https://api.openai.com/v1/chat/completions";

    private String stringAPIKey = "API KEY";

    public static String promptResponse = "";

    private String prompt = "output a string of an mcq question in this format. question; option A; option B; option C; option D; answer make sure to add the semicolons. only output the string. do this for 10 questions. separate the questions with semicolons as well. do not number the questions. label the options with A, B, C, D. do not have spaces after semicolons. The answer should be in this format A";

    // function to call ChatGPT API to generate questions based on the community note content
    public void callAPI(String prompt) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("model", "gpt-4o-mini");

            JSONArray jsonArrayMessage = new JSONArray();
            JSONObject jsonObjectMessage = new JSONObject();
            jsonObjectMessage.put("role", "user");
            jsonObjectMessage.put("content", prompt);
            jsonArrayMessage.put(jsonObjectMessage);

            jsonObject.put("messages", jsonArrayMessage);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                stringURLEndPoint, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String content = null;
                try {
                    content = response.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                promptResponse = promptResponse + content;

                Intent quiz = new Intent(CommunityActivity.this, CommunityQuizActivity.class);
                startActivity(quiz);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> mapHeader = new HashMap<>();
                mapHeader.put("Authorization", "Bearer " + stringAPIKey);
                mapHeader.put("Content-Type", "application/json");

                return mapHeader;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };

        int intTimeoutPeriod = 60000; // 60 seconds timeout duration defined
        RetryPolicy retryPolicy = new DefaultRetryPolicy(intTimeoutPeriod,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);
    }

    // Method to set items in the recycler view
    private void recyclerView(ArrayList<CommunityNote> communityNoteList, ArrayList<CommunityNote> communityNotes) {
        RecyclerView recyclerView = findViewById(R.id.communityRecyclerView);
        CommunityNoteAdapter adapter = new CommunityNoteAdapter(communityNoteList, communityNotes, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    // method to filter the contents of the recycler view based on the search query
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

        communityNoteList = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.communityRecyclerView);

        manageNotes = false;

        ViewAnimator viewAnimator = findViewById(R.id.viewAnimator);

        ImageButton swapButton = findViewById(R.id.swapButton);

        // button to swap displayed child of view animator
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

        // button to allow user to add the community note to their own notes
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedNote != null) {
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

                    Toast toast = new Toast(CommunityActivity.this);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    LayoutInflater layoutInflater = (LayoutInflater) CommunityActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = layoutInflater.inflate(R.layout.toast_added, null);
                    toast.setView(view);
                    toast.show();
                } else {
                    Toast toast = new Toast(CommunityActivity.this);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    LayoutInflater layoutInflater = (LayoutInflater) CommunityActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = layoutInflater.inflate(R.layout.toast_no_note_selected, null);
                    toast.setView(view);
                    toast.show();
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

        TextView pageTitle = findViewById(R.id.pageTitle);

        // allows user to remove their own community notes
        manageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (manageNotes) {
                    manageNotes = false;

                    pageTitle.setText("Community");

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

                    pageTitle.setText("My Notes");

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

        ImageButton leaderboardButton = findViewById(R.id.leaderboardButton);

        // go to leaderboard activity
        leaderboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedNote != null) {
                    Intent leaderboard = new Intent(CommunityActivity.this, LeaderboardActivity.class);
                    startActivity(leaderboard);
                } else {
                    Toast toast = new Toast(CommunityActivity.this);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    LayoutInflater layoutInflater = (LayoutInflater) CommunityActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = layoutInflater.inflate(R.layout.toast_no_note_selected, null);
                    toast.setView(view);
                    toast.show();
                }
            }
        });

        Button quizButton = findViewById(R.id.quizButton);

        // start ai generated quiz
        quizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedNote != null) {
                    callAPI(noteBody.getText().toString() + prompt);
                } else {
                    Toast toast = new Toast(CommunityActivity.this);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    LayoutInflater layoutInflater = (LayoutInflater) CommunityActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = layoutInflater.inflate(R.layout.toast_no_note_selected, null);
                    toast.setView(view);
                    toast.show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // get data of all community notes
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

        // get data of the user's notes to store the data of their note count
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