package com.example.smarthomesystem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import static com.example.smarthomesystem.MainActivity.ARGB;
import java.io.IOException;

/**
 * Clasa receiver care este notificata atunci cand se intampla lucruri in afara aplicatiei (apasarea unui buton din notificare momentan)
 *
 * @author Manel
 * @since 06/02/2020-16:14
 */
public class NotificationReceiver extends BroadcastReceiver {

    static MainActivity main;

    @Override
    public void onReceive(Context context, Intent intent) {
        //<editor-fold desc="body" defaultstate="collapsed">
        for (String tag : App.TAGS) {
            String command = intent.getStringExtra(tag);
            if (command != null) {
                try {
                    if (MainActivity.PRIORITATE_STRIP==MainActivity.PRIORITATE_STRIP_NORMAL){
                        main.write(command);
                    }else if (MainActivity.PRIORITATE_STRIP==MainActivity.PRIORITATE_STRIP_ARGB){
                        if (command.contains("L0")){
                            main.write(ARGB.TURN_OFF);
                        }else if (command.contains("L1")){
                            main.write(ARGB.TURN_ON_WHITE_LIGHT);
                        }else{
                            Log.println(Log.ASSERT,"Eroare","nu am parsuit ok comanda "+command);
                        }
                    }else{
                        Log.println(Log.ASSERT,"ce cauti","eroare in NotificationReceiver");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for (String tag : App.COLOR_TAGS) {
            String culoare = intent.getStringExtra(tag);

            if (culoare != null) {
                Log.println(Log.ASSERT, "COLOR TAG", culoare);
                int selectedColor = Integer.valueOf(culoare);
                int red = 255, green = 255, blue = 255;
                if (selectedColor != 0) {
                    red = App.getRed(selectedColor);
                    green = App.getGreen(selectedColor);
                    blue = App.getBlue(selectedColor);
                }

                MainActivity.Values.set(red, green, blue);
                try {
                    if (MainActivity.PRIORITATE_STRIP==MainActivity.PRIORITATE_STRIP_NORMAL){
                        main.write(MainActivity.Values.impachetatBytes((byte) 'K'));
                    }else if (MainActivity.PRIORITATE_STRIP==MainActivity.PRIORITATE_STRIP_ARGB){
                        main.write(ARGB.TURN_ON_CUSTOM_COLOR);
                    }else{
                        Log.println(Log.ASSERT,"ce faci aci","eroare in NotificationReceiver");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        for (String tag : App.SWITCH_TAGS) {
            String masaj = intent.getStringExtra(tag);
            if (masaj != null) {
                Log.println(Log.ASSERT, "Anus", masaj);
            }
        }
        if (intent.getStringExtra(App.ASSISTANT) != null) {
            main.listenEvent();
        }
        //</editor-fold>
    }
}
