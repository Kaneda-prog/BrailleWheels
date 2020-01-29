package com.example.myapplication;

import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.example.myapplication.MainActivity.curRoute;
import static com.example.myapplication.MainActivity.json_object;

public class RouteInfo extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    TextView text;
    int route;
    private String TAG = RouteInfo.class.getSimpleName();
    ProgressDialog pd;
    String url = "https://maps.googleapis.com/maps/api/directions/json?origin=Liceu+Franco+Brasileiro&destination=Maracana%2CRio+de+Janeiro&mode=transit&alternatives=true&transit_mode=bus&key=AIzaSyA2n7hH6W6cHvZdRX2kBmL0b21ev6WWjag";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GetContacts().execute();
        setContentView(R.layout.activity_route_info);
        text = findViewById(R.id.info);

        route = getIntent().getIntExtra("route",curRoute ) ;
        text.setText("You are traveling at " + route);
        //Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, route, Toast.LENGTH_SHORT).show();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
    private class GetContacts extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pd = new ProgressDialog(RouteInfo.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {


                try {
                    JSONObject c = json_object.getJSONObject(route);
                    JSONArray legs = c.getJSONArray("legs");
                    JSONObject l = legs.getJSONObject(0);


                    JSONObject details = l.getJSONObject("transit_details");
                    String name = c.getString("name");
                    String email = c.getString("email");
                    String duration = c.getString("duration");
                    String gender = c.getString("departure_stop");

/*
                                    String busNumber = line.getString("shortname");
                                    //Icons made by <a href="https://www.flaticon.com/<?=_('authors/')?>smashicons" title="Smashicons">Smashicons</a> from <a href="https://www.flaticon.com/" title="Flaticon"> www.flaticon.com</a>
                                    //Icons made by <a href="https://www.flaticon.com/<?=_('authors/')?>photo3idea-studio" title="photo3idea_studio">photo3idea_studio</a> from <a href="https://www.flaticon.com/" title="Flaticon"> www.flaticon.com</a>
                                    String type = line.getString("type");
                                    JSONObject aStop = details.getJSONObject("arrival_stop");
                                    JSONObject aLocation = aStop.getJSONObject("location");
                                    String lat = aLocation.getString("lat");
                                    String lng = aLocation.getString("lng");
                                    String stopLocation = lat + ", " + lng;

*/
                    // tmp hash map for single contact
                    HashMap<String, String> contact = new HashMap<>();
                    // adding each child node to HashMap key => value
                    contact.put("name", name);

                    // adding contact to contact list




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


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pd.isShowing())
                pd.dismiss();

        }
    }
}