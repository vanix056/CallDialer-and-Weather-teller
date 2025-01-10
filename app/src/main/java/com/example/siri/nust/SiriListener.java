package com.example.siri.nust;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.SpeechRecognizer;

public class SiriListener extends Service {

    SpeechRecognizer speechRecognizer;

    public SiriListener() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}