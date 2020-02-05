package com.example.myapplication;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.lang.StrictMath.abs;


public class CompassActivity extends AppCompatActivity {
    static Location currentlocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 1011;
    private static final String TAG = "CompassActivity";
    Vibrator v;
    private Compass compass;
    private ImageView arrowView;
    private TextView sotwLabel;  // SOTW is for "side of the world"
    Button checkBus;
    TextView dist;
    String busNumber;
    String jedi = "-22.933276,-43.184910";
    String bipLocation;
    boolean goCompass= false;
    private float currentAzimuth;
    private SOTW sotwFormatter;
    private ProgressDialog pd;
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 60*1000;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sotwFormatter = new SOTW(this);
        dist = findViewById(R.id.distance);
        arrowView = findViewById(R.id.img_compass);
        sotwLabel = findViewById(R.id.txt_azimuth);
        new Warming().execute();
    }


@Override
    protected void onResume() {
        //start handler as activity become visible

        handler.postDelayed( runnable = new Runnable() {
            public void run() {
                if(busNumber != null)
                new GPS().execute();

                handler.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }
    private void fetchLastLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!= null) {
                    currentlocation = location;
                    //Intent intent = getIntent();
                   // String position = intent.getStringExtra("firstStop");
                    //String[] latlong =  position.split(",");
                   // double latitude = Double.parseDouble(latlong[0]);
                   // double longitude = Double.parseDouble(latlong[1]);
                    //LatLng latLng = new LatLng(latitude, longitude);

                }
            }

        });
    }

    private class GPS extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pd = new ProgressDialog(CompassActivity.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Http sh = new Http();

            // Making a request to url and getting response

            String jsonStr = sh.makeServiceCall("http://dadosabertos.rio.rj.gov.br/apiTransporte/apresentacao/rest/index.cfm/onibus/"+busNumber+".json");
//COLLUMS
// 0"DATAHORA","
// 1ORDEM",
// 2"LINHA",
// 3"LATITUDE",
// 4"LONGITUDE",
// 5"VELOCIDADE"

            Log.d(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr.substring(jsonStr.indexOf("{"), jsonStr.lastIndexOf("}") + 1));
                    // Getting JSON Array node
                    JSONArray points = jsonObj.getJSONArray("DATA");
                    Log.i(TAG, "ok");

            Log.i(TAG, "Lenght is " + points.length());
                    // looping through All Routes
                    for (int i = 0; i < points.length(); i++) {
                        //JSONObject t = points.getJSONObject(i);
                        Log.i(TAG, "ok2");
                        JSONArray times = points.getJSONArray(i);
                        Log.i(TAG, "ok3");
                        String curBus = times.getString(2);

                            Log.i(TAG, curBus);
                            String time = times.getString(0);
                            String latitude = times.getString(3);
                            String longitude= times.getString(4);
                            String velocidade= times.getString(5);
                            Log.i(TAG, time+"Last location is at " + latitude +", " + longitude + " at speed " + velocidade + " after " + i +" times checked.");
                            Log.i(TAG, "ok4");


                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pd.isShowing())
                pd.dismiss();

        }
    }
    private class Warming extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pd = new ProgressDialog(CompassActivity.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Https sh = new Https();

            // Making a request to url and getting response
            String jsonJedi = sh.makeServiceCall("http://dadosabertos.rio.rj.gov.br/apiTransporte/apresentacao/rest/index.cfm/onibus/439.json");

            Log.d(TAG, "Response from url: " + jsonJedi);
            if (jsonJedi != null) {
                try {
                    String[] latlong =  jedi.split(",");
                    double llatitude = abs(Double.parseDouble(latlong[0]));
                    double llongitude = abs(Double.parseDouble(latlong[1]));
                    double total =  llatitude + llongitude;

                    JSONObject jsonObj = new JSONObject(jsonJedi.substring(jsonJedi.indexOf("{"), jsonJedi.lastIndexOf("}") + 1));
                    // Getting JSON Array node
                    JSONArray points = jsonObj.getJSONArray("DATA");
                    Log.i(TAG, "ok");

                    Log.i(TAG, "Lenght is " + points.length());
                    double bips[];
                    double pibs[];
                    double bp[];
                    double piupow[];
                    String algorithmBus[];
                    Log.i(TAG, "wow" +points.length());
                    pibs = new double[points.length()];
                    Log.i(TAG, "woww" +points.length());
                    bips = new double[points.length()];
                    bp = new double[points.length()];
                    piupow = new double[points.length()];
                    algorithmBus = new String[points.length()];
                    // looping through All Routes
                    for (int i = 0; i < points.length(); i++) {
                        //JSONObject t = points.getJSONObject(i);
                        Log.i(TAG, "ok2");
                        JSONArray times = points.getJSONArray(i);
                        Log.i(TAG, "ok3");
                        String curBus = times.getString(2);

                        Log.i(TAG, curBus);
                        String time = times.getString(0);
                        String latitude = times.getString(3);
                        String longitude= times.getString(4);
                        String velocidade= times.getString(5);
                        Log.i(TAG, time+"Last location is at " + latitude +", " + longitude + " at speed " + velocidade + " after " + i +" times checked.");
                        bips[i] =  times.getDouble(3);
                        pibs[i] = times.getDouble(4);
                        bp[i] = abs(bips[i]) + abs(pibs[i]);
                        Log.i(TAG, "WWWoow " + bp[i]);
                        piupow[i] = bp[i] - total;
                        Log.i(TAG, "ok4" + piupow[i]);
                        algorithmBus[i] = times.getString(1);
                    }
                    double great = Arrays.stream(piupow).min().getAsDouble();
                    Log.i(TAG, "WAAAAA " + great);
                    for (int i = 0; i < piupow.length; i++) {
                        if (piupow[i] == great) {
                            index = i;
                            busNumber = algorithmBus[i];
                        }
                    }
                    String nowLocation = bips[index] +", " + pibs[index];
                    Log.i(TAG, "UNSINN! DIE LOOP IS KAPPUTT! " + index +" hahaha " + nowLocation +"   " + busNumber);


                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pd.isShowing())
                pd.dismiss();

            new GPS().execute();
        }
    }

}