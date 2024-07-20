package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class Dictionary extends AppCompatActivity implements BookmarkChangeListener {

    private static final int BOOKMARK_REQUEST_CODE = 1;
    private EditText searchBar;
    private Button searchButton;
    private ProgressBar progressBar;
    private TextView wordText, phoneticText;
    private ImageView speakerIcon, bookmarkCollectionIcon, backArrowIcon, bookmarkIcon, microphoneIcon;
    private LinearLayout definitionsLayout;
    private TextToSpeech textToSpeech;
    private BookmarkManager bookmarkManager;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        searchBar = findViewById(R.id.search_bar);
        searchButton = findViewById(R.id.search_button);
        progressBar = findViewById(R.id.progress_bar);
        wordText = findViewById(R.id.word_text);
        phoneticText = findViewById(R.id.phonetic_text);
        speakerIcon = findViewById(R.id.speaker_icon);
        bookmarkCollectionIcon = findViewById(R.id.bookmark_collection_icon);
        backArrowIcon = findViewById(R.id.back_arrow_icon);
        bookmarkIcon = findViewById(R.id.bookmark_icon);
        microphoneIcon = findViewById(R.id.microphone_icon);
        definitionsLayout = findViewById(R.id.definitions_layout);

        bookmarkManager = BookmarkManager.getInstance(this);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(Dictionary.this, "Language not supported", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Dictionary.this, "Text-to-Speech initialization failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = searchBar.getText().toString().trim();
                if (!word.isEmpty()) {
                    fetchDefinition(word);
                } else {
                    Toast.makeText(Dictionary.this, "Please enter a word", Toast.LENGTH_SHORT).show();
                }
            }
        });

        speakerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = wordText.getText().toString();
                if (!word.isEmpty()) {
                    textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });

        bookmarkCollectionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dictionary.this, BookmarkCollectionActivity.class);
                startActivityForResult(intent, BOOKMARK_REQUEST_CODE);
            }
        });

        backArrowIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        bookmarkIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = wordText.getText().toString();
                if (!word.isEmpty()) {
                    if (bookmarkManager.isBookmarked(word)) {
                        bookmarkManager.removeBookmark(word);
                        bookmarkIcon.setImageResource(R.drawable.baseline_bookmark_border_24);
                        onBookmarkChanged(word, false);
                    } else {
                        bookmarkManager.addBookmark(word);
                        bookmarkIcon.setImageResource(R.drawable.baseline_bookmark_24);
                        onBookmarkChanged(word, true);
                    }
                }
            }
        });

        // Initialize speech recognizer
        initializeSpeechRecognizer();

        microphoneIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceRecognition();
            }
        });

        // Handle selected word from KeywordSearchActivity
        String selectedWord = getIntent().getStringExtra("selected_word");
        if (selectedWord != null) {
            searchBar.setText(selectedWord);
            fetchDefinition(selectedWord);
        }
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {}

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    searchBar.setText(spokenText);
                    fetchDefinition(spokenText);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startVoiceRecognition() {
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    private void fetchDefinition(String word) {
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                parseJson(response);
                updateBookmarkIcon(word);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Dictionary.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(stringRequest);
    }

    private void parseJson(String jsonStr) {
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            definitionsLayout.removeAllViews();

            String word = jsonObject.getString("word");
            String phonetic = jsonObject.optString("phonetic");

            wordText.setText(word);
            phoneticText.setText(phonetic);

            JSONArray meaningsArray = jsonObject.getJSONArray("meanings");
            for (int i = 0; i < meaningsArray.length(); i++) {
                JSONObject meaningObject = meaningsArray.getJSONObject(i);
                String partOfSpeech = meaningObject.getString("partOfSpeech");

                LinearLayout partOfSpeechLayout = new LinearLayout(this);
                partOfSpeechLayout.setOrientation(LinearLayout.VERTICAL);
                partOfSpeechLayout.setPadding(0, 16, 0, 16);

                TextView partOfSpeechTextView = new TextView(this);
                partOfSpeechTextView.setText(partOfSpeech);
                partOfSpeechTextView.setTextSize(20);
                partOfSpeechTextView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                partOfSpeechLayout.addView(partOfSpeechTextView);

                JSONArray definitionsArray = meaningObject.getJSONArray("definitions");
                for (int j = 0; j < definitionsArray.length(); j++) {
                    JSONObject definitionObject = definitionsArray.getJSONObject(j);
                    String definition = definitionObject.getString("definition");

                    TextView definitionTextView = new TextView(this);
                    definitionTextView.setText((j + 1) + ". " + definition);
                    definitionTextView.setTextSize(16);
                    partOfSpeechLayout.addView(definitionTextView);
                }

                definitionsLayout.addView(partOfSpeechLayout);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBookmarkIcon(String word) {
        if (bookmarkManager.isBookmarked(word)) {
            bookmarkIcon.setImageResource(R.drawable.baseline_bookmark_24);
        } else {
            bookmarkIcon.setImageResource(R.drawable.baseline_bookmark_border_24);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BOOKMARK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String word = data.getStringExtra("word");
            boolean isBookmarked = data.getBooleanExtra("isBookmarked", false);
            if (word != null) {
                updateBookmarkIcon(word);
            }
        }
    }

    @Override
    public void onBookmarkChanged(String word, boolean isBookmarked) {
        if (isBookmarked) {
            bookmarkIcon.setImageResource(R.drawable.baseline_bookmark_24);
        } else {
            bookmarkIcon.setImageResource(R.drawable.baseline_bookmark_border_24);
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }
}
