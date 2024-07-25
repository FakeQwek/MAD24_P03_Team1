package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewPublicCollectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DrawingsAdapter adapter;
    private List<Map<String, Object>> drawingsList;
    private TextView emptyTextView;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_public_collection);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }

        emptyTextView = findViewById(R.id.text_view_empty);
        recyclerView = findViewById(R.id.recycler_view_public_drawings);

        drawingsList = new ArrayList<>();
        adapter = new DrawingsAdapter(drawingsList, this::onItemClick, drawing -> {

        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();

        adapter.setDeleteButtonVisibility(View.GONE);

        fetchPublicDrawings();
    }

    private void fetchPublicDrawings() {
        emptyTextView.setVisibility(View.GONE);
        firestore.collection("drawings").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Map<String, Object>> drawings = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> drawing = new HashMap<>();
                            drawing.put("imageUrl", document.getString("imageUrl"));
                            drawing.put("title", document.getString("title"));
                            drawing.put("name", document.getString("name"));
                            drawing.put("drawingId", document.getString("drawingId"));
                            drawings.add(drawing);
                        }
                        if (drawings.isEmpty()) {
                            emptyTextView.setVisibility(View.VISIBLE);
                        } else {
                            emptyTextView.setVisibility(View.GONE);
                        }
                        drawingsList.clear();
                        drawingsList.addAll(drawings);
                        adapter.notifyDataSetChanged();
                    } else {
                        emptyTextView.setVisibility(View.VISIBLE);
                        emptyTextView.setText("Failed to load drawings.");
                    }
                });
    }

    private void onItemClick(Map<String, Object> drawing) {
        Intent intent = new Intent(this, FullImageActivity.class);
        intent.putExtra("imageUrl", (String) drawing.get("imageUrl"));
        intent.putExtra("title", (String) drawing.get("title"));
        intent.putExtra("name", (String) drawing.get("name"));
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}