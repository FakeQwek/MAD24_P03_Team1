package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
        });
        recyclerView.setAdapter(adapter);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

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
                            drawingList.add(drawing);
                        }
                        adapter.notifyDataSetChanged();
                        layoutEmpty.setVisibility(drawingList.isEmpty() ? View.VISIBLE : View.GONE);
                    } else {
                        // Handle error
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}