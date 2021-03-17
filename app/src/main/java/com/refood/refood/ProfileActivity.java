package com.refood.refood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String LOG_TAG = ProfileActivity.class.getSimpleName();
    private static final int REQUEST_WEEKLY_SURVEY = 1;
    private static final int SURVEY_INTERVAL = 7;

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseFirestore mDb;

    private TextView mTextNextSurvey;
    private Button mButtonDoSurvey;
    private TimePicker mTimePicker;
    private Button mButtonSaveTime;
    private TextView mTextToggleTimePicker;
    private ImageView mImageToggleTimePicker;
    private TextView mNotificationTime;
    private Switch mSwitch;
    private boolean isChecked;

    private long mHour;
    private long mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDb = FirebaseFirestore.getInstance();

        mTextNextSurvey = findViewById(R.id.textProfileNextSurvey);
        mButtonDoSurvey = findViewById(R.id.buttonDoSurvey);
        mTimePicker = findViewById(R.id.notification_timepicker);
        mButtonSaveTime = findViewById(R.id.buttonSaveTime);
        mTextToggleTimePicker = findViewById(R.id.textToggleTimePicker);
        mImageToggleTimePicker = findViewById(R.id.imageToggleTimePicker);
        mNotificationTime = findViewById(R.id.textNotificationTime);
        mSwitch = findViewById(R.id.notification_switch);

        Context context = this;
        createNotificationChannel();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            ((TextView) findViewById(R.id.textProfileEmail)).setText(user.getEmail());

            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> userStore = document.getData();
                            Log.d(LOG_TAG, "DocumentSnapshot data: " + userStore);

                            ((TextView) findViewById(R.id.textProfileUsername)).setText((String)userStore.get("username"));
                            boolean notificationEnabled = (boolean)userStore.get("notificationEnabled");
                            mSwitch.setChecked(notificationEnabled);
                            mSwitch.setOnCheckedChangeListener((ProfileActivity)context);
                            isChecked = notificationEnabled;

                            mHour = (long)userStore.get("notificationHour");
                            mMinute = (long)userStore.get("notificationMinute");
                            mTimePicker.setHour((int)mHour);
                            mTimePicker.setMinute((int)mMinute);
                            showTime();

                        } else {
                            Log.d(LOG_TAG, "No such document");
                        }
                    } else {
                        Log.d(LOG_TAG, "get failed with ", task.getException());
                    }
                }
            });

            updateNextSurveyDate();
        }
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.menu is a reference to an xml file named menu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout_action) {
            FirebaseAuth.getInstance().signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setTime(View view) {
        mHour = mTimePicker.getHour();
        mMinute = mTimePicker.getMinute();
        if (isChecked){
            setAlarm(true, mHour, mMinute);
        }

        FirebaseUser user = mAuth.getCurrentUser();
        Map<String, Object> data = new HashMap<>();
        data.put("notificationHour", mHour);
        data.put("notificationMinute", mMinute);
        if (user != null) {
            String uid = user.getUid();
            mDb.collection("users").document(uid)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(LOG_TAG, "Notification time successfully updated in FireStore!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(LOG_TAG, "Error writing document", e);
                        }
                    });
        }
        showTime();
    }

    public void showTime() {
        String format = "";
        if (mHour == 0) {
            mHour += 12;
            format = "AM";
        } else if (mHour == 12) {
            format = "PM";
        } else if (mHour > 12) {
            mHour -= 12;
            format = "PM";
        } else {
            format = "AM";
        }

        mNotificationTime.setText(new StringBuilder().append(mHour).append(" : ").append(mMinute)
                .append(" ").append(format));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        this.isChecked = isChecked;
        if (isChecked){
            setAlarm(isChecked, mHour, mMinute);
        }

        FirebaseUser user = mAuth.getCurrentUser();
        Map<String, Object> data = new HashMap<>();
        data.put("notificationEnabled", isChecked);
        if (user != null) {
            String uid = user.getUid();
            mDb.collection("users").document(uid)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(LOG_TAG, "Notification enabled/disabled successfully updated in FireStore!");
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

    private void setAlarm(boolean isChecked, long hour, long minute){
        if (!isChecked){ return; }
        Toast.makeText(this, "Alarm set at "+mHour+':'+mMinute, Toast.LENGTH_LONG).show();

        AlarmManager alarmMgr;
        PendingIntent alarmIntent;
        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), ReminderBroadcast.class);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, (int) hour);
        calendar.set(Calendar.MINUTE, (int) minute);
        calendar.set(Calendar.SECOND, 0);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, alarmIntent);
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

    public void doSurvey(View view) {
        Intent intent = new Intent(this, com.refood.refood.SurveyActivity.class);
        startActivityForResult(intent, REQUEST_WEEKLY_SURVEY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_WEEKLY_SURVEY)
        {
            updateNextSurveyDate();
        }
    }

    private void updateNextSurveyDate() 
    {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null)
        {
            String uid = user.getUid();
            mDb.collection("surveys")
                    .whereEqualTo("userSerialNumber", com.refood.refood.SurveyActivity.md5(uid))
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(LOG_TAG, document.getId() + " => " + document.getData());
                                    Map<String, Object> surveyStore = document.getData();
                                    String timestamp = (String)surveyStore.get("timestamp");
                                    int differenceDays = getTimeDifferenceInDays(timestamp);
                                    if (differenceDays >= SURVEY_INTERVAL)
                                    {
                                        showSurveyButton();
                                    }
                                    else
                                    {
                                        mTextNextSurvey.setText(getString(R.string.next_survey_placeholder, SURVEY_INTERVAL-differenceDays));
                                        mButtonDoSurvey.setVisibility(View.INVISIBLE);
                                    }
                                }
                            } else {
                                Log.d(LOG_TAG, "Error getting documents: ", task.getException());
                                showSurveyButton();
                            }
                        }
                    });
        }
    }
    
    private void showSurveyButton()
    {
        mTextNextSurvey.setText("");
        mButtonDoSurvey.setVisibility(View.VISIBLE);
    }

    public static int getTimeDifferenceInDays(String timestamp) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
            Date current = new Date();
            Date timestampDate = format.parse(timestamp);
            long differenceMs = current.getTime() - timestampDate.getTime();
            int differenceDays = (int) (differenceMs / (1000*60*60*24));
            return differenceDays;
        } catch (Exception e) {
            Log.d(LOG_TAG, "getTimeDifferenceInDays Exception: ", e);
            return 0;
        }
    }

    public void doPasswordReset(View view) {
        String email = mAuth.getCurrentUser().getEmail();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(LOG_TAG, "Email sent.");
                            Toast.makeText(ProfileActivity.this, "Password Reset Email is sent", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void toggleTimePicker(View view) {
        if (mTimePicker.getVisibility() == View.GONE)
        {
            mImageToggleTimePicker.setRotation(180);
            mTextToggleTimePicker.setText(R.string.collapse_time_picker);
            mTimePicker.setVisibility(View.VISIBLE);
            mButtonSaveTime.setVisibility(View.VISIBLE);
        }
        else
        {
            mImageToggleTimePicker.setRotation(0);
            mTextToggleTimePicker.setText(R.string.set_notification_time);
            mTimePicker.setVisibility(View.GONE);
            mButtonSaveTime.setVisibility(View.GONE);
        }
    }
}