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
    private ImageButton save, color, stroke, undo, eraser, reset;
    private RangeSlider rangeSlider;

    // Custom color picker dialog views
    private View colorPickerDialog;
    private LinearLayout colorPalette;
    private ImageButton closeColorPickerButton;

    private int currentColor;
    private boolean isEraserOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing_main);

        paint = findViewById(R.id.draw_view);
        rangeSlider = findViewById(R.id.rangebar);
        undo = findViewById(R.id.btn_undo);
        save = findViewById(R.id.btn_save);
        color = findViewById(R.id.btn_color);
        stroke = findViewById(R.id.btn_stroke);
        eraser = findViewById(R.id.btn_eraser);
        reset = findViewById(R.id.btn_reset);

        // Initialize custom color picker dialog views
        colorPickerDialog = findViewById(R.id.color_picker_dialog);
        colorPalette = findViewById(R.id.color_palette);
        closeColorPickerButton = findViewById(R.id.btn_close_color_picker);

        // Set colors for the color buttons
        setColorButtons();

        color.setOnClickListener(view -> showCustomColorPickerDialog());
        closeColorPickerButton.setOnClickListener(view -> colorPickerDialog.setVisibility(View.GONE));

        undo.setOnClickListener(view -> paint.undo());

        save.setOnClickListener(view -> saveDrawing());

        stroke.setOnClickListener(view -> toggleRangeSliderVisibility());

        eraser.setOnClickListener(view -> toggleEraser());

        reset.setOnClickListener(view -> resetDrawing());

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

        blackButton.setBackgroundColor(Color.BLACK);
        blackButton.setOnClickListener(view -> selectColor(Color.BLACK));

        redButton.setBackgroundColor(Color.RED);
        redButton.setOnClickListener(view -> selectColor(Color.RED));

        greenButton.setBackgroundColor(Color.GREEN);
        greenButton.setOnClickListener(view -> selectColor(Color.GREEN));

        blueButton.setBackgroundColor(Color.BLUE);
        blueButton.setOnClickListener(view -> selectColor(Color.BLUE));
    }

    private void selectColor(int color) {
        currentColor = color;
        if (!isEraserOn) {
            paint.setColor(currentColor);
        }
        colorPickerDialog.setVisibility(View.GONE);
    }

    private void toggleEraser() {
        if (isEraserOn) {
            paint.setColor(currentColor);
            isEraserOn = false;
            eraser.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            paint.setColor(Color.WHITE);
            isEraserOn = true;
            eraser.setImageResource(android.R.drawable.ic_menu_revert);
        }
    }

    private void toggleRangeSliderVisibility() {
        if (rangeSlider.getVisibility() == View.GONE) {
            rangeSlider.setVisibility(View.VISIBLE);
        } else {
            rangeSlider.setVisibility(View.GONE);
        }
    }

    private void resetDrawing() {
        paint.clear();
    }
}