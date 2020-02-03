package com.example.myapplication;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.model.LatLng;


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
    boolean goCompass= false;
    private float currentAzimuth;
    private SOTW sotwFormatter;

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
            Log.d(TAG, "will set rotation from " + currentAzimuth + " to "
                    + azimuth);


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
}