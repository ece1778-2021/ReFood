package com.refood.refood;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ZemGardenActivity extends AppCompatActivity {
    private List<Zem> zemList;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private TextView coinCount;
    private long numCoin;
    private FirebaseFirestore mDB;
    private DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zem_garden);

        mDB = FirebaseFirestore.getInstance();
        documentReference = mDB.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        coinCount = findViewById(R.id.zem_coin_text);
        numCoin = getIntent().getLongExtra("numCoins", 0);
        coinCount.setText(getString(R.string.coins_template, numCoin));

        zemList = new ArrayList<>();
        prepareZem();
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerViewAdapter = new RecyclerViewAdapter(zemList, numCoin, coinCount, documentReference);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
//        recyclerViewAdapter.setOnItemClickListener(new ClickListener<Zem>(){
//            @Override
//            public void onItemClick(Zem data) {
//                Toast.makeText(ZemGardenActivity.this, data.getPrice(), Toast.LENGTH_SHORT).show();
//            }
//        });
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void prepareZem(){
//        Zem zem = new Zem(20,R.drawable.zem1);
//        zemList.add(zem);
        Zem zem = new Zem(15,R.drawable.zem2);
        zemList.add(zem);
        zem = new Zem(20,R.drawable.zem3);
        zemList.add(zem);
        zem = new Zem(20,R.drawable.zem4);
        zemList.add(zem);
        zem = new Zem(30,R.drawable.zem5);
        zemList.add(zem);
        zem = new Zem(40,R.drawable.zem6);
        zemList.add(zem);
        zem = new Zem(40,R.drawable.zem7);
        zemList.add(zem);
    }


}