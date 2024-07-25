package sg.edu.np.mad.inkwell;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.RangeSlider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DrawingMainActivity extends AppCompatActivity {

    private DrawView paint;
    private ImageButton save, color, stroke, undo, redo, eraser, reset, fill, selectionMode, deleteSelection;
    private RangeSlider rangeSlider;

    // Custom color picker dialog views
    private View colorPickerDialog;
    private LinearLayout colorPalette;
    private ImageButton closeColorPickerButton;

    private int currentColor;
    private boolean isEraserOn = false;
    private boolean isFillModeOn = false;

    private boolean isSelectionModeOn = false;

    private FirebaseAuth firebaseAuth;  // Firebase authentication instance
    private FirebaseFirestore firebaseFirestore;  // Firebase Firestore instance
    private FirebaseStorage firebaseStorage;  // Firebase Storage instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing_main);

        paint = findViewById(R.id.draw_view);
        rangeSlider = findViewById(R.id.rangebar);
        undo = findViewById(R.id.btn_undo);
        redo = findViewById(R.id.btn_redo);
        save = findViewById(R.id.btn_save);
        color = findViewById(R.id.btn_color);
        stroke = findViewById(R.id.btn_stroke);
        eraser = findViewById(R.id.btn_eraser);
        reset = findViewById(R.id.btn_reset);
        fill = findViewById(R.id.btn_fill);  // Add this line for fill button
        selectionMode = findViewById(R.id.btn_selection_mode);
        deleteSelection = findViewById(R.id.btn_delete_selection);


        // Initialize custom color picker dialog views
        colorPickerDialog = findViewById(R.id.color_picker_dialog);
        colorPalette = findViewById(R.id.color_palette);
        closeColorPickerButton = findViewById(R.id.btn_close_color_picker);

        // Set colors for the color buttons
        setColorButtons();

        color.setOnClickListener(view -> showCustomColorPickerDialog());
        closeColorPickerButton.setOnClickListener(view -> colorPickerDialog.setVisibility(View.GONE));

        undo.setOnClickListener(view -> paint.undo());

        redo.setOnClickListener(view -> paint.redo());

        save.setOnClickListener(view -> saveDrawing());

        stroke.setOnClickListener(view -> toggleRangeSliderVisibility());

        eraser.setOnClickListener(view -> toggleEraser());

        reset.setOnClickListener(view -> resetDrawing());

        fill.setOnClickListener(view -> toggleFillMode());  // Add this line to toggle fill mode

        selectionMode.setOnClickListener(view -> toggleSelectionMode());

        deleteSelection.setOnClickListener(view -> showDeleteSelectionConfirmation());

        rangeSlider.addOnChangeListener((slider, value, fromUser) -> paint.setStrokeWidth((int) value));

        final View rootView = getWindow().getDecorView().getRootView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = rootView.getWidth();
                int height = rootView.getHeight();
                paint.init(height, width);
            }
        });

        // Set default stroke width value
        rangeSlider.setValues(0.5f);

        // Initialize the color picker dialog with default color
        currentColor = Color.BLACK;
        paint.setColor(currentColor);
        paint.setStrokeWidth(0.5f);

        // Initialize Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
    }

    private void showCustomColorPickerDialog() {
        colorPickerDialog.setVisibility(View.VISIBLE);
    }

    private void saveDrawing() {
        showSaveDialog();
        Bitmap bmp = paint.save();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/DrawingApp/");
        values.put(MediaStore.Images.Media.IS_PENDING, 1);
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        try {
            OutputStream imageOutStream = getContentResolver().openOutputStream(uri);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, imageOutStream);
            imageOutStream.close();
            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            getContentResolver().update(uri, values, null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_save_drawing, null);

        EditText etName = view.findViewById(R.id.et_name);
        EditText etTitle = view.findViewById(R.id.et_title);
        Button btnSave = view.findViewById(R.id.btn_save);
        Button btnPublishToCollection = view.findViewById(R.id.btn_publish_to_collection);

        builder.setView(view);
        AlertDialog dialog = builder.create();

        // Create Bitmap outside of the dialog methods to use it in both save and publish functions
        Bitmap bmp = paint.save();

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String title = etTitle.getText().toString().trim();
            if (name.isEmpty() || title.isEmpty()) {
                Toast.makeText(this, "Please enter both name and title", Toast.LENGTH_SHORT).show();
            } else {
                dialog.dismiss();
                saveDrawingToUserSpecificPath(bmp, name, title);
            }
        });

        btnPublishToCollection.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String title = etTitle.getText().toString().trim();
            if (name.isEmpty() || title.isEmpty()) {
                Toast.makeText(this, "Please enter both name and title", Toast.LENGTH_SHORT).show();
            } else {
                dialog.dismiss();
                showPublishConfirmationDialog(bmp, name, title);
            }
        });

        dialog.show();
    }

    private void showPublishConfirmationDialog(Bitmap bmp, String name, String title) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Publish")
                .setMessage("Are you sure you want to publish this drawing? Once published, you cannot delete it.")
                .setPositiveButton("Publish", (dialog, which) -> saveDrawingToCollection(bmp, name, title))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveDrawingToCollection(Bitmap bmp, String name, String title) {
        // Call the saveDrawingToUserSpecificPath method first
        saveDrawingToUserSpecificPath(bmp, name, title);

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in to save your drawing.", Toast.LENGTH_SHORT).show();
            return;
        }

        String drawingId = UUID.randomUUID().toString();
        StorageReference storageRef = firebaseStorage.getReference().child("drawings").child(drawingId + ".png");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String downloadUrl = uri.toString();

            Map<String, Object> drawing = new HashMap<>();
            drawing.put("drawingId", drawingId);
            drawing.put("imageUrl", downloadUrl);
            drawing.put("timestamp", System.currentTimeMillis());
            drawing.put("name", name);
            drawing.put("title", title);

            firebaseFirestore.collection("drawings").document(drawingId).set(drawing)
                    .addOnSuccessListener(aVoid -> Toast.makeText(DrawingMainActivity.this, "Drawing published to collection.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(DrawingMainActivity.this, "Failed to publish drawing to collection.", Toast.LENGTH_SHORT).show());
        })).addOnFailureListener(e -> Toast.makeText(DrawingMainActivity.this, "Failed to upload drawing to Firebase.", Toast.LENGTH_SHORT).show());
    }

    private void saveDrawingToUserSpecificPath(Bitmap bmp, String name, String title) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in to save your drawing.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String drawingId = UUID.randomUUID().toString();
        StorageReference storageRef = firebaseStorage.getReference().child("users").child(userId).child("drawings").child(drawingId + ".png");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String downloadUrl = uri.toString();

            Map<String, Object> drawing = new HashMap<>();
            drawing.put("drawingId", drawingId);
            drawing.put("imageUrl", downloadUrl);
            drawing.put("timestamp", System.currentTimeMillis());
            drawing.put("name", name);
            drawing.put("title", title);

            firebaseFirestore.collection("users").document(userId).collection("drawings").document(drawingId).set(drawing)
                    .addOnSuccessListener(aVoid -> Toast.makeText(DrawingMainActivity.this, "Drawing saved successfully.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(DrawingMainActivity.this, "Failed to save drawing.", Toast.LENGTH_SHORT).show());
        })).addOnFailureListener(e -> Toast.makeText(DrawingMainActivity.this, "Failed to upload drawing to Firebase.", Toast.LENGTH_SHORT).show());
    }

    private void setColorButtons() {
        Button blackButton = (Button) colorPalette.getChildAt(0);
        Button redButton = (Button) colorPalette.getChildAt(1);
        Button greenButton = (Button) colorPalette.getChildAt(2);
        Button blueButton = (Button) colorPalette.getChildAt(3);
        Button yellowButton = (Button) colorPalette.getChildAt(4);
        Button purpleButton = (Button) colorPalette.getChildAt(5);
        Button orangeButton = (Button) colorPalette.getChildAt(6);
        Button pinkButton = (Button) colorPalette.getChildAt(7);
        Button brownButton = (Button) colorPalette.getChildAt(8);
        Button grayButton = (Button) colorPalette.getChildAt(9);

        blackButton.setOnClickListener(v -> setColor(Color.BLACK));
        blackButton.setBackgroundColor(Color.BLACK);
        redButton.setOnClickListener(v -> setColor(Color.RED));
        redButton.setBackgroundColor(Color.RED);
        greenButton.setOnClickListener(v -> setColor(Color.GREEN));
        greenButton.setBackgroundColor(Color.GREEN);
        blueButton.setOnClickListener(v -> setColor(Color.BLUE));
        blueButton.setBackgroundColor(Color.BLUE);
        yellowButton.setOnClickListener(v -> setColor(Color.YELLOW));
        yellowButton.setBackgroundColor(Color.YELLOW);
        purpleButton.setOnClickListener(v -> setColor(Color.parseColor("#800080")));
        purpleButton.setBackgroundColor(Color.parseColor("#800080"));
        orangeButton.setOnClickListener(v -> setColor(Color.parseColor("#FFA500")));
        orangeButton.setBackgroundColor(Color.parseColor("#FFA500"));
        pinkButton.setOnClickListener(v -> setColor(Color.parseColor("#FFC0CB")));
        pinkButton.setBackgroundColor(Color.parseColor("#FFC0CB"));
        brownButton.setOnClickListener(v -> setColor(Color.parseColor("#8B4513")));
        brownButton.setBackgroundColor(Color.parseColor("#8B4513"));
        grayButton.setOnClickListener(v -> setColor(Color.parseColor("#808080")));
        grayButton.setBackgroundColor(Color.parseColor("#808080"));
    }

    private void setColor(int color) {
        currentColor = color;
        paint.setColor(color);
    }

    private void toggleRangeSliderVisibility() {
        if (rangeSlider.getVisibility() == View.VISIBLE) {
            rangeSlider.setVisibility(View.GONE);
        } else {
            rangeSlider.setVisibility(View.VISIBLE);
        }
    }

    private void toggleEraser() {
        isEraserOn = !isEraserOn;
        paint.setEraserMode(isEraserOn);
        if (isEraserOn) {
            eraser.setColorFilter(Color.RED);  // Change icon color to red to indicate eraser mode
        } else {
            eraser.clearColorFilter();  // Clear color filter to reset icon color
        }
    }

    private void resetDrawing() {
        paint.clear();
    }

    private void toggleFillMode() {
        isFillModeOn = !isFillModeOn;
        fill.setColorFilter(isFillModeOn ? Color.GREEN : Color.BLACK);  // Change button color to indicate fill mode status
        paint.setFillMode(isFillModeOn);
    }

    private void toggleSelectionMode() {
        boolean isSelectionModeOn = !paint.isSelectionMode();
        paint.setSelectionMode(isSelectionModeOn);
        selectionMode.setColorFilter(isSelectionModeOn ? Color.GREEN : Color.BLACK);
        deleteSelection.setVisibility(isSelectionModeOn ? View.VISIBLE : View.GONE);
    }

    private void showDeleteSelectionConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete the selected area?")
                .setPositiveButton("Delete", (dialog, which) -> paint.deleteSelection())
                .setNegativeButton("Cancel", null)
                .show();
    }
}