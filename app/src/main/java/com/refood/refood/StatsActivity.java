package com.refood.refood;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
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
    ArrayList<Entry> mScoreList = new ArrayList<>();

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
                            ArrayList<String> xLabel = new ArrayList<>();
                            for (int i=0; i<dateList.size(); i++) {
                                String date = dateList.get(i);
                                Log.d(LOG_TAG, "Timestamp data: " + date);
//                                xLabel.add(date);
//                                xLabel.add("Mar 24");
                                entryList.add(new Entry(i, scoreList.get(i)));
                            }

                            setText(dateList);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setText(ArrayList<String> dateList){
//        boolean streakBroken = false;
        int m1, d1, y1, m2, d2, y2;
        int numStreak = 0;
        Calendar firstDay = Calendar.getInstance();
        if (dateList.isEmpty()) {
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
                    i--;
                } else {
                    break;
                }
            }
        }
        mStreak.setText(String.valueOf(numStreak));

        String date = dateList.get(0);
        String delims = "/";
        String[] tokens = date.split(delims);

        m1 = Integer.parseInt(tokens[0]);
        d1 = Integer.parseInt(tokens[1]);
        y1 = Integer.parseInt(tokens[2]);
        firstDay.set(y1, m1, d1);
        Log.d(LOG_TAG, "firstday: "+firstDay);
        Log.d(LOG_TAG, "current instance: "+Calendar.getInstance().toString());
        Long daysBetween = (Long) ChronoUnit.DAYS.between(firstDay.toInstant(), Calendar.getInstance().toInstant())+1;
        mDaysPlayed.setText(String.valueOf(daysBetween));
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

    private void setChart(ArrayList<Entry> entryList){
        LineDataSet set1 = new LineDataSet(entryList, "Data Set 1");

        set1.setFillAlpha(110);
        set1.setColor(Color.BLUE);
        set1.setLineWidth(3f);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        LineData data = new LineData(dataSets);

        mChart.setData(data);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setAxisMinimum(0);
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

        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setAxisMinimum(0);
        yAxis.setDrawGridLines(false);
        yAxis.setAxisLineWidth(5f);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

}