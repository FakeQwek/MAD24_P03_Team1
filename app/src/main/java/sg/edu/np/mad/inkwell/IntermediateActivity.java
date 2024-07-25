package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class IntermediateActivity extends AppCompatActivity {

    private Button btnNewDrawing, btnViewSavedDrawings, btnViewOtherDrawings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intermediate);

        btnNewDrawing = findViewById(R.id.btn_new_drawing);
        btnViewSavedDrawings = findViewById(R.id.btn_view_saved_drawings);
        btnViewOtherDrawings = findViewById(R.id.btn_view_other_drawings);

        btnNewDrawing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntermediateActivity.this, DrawingMainActivity.class);
                startActivity(intent);
            }
        });

        btnViewSavedDrawings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntermediateActivity.this, ViewSavedDrawingsActivity.class);
                startActivity(intent);
            }
        });

        btnViewOtherDrawings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntermediateActivity.this, ViewPublicCollectionActivity.class);
                startActivity(intent);
            }
        });
    }
}