package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnClickListener{
public ListView myList;
public Button myButton;
boolean enjoy = false;
public Switch cool;

public static final int VOICE_RECOGNIZITION_REQUESTCODE = 1234;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

