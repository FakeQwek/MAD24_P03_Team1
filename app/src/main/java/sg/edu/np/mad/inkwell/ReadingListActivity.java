package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ReadingListActivity extends AppCompatActivity implements BookAdapter.OnBookChangeListener, NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> bookList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Mock user ID for testing purposes
    private static final String MOCK_USER_ID = "mockUserId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_list);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up navigation drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Skip authentication check for debugging
        String userId = MOCK_USER_ID;

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookList = new ArrayList<>();
        bookAdapter = new BookAdapter(bookList, this);
        recyclerView.setAdapter(bookAdapter);

        findViewById(R.id.addBookButton).setOnClickListener(view -> showAddBookDialog());

        loadBooks(userId);
    }

    private void loadBooks(String userId) {
        db.collection("users").document(userId).collection("books").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Book book = document.toObject(Book.class);
                        book.setId(document.getId());
                        bookList.add(book);
                    }
                    bookAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load books", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onBookUpdated(Book book) {
        updateBook(book);
    }

    @Override
    public void onBookRemoved(Book book) {
        removeBook(book);
    }

    private void showAddBookDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_book, null);
        builder.setView(dialogView);

        EditText titleEditText = dialogView.findViewById(R.id.titleEditText);
        EditText authorEditText = dialogView.findViewById(R.id.authorEditText);
        EditText coverUrlEditText = dialogView.findViewById(R.id.coverUrlEditText);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = titleEditText.getText().toString();
            String author = authorEditText.getText().toString();
            String coverUrl = coverUrlEditText.getText().toString();

            String id = db.collection("books").document().getId();
            Book book = new Book(id, title, author, "To Read", "", coverUrl, 0, 0);
            addBook(book, MOCK_USER_ID);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addBook(Book book, String userId) {
        db.collection("users").document(userId).collection("books").add(book)
                .addOnSuccessListener(documentReference -> {
                    book.setId(documentReference.getId());
                    bookList.add(book);
                    bookAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Book added successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add book", Toast.LENGTH_SHORT).show());
    }

    private void updateBook(Book book) {
        db.collection("users").document(MOCK_USER_ID).collection("books").document(book.getId()).set(book)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Book updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update book", Toast.LENGTH_SHORT).show());
    }

    private void removeBook(Book book) {
        db.collection("users").document(MOCK_USER_ID).collection("books").document(book.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    bookList.remove(book);
                    bookAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Book removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to remove book", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        Navbar navbar = new Navbar(this);
        Intent newActivity = navbar.redirect(id);
        startActivity(newActivity);
        return true;
    }
}
