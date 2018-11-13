package com.example.lab418.testwebview2;

import android.telephony.SmsManager;

public class Test {

    public static void sendMS1()
    {
        SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
    }

    public  void sendMS2()
    {
        SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
    }
}
