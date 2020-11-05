package com.example.smarthomesystem;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

import static android.content.Context.AUDIO_SERVICE;
import static com.example.smarthomesystem.MainActivity.ARGB;

/**
 * Clasa Receiver
 * {@link #onReceive(Context, Intent)} este apelata atunci cand alarma ar trebui sa sune
 * @author Manel
 * @since 06.02.2020-14:27
 */
public class AlarmReceiver extends BroadcastReceiver {

    //<editor-fold desc="Variabile si quotes" defaultstate="collapsed">
    int red,green,blue;
    static MainActivity main;
    static MediaPlayer player;
    PendingIntent stopPendingIntent;
    SystemColor argbColor=new SystemColor(255,144,0);
    static String quotes[] = {
            "Good fucking morning, goddamnit!",
            "One day you may!",
            "Get the fuck out of bed!",
            "The worst thing I can be is the same as everybody else.",
            "Everybody wants to be a bodybuilder but don’t nobody want to lift heavy ass weights!",
            "LIGHT WEIGHT BABY !",
            "Being negative and lazy is a disease that leads to pain, hardships, depression, poor health and failure. Be proactive and give a damn to achieve success!",
            "Vision creates faith and faith creates willpower. With faith there is no anxiety and no doubt – just absolute confidence in yourself.",
            "Suffer the pain of discipline or suffer the pain of regret",
            "E o zi fututa ca oricare alta",
            "Mens sana in corpore sano",
            "Unde se termina visele, moartea respira, \n" +
                    "Doar Luna si urletul de lup ma inspira, \n" +
                    "Traieste-ti visul, daca n-ai curaj sa traiesti, \n" +
                    "Ca un copil batran dependent de povesti. ",
            "Cand cuvintele tac, asculta atent, \n" +
                    "Pentru ca ura ramane cel mai sincer sentiment pana-n prezent... "
    };
    //</editor-fold>

    /**
     * functie care reda o piesa din libraria aplicatiei
     *
     * @param date lista cu setarile utilizatorului legate de alarma (alarma.svt)
     */
    public static void play(List<String> date) {
        //<editor-fold desc="body" defaultstate="collapsed">
        if (player == null) {
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
                    player = MediaPlayer.create(main, fields[r].getInt(fields[r]));
                } else {
                    player = MediaPlayer.create(main, fields[selectedOption - 1].getInt(selectedOption - 1));
                }
            } catch (IllegalAccessException | NullPointerException e) {
                e.printStackTrace();
            }
            try {
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        stopPlayer();
                    }
                });
            } catch (NullPointerException ex) {

            }
        }
        try {
            player.start();
        } catch (NullPointerException ex) {
            Toast.makeText(main, "Nu am putut porni muzicuta,salveaza te rog din nou fisierul",
                    Toast.LENGTH_SHORT).show();
        }
        //</editor-fold>
    }

    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    public void stop() {
        stopPlayer();
    }
    static boolean runVolumChanger=true;
    public static void stopPlayer() {
        //<editor-fold desc="body" defaultstate="collapsed">
        if (player != null) {
            player.release();
            player = null;
            runVolumChanger=false;
            Toast.makeText(main, "Am oprit muzica", Toast.LENGTH_SHORT).show();
        }
        //</editor-fold>
    }

    public AlarmReceiver() {
        super();
    }

    void alarmaLinistita(final List<String>date){
        Log.println(Log.ASSERT,"ALARMA","Alarma Linistita");
        AlarmaLinistita.main=main;
        AlarmaLinistita.date=date;
        main.startService(new Intent(main,AlarmaLinistita.class));
    }

    /**
     * Functie apelata de Android la momentul setat in PendingIntent-ul asociat acestei clase, ca sa functioneze trebuie sa
     * aiba si o referinta in manifest <receiver android:name=".AlarmReceiver"/>
     * La executie, alarma suna dupa setarile utilizatorului depuse in fisierul alarma.svt cu ajutorul functiei {@link AlarmFragment#loadAlarmStatic()}
     * Daca conexiunea la Arduino nu este prezenta in momentul alarmei atunci alarma va suna in mod normal dar fara aprinderea luminilor
     * Daca aplicatia este inchisa atunci cand aceasta functie este apelata referinta la main este null si alarma nu mai poate suna deloc,
     * nici prin folosirea contextului pasat de {@link AlarmReceiver#onReceive(android.content.Context, android.content.Intent)}
     * nu se rezolva problema
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        //<editor-fold desc="body" defaultstate="collapsed">
        Log.println(Log.ASSERT, "AlarmReceiver", "Primit mesaj");
        try {
            final List<String> date = AlarmFragment.loadAlarmStatic();
            if (!Boolean.valueOf(date.get(8))) {
                Log.println(Log.ASSERT,"alarma","alarma normala");
                int culoare = Integer.valueOf(date.get(2));
                int red = App.getRed(culoare);
                int green = App.getGreen(culoare);
                int blue = App.getBlue(culoare);

                MainActivity.Values.set(red,green,blue);
                if (MainActivity.PRIORITATE_STRIP==MainActivity.PRIORITATE_STRIP_NORMAL){
                    main.write(MainActivity.Values.impachetatBytes((byte)'K'));
                }else if (MainActivity.PRIORITATE_STRIP==MainActivity.PRIORITATE_STRIP_ARGB){
                    argbColor.set(red,green,blue);
                    main.write(ARGB.TURN_ON_CUSTOM_COLOR,argbColor);
                }

                boolean quotes_enabled = Boolean.valueOf(date.get(4));
                boolean sound_enabled = Boolean.valueOf(date.get(5));

                Intent stopMusic = new Intent(main, StopAlarm.class);
                PendingIntent stopPendingIntent = PendingIntent.getBroadcast(main, 8, stopMusic, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(main, App.CHANNEL_2_ID)
                        .setSmallIcon(R.drawable.ic_access_alarm_black_24dp)
                        .setContentTitle("Alarm system : ")
                        .setStyle(new NotificationCompat.BigTextStyle())
                        .addAction(R.drawable.light_off, "OPRESTE ALARMA", stopPendingIntent)
                        .setContentText((quotes_enabled ? quotes[new Random().nextInt(quotes.length)] : ""))
                        .setContentIntent(stopPendingIntent)
                        .setDeleteIntent(stopPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setAutoCancel(true);
                if (sound_enabled) {
                    AudioManager am = (AudioManager) main.getSystemService(AUDIO_SERVICE);
                    int volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                    Log.println(Log.ASSERT, "Volum", volume_level + "");
                    am.setStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            Integer.valueOf(date.get(6)),
                            0);
                    play(date);
                }
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(main);
                notificationManager.notify(6, builder.build());
            } else {
               alarmaLinistita(date);
            }
        } catch (Exception e) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(main, App.CHANNEL_2_ID)
                    .setSmallIcon(R.drawable.ic_access_alarm_black_24dp)
                    .setContentTitle("Nu am putut aprinde luminile")
                    .setDeleteIntent(stopPendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            if (main != null) {
                List<String> date = null;
                try {
                    date = AlarmFragment.loadAlarmStatic(main);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(main);
                notificationManager.notify(6, builder.build());
                e.printStackTrace();
                if (date != null)
                    play(date);
            } else {
                Log.println(Log.ASSERT, "EROARE", "Aplicatia e probabil inchisa si referinta la main e null, nu am putut aprinde luminile si nu am putut porni alarma");
            }
        }
        //</editor-fold>
    }
}
