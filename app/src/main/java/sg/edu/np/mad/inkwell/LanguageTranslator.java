package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class LanguageTranslator extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 1;

    private Spinner fromSpinner, toSpinner;
    private TextInputEditText sourceText;
    private ImageView micTV;
    private MaterialButton translateBtn;
    private TextView translateTV;

    private ActivityResultLauncher<Intent> speechRecognitionLauncher;

    String[] fromLanguage = {"To", "English", "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Chinese"};
    String[] toLanguage = {"To", "English", "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Chinese"};

    String fromLanguageCode, toLanguageCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_translator);

        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        sourceText = findViewById(R.id.idEditSource);
        micTV = findViewById(R.id.idIvMic);
        translateBtn = findViewById(R.id.idBtnTranslation);
        translateTV = findViewById(R.id.idTranslatedTV);

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

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fromLanguageCode = getLanguageCode(fromLanguage[i]);
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner_item, toLanguage);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        micTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });

        translateBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                translateTV.setVisibility(View.VISIBLE);
                translateTV.setText("");
                if (sourceText.getText().toString().isEmpty()){
                    Toast.makeText(LanguageTranslator.this, "Please enter text to translate", Toast.LENGTH_SHORT).show();
                }else if(fromLanguageCode.isEmpty()){
                    Toast.makeText(LanguageTranslator.this, "Please select Source Language", Toast.LENGTH_SHORT).show();
                }else if(toLanguageCode.isEmpty()){
                    Toast.makeText(LanguageTranslator.this, "Please select the language to make translation", Toast.LENGTH_SHORT).show();
                }else {
                    translateText(fromLanguageCode, toLanguageCode, sourceText.getText().toString());
                }
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
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("LanguageTranslator", "Model downloaded successfully.");
                        translateTV.setText("Translation...");
                        translator.translate(source)
                                .addOnSuccessListener(new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String s) {
                                        Log.d("LanguageTranslator", "Translation successful: " + s);
                                        translateTV.setText(s);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("LanguageTranslator", "Translation failed: " + e.getMessage());
                                        Toast.makeText(LanguageTranslator.this, "Failed to translate!! Try again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("LanguageTranslator", "Model download failed: " + e.getMessage());
                        Toast.makeText(LanguageTranslator.this, "Failed to download model!! Check your internet connection.", Toast.LENGTH_SHORT).show();
                    }
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
            default:
                return "";
        }
    }
}
