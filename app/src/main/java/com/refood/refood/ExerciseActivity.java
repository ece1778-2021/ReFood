package com.refood.refood;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExerciseActivity extends AppCompatActivity {

    private static final String LOG_TAG = ExerciseActivity.class.getSimpleName();
    private static final String TRANSLATION_Y = "translationY";
    private static final String TRANSLATION_X = "translationX";

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseFirestore mDb;
    private FirebaseUser user;

    private static final int NUM_ROUND = 2;
    private static final int NUM_LEVEL = 5;
//    private static final int ROUND_DURATION = 40;
    private static final int ROUND_DURATION = 20;
    private static final int DEFAULT_SPECIAL_CUE_NUM = 2;
    private static final int DEFAULT_APPEAR_TIME = 1500;
    private static final int DEFAULT_ISI = 500;
    private int mGameProgress;

    private Button mStartButton;
    private TextView mExeInstr1;
    private TextView mExeInstr2;
    private ImageView mExeInstrImg1;
    private ImageView mExeInstrImg2;
    private ImageView mCue;
    private TextView mFeedback;
    private TextView mXpText;
    private ProgressBar mXpBar;

    private final int[] mGoCue = {R.drawable.healthy_apple, R.drawable.healthy_asparagus,
            R.drawable.healthy_avocado, R.drawable.healthy_banana, R.drawable.healthy_berries,
            R.drawable.healthy_broc, R.drawable.healthy_brocolli, R.drawable.healthy_carrot,
            R.drawable.healthy_chicken, R.drawable.healthy_egg, R.drawable.healthy_grain_bread,
            R.drawable.healthy_grapes, R.drawable.healthy_kiwi, R.drawable.healthy_nuts,
            R.drawable.healthy_salad, R.drawable.healthy_salad2, R.drawable.healthy_salad3,
            R.drawable.healthy_salad4, R.drawable.healthy_salmon, R.drawable.healthy_spinach,
            R.drawable.healthy_sprouts, R.drawable.healthy_squash, R.drawable.healthy_strawberry,
            R.drawable.healthy_zuc};
    private final int[] mNoGoCue = {R.drawable.unhealthy_burger, R.drawable.unhealthy_cake1,
            R.drawable.unhealthy_cake2, R.drawable.unhealthy_candy1, R.drawable.unhealthy_candy2,
            R.drawable.unhealthy_candy3, R.drawable.unhealthy_chickensandwich, R.drawable.unhealthy_chips,
            R.drawable.unhealthy_choco1, R.drawable.unhealthy_choco2, R.drawable.unhealthy_choco3,
            R.drawable.unhealthy_cookie1, R.drawable.unhealthy_cookie2, R.drawable.unhealthy_cookie3,
            R.drawable.unhealthy_cookie4, R.drawable.unhealthy_cookie5, R.drawable.unhealthy_fishnchips,
            R.drawable.unhealthy_friedchicken, R.drawable.unhealthy_fries, R.drawable.unhealthy_icecream1,
            R.drawable.unhealthy_icecream2, R.drawable.unhealthy_icecream3, R.drawable.unhealthy_icecream4,
            R.drawable.unhealthy_icecream5, R.drawable.unhealthy_nachos, R.drawable.unhealthy_pizza1,
            R.drawable.unhealthy_pizza2, R.drawable.unhealthy_snickers, R.drawable.unhealthy_twinkie};
    private final int[] mCoinCue = {R.drawable.coin};
    private final int[] mBombCue = {R.drawable.bomb};
    private final int[] mBellCue = {R.drawable.bell};

    private int mParentWidth;
    private int mParentHeight;

    private int mRound;
    private long mNumCoins;
    private int mLevel;
    private long mScore;
    private boolean mGoChosen;
    private boolean mSpecialCue;
    private boolean mIsBell;

    private int mNumHealthyFoodCues;
    private int mNumUnhealthyFoodCues;
    private int mNumCoinCues;
    private int mNumBombCues;
    private int mNumBellCues;

    private int mTotalResponseTime;
    private int mCurrentResponseTime;
    private int mNumClicks;

    private int mAppearTime;
    private int mISI;

    private String mTranslation;
    private MediaPlayer mCoinCollectedSound;
    private MediaPlayer mCoinMissedSound;
    private MediaPlayer mExplosionSound;
    private boolean mSoundEnabled;
    private boolean mIsInstrShown;

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    private Spinner mSelectLevelSpinner;
    private boolean mLevelSelected;

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
        mTranslation = TRANSLATION_Y;
        mStartButton = findViewById(R.id.start_exercise_button);
        mExeInstr1 = findViewById(R.id.exerInstruction1);
        mExeInstr2 = findViewById(R.id.exerInstruction2);
        mExeInstrImg1 = findViewById(R.id.exeInstrImage1);
        mExeInstrImg2 = findViewById(R.id.exeInstrImage2);
        mFeedback = findViewById(R.id.feedback_text);

        mXpText = findViewById(R.id.exercise_xp_text);
        mXpBar = findViewById(R.id.xp_bar);
        setXpBar();

        mGameProgress = ROUND_DURATION;
        mRound = 0;
        mTotalResponseTime = 0;
        mNumClicks = 0;

        mSoundEnabled = true;
        mCoinCollectedSound = MediaPlayer.create(getApplicationContext(), R.raw.coin_collect);
        mCoinMissedSound = MediaPlayer.create(getApplicationContext(), R.raw.coin_miss);
        mExplosionSound = MediaPlayer.create(getApplicationContext(), R.raw.explosion);

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                if (mIsBell && count > 0)
                {
                    countClickHelper();
                    mIsBell = false;
                }
            }
        });

        mLevelSelected = false;
        mSelectLevelSpinner = findViewById(R.id.select_level_spinner);
        mSelectLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                int level = Integer.parseInt(mSelectLevelSpinner.getSelectedItem().toString());
                if (level != 0)
                {
                    mLevel = level;
                    mLevelSelected = true;
                    setXpBarHelper(0);
                    mSelectLevelSpinner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        BackgroundMusic.getInstance(this).start();
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

    public void startButtonOnClick(View view) {
        Toast.makeText(this, "Start button clicked", Toast.LENGTH_LONG);
        mRound = mRound%NUM_ROUND + 1;
        Log.d(LOG_TAG, "start button clicked, current round: "+mRound+", level "+mLevel);
        mAppearTime = DEFAULT_APPEAR_TIME;
        switch (mLevel)
        {
            case 1:
                mNumCoinCues = 0;
                mNumBombCues = 0;
                mNumBellCues = 0;
                if (mRound == 2) {
                    if (mIsInstrShown) {
                        mIsInstrShown = false;
                        startExercise(view);
                    } else {
                        //TODO display moving instruction
                        mRound = 1;
                        mExeInstr1.setText("Beware that images are\ngoing to fall down");
                        mExeInstrImg1.setImageResource(R.drawable.healthy_apple);
                        mExeInstrImg2.setImageResource(R.drawable.unhealthy_burger);
                        mExeInstr2.setText("Click on them before\nthey reach the bottom");
                        mExeInstr1.setVisibility(view.VISIBLE);
                        mExeInstr2.setVisibility(view.VISIBLE);
                        mExeInstrImg1.setVisibility(view.VISIBLE);
                        mExeInstrImg2.setVisibility(view.VISIBLE);
                        mIsInstrShown = true;
                    }
                }else {
                    startExercise(view);
                }
                break;
            case 2:
                if (mRound == 2) {
                    if (mIsInstrShown) {
                        mIsInstrShown = false;
                        startExercise(view);
                    } else {
                        //TODO display faster falling instruction
                        mRound = 1;
                        mExeInstr1.setText("Beware that images are\ngoing to fall FASTER");
                        mExeInstrImg1.setImageResource(R.drawable.healthy_apple);
                        mExeInstrImg2.setImageResource(R.drawable.unhealthy_burger);
                        mExeInstr2.setText("Click on them before\nthey reach the bottom");
                        mExeInstr1.setVisibility(view.VISIBLE);
                        mExeInstr2.setVisibility(view.VISIBLE);
                        mExeInstrImg1.setVisibility(view.VISIBLE);
                        mExeInstrImg2.setVisibility(view.VISIBLE);
                        mIsInstrShown = true;
                    }
                }else {
                    startExercise(view);
                }
                break;
            case 3:
                mNumCoinCues = DEFAULT_SPECIAL_CUE_NUM;
                mNumBombCues = DEFAULT_SPECIAL_CUE_NUM;
                mNumBellCues = 0;
                if (mRound == 1) {
                    if (mIsInstrShown) {
                        mIsInstrShown = false;
                        startExercise(view);
                    } else {
                        //TODO display coin and bomb instruction
                        mRound = 2;
                        mExeInstr1.setText("Bombs and coins are\nadded to this level");
                        mExeInstrImg1.setImageResource(R.drawable.bomb);
                        mExeInstrImg2.setImageResource(R.drawable.coin);
                        mExeInstr2.setText("Clink on coins and\navoid bombs");
                        mExeInstr1.setVisibility(view.VISIBLE);
                        mExeInstr2.setVisibility(view.VISIBLE);
                        mExeInstrImg1.setVisibility(view.VISIBLE);
                        mExeInstrImg2.setVisibility(view.VISIBLE);
                        mIsInstrShown = true;
                    }
                }else {
                    startExercise(view);
                }
                break;
            case 4:
                if (mRound == 2) {
                    if (mIsInstrShown) {
                        mIsInstrShown = false;
                        startExercise(view);
                    } else {
                        //TODO display horrizontal instruction
                        mRound = 1;
                        mExeInstr1.setText("Beware that these cues\nwill move HORIZONTALLY");
                        mExeInstrImg1.setImageResource(R.drawable.bomb);
                        mExeInstrImg2.setImageResource(R.drawable.coin);
                        mExeInstr2.setText("Click on them before\nthey reach the edge");
                        mExeInstr1.setVisibility(view.VISIBLE);
                        mExeInstr2.setVisibility(view.VISIBLE);
                        mExeInstrImg1.setVisibility(view.VISIBLE);
                        mExeInstrImg2.setVisibility(view.VISIBLE);
                        mIsInstrShown = true;
                    }
                }else {
                    startExercise(view);
                }
                break;
            case 5:
                mNumCoinCues = DEFAULT_SPECIAL_CUE_NUM;
                mNumBombCues = DEFAULT_SPECIAL_CUE_NUM;
                mNumBellCues = DEFAULT_SPECIAL_CUE_NUM;
                Log.d(LOG_TAG, "Got to level 5, round "+mRound);
                if (mRound == 1) {
                    if (mIsInstrShown) {
                        mIsInstrShown = false;
                        startExercise(view);
                    } else {
                        //TODO display bell instruction
                        mRound = 2;
                        Log.d(LOG_TAG, "Got to level 5 instruction");
                        mExeInstr1.setText("Bell cue is added");
                        mExeInstrImg1.setImageResource(R.drawable.bell);
                        mExeInstrImg2.setImageResource(R.drawable.shake_phone);
                        mExeInstr2.setText("When it shows up\nshake your phone to\nearn coins");
                        mExeInstr1.setVisibility(view.VISIBLE);
                        mExeInstr2.setVisibility(view.VISIBLE);
                        mExeInstrImg1.setVisibility(view.VISIBLE);
                        mExeInstrImg2.setVisibility(view.VISIBLE);
                        mIsInstrShown = true;
                    }
                } else {
                    startExercise(view);
                }
                break;
        }

    }

    public void startExercise(View view) {
        mGameProgress = 1;
        if (mRound <= NUM_ROUND)
        {
            mNumHealthyFoodCues = (ROUND_DURATION - mNumCoinCues - mNumBombCues - mNumBellCues)/2;
            mNumUnhealthyFoodCues = mNumHealthyFoodCues;
            if (!mStartButton.getText().equals(getString(R.string.round_transition_text)))
            {
                BackgroundMusic.getInstance(getApplicationContext()).switchBGM();
            }
            mStartButton.setVisibility(View.GONE);
            mExeInstrImg1.setVisibility(View.GONE);
            mExeInstrImg2.setVisibility(View.GONE);
            mExeInstr1.setVisibility(View.GONE);
            mExeInstr2.setVisibility(View.GONE);
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
                    if (!mSpecialCue)
                    {
                        updateCoinsCounter(!mGoChosen, mSpecialCue);
                    }
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
                        case 5:
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
                        Toast.makeText(ExerciseActivity.this, "Average Response Time = " + (mNumClicks!=0? String.valueOf(mTotalResponseTime/mNumClicks) : "No clicks done"),
                                Toast.LENGTH_SHORT).show();
                        BackgroundMusic.getInstance(getApplicationContext()).switchBGM();
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
                ObjectAnimator animation;

                // animation direction
                switch (mLevel)
                {
                    case 1:
                    case 2:
                    case 3:
                    default:
                        animation = ObjectAnimator.ofFloat(mCue, TRANSLATION_Y, mParentHeight);
                        break;

                    case 4:
                    case 5:
                        if (mTranslation.equals(TRANSLATION_Y))
                        {
                            animation = ObjectAnimator.ofFloat(mCue, TRANSLATION_Y, mParentHeight);
                        }
                        else
                        {
                            mCue.setX(0);
                            mCue.setY(randomInRange(0, mParentHeight));
                            animation = ObjectAnimator.ofFloat(mCue, TRANSLATION_X, mParentWidth);
                        }
                        break;
                }

                // animation style
                switch (mLevel)
                {
                    case 1:
                    default:
                        animation.setInterpolator(new LinearInterpolator());
                        break;

                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        animation.setInterpolator(new AccelerateInterpolator(0.4f + 0.3f * mLevel));
                        break;
                }
                animation.setDuration(mAppearTime);
                animation.start();
                break;
            default:
                break;
        }
        mCue.setVisibility(View.VISIBLE);
    }
    private int[] chooseGoCue() {
        int sum = mNumHealthyFoodCues + mNumUnhealthyFoodCues + mNumCoinCues + mNumBombCues + mNumBellCues;
        double healthyFoodThreshold = ((double)mNumHealthyFoodCues) / ((double)sum);
        double bellThreshold = ((double)mNumBellCues) / ((double)sum) + healthyFoodThreshold;
        double unhealthyFoodThreshold = ((double)mNumUnhealthyFoodCues) / ((double)sum) + bellThreshold;
        double coinThreshold = ((double)mNumCoinCues) / ((double)sum) + unhealthyFoodThreshold;
        double randomNum = Math.random();
        mTranslation = TRANSLATION_Y;
        if (randomNum < healthyFoodThreshold)
        {
            mCue.setBackgroundResource(R.drawable.green_border_background);
            mGoChosen = true;
            mSpecialCue = false;
            mIsBell = false;
            mNumHealthyFoodCues--;
            return mGoCue;
        }
        else if (randomNum < bellThreshold)
        {
            mCue.setBackgroundResource(0);
            mGoChosen = true;
            mSpecialCue = true;
            mIsBell = true;
            mNumBellCues--;
            mTranslation = TRANSLATION_X;
            return mBellCue;
        }
        else if (randomNum < unhealthyFoodThreshold)
        {
            mCue.setBackgroundResource(R.drawable.red_border_background);
            mGoChosen = false;
            mSpecialCue = false;
            mIsBell = false;
            mNumUnhealthyFoodCues--;
            return mNoGoCue;
        }
        else if (randomNum < coinThreshold)
        {
            mCue.setBackgroundResource(0);
            mGoChosen = true;
            mSpecialCue = true;
            mIsBell = false;
            mNumCoinCues--;
            mTranslation = TRANSLATION_X;
            return mCoinCue;
        }
        else
        {
            mCue.setBackgroundResource(0);
            mGoChosen = false;
            mSpecialCue = true;
            mIsBell = false;
            mNumBombCues--;
            mTranslation = TRANSLATION_X;
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

    private void updateCoinsCounter(boolean increment, boolean special) {
        int stride = special?5:1;
        if (increment)
        {
            mNumCoins+=stride;
            mScore+=stride;
            mFeedback.setText(getString(R.string.increment_feedback_text, stride));
            mFeedback.setTextColor(getColor(R.color.green));
            replaySound(mCoinCollectedSound);
        }
        else
        {
            mNumCoins-=stride;
            mScore-=stride;
            mFeedback.setText(getString(R.string.decrement_feedback_text, stride));
            mFeedback.setTextColor(getColor(R.color.red));
            if (!special)
            {
                replaySound(mCoinMissedSound);
            }
            else
            {
                replaySound(mExplosionSound);
            }
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
        if (!mIsBell)
        {
            countClickHelper();
        }
    }

    private void countClickHelper()
    {
        mCue.setVisibility(View.INVISIBLE);
        mTotalResponseTime += mCurrentResponseTime;
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
        if (!mLevelSelected)
        {
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
        else
        {
            mXpText.setText(getString(R.string.level_template, mLevel));
            mXpBar.setProgress(120);
        }
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
        else if (id == R.id.sound_toggle_action) {
            mSoundEnabled = !mSoundEnabled;
            if (mSoundEnabled)
            {
                item.setIcon(R.drawable.unmute);
            }
            else
            {
                item.setIcon(R.drawable.mute);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void replaySound(MediaPlayer mp)
    {
        if (mSoundEnabled)
        {
            mp.stop();
            try
            {
                mp.prepare();
            }
            catch (Exception e){}
            mp.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BackgroundMusic.getInstance(this).start();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        BackgroundMusic.getInstance(this).pause();
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (mGameProgress>=ROUND_DURATION) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (mGameProgress>=ROUND_DURATION) {
            return super.onSupportNavigateUp();
        }
        return false;
    }

    public void toggleSelectLevel(View view) {
        if (mSelectLevelSpinner.getVisibility() == View.VISIBLE) {
            mSelectLevelSpinner.setVisibility(View.GONE);
        } else if (mGameProgress>=ROUND_DURATION){
            mSelectLevelSpinner.setVisibility(View.VISIBLE);
        }
    }
}