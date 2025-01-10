package com.example.siri.nust;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Function;

public class MainActivity extends AppCompatActivity {

    TextView status;
    ImageButton switchListen;
    SpeechRecognizer speechRecognizer;
    public static final Integer RecordAudioRequestCode = 1;

    Intent speechRecognizerIntent;

    public interface Callback {
        void onSuccess(Function callback);
        void onFailure(String errorMessage);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            init();
        }
    }

    private void checkPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{permission}, RecordAudioRequestCode);
        }
    }

    private void checkWeather(){

    }

    private void callContact(String searchText){

        Log.d("Finding Contact", searchText);

        Uri filterUri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, Uri.encode(searchText));
        String[] projection = new String[]{ ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER };
        Cursor cursor = getContentResolver().query(filterUri, projection, null, null, null);

        String contactName = "NONE";
        String contactNumber = "NONE";
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            @SuppressLint("Range") String num = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            contactName = name;
            contactNumber = num;
            cursor.close();
        }

        if ( contactName.equals("NONE") || contactNumber.equals("NONE") ){
            status.setText("No Contact Found for: " + searchText);
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contactNumber));
            startActivity(intent);
            status.setText("..^_^..");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void init(){

        checkPermission(Manifest.permission.CALL_PHONE);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, ComponentName.unflattenFromString(SpeechRecognizer.RESULTS_RECOGNITION));
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        switchListen = findViewById(R.id.start_listen);
        status = findViewById(R.id.listening_status);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d("onReady", "Ready For Speeching");
                status.setText("..^_^..");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d("onBegin", "Speeching beggining");
                status.setText("Listening...");
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                Log.d("onRMS", "Speeching RMS Changed");
            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
                status.setText("...");
            }

            @Override
            public void onError(int error) {
                Log.d("onError", "Speeching error " + error);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                float[] scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

                if (data.get(0).contains("weather")) {
                    status.setText("checking weather...");
                    checkWeather();
                } else if (data.get(0).startsWith("call")) {
                    String[] rest = data.get(0).split(" ");
                    if (rest.length > 1) {
                        status.setText("calling " + rest[1]);
                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                            checkPermission(Manifest.permission.READ_CONTACTS);
                            checkPermission(Manifest.permission.CALL_PHONE);
                        }
                        callContact(rest[1]);
                    } else {
                        status.setText("you must say `Call Abdullah` or any name starting with call");
                    }
                } else if (data.get(0).startsWith("open the app")) {
                    // Extract the app name from the voice command
                    String[] commandParts = data.get(0).split(" ");
                    if (commandParts.length > 3) {
                        String appName = commandParts[3];
                        openAppByName(appName);
                    } else {
                        status.setText("Please specify the app name after 'open the app'");
                    }
                } else if (data.get(0).equals("close the app")) {
                    closeApp();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        switchListen.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                    checkPermission(Manifest.permission.RECORD_AUDIO);
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    Log.d("stop recognize", "<-------------");
                    speechRecognizer.stopListening();
                    status.setText("..^_^..");
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    Log.d("start recognize", "<-------------");
                    status.setText("Listening...");
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        });

    }
    private void openAppByName(String appName) {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(appName);
        if (intent != null) {
            startActivity(intent);
            status.setText("Opening " + appName + "...");
        } else {
            Log.d("AppNotFound", "App '" + appName + "' not found or cannot be opened.");
            status.setText("App '" + appName + "' not found or cannot be opened.");
        }
        Log.d("AppPackageName", "Package Name: " + appName);
    }


    private void closeApp() {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
