package sg.edu.np.mad.inkwell;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Locale;

public class PronunciationActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private TextView wordTextView, spokenTextView, recordCountTextView;
    private ImageView microphoneIcon, backArrowIcon;
    private Button startRecordButton, stopRecordButton, viewAnalyticsButton;
    private String wordToPronounce;
    private String spokenText;
    private ArrayList<Integer> accuracies;
    private int recordCount = 0;
    private boolean permissionToRecordAccepted = false;
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    private boolean hasResults = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pronunciation);

        wordTextView = findViewById(R.id.word_text_view);
        spokenTextView = findViewById(R.id.spoken_text_view);
        recordCountTextView = findViewById(R.id.record_count_text_view);
        microphoneIcon = findViewById(R.id.microphone_icon);
        backArrowIcon = findViewById(R.id.back_arrow_icon);
        startRecordButton = findViewById(R.id.start_record_button);
        stopRecordButton = findViewById(R.id.stop_record_button);
        viewAnalyticsButton = findViewById(R.id.view_analytics_button);

        accuracies = new ArrayList<>();

        Intent intent = getIntent();
        wordToPronounce = intent.getStringExtra("word");

        wordTextView.setText(wordToPronounce);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);

        startRecordButton.setOnClickListener(v -> startSpeechRecognition());
        stopRecordButton.setOnClickListener(v -> stopRecording());
        viewAnalyticsButton.setOnClickListener(v -> viewAnalytics());

        stopRecordButton.setEnabled(false);
        viewAnalyticsButton.setEnabled(false);

        backArrowIcon.setOnClickListener(v -> onBackPressed());

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // Called when the endpointer is ready for the user to start speaking.
            }

            @Override
            public void onBeginningOfSpeech() {
                // Called when the user starts speaking.
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Called periodically to update the user interface with the sound level.
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Called when more sound has been received.
            }

            @Override
            public void onEndOfSpeech() {
                // Called after the user stops speaking.
            }

            @Override
            public void onError(int error) {
                if (isListening) {
                    Toast.makeText(PronunciationActivity.this, "Speech recognition error: " + error, Toast.LENGTH_SHORT).show();
                    stopRecording();  // Stop recording on error
                }
            }

            @Override
            public void onResults(Bundle results) {
                if (isListening) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        spokenText = matches.get(0);
                        spokenTextView.setText(spokenText);
                        hasResults = true;
                    } else {
                        spokenTextView.setText("");
                        hasResults = false;
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Called with partial recognition results.
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Called when an event occurs.
            }
        });
    }

    private void startSpeechRecognition() {
        isListening = true;
        hasResults = false;
        spokenTextView.setText("");  // Clear previous results
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Pronounce the word");

        speechRecognizer.startListening(intent);

        startRecordButton.setEnabled(false);
        stopRecordButton.setEnabled(true);
    }

    private void stopRecording() {
        if (isListening) {
            isListening = false;
            speechRecognizer.stopListening();
            stopRecordButton.setEnabled(false);
            startRecordButton.setEnabled(true);

            if (hasResults) {
                int accuracy = calculateAccuracy(wordToPronounce, spokenText);
                accuracies.add(accuracy);
            } else {
                accuracies.add(0); // Add 0 accuracy if no speech detected
            }
            recordCount++;
            recordCountTextView.setText("Record Sound Count: " + recordCount + "/10");
            hasResults = false; // Reset for next round

            if (recordCount == 10) {
                viewAnalyticsButton.setEnabled(true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    private int calculateAccuracy(String target, String spoken) {
        target = target.toLowerCase().trim();
        spoken = spoken.toLowerCase().trim();
        int distance = levenshteinDistance(target, spoken);
        int maxLength = Math.max(target.length(), spoken.length());
        return (int) (((double) (maxLength - distance) / maxLength) * 100);
    }

    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + costOfSubstitution(a.charAt(i - 1), b.charAt(j - 1)),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }

        return dp[a.length()][b.length()];
    }

    private int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    private void viewAnalytics() {
        Intent intent = new Intent(PronunciationActivity.this, AnalyticsActivity.class);
        intent.putIntegerArrayListExtra("accuracies", accuracies);
        intent.putExtra("word", wordToPronounce);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) {
            Toast.makeText(this, "Permission to record not granted", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
