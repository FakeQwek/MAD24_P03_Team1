package sg.edu.np.mad.inkwell;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.view.ViewTreeObserver;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.RangeSlider;

import java.io.OutputStream;

public class DrawingMainActivity extends AppCompatActivity {

    private DrawView paint;
    private ImageButton save, color, stroke, undo, redo, eraser, reset, fill;
    private RangeSlider rangeSlider;

    // Custom color picker dialog views
    private View colorPickerDialog;
    private LinearLayout colorPalette;
    private ImageButton closeColorPickerButton;

    private int currentColor;
    private boolean isEraserOn = false;
    private boolean isFillModeOn = false;

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
    }

    private void showCustomColorPickerDialog() {
        colorPickerDialog.setVisibility(View.VISIBLE);
    }

    private void saveDrawing() {
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
}