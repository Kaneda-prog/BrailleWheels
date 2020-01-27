package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.widget.Toast;


public class WebAppInterface {
    Context mContext;
            double latitude = MainActivity.currentLocation.getLatitude();
            double longitude = MainActivity.currentLocation.getLongitude();
            String location = new String(latitude + ", " + longitude);

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
        mContext = c;
        }

/** Get the value */
@JavascriptInterface
public String getValue(){
    return location;
        }
        @JavascriptInterface
    public void iLoveBanana(String banana){

            Toast.makeText(mContext, banana, Toast.LENGTH_LONG).show();

        }
}