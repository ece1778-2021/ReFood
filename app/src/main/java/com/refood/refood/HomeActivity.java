package com.refood.refood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDb = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        // TODO: get the actual dayStreak
        int dayStreak = 5;
        ((TextView)findViewById(R.id.day_streak_text)).setText(getString(R.string.day_streak_template, dayStreak));

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