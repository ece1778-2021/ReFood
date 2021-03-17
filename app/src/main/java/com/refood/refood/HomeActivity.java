package com.refood.refood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Text;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

public class HomeActivity extends AppCompatActivity {

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();
    private static final int REQUEST_PROFILE_LOGOUT = 1;
    private static final int REQUEST_EXERCISE_UPDATE = 2;

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseFirestore mDb;
    private FirebaseUser user;

    private long mNumCoins;
    private boolean isChecked;
    private long mHour;
    private long mMinute;

    private TextView mStreak;
    private int numStreak;
    private Long daysPlayed;

    private ImageView leftButton, rightButton, bottomButton, logo;
    private TextView leftText, rightText, instruction;
    private Button nextTip;

    private int tipProgress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        leftButton = findViewById(R.id.zem_garden_button);
        rightButton = findViewById(R.id.exercise_button);
        bottomButton = findViewById(R.id.stats_button);
        logo = findViewById(R.id.logo_image);
        leftText = findViewById(R.id.go_zemgarden_text);
        rightText = findViewById(R.id.go_exercise_text);
        instruction = findViewById(R.id.instructionBox);
        nextTip = findViewById(R.id.nextTip);

        instruction.setVisibility(View.GONE);
        nextTip.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDb = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        mStreak = (TextView)findViewById(R.id.day_streak_text);
        populateInfo();

        createNotificationChannel();
        init();
    }

    private void init() {
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

                            isChecked = (boolean)(userStore.get("notificationEnabled"));
                            mHour = (long)(userStore.get("notificationHour"));
                            mMinute = (long)(userStore.get("notificationMinute"));
                            setAlarm(isChecked, mHour, mMinute);

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

    private void setAlarm(boolean isChecked, long hour, long minute){
        if (!isChecked){ return; }

        AlarmManager alarmMgr;
        PendingIntent alarmIntent;
        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderBroadcast.class);
        alarmIntent = PendingIntent.getBroadcast(this, 2, intent, 0);

        boolean alarmUp = (PendingIntent.getBroadcast(this, 0,
                new Intent(getApplicationContext(), ReminderBroadcast.class),
                PendingIntent.FLAG_NO_CREATE) != null);
        if (alarmUp)
        {
            return;
        }

        Toast.makeText(this, "Alarm set"+mHour+':'+mMinute, Toast.LENGTH_LONG).show();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, (int) hour);
        calendar.set(Calendar.MINUTE, (int) minute);
        calendar.set(Calendar.SECOND, 0);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "alarmChannel";
            String description = "alarm channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("alarmNotify", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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
        intent.putExtra("daysPlayed", daysPlayed);
        intent.putExtra("streak", numStreak);
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

    private void populateInfo(){
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

                            ArrayList<Long> scoreList = (ArrayList<Long>) (userStore.get("scoreHistory"));
                            ArrayList<String> dateList = (ArrayList<String>) userStore.get("exerciseHistory");
                            ArrayList<Entry> entryList = new ArrayList<>();
                            ArrayList<String> xLabel = new ArrayList<>();
                            for (int i=0; i<dateList.size(); i++) {
                                String date = dateList.get(i);
                                Log.d(LOG_TAG, "Timestamp data: " + date);
                                entryList.add(new Entry(i, scoreList.get(i)));
                            }

                            setText(dateList);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setText(ArrayList<String> dateList){
//        boolean streakBroken = false;
        int m1, d1, y1, m2, d2, y2;
        numStreak = 0;
        Calendar firstDay = Calendar.getInstance();
        if (dateList.isEmpty()) {
            mStreak.setText(getString(R.string.day_streak_template, numStreak));
            return;
        } else{
            numStreak=1;
            int i = dateList.size()-1;
            String date = dateList.get(i);
            String delims = "/";
            String[] tokens = date.split(delims);

            m1 = Integer.parseInt(tokens[0]);
            d1 = Integer.parseInt(tokens[1]);
            y1 = Integer.parseInt(tokens[2]);

            i--;
            while (i>=0){
                date = dateList.get(i);
                delims = "/";
                tokens = date.split(delims);

                m2 = Integer.parseInt(tokens[0]);
                d2 = Integer.parseInt(tokens[1]);
                y2 = Integer.parseInt(tokens[2]);

                if (compareCalendar(y1, m1, d1, y2, m2, d2)==0){
                    i--;
                    continue;
                } else if (compareCalendar(y1, m1, d1, y2, m2, d2)==1){
                    numStreak++;
                    m1 = m2;
                    d1 = d2;
                    y1 = y2;
                    i--;
                } else {
                    break;
                }
            }
        }
//        mStreak.setText(String.valueOf(numStreak));
        mStreak.setText(getString(R.string.day_streak_template, numStreak));

        String date = dateList.get(0);
        String delims = "/";
        String[] tokens = date.split(delims);

        m1 = Integer.parseInt(tokens[0]);
        d1 = Integer.parseInt(tokens[1]);
        y1 = Integer.parseInt(tokens[2]);
        firstDay.set(y1, m1, d1);
        Log.d(LOG_TAG, "firstday: "+firstDay);
        Log.d(LOG_TAG, "current instance: "+Calendar.getInstance().toString());
        daysPlayed = (Long) ChronoUnit.DAYS.between(firstDay.toInstant(), Calendar.getInstance().toInstant())+1;
    }

    private static int compareCalendar(int y1, int m1, int d1, int year, int month, int day){
        Log.d(LOG_TAG, "arguments: "+y1+'/'+m1+'/'+d1+'/'+year+'/'+month+'/'+day);

        if (y1 == year && m1 == month && d1 == day){
            Log.d(LOG_TAG, "returned 0");
            return 0;
        } else {
            if (y1 == year && m1 == month && d1 == day+1){
                Log.d(LOG_TAG, "returned 1");
                return 1;
            } else {
                Log.d(LOG_TAG, "returned -1");
                return -1;
            }
        }
    }

    public void tipOnClick(View view) {
        if (tipProgress>0){return;}

        tipProgress++;

        changeVerticalBias(leftButton, 0.15f);
        changeVerticalBias(rightButton, 0.15f);
        changeVerticalBias(leftText, 0.15f);
        changeVerticalBias(rightText, 0.15f);
        changeVerticalBias(logo, 0.15f);

        instruction.setBackgroundResource(R.drawable.orange_border_background);
        instruction.setVisibility(View.VISIBLE);
        nextTip.setVisibility(View.VISIBLE);
    }

    public void nextTipOnClick(View view) {
        switch (tipProgress)
        {
            case 1:
                instruction.setText("Press \"No-Go Training\" to\n exercise and earn coins");
                rightButton.setBackgroundResource(R.drawable.orange_border_background);
                tipProgress++;
                break;
            case 2:
                instruction.setText("The exercise has 2 rounds.\nEarn XP to level up and \nincrease its difficulty");
                tipProgress++;
                break;
            case 3:
                instruction.setText("Press \"Stats\" to\nview progress");
                rightButton.setBackgroundResource(0);
                bottomButton.setBackgroundResource(R.drawable.orange_border_background);
                tipProgress++;
                break;
            case 4:
                instruction.setText("Press \"Zem Garden\" to\nspend coins you earn");
                bottomButton.setBackgroundResource(0);
                leftButton.setBackgroundResource(R.drawable.orange_border_background);
                tipProgress++;
                break;
            case 5:
                instruction.setVisibility(View.GONE);
                nextTip.setVisibility(View.GONE);
                leftButton.setBackgroundResource(0);
                tipProgress = 0;

                changeVerticalBias(leftButton, 0.3f);
                changeVerticalBias(rightButton, 0.3f);
                changeVerticalBias(leftText, 0.3f);
                changeVerticalBias(rightText, 0.3f);
                changeVerticalBias(logo, 0.3f);
        }

    }

    private void changeVerticalBias(View view, float newBias){
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        params.verticalBias = newBias;
        view.setLayoutParams(params);
    }
}