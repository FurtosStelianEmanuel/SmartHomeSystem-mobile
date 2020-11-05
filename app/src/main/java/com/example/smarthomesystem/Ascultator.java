package com.example.smarthomesystem;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

/**
 * Clasa care prelucreaza event-urile de la SpeechRecognizer
 * @author Manel
 * @since 06/02/2020-15:58
 */
class Ascultator implements RecognitionListener {

    //<editor-fold desc="Variabile" defaultstate="collapsed">
    public boolean running = false;
    MainActivity main;
    //</editor-fold>

    Ascultator(MainActivity main) {
        this.main = main;
    }

    public void onReadyForSpeech(Bundle params) {
        Log.println(Log.ASSERT, "on ready", "onReadyForSpeech");
        main.asistentReady();
    }

    public void onBeginningOfSpeech() {
        Log.println(Log.ASSERT, "on begin", "onBeginningOfSpeech");
        main.asistentRecording();
    }

    public void onRmsChanged(float rmsdB) {
        // Log.d(TAG, "onRmsChanged");
    }

    public void onBufferReceived(byte[] buffer) {
        // Log.d(TAG, "onBufferReceived");
    }

    public void onEndOfSpeech() {
        Log.println(Log.ASSERT, "eos", "onEndOfSpeech");
        main.endOfSpeech();
        //  Log.d(TAG, "onEndofSpeech");
    }

    public void onError(int error) {
        main.asistentError(error);
            /*Log.d(TAG,  "error " +  error);
            mText.setText("error " + error);*/
    }

    Bundle lastResults;

    public void onResults(Bundle results) {
        ArrayList currentData= results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (lastResults==null){
            main.asistentFinalResult(results);
        }else {
            ArrayList previousData=lastResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (!previousData.equals(currentData)){
                main.asistentFinalResult(results);
            }else{
                Log.println(Log.ASSERT,"Cacat","Cacatul a apelat de doua ori cu aceleasi date");
            }
        }
        this.lastResults = results;
    }

    public void onPartialResults(Bundle results) {
        //Log.d(TAG, "onPartialResults");
        main.asistentPartialResult(results);
    }

    void setState(boolean running) {
        this.running = running;
        if (!running) {
            main.hidePepe(3000);
        }
    }

    public void onEvent(int eventType, Bundle params) {
        //Log.d(TAG, "onEvent " + eventType);
    }
}