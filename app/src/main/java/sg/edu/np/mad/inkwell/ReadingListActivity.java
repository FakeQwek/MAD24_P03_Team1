package sg.edu.np.mad.inkwell;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ReadingListActivity extends AppCompatActivity implements BookAdapter.OnBookChangeListener {
    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> bookList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_list);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookList = new ArrayList<>();
        bookAdapter = new BookAdapter(bookList, this);
        recyclerView.setAdapter(bookAdapter);

        findViewById(R.id.addBookButton).setOnClickListener(view -> showAddBookDialog());

        loadBooks();
    }

    private void loadBooks() {
        String userId = auth.getCurrentUser().getUid();
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
            Book book = new Book(id, title, author, "To Read", "", coverUrl);
            addBook(book);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addBook(Book book) {
        String userId = auth.getCurrentUser().getUid();
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
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("books").document(book.getId()).set(book)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Book updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update book", Toast.LENGTH_SHORT).show());
    }

    private void removeBook(Book book) {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("books").document(book.getId()).delete()
                .addOnSuccessListener(aVoid -> {
                    bookList.remove(book);
                    bookAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Book removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to remove book", Toast.LENGTH_SHORT).show());
    }
}





