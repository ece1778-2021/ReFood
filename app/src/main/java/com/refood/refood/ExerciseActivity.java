package com.refood.refood;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ExerciseActivity extends AppCompatActivity {

    private static final int NUM_ROUND = 2;
    private static final int NUM_LEVEL = 1;
    private static final int ROUND_DURATION = 40;
    private static final int DEFAULT_CUE_NUM = 15;
    private static final int DEFAULT_APPEAR_TIME = 1500;
    private static final int DEFAULT_ISI = 500;
    private int mGameProgress;

    private Button mStartButton;
    private ImageView mCue;
    private TextView mFeedback;

    private final int[] mGoCue = {R.drawable.brocolli};
    private final int[] mNoGoCue = {R.drawable.burger};
    private final int[] mNonFoodCue = {R.drawable.clothes};

    private int mParentWidth;
    private int mParentHeight;

    private int mRound;
    private int mCoins;
    private boolean mGoChosen;

    private int mNumHealthyFoodCues;
    private int mNumUnhealthyFoodCues;
    private int mNumNonFoodCues;

    private int mtotalResponseTime;
    private int mCurrentResponseTime;
    private int mNumClicks;

    private int mAppearTime;
    private int mISI;

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
        mtotalResponseTime = 0;
        mNumClicks = 0;
    }

    public void startExercise(View view) {
        mGameProgress = 1;
        mRound++;
        if (mRound <= NUM_ROUND)
        {
            mAppearTime = DEFAULT_APPEAR_TIME;
            mISI = DEFAULT_ISI;
            mNumHealthyFoodCues = DEFAULT_CUE_NUM;
            mNumUnhealthyFoodCues = DEFAULT_CUE_NUM;
            mNumNonFoodCues = ROUND_DURATION - mNumHealthyFoodCues - mNumUnhealthyFoodCues;
            mStartButton.setVisibility(View.GONE);
            View parent = (View)mStartButton.getParent();
            mParentWidth = parent.getWidth() - mCue.getWidth();
            mParentHeight = parent.getHeight() - mCue.getHeight();
            startExerciseHelper(view);
        }
    }

    private void startExerciseHelper(View view) {
        chooseCue();
        // Outer timer for Appear time
        new CountDownTimer(mAppearTime, 10) {
            public void onTick(long millisUntilFinished) {
                mCurrentResponseTime = mAppearTime - (int)millisUntilFinished;
            }

            public void onFinish() {
                if (mCue.getVisibility()==View.VISIBLE)
                {
                    updateCoinCounter(!mGoChosen);
                    mCue.setVisibility(View.INVISIBLE);
                }
                if (mGameProgress < ROUND_DURATION)
                {
                    // Inner timer for ISI time
                    new CountDownTimer(mISI, mISI) {
                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
                            startExerciseHelper(view);
                            mGameProgress++;
                        }
                    }.start();
                }
                else
                {
                    mStartButton.setVisibility(View.VISIBLE);
                    if (mRound+1 > NUM_ROUND)
                    {
                        mStartButton.setText(R.string.game_end_text);
                        Toast.makeText(ExerciseActivity.this, "Average Response Time = " + String.valueOf(mtotalResponseTime/mNumClicks),
                                Toast.LENGTH_SHORT).show();
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
                animation.setDuration(mAppearTime);
                animation.start();
                break;
            default:
                break;
        }
        mCue.setVisibility(View.VISIBLE);
    }
    private int[] chooseGoCue() {
        double healthyFoodThreshold = (double)mNumHealthyFoodCues / ((double)(mNumHealthyFoodCues + mNumUnhealthyFoodCues + mNumNonFoodCues));
        double nonFoodThreshold = ((double)(mNumHealthyFoodCues + mNumNonFoodCues)) / ((double)(mNumHealthyFoodCues + mNumUnhealthyFoodCues + mNumNonFoodCues));
        double randomNum = Math.random();
        if (randomNum < healthyFoodThreshold)
        {
            mCue.setBackgroundResource(R.drawable.green_border_background);
            mGoChosen = true;
            mNumHealthyFoodCues--;
            return mGoCue;
        }
        else if (randomNum < nonFoodThreshold)
        {
            mCue.setBackgroundResource(R.drawable.green_border_background);
            mGoChosen = true;
            mNumNonFoodCues--;
            return mNonFoodCue;
        }
        else
        {
            mCue.setBackgroundResource(R.drawable.red_border_background);
            mGoChosen = false;
            mNumUnhealthyFoodCues--;
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
        mtotalResponseTime += mCurrentResponseTime;
        mNumClicks++;
        updateCoinCounter(mGoChosen);
    }
}