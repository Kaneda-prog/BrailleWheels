package com.example.myapplication;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.VoiceInteractor;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpResponse;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationListener;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import okhttp3.internal.http.HttpHeaders;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.lang.StrictMath.abs;
import static java.sql.DriverManager.println;


public class CompassActivity  extends AppCompatActivity implements LocationListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, SensorEventListener, TextToSpeech.OnInitListener {


    private boolean busSelected = false;
    private boolean checkCheck;

    private Marker marker;
    private Marker markerr;
    private Marker m;
    private Button exitButton;
    private MediaPlayer mp;

    Handler bluetoothIn;

    final int handlerState = 0;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private OutputStream outStream = null;

    private static final int SolicitaAtivaçao = 1;

    private static final int SolicitaConexao = 2;

    //Entrada das informações para fazer a interação entre o celular e o módulo bluetooth
    private BluetoothAdapter meuBluetooth = null;

    // SPP UUID service - isso deve funcionar para a maioria dos dispositivos
    private static final UUID MEU_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String para o endereço MAC
    //quando usa lista de dispositivos, se não colocar igual a null da erro
    private static String MAC = null;

    @Override
    public void onInit(int status) {
    }

    private GoogleMap mMap;
    //ImageViews
    ImageView compass_img;
    ImageView busStop;
    ImageView busGo;
    ImageView busAboard;

    //TextView
    TextView txt_compass;
    TextView dist;
    TextView duration;
    TextView velocity;
    TextView lineView;

    //double
    double mAzimuth;
    private double lati = -22.93107;
    private double longi = -43.17901;
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
    private Handler handlerr;

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
    public static String busNumber;
    String distValue;
    public static String line = MainActivity.linha;
    static public String stops1;
    static public String stops2;
    static public String stops3;
    static public String money;
    static public String point1;
    private String value;
    private static final String TAG = "Compass";
    public static String nowLocation;

    //Locations
    FusedLocationProviderClient fusedLocationProviderClient;

    LocationRequest mLocationRequest;

    private Location location;
    public Location pPosition;
    public Location busLocation = new Location("location");
    private Location previousLocation;Location mCurrentLocation;

    public final static int SENDING = 1;
    public final static int CONNECTING = 2;
    public final static int ERROR = 3;
    public final static int SENT = 4;
    public final static int SHUTDOWN = 5;

    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;

    GoogleApiClient mGoogleApiClient;

    LatLng previouslatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        meuBluetooth = BluetoothAdapter.getDefaultAdapter();

        //Verifica se o didpositivo tem bluetooth
        if(meuBluetooth == null){
            //Se não tiver, a mensagem abaixo será mostrada e o programa será encerrado
            Toast.makeText(getApplicationContext(), "Seu dispositivo não possui bluetooth", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        /*bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {										//se a mensagem é o que queremos,
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes de conexão thread
                    recDataString.append(readMessage);      								//Pega os dados doa sensores até a string '~'
                    int endOfLineIndex = recDataString.indexOf("~");                       // que determina o final de linha
                    if (endOfLineIndex > 0) {
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extrai a string
                        Log.i(TAG,"Data Received = " + dataInPrint);
                        int dataLength = dataInPrint.length();							//Pega o tamanho dos dados recebidos
                        Log.i(TAG,"String Length = " + String.valueOf(dataLength));

                        if (recDataString.charAt(0) == '#')								//se ele começa com # sabemos que é o que estamos procurando
                        {
                            String sensor0 = recDataString.substring(1, 5);             //obtem o valor do sensor entre índices 1-5

                            Log.i(TAG," Sensor 0 Voltage = " + sensor0 + "cm");	//coloca o valor recebido no textview

                        }
                        recDataString.delete(0, recDataString.length()); 					//limpa as strings
                        // strIncom =" ";
                        dataInPrint = " ";
                    }
                }
            }
        };
        //Se o bluetooth não estivver ativado, será solicitada a ativação do mesmo
        //Através do intent, que inicia uma nova ação
        if(!meuBluetooth.isEnabled()){
            Intent solicita = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);//Cria o intent
            startActivityForResult(solicita,SolicitaAtivaçao);//Starta o intent
        }*/
        //Intent
        Intent intent = getIntent();
        if(MainActivity.cool.isChecked()) {
            line = MainActivity.linha;
        }
        else{
            line = "133";
        }
        line = line.trim();
        stops1 = intent.getStringExtra("stops1");
        money = intent.getStringExtra("price");
        point1 = intent.getStringExtra("firstStop");

        //Location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        //VIEWS
        //Butons
        checkBus = findViewById(R.id.atBus);
        checkBus.setOnClickListener(this);
        checkBus.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
        exitButton = findViewById(R.id.exit);
        exitButton .setOnClickListener(this);

        //Images
        compass_img = findViewById(R.id.img_compass);
        busStop = findViewById(R.id.busStop);
        busAboard = findViewById(R.id.busAt);
        busGo   = findViewById(R.id.busGo);
        //Text
        dist = findViewById(R.id.distance);
        duration = findViewById(R.id.duration);
        velocity = findViewById(R.id.veclopis);
        txt_compass = findViewById(R.id.txt_azimuth);
        lineView = findViewById(R.id.line);
        tts = new TextToSpeech(getApplicationContext(), this);
        //Vibrator Service
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        location = MainActivity.currentLocation;
        //Stuff to be executed
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        busStop.setVisibility(View.INVISIBLE);
        busAboard.setVisibility(View.INVISIBLE);
        busGo.setVisibility(View.INVISIBLE);
        fetchLastLocation();
        DebugPlace();
        createLocationRequest();

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
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
        //conectar();
    }
    public void intent()
    {
        Intent au = new Intent(getApplicationContext(), AboardBus.class);
        startActivity(au);

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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){

            case SolicitaAtivaçao:
                if(resultCode ==Activity.RESULT_OK)//Se o bluetooth for ligado, a mensagem abaixo será mostrada
                {                                   //E o progrma continuará sendo executado
                    Toast.makeText(getApplicationContext(), "O BLUETOOTH FOI LIGADO!", Toast.LENGTH_LONG).show();
                }else//Se o bluetooth não foi ativado, a mensagem abaixo será mostrada e o programa será fechado
                {
                    Toast.makeText(getApplicationContext(), "O BLUETOOTH NÃO FOI LIGADO!", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            case SolicitaConexao:
                if(resultCode==Activity.RESULT_OK){
                    MAC = data.getExtras().getString(ListadeDispositivos.EnderecoMAC);

                    //Para se ter um bluetoothdevice é necessário uilizar o BluetoothAdapter.getRemoteDevice(string)
                    //Que representa um endereço Mac conhecido, que já foi apresentado no início
                    BluetoothDevice device = meuBluetooth.getRemoteDevice(MAC);
                    try{
                        //A função device.createRfcommSocketToServiceRecord(MEU_UUID) abre m conexão
                        //Entre o dispositivo e o módulo
                        btSocket = device.createRfcommSocketToServiceRecord(MEU_UUID);
                        //É iniciada a saída d dados do dispositivo
                        btSocket.connect();

                        //Se der tudo certo na hora da conexão, irá aparecer a tela do controle
                        if(btSocket!=null){
                            Toast.makeText(getApplicationContext(), "A CONEXÃO FOI BEM SUCEDIDA!", Toast.LENGTH_LONG).show();
                        }
                    }catch(IOException e){
                        Toast.makeText(getApplicationContext(), "ERRO AO FAZER CONEXÃO", Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(getApplicationContext(), "Falha ao obter o endereço MAC", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
    public void conectar(){
        Intent abreLista = new Intent(CompassActivity.this,ListadeDispositivos.class);
        startActivityForResult(abreLista, SolicitaConexao);
    }

    //Definição da função desconectar
    public void desconectar(){
        try{
            btSocket.close();//Fecha a conexão
            btSocket = null;//E a conexão volta a ser nula

        }catch(IOException e){

        }
    }
    public void Instructions(double azimuth) {
        if (mAzimuth < 180 && !way) {
            atLeft = false;
            turn = abs((azimuth));
              }
        else if (mAzimuth > 180 && !way) {
            atLeft = true;
            turn = abs(azimuth);

             }
    }
    public void onClick(View v) {
        if (v == checkBus) {
            if(!MainActivity.cool.isChecked())
                intent();
            //Se o clique for verdadeiro
            if (busAtStop) {
                aboardBus = true;
                intent();
                Toast.makeText(this, "Você está no ônibus.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "você possui esquizofrenia. não há nenhum ônibus.", Toast.LENGTH_SHORT).show();

            }
        }
        if(v == exitButton)
        {
            finish();
            System.exit(0);
        }
    }
    //TODO: remove debug lixeira location
    private void DebugPlace() {
        pPosition = new Location("aak");
        pPosition.setLatitude(lati);
        pPosition.setLongitude(longi);
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
                    Log.i(TAG, "LINHAA" + line);
                }
            handler.postDelayed(runnable2, delay);
        }, delay);
        start();
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        handler.removeCallbacks(runnable2);
        vibratorHandler.removeCallbacks(runnable);
        mSensorManager.unregisterListener(this, mRotationV);
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
        mSensorManager = null;
        Log.d(TAG, "ow");

    }
    @Override
    protected void onStop() {
        super.onStop();
        //Stop UpdateBusInfo callback
        handler.removeCallbacks(runnable2);
        //Stop compass callback
        vibratorHandler.removeCallbacks(runnable);

        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(mMap.MAP_TYPE_SATELLITE);
        Log.i(TAG, " HEELPPPP ");
        //Remove existing marker, if existed
        if (markerr != null) {
            markerr.remove();
            //markerr = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
        int height = 100;
        int width = 100;
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.blind);
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
        Bitmap bb = BitmapFactory.decodeResource(getResources(), R.drawable.bubs);
        Bitmap smallMarkerr = Bitmap.createScaledBitmap(bb, width, height, false);
        BitmapDescriptor smallMarkerIconn = BitmapDescriptorFactory.fromBitmap(smallMarkerr);
        // Add a marker at current place, zoom and move the camera

            Log.i(TAG, " HELP ME ");
            LatLng lating = new LatLng(pPosition.getLatitude(), pPosition.getLongitude());
            markerr = mMap.addMarker(new MarkerOptions().position(lating).title(lating.latitude + ", " + lating.longitude).icon(smallMarkerIcon));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(lating));
            int distanc = Math.round(pPosition.distanceTo(busLocation));
            double middleLat = (busLocation.getLatitude()- pPosition.getLatitude())/2 + (pPosition.getLatitude());
            double middleLng = (busLocation.getLongitude()- pPosition.getLongitude())/2+(pPosition.getLongitude());
        LatLng latLng = new LatLng(middleLat, middleLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom(distanc)));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }
        if (busLocation != null) {
            pPosition.setLatitude(lati);
            pPosition.setLongitude(longi);

            //Remove existing busStop marker
            if (marker != null) {
                marker.remove();
            }
            LatLng lat = new LatLng(busLocation.getLatitude(), busLocation.getLongitude());
            marker = mMap.addMarker(new MarkerOptions().position(lat).title("Your busStop").icon(smallMarkerIconn));
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }
    @Override
    public void onLocationChanged(Location location) {
        previouslatLng = new LatLng(location.getLatitude(), location.getLongitude());
        ////pPosition = location;
        duration.setText( pPosition.getLatitude() + ", " + pPosition.getLongitude());
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
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    public void playMusic(String musicFile)
    {
        int resID = getResources().getIdentifier(musicFile, "raw", getPackageName());
        mp = MediaPlayer.create(this, resID);
        mp.start();
    }
    //TODO: Checking closest busStop in the line
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
            Log.i(TAG, " A linha linha "+ line + ".");
            String url = "http://dadosabertos.rio.rj.gov.br/apiTransporte/apresentacao/rest/index.cfm/onibus/" + line +".json";
            String jsonJedi = sh.makeServiceCall(url );
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
                        if (busSelected == false) {
                            //Bus - current Location
                            distanceLat[i] = abs(abs(latitudes[i]) - abs(pPosition.getLatitude()));
                            distanceLng[i] = abs(abs(longitudes[i]) - abs(pPosition.getLongitude()));
                            //Sum of lat and lng
                            calculations[i] = distanceLat[i] + distanceLng[i];
                        } else {
                            if (algorithmBus[i] == busNumber) {
                                //busNumber = algorithmBus[i];
                                busLocation.setLatitude(latitudes[i]);
                                busLocation.setLongitude(longitudes[i]);
                                veclopis = speed[i];
                                nowLocation = latitudes[i] + "," + longitudes[i];

                            }
                        }
                    }
                    double great = Arrays.stream(calculations).min().getAsDouble();
                    if (busSelected == false) {
                        for (int i = 0; i < calculations.length; i++) {
                            if (calculations[i] == great) {
                                index = i;
                                Log.i(TAG, "Bus at order: " + index + "is the closest.");
                                busNumber = algorithmBus[index];
                                veclopis = speed[index];
                                nowLocation = latitudes[index] + "," + longitudes[index];

                            }
                        }
                        busLocation = new Location("abc");
                        busLocation.setLatitude(latitudes[index]);
                        busLocation.setLongitude(longitudes[index]);
                    }

                    Log.i(TAG, "Bus location at warming: " + busLocation.getLatitude() + ", " + busLocation.getLongitude() + "Stop location at warming: " + pPosition.getLatitude() + ", " + pPosition.getLongitude());

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
            if (!aboardBus) {
                if (dis < 50.0f) {
                    busGo.setVisibility(View.VISIBLE);
                    busClose = true;

                    if (dis < 19) {
                        if (veclopis != 0) {
                            tts.speak("Espere o ônibus parar", TextToSpeech.QUEUE_ADD, null);
                            busAtStop = false;
                        }
                        //Entrance instructions
                        if (veclopis == 0 && !busAtStop) {
                            busAtStop = true;
                            playMusic("busready");
                            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    tts.setLanguage(Locale.forLanguageTag("pt"));
                                    tts.speak("Seu ônibus parou. Siga as próximas instruções!", TextToSpeech.QUEUE_ADD, null);

                                }
                            });

                        }
                        //Aboard the busStop
                        if (dis < 2) {
                            aboardBus = true;
                            intent();
                        }
                    } else {
                        //float val = Math.round(dis/veclopis);
                        //duration.setText("Tempo de chegada: " + val + ".");
                        //tts.setLanguage(Locale.forLanguageTag("pt"));
                        //tts.speak("Tempo de chegada: " + val,TextToSpeech.QUEUE_ADD, null);
                        busAtStop = false;
                        tts.setLanguage(Locale.forLanguageTag("pt"));
                        tts.speak("Há um ônibus perto. Sinalize para o " + busNumber, TextToSpeech.QUEUE_ADD, null);

                    }
                } else {
                    busStop.setVisibility(View.INVISIBLE);
                    busGo.setVisibility(View.INVISIBLE);

                }
                if (busAtStop) {
                    busStop.setVisibility(View.VISIBLE);
                    busGo.setVisibility(View.INVISIBLE);
                    busSelected = true;
                }
                if (!busAtStop) {
                    busStop.setVisibility(View.INVISIBLE);
                    busSelected = false;
                }
                new busDriving().execute();
            }
            lineView.setText(busNumber + " --- " + line);
            velocity.setText("Dirige a " + veclopis + getString(R.string.speed));
            dist.setText(getString(R.string.onibus) + dis + getString(R.string.metro));
            sayInfo();
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(CompassActivity.this);
            start();

                String urlParameters = "/Login/Autenticar?token=aa578a3a758b2b862d5c990057cad8b238699c1a540a65130f985b713b2c51e1";
                //url = new URL("http://api.olhovivo.sptrans.com.br/v2.1");



        }

}
    public void sayInfo()
    {
        tts.setLanguage(Locale.forLanguageTag("pt"));
        tts.speak(getString(R.string.onibus)+ dis + " " + getString(R.string.metro),TextToSpeech.QUEUE_ADD, null);
        if(veclopis != 0) {
            tts.speak(getString(R.string.ui) + veclopis + " " + getString(R.string.speed), TextToSpeech.QUEUE_ADD, null);
        }
        tts.setLanguage(Locale.forLanguageTag("pt"));
        tts.speak("Tempo de chegada: " + value, TextToSpeech.QUEUE_ADD, null);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_HEADSETHOOK){
            sayInfo();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    //<a href="https://www.freepik.com/free-photos-vectors/design">Design vector created by freepik - www.freepik.com</a> COMPASS
    ////<a href="https://www.freepik.com/free-photos-vectors/design">Design vector created by freepik - www.freepik.com</a>//<a href="https://www.freepik.com/free-photos-vectors/design">Design vector created by freepik - www.freepik.com</a>
    //Icons made by <a href="https://www.flaticon.com/<?=_('authors/')?>freepik" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" title="Flaticon"> www.flaticon.com</a>
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
            duration.setText("Tempo de chegada: " + "1 minuto" + ".");
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
            //TODO: remove debug lixeira location
            pPosition.setLatitude(lati);
            pPosition.setLongitude(longi);
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
        mAzimuth = (bearTo - mag) *-1;
        //mAzimuth += 16;

        if(mAzimuth < 0f) {
            mAzimuth = 360 + mAzimuth;
        }
        else if(mAzimuth > 360) {

            mAzimuth = mAzimuth - 360;
        }

        Math.round(-mAzimuth/ 360 + 180);
        //Log.i(TAG, " Round Bearing Azimuth " + mAzimuth);
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