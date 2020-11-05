package com.example.smarthomesystem;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import com.example.smarthomesystem.MainActivity.ARGB;

/**
 * Functionalitatea de 'alarma linistita' nu actiona corespunzator atunci cand telefonul intra in sleep
 * asa ca a trebuit creat un service care sa activeze un wakelock pe durata alarmei
 * wakelock-ul este oprit automat dupa 5 minute de inactivitate sau la oprirea alarmei
 * @author Manel
 * @since 06/02/2020-14:23
 */
public class AlarmaLinistita extends Service {

    //<editor-fold desc="Variabile">
    static MainActivity main;
    int red,green,blue;
    int percentage=0;
    static List<String>date;
    static int percentage(int color,int percentage){
        return (color*percentage/100);
    }
    static boolean runVolumChanger=true;
    PowerManager powerManager;
    static PowerManager.WakeLock wakeLock;
    static boolean runAlarm=true;
    SystemColor argbColor=new SystemColor(255,144,0);

    //</editor-fold>

    public AlarmaLinistita(){
    }

    public static void stopPlayer() {
        //<editor-fold desc="body" defaultstate="collapsed">
        if (AlarmReceiver.player != null) {
            AlarmReceiver.player.release();
            AlarmReceiver.player = null;
            runVolumChanger=false;
            Toast.makeText(main, "Am oprit muzica", Toast.LENGTH_SHORT).show();
        }
        //</editor-fold>
    }

    public static void play() {
        //<editor-fold desc="body" defaultstate="collapsed">
        if (AlarmReceiver.player == null) {
            Field[] fields = R.raw.class.getFields();
            Log.println(Log.ASSERT, "lungime", " " + fields.length);
            int r = new Random().nextInt(fields.length);
            try {
                int selectedOption = Integer.valueOf(date.get(7));
                Log.println(Log.ASSERT, "OPTSELECT", selectedOption + "");
                /*for (Field f:fields){
                    Log.println(Log.ASSERT,"piesa",f.getName());
                }*/
                if (selectedOption == 0) {
                    AlarmReceiver.player = MediaPlayer.create(main, fields[r].getInt(fields[r]));
                } else {
                    AlarmReceiver.player = MediaPlayer.create(main, fields[selectedOption - 1].getInt(selectedOption - 1));
                }
            } catch (IllegalAccessException | NullPointerException e) {
                e.printStackTrace();
            }
            try {
                AlarmReceiver.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        stopPlayer();
                    }
                });
            } catch (NullPointerException ex) {

            }
        }
        try {
            AlarmReceiver.player.start();
        } catch (NullPointerException ex) {
            Toast.makeText(main, "Nu am putut porni muzicuta,salveaza te rog din nou fisierul",
                    Toast.LENGTH_SHORT).show();
        }
        //</editor-fold>
    }

    void startCalmMusic(final List<String>date){
        //<editor-fold desc="body">
        final AudioManager am = (AudioManager) main.getSystemService(AUDIO_SERVICE);
        final int volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.println(Log.ASSERT, "Volum", volume_level + "");
        runVolumChanger=true;
        new Thread(){
            public void run(){
                int volum=0;
                am.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        0,
                        0);
                play();
                while(volum<=Integer.valueOf(date.get(6)) && volum<am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) && runVolumChanger){
                    volum++;
                    Log.println(Log.ASSERT,"Volum",volum+"");
                    am.setStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            volum,
                            0);
                    if (volum==1) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else if (volum==2){
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else if (volum==3){
                        try {
                            Thread.sleep(15000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else{
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Log.println(Log.ASSERT,"Alarma","GATA ALARMA,dam release la wakelock");
                try {
                    wakeLock.release();
                }catch(Exception ex){
                    Log.println(Log.ASSERT,"EROARE","Cand am dat release la wakeLock " + ex.toString());
                    ex.printStackTrace();
                }
            }
        }.start();
        //</editor-fold>
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //<editor-fold desc="body">
        Log.println(Log.ASSERT,"Apelat","onStartCommand");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.println(Log.ASSERT,"Sunam","alarmaLinistita");
            powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyApp::MyWakelockTag");
            wakeLock.acquire(5*60*1000L /*5 minutes*/);
            try{
                alarmaLinistita();
            }catch(Exception ex){
                Log.println(Log.ASSERT,"EROARE","Cand am strigat alarmaLinistita "+ex.toString());
                ex.printStackTrace();
            }
        }
        return super.onStartCommand(intent, flags, startId);
        //</editor-fold>
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void alarmaLinistita(){
        //<editor-fold desc="body">
        Log.println(Log.ASSERT,"alarma","alarma linistita");
        int culoare=0;
        try{
            culoare = Integer.valueOf(date.get(2));
        }catch(Exception ex){
            throw ex;
        }
        red = App.getRed(culoare);
        green = App.getGreen(culoare);
        blue = App.getBlue(culoare);
        final boolean sound_enabled = Boolean.valueOf(date.get(5));
        new Thread(){
            public void run(){
                int r=0;
                int g=0;
                int b=0;
                int procent=0;
                runAlarm=true;
                while((r!=red || g!=green || b!=blue)&& procent<=100 && runAlarm){
                    if (r!=red){
                        r=percentage(red,procent);
                    }
                    if (g!=green){
                        g=percentage(green,procent);
                    }
                    if (b!=blue){
                        b=percentage(blue,procent);
                    }
                    Log.println(Log.ASSERT,"culori",r+" "+g+" "+b);
                    try {
                        MainActivity.Values.set(r,g,b);
                        if (MainActivity.PRIORITATE_STRIP==MainActivity.PRIORITATE_STRIP_NORMAL){
                            main.write(MainActivity.Values.impachetatBytes((byte)'K'));
                        }else if (MainActivity.PRIORITATE_STRIP==MainActivity.PRIORITATE_STRIP_ARGB){
                            argbColor.set(r,g,b);
                            main.write(ARGB.TURN_ON_CUSTOM_COLOR,argbColor);
                        }
                    } catch (IOException e) {
                        Log.println(Log.ASSERT,"eroare","trimiteam alarma dar s a luat curentu");
                    }
                    if (procent==90){
                        if (sound_enabled) {
                            startCalmMusic(date);
                        }
                    }
                    procent++;
                    try {
                        Thread.sleep(800);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.println(Log.ASSERT,"ALARMA","Alarma anulata, wakeLock eliberat de StopAlarm");
            }
        }.start();
        boolean quotes_enabled = Boolean.valueOf(date.get(4));
        Intent stopMusic = new Intent(main, StopAlarm.class);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(main, 8, stopMusic, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(main, App.CHANNEL_2_ID)
                .setSmallIcon(R.drawable.ic_access_alarm_black_24dp)
                .setContentTitle("Alarm system : ")
                .setStyle(new NotificationCompat.BigTextStyle())
                .addAction(R.drawable.light_off, "OPRESTE ALARMA", stopPendingIntent)
                .setContentText((quotes_enabled ? AlarmReceiver.quotes[new Random().nextInt(AlarmReceiver.quotes.length)] : ""))
                .setContentIntent(stopPendingIntent)
                .setDeleteIntent(stopPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(main);
        notificationManager.notify(6, builder.build());
        //</editor-fold>
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //<editor-fold desc="body">
        Log.println(Log.ASSERT,"onBind",intent.toString());
        return null;
        //</editor-fold>
    }
}
