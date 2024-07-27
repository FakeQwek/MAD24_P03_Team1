package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.navigation.NavigationView;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LanguageTranslator extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Spinner fromSpinner, toSpinner;
    private TextInputEditText sourceText;
    private ImageView micTV, clearText;
    private MaterialButton translateBtn;
    private TextView translateTV;
    private RecyclerView recyclerView;

    private ActivityResultLauncher<Intent> speechRecognitionLauncher;

    String[] fromLanguage = {"From", "English", "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Chinese",
            "Danish", "Dutch", "French", "German", "Greek", "Gujarati", "Hebrew", "Hungarian", "Indonesian", "Irish", "Italian", "Japanese",
            "Kannada", "Korean", "Lithuanian", "Malay", "Marathi", "Nepali", "Norwegian", "Polish", "Portuguese", "Punjabi", "Romanian",
            "Russian", "Spanish", "Swedish", "Tamil", "Telugu", "Thai", "Turkish", "Ukrainian", "Urdu", "Vietnamese"};

    String[] toLanguage = {"To", "English", "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Chinese",
            "Danish", "Dutch", "French", "German", "Greek", "Gujarati", "Hebrew", "Hungarian", "Indonesian", "Irish", "Italian", "Japanese",
            "Kannada", "Korean", "Lithuanian", "Malay", "Marathi", "Nepali", "Norwegian", "Polish", "Portuguese", "Punjabi", "Romanian",
            "Russian", "Spanish", "Swedish", "Tamil", "Telugu", "Thai", "Turkish", "Ukrainian", "Urdu", "Vietnamese"};

    String fromLanguageCode, toLanguageCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_translator);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up navigation drawer
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        sourceText = findViewById(R.id.idEditSource);
        micTV = findViewById(R.id.idIvMic);
        clearText = findViewById(R.id.idClearText);
        translateBtn = findViewById(R.id.idBtnTranslation);
        translateTV = findViewById(R.id.idTranslatedTV);
        recyclerView = findViewById(R.id.idRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        speechRecognitionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            sourceText.setText(matches.get(0));
                        }
                    }
                }
        );

        clearText.setOnClickListener(v -> {
            sourceText.setText("");
            recyclerView.setVisibility(View.GONE);
        });

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fromLanguageCode = getLanguageCode(fromLanguage[i]);
                Log.d("LanguageTranslator", "Selected from language: " + fromLanguageCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
        ArrayAdapter fromAdapter = new ArrayAdapter(this, R.layout.spinner_item, fromLanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                toLanguageCode = getLanguageCode(toLanguage[i]);
                Log.d("LanguageTranslator", "Selected to language: " + toLanguageCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner_item, toLanguage);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        micTV.setOnClickListener(view -> {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something to translate");

            try {
                speechRecognitionLauncher.launch(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(LanguageTranslator.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        translateBtn.setOnClickListener(view -> {
            translateTV.setVisibility(View.VISIBLE);
            translateTV.setText("");
            if (sourceText.getText().toString().isEmpty()) {
                Toast.makeText(LanguageTranslator.this, "Please enter text to translate", Toast.LENGTH_SHORT).show();
            } else if (fromLanguageCode.isEmpty()) {
                Toast.makeText(LanguageTranslator.this, "Please select Source Language", Toast.LENGTH_SHORT).show();
            } else if (toLanguageCode.isEmpty()) {
                Toast.makeText(LanguageTranslator.this, "Please select the language to make translation", Toast.LENGTH_SHORT).show();
            } else {
                translateText(fromLanguageCode, toLanguageCode, sourceText.getText().toString());
            }
        });
    }

    private void translateText(String fromLanguageCode, String toLanguageCode, String source) {
        translateTV.setText("Downloading model, please wait...");
        Log.d("LanguageTranslator", "Downloading model for languages: " + fromLanguageCode + " to " + toLanguageCode);

        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();

        final Translator translator = com.google.mlkit.nl.translate.Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder().requireWifi().build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(aVoid -> {
                    Log.d("LanguageTranslator", "Model downloaded successfully.");
                    translateTV.setText("Translating...");
                    translator.translate(source)
                            .addOnSuccessListener(s -> {
                                Log.d("LanguageTranslator", "Translation successful: " + s);
                                translateTV.setText(s);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("LanguageTranslator", "Translation failed: " + e.getMessage());
                                Toast.makeText(LanguageTranslator.this, "Failed to translate!! Try again", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("LanguageTranslator", "Model download failed: " + e.getMessage());
                    Toast.makeText(LanguageTranslator.this, "Failed to download model!! Check your internet connection.", Toast.LENGTH_SHORT).show();
                });
    }

    private String getLanguageCode(String language) {
        switch (language) {
            case "English":
                return TranslateLanguage.ENGLISH;
            case "Afrikaans":
                return TranslateLanguage.AFRIKAANS;
            case "Arabic":
                return TranslateLanguage.ARABIC;
            case "Belarusian":
                return TranslateLanguage.BELARUSIAN;
            case "Bulgarian":
                return TranslateLanguage.BULGARIAN;
            case "Bengali":
                return TranslateLanguage.BENGALI;
            case "Catalan":
                return TranslateLanguage.CATALAN;
            case "Czech":
                return TranslateLanguage.CZECH;
            case "Welsh":
                return TranslateLanguage.WELSH;
            case "Hindi":
                return TranslateLanguage.HINDI;
            case "Chinese":
                return TranslateLanguage.CHINESE;
            case "Danish":
                return TranslateLanguage.DANISH;
            case "Dutch":
                return TranslateLanguage.DUTCH;
            case "French":
                return TranslateLanguage.FRENCH;
            case "German":
                return TranslateLanguage.GERMAN;
            case "Greek":
                return TranslateLanguage.GREEK;
            case "Gujarati":
                return TranslateLanguage.GUJARATI;
            case "Hebrew":
                return TranslateLanguage.HEBREW;
            case "Hungarian":
                return TranslateLanguage.HUNGARIAN;
            case "Indonesian":
                return TranslateLanguage.INDONESIAN;
            case "Irish":
                return TranslateLanguage.IRISH;
            case "Italian":
                return TranslateLanguage.ITALIAN;
            case "Japanese":
                return TranslateLanguage.JAPANESE;
            case "Kannada":
                return TranslateLanguage.KANNADA;
            case "Korean":
                return TranslateLanguage.KOREAN;
            case "Lithuanian":
                return TranslateLanguage.LITHUANIAN;
            case "Malay":
                return TranslateLanguage.MALAY;
            case "Marathi":
                return TranslateLanguage.MARATHI;

            case "Norwegian":
                return TranslateLanguage.NORWEGIAN;
            case "Polish":
                return TranslateLanguage.POLISH;
            case "Portuguese":
                return TranslateLanguage.PORTUGUESE;

            case "Romanian":
                return TranslateLanguage.ROMANIAN;
            case "Russian":
                return TranslateLanguage.RUSSIAN;
            case "Spanish":
                return TranslateLanguage.SPANISH;
            case "Swedish":
                return TranslateLanguage.SWEDISH;
            case "Tamil":
                return TranslateLanguage.TAMIL;
            case "Telugu":
                return TranslateLanguage.TELUGU;
            case "Thai":
                return TranslateLanguage.THAI;
            case "Turkish":
                return TranslateLanguage.TURKISH;
            case "Ukrainian":
                return TranslateLanguage.UKRAINIAN;
            case "Urdu":
                return TranslateLanguage.URDU;
            case "Vietnamese":
                return TranslateLanguage.VIETNAMESE;
            default:
                return "";
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        Navbar navbar = new Navbar(this);
        Intent newActivity = navbar.redirect(id);
        startActivity(newActivity);
        return true;
    }
}
