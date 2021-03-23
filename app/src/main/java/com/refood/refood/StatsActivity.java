package com.refood.refood;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class StatsActivity extends AppCompatActivity{

    private static final String LOG_TAG = StatsActivity.class.getSimpleName();

    LineChart mChart;
    TextView mStreak;
    TextView mDaysPlayed;

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseFirestore mDb;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDb = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        mChart = (LineChart) findViewById(R.id.chart);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(false);

        mDaysPlayed = (TextView) findViewById(R.id.textView4);
        mStreak = (TextView) findViewById(R.id.textView5);

        Intent intent = getIntent();
        mDaysPlayed.setText(String.valueOf(intent.getLongExtra("daysPlayed", 0)));
        mStreak.setText(String.valueOf(intent.getIntExtra("streak", 0)));

        populateInfo();
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
                            for (int i=dateList.size()-5; i<dateList.size(); i++) {
                                String date = dateList.get(i);
                                Log.d(LOG_TAG, "Timestamp data: " + date);
//                                xLabel.add(date);
//                                xLabel.add("Mar 24");
                                entryList.add(new Entry(i, scoreList.get(i).intValue()));
                            }

                            setChart(entryList);
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

    private void setChart(ArrayList<Entry> entryList){

        LineDataSet set1 = new LineDataSet(entryList, "Data Set 1");

        set1.setFillAlpha(110);
        set1.setColor(Color.LTGRAY);
        set1.setLineWidth(3f);
        set1.setValueTextSize(18);
        set1.setCircleRadius(6f);
        set1.setCircleColor(0xFFFF8800);
        set1.setLabel("Score History");

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        LineData data = new LineData(dataSets);

        mChart.setData(data);
        mChart.invalidate();

        Description desc = new Description();
        desc.setText("");
        mChart.setDescription(desc);

        XAxis xAxis = mChart.getXAxis();
//        xAxis.setAxisMinimum(-0.2f);
        xAxis.setDrawLabels(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
//                            xAxis.setValueFormatter(new IAxisValueFormatter() {
//                                @Override
//                                public String getFormattedValue(float value, AxisBase axis) {
//                                    return xLabel.get((int)value);
//                                }
//                            });
        xAxis.setAxisLineWidth(5f);
        xAxis.setAxisLineColor(Color.LTGRAY);

        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setAxisMinimum(0);
        yAxis.setDrawGridLines(false);
        yAxis.setAxisLineWidth(5f);
        yAxis.setAxisLineColor(Color.LTGRAY);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

}