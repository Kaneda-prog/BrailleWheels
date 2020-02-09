package com.example.myapplication;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Arrays;

import static java.lang.StrictMath.abs;


public class CompassActivity  extends AppCompatActivity implements View.OnClickListener {

    static Location currentlocation;
    Location jediBus;
    FusedLocationProviderClient fusedLocationProviderClient;

    private static final int REQUEST_CODE = 1011;
    private static final String TAG = "CompassActivity";

    private Compass compass;
    private ImageView arrowView;
    private TextView sotwLabel;  // SOTW is for "side of the world"
    Vibrator v;
    public boolean busAtStop = false;
    public boolean aboardBus = false;
    public Button checkBus;
    TextView dist;
    TextView duration;
    TextView atStop;
    private TextView velocity;
    private TextView busId;
    String distancee;
    String busNumber;
    int linha = 133;
    //-22.917386,-43.250297
    static public String currentPosition = "-22.9363353,-43.1915986";
    String busLocation;
    String otherBus;
    boolean goCompass = false;
    private float currentAzimuth;
    private SOTW sotwFormatter;
    private ProgressDialog pd;
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 25 * 1000;
    int delayy = 60 * 1000;
    private int index;
    private String value;
    private String distValue;
    static public String nowLocation;
    private int veclopis;
    private float lastDistance;
    private float distance;
    private Location busStop;
    private Location checkProximity;
    public boolean busClose;


    @Override


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        //Location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
        //Button
        checkBus = findViewById(R.id.atBus);
        checkBus.setOnClickListener(this);
        //Vibrator Service
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        sotwFormatter = new SOTW(this);
        //Text Views
        dist = findViewById(R.id.distance);
        atStop = findViewById(R.id.busAtStop);
        duration = findViewById(R.id.duration);
        busId = findViewById(R.id.busId);
        velocity = findViewById(R.id.veclopis);

        arrowView = findViewById(R.id.img_compass);
        sotwLabel = findViewById(R.id.txt_azimuth);

        new Warming().execute();
        setupCompass();
    }

    public void onClick(View v) {
        if (v == checkBus) {
            if (busAtStop == true) {
                aboardBus = true;
                Toast.makeText(getApplicationContext(), "You are aboard the bus.", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "You are schizophrenic. There's no bus.", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
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

    private Compass.CompassListener getCompassListener() {
        return new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(final float azimuth) {
                // UI updates only in UI thread
                // https://stackoverflow.com/q/11140285/444966
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //adjustArrow(azimuth);
                        adjustSotwLabel(azimuth);
                    }
                });

            }
        };
    }

    @Override
    protected void onResume() {
        //start handler as activity become visible
        if (aboardBus == false) {
            handler.postDelayed(runnable = new Runnable() {
                public void run() {
                    if (busNumber != null)
                        new Warming().execute();
                    atStop.setText("Bus at Stop: " + busAtStop);
                    handler.postDelayed(runnable, delay);
                }
            }, delay);
        }
        compass.start();
        super.onResume();
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

            String jsonStr = sh.makeServiceCall("http://dadosabertos.rio.rj.gov.br/apiTransporte/apresentacao/rest/index.cfm/onibus/" + busNumber + ".json");
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
                    String longitude = times.getString(4);
                    String velocidade = times.getString(5);
                    busLocation = latitude + "," + longitude;
                    Log.i(TAG, time + "Last location is at " + busLocation + " at speed " + velocidade);


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
            String[] position = busLocation.split(",");
            double latitude = Double.parseDouble(position[0]);
            double longitude = Double.parseDouble(position[1]);
            jediBus = new Location("Aa");
            jediBus.setLatitude(latitude);
            jediBus.setLongitude(longitude);
            //StopLocation
            String[] latlong = currentPosition.split(",");
            double llatitude = Double.parseDouble(latlong[0]);
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
            Log.i(TAG, "Distance:" + distance);
            busId.setText(busNumber);
            lastDistance = busStop.distanceTo(jediBus);
        }

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
            String jsonJedi = sh.makeServiceCall("http://dadosabertos.rio.rj.gov.br/apiTransporte/apresentacao/rest/index.cfm/onibus/" + linha + ".json");

            // Log.d(TAG, "Response from url: " + jsonJedi);
            if (jsonJedi != null) {
                try {
                    String[] latlong = currentPosition.split(",");
                    double currentLatitude = abs(Double.parseDouble(latlong[0]));
                    double currentLongitude = abs(Double.parseDouble(latlong[1]));

                    JSONObject jsonObj = new JSONObject(jsonJedi.substring(jsonJedi.indexOf("{"), jsonJedi.lastIndexOf("}") + 1));
                    // Getting JSON Array node
                    JSONArray points = jsonObj.getJSONArray("DATA");
                    double latitudes[];
                    latitudes = new double[points.length()];
                    double longitudes[];
                    longitudes = new double[points.length()];

                    double distanceLat[];
                    distanceLat = new double[points.length()];
                    double distanceLng[];
                    distanceLng = new double[points.length()];
                    double calculations[];
                    calculations = new double[points.length()];
                    double calculations2[];
                    calculations2 = new double[points.length()];
                    String algorithmBus[];
                    algorithmBus = new String[points.length()];
                    //Looping thought the buses
                    for (int i = 0; i < points.length(); i++) {
                        JSONArray bus = points.getJSONArray(i);
                        //BusStop location


                        latitudes[i] = bus.getDouble(3);
                        longitudes[i] = bus.getDouble(4);


                        distanceLat[i] = abs(abs(latitudes[i]) - currentLatitude);
                        distanceLng[i] = abs(abs(longitudes[i]) - currentLongitude);
                        calculations[i] = distanceLat[i] + distanceLng[i];
                        calculations2[i] = distanceLat[i] + distanceLng[i];
                        algorithmBus[i] = bus.getString(1);

                        String[] latLong = currentPosition.split(",");
                        double latitude = Double.parseDouble(latLong[0]);
                        double longitude = Double.parseDouble(latLong[1]);
                        busStop = new Location("Bb");
                        busStop.setLatitude(latitude);
                        busStop.setLongitude(longitude);
                        checkProximity = new Location("abc");
                        checkProximity.setLatitude(latitudes[i]);
                        checkProximity.setLongitude(longitudes[i]);
                        veclopis = bus.getInt(5);
                        distance = checkProximity.distanceTo(busStop);


                    }
                    double great = Arrays.stream(calculations).min().getAsDouble();
                    for (int i = 0; i < calculations.length; i++) {
                        if (calculations[i] == great) {
                            index = i;
                            busNumber = algorithmBus[i];
                        }
                    }

                    nowLocation = latitudes[index] + "," + longitudes[index];
                    Log.i(TAG, "Bus location at warming: " + nowLocation + "Stop location at warming: " + currentPosition + ". Aditional bus: " + otherBus);
                    checkProximity.setLatitude(latitudes[index]);
                    checkProximity.setLongitude(longitudes[index]);
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
            if (busStop.distanceTo(checkProximity) < 50.0f) {
                busClose = true;
                Log.i(TAG, " FINALLY A BUS IS CLOSE!!!!");
                Toast.makeText(getApplicationContext(), "There's one bus close to you. It's time to call " + busNumber, Toast.LENGTH_SHORT).show();
                if (busStop.distanceTo(checkProximity) < 15.0f) {
                    Toast.makeText(getApplicationContext(), "Wait for the bus to stop", Toast.LENGTH_SHORT).show();
                    if (veclopis == 0) {
                        Toast.makeText(getApplicationContext(), "The bus has stopped. Follow the next directions to get in!", Toast.LENGTH_SHORT).show();
                        busAtStop = true;
                    }
                }
            }

            busId.setText(busNumber);
            velocity.setText("Bus is running at " + veclopis + " km.");
            Log.i(TAG, "DISTANCE: " + busStop.distanceTo(checkProximity));
            dist.setText("Closest bus at " + busStop.distanceTo(checkProximity));
            setupCompass();
            // new busDriving().execute();
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
            String jsonStr = sh.makeServiceCall("https://maps.googleapis.com/maps/api/directions/json?origin=" + nowLocation + "&destination=" + currentPosition + "&key=AIzaSyA2n7hH6W6cHvZdRX2kBmL0b21ev6WWjag");


            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node
                    JSONArray points = jsonObj.getJSONArray("geocoded_waypoints");
                    JSONObject destination = points.getJSONObject(1);

                    JSONArray routes = jsonObj.getJSONArray("routes");
                    // looping through All Routes
                    for (int i = 0; i < routes.length(); i++) {
                        Log.i(TAG, "This is route no " + i);
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
            dist.setText("Closest bus at " + busStop.distanceTo(checkProximity) + ", real value of " + distValue);
            duration.setText("Your bus will arive in " + value + ".");

        }
    }
}
