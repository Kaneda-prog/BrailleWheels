package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View.OnClickListener;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnClickListener{
public ListView myList;
public Button myButton;
boolean enjoy = false;
public Switch cool;
public WebView view;
static int wow;
static Location currentLocation;
FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 10;

public static final int VOICE_RECOGNIZITION_REQUESTCODE = 1234;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
        view = findViewById(R.id.web);
        if(currentLocation != null) {
            WebSettings webSettings = view.getSettings();
            view.getSettings().setJavaScriptEnabled(true);
            view.setWebViewClient(new WebViewClient());
            view.addJavascriptInterface(new WebAppInterface(this), "Android");
            //https://www.google.com/maps/dir/?api=1&origin=default&destination=Maracana%2CRio+de+Janeiro&travelmode=transit
            view.loadUrl("file:///android_asset/www/app.js");
            Toast.makeText(this, "lalala", Toast.LENGTH_SHORT).show();
        }
        cool = findViewById(R.id.switch1);
        cool.setOnClickListener(this);
        myButton = findViewById(R.id.speak);
        myButton.setOnClickListener(this);
        voiceinputbuttons();
        PackageManager pm = getPackageManager();
        List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),0);
    if(activities.size() != 0) {
        myButton.setOnClickListener(this);
    }else{
        myButton.setEnabled(false);
        myButton.setText("Recognizer not present");

    }

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
                    WebSettings webSettings = view.getSettings();
                    view.getSettings().setJavaScriptEnabled(true);
                    view.setWebViewClient(new WebViewClient());
                    view.addJavascriptInterface(new WebAppInterface(MainActivity.this), "Android");
                    //https://www.google.com/maps/dir/?api=1&origin=default&destination=Maracana%2CRio+de+Janeiro&travelmode=transit
                    view.loadUrl("file:///android_asset/www/app.js");
                    Toast.makeText(getApplicationContext(), "lalala", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    public void onClick(View v){
        if(v == myButton) {
            if(enjoy) {
                startVoiceRecognizitionActivity();
            }
            else {
                Intent intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
            }
        }
        if(v == cool)
        enjoy = !enjoy;
    }
    public void voiceinputbuttons(){
        myButton = findViewById(R.id.speak);
        myList = findViewById(R.id.list);
    }
    public void startVoiceRecognizitionActivity(){


        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say start!");
    startActivityForResult(intent, VOICE_RECOGNIZITION_REQUESTCODE);
    }

@Override
public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == VOICE_RECOGNIZITION_REQUESTCODE && resultCode == RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            myList.setAdapter((new ArrayAdapter(this, android.R.layout.simple_list_item_1, matches)));
                    if (matches.contains("start")) {
                       Intent intent = new Intent(this,MapsActivity.class);
                       startActivity(intent);


                    }
                    }
                }



}



