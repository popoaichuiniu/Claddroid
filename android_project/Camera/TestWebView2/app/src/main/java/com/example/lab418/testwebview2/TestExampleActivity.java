package com.example.lab418.testwebview2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;

import com.google.common.collect.HashMultiset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TestExampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_un_used_ifblock_algorithm);

        int choice = new Integer(5);

        if (choice == 5)//
        {
            if (getIntent().getBundleExtra("bundle").getString("bundleKey").equals("bundleData"))
            {
                SmsManager.getDefault().sendTextMessage("123456789", null, getIntent().getStringExtra("content"), null, null);
            }
        }


//        if (choice == 0)//ok
//        {
//            if(getIntent().getAction().equals("ttt"))
//            {
//                testCallPath(getIntent());
//                testIntentBundle2();
//            }
//
//        }



//        if (choice == 1) {
//            testIntentBundle1();
//            testIntentBundle1();-----problem
//
//        }
//
//        if (choice == 2)//ok
//            testIntentBundle2();

//        if (choice == 3)
//            testIntentGetExtras1();----
//
//        if (choice == 4)//ok
//            testIntentGetExtras2();
//
//        if (choice == 5)
//            testHasCate();// not cate not support
//
//        if (choice == 6)
//            testHasExtra();----
//        if (choice == 7)//ok
//            testAction();
//
//
//        if (choice == 8)
//            testFloatExtra();
//




    }

    private void testCallPath(Intent intent) {

//        if (intent.getAction().equals("action1")) {
////            if(intent.hasCategory("cate1"))
////            {
////                if(intent.getStringExtra("stringExtra1").equals("stringExtra1"))
////                {
////                    SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
////                }
////
////            }
//        } else
{
            if (intent.hasCategory("cate1")) {


            } else {
                //if(!intent.getStringExtra("stringExtra1").equals("stringExtra1"))
                //if(intent.getStringExtra("stringExtra1").equals("stringExtra1")==true)//fail  can't find in app

                if ((intent.getStringExtra("stringExtra1").equals("stringExtra1") == true) == true)//fail  can't find in app
                //if((intent.getFloatExtra("ddd",-1)>8)==true) //ok
                {

                } else {
                    sendSMS();
                }
            }
        }


    }



    private void testIntentBundle1()//
    {
        List<Integer> list=new ArrayList<>();
        list.add(5);
        System.out.println(list);
        startActivity(new Intent());
        if (getIntent().getBundleExtra("bundle").containsKey("bundleKey")) {
            sendSMS();
        } else {
            SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
        }

        Test.sendMS1();

        Test test=new Test();
        test.sendMS2();
    }


    private void testIntentBundle2()//
    {
        if (getIntent().getBundleExtra("bundle").getString("iambundleExtra").equals("iambundleExtraValue")) {
           sendSMS();
        } else {
            sendSMS();
        }
    }

    private void testIntentGetExtras1()//---
    {
        if (getIntent().getExtras().containsKey("intentGetExtrasIntentKey")) {
            SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
        } else {
            SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
        }
    }


    private void testIntentGetExtras2()//ok
    {
        if (getIntent().getExtras().getString("intentGetExtras").equals("intentGetExtrasValue")==false) {
            SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
        } else {
            //SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
        }
    }

    private void testHasCate() {
        if (getIntent().hasCategory("hascate")) {//ok
            SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
        } else//not support
        {
            SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
        }
    }

    private void testHasExtra()//do not handle containsKey,just produce containsKey assert;
    {
        if (getIntent().hasExtra("hasxtra")) {
            SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
        } else {
            SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
        }
    }

    private void testAction() {//ok
        if (getIntent().getAction().equals("action")) {
            SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
        } else {
            SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
        }
    }

    private void testFloatExtra() {

       // if(!(getIntent().getFloatExtra("floatExtraGreatThan7", -1) > 7)){//ok
        if ((getIntent().getFloatExtra("floatExtraGreatThan7", -1) > 7)==false) {//fail
          SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
        } else {
            //SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
        }
    }

    private void testStringExtra() {//ok

        if (getIntent().getStringExtra("stringExtraEqualsHaha").equals("haha")==false) {//ok
            if ((getIntent().getFloatExtra("floatExtraGreatThan7", -1) > 7))////
            {
                SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
            }

        } else {
            if (getIntent().getBundleExtra("bundle").getString("iambundleExtra").equals("iambundleExtraValue"))
            {
                SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
            }

        }
    }

    private void sendSMS()
    {
        SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
    }

   


}
