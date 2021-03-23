package com.refood.refood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZemGardenActivity extends AppCompatActivity {
    private static final String LOG_TAG = ZemGardenActivity.class.getSimpleName();
    private List<Zem> zemList;
    private List<String> ownedList;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private TextView coinCount;
    private long numCoin;
    private FirebaseFirestore mDB;
    private DocumentReference documentReference;
    private Spinner zemImage;
    private SpinnerAdapter spinnerAdapter;
    private DocumentReference docRef;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zem_garden);

        mDB = FirebaseFirestore.getInstance();
        documentReference = mDB.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        storageReference = FirebaseStorage.getInstance().getReference();

        coinCount = findViewById(R.id.zem_coin_text);
        numCoin = getIntent().getLongExtra("numCoins", 0);
        coinCount.setText(getString(R.string.coins_template, numCoin));

        docRef = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        zemImage = findViewById(R.id.spinner_zems);

        prepareOwned();

        zemImage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Collections.swap(zemList, 0, position);
                String zemUrl = ownedList.get(position);

                Map<String, Object> data = new HashMap<>();
                data.put("zemPath", zemUrl);
                docRef.set(data, SetOptions.merge());
                Log.d(LOG_TAG, "OnItemSelectedListener");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


//        zemList = new ArrayList<>();
//        prepareZem();
//        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
//        assert(!ownedList.isEmpty());
//        recyclerViewAdapter = new RecyclerViewAdapter(zemList, numCoin, coinCount, documentReference, ownedList);
//        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(layoutManager);
////        recyclerViewAdapter.setOnItemClickListener(new ClickListener<Zem>(){
////            @Override
////            public void onItemClick(Zem data) {
////                Toast.makeText(ZemGardenActivity.this, data.getPrice(), Toast.LENGTH_SHORT).show();
////            }
////        });
//        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void prepareZem(){
        Zem zem = new Zem(10,R.drawable.zem1, "/zems/zem1.png");
        zemList.add(zem);
        zem = new Zem(15,R.drawable.zem2, "/zems/zem2.png");
        zemList.add(zem);
        zem = new Zem(20,R.drawable.zem3, "/zems/zem3.png");
        zemList.add(zem);
        zem = new Zem(20,R.drawable.zem4, "/zems/zem4.png");
        zemList.add(zem);
        zem = new Zem(30,R.drawable.zem5, "/zems/zem5.png");
        zemList.add(zem);
        zem = new Zem(40,R.drawable.zem6, "/zems/zem6.png");
        zemList.add(zem);
        zem = new Zem(40,R.drawable.zem7, "/zems/zem7.png");
        zemList.add(zem);
    }

    private void prepareOwned(){
        Log.d(LOG_TAG, "prepareOwned");
        this.docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d(LOG_TAG, "oncomplete");
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> userStore = document.getData();
                        ownedList = (List<String>) (userStore.get("ownedZems"));



                        assert(!ownedList.isEmpty());
                        Log.d(LOG_TAG, ownedList.toString());

                        String zemPath = (String) document.get("zemPath");
                        int index = getIndex(zemPath);
                        Log.d(LOG_TAG, "index "+index);
                        Log.d(LOG_TAG, "setSelection");


                        spinnerAdapter = new SpinnerAdapter(ZemGardenActivity.this, (ArrayList<String>) ownedList, storageReference);
                        zemImage.setAdapter(spinnerAdapter);

                        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                                @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.w(LOG_TAG, "Listen failed.", e);
                                    return;
                                }

                                if (snapshot != null && snapshot.exists()) {
                                    List<String> list = (List<String>) (snapshot.get("ownedZems"));
                                    for (String url:
                                         list) {
                                        if (!ownedList.contains(url)){
                                            ownedList.add(url);
                                        }
                                    }
                                    spinnerAdapter.notifyDataSetChanged();
                                    Log.d(LOG_TAG, "Current data: " + snapshot.getData());
                                } else {
                                    Log.d(LOG_TAG, "Current data: null");
                                }
                            }
                        });


                        zemImage.setSelection(index);




                        zemList = new ArrayList<>();
                        prepareZem();
                        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
                        assert(!ownedList.isEmpty());
                        recyclerViewAdapter = new RecyclerViewAdapter(zemList, numCoin, coinCount, documentReference, ownedList);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ZemGardenActivity.this);
                        recyclerView.setLayoutManager(layoutManager);
//        recyclerViewAdapter.setOnItemClickListener(new ClickListener<Zem>(){
//            @Override
//            public void onItemClick(Zem data) {
//                Toast.makeText(ZemGardenActivity.this, data.getPrice(), Toast.LENGTH_SHORT).show();
//            }
//        });
                        recyclerView.setAdapter(recyclerViewAdapter);

                    } else {
                        Log.d(LOG_TAG, "No such document");
                    }
                } else {
                    Log.d(LOG_TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private int getIndex(String path){
        Log.d(LOG_TAG, "here");
        assert(!ownedList.isEmpty());
        for (int i = 0; i<ownedList.size(); i++){
            if(ownedList.get(i).equals(path)){
                return i;
            }
        }
        Log.d(LOG_TAG, "oops");
        return -1;
    }



}