package sg.edu.np.mad.inkwell;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.Locale;

public class Dictionary extends AppCompatActivity {

    private EditText searchBar;
    private Button searchButton;
    private ProgressBar progressBar;
    private TextView wordText, phoneticText;
    private ImageView speakerIcon;
    private LinearLayout definitionsLayout;
    private TextToSpeech textToSpeech;

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
        definitionsLayout = findViewById(R.id.definitions_layout);

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

        // Handle selected word from KeywordSearchActivity
        String selectedWord = getIntent().getStringExtra("selected_word");
        if (selectedWord != null) {
            searchBar.setText(selectedWord);
            fetchDefinition(selectedWord);
        }
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

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
