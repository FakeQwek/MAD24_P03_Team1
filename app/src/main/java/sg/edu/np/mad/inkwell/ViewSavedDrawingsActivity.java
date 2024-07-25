package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewSavedDrawingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DrawingsAdapter adapter;
    private List<Map<String, Object>> drawingList;
    private TextView layoutEmpty;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_saved_drawings);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recycler_view_drawings);
        layoutEmpty = findViewById(R.id.layout_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        drawingList = new ArrayList<>();
        adapter = new DrawingsAdapter(drawingList, drawing -> {
            Intent intent = new Intent(ViewSavedDrawingsActivity.this, FullImageActivity.class);
            intent.putExtra("imageUrl", (String) drawing.get("imageUrl"));
            intent.putExtra("title", (String) drawing.get("title"));
            intent.putExtra("name", (String) drawing.get("name"));
            startActivity(intent);
        }, this::onDeleteClick); // Pass delete listener
        recyclerView.setAdapter(adapter);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        adapter.setDeleteButtonVisibility(View.VISIBLE);

        fetchSavedDrawings();
    }

    private void fetchSavedDrawings() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("users").document(userId).collection("drawings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        drawingList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Map<String, Object> drawing = document.getData();
                            drawing.put("documentId", document.getId());
                            drawingList.add(drawing);
                        }
                        adapter.notifyDataSetChanged();
                        layoutEmpty.setVisibility(drawingList.isEmpty() ? View.VISIBLE : View.GONE);
                    } else {
                        // Handle error
                    }
                });
    }

    private void onDeleteClick(Map<String, Object> drawing) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Drawing")
                .setMessage("Are you sure you want to delete this drawing from your saved drawings?")
                .setPositiveButton("Yes", (dialog, which) -> deleteDrawing(drawing))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteDrawing(Map<String, Object> drawing) {
        String userId = firebaseAuth.getCurrentUser().getUid();
        String drawingId = (String) drawing.get("documentId");

        firebaseFirestore.collection("users").document(userId).collection("drawings").document(drawingId).delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        firebaseFirestore.collection("drawings").document(drawingId).delete()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        drawingList.remove(drawing);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(this, "Drawing deleted successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Failed to delete from collection", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Failed to delete from saved drawings", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}