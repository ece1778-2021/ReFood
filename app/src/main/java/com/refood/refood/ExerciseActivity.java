package com.refood.refood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExerciseActivity extends AppCompatActivity {

    private static final String LOG_TAG = ExerciseActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseFirestore mDb;
    private FirebaseUser user;

    private static final int NUM_ROUND = 2;
    private static final int NUM_LEVEL = 1;
//    private static final int ROUND_DURATION = 40;
    private static final int ROUND_DURATION = 4;
//    private static final int DEFAULT_CUE_NUM = 15;
    private static final int DEFAULT_CUE_NUM = 1;
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
    private long mNumCoins;
    private long mScore;
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

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDb = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        Intent intent = getIntent();
        mNumCoins = intent.getLongExtra("numCoins", 0);
        mScore = 0;
        setCoinDisplay();

        mCue = findViewById(R.id.cue_view);
        mStartButton = findViewById(R.id.start_exercise_button);
        mFeedback = findViewById(R.id.feedback_text);
        mRound = 0;
        mtotalResponseTime = 0;
        mNumClicks = 0;
    }

    private void updateCoinsStorage() {
        Map<String, Object> data = new HashMap<>();
        data.put("numCoins", mNumCoins);

        Intent replyIntent = new Intent();
        replyIntent.putExtra("numCoins", mNumCoins);
        setResult(RESULT_OK, replyIntent);

        if (user != null) {
            String uid = user.getUid();
            mDb.collection("users").document(uid)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(LOG_TAG, "Coins successfully updated in FireStore!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(LOG_TAG, "Error writing document", e);
                        }
                    });
        }
    }

    private void updateScoreHistoryStorage() {
//        if (user != null) {
//            String uid = user.getUid();
//            mDb.collection("users").document(uid)
//                    .update("scoreHistory", FieldValue.arrayUnion(mScore))
//                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Log.d(LOG_TAG, "scoreHistory successfully updated in FireStore!");
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.w(LOG_TAG, "Error writing document", e);
//                        }
//                    });
//
//            mDb.collection("users").document(uid)
//                    .update("exerciseHistory", FieldValue.arrayUnion(Calendar.getInstance().getTime()))
//                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Log.d(LOG_TAG, "exerciseHistory successfully updated in FireStore!");
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.w(LOG_TAG, "Error writing document", e);
//                        }
//                    });
//        }
        if (user == null){ return; }

        final DocumentReference sfDocRef = mDb.collection("users").document(user.getUid());

        mDb.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(sfDocRef);

                List<String> exerciseHistory = (List<String>) snapshot.get("exerciseHistory");
                Calendar c = Calendar.getInstance();
                exerciseHistory.add(String.valueOf(c.get(Calendar.MONTH))+'/'+String.valueOf(c.get(Calendar.DAY_OF_MONTH))+'/'+c.get(Calendar.YEAR));
                List<Long> scoreHistory = (List<Long>) snapshot.get("scoreHistory");
                scoreHistory.add(mScore);

                transaction.update(sfDocRef, "exerciseHistory", exerciseHistory);
                transaction.update(sfDocRef, "scoreHistory", scoreHistory);

                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(LOG_TAG, "Transaction success!");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(LOG_TAG, "Transaction failure.", e);
                    }
                });
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
                    updateCoinsCounter(!mGoChosen);
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
                        updateCoinsStorage();
                        updateScoreHistoryStorage();
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

    private void updateCoinsCounter(boolean increment)
    {
        if (increment)
        {
            mNumCoins++;
            mScore++;
            mFeedback.setText(R.string.increment_feedback_text);
            mFeedback.setTextColor(getColor(R.color.green));
        }
        else
        {
            mNumCoins--;
            mScore--;
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
        setCoinDisplay();
    }

    private void setCoinDisplay()
    {
        ((TextView)findViewById(R.id.exercise_coin_text)).setText(getString(R.string.coins_template, mNumCoins));
    }

    public void countClick(View view) {
        mCue.setVisibility(View.INVISIBLE);
        mtotalResponseTime += mCurrentResponseTime;
        mNumClicks++;
        updateCoinsCounter(mGoChosen);
    }
}