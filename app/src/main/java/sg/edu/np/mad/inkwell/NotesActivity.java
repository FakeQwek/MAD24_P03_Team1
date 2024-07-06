package sg.edu.np.mad.inkwell;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
import java.util.Objects;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class NotesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Declaration of variables

    // currentNoteId keeps track of the ids that have already been assigned
    public static int currentNoteId;

    // selectedNoteId keeps track of the note that has been selected
    public static int selectedNoteId = 1;

    public static ArrayList<File> files = new ArrayList<>();

    public static ArrayList<Integer> fileIds = new ArrayList<>();

    public static ArrayList<File> fileOrder = new ArrayList<>();

    public static int fileOrderIndex;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());

    OkHttpClient client = new OkHttpClient();

    MediaType JSON = MediaType.get("application/json; charset=utf-8");

    void callAPI(String prompt) {
        TextView promptAnswer = findViewById(R.id.promptAnswer);
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo");
            jsonBody.put("prompt", prompt);
            jsonBody.put("max_tokens", 4000);
            jsonBody.put("temperature", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
            .url("https://api.openai.com/v1/completions")
            .header("Authorization", "Bearer API KEY")
            .post(body)
            .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("apple", "Failed to load response due to " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getString("text");
                        Log.d("apple", result.trim());
                        promptAnswer.setText(result.trim());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else {
                    Log.d("apple", "Failed to load response due to " + response.body().string());
                }
            }
        });
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
        menu.findItem(R.id.nav_todos).setVisible(false);
        menu.findItem(R.id.nav_flashcards).setVisible(false);
        menu.findItem(R.id.nav_calendar).setVisible(false);
        menu.findItem(R.id.nav_timetable).setVisible(false);
        menu.findItem(R.id.nav_settings).setVisible(false);
        menu.findItem(R.id.nav_profile).setVisible(false);
        menu.findItem(R.id.nav_logout).setVisible(false);

        ImageButton swapButton = findViewById(R.id.swapButton);

        if (swapButton != null) {
            swapButton.setVisibility(View.VISIBLE);

            swapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (menu.hasVisibleItems()) {
                        menu.findItem(R.id.nav_home).setVisible(false);
                        menu.findItem(R.id.nav_notes).setVisible(false);
                        menu.findItem(R.id.nav_todos).setVisible(false);
                        menu.findItem(R.id.nav_flashcards).setVisible(false);
                        menu.findItem(R.id.nav_calendar).setVisible(false);
                        menu.findItem(R.id.nav_timetable).setVisible(false);
                        menu.findItem(R.id.nav_settings).setVisible(false);
                        menu.findItem(R.id.nav_profile).setVisible(false);
                        menu.findItem(R.id.nav_logout).setVisible(false);
                        searchView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        menu.findItem(R.id.nav_home).setVisible(true);
                        menu.findItem(R.id.nav_notes).setVisible(true);
                        menu.findItem(R.id.nav_todos).setVisible(true);
                        menu.findItem(R.id.nav_flashcards).setVisible(true);
                        menu.findItem(R.id.nav_calendar).setVisible(true);
                        menu.findItem(R.id.nav_timetable).setVisible(true);
                        menu.findItem(R.id.nav_settings).setVisible(true);
                        menu.findItem(R.id.nav_profile).setVisible(true);
                        menu.findItem(R.id.nav_logout).setVisible(true);
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

        EditText noteTitle = findViewById(R.id.noteTitle);
        EditText noteBody = findViewById(R.id.noteBody);

        fileOrder = new ArrayList<>();

        fileOrderIndex = -1;

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

        aiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewAnimator.setDisplayedChild(1);
            }
        });

        TextInputEditText promptEditText = findViewById(R.id.promptEditText);
        ImageButton sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callAPI(promptEditText.getText().toString().trim());
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
