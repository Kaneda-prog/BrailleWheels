package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnClickListener {
    private String TAG = MainActivity.class.getSimpleName();
    public ListView myList;
    public Button myButton;
    public Switch cool;
    public WebView view;

    public String BUS_NUMBER;
    public String BUS_STOP;
    private static final int REQUEST_CODE = 10;
    public static final int VOICE_RECOGNIZITION_REQUESTCODE = 1234;
    public static int curRoute;

    static Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    ProgressDialog pd;

    static public JSONArray json_object;
    boolean enjoy = false;
    String url = "https://maps.googleapis.com/maps/api/directions/json?origin=Liceu+Franco+Brasileiro&destination=Maracana%2CRio+de+Janeiro&mode=transit&alternatives=true&transit_mode=bus&key=AIzaSyA2n7hH6W6cHvZdRX2kBmL0b21ev6WWjag";
    JSONObject leObject;
    ArrayList<HashMap<String, String>> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myList = findViewById(R.id.list);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent in = new Intent(getApplicationContext(), RouteInfo.class);

                curRoute = position +1;
                in.putExtra("route",curRoute);
                Log.i(TAG, "This" + curRoute);
                startActivity(in);

            }
        });
        contactList = new ArrayList<>();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
        //view = findViewById(R.id.web);
new GetContacts().execute();
        if (currentLocation != null && view!=null) {

            WebSettings webSettings = view.getSettings();
            view.getSettings().setJavaScriptEnabled(true);
            view.addJavascriptInterface(new WebAppInterface(this), "Android");
            view.setWebViewClient(new WebViewClient());
            //https://www.google.com/maps/dir/?api=1&origin=default&destination=Maracana%2CRio+de+Janeiro&travelmode=transit
            view.loadUrl("file:///android_asset/www/app.js");
            Toast.makeText(this, currentLocation.getLatitude() + ".", Toast.LENGTH_SHORT).show();
        }
        cool = findViewById(R.id.switch1);
        cool.setOnClickListener(this);
        myButton = findViewById(R.id.speak);
        myButton.setOnClickListener(this);
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
                    /*WebSettings webSettings = view.getSettings();
                    view.getSettings().setJavaScriptEnabled(true);
                    view.setWebViewClient(new WebViewClient());
                    view.addJavascriptInterface(new WebAppInterface(MainActivity.this), "Android");
                    //https://www.google.com/maps/dir/?api=1&origin=default&destination=Maracana%2CRio+de+Janeiro&travelmode=transit
                    //view.loadUrl("file:///android_asset/www/app.js");
                    //view.loadUrl("https://maps.googleapis.com/maps/api/directions/json?origin=Liceu+Franco+Brasileiro&destination=Maracana%2CRio+de+Janeiro&mode=transit&alternatives=true&key=AIzaSyA2n7hH6W6cHvZdRX2kBmL0b21ev6WWjag");
                    */
                }
            }

        });
    }

    public void onClick(View v) {
        if (v == myButton) {
            if (enjoy) {
                startVoiceRecognizitionActivity();
            } else {
                Intent intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
            }
        }
        if (v == cool)
            enjoy = !enjoy;
    }

    public void voiceinputbuttons() {
        myButton = findViewById(R.id.speak);

    }

    public void startVoiceRecognizitionActivity() {


        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say start!");
        startActivityForResult(intent, VOICE_RECOGNIZITION_REQUESTCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_RECOGNIZITION_REQUESTCODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            myList.setAdapter((new ArrayAdapter(this, android.R.layout.simple_list_item_1, matches)));
            if (matches.contains("start")) {
                Intent intent = new Intent(this, MapsActivity.class);
                startActivity(intent);


            }
        }
    }


    private class GetContacts extends AsyncTask<Void, Void, Void> {
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
            String jsonStr = sh.makeServiceCall(url);

            Log.d(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node
                    JSONArray routes = jsonObj.getJSONArray("routes");
                    json_object = routes;
                    // looping through All Routes
                    for (int i = 0; i < routes.length(); i++) {

                        JSONObject c = routes.getJSONObject(i);
                        HashMap<String, String> contact = new HashMap<>();
                        int gole = i +1;
                       /* String id = c.getString("transit_details");
                        String name = c.getString("name");
                        String email = c.getString("email");
                        String duration = c.getString("duration");
                        String gender = c.getString("departure_stop");
*/
                        // Phone node is JSON Object
                        JSONObject phone = c.getJSONObject("fare");
                        String currency = phone.getString("currency");
                        String text = phone.getString("text");
                        String value = phone.getString("value");

                        //Looping through all Legs in all routes
                        JSONArray legs = c.getJSONArray("legs");
                        for (int o = 0; o < legs.length(); o++) {
                            JSONObject d = legs.getJSONObject(o);

                            JSONObject arrival = d.getJSONObject("arrival_time");
                            String arrivalT = arrival.getString("text");

                            //Info
                            JSONObject departure = d.getJSONObject("departure_time");
                            String departureT = departure.getString("text");
                            JSONObject distance = d.getJSONObject("distance");
                            String dist = distance.getString("text");
                            JSONObject duration = d.getJSONObject("duration");
                            String durationT = duration.getString("text");

                            JSONArray steps = d.getJSONArray("steps");
                            for (int e = 0; e< steps.length(); e++) {
                                JSONObject a = steps.getJSONObject(e);
                                String mode = a.getString("travel_mode");

                                Log.i(TAG, "Hello" + mode +i);
                                int num = 0;
                                if (mode == "TRANSIT"){
                                    Log.i(TAG, "OH YEAH");
                                }else{
                                    throw new JSONException("a");
                                }
                                if(a.getString("travel_mode") == "TRANSIT") {
                                    Log.i(TAG, "OH YEAH");
                                    num ++;
                                    /**
                                    JSONObject details = a.getJSONObject("transit_details");
                                    JSONObject aStop = details.getJSONObject("arrival_stop");
                                    JSONObject aLocation = aStop.getJSONObject("location");
                                    String lat = aLocation.getString("lat");
                                    String lng = aLocation.getString("lng");
                                    String stopLocation = lat + ", " + lng;
                                    JSONObject line = a.getJSONObject("line");
                                    String busNumber = line.getString("shortname");
                                    String type = line.getString("type");
                                    String color = line.getString("color");
                                    String busName = line.getString("name");
                                    String stopsNum = a.getString("num_stops");
                                    Log.e(TAG, "Hello" + stopsNum);
                                    Log.println(Log.VERBOSE, TAG, stopsNum);
                                     **/

                                }


                            }

                            // tmp hash map for single contact
                            //contact.put("stopsNum", stopsNum );
                            // adding each child node to HashMap key => value
                            contact.put("totalTime", departureT + " > " + arrivalT);
                            contact.put("distance", dist);
                            contact.put("duration", durationT);
                            contact.put("price", text);
                            contact.put("val", gole + ".");


                            // adding contact to contact list
                            contactList.add(contact); /**

                                    //Icons made by <a href="https://www.flaticon.com/<?=_('authors/')?>smashicons" title="Smashicons">Smashicons</a> from <a href="https://www.flaticon.com/" title="Flaticon"> www.flaticon.com</a>
                                    //Icons made by <a href="https://www.flaticon.com/<?=_('authors/')?>photo3idea-studio" title="photo3idea_studio">photo3idea_studio</a> from <a href="https://www.flaticon.com/" title="Flaticon"> www.flaticon.com</a>

                                   **/

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
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, contactList,
                    R.layout.list_item, new String[]{"totalTime", "duration",
                    "price", "distance", "busNumber","val", "stopsNum"}, new int[]{R.id.totalTime,
                    R.id.duration, R.id.price, R.id.distance, R.id.busNumber, R.id.route, R.id.stops});

            myList.setAdapter(adapter);

    }
}
}