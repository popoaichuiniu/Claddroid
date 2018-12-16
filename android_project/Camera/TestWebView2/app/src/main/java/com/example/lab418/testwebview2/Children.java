package com.example.lab418.testwebview2;

import android.content.Intent;

public class Children extends Parent {
    @Override
    Intent getMyIntent() {
       Intent intent=new Intent("child");
       return  intent;
    }
}
