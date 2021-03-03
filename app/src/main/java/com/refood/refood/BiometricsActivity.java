package com.refood.refood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

public class BiometricsActivity extends AppCompatActivity {

    private static final String LOG_TAG = BiometricsActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;

    NumberPicker mAgePicker;
    Spinner mSexSpinner;
    NumberPicker mWeightPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometrics);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        mAgePicker = findViewById(R.id.numberPickerBioAge);
        mSexSpinner = findViewById(R.id.spinnerBioSex);
        mWeightPicker = findViewById(R.id.numberPickerBioWeight);

        mAgePicker.setMinValue(0);
        mAgePicker.setMaxValue(200);
        mWeightPicker.setMinValue(0);
        mWeightPicker.setMaxValue(1000);
    }

    public void SaveBiometrics(View view) {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null)
        {
            String uid = user.getUid();

            Map<String, Object> userStore = new HashMap<>();
            userStore.put("age", mAgePicker.getValue());
            userStore.put("gender", mSexSpinner.getSelectedItem());
            userStore.put("weight", mWeightPicker.getValue());

            mDb.collection("users").document(uid)
                    .set(userStore, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(LOG_TAG, "biometrics successfully written!");
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(LOG_TAG, "Error writing biometrics", e);
                        }
                    });
        }
    }
}