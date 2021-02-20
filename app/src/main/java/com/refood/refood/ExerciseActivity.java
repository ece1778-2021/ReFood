package com.refood.refood;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;

public class ExerciseActivity extends AppCompatActivity {

    private final int EXERCISE_DURATION = 10;
    private int mCount;

    private Button mStartButton;
    private ImageView mCue;

    private final int[] mGoCue = {R.drawable.brocolli};
    private final int[] mNoGoCue = {R.drawable.burger};
    private int mParentWidth;
    private int mParentHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        // TODO: get the actual number of coins
        int numCoin = 132;
        ((TextView)findViewById(R.id.exercise_coin_text)).setText(getString(R.string.coins_template, numCoin));

        mCue = findViewById(R.id.cue_view);
        mStartButton = findViewById(R.id.start_exercise_button);
    }

    public void startExercise(View view) {
        mCount = 0;
        mStartButton.setVisibility(View.GONE);
        View parent = (View)mStartButton.getParent();
        mParentWidth = parent.getWidth() - mCue.getWidth();
        mParentHeight = parent.getHeight() - mCue.getHeight();
        startExerciseHepler(view);
    }

    private void startExerciseHepler(View view) {
        chooseCue();
        new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                mCue.setVisibility(View.INVISIBLE);
                if (mCount < EXERCISE_DURATION)
                {
                    startExerciseHepler(view);
                    mCount++;
                }
                else
                {
                    mStartButton.setVisibility(View.VISIBLE);
                }
            }
        }.start();
    }

    // Helpers for choosing a random image for generating a cue
    private void chooseCue() {
        mCue.setImageResource(chooseResource(chooseGoCue()));
        mCue.setX(randomInRange(0, mParentWidth));
        mCue.setY(randomInRange(0, mParentHeight));
        mCue.setVisibility(View.VISIBLE);
    }
    private int[] chooseGoCue() {
        return (Math.random() < 0.5) ? mGoCue:mNoGoCue;
    }

    private int chooseResource(int[] arr)
    {
        return arr[randomInRange(0, arr.length)];
    }

    private int randomInRange(int min, int max)
    {
        return (int)(Math.random() * (max-min) + min);
    }
}