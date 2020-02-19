package com.example.myapplication;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
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

import java.util.Arrays;
import java.util.Locale;

import static java.lang.StrictMath.abs;


public class CompassActivity  extends AppCompatActivity implements View.OnClickListener, SensorEventListener, TextToSpeech.OnInitListener {


    private boolean busSelected;
    private boolean checkCheck;

    @Override
    public void onInit(int status) {
    }
            //ImageViews
    ImageView compass_img;
    ImageView arrowView;
    ImageView bus;

            //TextView
    TextView txt_compass;
    TextView dist;
    TextView duration;
    TextView velocity;
    TextView busId;

             //double
    double mAzimuth;
    private double turn;
    public double mag;

            //Sensors
    private SensorManager mSensorManager;
    private Sensor mRotationV, mAccelerometer, mMagnetometer;

             //Floats
    float[] rMat = new float[9];
    float[] orientation = new float[3];
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];

            //Booleans
    boolean haveSensor = false, haveSensor2 = false;
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private boolean atLeft;
    private boolean way;
    public boolean aboardBus = false;
    public boolean busAtStop = false;
    public boolean busClose = false;

            //TextToSpeech
    public TextToSpeech tts;

            //Vibrator
    Vibrator v;

            //Button
    public Button checkBus;

            //ProgressDialog
    private ProgressDialog pd;

            //Handler
    Handler handler = new Handler();
    Handler vibratorHandler = new Handler();

            //Runnable
    Runnable runnable;
    Runnable runnable2;

            //int
    int delay = 25 * 1000;
    int delayy = 2 * 1000;
    private int index;
    private int veclopis;
    private int dis;
    private static final int REQUEST_CODE = 1011;
    public int distance;

            //Strings
    String busNumber;
    String distValue;
    String line;
    private String value;
    private static final String TAG = "Compass";
    public static String currentPosition = "-22.92715, -43.25187";
    public static String nowLocation;

            //Locations
    FusedLocationProviderClient fusedLocationProviderClient;
    private Location location;
    public Location pPosition;
    public Location checkProximity;

    public void Instructions( double azimuth) {
        if(mAzimuth < 180 && !way) {
            atLeft = false;
            turn = abs((azimuth));
            Toast.makeText(getApplicationContext(), "Vire " + Math.round(turn) + "graus a esquerda.Ande " + Math.round(checkProximity.distanceTo(pPosition)) + " meters.", Toast.LENGTH_LONG).show();
        }
        else if(mAzimuth > 180&& !way){
            atLeft = true;
            turn = abs(azimuth);
            Toast.makeText(getApplicationContext(), "Vire " + Math.round(turn) + " graus a direita.Ande " + Math.round(checkProximity.distanceTo(pPosition)) + " meters.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        //Location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();

        Intent intent = getIntent();
        line = intent.getStringExtra("bubus");
        line = "coconut 133";
        line = line.replaceAll("[^0-9.]", "");
                    //VIEWS
        //Aboard Bus button
        checkBus = findViewById(R.id.atBus);
        checkBus.setOnClickListener(this);
        checkBus.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
        //Images
        compass_img = findViewById(R.id.img_compass);
        bus =findViewById(R.id.busStop);
        //Text
        busId = findViewById(R.id.busId);
        dist = findViewById(R.id.distance);
        duration = findViewById(R.id.duration);
        velocity = findViewById(R.id.veclopis);
        txt_compass = findViewById(R.id.txt_azimuth);
        tts = new TextToSpeech(this,this);
        //Vibrator Service
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //Well, sotwFormatter
        location = MainActivity.currentLocation;
        //Stuff to be executed
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        bus.setVisibility(View.INVISIBLE);
    }

    private void fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                pPosition = location;
                if(!checkCheck)
                new Warming().execute();
            }
        });
    }

    public void onClick(View v) {
        if (v == checkBus) {
            if (busAtStop) {
                aboardBus = true;
                Toast.makeText(this, "Você está no ônibus.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "você possui esquizofrenia. não há nenhum ônibus.", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    protected void onResume() {
        vibratorHandler.postDelayed(runnable = () -> {
            if (busAtStop) {
                checkCheck = true;
                fetchLastLocation();
                if (mAzimuth < 15 || mAzimuth > 355) {
                    way = true;
                } else {
                    way = false;
                }
                if (way) {
                    long[] pattern = {0, 200, 200, 300};
                    v.vibrate(pattern, 0);
                }
                if (!way) {
                    Instructions(mAzimuth);
                    v.cancel();
                }
            }
            handler.postDelayed(runnable, delayy);
        }, delayy);
        handler.postDelayed(runnable2 = () -> {
            if (busNumber != null)
                if (aboardBus == false) {
                    fetchLastLocation();
                }
            handler.postDelayed(runnable2, delay);
        }, delay);
        start();
        super.onResume();
    }

    //TODO: Checking closest bus in the line
    public class Warming extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pd = new ProgressDialog(CompassActivity.this);
            pd.setCancelable(false);
            pd.show();
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            Http sh = new Http();
            // Making a request to url and getting response
            String jsonJedi = sh.makeServiceCall("http://dadosabertos.rio.rj.gov.br/apiTransporte/apresentacao/rest/index.cfm/onibus/" + line + ".json");
            if (jsonJedi != null) {
                try {
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


                           //Bus id for each instance
                           algorithmBus[i] = bus.getString(1);
                           //Bus speed
                            speed[i] = bus.getInt(5);
                           if(busSelected == false) {
                           //Bus - current Location
                               distanceLat[i] = abs(abs(latitudes[i]) - abs(pPosition.getLatitude()));
                               distanceLng[i] = abs(abs(longitudes[i]) - abs(pPosition.getLongitude()));
                           //Sum of lat and lng
                               calculations[i] = distanceLat[i] + distanceLng[i];
                           //Bus Location
                                checkProximity = new Location("abc");
                                //checkProximity.setLatitude(latitudes[i]);
                                //checkProximity.setLongitude(longitudes[i]);
                           }
                       else{
                           if(algorithmBus[i] == busNumber)
                           {
                               busNumber = algorithmBus[i];
                               checkProximity.setLatitude(latitudes[i]);
                               checkProximity.setLongitude(longitudes[i]);
                               veclopis=speed[i];
                               nowLocation = latitudes[i] + "," + longitudes[i];

                           }
                       }
                    }
                    double great = Arrays.stream(calculations).min().getAsDouble();
                   if(busSelected == false) {
                       for (int i = 0; i < calculations.length; i++) {
                           if (calculations[i] == great) {
                               index = i;
                               busNumber = algorithmBus[index];
                               veclopis = speed[index];
                               nowLocation = latitudes[index] + "," + longitudes[index];

                           }
                       }
                       checkProximity.setLatitude(latitudes[index]);
                       checkProximity.setLongitude(longitudes[index]);
                   }
                       //Bus distance to user
                       distance = (int) checkProximity.distanceTo(pPosition);

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
            duration.setText( "Linha: " + line);
            dis = 10;
            if(!aboardBus) {
                if (dis < 50.0f) {
                    busClose = true;
                    //float val = Math.round(dis/veclopis);
                    //duration.setText("Tempo de chegada: " + val + ".");
                   // tts.setLanguage(Locale.forLanguageTag("pt"));
                   // tts.speak("Tempo de chegada: " + val,TextToSpeech.QUEUE_ADD, null);
                    if (dis < 15) {
                        if (veclopis != 0) {
                            tts.speak("Espere o ônibus parar", TextToSpeech.QUEUE_ADD, null);
                            busAtStop = false;
                        }
                        //Entrance instructions
                        if (veclopis == 0 && !busAtStop) {
                            busAtStop = true;
                            tts.setLanguage(Locale.forLanguageTag("pt"));
                            tts.speak("Seu ônibus parou. Siga as próximas instruções!", TextToSpeech.QUEUE_ADD, null);
                        }
                        //Aboard the bus
                        if (dis < 2) {
                            aboardBus = true;
                        }
                    }
                    else {
                        busSelected = false;
                        tts.setLanguage(Locale.forLanguageTag("pt"));
                        tts.speak("Há um ônibus perto. Sinalize para o " + busNumber, TextToSpeech.QUEUE_ADD, null);

                    }
                }
                else {
                    //new busDriving().execute();
                }
                if(busAtStop) {
                    bus.setVisibility(View.VISIBLE);
                    busSelected = true;
                }
                if(!busAtStop) {
                    bus.setVisibility(View.INVISIBLE);
                    busSelected = false;
                }
            }
            tts.setLanguage(Locale.forLanguageTag("pt"));
            tts.speak(getString(R.string.onibus)+ dis + getString(R.string.metro),TextToSpeech.QUEUE_ADD, null);
            tts.speak(getString(R.string.ui) + veclopis + getString(R.string.speed ),TextToSpeech.QUEUE_ADD, null);
            busId.setText(busNumber);
            velocity.setText(veclopis + getString(R.string.speed ));
            dist.setText(getString(R.string.onibus)+ dis + getString(R.string.metro));
            Toast.makeText(getApplicationContext(),"LOCATION:"+ pPosition.getLatitude() +", " + pPosition.getLongitude() + " BUS: "+ checkProximity.getLatitude() + ", " + checkProximity.getLongitude(), Toast.LENGTH_LONG).show();
            start();

        }
    }

    //TODO: Navigations time and distance
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
            Log.i(TAG, "Tempo de chegada: " + value);
            duration.setText("Tempo de chegada: " + value + ".");
            tts.setLanguage(Locale.forLanguageTag("pt"));
            tts.speak("Tempo de chegada: " + value,TextToSpeech.QUEUE_ADD, null);



        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {



            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        mag = mAzimuth;
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }
        if(checkProximity != null){
            toBus(pPosition.getLatitude(), pPosition.getLongitude(), checkProximity.getLatitude(), checkProximity.getLongitude());
        }
        mAzimuth = Math.round(mAzimuth);
        compass_img.setRotation(-Math.round(mAzimuth));

        txt_compass.setText(mAzimuth + "° ");
    }

    public void toBus(double startLat, double startLng, double endLat, double endLng)
    {
        double angle = Math.atan((endLat - startLat)/(endLng - startLng));
        double b = 90 - angle;
        double mod = (mag +b)%360;
        mAzimuth = mod;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void start() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if ((mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) || (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null)) {
                noSensorsAlert();
            }
            else {
                mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                haveSensor = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
                haveSensor2 = mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
            }
        }
        else{
            mRotationV = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            haveSensor = mSensorManager.registerListener(this, mRotationV, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void noSensorsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Your device doesn't support the Compass.")
                .setCancelable(false)
                .setNegativeButton("Close",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        alertDialog.show();
    }
    @Override
    public void onStop() {
        super.onStop();
        if (haveSensor) {
            mSensorManager.unregisterListener(this, mRotationV);
        } else {
            mSensorManager.unregisterListener(this, mAccelerometer);
            mSensorManager.unregisterListener(this, mMagnetometer);
        }
    }


}