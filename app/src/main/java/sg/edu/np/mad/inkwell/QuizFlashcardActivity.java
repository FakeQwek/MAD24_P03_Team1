package sg.edu.np.mad.inkwell;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewAnimator;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class QuizFlashcardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // Get firebase
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Get id of current user
    String currentFirebaseUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Declaration of variables

    // currentFlashcardPosition keeps track of the number of flashcards that have been shown
    private int currentFlashcardPosition = 0;

    private int correct;

    private int stillLearning;

    private int flashcardCount;

    ArrayList<String> questionList;

    ArrayList<String> answerList;

    TextView question1;

    TextView flashcardsLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_flashcard);

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

        ViewAnimator viewAnimator = findViewById(R.id.viewAnimator);

        questionList = new ArrayList<>();

        answerList = new ArrayList<>();

        question1 = findViewById(R.id.question1);

        TextView question2 = findViewById(R.id.question2);

        TextView knownCount = findViewById(R.id.knownCount);

        flashcardsLeft = findViewById(R.id.flashcardsLeft);

        TextView stillLearningCount = findViewById(R.id.stillLearningCount);

        ProgressBar progressBar = findViewById(R.id.progressBar);

        ImageButton answerButton1 = findViewById(R.id.answerButton1);

        ImageButton answerButton2 = findViewById(R.id.answerButton2);

        ImageButton wrongButton = findViewById(R.id.wrongButton);

        // Increments stillLearning and goes to the next flashcard
        wrongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFlashcardPosition++;
                answerButton1.setImageResource(R.drawable.eye_off_outline);
                answerButton2.setImageResource(R.drawable.eye_off_outline);
                if (currentFlashcardPosition > questionList.size()) {
                    Intent flashcardActivity = new Intent(QuizFlashcardActivity.this, FlashcardActivity.class);
                    startActivity(flashcardActivity);

                    db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).update("correct", correct);
                } else if (currentFlashcardPosition == questionList.size()) {
                    stillLearning++;
                    viewAnimator.setDisplayedChild(2);
                    knownCount.setText(String.valueOf(correct));
                    stillLearningCount.setText(String.valueOf(stillLearning));
                    progressBar.setMax(correct + stillLearning);
                    progressBar.setProgress(correct);
                } else {
                    stillLearning++;
                    if (viewAnimator.getDisplayedChild() == 0) {
                        question2.setText(questionList.get(currentFlashcardPosition));
                        viewAnimator.setDisplayedChild(1);
                    } else {
                        question1.setText(questionList.get(currentFlashcardPosition));
                        viewAnimator.setDisplayedChild(0);
                    }
                }

                if (flashcardCount != 0) {
                    flashcardCount -= 1;
                    flashcardsLeft.setText(flashcardCount + " Left");
                }
            }
        });

        ImageButton correctButton = findViewById(R.id.correctButton);

        // Increments correct and goes to the next flashcard
        correctButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFlashcardPosition++;
                answerButton1.setImageResource(R.drawable.eye_off_outline);
                answerButton2.setImageResource(R.drawable.eye_off_outline);
                if (currentFlashcardPosition > questionList.size()) {
                    Intent flashcardActivity = new Intent(QuizFlashcardActivity.this, FlashcardActivity.class);
                    startActivity(flashcardActivity);

                    db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).update("correct", correct);
                } else if (currentFlashcardPosition == questionList.size()) {
                    correct++;
                    viewAnimator.setDisplayedChild(2);
                    knownCount.setText(String.valueOf(correct));
                    stillLearningCount.setText(String.valueOf(stillLearning));
                    progressBar.setMax(correct + stillLearning);
                    progressBar.setProgress(correct);
                } else {
                    correct++;
                    if (viewAnimator.getDisplayedChild() == 0) {
                        question2.setText(questionList.get(currentFlashcardPosition));
                        viewAnimator.setDisplayedChild(1);
                    } else {
                        question1.setText(questionList.get(currentFlashcardPosition));
                        viewAnimator.setDisplayedChild(0);
                    }
                }

                if (flashcardCount != 0) {
                    flashcardCount -= 1;
                    flashcardsLeft.setText(flashcardCount + " Left");
                }
            }
        });

        // Toggles between the question and answer
        answerButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (question1.getText() == answerList.get(currentFlashcardPosition)) {
                    question1.setText(questionList.get(currentFlashcardPosition));
                    answerButton1.setImageResource(R.drawable.eye_off_outline);
                } else {
                    question1.setText(answerList.get(currentFlashcardPosition));
                    answerButton1.setImageResource(R.drawable.eye_outline);
                }
            }
        });

        // Toggles between the question and answer
        answerButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (question2.getText() == answerList.get(currentFlashcardPosition)) {
                    question2.setText(questionList.get(currentFlashcardPosition));
                    answerButton2.setImageResource(R.drawable.eye_off_outline);
                } else {
                    question2.setText(answerList.get(currentFlashcardPosition));
                    answerButton2.setImageResource(R.drawable.eye_outline);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Read from firebase and create flashcards on create
        db.collection("users").document(currentFirebaseUserUid).collection("flashcardCollections").document(String.valueOf(FlashcardActivity.selectedFlashcardCollectionId)).collection("flashcards")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                questionList.add(document.getData().get("question").toString());
                                answerList.add(document.getData().get("answer").toString());
                                flashcardCount += 1;
                            }
                        } else {
                            Log.d("testing", "Error getting documents: ", task.getException());
                        }
                        question1.setText(questionList.get(currentFlashcardPosition));
                        flashcardsLeft.setText(flashcardCount + " Left");
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