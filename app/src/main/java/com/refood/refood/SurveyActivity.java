package com.refood.refood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SurveyActivity extends AppCompatActivity {

    private static final String LOG_TAG = SurveyActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    private String mQ1Answer;
    private String mQ2Answer;
    private String mQ3Answer;
    private String mQ4Answer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        mQ1Answer = "";
        mQ2Answer = "";
        mQ3Answer = "";
        mQ4Answer = "";
    }

    public void saveSurveyResults(View view)
    {
        if (com.refood.refood.MainActivity.isEmptyString(mQ1Answer) ||
                com.refood.refood.MainActivity.isEmptyString(mQ2Answer) ||
                com.refood.refood.MainActivity.isEmptyString(mQ3Answer) ||
                com.refood.refood.MainActivity.isEmptyString(mQ4Answer))
        {
            Toast.makeText(SurveyActivity.this, "Please fill in the survey",
                    Toast.LENGTH_SHORT).show();
        }
        else
        {
            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null)
            {
                String uid = user.getUid();

                Map<String, Object> surveyStore = new HashMap<>();
                surveyStore.put("userSerialNumber", md5(uid));
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                surveyStore.put("timestamp", timeStamp);
                surveyStore.put("q1Answer", mQ1Answer);
                surveyStore.put("q2Answer", mQ2Answer);
                surveyStore.put("q3Answer", mQ3Answer);
                surveyStore.put("q4Answer", mQ4Answer);

                mDb.collection("surveys").add(surveyStore);
                finish();
            }
        }
    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.q1_a:
                if (checked)
                    mQ1Answer = getResources().getString(R.string.strongly_disagree);
                    break;
            case R.id.q1_b:
                if (checked)
                    mQ1Answer = getResources().getString(R.string.disagree);
                    break;
            case R.id.q1_c:
                if (checked)
                    mQ1Answer = getResources().getString(R.string.neutral);
                break;
            case R.id.q1_d:
                if (checked)
                    mQ1Answer = getResources().getString(R.string.agree);
                break;
            case R.id.q1_e:
                if (checked)
                    mQ1Answer = getResources().getString(R.string.strongly_agree);
                break;
            case R.id.q2_a:
                if (checked)
                    mQ2Answer = getResources().getString(R.string.strongly_disagree);
                break;
            case R.id.q2_b:
                if (checked)
                    mQ2Answer = getResources().getString(R.string.disagree);
                break;
            case R.id.q2_c:
                if (checked)
                    mQ2Answer = getResources().getString(R.string.neutral);
                break;
            case R.id.q2_d:
                if (checked)
                    mQ2Answer = getResources().getString(R.string.agree);
                break;
            case R.id.q2_e:
                if (checked)
                    mQ2Answer = getResources().getString(R.string.strongly_agree);
                break;
            case R.id.q3_a:
                if (checked)
                    mQ3Answer = getResources().getString(R.string.strongly_disagree);
                break;
            case R.id.q3_b:
                if (checked)
                    mQ3Answer = getResources().getString(R.string.disagree);
                break;
            case R.id.q3_c:
                if (checked)
                    mQ3Answer = getResources().getString(R.string.neutral);
                break;
            case R.id.q3_d:
                if (checked)
                    mQ3Answer = getResources().getString(R.string.agree);
                break;
            case R.id.q3_e:
                if (checked)
                    mQ3Answer = getResources().getString(R.string.strongly_agree);
                break;
            case R.id.q4_a:
                if (checked)
                    mQ4Answer = getResources().getString(R.string.strongly_disagree);
                break;
            case R.id.q4_b:
                if (checked)
                    mQ4Answer = getResources().getString(R.string.disagree);
                break;
            case R.id.q4_c:
                if (checked)
                    mQ4Answer = getResources().getString(R.string.neutral);
                break;
            case R.id.q4_d:
                if (checked)
                    mQ4Answer = getResources().getString(R.string.agree);
                break;
            case R.id.q4_e:
                if (checked)
                    mQ4Answer = getResources().getString(R.string.strongly_agree);
                break;
        }

    }
}