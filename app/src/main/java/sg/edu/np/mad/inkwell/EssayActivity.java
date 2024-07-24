package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.w3c.dom.Text;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

public class EssayActivity extends AppCompatActivity implements SpellCheckerSession.SpellCheckerSessionListener, NavigationView.OnNavigationItemSelectedListener{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_essay);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Finds nav bar drawer and nav view before setting listener
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Sets listener to allows for closing and opening of the navbar
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        TextView titleText = findViewById(R.id.titleText);
        EditText essayText = findViewById(R.id.essayText);
        String essay = essayText.getText().toString();

        TextView wordCount = findViewById(R.id.wordCount);
        TextView charCount = findViewById(R.id.charCount);
        TextView sentenceCount = findViewById(R.id.sentenceCount);
        TextView essayDetails = findViewById(R.id.essayDetails);
        TextView wordsTitle = findViewById(R.id.wordsTitle);
        TextView wordCounts = findViewById(R.id.wordCounts);
        charCount.setText("Chars: ");
        wordCount.setText("Words: ");
        sentenceCount.setText("Sentences: ");
        wordsTitle.setText("Most used Words: ");

        Button confirmButton = findViewById(R.id.checkButton);
        confirmButton.setText("Check Essay");
        essayDetails.setText("Essay Information");
        titleText.setText("Essay Tool");
        wordsTitle.setTypeface(wordsTitle.getTypeface(), Typeface.BOLD);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String essay = essayText.getText().toString();
                if(essay.equals("")) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(EssayActivity.this);
                    alertBuilder.setMessage("Your essay cannot have no words!");
                    alertBuilder.setCancelable(true);
                    AlertDialog emptyAlert = alertBuilder.create();
                    emptyAlert.show();
                    return;
                }
                charCount.setText("Chars: " + essay.length());
                String[] words = essay.split(" ");
                String[] sentences = essay.split("\\.");
                String[] usedWords;
                Dictionary<String, Integer> wordCounts = new Hashtable<>();

                int count = 0;
                String word = "";
                //Goes through each word
                for(int i=0; i < words.length; i++) {
                    //Assigns the word
                    word = words[i];
                    //Goes through each word to get count
                    for(int x=0; x < words.length; x++) {
                        //When it reaches selected word
                        if(word == words[x]) {
                            //And word has not already been counted
                            if(wordCounts.get(word) != null) {
                                continue;
                            }
                            count+= 1;


                        }
                    }


                    int wordPer = Math.round(count/words.length);

                    wordCounts.put(word, wordPer);
                }
                Enumeration<String> keys = wordCounts.keys();
                String wordCountText = "";
                while(keys.hasMoreElements()) {
                    String key = keys.nextElement();
                    wordCountText += key + " " + wordCounts.get(key) + "%";
                }
                wordCount.setText("Words: " + words.length);
                sentenceCount.setText("Sentences: " + sentences.length);
                titleText.setText("Essay Tool");
                fetchSuggestionsFor(essay);
            }
        });

        TextView recommendationText = findViewById(R.id.recommended);
        recommendationText.setText("Spelling recommendations: " + "\n\n");
        recommendationText.setTypeface(recommendationText.getTypeface(), Typeface.BOLD);

    }
    @Override
    public void onGetSuggestions(SuggestionsInfo[] results) {
    }
    @Override
    public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results) {
        final StringBuffer sb = new StringBuffer("");
        for(SentenceSuggestionsInfo result: results){
            Log.i("Debug", "Result: " + result);
            int n = result.getSuggestionsCount();
            for(int i=0; i < n; i++){
                int m = result.getSuggestionsInfoAt(i).getSuggestionsCount();

                //Removes words that dont need spell checks
                if((result.getSuggestionsInfoAt(i).getSuggestionsAttributes() & SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO) != SuggestionsInfo.RESULT_ATTR_LOOKS_LIKE_TYPO ) {
                    continue;
                }
                for(int k=0; k < m; k++) {
                    sb.append(result.getSuggestionsInfoAt(i).getSuggestionAt(k))
                            .append("\n");
                    Log.i("Debug", result.getSuggestionsInfoAt(i).getSuggestionAt(k));


                }
                sb.append("\n");
                Log.i("Debug", "sb: " +sb);
            }
        }
        //Sets the text in ui thread as spellchecker is asynchronous
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView recommendationText = findViewById(R.id.recommended);

                recommendationText.setText("Spelling recommendations: " + "\n" + sb.toString());
            }
        });
    }


    private void fetchSuggestionsFor(String essay) {
        TextServicesManager tsm = (TextServicesManager) getSystemService(TEXT_SERVICES_MANAGER_SERVICE);
        SpellCheckerSession session = tsm.newSpellCheckerSession(null, Locale.ENGLISH, this,true);
        session.getSentenceSuggestions(
                new TextInfo[]{new TextInfo(essay)},
                5

        );
    }
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int id = menuItem.getItemId();
        Navbar navbar = new Navbar(this);
        Intent newActivity = navbar.redirect(id);
        startActivity(newActivity);
        Log.d("Alert", "Starting" + newActivity);
        return true;
    }

}