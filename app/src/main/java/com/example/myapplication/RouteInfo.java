package com.example.myapplication;

import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
    String route;
    private String TAG = RouteInfo.class.getSimpleName();
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_info);
        text = findViewById(R.id.info);
       // Location dest = LatLng(route);
        route = getIntent().getStringExtra("firstStop");
        Log.i(TAG, "Here ate RouteInfo, we've got the current information about the stop: it its at "+route);
        //Uri gmmIntentUri = Uri.parse("google.navigation:q="+route+"&mode=w");
        text.setText("You are traveling at " + route);
        //Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, route, Toast.LENGTH_SHORT).show();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
             // mapIntent.setPackage("com.google.android.apps.maps");
              //startActivity(mapIntent);
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
}