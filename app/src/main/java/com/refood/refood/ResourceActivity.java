package com.refood.refood;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class ResourceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource);
        BackgroundMusic.getInstance(this).start();
    }

    @Override
    public void onResume(){
        super.onResume();
        BackgroundMusic.getInstance(this).start();
    }

    @Override
    protected void onPause() {
        BackgroundMusic.getInstance(this).pause();
        super.onPause();
    }
}