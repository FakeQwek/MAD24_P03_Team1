package sg.edu.np.mad.inkwell;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private EditText signupEmail, signupPassword;
    private Button signupButton;
    private TextView loginRedirectText;
    private SharedPreferences sharedPreferences;
    private ImageView signupShowPassword;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE);

        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signupShowPassword = findViewById(R.id.signup_show_password);

        signupShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    signupPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    signupShowPassword.setImageResource(R.drawable.baseline_remove_red_eye_24);
                } else {
                    signupPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    signupShowPassword.setImageResource(R.drawable.baseline_remove_red_eye_24);
                }
                isPasswordVisible = !isPasswordVisible;
                signupPassword.setSelection(signupPassword.length()); // Move cursor to the end of the text
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = signupEmail.getText().toString().trim();
                String password = signupPassword.getText().toString().trim();

                if (email.isEmpty()) {
                    signupEmail.setError("Email cannot be empty");
                    return;
                }
                if (password.isEmpty()) {
                    signupPassword.setError("Password cannot be empty");
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    signupEmail.setError("Please enter a valid email");
                    return;
                }

                checkIfUserExists(email, password); // Proceed with user registration
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class)); // Ensure LoginActivity exists
            }
        });
    }

    private void checkIfUserExists(String email, String password) {
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()) {
                    boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                    if (isNewUser) {
                        Log.d(TAG, "No existing user found, creating new user.");
                        createNewUser(email, password);
                    } else {
                        Log.d(TAG, "Existing user found, signing in.");
                        signInAndCheckVerification(email, password);
                    }
                } else {
                    Log.e(TAG, "Failed to check if user exists", task.getException());
                    Toast.makeText(SignUpActivity.this, "Failed to check if user exists: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createNewUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String username = email.split("@")[0];
                    saveUsernameToDatabase(username);
                    sendVerificationEmail();
                } else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Log.d(TAG, "User already exists, signing in.");
                        signInAndCheckVerification(email, password);
                    } else {
                        Log.e(TAG, "Signup failed", task.getException());
                        Toast.makeText(SignUpActivity.this, "Signup Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void saveUsernameToDatabase(String username) {
        String userId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Saving username " + username + " with userId " + userId);
        databaseReference.child(userId).setValue(username).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Username saved to database");
                    Toast.makeText(SignUpActivity.this, "Username saved to database", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to save username", task.getException());
                    Toast.makeText(SignUpActivity.this, "Failed to save username: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signInAndCheckVerification(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        if (!user.isEmailVerified()) {
                            Toast.makeText(SignUpActivity.this, "Email not verified. Redirecting to verification page.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUpActivity.this, VerifyEmailActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(SignUpActivity.this, "Email is already verified. Please log in.", Toast.LENGTH_SHORT).show();
                            auth.signOut(); // Sign out the user to prevent unauthorized access
                        }
                    }
                } else {
                    Log.e(TAG, "Authentication failed", task.getException());
                    if (task.getException() != null && task.getException().getMessage().contains("The password is invalid")) {
                        Toast.makeText(SignUpActivity.this, "Email is already registered but the password is incorrect.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void sendVerificationEmail() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "Verification Email Sent. Please check your email.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Verification email sent, signing out user.");
                        auth.signOut(); // Sign out the user to prevent unauthorized access

                        // Pass email and password to VerifyEmailActivity
                        Intent intent = new Intent(SignUpActivity.this, VerifyEmailActivity.class);
                        intent.putExtra("email", signupEmail.getText().toString().trim());
                        intent.putExtra("password", signupPassword.getText().toString().trim());
                        startActivity(intent);
                        finish(); // Ensure this activity is finished to prevent it from appearing in the back stack
                    } else {
                        Log.e(TAG, "Failed to send verification email", task.getException());
                        Toast.makeText(SignUpActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void saveToSharedPreferences(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetSharedPreferencesToDefault();
    }

    private void resetSharedPreferencesToDefault() {
        SharedPreferences sharedPreferences = getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", "user@gmail.com");
        editor.putString("password", "Pa$$w0rd");
        editor.apply();
    }
}
