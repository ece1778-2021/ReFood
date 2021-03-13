package com.refood.refood;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;
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

import java.util.ArrayList;
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
    private static final int NUM_LEVEL = 4;
//    private static final int ROUND_DURATION = 40;
    private static final int ROUND_DURATION = 20;
//    private static final int DEFAULT_CUE_NUM = 15;
    private static final int DEFAULT_CUE_NUM = 7;
    private static final int DEFAULT_APPEAR_TIME = 1500;
    private static final int DEFAULT_ISI = 500;
    private int mGameProgress;

    private Button mStartButton;
    private ImageView mCue;
    private TextView mFeedback;
    private TextView mXpText;
    private ProgressBar mXpBar;

    private final int[] mGoCue = {R.drawable.brocolli};
    private final int[] mNoGoCue = {R.drawable.burger};
    private final int[] mNonFoodCue = {R.drawable.clothes};
    private final int[] mCoinCue = {R.drawable.coin};
    private final int[] mBombCue = {R.drawable.bomb};

    private int mParentWidth;
    private int mParentHeight;

    private int mRound;
    private long mNumCoins;
    private int mLevel;
    private long mScore;
    private boolean mGoChosen;
    private boolean mSpecialCue;

    private int mNumHealthyFoodCues;
    private int mNumUnhealthyFoodCues;
    private int mNumNonFoodCues;
    private int mNumCoinCues;
    private int mNumBombCues;

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

        mXpText = findViewById(R.id.exercise_xp_text);
        mXpBar = findViewById(R.id.xp_bar);
        setXpBar();

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
                setXpBar();
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
        mRound = mRound%NUM_ROUND + 1;
        if (mRound <= NUM_ROUND)
        {
            mAppearTime = DEFAULT_APPEAR_TIME;
            switch (mLevel)
            {
                case 1:
                case 2:
                    mNumHealthyFoodCues = DEFAULT_CUE_NUM;
                    mNumUnhealthyFoodCues = DEFAULT_CUE_NUM;
                    mNumCoinCues = 0;
                    mNumBombCues = 0;
                    mNumNonFoodCues = ROUND_DURATION - mNumHealthyFoodCues - mNumUnhealthyFoodCues - mNumCoinCues - mNumBombCues;
                    break;
                case 3:
                case 4:
                    mNumHealthyFoodCues = DEFAULT_CUE_NUM;
                    mNumUnhealthyFoodCues = DEFAULT_CUE_NUM;
                    mNumCoinCues = 2;
                    mNumBombCues = 2;
                    mNumNonFoodCues = ROUND_DURATION - mNumHealthyFoodCues - mNumUnhealthyFoodCues - mNumCoinCues - mNumBombCues;
                    break;
            }
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
                    updateCoinsCounter(!mGoChosen, mSpecialCue);
                    mCue.setVisibility(View.INVISIBLE);
                }
                if (mGameProgress < ROUND_DURATION)
                {
                    // Inner timer for ISI time
                    switch (mLevel)
                    {
                        case 1:
                            mISI = DEFAULT_ISI;
                            break;
                        case 2:
                            mISI = randomInRange(300, 800);
                            break;
                        case 3:
                            mISI = randomInRange(300, 1000);
                            break;
                        case 4:
                            mISI = randomInRange(300, 1200);
                            break;
                    }
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
                        Toast.makeText(ExerciseActivity.this, "Average Response Time = " + (mNumClicks!=0? String.valueOf(mtotalResponseTime/mNumClicks) : "No clicks done"),
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
        int sum = mNumHealthyFoodCues + mNumNonFoodCues + mNumUnhealthyFoodCues + mNumCoinCues + mNumBombCues;
        double healthyFoodThreshold = ((double)mNumHealthyFoodCues) / ((double)sum);
        double nonFoodThreshold = ((double)mNumNonFoodCues) / ((double)sum) + healthyFoodThreshold;
        double unhealthyFoodThreshold = ((double)mNumUnhealthyFoodCues) / ((double)sum) + nonFoodThreshold;
        double coinThreshold = ((double)mNumCoinCues) / ((double)sum) + unhealthyFoodThreshold;
        double randomNum = Math.random();
        if (randomNum < healthyFoodThreshold)
        {
            mCue.setBackgroundResource(R.drawable.green_border_background);
            mGoChosen = true;
            mSpecialCue = false;
            mNumHealthyFoodCues--;
            return mGoCue;
        }
        else if (randomNum < nonFoodThreshold)
        {
            mCue.setBackgroundResource(R.drawable.green_border_background);
            mGoChosen = true;
            mSpecialCue = false;
            mNumNonFoodCues--;
            return mNonFoodCue;
        }
        else if (randomNum < unhealthyFoodThreshold)
        {
            mCue.setBackgroundResource(R.drawable.red_border_background);
            mGoChosen = false;
            mSpecialCue = false;
            mNumUnhealthyFoodCues--;
            return mNoGoCue;
        }
        else if (randomNum < coinThreshold)
        {
            mCue.setBackgroundResource(0);
            mGoChosen = true;
            mSpecialCue = true;
            mNumCoinCues--;
            return mCoinCue;
        }
        else
        {
            mCue.setBackgroundResource(0);
            mGoChosen = false;
            mSpecialCue = true;
            mNumBombCues--;
            return mBombCue;
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

    private void updateCoinsCounter(boolean increment, boolean special)
    {
        int stride = special?5:1;
        if (increment)
        {
            mNumCoins+=stride;
            mScore+=stride;
            mFeedback.setText(getString(R.string.increment_feedback_text, stride));
            mFeedback.setTextColor(getColor(R.color.green));
        }
        else
        {
            mNumCoins-=stride;
            mScore-=stride;
            mFeedback.setText(getString(R.string.decrement_feedback_text, stride));
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
        updateCoinsCounter(mGoChosen, mSpecialCue);
    }

    private void setXpBar()
    {
        // read the size of history from firebase, and then set xp related info
        if (user!=null) {
            String uid = user.getUid();
            DocumentReference docRef = mDb.collection("users").document(uid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> userStore = document.getData();
                            Log.d(LOG_TAG, "DocumentSnapshot data: " + userStore);

                            ArrayList<String> dateList = (ArrayList<String>) userStore.get("exerciseHistory");

                            //12 takes 2xp, 23 takes 3xp, 45xp takes 4xp, etc… (progressive ratio)​
                            int xp = dateList.size();
                            setXpBarHelper(xp);

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

    private void setXpBarHelper(int xp) {
        int progressRatio = 2;
        mLevel = 1;
        while (xp >= progressRatio && mLevel < NUM_LEVEL)
        {
            xp-=progressRatio;
            progressRatio ++;
            mLevel++;
        }
        mXpText.setText(getString(R.string.level_template, mLevel));
        mXpBar.setProgress(120/progressRatio*xp);
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.menu is a reference to an xml file named menu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.menu_skip, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.skip_action) {
            mGameProgress = ROUND_DURATION;
        }
        return super.onOptionsItemSelected(item);
    }
}