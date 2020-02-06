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
import android.view.View;
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

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.lang.StrictMath.abs;


public class CompassActivity  extends AppCompatActivity implements View.OnClickListener {

    static Location currentlocation;
    Location  jediBus;
    FusedLocationProviderClient fusedLocationProviderClient;

    private static final int REQUEST_CODE = 1011;
    private static final String TAG = "CompassActivity";

    private Compass compass;
    private ImageView arrowView;
    private TextView sotwLabel;  // SOTW is for "side of the world"
    Vibrator v;
    public boolean busAtStop = false;
    public boolean aboardBus = false;
    public boolean checkOtherBus = false;
     public Button checkBus;
    TextView dist;
    TextView duration;
    String distancee;
    String busNumber;
    //-22.917386,-43.250297
    String jedi = "-22.9162006,-43.250998";
    String bipLocation;
    boolean goCompass= false;
    private float currentAzimuth;
    private SOTW sotwFormatter;
    private ProgressDialog pd;
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 60*1000;
    int delayy = 120*1000;
    private int index;
    private String value;
    private String distValue;
    private String nowLocation;
    private int ii;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
        checkBus  = findViewById(R.id.atBus);
        checkBus.setOnClickListener(this);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sotwFormatter = new SOTW(this);
        dist = findViewById(R.id.distance);
        arrowView = findViewById(R.id.img_compass);
        sotwLabel = findViewById(R.id.txt_azimuth);
        duration = findViewById(R.id.duration);
        new Warming().execute();

    }

@Override
    protected void onResume() {
        //start handler as activity become visible
    if(aboardBus == false) {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                if (busNumber != null)
                    new GPS().execute();
                handler.postDelayed(runnable, delay);
            }
        }, delay);
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                if (busNumber != null)
                    new Warming().execute();
                handler.postDelayed(runnable, delayy);
            }
        }, delayy);
    }

        super.onResume();
    }
    public void onClick(View v) {
        if (v == checkBus) {
            if (busAtStop == true) {
                aboardBus = true;
                Toast.makeText(getApplicationContext(), "You are aboard the bus.", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "The bus has arrived", Toast.LENGTH_SHORT).show();

            }
        }
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


    //TODO: Updating bus location
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
// 5"VELOCIDADE",
//6"DIRECAO"

           // Log.d(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr.substring(jsonStr.indexOf("{"), jsonStr.lastIndexOf("}") + 1));
                    // Getting JSON Array node
                    JSONArray points = jsonObj.getJSONArray("DATA");
                        JSONArray times = points.getJSONArray(0);
                        String curBus = times.getString(1);
                            Log.i(TAG, "The bus is " + curBus);
                            String time = times.getString(0);
                            String latitude = times.getString(3);
                            String longitude= times.getString(4);
                            String velocidade= times.getString(5);
                            bipLocation = latitude +"," + longitude;
                            Log.i(TAG, time+"Last location is at " + bipLocation + " at speed " + velocidade);


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
            //BusLocation
            String[] position =  bipLocation.split(",");
            double latitude = Double.parseDouble(position[0]);
            double longitude = Double.parseDouble(position[1]);
            jediBus = new Location("Aa");
            jediBus.setLatitude(latitude);
            jediBus.setLongitude(longitude);
            //StopLocation
            String[] latlong = jedi.split(",");
            double llatitude =Double.parseDouble(latlong[0]);
            double llongitude = Double.parseDouble(latlong[1]);
            Location busStop = new Location("Bb");
            busStop.setLatitude(llatitude);
            busStop.setLongitude(llongitude);
            //Getting the distance
            float distance = busStop.distanceTo(jediBus);//To convert Meter in Kilometer
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(0);
            distancee = nf.format(distance);
            dist.setText("The bus is " + distancee + " meters away.");
            new busDriving().execute();
            Log.i(TAG, "Distance:" + distance);
            float dist = busStop.distanceTo(jediBus);
            if ( dist < 15) {
                busAtStop = true;
                Log.i(TAG, "The bus has arrived.");
                Toast.makeText(getApplicationContext(), "The bus has arrived", Toast.LENGTH_SHORT).show();
            v.vibrate(500);
            }
        }

    }
    private double getDistanceBetweenTwoPoints(double lat1,double lon1,double lat2,double lon2) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = dist * 180.0 / Math.PI;
        dist = dist * 60 * 1.1515*1000;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    //TODO: Checking closest bus in the line
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
            Http sh = new Http();

            // Making a request to url and getting response
            String jsonJedi = sh.makeServiceCall("http://dadosabertos.rio.rj.gov.br/apiTransporte/apresentacao/rest/index.cfm/onibus/439.json");

           // Log.d(TAG, "Response from url: " + jsonJedi);
            if (jsonJedi != null) {
                try {
                    String[] latlong =  jedi.split(",");
                    double llatitude = abs(Double.parseDouble(latlong[0]));
                    double llongitude = abs(Double.parseDouble(latlong[1]));

                    JSONObject jsonObj = new JSONObject(jsonJedi.substring(jsonJedi.indexOf("{"), jsonJedi.lastIndexOf("}") + 1));
                    // Getting JSON Array node
                    JSONArray points = jsonObj.getJSONArray("DATA");
                    double latitudes[];
                    double longitudes[];
                    double distanceLat[];
                    double distanceLng[];
                    double calculations[];
                    String algorithmBus[];
                    longitudes = new double[points.length()];
                    latitudes = new double[points.length()];
                    distanceLng = new double[points.length()];
                    distanceLat = new double[points.length()];
                    calculations = new double[points.length()];
                    algorithmBus = new String[points.length()];
                    // looping through All Routes

                    for (int i = 0; i < points.length(); i++) {
                        JSONArray times = points.getJSONArray(i);
                        String curBus = times.getString(2);
                        Log.i(TAG, curBus);

                        //BusStop location
                        String[] latLong = jedi.split(",");
                        double latitude =Double.parseDouble(latLong[0]);
                        double longitude = Double.parseDouble(latLong[1]);
                        Location busStop = new Location("Bb");
                        busStop.setLatitude(latitude);
                        busStop.setLongitude(longitude);
                       //Currrent bus location
                        Location checkProximity = new Location("abc");
                        checkProximity.setLatitude(times.getDouble(3));
                        checkProximity.setLongitude(times.getDouble(4));


                        if(busStop.distanceTo(checkProximity) >15)
                        {
                            latitudes[i] =  times.getDouble(3);
                            longitudes[i] = times.getDouble(4);
                        }
                        else if(busStop.distanceTo(checkProximity) <15 && times.getInt(5) == 0){
                          busAtStop = true;
                          latitudes[i] =  times.getDouble(3);
                          longitudes[i] = times.getDouble(4);

                        }

                        distanceLat[i] = abs(abs(latitudes[i])-llatitude );
                        distanceLng[i] = abs(abs(longitudes[i])-llongitude );
                        //Log.i(TAG, "Distance at latitude value to bus stop is " + distanceLat[i]+ " and at value of longitude is " + distanceLng[i]);
                        calculations[i] = distanceLat[i] + distanceLng[i];
                        //Log.i(TAG, "Distance for LatLng is " + calculations[i]);
                        algorithmBus[i] = times.getString(1);



                    }
                    double great = Arrays.stream(calculations).min().getAsDouble();
                    Log.i(TAG, "It looks like " + great +" is our minimal distance.");
                    for (int i = 0; i < calculations.length; i++) {
                        if (calculations[i] == great) {
                            index = i;
                            busNumber = algorithmBus[i];
                        }
                    }
                    nowLocation = latitudes[index] +"," + longitudes[index];
                    Log.i(TAG, "Bus location at warming: " + nowLocation +"Stop location at warming: " +jedi);


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
ii = 0;
            if(ii == 0) {
                new GPS().execute();
ii++;
            }
        }
    }
    private class busDriving extends AsyncTask<Void, Void, Void> {
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
            String jsonStr = sh.makeServiceCall("https://maps.googleapis.com/maps/api/directions/json?origin="+nowLocation+"&destination="+jedi+"%2CRio+de+Janeiro&key=AIzaSyA2n7hH6W6cHvZdRX2kBmL0b21ev6WWjag");


           Log.d(TAG, jediBus.getLatitude()+"," +jediBus.getLongitude());

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node
                    JSONArray points = jsonObj.getJSONArray("geocoded_waypoints");
                    JSONObject destination = points.getJSONObject(1);

                    JSONArray routes = jsonObj.getJSONArray("routes");
                    // looping through All Routes
                    for (int i = 0; i < routes.length(); i++) {
                        Log.i(TAG, "This is route no " +i);
                        JSONObject c = routes.getJSONObject(i);
                        //Looping through all Legs in all routes
                        JSONArray legs = c.getJSONArray("legs");
                        for (int e = 0; e < routes.length(); e++) {
                            JSONObject leg = legs.getJSONObject(e);

                JSONObject duration = leg.getJSONObject("duration");
                value = duration.getString("text");
                JSONObject distance = leg.getJSONObject("distance");
                distValue = distance.getString("text");

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
            Log.i(TAG, "Duration is " + value);
          duration.setText("Your bus will arive in " + value +".");

        }
    }
}