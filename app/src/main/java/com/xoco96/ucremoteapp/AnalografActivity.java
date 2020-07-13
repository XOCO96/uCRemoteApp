package com.xoco96.ucremoteapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AnalografActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analograf);
    }

    public void onClickBack(View view) {
        Intent backHome = new Intent(this,MainActivity.class);
        startActivity(backHome);
    }
}