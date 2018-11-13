package jacy.popoaichuiniu.com.examplepermissionleakge;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class LocalService extends Service {
    public LocalService() {
    }


    private  int i=-1;

    private  int j=-2;


    @Override
    public void onCreate() {
        Log.i("ZMS","onCreate");
        Toast.makeText(this,"onCreate",Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("ZMS","onStartCommand"+startId+intent);

        Toast.makeText(this,"onStartCommand"+startId+intent,Toast.LENGTH_SHORT).show();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("ZMS","onDestroy");
        Toast.makeText(this,"onDestroy",Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("ZMS","onBind");
        Toast.makeText(this,"onBind",Toast.LENGTH_SHORT).show();

        if(intent.getStringExtra("type").equals("kill"))
        {
              ActivityManager activityManager= (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
              activityManager.killBackgroundProcesses(intent.getStringExtra("packageName"));
        }

        Log.i("ZMS","onKill");
        Toast.makeText(this,"onKill",Toast.LENGTH_SHORT).show();



        return new MyBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {

        Log.i("ZMS","onUnbind");
        Toast.makeText(this,"onUnbind",Toast.LENGTH_SHORT).show();

        return super.onUnbind(intent);
    }

    class MyBinder extends Binder{


        public int getValue()
        {
            return i+j;
        }


    }
}
