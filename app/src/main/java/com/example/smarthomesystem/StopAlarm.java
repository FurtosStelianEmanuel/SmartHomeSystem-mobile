package com.example.smarthomesystem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receiver apelat cand utilizatorul opreste alarma prin inchiderea notificarii sau apasarea butonului dedicat din notificare
 *
 * @author Manel
 * @since 06/02/2020
 */
public class StopAlarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //<editor-fold desc="body" defaultstate="collapsed">
        Log.println(Log.ASSERT,"BA","baba");

        try{
            AlarmReceiver.stopPlayer();
        }catch(Exception ex){
            Log.println(Log.ASSERT,"Eroare","N am putut opri player-ul din AlarmReceiver " +ex.toString());
            ex.printStackTrace();
        }
        AlarmaLinistita.runAlarm=false;
        AlarmaLinistita.runVolumChanger=false;
        try{
            AlarmaLinistita.wakeLock.release();
        }catch(Exception ex){

        }
        //</editor-fold>
    }
}
