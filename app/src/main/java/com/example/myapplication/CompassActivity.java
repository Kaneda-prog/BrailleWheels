package com.example.myapplication;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Locale;

import static java.lang.StrictMath.abs;


public class CompassActivity  extends AppCompatActivity implements LocationListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, SensorEventListener, TextToSpeech.OnInitListener {


    private boolean busSelected;
    private boolean checkCheck;
    private Marker marker;
    private Marker markerr;
    private double lati = -22.95965;
    private double longi = -43.20119;

    @Override
    public void onInit(int status) {
    }

    private GoogleMap mMap;
    //ImageViews
    ImageView compass_img;
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

    public static String nowLocation;

    //Locations
    FusedLocationProviderClient fusedLocationProviderClient;
    private Location location;
    public Location pPosition;
    public Location busLocation = new Location("location");

    private final int MY_LOCATION_REQUEST_CODE = 100;
    private Handler handlerr;
    private Marker m;
//    private GoogleApiClient googleApiClient;

    public final static int SENDING = 1;
    public final static int CONNECTING = 2;
    public final static int ERROR = 3;
    public final static int SENT = 4;
    public final static int SHUTDOWN = 5;

    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    Button btnFusedLocation;
    TextView tvLocation;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    private Location previousLocation;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.i(TAG, " HEELPPPP ");
        //Remove existing marker, if existed
        if (markerr != null) {
            markerr.remove();
            //markerr = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
        // Add a marker at current place, zoom and move the camera
        if (pPosition != null) {
            Log.i(TAG, " HELP ME ");
            LatLng lating = new LatLng(pPosition.getLatitude(), pPosition.getLongitude());
            markerr = mMap.addMarker(new MarkerOptions().position(lating).title(lating.latitude + ", " + lating.longitude));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(lating));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lating, 19));
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }
        if (busLocation != null) {
            busLocation.setLatitude(lati);
            busLocation.setLongitude(longi);

            //Remove existing bus marker
            if (marker != null) {
                marker.remove();
            }
            LatLng lat = new LatLng(busLocation.getLatitude(), busLocation.getLongitude());
            marker = mMap.addMarker(new MarkerOptions().position(lat).title("Your bus").icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
    }

    public void rotateMarker(final Marker marker, final float toRotation, final float st) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = st;
        final long duration = 1555;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;

                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.d(TAG, "Location update started ..............: ");
    }

    LatLng previouslatLng;

    @Override
    public void onLocationChanged(Location location) {
        previouslatLng = new LatLng(location.getLatitude(), location.getLongitude());
        //pPosition = location;
        duration.setText( pPosition.getLatitude() + ", " + pPosition.getLongitude());
        double rota = 0.0;
        double startrota = 0.0;
        if (previousLocation != null) {

            rota = bearingBetweenLocations(previouslatLng, new LatLng(location.getLatitude
                    (), location.getLongitude()));
        }


        rotateMarker(m, (float) rota, (float) startrota);

        previousLocation = location;
        Log.d(TAG, "Firing onLocationChanged..........................");
        Log.d(TAG, "lat :" + location.getLatitude() + "long :" + location.getLongitude());
        Log.d(TAG, "bearing :" + location.getBearing());

        animateMarker(new LatLng(location.getLatitude(), location.getLongitude()), false);
//        new ServerConnAsync(handler, MapsActivity.this,location).execute();


    }
    private double bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
        Log.d(TAG, "Location update stopped .......................");
    }

    public void animateMarker(final LatLng toPosition, final boolean hideMarke) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(m.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 5000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                m.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarke) {
                        m.setVisible(false);
                    } else {
                        m.setVisible(true);
                    }
                }
            }
        });
    }


    public void Instructions(double azimuth) {
        if (mAzimuth < 180 && !way) {
            atLeft = false;
            turn = abs((azimuth));
            // Toast.makeText(getApplicationContext(), "Vire " + Math.round(turn) + "graus a esquerda.Ande " + Math.round(busLocation.distanceTo(pPosition)) + " meters.", Toast.LENGTH_LONG).show();
        } else if (mAzimuth > 180 && !way) {
            atLeft = true;
            turn = abs(azimuth);

            //Toast.makeText(getApplicationContext(), "Vire " + Math.round(turn) + " graus a direita.Ande " + Math.round(busLocation.distanceTo(pPosition)) + " meters.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        //Location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
        DebugPlace();
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

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
        bus = findViewById(R.id.busStop);
        //Text
        busId = findViewById(R.id.busId);
        dist = findViewById(R.id.distance);
        duration = findViewById(R.id.duration);
        velocity = findViewById(R.id.veclopis);
        txt_compass = findViewById(R.id.txt_azimuth);
        tts = new TextToSpeech(this, this);
        //Vibrator Service
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //Well, sotwFormatter
        location = MainActivity.currentLocation;
        //Stuff to be executed
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        bus.setVisibility(View.INVISIBLE);

        handlerr = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case SENDING:

                        break;

                }

            }

        };
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
                new UpdateBusInfo().execute();
            }
        });
    }

    public void onClick(View v) {
        if (v == checkBus) {
            //Se o clique for verdadeiro
            if (busAtStop) {
                aboardBus = true;
                Toast.makeText(this, "Você está no ônibus.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "você possui esquizofrenia. não há nenhum ônibus.", Toast.LENGTH_SHORT).show();

            }
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        //Stop UpdateBusInfo callback
        handler.removeCallbacks(runnable2);
        //Stop compass callback
        vibratorHandler.removeCallbacks(runnable);
        if (haveSensor) {
            mSensorManager.unregisterListener(this, mRotationV);
        } else {
            mSensorManager.unregisterListener(this, mAccelerometer);
            mSensorManager.unregisterListener(this, mMagnetometer);
        }
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }
    @Override
    protected void onResume() {
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
        vibratorHandler.postDelayed(runnable = () -> {
            //fetchLastLocation();
            //Log.i(TAG, " help me");
            if (busAtStop && !aboardBus) {
                checkCheck = true;
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
    public class UpdateBusInfo extends AsyncTask<Void, Void, Void> {
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
                           }
                            else{
                                if(algorithmBus[i] == busNumber)
                                {
                               //busNumber = algorithmBus[i];
                               busLocation.setLatitude(latitudes[i]);
                               busLocation.setLongitude(longitudes[i]);
                               veclopis = speed[i];
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
                       busLocation = new Location("abc");
                       busLocation.setLatitude(latitudes[index]);
                       busLocation.setLongitude(longitudes[index]);
                   }
                       //Bus distance to user
                       distance = (int) busLocation.distanceTo(pPosition);

                       Log.i(TAG, "Bus location at warming: " + busLocation + "Stop location at warming: " + pPosition);

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
                runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                        "Couldn't get json from server. Check LogCat for possible errors!",
                        Toast.LENGTH_LONG)
                        .show());

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
           // Dismiss the progress dialog
            if (pd.isShowing())
                pd.dismiss();
            DebugPlace();
            dis = Math.round(busLocation.distanceTo(pPosition));
            if(!aboardBus) {
                if (dis < 50.0f) {
                    busClose = true;
                    //float val = Math.round(dis/veclopis);
                    //duration.setText("Tempo de chegada: " + val + ".");
                    //tts.setLanguage(Locale.forLanguageTag("pt"));
                    //tts.speak("Tempo de chegada: " + val,TextToSpeech.QUEUE_ADD, null);
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
            tts.speak(getString(R.string.onibus)+ dis + " " + getString(R.string.metro),TextToSpeech.QUEUE_ADD, null);
           if(veclopis != 0) {
               tts.speak(getString(R.string.ui) + veclopis + " " + getString(R.string.speed), TextToSpeech.QUEUE_ADD, null);
           }
            busId.setText(busNumber);
            velocity.setText(veclopis + getString(R.string.speed ));
            dist.setText(getString(R.string.onibus)+ dis + getString(R.string.metro));
            Toast.makeText(getApplicationContext(),"LOCATION:"+ pPosition.getLatitude() +", " + pPosition.getLongitude() + " BUS: "+ busLocation.getLatitude() + ", " + busLocation.getLongitude(), Toast.LENGTH_LONG).show();
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(CompassActivity.this);
            start();

        }
    }

    private void DebugPlace() {
        busLocation.setLatitude(lati);
        busLocation.setLongitude(longi);
    }

    //TODO: Navigation's time and distance
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
            String jsonStr = sh.makeServiceCall("https://maps.googleapis.com/maps/api/directions/json?origin=" + nowLocation + "&destination=" + pPosition + "&key=AIzaSyA2n7hH6W6cHvZdRX2kBmL0b21ev6WWjag");
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
            tts.speak("Tempo de chegada: " + value, TextToSpeech.QUEUE_ADD, null);


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

        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }
        mag = mAzimuth;

        if(busLocation != null && pPosition != null){
            busLocation.setLatitude(lati);
            busLocation.setLongitude(longi);
            //Log.i(TAG, "check " + busLocation.getLatitude());
            toBus(pPosition.getLatitude(), pPosition.getLongitude(), busLocation.getLatitude(), busLocation.getLongitude());
        }
        mAzimuth = Math.round(mAzimuth);
        compass_img.setRotation(-Math.round(mAzimuth));

        txt_compass.setText(mAzimuth + "° ");
    }

    public void toBus(double startLat, double startLng, double endLat, double endLng) {
        double angle = Math.atan(abs((endLat - startLat))/abs((endLng - startLng)));
        double b = 90 - angle;
        double mod = (mag +b)%360;

        float bearTo= pPosition.bearingTo(busLocation);
        Log.i(TAG, "Bearing " + bearTo);
        //normalizeDegree(bearTo);
        Log.i(TAG, "Normal Azimuth " + mAzimuth);
        mAzimuth = (bearTo - mag) *-1;
        Log.i(TAG, "Bearing Azimuth " + mAzimuth);
        //mAzimuth += 16;

        if(mAzimuth < 0f) {
            mAzimuth = 360 + mAzimuth;
            Log.i(TAG, " Negative Bearing Azimuth " + mAzimuth);
        }
        else if(mAzimuth > 360) {

            mAzimuth = mAzimuth - 360;
        }

        Math.round(-mAzimuth/ 360 + 180);
        Log.i(TAG, " Round Bearing Azimuth " + mAzimuth);
    }
    private float normalizeDegree(float value){
        if (value >= 0.0f && value <= 180.0f) {
            return value;
        } else {
            return(value + 360) % 360;
        }

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


}