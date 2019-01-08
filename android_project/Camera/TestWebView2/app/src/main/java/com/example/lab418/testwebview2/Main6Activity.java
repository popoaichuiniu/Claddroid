package com.example.lab418.testwebview2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Main6Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);
        zms250();
        
    }

    private void zms250() {
        System.out.println("zms250");
        Intent intent=new Intent();
        intent.setClass(this,Main7Activity.class);
        startActivity(intent);

    }
}
