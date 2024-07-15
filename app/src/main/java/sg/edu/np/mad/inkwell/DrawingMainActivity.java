package sg.edu.np.mad.inkwell;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.RangeSlider;

import java.io.OutputStream;

public class DrawingMainActivity extends AppCompatActivity {

    private DrawView paint;
    private ImageButton save, color, stroke, undo;
    private RangeSlider rangeSlider;

    // Custom color picker dialog views
    private View colorPickerDialog;
    private SeekBar redSeekBar, greenSeekBar, blueSeekBar;
    private TextView colorPreview;

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

        // Initialize custom color picker dialog views
        colorPickerDialog = (LinearLayout) findViewById(R.id.color_picker_dialog);
        redSeekBar = colorPickerDialog.findViewById(R.id.color_picker_red);
        greenSeekBar = colorPickerDialog.findViewById(R.id.color_picker_green);
        blueSeekBar = colorPickerDialog.findViewById(R.id.color_picker_blue);
        colorPreview = colorPickerDialog.findViewById(R.id.color_preview);

        redSeekBar.setOnSeekBarChangeListener(colorChangeListener);
        greenSeekBar.setOnSeekBarChangeListener(colorChangeListener);
        blueSeekBar.setOnSeekBarChangeListener(colorChangeListener);

        color.setOnClickListener(view -> showCustomColorPickerDialog());

        undo.setOnClickListener(view -> paint.undo());

        save.setOnClickListener(view -> saveDrawing());

        stroke.setOnClickListener(view -> {
            if (rangeSlider.getVisibility() == View.VISIBLE) {
                rangeSlider.setVisibility(View.GONE);
            } else {
                rangeSlider.setVisibility(View.VISIBLE);
            }
        });

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

        // Initialize the color picker dialog with default color
        updateColorPreview();
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

    private final SeekBar.OnSeekBarChangeListener colorChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updateColorPreview();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    private void updateColorPreview() {
        int red = redSeekBar.getProgress();
        int green = greenSeekBar.getProgress();
        int blue = blueSeekBar.getProgress();
        int color = getColorFromRGB(red, green, blue);
        colorPreview.setBackgroundColor(color);
        paint.setColor(color);
    }

    private int getColorFromRGB(int red, int green, int blue) {
        return 0xff000000 | (red << 16) | (green << 8) | blue;
    }
}