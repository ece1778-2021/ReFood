package com.refood.refood;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // TODO: get the actual dayStreak
        int dayStreak = 5;
        ((TextView)findViewById(R.id.day_streak_text)).setText(getString(R.string.day_streak_template, dayStreak));

        // TODO: get the actual number of coins
        int numCoin = 132;
        ((TextView)findViewById(R.id.coin_text)).setText(getString(R.string.coins_template, numCoin));
    }

    public void goFoodProfile(View view) {
        Intent intent = new Intent(this, com.refood.refood.ProfileActivity.class);
        startActivity(intent);
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
}