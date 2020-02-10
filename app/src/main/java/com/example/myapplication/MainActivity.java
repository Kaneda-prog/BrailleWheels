package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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


public class MainActivity extends AppCompatActivity implements OnClickListener {
    private String TAG = MainActivity.class.getSimpleName();
    public String voice;
    public String latLng;
    public String info;
    public TextView title;
    public ListView myList;
    public Button myButton;
    public Switch cool;
    public WebView view;
    PlacesClient placesClient;

    public static String BUS_NUMBER;
    public String BUS_STOP;
    private static final int REQUEST_CODE = 10;
    public static final int VOICE_RECOGNIZITION_REQUESTCODE = 1234;
    public static int curRoute;

    public static Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    ProgressDialog pd;
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 3001;
    static public JSONArray json_object;
    boolean enjoy = false;
    //-22.9596397,-43.2011472
    JSONObject leObject;
    ArrayList<HashMap<String, String>> contactList;
    Locale locale = new Locale("pt", "BR");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale.setDefault(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            askPermission();
        }
        setContentView(R.layout.activity_main);
        myList = findViewById(R.id.list);
        title = findViewById(R.id.title);
        //Set button
        myButton = findViewById(R.id.speak);
        myButton.setOnClickListener(this);
        //view = findViewById(R.id.web);
        contactList = new ArrayList<>();
        //Locations utils
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
        //Routes list overview

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent in = new Intent(getApplicationContext(), CompassActivity.class);
                TextView firstEtape = view.findViewById(R.id.stopLocation0);
                in.putExtra("firstStop",firstEtape.getText());
                BUS_NUMBER = firstEtape.toString();
                if(view.findViewById(R.id.stopLocation1) != null)
                {
                    TextView secondEtape = view.findViewById(R.id.stopLocation1);
                    in.putExtra("sncStop",secondEtape.getText());
                    if(view.findViewById(R.id.stopLocation1) != null) {
                        TextView thirdEtape = view.findViewById(R.id.stopLocation2);
                        in.putExtra("thdStop",thirdEtape.getText());
                    }
                }
                Log.e(TAG," A POTATO "+ firstEtape.getText());
                curRoute = position +1;
                Log.i(TAG, "This " + curRoute);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    startService(new Intent(MainActivity.this, FloatingOverMapIconService.class));
                    finish();
                    startActivity(in);
                } else if (Settings.canDrawOverlays(getApplicationContext())) {
                    startService(new Intent(MainActivity.this, FloatingOverMapIconService.class));
                    finish();
                    startActivity(in);
                } else {
                    askPermission();
                    Toast.makeText(getApplicationContext(), "You need System Alert Window Permission to do this", Toast.LENGTH_SHORT).show();
                    startActivity(in);
                }


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

    }
    private void askPermission() {
        if (!Settings.canDrawOverlays(getApplicationContext())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION ,Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 0);
        }
    }
public void text()
{

    Toast.makeText(this, BUS_NUMBER, Toast.LENGTH_LONG).show();
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
                }
            }

        });
    }

    public void onClick(View v) {
        if (v == myButton) {
            fetchLastLocation();
            Intent on = new Intent(this,CompassActivity.class);
            startActivity(on);
                //startVoiceRecognizitionActivity();
//voice = "Maracana";
            //new GetContacts().execute();
        }
    }

    public void voiceinputbuttons() {
        myButton = findViewById(R.id.speak);

    }

    public void startVoiceRecognizitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().getLanguage());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "say where you want to go NOW");
        startActivityForResult(intent, VOICE_RECOGNIZITION_REQUESTCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_RECOGNIZITION_REQUESTCODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            voice = matches.get(0);
            voice.replaceAll("","+");
            voice.replaceAll("-","+");
            Log.i(TAG, "You said " + voice);
            new GetContacts().execute();
        }
    }


    private class GetContacts extends AsyncTask<Void, Void, Void> {
        public int baldiacao[];
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

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Https sh = new Https();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall("https://maps.googleapis.com/maps/api/directions/json?origin="+latLng+"&destination="+voice+"%2CRio+de+Janeiro&mode=transit&alternatives=true&transit_mode=bus&key=AIzaSyA2n7hH6W6cHvZdRX2kBmL0b21ev6WWjag");


            Log.d(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node
                    JSONArray points = jsonObj.getJSONArray("geocoded_waypoints");
                    JSONObject destination = points.getJSONObject(1);

                    JSONArray routes = jsonObj.getJSONArray("routes");
                    // looping through All Routes
                    for (int i = 0; i < routes.length(); i++) {
                        HashMap<String, String> contact = new HashMap<>();
                        Log.i(TAG, "This is route no " +i);
                        baldiacao = new int[routes.length()];
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

                            //Looping through the steps
                            for (int e = 0; e< steps.length(); e++) {
                                JSONObject a = steps.getJSONObject(e);
                                String mode = a.getString("travel_mode");
                                Log.i(TAG, "The mode is " + mode +" for route no "+ i + " at leg " + o +", and at steps " + e );
                                try{
                                    if(mode.equals("TRANSIT")) {
                                        num++;
                                        contact.put("totalTime", "Leave at "+ departureT + " and arrive at " + arrivalT);
                                        contact.put("distance", "You'll travel " + dist);
                                        contact.put("duration", "Arrive after " + durationT);
                                        contact.put("price", "Total price: " + text);
                                        contact.put("val", "Route number: " + gole);
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
                                            contact.put("depStopLocation" + nm, depStopLocation);
                                            contact.put("busNumber" + nm, busNumber +" ");
                                            contact.put("busName" + nm, busName);
                                            contact.put("color" + nm, color);
                                            contact.put("numStops" + nm, stopsNum);
                                            if(nm > 0) {

                                            }
                                            Log.e(TAG, "Number of bus stops is " + stopsNum);
                                            Log.wtf(TAG, "We have added " + nm +" bus jumps, that's right.");
                                            Log.i(TAG, "The starting bus Stop for step " + e + " is at " + depStopLocation);
                                            Log.i(TAG, "The ending bus Stop for step " + e + " is at " + arStopLocation);
                                        }
                                        Log.i(TAG,"For loop finished." );

                                    }
                                    else{
                                        throw  new Exception("walking is not what we want right now");
                                    }
                                    // adding contact to contact list
                                    if(steps.length() > 1) {
                                        Log.i(TAG, "LOOKA!");
                                        if(e == (steps.length()-1))
                                            contactList.add(contact);
                                    }
                                    else {
                                        Log.i(TAG, "looka!");
                                        contactList.add(contact);
                                    }
                                    baldiacao[i] = num;
                                    Log.e(TAG, "Nice, now we have the number of changes as " + num);
                                }
                                catch (Exception e1) {
                                    Log.e(TAG, "Hey!" + e1 + " >:(");
                                }

                            }
                            // adding each child node to HashMap key => value


                            /**
                             Icons made by <a href="https://www.flaticon.com/<?=_('authors/')?>smashicons" title="Smashicons">Smashicons</a> from <a href="https://www.flaticon.com/" title="Flaticon"> www.flaticon.com</a>
                             Icons made by <a href="https://www.flaticon.com/<?=_('authors/')?>photo3idea-studio" title="photo3idea_studio">photo3idea_studio</a> from <a href="https://www.flaticon.com/" title="Flaticon"> www.flaticon.com</a>
                             **/

                            // adding contact to contact list

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
        public void makeStuff(){




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
                    "price","distance","busNumber0","busNumber1","busNumber2","val","stopsNum0", "depStopLocation0","depStopLocation1","depStopLocation2" }, new int[]{R.id.totalTime,
                    R.id.duration, R.id.price, R.id.distance, R.id.busNumber0, R.id.busNumber1, R.id.busNumber2, R.id.route, R.id.stops, R.id.stopLocation0,R.id.stopLocation1,R.id.stopLocation2});
            myList.setAdapter(adapter);
            Log.i(TAG, "uh" );
        }
    }
}