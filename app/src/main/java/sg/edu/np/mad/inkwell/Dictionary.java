package sg.edu.np.mad.inkwell;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class Dictionary extends AppCompatActivity implements BookmarkChangeListener {

    private static final int BOOKMARK_REQUEST_CODE = 1;
    private static final int VOICE_SEARCH_REQUEST_CODE = 2;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_CODE = 201;

    private static final String DICTIONARY_API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";

    private EditText searchBar;
    private Button searchButton;
    private ProgressBar progressBar;
    private TextView wordText, phoneticText;
    private ImageView bookmarkCollectionIcon, backArrowIcon, bookmarkIcon, pronunciationIcon, speakerIcon, microphoneIcon, downloadIcon;
    private LinearLayout definitionsLayout;
    private TextToSpeech textToSpeech;
    private BookmarkManager bookmarkManager;
    private SpeechRecognizer speechRecognizer;
    private boolean permissionToRecordAccepted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        initializeViews();
        initializeTextToSpeech();
        initializeSpeechRecognizer();

        searchButton.setOnClickListener(v -> {
            String word = searchBar.getText().toString().trim();
            if (!word.isEmpty()) {
                fetchDefinition(word);
            } else {
                Toast.makeText(Dictionary.this, "Please enter a word", Toast.LENGTH_SHORT).show();
            }
        });

        bookmarkCollectionIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Dictionary.this, BookmarkCollectionActivity.class);
            startActivityForResult(intent, BOOKMARK_REQUEST_CODE);
        });

        backArrowIcon.setOnClickListener(v -> onBackPressed());

        bookmarkIcon.setOnClickListener(v -> {
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
        });

        pronunciationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Dictionary.this, PronunciationActivity.class);
            intent.putExtra("word", wordText.getText().toString()); // Pass the searched word to the PronunciationActivity
            startActivity(intent);
        });

        speakerIcon.setOnClickListener(v -> {
            String word = wordText.getText().toString();
            if (!word.isEmpty()) {
                textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                Toast.makeText(Dictionary.this, "No word to pronounce", Toast.LENGTH_SHORT).show();
            }
        });

        microphoneIcon.setOnClickListener(v -> startSpeechRecognition());

        downloadIcon.setOnClickListener(v -> createPdf(wordText.getText().toString(), phoneticText.getText().toString(), getDefinitions()));

        checkAudioPermission();

        // Handle selected word from KeywordSearchActivity
        String selectedWord = getIntent().getStringExtra("selected_word");
        if (selectedWord != null) {
            searchBar.setText(selectedWord);
            fetchDefinition(selectedWord);
        }
    }

    private void initializeViews() {
        searchBar = findViewById(R.id.search_bar);
        searchButton = findViewById(R.id.search_button);
        progressBar = findViewById(R.id.progress_bar);
        wordText = findViewById(R.id.word_text);
        phoneticText = findViewById(R.id.phonetic_text);
        bookmarkCollectionIcon = findViewById(R.id.bookmark_collection_icon);
        backArrowIcon = findViewById(R.id.back_arrow_icon);
        bookmarkIcon = findViewById(R.id.bookmark_icon);
        pronunciationIcon = findViewById(R.id.pronunciation_icon);
        speakerIcon = findViewById(R.id.speaker_icon);
        microphoneIcon = findViewById(R.id.microphone_icon);
        downloadIcon = findViewById(R.id.download_icon);
        definitionsLayout = findViewById(R.id.definitions_layout);
        bookmarkManager = BookmarkManager.getInstance(this);
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(Dictionary.this, "Language not supported", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Dictionary.this, "Text-to-Speech initialization failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d("SpeechRecognition", "Ready for speech");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d("SpeechRecognition", "Beginning of speech");
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                Log.d("SpeechRecognition", "RMS changed: " + rmsdB);
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                Log.d("SpeechRecognition", "Buffer received");
            }

            @Override
            public void onEndOfSpeech() {
                Log.d("SpeechRecognition", "End of speech");
            }

            @Override
            public void onError(int error) {
                Log.d("SpeechRecognition", "Error: " + error);
                Toast.makeText(Dictionary.this, "Speech recognition error: " + error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    searchBar.setText(matches.get(0));
                    fetchDefinition(matches.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                Log.d("SpeechRecognition", "Partial results");
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                Log.d("SpeechRecognition", "Event: " + eventType);
            }
        });
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            permissionToRecordAccepted = true; // Permission already granted
        }
    }

    private void startSpeechRecognition() {
        if (permissionToRecordAccepted) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the word");

            speechRecognizer.startListening(intent);
        } else {
            Toast.makeText(this, "Permission to record not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchDefinition(String word) {
        progressBar.setVisibility(View.VISIBLE);
        String url = DICTIONARY_API_URL + word;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            progressBar.setVisibility(View.GONE);
            parseJson(response);
            updateBookmarkIcon(word);
        }, error -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(Dictionary.this, "Error fetching data", Toast.LENGTH_SHORT).show();
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

    private void createPdf(String word, String phonetic, String definitions) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION_CODE);
        } else {
            generatePdf(word, phonetic, definitions);
        }
    }

    private void generatePdf(String word, String phonetic, String definitions) {
        PdfDocument document = new PrintedPdfDocument(this, new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(new PrintAttributes.Resolution("zooey", PRINT_SERVICE, 300, 300))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build());

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(14);

        int x = 40, y = 50;
        int pageWidth = pageInfo.getPageWidth();
        int textWidth = pageWidth - 2 * x;

        // Title
        paint.setTextSize(18);
        paint.setFakeBoldText(true);
        y = drawText(canvas, "Word Information", x, y, paint, textWidth) + 20;

        // Word
        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        y = drawText(canvas, "Word: " + word, x, y, paint, textWidth) + 10;

        // Phonetic
        y = drawText(canvas, "Phonetic: " + phonetic, x, y, paint, textWidth) + 10;

        // Definitions
        paint.setFakeBoldText(true);
        y = drawText(canvas, "Definitions:", x, y, paint, textWidth) + 10;
        paint.setFakeBoldText(false);
        y = drawText(canvas, definitions, x, y, paint, textWidth);

        document.finishPage(page);

        String directoryPath = Environment.getExternalStorageDirectory().getPath() + "/Download/";
        File file = new File(directoryPath, word + ".pdf");

        try {
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF saved in Download folder", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving PDF", Toast.LENGTH_SHORT).show();
        }

        document.close();
    }

    private int drawText(Canvas canvas, String text, int x, int y, Paint paint, int maxWidth) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (paint.measureText(line + word) <= maxWidth) {
                line.append(word).append(" ");
            } else {
                canvas.drawText(line.toString(), x, y, paint);
                y += paint.descent() - paint.ascent();
                line = new StringBuilder(word).append(" ");
            }
        }
        if (!line.toString().isEmpty()) {
            canvas.drawText(line.toString(), x, y, paint);
            y += paint.descent() - paint.ascent();
        }
        return y;
    }

    private String getDefinitions() {
        StringBuilder definitions = new StringBuilder();
        for (int i = 0; i < definitionsLayout.getChildCount(); i++) {
            View view = definitionsLayout.getChildAt(i);
            if (view instanceof LinearLayout) {
                LinearLayout partOfSpeechLayout = (LinearLayout) view;
                for (int j = 0; j < partOfSpeechLayout.getChildCount(); j++) {
                    View child = partOfSpeechLayout.getChildAt(j);
                    if (child instanceof TextView) {
                        TextView textView = (TextView) child;
                        definitions.append(textView.getText().toString()).append("\n");
                    }
                }
            }
        }
        return definitions.toString();
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
        } else if (requestCode == VOICE_SEARCH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) {
                String spokenText = matches.get(0);
                searchBar.setText(spokenText);
                fetchDefinition(spokenText);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (!permissionToRecordAccepted) {
                Toast.makeText(this, "Permission to record not granted", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String word = wordText.getText().toString();
                String phonetic = phoneticText.getText().toString();
                String definitions = getDefinitions();
                generatePdf(word, phonetic, definitions);
            } else {
                Toast.makeText(this, "Permission to write to external storage not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
