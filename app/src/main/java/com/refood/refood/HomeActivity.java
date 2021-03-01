package com.refood.refood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();
    private static final int REQUEST_PROFILE_LOGOUT = 1;
    private static final int REQUEST_EXERCISE_UPDATE = 2;

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseFirestore mDb;

    private long mNumCoins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDb = FirebaseFirestore.getInstance();

        // TODO: get the actual dayStreak
        int dayStreak = 5;
        ((TextView)findViewById(R.id.day_streak_text)).setText(getString(R.string.day_streak_template, dayStreak));

        initCoins();
    }

    private void initCoins() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DocumentReference docRef = mDb.collection("users").document(uid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> userStore = document.getData();
                            Log.d(LOG_TAG, "DocumentSnapshot data: " + userStore);
                            mNumCoins = (long)(userStore.get("numCoins"));
                            setCoinsDisplay();

                        } else {
                            Log.d(LOG_TAG, "No such document");
                        }
                    } else {
                        Log.d(LOG_TAG, "get failed with ", task.getException());
                    }
                }
            });

        }
    }

    private void setCoinsDisplay()
    {
        ((TextView)findViewById(R.id.home_coin_text)).setText(getString(R.string.coins_template, mNumCoins));
    }

    public void goFoodProfile(View view) {
        Intent intent = new Intent(this, com.refood.refood.ProfileActivity.class);
        startActivityForResult(intent, REQUEST_PROFILE_LOGOUT);
    }

    public void goExercise(View view) {
        Intent intent = new Intent(this, com.refood.refood.ExerciseActivity.class);
        intent.putExtra("numCoins", mNumCoins);
        startActivityForResult(intent, REQUEST_EXERCISE_UPDATE);
    }

    public void goStats(View view) {
        Intent intent = new Intent(this, com.refood.refood.StatsActivity.class);
        startActivity(intent);
    }

    public void goZemGarden(View view) {
        Intent intent = new Intent(this, com.refood.refood.ZemGardenActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PROFILE_LOGOUT)
        {
            if (mAuth.getCurrentUser()==null)
            {
                finish();
            }
        }
        else if (requestCode == REQUEST_EXERCISE_UPDATE && data != null)
        {
            mNumCoins = data.getLongExtra("numCoins", mNumCoins);
            setCoinsDisplay();
        }
    }
}