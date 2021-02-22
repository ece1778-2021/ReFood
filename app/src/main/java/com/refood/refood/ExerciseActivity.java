package com.refood.refood;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ExerciseActivity extends AppCompatActivity {

    private static final int MAX_LEVEL = 2;
    private final int EXERCISE_DURATION = 10;
    private int mGameProgress;

    private Button mStartButton;
    private ImageView mCue;
    private TextView mFeedback;

    private final int[] mGoCue = {R.drawable.brocolli};
    private final int[] mNoGoCue = {R.drawable.burger};
    private int mParentWidth;
    private int mParentHeight;
    private int mRound;
    private int mCoins;
    private boolean mGoChosen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        // TODO: get the actual number of coins from firebase
        mCoins = 0;
        setCoinCounter();

        mCue = findViewById(R.id.cue_view);
        mStartButton = findViewById(R.id.start_exercise_button);
        mFeedback = findViewById(R.id.feedback_text);
        mRound = 0;
    }

    public void startExercise(View view) {
        mGameProgress = 1;
        mRound++;
        if (mRound <= MAX_LEVEL)
        {
            mStartButton.setVisibility(View.GONE);
            View parent = (View)mStartButton.getParent();
            mParentWidth = parent.getWidth() - mCue.getWidth();
            mParentHeight = parent.getHeight() - mCue.getHeight();
            startExerciseHelper(view);
        }
    }

    private void startExerciseHelper(View view) {
        chooseCue();
        new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                if (mCue.getVisibility()==View.VISIBLE)
                {
                    updateCoinCounter(!mGoChosen);
                    mCue.setVisibility(View.INVISIBLE);
                }
                if (mGameProgress < EXERCISE_DURATION)
                {
                    startExerciseHelper(view);
                    mGameProgress++;
                }
                else
                {
                    mStartButton.setVisibility(View.VISIBLE);
                    if (mRound+1 > MAX_LEVEL)
                    {
                        mStartButton.setText(R.string.game_end_text);
                    }
                    else
                    {
                        mStartButton.setText(R.string.round_transition_text);
                    }
                }
            }
        }.start();
    }

    // Helpers for choosing a random image for generating a cue
    private void chooseCue() {
        mCue.setImageResource(chooseResource(chooseGoCue()));
        mCue.setX(randomInRange(0, mParentWidth));
        switch (mRound)
        {
            case 1:
                mCue.setY(randomInRange(0, mParentHeight));
                break;
            case 2:
                mCue.setY(0);
                ObjectAnimator animation = ObjectAnimator.ofFloat(mCue, "translationY", mParentHeight);
                animation.setDuration(2000);
                animation.start();
                break;
            default:
                break;
        }
        mCue.setVisibility(View.VISIBLE);
    }
    private int[] chooseGoCue() {
        if (Math.random() < 0.5)
        {
            mCue.setBackgroundResource(R.drawable.green_border_background);
            mGoChosen = true;
            return mGoCue;
        }
        else
        {
            mCue.setBackgroundResource(R.drawable.red_border_background);
            mGoChosen = false;
            return mNoGoCue;
        }
    }

    private int chooseResource(int[] arr)
    {
        return arr[randomInRange(0, arr.length)];
    }

    private int randomInRange(int min, int max)
    {
        return (int)(Math.random() * (max-min) + min);
    }

    private void updateCoinCounter(boolean increment)
    {
        if (increment)
        {
            mCoins++;
            mFeedback.setText(R.string.increment_feedback_text);
            mFeedback.setTextColor(getColor(R.color.green));
        }
        else
        {
            mCoins--;
            mFeedback.setText(R.string.decrement_feedback_text);
            mFeedback.setTextColor(getColor(R.color.red));
        }
        mFeedback.setVisibility(View.VISIBLE);
        new CountDownTimer(500, 500) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                mFeedback.setVisibility(View.INVISIBLE);
            }
        }.start();
        setCoinCounter();
    }

    private void setCoinCounter()
    {
        ((TextView)findViewById(R.id.exercise_coin_text)).setText(getString(R.string.coins_template, mCoins));
    }

    public void countClick(View view) {
        mCue.setVisibility(View.INVISIBLE);
        updateCoinCounter(mGoChosen);
    }
}