package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class PracticeWordActivity extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private String wordToPractice;
    private TextView wordTextView;
    private TextView averageAccuracyTextView;
    private TextView currentAccuracyTextView;
    private ImageView micIcon;
    private TextView feedbackTextView;
    private Button tryAgainButton;
    private Button completeButton;
    private float averageAccuracy;
    private int currentAccuracy;
    private ImageView backIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_word);

        wordTextView = findViewById(R.id.word_text_view);
        averageAccuracyTextView = findViewById(R.id.average_accuracy_text_view);
        currentAccuracyTextView = findViewById(R.id.current_accuracy_text_view);
        micIcon = findViewById(R.id.mic_icon);
        feedbackTextView = findViewById(R.id.feedback_text_view);
        tryAgainButton = findViewById(R.id.try_again_button);
        completeButton = findViewById(R.id.complete_button);
        backIcon = findViewById(R.id.back_icon);

        wordToPractice = getIntent().getStringExtra("word");
        averageAccuracy = getIntent().getFloatExtra("average_accuracy", 0);

        wordTextView.setText(wordToPractice);
        averageAccuracyTextView.setText(String.format(Locale.getDefault(), "%.1f%%", averageAccuracy));

        micIcon.setOnClickListener(v -> promptSpeechInput());

        tryAgainButton.setOnClickListener(v -> resetPractice());

        completeButton.setOnClickListener(v -> {
            Intent keywordSearchIntent = new Intent(PracticeWordActivity.this, KeywordSearchActivity.class);
            startActivity(keywordSearchIntent);
        });

        backIcon.setOnClickListener(v -> onBackPressed());

        updateFeedback(averageAccuracy);
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the word");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Speech input is not supported on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                calculateAccuracy(spokenText);
            }
        }
    }

    private void calculateAccuracy(String spokenText) {
        int distance = levenshteinDistance(wordToPractice.toLowerCase(), spokenText.toLowerCase());
        int maxLength = Math.max(wordToPractice.length(), spokenText.length());
        currentAccuracy = 100 - (int) ((distance / (float) maxLength) * 100);

        currentAccuracyTextView.setText(String.format(Locale.getDefault(), "%d%% / 100%%", currentAccuracy));
        updateFeedback(currentAccuracy);

        if (currentAccuracy >= 80) {
            completeButton.setVisibility(View.VISIBLE);
        } else {
            completeButton.setVisibility(View.GONE);
        }
    }

    private void resetPractice() {
        currentAccuracy = 0;
        currentAccuracyTextView.setText(String.format(Locale.getDefault(), "%d%% / 100%%", currentAccuracy));
        feedbackTextView.setText("");
        completeButton.setVisibility(View.GONE);
    }

    private void updateFeedback(float accuracy) {
        String feedback;
        if (accuracy >= 80) {
            feedback = "Excellent pronunciation!";
        } else if (accuracy >= 60) {
            feedback = "Good pronunciation, but there's room for improvement.";
        } else {
            feedback = "Needs improvement. Keep practicing!";
        }
        feedbackTextView.setText("Feedback: " + feedback);
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                            dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1));
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }
}