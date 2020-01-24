package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.speech.RecognizerIntent;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback ,OnClickListener {
    //Location'n stuff
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    public Place destination;
    public Place begin;

    private GoogleMap mMap;
    //Requests
    private static final int REQUEST_CODE = 101;
    public static final int VOICE_RECOGNIZITION_REQUESTCODE = 1;
    public static final int THE_CODE = 221;

    //Alone button view :O
    public Button myButton;

    //Strings
    private static String TAG = MapsActivity.class.getSimpleName();
    String speech;

    //Other stuff
    Geocoder geocoder;
    List<Address> add;
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 3030;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            askPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        myButton = findViewById(R.id.sd);
        myButton.setOnClickListener(this);
        //Voice recognizer initialize
        voiceInputButtons();
        PackageManager pm = getPackageManager();

        List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),0);
        if(activities.size() != 0) {
            myButton.setOnClickListener(this);
        }else{
            myButton.setEnabled(false);
            myButton.setText("Recognizer not present");
        }

        //Places API
        String apiKey = getString(R.string.google_maps_key);
        Places.initialize(getApplicationContext(), apiKey);
        Toast.makeText(this,R.string.google_maps_key, Toast.LENGTH_SHORT).show();
        PlacesClient placesClient = Places.createClient(this);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteFragment.setCountry("BR");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Add a marker to the map using geocoder
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                destination = place;

                Geocoder geocode = new Geocoder(MapsActivity.this);
                try{
                add = geocode.getFromLocationName(destination.getName(), 1);
                }catch (IOException e){
                    e.printStackTrace();
                }

                Address address = add.get(0);
                LatLng lani = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.addMarker(new MarkerOptions().position(lani).title("Let's go here!"));

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    startService(new Intent(MapsActivity.this, FloatingOverMapIconService.class));
                    finish();
                } else if (Settings.canDrawOverlays(getApplicationContext())) {
                    startService(new Intent(MapsActivity.this, FloatingOverMapIconService.class));
                    finish();
                } else {
                    askPermission();
                    Toast.makeText(getApplicationContext(), "You need System Alert Window Permission to do this", Toast.LENGTH_SHORT).show();
                }
                //http://maps.google.com/maps?daddr=lat,long&dirflg=r  to search

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
    }
    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }
    private void fetchLastLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
        return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!= null) {
                    currentLocation = location;
                    Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(MapsActivity.this);
                }
            }

        });
        }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker at current place, zoom and move the camera, because you deserve it ;)
        LatLng lating = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(lating).title("You are here now"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(lating));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lating,12));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLastLocation();
                }
                break;
        }
    }
    
    public void onClick(View v){
        Uri gmmIntentUri = Uri.parse("google.navigation:q=Maracanã,+Rio+de+Janeiro,Brazil&mode=transit");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startService(new Intent(MapsActivity.this, FloatingOverMapIconService.class));
            finish();
        } else if (Settings.canDrawOverlays(getApplicationContext())) {
            startService(new Intent(MapsActivity.this, FloatingOverMapIconService.class));
            finish();
        } else {
            askPermission();
            Toast.makeText(getApplicationContext(), "You need System Alert Window Permission to do this", Toast.LENGTH_SHORT).show();
        }

        //startVoiceRecognitionActivity();
    }

    public void voiceInputButtons(){
        myButton = findViewById(R.id.sd);
    }
    public void startVoiceRecognitionActivity(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recogniztion demo");
        startActivityForResult(intent, VOICE_RECOGNIZITION_REQUESTCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_RECOGNIZITION_REQUESTCODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            speech = matches.get(0);
            AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                    getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
            autocompleteFragment.setText(speech);
            Toast.makeText(this, speech, Toast.LENGTH_SHORT).show();


        }
    }

    

}


