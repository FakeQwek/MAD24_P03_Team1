package sg.edu.np.mad.inkwell;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class AnalyticsActivity extends AppCompatActivity {

    private BarChart barChart;
    private PieChart pieChart;
    private ArrayList<Integer> accuracies;
    private String wordPracticed;
    private TextView averageAccuracyTextView;
    private TextView feedbackTextView;
    private static final int STORAGE_PERMISSION_CODE = 100;
    private float averageAccuracy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        TextView practicedWordTextView = findViewById(R.id.practiced_word);
        barChart = findViewById(R.id.bar_chart);
        pieChart = findViewById(R.id.pie_chart);
        ImageView downloadIcon = findViewById(R.id.download_icon);
        ImageView backArrowIcon = findViewById(R.id.back_arrow_icon);
        averageAccuracyTextView = findViewById(R.id.average_accuracy);
        feedbackTextView = findViewById(R.id.feedback);
        Button practiceButton = findViewById(R.id.practice_button);

        accuracies = getIntent().getIntegerArrayListExtra("accuracies");
        wordPracticed = getIntent().getStringExtra("word");

        // Log the received data
        Log.d("AnalyticsActivity", "Word Practiced: " + wordPracticed);
        Log.d("AnalyticsActivity", "Accuracies: " + accuracies);

        // Set the practiced word text
        practicedWordTextView.setText(wordPracticed);

        setupChart();
        setupPieChart();
        calculateAverageAccuracy();

        downloadIcon.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            } else {
                saveChartAsPDF();
            }
        });

        practiceButton.setOnClickListener(v -> {
            Intent practiceIntent = new Intent(AnalyticsActivity.this, PracticeWordActivity.class);
            practiceIntent.putExtra("word", wordPracticed);
            practiceIntent.putExtra("average_accuracy", averageAccuracy);
            practiceIntent.putIntegerArrayListExtra("accuracies", accuracies);
            startActivity(practiceIntent);
        });

        backArrowIcon.setOnClickListener(v -> onBackPressed());
    }

    private void setupChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < accuracies.size(); i++) {
            entries.add(new BarEntry(i + 1, accuracies.get(i))); // Ensure we start from 1
        }

        // If there are fewer than 10 entries, add missing entries with zero accuracy
        for (int i = accuracies.size(); i < 10; i++) {
            entries.add(new BarEntry(i + 1, 0));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Pronunciation Accuracy");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.TRANSPARENT); // Remove red small text
        dataSet.setValueTextSize(16f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f); // Set custom bar width to ensure proper spacing
        barChart.setData(barData);

        // Customizing the X-Axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(10, true);
        xAxis.setAxisMinimum(1f); // Adjust to show all bars properly
        xAxis.setAxisMaximum(10f); // Adjust to show all bars properly
        xAxis.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int intValue = (int) value;
                return intValue >= 1 && intValue <= 10 ? String.valueOf(intValue) : "";
            }
        });

        // Customizing the Y-Axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setLabelCount(11, true);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "%";
            }
        });

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Adding Description
        Description description = new Description();
        description.setText(wordPracticed);
        description.setTextSize(16f);
        description.setTextColor(Color.RED);
        barChart.setDescription(description);

        barChart.setFitBars(true);
        barChart.invalidate(); // refresh
    }








    private void setupPieChart() {
        float averageAccuracy = calculateAverageAccuracy();
        float incorrectPercentage = 100 - averageAccuracy;

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(averageAccuracy, "Correct"));
        entries.add(new PieEntry(incorrectPercentage, "Incorrect"));

        PieDataSet dataSet = new PieDataSet(entries, "Accuracy Distribution");
        dataSet.setColors(Color.GREEN, Color.RED);
        dataSet.setValueTextSize(16f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Removing Description
        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);

        pieChart.invalidate(); // refresh
    }

    private float calculateAverageAccuracy() {
        int totalAccuracy = 0;
        for (int accuracy : accuracies) {
            totalAccuracy += accuracy;
        }
        averageAccuracy = (float) totalAccuracy / accuracies.size();
        averageAccuracyTextView.setText(String.format(Locale.getDefault(), "Average Accuracy: %.1f%%", averageAccuracy));

        String feedback;
        if (averageAccuracy >= 80) {
            feedback = "Excellent pronunciation!";
        } else if (averageAccuracy >= 60) {
            feedback = "Good pronunciation, but there's room for improvement.";
        } else {
            feedback = "Needs improvement. Keep practicing!";
        }
        feedbackTextView.setText("Feedback: " + feedback);

        return averageAccuracy;
    }

    private void saveChartAsPDF() {
        Bitmap barChartBitmap = getChartBitmap(barChart);
        Bitmap pieChartBitmap = getChartBitmap(pieChart);

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(barChartBitmap.getWidth(), barChartBitmap.getHeight() + pieChartBitmap.getHeight() + 200, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        canvas.drawBitmap(barChartBitmap, 0, 100, new Paint());
        canvas.drawBitmap(pieChartBitmap, 0, barChartBitmap.getHeight() + 150, new Paint());

        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(24f);
        paint.setColor(Color.BLACK);
        canvas.drawText("Analytics Report for: " + wordPracticed, canvas.getWidth() / 2, 50, paint);

        document.finishPage(page);

        String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/";
        String filePath = directoryPath + "Analytics_Report_" + System.currentTimeMillis() + ".pdf";
        File file = new File(filePath);
        try {
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        document.close();
    }

    private Bitmap getChartBitmap(View chart) {
        chart.setDrawingCacheEnabled(true);
        chart.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(chart.getDrawingCache());
        chart.setDrawingCacheEnabled(false);
        return bitmap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveChartAsPDF();
            } else {
                Toast.makeText(this, "Permission denied to write to storage", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
