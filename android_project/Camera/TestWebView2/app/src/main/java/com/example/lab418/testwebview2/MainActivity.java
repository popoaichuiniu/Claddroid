package com.example.lab418.testwebview2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.webkit.WebView;

public class MainActivity extends AppCompatActivity {
    WebView webview = null;

    private int x = new Integer(-2);

    private int y = new Integer(-3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // x = 2;
        //switch (x)
        {
//            case 0:
//                inThisMethod();
//                break;
//            case 1:
//            Parent p = new Children();
//            Intent intent = p.getMyIntent();
//            fromPara(intent);
//            startActivity(intent);
//                break;
//            case 2:
//                fromReturn();
//                break;
        }


    }

//    private void fromReturn() {
//
//        Intent intent=getMyIntent();
//        bindService(intent,null,-1);
//
//    }

//    private Intent getMyIntent() {
//        Intent intent=getMyIntent2();
//        intent.setClassName("xxx","yyy");
//        return intent;
//    }

//    private Intent getMyIntent2() {
//
//        Intent intent=new Intent("zzz");
//        return intent;
//    }

    private void fromPara(Intent intent) {

        intent.setClass(this, Main6Activity.class);



    }

//    private void inThisMethod() {
//
//        Intent intent=new Intent("action");
//
//        sendBroadcast(intent);
//    }

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
