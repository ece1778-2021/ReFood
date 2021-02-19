package com.refood.refood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.refood.refood.R;
import com.refood.refood.RegisterActivity;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private FirebaseAuth mAuth;

    private EditText mEmailEditText;
    private EditText mPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.login_appbar_text);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mEmailEditText = findViewById(R.id.editTextLoginEmailAddress);
        mPasswordEditText = findViewById(R.id.editTextLoginPassword);
    }

    public void LaunchRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser!=null) {
            Intent intent = new Intent(this, com.refood.refood.HomeActivity.class);
            startActivity(intent);
        }
    }


    public void DoLogin(View view) {
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        if (MainActivity.isEmptyString(email) || MainActivity.isEmptyString(password))
        {
            Toast.makeText(MainActivity.this, "Fields cannot be empty",
                    Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
        else
        {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(LOG_TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(LOG_TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                updateUI(null);
                                // ...
                            }

                            // ...
                        }
                    });
        }

    }

    static public boolean isEmptyString(String inStr) {
        return inStr==null || inStr.trim().isEmpty();
    }
}