package com.example.lab418.testwebview2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.webkit.WebView;

public class MainActivity extends AppCompatActivity {
    WebView webview = null;

    private  int x=new Integer(-2);

    private  int y=new Integer(-3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent=new Intent();

        if(x==2)
            {
                intent=new Intent("action");

                intent.setAction("zzz");

            }
            intent.setClassName("zzzzz","tttttt");

            startActivity(intent);





    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
