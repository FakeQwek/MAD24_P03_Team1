package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;


public class NotesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Declaration of variables

    // currentNoteId keeps track of the ids that have already been assigned
    public static int currentNoteId = 1;

    // selectedNoteId keeps track of the note that has been selected
    public static int selectedNoteId = 1;

    public static File selectedFile;

    public static ArrayList<File> files = new ArrayList<>();

    public static ArrayList<Integer> fileIds = new ArrayList<>();

    public static ArrayList<File> fileOrder = new ArrayList<>();

    public static int fileOrderIndex;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private String stringURLEndPoint = "https://api.openai.com/v1/chat/completions";

    private String stringAPIKey = "API KEY";

    private String promptResponse = "";

    public static int currentMessageId;

    public static ArrayList<Message> messageList = new ArrayList<>();

    FirebaseStorage storage = FirebaseStorage.getInstance();

    StorageReference storageRef = storage.getReference();

    ArrayList<Friend> friendList;

    EditText noteTitle;

    EditText noteBody;

    public static String longClickSelectedNoteTitle;

    public static String longClickSelectedNoteBody;

    int currentFlashcardCollectionId = 1;

    // function to prompt ChatGPT API questions
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

                currentMessageId += 1;

                Map<String, Object> newMessage = new HashMap<>();
                newMessage.put("message", promptResponse);
                newMessage.put("uid", currentFirebaseUserUid);
                newMessage.put("type", "received");

                db.collection("users").document(currentFirebaseUserUid).collection("notes").document(String.valueOf(selectedFile.getId())).collection("messages").document(String.valueOf(currentMessageId)).set(newMessage);

                Message message = new Message(currentMessageId, promptResponse, "received");

                messageList.add(message);
                messageList.sort(Comparator.comparingInt(i -> i.id));
                messageRecyclerView(messageList);

                promptResponse = "";
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

    // function to use ChatGPT API to generate flashcards and create flashcards in the flashcard activity
    public void callAPIForFlashcards(String prompt) {
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

                String[] questions = content.split(";", 0);

                if (questions.length == 20) {
                    db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            if (document.getData().get("uid").equals(currentFirebaseUserUid)) {
                                                if (Integer.parseInt(document.getId()) > currentFlashcardCollectionId) {
                                                    currentFlashcardCollectionId = Integer.parseInt(document.getId());
                                                }
                                            }
                                        }
                                        currentFlashcardCollectionId++;

                                        Map<String, Object> flashcardCollectionData = new HashMap<>();
                                        flashcardCollectionData.put("title", noteTitle.getText().toString());
                                        flashcardCollectionData.put("flashcardCount", 10);
                                        flashcardCollectionData.put("correct", 0);
                                        flashcardCollectionData.put("uid", currentFirebaseUserUid);

                                        db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(currentFlashcardCollectionId)).set(flashcardCollectionData);

                                        for (int i = 0; i < 10; i++) {
                                            Map<String, Object> flashcardData = new HashMap<>();
                                            flashcardData.put("question", questions[i * 2]);
                                            flashcardData.put("answer", questions[i * 2 + 1]);

                                            db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(currentFlashcardCollectionId)).collection("flashcards").document(String.valueOf(i + 1)).set(flashcardData);
                                        }

                                        Intent activity = new Intent(NotesActivity.this, FlashcardActivity.class);
                                        startActivity(activity);
                                    } else {
                                        Log.d("testing", "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                } else {
                    Toast toast = new Toast(NotesActivity.this);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    LayoutInflater layoutInflater = (LayoutInflater) NotesActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = layoutInflater.inflate(R.layout.toast_deleted, null);
                    TextView toastMessage = view.findViewById(R.id.toastMessage);
                    toastMessage.setText("Try Again");
                    toast.setView(view);
                    toast.show();
                }
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
    private void recyclerView(ArrayList<Object> allNotes) {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        NotesAdapter adapter = new NotesAdapter(allNotes, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(adapter);
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    public void messageRecyclerView(ArrayList<Message> messageList) {
        RecyclerView recyclerView = findViewById(R.id.messageRecyclerView2);
        MessageAdapter adapter = new MessageAdapter(messageList, null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    // Method to filter items already in the recycler view
    private void filter(ArrayList<File> files, ArrayList<Object> notes, String query) {
        ArrayList<Object> filterList = new ArrayList<>();
        if (query.isEmpty()) {
            recyclerView(notes);
        } else {
            for (File file : files){
                if (file.title.toLowerCase().contains(query)) {
                    filterList.add(file);
                }
            }
            recyclerView(filterList);
        }
    }

    // Method to search the items in recycler view
    private void search(ArrayList<File> files, ArrayList<Object> notes) {
        SearchView searchView = findViewById(R.id.searchView);

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filter(files, notes, query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filter(files, notes, newText);
                    return false;
                }
            });
        }
    }

    // Method to notify recycler view a new item has been inserted
    private void notifyInsert() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(0);
    }

    private void navigationBar() {
        SearchView searchView = findViewById(R.id.searchView);

        if (searchView != null) {
            searchView.setVisibility(View.VISIBLE);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();

        menu.findItem(R.id.nav_home).setVisible(false);
        menu.findItem(R.id.nav_notes).setVisible(false);
        menu.findItem(R.id.nav_mind_map).setVisible(false);
        menu.findItem(R.id.nav_todos).setVisible(false);
        menu.findItem(R.id.nav_flashcards).setVisible(false);
        menu.findItem(R.id.nav_calendar).setVisible(false);
        menu.findItem(R.id.nav_timetable).setVisible(false);
        menu.findItem(R.id.nav_settings).setVisible(false);
        menu.findItem(R.id.nav_profile).setVisible(false);
        menu.findItem(R.id.nav_logout).setVisible(false);
        menu.findItem(R.id.nav_friends).setVisible(false);
        menu.findItem(R.id.nav_community).setVisible(false);
        menu.findItem(R.id.nav_essay).setVisible(false);
        menu.findItem(R.id.nav_drawing).setVisible(false);

        ImageButton swapButton = findViewById(R.id.swapButton);

        if (swapButton != null) {
            swapButton.setVisibility(View.VISIBLE);

            swapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (menu.hasVisibleItems()) {
                        menu.findItem(R.id.nav_home).setVisible(false);
                        menu.findItem(R.id.nav_notes).setVisible(false);
                        menu.findItem(R.id.nav_mind_map).setVisible(false);
                        menu.findItem(R.id.nav_todos).setVisible(false);
                        menu.findItem(R.id.nav_flashcards).setVisible(false);
                        menu.findItem(R.id.nav_calendar).setVisible(false);
                        menu.findItem(R.id.nav_timetable).setVisible(false);
                        menu.findItem(R.id.nav_settings).setVisible(false);
                        menu.findItem(R.id.nav_profile).setVisible(false);
                        menu.findItem(R.id.nav_logout).setVisible(false);
                        menu.findItem(R.id.nav_friends).setVisible(false);
                        menu.findItem(R.id.nav_community).setVisible(false);
                        menu.findItem(R.id.nav_essay).setVisible(false);
                        menu.findItem(R.id.nav_drawing).setVisible(false);
                        searchView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        menu.findItem(R.id.nav_home).setVisible(true);
                        menu.findItem(R.id.nav_notes).setVisible(true);
                        menu.findItem(R.id.nav_mind_map).setVisible(true);
                        menu.findItem(R.id.nav_todos).setVisible(true);
                        menu.findItem(R.id.nav_flashcards).setVisible(true);
                        menu.findItem(R.id.nav_calendar).setVisible(true);
                        menu.findItem(R.id.nav_timetable).setVisible(true);
                        menu.findItem(R.id.nav_settings).setVisible(true);
                        menu.findItem(R.id.nav_profile).setVisible(true);
                        menu.findItem(R.id.nav_logout).setVisible(true);
                        menu.findItem(R.id.nav_friends).setVisible(true);
                        menu.findItem(R.id.nav_community).setVisible(true);
                        menu.findItem(R.id.nav_essay).setVisible(true);
                        menu.findItem(R.id.nav_drawing).setVisible(true);
                        searchView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

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

        ArrayList<Object> notes = new ArrayList<>();

        noteTitle = findViewById(R.id.noteTitle);
        noteBody = findViewById(R.id.noteBody);

        fileOrder = new ArrayList<>();

        fileOrderIndex = -1;

        friendList = new ArrayList<>();

        // Read from firebase and create files and folders on create
        db.collection("users").document(currentFirebaseUserUid).collection("notes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                search(files, notes);
                                navigationBar();
                                String docNoteType = document.getData().get("type").toString();
                                String docNoteUid = document.getData().get("uid").toString();
                                if (Integer.parseInt(document.getId()) > currentNoteId) {
                                    currentNoteId = Integer.parseInt(document.getId());
                                }
                                if (docNoteType.equals("file") && docNoteUid.equals(currentFirebaseUserUid)) {
                                    File file;
                                    try {
                                        file = new File(document.getData().get("title").toString(), document.getData().get("body").toString(), Integer.parseInt(document.getId()), docNoteType, document.getReference(), simpleDateFormat.parse(document.getData().get("dateCreated").toString()), simpleDateFormat.parse(document.getData().get("dateUpdated").toString()));
                                    } catch (ParseException e) {
                                        throw new RuntimeException(e);
                                    }
                                    notes.add(file);
                                    filter(files, notes, "");
                                } else if (docNoteType.equals("folder") && docNoteUid.equals(currentFirebaseUserUid)) {
                                    Folder folder;
                                    try {
                                        folder = new Folder(document.getData().get("title").toString(), document.getData().get("body").toString(), Integer.parseInt(document.getId()), docNoteType, db.collection("users").document(currentFirebaseUserUid).collection("notes"), simpleDateFormat.parse(document.getData().get("dateCreated").toString()), simpleDateFormat.parse(document.getData().get("dateUpdated").toString()), document.getData().get("bookmarkColour").toString());
                                    } catch (ParseException e) {
                                        throw new RuntimeException(e);
                                    }
                                    notes.add(folder);
                                    filter(files, notes, "");
                                }
                            }
                        } else {
                            Log.d("testing", "Error getting documents: ", task.getException());
                        }
                    }
                });

        ImageButton addFileButton = findViewById(R.id.addFileButton);

        // Adds a file to firebase and updates the recycler view
        addFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentNoteId++;

                Date currentDate = Calendar.getInstance().getTime();

                String dateString = simpleDateFormat.format(currentDate);

                Map<String, Object> fileData = new HashMap<>();
                fileData.put("title", "Title");
                fileData.put("body", "Enter your text");
                fileData.put("type", "file");
                fileData.put("uid", currentFirebaseUserUid);
                fileData.put("dateCreated", dateString);
                fileData.put("dateUpdated", dateString);

                db.collection("users").document(currentFirebaseUserUid).collection("notes").document(String.valueOf(currentNoteId)).set(fileData);

                File file = new File("Title", "Enter your text", currentNoteId, "file", db.collection("users").document(currentFirebaseUserUid).collection("notes").document(String.valueOf(currentNoteId)), currentDate, currentDate);
                fileIds.add(file.id);
                files.add(file);
                notes.add(0, file);

                if (currentNoteId == 1) {
                    recyclerView(notes);
                } else if (notes.size() == 1) {
                    recyclerView(notes);
                } else {
                    notifyInsert();
                }

                Toast toast = new Toast(NotesActivity.this);
                toast.setDuration(Toast.LENGTH_SHORT);
                LayoutInflater layoutInflater = (LayoutInflater) NotesActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = layoutInflater.inflate(R.layout.toast_added, null);
                toast.setView(view);
                toast.show();
            }
        });

        ImageButton addFolderButton = findViewById(R.id.addFolderButton);

        // Adds a folder to firebase and updates the recycler view
        addFolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentNoteId++;

                Date currentDate = Calendar.getInstance().getTime();

                String dateString = simpleDateFormat.format(currentDate);

                Map<String, Object> folderData = new HashMap<>();
                folderData.put("title", "Folder");
                folderData.put("body", "");
                folderData.put("type", "folder");
                folderData.put("uid", currentFirebaseUserUid);
                folderData.put("dateCreated", dateString);
                folderData.put("dateUpdated", dateString);
                folderData.put("bookmarkColour", "none");


                db.collection("users").document(currentFirebaseUserUid).collection("notes").document(String.valueOf(currentNoteId)).set(folderData);

                Folder folder = new Folder("Folder", "", NotesActivity.currentNoteId, "folder", db.collection("users").document(currentFirebaseUserUid).collection("notes"), currentDate, currentDate, "none");
                notes.add(0, folder);

                if (currentNoteId == 1) {
                    recyclerView(notes);
                } else if (notes.size() == 1) {
                    recyclerView(notes);
                } else {
                    notifyInsert();
                }

                Toast toast = new Toast(NotesActivity.this);
                toast.setDuration(Toast.LENGTH_SHORT);
                LayoutInflater layoutInflater = (LayoutInflater) NotesActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = layoutInflater.inflate(R.layout.toast_added, null);
                toast.setView(view);
                toast.show();
            }
        });

        ImageButton leftButton = findViewById(R.id.leftButton);

        // go to the last visited note
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileOrderIndex > 0) {
                    fileOrderIndex--;
                    selectedNoteId = fileOrder.get(fileOrderIndex).id;

                    noteTitle.setText(fileOrder.get(fileOrderIndex).title);
                    noteBody.setText(fileOrder.get(fileOrderIndex).body);
                }
            }
        });

        ImageButton rightButton = findViewById(R.id.rightButton);

        // go to the next note
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileOrderIndex < fileOrder.size() - 1) {
                    fileOrderIndex++;
                    selectedNoteId = fileOrder.get(fileOrderIndex).id;

                    noteTitle.setText(fileOrder.get(fileOrderIndex).title);
                    noteBody.setText(fileOrder.get(fileOrderIndex).body);
                }
            }
        });

        ImageButton readOnlyButton = findViewById(R.id.readOnlyButton);

        // make the notes read only
        readOnlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noteTitle.isEnabled()) {
                    noteTitle.setEnabled(false);
                    noteBody.setEnabled(false);
                    readOnlyButton.setImageResource(R.drawable.pencil_outline);
                } else {
                    noteTitle.setEnabled(true);
                    noteBody.setEnabled(true);
                    readOnlyButton.setImageResource(R.drawable.book_open_blank_variant_outline);
                }
            }
        });

        ViewAnimator viewAnimator = findViewById(R.id.viewAnimator);
        ImageButton aiButton = findViewById(R.id.aiButton);

        // make view animator swap pages between the notes writing page and the ai chat page
        aiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewAnimator.getDisplayedChild() == 0) {
                    viewAnimator.setDisplayedChild(1);
                    aiButton.setImageResource(R.drawable.note_outline);
                } else {
                    viewAnimator.setDisplayedChild(0);
                    aiButton.setImageResource(R.drawable.robot_outline);
                }
            }
        });

        ImageButton aiOthersButton = findViewById(R.id.aiOthersButton);

        // bring up a bottom sheet to give the user the option to use AI to generate flashcards
        aiOthersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedFile != null) {
                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(NotesActivity.this);
                    View view = LayoutInflater.from(NotesActivity.this).inflate(R.layout.friend_bottom_sheet, null);
                    bottomSheetDialog.setContentView(view);
                    bottomSheetDialog.show();

                    Button generateAIFlashcardsButton = view.findViewById(R.id.deleteButton);
                    generateAIFlashcardsButton.setText("Generate Flashcards Using AI");
                    generateAIFlashcardsButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String prompt = noteBody.getText().toString().trim() + " output a string of a flashcard in this format. question; answer; make sure to add the semicolons. only output the string. do this for 10 flashcards. separate the questions with semicolons as well. do not number the questions. do not have spaces after semicolons. make sure it is 10 questions.";

                            callAPIForFlashcards(prompt);

                            Toast toast = new Toast(NotesActivity.this);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            LayoutInflater layoutInflater = (LayoutInflater) NotesActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View view = layoutInflater.inflate(R.layout.toast_added, null);
                            TextView toastMessage = view.findViewById(R.id.toastMessage);
                            toastMessage.setText("Generating");
                            toast.setView(view);
                            toast.show();
                        }
                    });
                }
            }
        });

        // listener when scanning a QR code to get a copy of a note from someone else
        ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
                result -> {
                    if (result.getContents() == null) {
                        Toast.makeText(NotesActivity.this, "Cancelled", Toast.LENGTH_LONG).show();
                    } else {
                        String data = result.getContents();

                        String[] dataList = data.split(";");

                        if (dataList.length == 2) {
                            currentNoteId++;

                            Date currentDate = Calendar.getInstance().getTime();

                            String dateString = simpleDateFormat.format(currentDate);

                            Map<String, Object> fileData = new HashMap<>();
                            fileData.put("title", "Title");
                            fileData.put("body", "Enter your text");
                            fileData.put("type", "file");
                            fileData.put("uid", currentFirebaseUserUid);
                            fileData.put("dateCreated", dateString);
                            fileData.put("dateUpdated", dateString);

                            db.collection("users").document(currentFirebaseUserUid).collection("notes").document(String.valueOf(currentNoteId)).set(fileData);

                            File file = new File("Title", "Enter your text", currentNoteId, "file", db.collection("users").document(currentFirebaseUserUid).collection("notes").document(String.valueOf(currentNoteId)), currentDate, currentDate);
                            fileIds.add(file.id);
                            files.add(file);
                            notes.add(0, file);

                            if (currentNoteId == 1) {
                                recyclerView(notes);
                            } else if (notes.size() == 1) {
                                recyclerView(notes);
                            } else {
                                notifyInsert();
                            }
                        }
                        Toast.makeText(NotesActivity.this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                    }
                });

        ImageButton scanQRCodeButton = findViewById(R.id.scanQRCodeButton);

        // enable the camera to scan a QR code
        scanQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanOptions scanOptions = new ScanOptions();
                scanOptions.setOrientationLocked(false);
                scanOptions.setPrompt("Scan a QR code generated by Inkwell");
                barcodeLauncher.launch(scanOptions);
            }
        });

        TextInputEditText promptEditText = findViewById(R.id.promptEditText);
        ImageButton sendButton = findViewById(R.id.sendButton);

        // sends prompt in text input text box to ChatGPT API and displays the message
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedFile != null) {
                    String prompt = noteBody.getText().toString().trim() + " " + promptEditText.getText().toString().trim();

                    currentMessageId += 1;

                    Map<String, Object> newMessage = new HashMap<>();
                    newMessage.put("message", promptEditText.getText().toString());
                    newMessage.put("uid", currentFirebaseUserUid);
                    newMessage.put("type", "sent");

                    db.collection("users").document(currentFirebaseUserUid).collection("notes").document(String.valueOf(selectedFile.getId())).collection("messages").document(String.valueOf(currentMessageId)).set(newMessage);

                    Message message = new Message(currentMessageId, promptEditText.getText().toString(), "sent");

                    messageList.add(message);
                    messageList.sort(Comparator.comparingInt(i -> i.id));
                    messageRecyclerView(messageList);

                    promptEditText.setText("");

                    callAPI(prompt);
                }
            }
        });

        // get data of the user's friends from firebase
        db.collection("users").document(currentFirebaseUserUid).collection("friends")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getData().get("uid").equals(currentFirebaseUserUid)) {
                                    StorageReference imageRef = storageRef.child("users/" + document.getId() + "/profile.jpg");

                                    long ONE_MEGABYTE = 1024 * 1024;

                                    imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            Friend friend = new Friend(document.getId(), document.getId(), document.getData().get("friendEmail").toString(), bitmap);
                                            friendList.add(friend);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Friend friend = new Friend(document.getId(), document.getId(), document.getData().get("friendEmail").toString(), null);
                                            friendList.add(friend);
                                        }
                                    });
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
