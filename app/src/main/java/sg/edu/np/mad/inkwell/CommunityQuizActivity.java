package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class CommunityQuizActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    String currentFirebaseUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

    private String response = "What is the capital of France?;Paris;Berlin;Madrid;Rome;A;What is the largest planet in our solar system?;Mars;Venus;Earth;Jupiter;D;What is the chemical symbol for water?;H2O;O2;CO2;NaCl;A;Who wrote 'To Kill a Mockingbird'?;J.K. Rowling;Harper Lee;Mark Twain;Jane Austen;B;What is the smallest prime number?;1;2;3;5;B;Which element has the atomic number 1?;Helium;Oxygen;Hydrogen;Carbon;C;Who painted the Mona Lisa?;Vincent van Gogh;Pablo Picasso;Leonardo da Vinci;Claude Monet;C;What is the powerhouse of the cell?;Nucleus;Ribosome;Mitochondria;Chloroplast;C;What is the longest river in the world?;Nile;Amazon;Yangtze;Mississippi;A;What is the freezing point of water in Celsius?;0;32;100;212;A";

    private int points;

    private int questionCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_quiz);

        //Sets toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Finds drawer and nav view before setting listener
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);

        String[] questions = response.split(";", 0);

        TextView question = findViewById(R.id.question);

        RadioButton option1 = findViewById(R.id.option1);

        RadioButton option2 = findViewById(R.id.option2);

        RadioButton option3 = findViewById(R.id.option3);

        RadioButton option4 = findViewById(R.id.option4);

        question.setText(questions[0]);
        option1.setText(questions[1]);
        option2.setText(questions[2]);
        option3.setText(questions[3]);
        option4.setText(questions[4]);

        RadioGroup radioGroup = findViewById(R.id.radioGroup);

        Button submitButton = findViewById(R.id.submitButton);

        TextView pointCounter = findViewById(R.id.pointCounter);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();

                if (selectedRadioButtonId != -1) {
                    if (selectedRadioButtonId == R.id.option1) {
                        if (questions[6 * questionCount - 1].equals("A")) {
                            points += 1;
                        }
                    } else if (selectedRadioButtonId == R.id.option2) {
                        if (questions[6 * questionCount - 1].equals("B")) {
                            points += 1;
                        }
                    } else if (selectedRadioButtonId == R.id.option3) {
                        if (questions[6 * questionCount - 1].equals("C")) {
                            points += 1;
                        }
                    } else if (selectedRadioButtonId == R.id.option4) {
                        if (questions[6 * questionCount - 1].equals("D")) {
                            points += 1;
                        }
                    }

                    pointCounter.setText("Points: " + points);

                    if (questionCount < 10) {
                        question.setText(questions[6 * questionCount]);
                        option1.setText(questions[6 * questionCount + 1]);
                        option2.setText(questions[6 * questionCount + 2]);
                        option3.setText(questions[6 * questionCount + 3]);
                        option4.setText(questions[6 * questionCount + 4]);

                        questionCount += 1;

                        radioGroup.clearCheck();
                    } else {
                        radioGroup.clearCheck();

                        Map<String, Object> newLeaderboardData = new HashMap<>();
                        newLeaderboardData.put("email", currentFirebaseUserEmail);
                        newLeaderboardData.put("points", points);

                        db.collection("community").document(CommunityActivity.selectedNote.getId()).collection("leaderboard").document(currentFirebaseUserUid).set(newLeaderboardData);
                    }
                } else {

                }
            }
        });
    }

    //Allows movement between activities upon clicking
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        Navbar navbar = new Navbar(this);
        Intent newActivity = navbar.redirect(id);
        startActivity(newActivity);

        return true;
    }
}