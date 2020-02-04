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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


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
    String busNumber = "895.0";
    boolean goCompass= false;
    private float currentAzimuth;
    private SOTW sotwFormatter;
    private ProgressDialog pd;

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
        new GPS().execute();
        setupCompass();
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
                    Intent intent = getIntent();
                    String position = intent.getStringExtra("firstStop");
                    String[] latlong =  position.split(",");
                    double latitude = Double.parseDouble(latlong[0]);
                    double longitude = Double.parseDouble(latlong[1]);
                    LatLng latLng = new LatLng(latitude, longitude);
                    Location temp = new Location(LocationManager.GPS_PROVIDER);
                    temp.setLatitude(latitude);
                    temp.setLongitude(longitude);
                    Log.i(TAG, "LOOOOOK" + temp +"   " +  currentlocation);
                    dist.setText("Distance to bus is " + currentlocation.distanceTo(temp) + " meters.");
                }
            }

        });
    }

       @Override
    protected void onStart() {
        super.onStart();

            Log.d(TAG, "start compass");
            compass.start();
    }

    @Override
    protected void onPause() {
            super.onPause();
            compass.stop();

    }

    @Override
    protected void onResume() {

            super.onResume();
            compass.start();

    }

    @Override
    protected void onStop() {

            super.onStop();
            Log.d(TAG, "stop compass");
            compass.stop();

    }

    private void setupCompass() {
        compass = new Compass(this);
        Compass.CompassListener cl = getCompassListener();
        compass.setListener(cl);
    }
    private void shakeItBaby() {
        long[] pattern = {0, 100, 50, 300};
        v.vibrate(pattern, -1);
    }
    private void adjustArrow(float azimuth) {
            //Log.d(TAG, "will set rotation from " + currentAzimuth + " to "
              //      + azimuth);


            Animation an = new RotateAnimation(-currentAzimuth, -azimuth,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            currentAzimuth = azimuth;

            an.setDuration(100);
            an.setRepeatCount(0);
            if (azimuth > -1 && azimuth < 1) {
                shakeItBaby();
            }
            arrowView.startAnimation(an);

    }

    private void adjustSotwLabel(float azimuth) {
        sotwLabel.setText(sotwFormatter.format(azimuth));
    }
    protected double bearing(double startLat, double startLng, double endLat, double endLng){
        double longitude1 = startLng;
        double longitude2 = endLng;
        double latitude1 = Math.toRadians(startLat);
        double latitude2 = Math.toRadians(endLat);
        double longDiff= Math.toRadians(longitude2-longitude1);
        double y= Math.sin(longDiff)*Math.cos(latitude2);
        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x))+360)%360;
    }
    private Compass.CompassListener getCompassListener() {
        return new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(final float azimuth) {
                // UI updates only in UI thread
                // https://stackoverflow.com/q/11140285/444966
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adjustArrow(azimuth);
                        adjustSotwLabel(azimuth);
                    }
                });
            }
        };
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
                        if(curBus.contains(busNumber))
                        {
                            Log.i(TAG, curBus);
                            String latitude = times.getString(3);
                            String longitude= times.getString(4);
                            String velocidade= times.getString(5);
                            Log.i(TAG, "Last location is at " + latitude +", " + longitude + " at speed " + velocidade + " after " + i +" times checked.");
                            Log.i(TAG, "ok4");
                        }

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
}