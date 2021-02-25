package com.refood.refood;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_PROFILE_LOGOUT = 1;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();

        // TODO: get the actual dayStreak
        int dayStreak = 5;
        ((TextView)findViewById(R.id.day_streak_text)).setText(getString(R.string.day_streak_template, dayStreak));

        // TODO: get the actual number of coins
        int numCoin = 132;
        ((TextView)findViewById(R.id.home_coin_text)).setText(getString(R.string.coins_template, numCoin));
    }

    public void goFoodProfile(View view) {
        Intent intent = new Intent(this, com.refood.refood.ProfileActivity.class);
        startActivityForResult(intent, REQUEST_PROFILE_LOGOUT);
    }

    public void goExercise(View view) {
        Intent intent = new Intent(this, com.refood.refood.ExerciseActivity.class);
        startActivity(intent);
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
        if (mAuth.getCurrentUser()==null)
        {
            finish();
        }
    }
}