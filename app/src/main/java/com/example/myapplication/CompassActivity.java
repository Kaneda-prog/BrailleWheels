package com.example.myapplication;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorEvent;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Arrays;

import static android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO;
import static com.example.myapplication.MainActivity.currentLocation;
import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.sin;


public class CompassActivity  extends AppCompatActivity implements View.OnClickListener {

    static Location currentlocation;
    Location jediBus;
    FusedLocationProviderClient fusedLocationProviderClient;

    private static final int REQUEST_CODE = 1011;
    private static final String TAG = "CompassActivity";

    private Compass compass;
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
    int linha = 112;
    //-22.917386,-43.250297
    static public String currentPosition = "-22.92715, -43.25187";
    String busLocation;
    String otherBus;
    private float currentAzimuth;
    private SOTW sotwFormatter;
    private ProgressDialog pd;
    Handler handler = new Handler();
    Handler vibratorHandler = new Handler();
    Runnable runnable;
    Runnable runnable2;
    Runnable runnable3;
    int delay = 25 * 1000;
    int delayy = 2 * 1000;
    private int index;
    private String value;
    private String distValue;
    static public String nowLocation;
    private int veclopis;
    private float lastDistance;
    private float distance;
    public Location checkProximity;
    public boolean busClose;
    private int ii;
    private float degree;
    private double turn;
    private boolean atLeft;
    private float hipDist;
    private double heightDist;
    private boolean precision;
    private double altura;
    private double largura;
    private double sideDist;
    private boolean way;
    private ImageView arrowView;
    final Object lock = new Object();

    @Override


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        //Location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
                    //VIEWS
        //Aboard Bus button
        checkBus = findViewById(R.id.atBus);
        checkBus.setText("Aboard the bus? Click here!");
        checkBus.setOnClickListener(this);
        checkBus.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
        //Images
        arrowView = findViewById(R.id.img_compass);
        //Text
        dist = findViewById(R.id.distance);
        atStop = findViewById(R.id.busAtStop);
        duration = findViewById(R.id.duration);
        busId = findViewById(R.id.busId);
        velocity = findViewById(R.id.veclopis);
        sotwLabel = findViewById(R.id.txt_azimuth);
        //Vibrator Service
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Well, sotwFormatter
        sotwFormatter = new SOTW(this);


        //Stuff to be executed
        new Warming().execute();
        setupCompass();

    }

    public void onClick(View v) {
        if (v == checkBus) {
            if (busAtStop) {
                aboardBus = true;
                Toast.makeText(this, "You are aboard the bus.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "You are schizophrenic. There's no bus.", Toast.LENGTH_SHORT).show();

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
        fetchLastLocation();
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

    private void adjustArrow(float azimuth) {
        Animation an = new RotateAnimation(-currentAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currentAzimuth = azimuth;

        an.setDuration(210);
        an.setRepeatCount(0);
        if (azimuth > -1 && azimuth < 1) {

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

    @Override
    protected void onResume() {
        vibratorHandler.postDelayed(runnable = new Runnable() {
            public void run() {
                if (busAtStop) {
                    if (currentAzimuth <-172&& currentAzimuth >-188) {
                        way = true;
                        long[] pattern = {0, 200, 200, 300};
                        v.vibrate(pattern, -1);
                    }
                    else if (currentAzimuth > 172 && currentAzimuth < 188) {
                        way = true;
                        long[] pattern = {0, 200, 200, 300};
                        v.vibrate(pattern, -1);
                    }
                    if(!way)
                    Instructions(currentAzimuth);
                }
                handler.postDelayed(runnable, delayy);
            }
        }, delayy);
        handler.postDelayed(runnable2 = new Runnable() {
            public void run() {
                if (busNumber != null)
                    if (aboardBus == false) {
                        new Warming().execute();
                    }
                handler.postDelayed(runnable2, delay);
            }
        }, delay);

        compass.start();
        super.onResume();
    }



    //TODO: Checking closest bus in the line
    public class Warming extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.combo);
            mp.setVolume(0.08f, 0.08f);
            mp.start();
            // Showing progress dialog
            pd = new ProgressDialog(CompassActivity.this);
            pd.setCancelable(false);
            pd.show();

        }

        //Check closest bus:
        //Set it Id, velocity and distance
        @Override

        protected Void doInBackground(Void... arg0) {
            Http sh = new Http();

            // Making a request to url and getting response
            String jsonJedi = sh.makeServiceCall("http://dadosabertos.rio.rj.gov.br/apiTransporte/apresentacao/rest/index.cfm/onibus/" + linha + ".json");

            if (jsonJedi != null) {
                try {
                    //String[] latlong = currentPosition.split(",");
                    //double currentLatitude = abs(Double.parseDouble(latlong[0]));
                    //double currentLongitude = abs(Double.parseDouble(latlong[1]));

                    JSONObject jsonObj = new JSONObject(jsonJedi.substring(jsonJedi.indexOf("{"), jsonJedi.lastIndexOf("}") + 1));
                    // Getting JSON Array node
                        JSONArray points = jsonObj.getJSONArray("DATA");
                        //Bus location array
                        double latitudes[];
                        latitudes = new double[points.length()];
                        double longitudes[];
                        longitudes = new double[points.length()];
                        //Bus distance array
                        double distanceLat[];
                        distanceLat = new double[points.length()];
                        double distanceLng[];
                        distanceLng = new double[points.length()];
                        //Sum of lat an lng distances
                        double calculations[];
                        calculations = new double[points.length()];
                        //Bus id for each instance
                        String algorithmBus[];
                        algorithmBus = new String[points.length()];

                    int speed[];
                    speed = new int[points.length()];
                    //Looping thought the buses
                    for (int i = 0; i < points.length(); i++) {
                        JSONArray bus = points.getJSONArray(i);
                        //Bus location lat and lng
                        latitudes[i] = bus.getDouble(3);
                        longitudes[i] = bus.getDouble(4);

                        //Bus - current Location
                        distanceLat[i] = abs(abs(latitudes[i]) - abs(currentLocation.getLatitude()));
                        distanceLng[i] = abs(abs(longitudes[i]) - abs(currentLocation.getLongitude()));
                       //Sum of lat and lng
                        calculations[i] = distanceLat[i] + distanceLng[i];
                        //Bus id for each instance
                        algorithmBus[i] = bus.getString(1);

                        //Bus Location
                        checkProximity = new Location("abc");
                        checkProximity.setLatitude(latitudes[i]);
                        checkProximity.setLongitude(longitudes[i]);
                        //Bus speed
                       speed[i] = bus.getInt(5);


                    }
                    double great = Arrays.stream(calculations).min().getAsDouble();
                    for (int i = 0; i < calculations.length; i++) {
                        if (calculations[i] == great) {
                            index = i;
                            busNumber = algorithmBus[index];
                            veclopis = speed[index];

                        }
                    }
                    checkProximity.setLatitude(latitudes[index]);
                    checkProximity.setLongitude(longitudes[index]);
                    //Bus distance to user
                    distance = checkProximity.distanceTo(currentLocation);
                    nowLocation = latitudes[index] + "," + longitudes[index];
                    Log.i(TAG, "Bus location at warming: " + checkProximity + "Stop location at warming: " + currentPosition);

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
            //setupCompass();
            float dis = Math.round(currentLocation.distanceTo(checkProximity));
            if (dis < 50.0f) {
                busClose = true;
                if (dis < 15.0f) {
                    if (veclopis != 0) {
                        Toast.makeText(getApplicationContext(), "Wait for the bus to stop", Toast.LENGTH_SHORT).show();
                    }
                    if(veclopis == 0) {
                        busAtStop = true;
                        Toast.makeText(getApplicationContext(), "The bus has stopped. Follow the next directions to get in!", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "There's one bus close to you. It's time to call " + busNumber, Toast.LENGTH_SHORT).show();
                }
            }

            busId.setText(busNumber);
            atStop.setText("Bus at stop: "+ busAtStop);
            velocity.setText("Speed:" + veclopis + " km.");
            dist.setText("Closest bus at " + dis + " meters.");
            SensorEvent obj = new SensorEvent();
            compass.onSensorChanged(obj);
setupCompass();
            //new busDriving().execute();
        }
    }
public void Instructions( double azimuth) {
    Location start = new Location("loc");
    start.setLatitude(checkProximity.getLatitude());
    start.setLongitude(checkProximity.getLongitude());
    Location end = new Location("locc");
    fetchLastLocation();
    end.setLatitude(currentLocation.getLatitude());
    end.setLongitude(currentLocation.getLongitude());
    double latitude = abs(end.getLongitude()) - abs(start.getLongitude());
    if(latitude > 0 && !way) {
        atLeft = true;
        turn = abs((180f - azimuth));
       Toast.makeText(getApplicationContext(), "Turn left " + Math.round(turn) + " degrees.You need to walk " + Math.round(start.distanceTo(end)) + " meters.", Toast.LENGTH_LONG).show();

    }
    else if(latitude < 0&& !way){
        atLeft = false;
        turn = abs(180 - azimuth);
       Toast.makeText(getApplicationContext(), "Turn right " + Math.round(turn) + " degrees.You need to walk " + Math.round(start.distanceTo(end)) + " meters.", Toast.LENGTH_LONG).show();

    }



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
            dist.setText("Closest bus at " + Math.round(currentLocation.distanceTo(checkProximity)) + " meters, real value of " + distValue);
            duration.setText("Your bus will arive in " + value + ".");

        }
    }
}
