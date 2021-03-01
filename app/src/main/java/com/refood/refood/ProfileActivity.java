package com.refood.refood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.refood.refood.R;
import com.refood.refood.RegisterActivity;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String LOG_TAG = ProfileActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseFirestore mDb;

    private TimePicker mTimePicker;
    private TextView mNotificationTime;
    private Switch mSwitch;

    private long mHour;
    private long mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDb = FirebaseFirestore.getInstance();

        mTimePicker = findViewById(R.id.notification_timepicker);
        mNotificationTime = findViewById(R.id.textNotificationTime);
        mSwitch = findViewById(R.id.notification_switch);

        Context context = this;

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
}