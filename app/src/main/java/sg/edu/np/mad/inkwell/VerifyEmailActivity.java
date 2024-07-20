package sg.edu.np.mad.inkwell;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerifyEmailActivity extends AppCompatActivity {

    private static final String TAG = "VerifyEmailActivity";

    private TextView verifyMessage, timerText;
    private Button checkVerificationButton, resendButton;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private SharedPreferences sharedPreferences;
    private CountDownTimer countDownTimer;
    private static final long TIMER_DURATION = 60000; // 60 seconds
    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        auth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE);

        verifyMessage = findViewById(R.id.verify_message);
        timerText = findViewById(R.id.timer_text);
        checkVerificationButton = findViewById(R.id.check_verification_button);
        resendButton = findViewById(R.id.resend_button);

        // Retrieve email and password from intent
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");

        if (email == null || password == null) {
            Toast.makeText(this, "Failed to get email or password. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        user = auth.getCurrentUser();
        if (user != null) {
            startTimer();
        } else {
            // Sign in the user to get the current FirebaseUser
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        user = auth.getCurrentUser();
                        startTimer();
                    } else {
                        Toast.makeText(VerifyEmailActivity.this, "Failed to authenticate user", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        checkVerificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkVerification();
            }
        });

        resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationEmail();
            }
        });

        // Initially disable the resend button
        resendButton.setEnabled(false);
    }

    private void checkVerification() {
        if (user != null) {
            user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (user.isEmailVerified()) {
                        Toast.makeText(VerifyEmailActivity.this, "Email Verified Successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(VerifyEmailActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(VerifyEmailActivity.this, "Email not verified yet. Please check your email.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void resendVerificationEmail() {
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(VerifyEmailActivity.this, "Verification Email Sent Again", Toast.LENGTH_SHORT).show();
                        startTimer();
                    } else {
                        Toast.makeText(VerifyEmailActivity.this, "Failed to resend verification email.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(TIMER_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText(millisUntilFinished / 1000 + " secs");
            }

            @Override
            public void onFinish() {
                timerText.setText("0 secs");
                resendButton.setEnabled(true); // Enable the resend button after the timer finishes
            }
        }.start();
    }
}


