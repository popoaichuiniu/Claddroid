package jacy.popoaichuiniu.com.examplepermissionleakge;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        intent.setClass(this,LocalService.class);
        intent.setAction("ttt");
        intent.putExtra("type","kill");
        intent.putExtra("packageName","com.calendar.UI");
        Button button1=findViewById(R.id.button1);
        Button button2=findViewById(R.id.button2);
        Button button3=findViewById(R.id.button3);
        Button button4=findViewById(R.id.button4);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);





    }
    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            LocalService.MyBinder myBinder= (LocalService.MyBinder) service;



            Log.i("ZMS","onServiceConnected"+myBinder.getValue());

            Toast.makeText(MainActivity.this,"onServiceConnected"+myBinder.getValue(),Toast.LENGTH_SHORT).show();




        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Log.i("ZMS","onServiceDisconnected");

            Toast.makeText(MainActivity.this,"onServiceDisconnected",Toast.LENGTH_SHORT).show();

        }
    };

    private Intent intent=new Intent();

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.button1:

                startService(intent);

                break;
            case R.id.button2:

                stopService(intent);
                break;
            case R.id.button3:



                bindService(intent, serviceConnection,BIND_AUTO_CREATE );


                break;


            case R.id.button4:

                unbindService(serviceConnection);


                break;
        }
    }
}
