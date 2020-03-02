package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.BitmapDescriptor;
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

import static com.example.myapplication.CompassActivity.busNumber;
import static java.lang.StrictMath.abs;

public class AboardBus extends AppCompatActivity implements LocationListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    String stops = CompassActivity.point1;
    String price = CompassActivity.money;
    String point = CompassActivity.stops1;

    TextView dist;
    TextView duration;
    TextView velocity;

    LocationRequest mLocationRequest;

    private Marker marker;
    private Marker markerr;
    private Marker m;
    private GoogleMap mMap;
    private static final String TAG = "AboardBus";
    private Location previousLocation;
    Location mCurrentLocation;

    public final static int SENDING = 1;
    public final static int CONNECTING = 2;
    public final static int ERROR = 3;
    public final static int SENT = 4;
    public final static int SHUTDOWN = 5;

    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private static final int REQUEST_CODE = 101221;
    GoogleApiClient mGoogleApiClient;
    FusedLocationProviderClient fusedLocationProviderClient;
    LatLng previouslatLng;


    public TextToSpeech tts;
    private Location pPosition;
    private Location busStop;
    private ProgressDialog pd;
    Handler handler = new Handler();
    private Runnable runnable2;
    private long delay = 25 * 1000;
    private float zoom;
    private int speed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboard_bus);
        Log.i(TAG, "HAHAHAHAHAHAH" + stops);
        String stop[] = stops.split(",");
        double lat = Double.valueOf(stop[0]);
        double lng = Double.valueOf(stop[1]);
        busStop = new Location("bs");
        busStop.setLatitude(lat);
        busStop.setLongitude(lng);
        dist = findViewById(R.id.distance);
        velocity = findViewById(R.id.speed);
        duration = findViewById(R.id.duration);
        Log.i(TAG, " ============ABOARDBUS===========");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        tts =new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.forLanguageTag("pt"));
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("error", "This Language is not supported");
                } else {
                            tts.speak("O preço é "+ price, TextToSpeech.QUEUE_FLUSH, null);

                }
            } else {
                Log.e("error", "Initilization Failed!");
            }
        });
        fetchLastLocation();
    }
    public float zoom(int distance)
    {

        if(distance <10000) {
            float zoom = 12;
            if (distance <( 6000)){
                zoom = 13;

                    if (distance <( 1000)){
                        zoom = 15;
                        if (distance <( 800)){
                            zoom = 16;
                            if (distance <( 200)){
                                zoom = 18;

                            }
                        }

                }
            }

            return zoom;
        }



        return distance;
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
                new UpdateBusInfo().execute();
            }
        });
    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
        Log.d(TAG, "Location update stopped .......................");
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(mMap.MAP_TYPE_SATELLITE);

        //Remove existing marker, if existed
        if (markerr != null) {
            markerr.remove();
            //markerr = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
        int height = 100;
        int width = 100;
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.bubs);
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
        Bitmap bb = BitmapFactory.decodeResource(getResources(), R.drawable.bus_stop);
        Bitmap smallMarkerr = Bitmap.createScaledBitmap(bb, width, height, false);
        BitmapDescriptor smallMarkerIconn = BitmapDescriptorFactory.fromBitmap(smallMarkerr);
        // Add a marker at current place, zoom and move the camera
        if (pPosition != null) {
            Log.i(TAG, " HELP ME ");
            LatLng lating = new LatLng(pPosition.getLatitude(), pPosition.getLongitude());
            markerr = mMap.addMarker(new MarkerOptions().position(lating).title(lating.latitude + ", " + lating.longitude).icon(smallMarkerIcon));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(lating));
            int distance = Math.round(pPosition.distanceTo(busStop));
            double middleLat = (busStop.getLatitude()- pPosition.getLatitude())/2 + (pPosition.getLatitude());
            double middleLng = (busStop.getLongitude()- pPosition.getLongitude())/2+(pPosition.getLongitude());
            LatLng latLng = new LatLng(middleLat, middleLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom(distance)));
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }
        if (busStop != null) {
            //busLocation.setLatitude(lati);
            //busLocation.setLongitude(longi);

            //Remove existing busStop marker
            LatLng lat = new LatLng(busStop.getLatitude(), busStop.getLongitude());
            marker = mMap.addMarker(new MarkerOptions().position(lat).title("Your busStop").icon(smallMarkerIconn));
        }
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
    public void onLocationChanged(Location location) {
        checkingCheck(location);
        Log.i(TAG, "Alas, a location is a location");
        Toast.makeText(getApplicationContext(),"NICE", Toast.LENGTH_LONG).show();
        previouslatLng = new LatLng(location.getLatitude(), location.getLongitude());
        pPosition = location;
        double rota = 0.0;
        double startrota = 0.0;
        if (previousLocation != null) {

            rota = bearingBetweenLocations(previouslatLng, new LatLng(location.getLatitude
                    (), location.getLongitude()));
        }


        //rotateMarker(m, (float) rota, (float) startrota);

        previousLocation = location;
        Log.d(TAG, "Firing onLocationChanged..........................");
        Log.d(TAG, "lat :" + location.getLatitude() + "long :" + location.getLongitude());
        Log.d(TAG, "bearing :" + location.getBearing());

        animateMarker(new LatLng(location.getLatitude(), location.getLongitude()), false);
        //new ServerConnAsync(handler, CompassActivity.this,location).execute();

    }
    public void checkingCheck(Location location)
    {
        Log.i(TAG, "Alas, a location is a location");
        Toast.makeText(getApplicationContext(),"CheckingCheck", Toast.LENGTH_LONG).show();
        if(location.distanceTo(busStop) < 10)
        {
            Toast.makeText(getApplicationContext(),"NICE", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable2 = () -> {
                    new UpdateBusInfo().execute();
            handler.postDelayed(runnable2, delay);
        }, delay);

    }

    public class UpdateBusInfo extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pd = new ProgressDialog(AboardBus.this);
            pd.setCancelable(false);
            pd.show();
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            Http sh = new Http();
            // Making a request to url and getting response
            String jsonJedi = sh.makeServiceCall("http://dadosabertos.rio.rj.gov.br/apiTransporte/apresentacao/rest/index.cfm/onibus/" + busNumber + ".json");
            if (jsonJedi != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonJedi.substring(jsonJedi.indexOf("{"), jsonJedi.lastIndexOf("}") + 1));
                    JSONArray points = jsonObj.getJSONArray("DATA");
                    JSONArray bus = points.getJSONArray(0);;

                    //Bus speed
                    speed = bus.getInt(5);
                    double latitude = bus.getDouble(3);
                    double longitude = bus.getDouble(4);
                    pPosition.setLatitude(latitude);
                    pPosition.setLongitude(longitude);

                    //Log.i(TAG, "Bus location at warming: " + busStop.getLatitude() + ", " + busStop.getLongitude() + "Stop location at warming: " + pPosition.getLatitude() + ", " + pPosition.getLongitude());

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

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(AboardBus.this);
            createLocationRequest();
            float dis = Math.round(pPosition.distanceTo(busStop));
            dist.setText("Distância do ponto: " + dis + " metros.");
            velocity.setText("Velocidade: " + speed);
            duration.setText("Pontos até o ponto final: "+ point );
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onProviderEnabled(String provider) {

    }
    @Override
    public void onProviderDisabled(String provider) {

    }
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


}
