package com.refood.refood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.refood.refood.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private static final String LOG_TAG = RegisterActivity.class.getSimpleName();
    private static final int REQUEST_BIOMETRICS_SURVEY = 1;
    private static final int REQUEST_WEEKLY_SURVEY = 2;

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseFirestore mDb;

    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mConfirmPasswordEditText;
    private EditText mUsernameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDb = FirebaseFirestore.getInstance();

        mEmailEditText = findViewById(R.id.editTextRegisterEmailAddress);
        mPasswordEditText = findViewById(R.id.editTextRegisterPassword);
        mConfirmPasswordEditText = findViewById(R.id.editTextRegisterConfirmPassword);
        mUsernameEditText = findViewById(R.id.editTextRegisterUsername);

        BackgroundMusic.getInstance(this).start();
    }

    public void DoRegister(View view) {
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        String confirmPassword = mConfirmPasswordEditText.getText().toString();
        String username = mUsernameEditText.getText().toString();

        if (!password.equals(confirmPassword))
        {
            Toast.makeText(RegisterActivity.this, "Confirm password mismatches",
                    Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
        else if (com.refood.refood.MainActivity.isEmptyString(email) || com.refood.refood.MainActivity.isEmptyString(password)
                || com.refood.refood.MainActivity.isEmptyString(username))
        {
            Toast.makeText(RegisterActivity.this, "Fields cannot be empty",
                    Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
        else
        {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(LOG_TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                // Create a new user with a first and last name
                                Map<String, Object> userStore = new HashMap<>();
                                userStore.put("username", username);
                                userStore.put("numCoins", 20);
                                userStore.put("scoreHistory", Collections.emptyList());
                                userStore.put("exerciseHistory", Collections.emptyList());
                                userStore.put("notificationEnabled", false);
                                userStore.put("notificationHour", 0);
                                userStore.put("notificationMinute", 0);
                                userStore.put("zemPath", "/zems/zem1.png");

                                List<String> zemList = new ArrayList<>();
                                zemList.add("/zems/zem1.png");
                                userStore.put("ownedZems", zemList);

                                String uid = user.getUid();
                                // Add a new document with a generated ID
                                mDb.collection("users").document(uid)
                                        .set(userStore)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(LOG_TAG, "DocumentSnapshot successfully written!");
                                                updateUI(user);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(LOG_TAG, "Error writing document", e);
                                            }
                                        });
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(LOG_TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterActivity.this,
                                        "Sign up failed: password is not strong enough or email already exists",
                                        Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }
                        }
                    });


        }
    }

    private void updateUI(FirebaseUser user) {
        if (user!=null){
            Intent intent = new Intent(this, com.refood.refood.BiometricsActivity.class);
            startActivityForResult(intent, REQUEST_BIOMETRICS_SURVEY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BIOMETRICS_SURVEY)
        {
            Intent intent = new Intent(this, com.refood.refood.SurveyActivity.class);
            startActivityForResult(intent, REQUEST_WEEKLY_SURVEY);
        }
        else if (requestCode == REQUEST_WEEKLY_SURVEY)
        {
            Intent intent = new Intent(this, com.refood.refood.HomeActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume(){
        super.onResume();
        BackgroundMusic.getInstance(this).start();
    }

    @Override
    protected void onPause() {
        BackgroundMusic.getInstance(this).pause();
        super.onPause();
    }

}