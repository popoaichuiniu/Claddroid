package jacy.popoaichuiniu.com.testpermissionleakge;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //System.out.println(new File(".").getAbsoluteFile());
        final TextView textView = findViewById(R.id.testAPP);


        final Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:

                        textView.setText(msg.getData().getString("appPath"));

                        break;

                    case 1:
                        Toast.makeText(MainActivity.this, msg.getData().getString("comPonentName"), Toast.LENGTH_SHORT).show();

                }
            }

            ;
        };


        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean flagFirstIn = true;
                try {
                    BufferedReader bufferedRead = new BufferedReader(new InputStreamReader(MainActivity.this.openFileInput("intentInfo.txt")));


                    String content = null;
                    Log.i("ZMSStart", "11111111111111111111111111111111111111111111111111111111111111");
                    while ((content = bufferedRead.readLine()) != null) {
                        String[] str = content.split("#");

                        String appPath = str[0];
                        if (flagFirstIn) {

                            Message message = new Message();
                            message.what = 0;
                            Bundle bundle = new Bundle();
                            bundle.putString("appPath", appPath);
                            message.setData(bundle);
                            mHandler.sendMessage(message);

                            flagFirstIn = false;
                        }

                        String appPackageName = str[1];
                        String comPonentType = str[2];
                        String comPonentName = str[3];

                        String comPonentAction = str[4];
                        String comPonentCategory = str[5];
                        String comPonentData = str[6];
                        String comPonentExtraData = str[7];

                        Intent intent = new Intent();
                        if (!comPonentAction.equals("null")) {
                            intent.setAction(comPonentAction);
                        }

                        if (!comPonentCategory.equals("null")) {
                            String cates[] = comPonentCategory.split(",");
                            for (String cate : cates) {
                                intent.addCategory(cate);


                            }
                        }
                        if (!comPonentData.equals("null")) {
                            intent.setData(Uri.parse(comPonentData));
                        }


                        try {
                            if (!comPonentExtraData.equals("null")) //type&key&value,
                            {
                                String extraDatas[] = comPonentExtraData.split(";");

                                for (String extraData : extraDatas) {
                                    String attrs[] = extraData.split("&");
                                    if (attrs[0].equals("java.lang.String")) {
                                        String strValue = getStringValue(attrs[2]);
                                        intent.putExtra(attrs[1], strValue);

                                    } else if (attrs[0].equals("byte")) {
                                        intent.putExtra(attrs[1], Byte.valueOf(attrs[2]));
                                    } else if (attrs[0].equals("short")) {
                                        intent.putExtra(attrs[1], Short.valueOf(attrs[2]));
                                    } else if (attrs[0].equals("int")) {
                                        intent.putExtra(attrs[1], Integer.valueOf(attrs[2]));

                                    } else if (attrs[0].equals("long")) {
                                        intent.putExtra(attrs[1], Long.valueOf(attrs[2]));
                                    } else if (attrs[0].equals("float")) {
                                        intent.putExtra(attrs[1], Float.valueOf(attrs[2]));
                                    } else if (attrs[0].equals("double")) {
                                        intent.putExtra(attrs[1], Double.valueOf(attrs[2]));
                                    } else if (attrs[0].equals("boolean")) {
                                        intent.putExtra(attrs[1], Boolean.valueOf(attrs[2]));
                                    } else if (attrs[0].contains("android.os.Bundle"))//[BundleKey, java.lang.String, iambundleExtra, "iambundleExtraValue"]
                                    {
                                        String value = attrs[2];
                                        if (value.contains("[BundleKey")) {
                                            Bundle bundle = new Bundle();
                                            value = value.substring(1, value.length() - 1);
                                            String[] bundleAttr = value.split(",");
                                            bundleAttr[1] = bundleAttr[1].trim();
                                            bundleAttr[2] = bundleAttr[2].trim();
                                            bundleAttr[3] = bundleAttr[3].trim();
                                            if (bundleAttr[1].trim().equals("java.lang.String")) {
                                                bundle.putString(bundleAttr[2], getStringValue(bundleAttr[3]));
                                            } else if (bundleAttr[1].trim().equals("byte")) {
                                                bundle.putByte(bundleAttr[2], Byte.valueOf(bundleAttr[3]));
                                            } else if (bundleAttr[1].trim().equals("short")) {
                                                bundle.putShort(bundleAttr[2], Short.valueOf(bundleAttr[3]));
                                            } else if (bundleAttr[1].trim().equals("int")) {
                                                bundle.putInt(bundleAttr[2], Integer.valueOf(bundleAttr[3]));
                                            } else if (bundleAttr[1].trim().equals("long")) {
                                                bundle.putLong(bundleAttr[2], Long.valueOf(bundleAttr[3]));
                                            } else if (bundleAttr[1].trim().equals("float")) {
                                                bundle.putFloat(bundleAttr[2], Float.valueOf(bundleAttr[3]));
                                            } else if (bundleAttr[1].trim().equals("double")) {
                                                bundle.putDouble(bundleAttr[2], Double.valueOf(bundleAttr[3]));
                                            } else if (bundleAttr[1].trim().equals("boolean")) {
                                                bundle.putBoolean(bundleAttr[2], Boolean.valueOf(bundleAttr[3]));
                                            } else {
                                                Log.i("ZMSStart", "can't handle " + value);
                                            }

                                            intent.putExtra(attrs[1], bundle);


                                        } else {
                                            if (value.contains("Not")) {
                                                Bundle bundle = new Bundle();
                                                intent.putExtra(attrs[1], bundle);
                                            }
                                        }

                                    } else {
                                        Log.i("ZMSStart", "can't handle " + extraData+"eeeeeeeeeeee");
                                    }


                                }


                            }
                        } catch (NumberFormatException e) {
                            Log.i("ZMSStart", appPath + "数据转化异常！"+content+"eeeeeeeeeeee");
                            continue;
                        }

                        Log.i("ZMSStart", appPath);


                        intent.setClassName(appPackageName, comPonentName);

                        Log.i("ZMSStart", "启动********" + appPackageName + "******************"+comPonentName);


                        if (!comPonentType.equals("null")) {


                            if (sendIntentByType(intent, comPonentType, comPonentName)) {
                                Message message = new Message();
                                message.what = 1;
                                Bundle bundle = new Bundle();
                                bundle.putString("comPonentName", comPonentType + ":" + comPonentName);
                                message.setData(bundle);
                                mHandler.sendMessage(message);

                                Log.i("ZMSStart", comPonentType + ":" + comPonentName + "启动成功！");


                            }

                        }

//
//                        try {
//                            Thread.sleep(300);
//                        } catch (InterruptedException e) {  //one--------------------------------------------
//                            e.printStackTrace();
//                        }

                    }



                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("ZMSStart", "intentInfo.txt打开失败"+"eeeeeeeeeeee");
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
                Log.i("ZMSStart", "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& app is died now!");
                android.os.Process.killProcess(android.os.Process.myPid());

            }
        }).start();


    }

    private String getStringValue(String attr) {

        if (attr.charAt(0) == '"' && attr.charAt(attr.length() - 1) == '"') {
            return attr.substring(1, attr.length() - 1);
        }
        return attr;
    }

    private boolean sendIntentByType(Intent intent, String comPonentType, String comPonentName) {
        try {
            if (comPonentType.equals("activity")) {
                startActivity(intent);


                return true;
            } else if (comPonentType.equals("service")) {
                startService(intent);


                return true;
            } else if (comPonentType.equals("receiver")) {
                sendBroadcast(intent);


                return true;
            } else if (comPonentType.equals("provider")) {


                Log.i("ZMSStart", "provider忽略");
            }

        } catch (Exception e) {

            Log.i("ZMSStart", comPonentType + ":" + comPonentName +"启动异常! "+ e+"eeeeeeeeeeee");

            return false;
        }

        return false;

    }


}


