package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements OnClickListener, TextToSpeech.OnInitListener {
    private String TAG = MainActivity.class.getSimpleName();
    public String voice;
    public String latLng;
    public String info;
    public TextView title;
    public ImageView backrgound;
    public ImageView sim;
    public ListView myList;
    public Button myButton;
    public static Switch cool;


    public static String BUS_NUMBER;
    public String BUS_STOP;
    private static final int REQUEST_CODE = 10;
    public static final int VOICE_RECOGNIZITION_REQUESTCODE = 1234;
    public static int curRoute;

    public static Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    ProgressDialog pd;
    static public JSONArray json_object;
    boolean enjoy = true;
    ArrayList<HashMap<String, String>> contactList;
    Locale locale = new Locale("pt", "BR");
    public TextToSpeech tts;
    private MediaPlayer mp;
    Geocoder geocoder;
    List<Address> addresses;
    private String address;
    private String knownName;
    private PlacesClient placesClient;
    private String placeId;
    private AccessibilityManager accessibilityManager;
    private AccessibilityEvent accessibilityEvent;
    public static boolean sp;
    public static boolean rj;


    @Override
    public void onInit(int status) {

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        accessibilityManager = (AccessibilityManager) this.getSystemService(Context.ACCESSIBILITY_SERVICE);
        accessibilityEvent = AccessibilityEvent.obtain();
        accessibilityEvent.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);

        Places.initialize(getApplicationContext(), "AIzaSyA2n7hH6W6cHvZdRX2kBmL0b21ev6WWjag");

// Create a new Places client instance
         placesClient = Places.createClient(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            askPermission();
        }
        Locale.setDefault(locale);
        cool = findViewById(R.id.enjoy);
        myList = findViewById(R.id.list);
        title = findViewById(R.id.title);
        backrgound = findViewById(R.id.bb);
        sim = findViewById(R.id.sim);
        backrgound.setVisibility(View.VISIBLE);
        sim.setVisibility(View.VISIBLE);
        //Set button
        myButton = findViewById(R.id.speak);
        myButton.setOnClickListener(this);
        contactList = new ArrayList<>();
        //Locations utils
        int resID = getResources().getIdentifier("start", "raw", getPackageName());
        mp = MediaPlayer.create(getApplicationContext(), resID);
        mp.start();
        //Routes list overview
        tts =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.forLanguageTag("pt"));
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    } else {

                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                        {
                            @Override
                            public void onCompletion(MediaPlayer mp)
                            {
                                tts.speak("Olá! Bem vindo ao S.I.M, o sistema inteligente de mobilidade! Na versão beta, espera-se que você já esteja no ponto de ônibus. Clique no botão começar em baixo da tela e diga para onde você quer ir!", TextToSpeech.QUEUE_FLUSH, null);
                            }
                        });

                    }
                } else {
                    Log.e("error", "Initilization Failed!");
                }
            }
        });
         myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int resID = getResources().getIdentifier("whoosh", "raw", getPackageName());
                mp = MediaPlayer.create(getApplicationContext(), resID);
                mp.start();
                Intent in = new Intent(getApplicationContext(), CompassActivity.class);
                TextView money = view.findViewById(R.id.price);
                TextView line1 = view.findViewById(R.id.busNumber0);
                TextView firstEtape = view.findViewById(R.id.stopLocation0);
                TextView firstStops = view.findViewById(R.id.stopNum0);
                in.putExtra("firstStop",firstEtape.getText());
                in.putExtra("price", money.getText());
                in.putExtra("stops1",firstStops.getText());
                in.putExtra("bubus", line1.getText());
                BUS_NUMBER = firstEtape.toString();
                if(view.findViewById(R.id.stopLocation1) != null)
                {
                    TextView secondEtape = view.findViewById(R.id.stopLocation1);
                    TextView secondStops = view.findViewById(R.id.stopNum1);
                    in.putExtra("sncStop",secondEtape.getText());
                    in.putExtra("stops2",secondStops.getText());
                    if(view.findViewById(R.id.stopLocation1) != null) {
                        TextView thirdEtape = view.findViewById(R.id.stopLocation2);
                        TextView thirdStops = view.findViewById(R.id.stopNum2);
                        in.putExtra("stops3",thirdStops.getText());
                        in.putExtra("thdStop",thirdEtape.getText());
                    }
                }

                Log.e(TAG," A POTATO "+ firstEtape.getText());
                curRoute = position +1;
                Log.i(TAG, "This " + curRoute);
                startActivity(in);
            }
        });

        voiceinputbuttons();
        PackageManager pm = getPackageManager();
        List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            myButton.setOnClickListener(this);
        } else {
            myButton.setEnabled(false);
            myButton.setText("Recognizer not present");

        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
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
                    currentLocation = location;
                    latLng = currentLocation.getLatitude() +"," +currentLocation.getLongitude();
                    geocoder = new Geocoder(getApplication(), Locale.getDefault());
                    try {
                        addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    Log.i(TAG, "hey " + address);
                    if(address.contains("São Paulo"))
                    {
                        Log.i(TAG, "olá nacional de são paulo!");
                        sp = true;
                        rj= false;
                    }
                    if(address.contains("Rio de Janeiro"))
                    {
                        Log.i(TAG, "você está no rio de janeiro!");
                        sp = false;
                        rj = true;
                    }
                }
            }

        });
    }
    private void askPermission() {
        if (!Settings.canDrawOverlays(getApplicationContext())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION ,Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 0);
        }
    }
    public void onClick(View v) {
        if (v == myButton) {
            if(!cool.isChecked()) {
                fetchLastLocation();
                Intent in = new Intent(getApplicationContext(), CompassActivity.class);
                in.putExtra("firstStop", "-22.9316609,-43.1826521");
                in.putExtra("price", "R$4,60");
                in.putExtra("stops1", "10");
                in.putExtra("bubus", "133");
                in.putExtra("sncStop", "cool");
                in.putExtra("stops2", "cool");
                in.putExtra("stops3", "cool");
                in.putExtra("thdStop", "cool");
                startActivity(in);
            }
            else{
                tts.shutdown();
                fetchLastLocation();
            /*Intent on = new Intent(this,CompassActivity.class);
            on.putExtra("bubus", voice);
            startActivity(on);*/
                startVoiceRecognizitionActivity();
            }
        }
    }

    public void voiceinputbuttons() {
        myButton = findViewById(R.id.speak);
    }

    public void startVoiceRecognizitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().getLanguage());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga seu destino!");
        startActivityForResult(intent, VOICE_RECOGNIZITION_REQUESTCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_RECOGNIZITION_REQUESTCODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            voice = matches.get(0);
            Log.i(TAG, "You said " + voice);
            /*Intent on = new Intent(this,CompassActivity.class);
            on.putExtra("bubus", voice);
            startActivity(on);*/
            voice.replaceAll("","+");
            voice.replaceAll("-","+");
            backrgound.setVisibility(View.INVISIBLE);
            sim.setVisibility(View.INVISIBLE);
            new GetContacts().execute();
        }
    }

    private class GetContacts extends AsyncTask<Void, Void, Void> {
        public String stopsNum;
        private String dist;



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.show();
            myList.clearChoices();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Https sh = new Https();

            String jsonStr = sh.makeServiceCall("https://maps.googleapis.com/maps/api/directions/json?origin="+latLng+"&destination="+voice+"%2CRio+de+Janeiro&region=br&mode=transit&alternatives=true&transit_mode=bus&key=AIzaSyA2n7hH6W6cHvZdRX2kBmL0b21ev6WWjag");


            Log.d(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node
                    JSONArray points = jsonObj.getJSONArray("geocoded_waypoints");
                    JSONObject destination = points.getJSONObject(1);
                    placeId = destination.getString("place_id");
                    Log.i(TAG, placeId);

// Specify the fields to return.

                    JSONArray routes = jsonObj.getJSONArray("routes");
                    // looping through All Routes
                    for (int i = 0; i < routes.length(); i++) {
                        HashMap<String, String> contact = new HashMap<>();
                        Log.i(TAG, "This is route no " +i);
                        JSONObject c = routes.getJSONObject(i);
                        //The route

                        String gole = String.valueOf(i +1);
                        Log.i(TAG, "Lets confirm... Route number: " + gole);
                        //Route info
                        JSONObject phone = c.getJSONObject("fare");
                        String currency = phone.getString("currency");
                        String text = phone.getString("text");
                        String value = phone.getString("value");


                        //Looping through all Legs in all routes
                        JSONArray legs = c.getJSONArray("legs");
                        for (int o = 0; o < legs.length(); o++) {
                            Log.i(TAG, "This is leg no " +o);
                            int num = 0;
                            int nm = 0;
                            JSONObject d = legs.getJSONObject(o);
                            JSONObject arrival = d.getJSONObject("arrival_time");
                            String arrivalT = arrival.getString("text");
                            JSONObject departure = d.getJSONObject("departure_time");
                            String departureT = departure.getString("text");
                            JSONObject distance = d.getJSONObject("distance");
                            dist = distance.getString("text");
                            JSONObject duration = d.getJSONObject("duration");
                            String durationT = duration.getString("text");
                            JSONArray steps = d.getJSONArray("steps");
                            if(o == 0) {
                                JSONObject end_location = d.getJSONObject("end_location");
                                double destLat = end_location.getDouble("lat");
                                double destLng = end_location.getDouble("lng");
                                geocoder = new Geocoder(getApplication(), Locale.getDefault());
                                try {
                                    addresses = geocoder.getFromLocation(destLat, destLng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                String city = addresses.get(0).getLocality();
                                String state = addresses.get(0).getAdminArea();
                                String country = addresses.get(0).getCountryName();
                                String postalCode = addresses.get(0).getPostalCode();
                            }
                            //Looping through the steps
                            for (int e = 0; e< steps.length(); e++) {
                                JSONObject a = steps.getJSONObject(e);
                                String mode = a.getString("travel_mode");
                                Log.i(TAG, "The mode is " + mode +" for route no "+ i + " at leg " + o +", and at steps " + e );
                                try{
                                    if(mode.equals("TRANSIT")) {
                                        num++;
                                        contact.put("totalTime", "Saia "+ departureT + " e chegue " + arrivalT);
                                        contact.put("distance", "Você percorrerá " + dist);
                                        contact.put("duration", "Tempo total: " + durationT);
                                        contact.put("price", "Preço total " + text);
                                        contact.put("val", "Rota " + gole);
                                        Log.i(TAG, "We have now " + num + " transit jumps in route no " + i + "at leg " + o);
                                        for (nm = (num-1); nm < num; nm++) {

                                            JSONObject details = a.getJSONObject("transit_details");
                                            JSONObject departure_stop = details.getJSONObject("departure_stop");
                                            JSONObject aLocation = departure_stop.getJSONObject("location");
                                            String lat = aLocation.getString("lat");
                                            String lng = aLocation.getString("lng");
                                            String depStopLocation = lat + "," + lng;
                                            JSONObject arrival_stop = details.getJSONObject("arrival_stop");
                                            JSONObject arLocation = arrival_stop.getJSONObject("location");
                                            String lati = arLocation.getString("lat");
                                            String lngo = arLocation.getString("lng");
                                            String arStopLocation = lati + "," + lngo;
                                            JSONObject line = details.getJSONObject("line");
                                            String busNumber = line.getString("short_name");
                                            JSONObject vehicle = line.getJSONObject("vehicle");
                                            String type = vehicle.getString("type");
                                            String color = line.getString("color");
                                            String busName = vehicle.getString("name");
                                            stopsNum = details.getString("num_stops");
                                            contact.put("stopsNum"+ nm, stopsNum);
                                            contact.put("depStopLocation" + nm, depStopLocation);
                                            contact.put("busNumber" + nm, busNumber +" ");
                                            contact.put("busName" + nm, busName);
                                            contact.put("color" + nm, color);
                                            contact.put("numStops" + nm, stopsNum);
                                            Log.i(TAG, busNumber);
                                            Log.e(TAG, "Number of busStop stops is " + stopsNum);
                                            Log.wtf(TAG, "We have added " + nm +" busStop jumps, that's right.");
                                            Log.i(TAG, "The starting busStop Stop for step " + e + " is at " + depStopLocation);
                                            Log.i(TAG, "The ending busStop Stop for step " + e + " is at " + arStopLocation);
                                        }
                                        Log.i(TAG,"For loop finished." );
                                    }
                                    else{
                                        if(steps.length() > 1) {
                                            Log.i(TAG, "Length!" + steps.length() + "   a   " + e);
                                            if(e == (steps.length()-1)) {
                                                contactList.add(contact);
                                                Log.i(TAG, "LOOKA!");
                                            }

                                        }
                                        else {
                                            contactList.add(contact);
                                            Log.i(TAG, "looka!");
                                        }
                                        throw  new Exception("walking is not what we want right now");
                                    }
                                    // adding contact to contact list
                                    if(steps.length() > 1) {
                                        Log.i(TAG, "Length!" + steps.length() + "   a   " + e);
                                        if(e == (steps.length()-1)) {
                                            contactList.add(contact);
                                            Log.i(TAG, "LOOKA!");
                                        }

                                    }
                                    else {
                                        contactList.add(contact);
                                        Log.i(TAG, "looka!");
                                    }
                                    Log.e(TAG, "Nice, now we have the number of changes as " + num);
                                }
                                catch (Exception e1) {
                                    Log.e(TAG, "Hey!" + e1 + " >:(");
                                }

                            }
                            /**
                             Icons made by <a href="https://www.flaticon.com/<?=_('authors/')?>smashicons" title="Smashicons">Smashicons</a> from <a href="https://www.flaticon.com/" title="Flaticon"> www.flaticon.com</a>
                             Icons made by <a href="https://www.flaticon.com/<?=_('authors/')?>photo3idea-studio" title="photo3idea_studio">photo3idea_studio</a> from <a href="https://www.flaticon.com/" title="Flaticon"> www.flaticon.com</a>
                             **/
                        }
                    }
                }
                catch (final JSONException e) {
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
            /**
             Updating parsed JSON data into ListView
             **/
            Log.i(TAG, "ah" );

            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, contactList,
                    R.layout.list_item, new String[]{"totalTime","duration",
                    "price","distance","busNumber0","busNumber1","busNumber2","val","stopsNum0", "depStopLocation0","depStopLocation1","depStopLocation2","stopsNum0","stopsNum1","stopsNum2"}, new int[]{R.id.totalTime,
                    R.id.duration, R.id.price, R.id.distance, R.id.busNumber0, R.id.busNumber1, R.id.busNumber2, R.id.route, R.id.stops, R.id.stopLocation0,R.id.stopLocation1,R.id.stopLocation2,R.id.stopNum0,R.id.stopNum1,R.id.stopNum2});
            myList.setAdapter(adapter);
            myList.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            accessibilityEvent.getText().add("Text to be spoken by TalkBack");
            if (accessibilityManager != null) {
                accessibilityManager.sendAccessibilityEvent(accessibilityEvent);
            }
            List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

// Construct a request object, passing the place ID and fields array.
            FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

            placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                Place place = response.getPlace();
                Log.i(TAG, "Place found: " + place.getName());
                title.setText(place.getName() + "    " + address);
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    // Handle error with given status code.
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                }
            });
            Toast.makeText(getApplicationContext(), "List added", Toast.LENGTH_LONG).show();
            Log.i(TAG, "uh" );
        }
    }
}