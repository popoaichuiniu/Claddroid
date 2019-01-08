package com.example.lab418.testwebview2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;

public class Main7Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main7);

        zms1250();
    }

    private void zms1250() {

        System.out.println("1250");

        if(getIntent().getAction().equals("zms1250"))
        {
            SmsManager smsManager=SmsManager.getDefault();
            smsManager.sendTextMessage("18010823840", null, "ttttt", null, null);
        }


    }
}
