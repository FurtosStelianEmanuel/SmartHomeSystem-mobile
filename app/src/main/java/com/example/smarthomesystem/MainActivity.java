package com.example.smarthomesystem;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.slider.LightnessSlider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Clasa principala
 *
 * @author Manel
 * @since 06/02/2020
 */
public class MainActivity extends AppCompatActivity {

    //<editor-fold desc="Variabile si clase" defaultstate="collapsed">
    int SLEEP_TIME = 1000;
    int CONNECTED_SLEEP_TIME = 3000;
    int DISCONNECTED_SLEEP_TIME = 1000;
    int on_color = -1;
    int intentIterator = 0;
    long pTime;
    boolean runCheker = true;
    boolean sending = false;
    boolean connected = false;
    boolean runInputStreamHandler = false;

    String lastCommand = "";
    byte[] lastByteCommand;
    String connected_color = "K 125 0 0";

    public static final int PRIORITATE_STRIP_NORMAL = 0;
    public static final int PRIORITATE_STRIP_ARGB = 1;

    public static int PRIORITATE_STRIP = PRIORITATE_STRIP_NORMAL;

    PCReader pcReader;

    AlertDialog.Builder conectare_automata;
    AlertDialog.Builder builderSingle;
    AlertDialog.Builder builderInner;

    RemoteViews expandedView;
    RemoteViews collapsedView;

    Button connectButton;
    Button door_settings;
    Button colorSettingsButton;
    Button meniuButton;
    Button openAlarmFragmentButton;
    Button openMusicFragmentButton;
    Button openAsistentFragmentButton;
    Button openPCFragment;
    Button openArgb;
    Button openOnConnectAutomation;

    ImageView asistentIcon;
    TextView continutAsistent;
    TextView limbaAsistent;

    FloatingActionButton light_on_fab;
    FloatingActionButton light_off_fab;
    FloatingActionButton disconnect;
    FloatingActionButton asistent;

    BlankFragment blankFragment;
    Fragment choosenFragment;
    musicFragment musicFragment;
    doorEvents doorFragment;
    AlarmFragment alarmFragment;
    ColorPickerSettings colorPickerSettings;
    AsistentFragment asistentFragment;
    static pcControl PC_FRAGMENT;
    pcSettings pcSettings;
    ArgbFragment argbFragment;
    Argb_customizeLightOnLightFragment argbCustomizeLightOnLightFragment;
    Argb_customizeLightOffFragment argbCustomizeLightOffFragment;
    Argb_customizeColorFragment argbCustomizeColorFragment;
    Argb_customizeRomanianFlagFragment argbCustomizeRomanianFlagFragment;
    Argb_customizeFastLedFragment argbCustomizeFastLedFragment;
    LedTypePriorityFragment ledTypePriorityFragment;
    FragmentOnConnectAutomation onConnectionAutomationFragment;


    Thread connectionCheckThread;
    Timer valve = null;
    Timer dataSender;

    Intent colorIntent;
    PendingIntent colorPendingIntent;

    ColorPickerView colorPicker;
    LightnessSlider lightnessSlider;
    DrawerLayout mDrawerLayout;
    NotificationManagerCompat notificationManager;
    Notification notification;
    ArrayAdapter<String> arrayAdapter;
    List<String> nume = new ArrayList<>();
    List<String> pipe = new ArrayList<>();
    static MediaPlayer player;
    BluetoothAdapter blueAdapter;
    BluetoothSocket socket;
    BluetoothDevice modulBluetooth;
    OutputStream outputStream;
    java.io.InputStream inputStream;
    BufferedReader bufferedInputStream;
    public SpeechRecognizer sr;
    Ascultator ascultator;
    Traducator traducator;
    boolean LAST_COMMAND_WAS_STRING = false;
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = "00:1A:7D:DA:71:15";

    static Thread inputThread;

    /**
     * prin aceasta clasa se defineste Runnable-ul care o sa monitorizeze datele primite (si handle-ul pentru erori de comunicare)
     * fie de la modulul bluetooth fie de la aplicatia desktop
     */
    class InputStreamHandler implements Runnable {
        //<editor-fold desc="Class body" defaultstate="collapsed">
        @Override
        public void run() {
            Log.println(Log.ASSERT, "Notify", "Pornim InputStreamHandler");
            runInputStreamHandler = true;
            while (runInputStreamHandler) {
                try {
                    String in = bufferedInputStream.readLine();
                    Log.println(Log.ASSERT, "citit", in);
                    if (in.charAt(0) == App.CONFIRM_CONNECTION_TYPE_TOKEN) {
                        outputStream.write((App.SWITCH_CONNECTION_TYPE + "" + App.PC_MASTER).getBytes());
                        App.CONNECTION_TYPE = App.PC_MASTER;
                        runCheker = false;
                        Log.println(Log.ASSERT, "MARE TARE CONFIRMARE", "hai la pacanea ca am un cumatru " + in.substring(1));
                    } else if (in.contains("inv")) {

                        ///
                        if (LAST_COMMAND_WAS_STRING) {
                            write(lastCommand);
                            Log.println(Log.ASSERT, "AM PRIMIT inv", "se retrimite " + lastCommand);
                        } else {
                            write(lastByteCommand);
                            Log.println(Log.ASSERT, "AM PRIMIT inv", "se retrimite " + lastByteCommand);
                        }

                    }
                } catch (IOException | StringIndexOutOfBoundsException e) {
                    if (e instanceof IOException) {
                        runInputStreamHandler = false;
                    }
                    e.printStackTrace();
                }
            }
            Log.println(Log.ASSERT, "Notify", "Am oprit InputStreamHandler");
        }
        //</editor-fold>
    }

    //</editor-fold>

    /**
     * Afiseaza toate conexiunile posibile ale telefonului si le printeaza in ASSERT
     */
    void showConnections() {
        //<editor-fold desc="body" defaultstate="collapsed">
        int position = 6;
        blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (blueAdapter != null) {
            if (blueAdapter.isEnabled()) {
                Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();
                for (BluetoothDevice bd : bondedDevices) {
                    Log.println(Log.ASSERT, bd.getName(), bd.getAddress());
                }
                if (bondedDevices.size() > 0) {
                    Object[] devices = (Object[]) bondedDevices.toArray();
                    for (position = 0; position < devices.length; position++) {
                        BluetoothDevice device = (BluetoothDevice) devices[position];
                        ParcelUuid[] uuids = device.getUuids();
                        Log.println(Log.ASSERT, device.getName() + " index= " + position, device.getAddress());
                        //pare ca toate uuid-urile sunt identice, nu stiu de ce returneaza un array
                        for (int i = 0; i < uuids.length; i++) {
                            //"c7f94713-891e-496a-a0e7-983a0946126e"
                            Log.println(Log.ASSERT, "UUID : ", uuids[i].toString());
                        }
                    }
                }
            } else {
                Log.println(Log.ASSERT, "Error", "Bluetooth disabled");
            }
        }
        //</editor-fold>
    }


    void connectTo(String uuid) throws IOException {
        //<editor-fold desc="body" defaultstate="collapsed">
        Log.println(Log.ASSERT, "CONNECT", "Incercam conectarea la modulul cu uuid-ul " + uuid);
        socket = modulBluetooth.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
        try {
            socket.connect();
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            bufferedInputStream = new BufferedReader(new InputStreamReader(inputStream));
            inputThread = new Thread(new InputStreamHandler());
            inputThread.start();

            outputStream.write(App.CHECK_CONNECTION_TYPE_TOKEN);
            Log.println(Log.ASSERT, "SUCCES", "-" + uuid + "-");
        } catch (Exception ex) {
            //builderInner.setTitle("Conexiune esuata");
            Log.println(Log.ASSERT, "Reconectare inutila", "Nu am putut realiza conexiunea cu modulul cu adresa " + uuid);
        }
        //</editor-fold>
    }


    /**
     * Functia principala de trimitere a datelor catre arduino se invarte in jurul variabiliei {@link #outputStream}.write()
     *
     * @param s comanda trimisa trebuie neaparat sa contina \n la final
     * @throws IOException
     */
    public void write(String s) throws IOException {
        //<editor-fold desc="body" defaultstate="collapsed">
        if (s.equals(">\n")) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            outputStream.write(s.getBytes());
            LAST_COMMAND_WAS_STRING = true;
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
/*
            pipe.add(s);
            if (valve == null) {
                valve = new Timer();
                valve.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (pipe.size() == 0) {
                            valve.cancel();
                            valve = null;
                            return;
                        }
                        try {
                            Log.println(Log.ASSERT,"Trimitem","Trimitem prin pipa");
                            outputStream.write(pipe.get(0).getBytes());
                            pipe.remove(0);
                        } catch (IOException e) {
                            // e.printStackTrace();
                            // valve.cancel(); //#fix
                        } catch (java.lang.NullPointerException ex) {
                            // ex.printStackTrace();
                            //valve.cancel(); //#fix pix
                            //Log.println(Log.ASSERT, "EROARE", "nu putem trimite date daca nu suntem conectati");
                        }
                    }
                }, 0, App.ARDUINO_TIMEOUT);
            }
*/
            lastCommand = s;
            outputStream.write(s.getBytes());

        }
        //</editor-fold>
    }

    public void write(byte data[]) throws IOException {
        //<editor-fold desc="body" defaultstate="collapsed">
        lastByteCommand = data;
        outputStream.write(data);
        LAST_COMMAND_WAS_STRING = false;
        //</editor-fold>
    }


    public enum ARGB {
        TURN_ON_WHITE_LIGHT, TURN_OFF_NO_ANIMATION, TURN_OFF, TURN_ON_WHITE_LIGHT_NO_ANIMATION,
        TURN_ON_CUSTOM_COLOR, TURN_ON_COLOR_NO_ANIMATION, ROMANIAN_FLAG_ANIMATION,
        FAST_LED_ANIMATION
    }

    public void write(ARGB identifier, Object... other) throws IOException {
        byte[] byteData;
        byte suma;

        switch (identifier) {
            case TURN_ON_WHITE_LIGHT:
                byteData = new byte[10];
                byteData[0] = 'j';
                byteData[1] = '+';
                byteData[2] = MainActivity.getByte(255);
                byteData[3] = MainActivity.getByte(255);
                byteData[4] = MainActivity.getByte(255);
                byteData[5] = MainActivity.getByte(argbCustomizeLightOnLightFragment.getTip() + 1);
                byteData[6] = MainActivity.getByte(argbCustomizeLightOnLightFragment.getAnimationViteza());
                byteData[7] = MainActivity.getByte(argbCustomizeLightOnLightFragment.getIncrement());
                byteData[8] = MainActivity.getByte(argbCustomizeLightOnLightFragment.getSectiuni());

                suma = byteData[0];
                for (int i = 1; i < 9; i++) {
                    suma += byteData[i];
                }
                byteData[9] = suma;
                write(byteData);

                break;
            case TURN_OFF_NO_ANIMATION:
                byteData = new byte[5];
                byteData[0] = 'j';
                byteData[1] = getByte(0);
                byteData[2] = getByte(0);
                byteData[3] = getByte(0);

                suma = byteData[0];
                for (int i = 1; i < 4; i++) {
                    suma += byteData[i];
                }
                byteData[4] = suma;
                try {
                    write(byteData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case TURN_OFF:
                byteData = new byte[7];
                byteData[0] = 'j';
                byteData[1] = '-';
                byteData[2] = getByte(argbCustomizeLightOffFragment.getAnimationViteza());
                byteData[3] = getByte(argbCustomizeLightOffFragment.getDecrement());
                byteData[4] = getByte(argbCustomizeLightOffFragment.getTip() + 1);
                byteData[5] = getByte(argbCustomizeLightOffFragment.getSectiuni());
                suma = byteData[0];
                for (int i = 1; i < 6; i++) {
                    suma += byteData[i];
                }
                byteData[6] = suma;

                write(byteData);

                break;
            case TURN_ON_WHITE_LIGHT_NO_ANIMATION:
                byteData = new byte[5];
                byteData[0] = 'j';
                byteData[1] = getByte(255);
                byteData[2] = getByte(255);
                byteData[3] = getByte(255);

                suma = byteData[0];
                for (int i = 1; i < 4; i++) {
                    suma += byteData[i];
                }
                byteData[4] = suma;

                write(byteData);

                break;
            case TURN_ON_CUSTOM_COLOR:
                if (other.length == 0) {
                    byteData = new byte[10];
                    byteData[0] = 'j';
                    byteData[1] = '+';
                    SystemColor c = argbCustomizeColorFragment.getColor();
                    byteData[2] = MainActivity.getByte(c.getRed());
                    byteData[3] = MainActivity.getByte(c.getGreen());
                    byteData[4] = MainActivity.getByte(c.getBlue());
                    byteData[5] = MainActivity.getByte(argbCustomizeColorFragment.getTip() + 1);
                    byteData[6] = MainActivity.getByte(argbCustomizeColorFragment.getAnimationViteza());
                    byteData[7] = MainActivity.getByte(argbCustomizeColorFragment.getIncrement());
                    byteData[8] = MainActivity.getByte(argbCustomizeColorFragment.getSectiuni());
                    suma = byteData[0];
                    for (int i = 1; i < 9; i++) {
                        suma += byteData[i];
                    }
                    byteData[9] = suma;

                    write(byteData);

                } else {
                    byteData = new byte[10];
                    byteData[0] = 'j';
                    byteData[1] = '+';
                    SystemColor c = (SystemColor) other[0];
                    byteData[2] = MainActivity.getByte(c.getRed());
                    byteData[3] = MainActivity.getByte(c.getGreen());
                    byteData[4] = MainActivity.getByte(c.getBlue());
                    byteData[5] = MainActivity.getByte(argbCustomizeColorFragment.getTip() + 1);
                    byteData[6] = MainActivity.getByte(argbCustomizeColorFragment.getAnimationViteza());
                    byteData[7] = MainActivity.getByte(argbCustomizeColorFragment.getIncrement());
                    byteData[8] = MainActivity.getByte(argbCustomizeColorFragment.getSectiuni());
                    suma = byteData[0];
                    for (int i = 1; i < 9; i++) {
                        suma += byteData[i];
                    }
                    byteData[9] = suma;
                    try {
                        write(byteData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case TURN_ON_COLOR_NO_ANIMATION:
                if (other[0] instanceof SystemColor) {
                    SystemColor color = (SystemColor) other[0];
                    byteData = new byte[5];
                    byteData[0] = 'j';
                    byteData[1] = getByte(color.getRed());
                    byteData[2] = getByte(color.getGreen());
                    byteData[3] = getByte(color.getBlue());

                    suma = byteData[0];
                    for (int i = 1; i < 4; i++) {
                        suma += byteData[i];
                    }
                    byteData[4] = suma;

                    write(byteData);

                } else {
                    Log.println(Log.ASSERT, "eroare", "Ai apelat turn on color " +
                            "no animation fara sa ii dai culoarea pe care o vrei");
                }
                break;
            case ROMANIAN_FLAG_ANIMATION:

                byteData = new byte[9];
                byteData[0] = 'j';
                byteData[1] = '0';
                byteData[2] = MainActivity.getByte(argbCustomizeRomanianFlagFragment.getNrSteaguri());
                byteData[3] = MainActivity.getByte(argbCustomizeRomanianFlagFragment.getLatimeSteaguri());
                byteData[4] = MainActivity.getByte(argbCustomizeRomanianFlagFragment.getViteza());
                byteData[5] = MainActivity.getByte(argbCustomizeRomanianFlagFragment.getDirectie());
                byteData[6] = MainActivity.getByte(argbCustomizeRomanianFlagFragment.getOrientare());
                byteData[7] = MainActivity.getByte(argbCustomizeRomanianFlagFragment.getIntensitate());
                suma = byteData[0];
                for (int i = 1; i < 8; i++) {
                    suma += byteData[i];
                }
                byteData[8] = suma;


                write(byteData);


                break;
            case FAST_LED_ANIMATION:
                byteData = new byte[6];
                byteData[0] = 'j';
                byteData[1] = '1';
                byteData[2] = MainActivity.getByte(argbCustomizeFastLedFragment.getViteza());
                byteData[3] = MainActivity.getByte(argbCustomizeFastLedFragment.getTip() + 1);
                byteData[4] = MainActivity.getByte(argbCustomizeFastLedFragment.getIntensitate());
                suma = byteData[0];
                for (int i = 1; i < 5; i++) {
                    suma += byteData[i];
                }
                byteData[5] = suma;

                write(byteData);

                break;
        }
    }

    public void savePrioritateLedType(StoreClass store) throws IOException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileOutputStream fos = this.openFileOutput("prioritate.svt", Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(store);
        os.close();
        fos.close();
        Log.println(Log.ASSERT, getFilesDir().toString(), "Am salvat fisierul prioritate.svt");
        //</editor-fold>
    }

    public StoreClass loadPrioritateLedType() throws IOException, ClassNotFoundException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileInputStream fis = this.openFileInput("prioritate.svt");
        ObjectInputStream is = new ObjectInputStream(fis);
        StoreClass simpleClass = (StoreClass) is.readObject();
        is.close();
        fis.close();
        return simpleClass;
        //</editor-fold>
    }

    /**
     * Serializeaza {@link FragmentOnConnectAutomation#ON_CONNECT_AUTOMATION_SETTINGS} in fisierul onConnect.svt
     *
     * @throws IOException
     */
    public void saveOnConnectAutomationSettings() throws IOException {
        FileOutputStream fos = this.openFileOutput("onConnect.svt", Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(FragmentOnConnectAutomation.ON_CONNECT_AUTOMATION_SETTINGS);
        os.close();
        fos.close();
        Log.println(Log.ASSERT, getFilesDir().toString(), "Am salvat fisierul onConnect.svt");
    }

    public FragmentOnConnectAutomation.Store loadOnConnectStore() throws IOException, ClassNotFoundException {
        FileInputStream fis = this.openFileInput("onConnect.svt");
        ObjectInputStream is = new ObjectInputStream(fis);
        FragmentOnConnectAutomation.Store simpleClass = (FragmentOnConnectAutomation.Store) is.readObject();
        is.close();
        fis.close();
        return simpleClass;
    }

    /**
     * Salveaza date legate de modulul bluetooth la care aplicatia s-a putut conecta in fisierul save.svt
     *
     * @param name    Numele modulului
     * @param address Adresa modulului
     * @param uuid    UUID-ul modulului
     * @throws IOException Daca fisierul nu poate fi accesat
     */
    void saveBluetooth(String name, String address, String uuid) throws IOException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileOutputStream fos = this.openFileOutput("save.svt", Context.MODE_PRIVATE);
        List<String> x = new ArrayList<>();
        ObjectOutputStream os = new ObjectOutputStream(fos);
        x.add(name);
        x.add(address);
        x.add(uuid);
        os.writeObject(x);
        os.close();
        fos.close();
        Log.println(Log.ASSERT, getFilesDir().toString(), "Am salvat fisierul save.svt");
        //</editor-fold>
    }

    public void saveARGBProfile(ARGBProfile profile) throws IOException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileOutputStream fos = this.openFileOutput(profile.name + ".svt", Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(profile);
        os.close();
        fos.close();
        Log.println(Log.ASSERT, getFilesDir().toString(), "Am salvat fisierul " + profile.name + ".svt");
        //</editor-fold>
    }

    public ARGBProfile loadARGBProfile(String name) throws IOException, ClassNotFoundException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileInputStream fis = this.openFileInput(name + ".svt");
        ObjectInputStream is = new ObjectInputStream(fis);
        ARGBProfile simpleClass = (ARGBProfile) is.readObject();
        is.close();
        fis.close();
        return simpleClass;
        //</editor-fold>
    }

    /**
     * Cauta fisierul save.svt si extrage datele necesare pentru reconectarea la modulul bluetooth
     *
     * @return O lista care contine numele,adresa si uuid-ul modulului bluetooth
     * @throws IOException
     * @throws ClassNotFoundException
     */
    List<String> loadBluetooth() throws IOException, ClassNotFoundException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileInputStream fis = this.openFileInput("save.svt");
        ObjectInputStream is = new ObjectInputStream(fis);
        List<String> simpleClass = (ArrayList<String>) is.readObject();
        is.close();
        fis.close();
        return simpleClass;
        //</editor-fold>
    }


    int map(double x, double in_min, double in_max, double out_min, double out_max) {
        return (int) ((x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
    }

    /**
     * Clasa ce retine culorile selectate in ColorPicker si functioneaza impreuna cu dataSender {same_counter}
     * ca sa opreasca trimiterea de date daca utilizatorul nu mai foloseste ColorPicker-ul
     */
    static class Values {
        private static int red = 0, green = 0, blue = 0;
        private static int pr = 0, pg = 0, pb = 0;
        public static int same_counter = 0;
        public static int SAME_THRESHOLD = 3;

        public static void set(int r, int g, int b) {
            red = r;
            green = g;
            blue = b;
            //Log.println(Log.ASSERT,"iei",pr+" "+pg+" "+pb+" "+red+" "+green+" "+blue);
            if (pr == red && pg == green && pb == blue) {

                same_counter++;
            } else {
                same_counter = 0;
            }
            pr = red;
            pg = green;
            pb = blue;
        }

        private static byte getBytes(int color) {
            color -= 128;
            return (byte) color;
        }

        public static String impachetat() {
            int suma = red + green + blue;
            byte ar[] = new byte[5];
            suma %= 256;
            suma -= 128;
            byte s = getBytes(suma);
            byte r = getBytes(red);
            byte g = getBytes(green);
            byte b = getBytes(blue);
            ar[0] = 'G';
            ar[1] = r;
            ar[2] = g;
            ar[3] = b;
            ar[4] = s;
            return new String(ar);
        }

        public static byte[] impachetatBytes(byte... prefix) {
            byte ar[] = new byte[3 + prefix.length];
            byte r = getBytes(red);
            byte g = getBytes(green);
            byte b = getBytes(blue);
            int counter = 0;
            for (; counter < prefix.length; counter++) {
                ar[counter] = prefix[counter];
            }
            ar[counter++] = r;
            ar[counter++] = g;
            ar[counter] = b;
            return ar;
        }

    }

    //<editor-fold desc="Portate din desktop" defaultstate="collapsed">
    public static final int COMMAND_TURN_ON_ARGB = 0;
    /*
    public byte[] getCommand(int identifier) {
        switch (identifier) {
            case COMMAND_TURN_ON_ARGB:
                byte byteData[]=new byte[];
                break;
        }
        return new byte[]{};
    }
    */

    //</editor-fold>

    public static byte getByte(int value) {
        value -= 128;
        if (value > 127) {
            value = 127;
        } else if (value < -128) {
            value = -128;
        }
        return (byte) (value);
    }

    @Deprecated
    void sendDataEvent() {
        //<editor-fold desc="body" defaultstate="collapsed">

        if (sending) {

        } else {
            startComms();
        }
        //</editor-fold>
    }

    /**
     * @param address este textul pe care il vede utilizatorul cand vrea sa selecteze un modul la care sa se conecteze - ex: 98:D3:11:F8:1C:78
     * @return Modulul bluetooth cu adresa specifata
     */
    BluetoothDevice getDevice(String address) {
        //<editor-fold desc="body" defaultstate="collapsed">
        blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (blueAdapter != null) {
            if (blueAdapter.isEnabled()) {
                Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();
                Object devices[] = (Object[]) bondedDevices.toArray();
                for (int i = 0; i < devices.length; i++) {
                    BluetoothDevice device = (BluetoothDevice) devices[i];
                    //Log.println(Log.ASSERT, bd.getName(), bd.getAddress()+" "+bd.getUuids()[0]);
                    if (device.getAddress().equals(address)) {
                        Log.println(Log.ASSERT, "MATCH: ", "La index " + i + " " + device.getName() + " " +
                                device.getAddress() + " " + device.getUuids()[0]);
                        return device;
                    } else {
                        Log.println(Log.ASSERT, address, device.getAddress());
                    }
                }
            }
        }
        throw new Resources.NotFoundException("Nu am gasit modulul cu adresa " + address + " poate iti da eroare asta pt ca nu ai " +
                "pasat adresa goala si ai pus si numele de ex 23:41:AB:32 HC-06");
        //</editor-fold>
    }

    void afisConfirmareIesire() {
        new AlertDialog.Builder(this, AlarmFragment.getTimePickerDialogType())
                .setMessage("Vrei să inchizi aplicația?")
                .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("Nu", null)
                .show();
    }

    /**
     * Event global la functia onBackPressed(), comportamentul default rezulta in inchiderea aplicatiei la apasarea butonului de back
     * dar acum utilizatorului ii este prezentat un mesaj de confirmare a iesirii
     */
    @Override
    public void onBackPressed() {
        //<editor-fold desc="body" defaultstate="collapsed">
        if (choosenFragment == blankFragment || choosenFragment == null) {
            afisConfirmareIesire();
        } else {
            if (choosenFragment == pcSettings) {
                Log.println(Log.ASSERT, "back to PC CONTROL", "back pressed");
                openFragment(PC_FRAGMENT);
            } else if (choosenFragment == argbCustomizeLightOnLightFragment ||
                    choosenFragment == argbCustomizeLightOffFragment ||
                    choosenFragment == argbCustomizeColorFragment ||
                    choosenFragment == argbCustomizeRomanianFlagFragment ||
                    choosenFragment == argbCustomizeFastLedFragment) {
                openFragment(argbFragment);
            } else {
                if (PRIORITATE_STRIP == PRIORITATE_STRIP_ARGB) {
                    openFragment(argbFragment);
                } else {
                    openFragment(null);
                }
                Log.println(Log.ASSERT, "back to home", "back pressed");
            }

        }
        //</editor-fold>
    }

    /**
     * Initializeaza componentele vizuale ... butoane,onClickListeners si unele componente legate de ramura vizuala.
     */
    void initialiseScreenComponents() {
        //<editor-fold desc="body" defaultstate="collapsed">
        disconnect = findViewById(R.id.disconnect);
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        connectButton.setBackground(getResources().getDrawable(R.drawable.connect_off));
                    }
                    runCheker = false;
                } else {
                    Toast.makeText(disconnect.getContext(), "Nu ești conectat la un modul bluetooth", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            disconnect.setTooltipText("Butonul pentru deconectare de la modulul bluetooth");
        }
        light_on_fab = findViewById(R.id.light_on_fab);
        light_on_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isEnabled()) {
                    try {
                        write("L1\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        light_on_fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    write("RA1");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return true;
            }
        });
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            light_on_fab.setTooltipText("Butonul ce aprinde ledurile albe");
        }*/
        light_off_fab = findViewById(R.id.light_off_fab);
        light_off_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isEnabled()) {
                    try {
                        write("L0\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        light_off_fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    write("RA0");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return true;
            }
        });
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            light_off_fab.setTooltipText("Butonul ce stinge ledurile ");
        }*/
        lightnessSlider = findViewById(R.id.brightness);
        colorPicker = findViewById(R.id.color_picker);

        colorPicker.addOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int selectedColor) {
                /*Color c = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    c = Color.valueOf(colorPicker.getSelectedColor());
                    int red = map(c.red(), 0, 1, 0, 255);
                    int green = map(c.green(), 0, 1, 0, 255);
                    int blue = map(c.blue(), 0, 1, 0, 255);
                    Values.set(red, green, blue);
                    try {
                        write(Values.impachetat());
                        Thread.sleep(30);
                        write(Values.impachetat());
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
*/
                /*if (!sending)
                    sendDataEvent();*/


                try {
                    Color c = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        c = Color.valueOf(colorPicker.getSelectedColor());
                        int red = map(c.red(), 0, 1, 0, 255);
                        int green = map(c.green(), 0, 1, 0, 255);
                        int blue = map(c.blue(), 0, 1, 0, 255);
                        Values.set(red, green, blue);

                        //Log.println(Log.ASSERT,"Trimitem culoare",Values.impachetat());
                        write(Values.impachetatBytes((byte) 'G'));
                        //Thread.sleep(5);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        colorPicker.setDensity(25);

        conectare_automata = new AlertDialog.Builder(MainActivity.this, AlarmFragment.getTimePickerDialogType());
        builderSingle = new AlertDialog.Builder(MainActivity.this, AlarmFragment.getTimePickerDialogType());
        builderSingle.setIcon(R.drawable.bluetooth);
        builderSingle.setTitle("Selecteaza modulul bluetooth");
        builderInner = new AlertDialog.Builder(MainActivity.this, AlarmFragment.getTimePickerDialogType());
        arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                modulBluetooth = getDevice((strName.contains(nume.get(which))
                        ? strName.replace(" " + nume.get(which), "") : strName));
                builderInner.setMessage(strName);
                builderInner.setTitle("Asteptam conectarea cu modulul");

                final AlertDialog alert = new AlertDialog.Builder(MainActivity.this, AlarmFragment.getTimePickerDialogType()).create();
                alert.setTitle("Conectare . . . .");
                alert.show();

                Thread thread = new Thread() {
                    Handler mHandler = new Handler(Looper.getMainLooper()) {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void handleMessage(Message message) {
                            if (socket.isConnected()) {
                                builderInner.setPositiveButton("Aprinde luminile", new DialogInterface.OnClickListener() {
                                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (socket.isConnected()) {
                                            connectButton.setBackground(getResources().getDrawable(R.drawable.connect));
                                            String ar[] = connected_color.split(" ");
                                            Values.set(Integer.valueOf(ar[1]), Integer.valueOf(ar[2]), Integer.valueOf(ar[3]));
                                            try {
                                                write(Values.impachetatBytes((byte) connected_color.charAt(0)));
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            connectButton.setBackground(getResources().getDrawable(R.drawable.connect_off));
                                        }
                                        dialog.dismiss();
                                    }
                                });
                                builderInner.setNeutralButton("Nu modifica luminile", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (socket.isConnected()) {
                                            connectButton.setBackground(getResources().getDrawable(R.drawable.connect));
                                        } else {
                                            connectButton.setBackground(getResources().getDrawable(R.drawable.connect_off));
                                        }
                                        dialog.dismiss();
                                    }
                                });
                                builderInner.setNegativeButton("Stinge luminile", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (socket.isConnected()) {
                                            connectButton.setBackground(getResources().getDrawable(R.drawable.connect));
                                            try {
                                                Values.set(0, 0, 0);
                                                write(Values.impachetatBytes((byte) 'K'));
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            connectButton.setBackground(getResources().getDrawable(R.drawable.connect_off));
                                        }
                                        dialog.dismiss();
                                    }
                                });
                                builderInner.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        connectButton.setBackground(getResources().getDrawable(R.drawable.connect));
                                        dialog.dismiss();
                                    }
                                });
                            } else {
                                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builderInner.setNeutralButton("", null);

                                builderInner.setNegativeButton("", null);

                                builderInner.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {

                                        dialog.dismiss();
                                    }
                                });


                            }

                            builderInner.show();
                            alert.dismiss();
                            if (PRIORITATE_STRIP == PRIORITATE_STRIP_ARGB) {
                                openFragment(argbFragment);
                            }
                        }
                    };

                    @Override
                    public void run() {
                        try {

                            connectTo(modulBluetooth.getUuids()[0].toString());
                            if (socket.isConnected()) {
                                builderInner.setTitle("Conexiune realizata");
                                connectEvent();

                                Log.println(Log.ASSERT, "CONNECTION", "ESTABLISHED");
                                saveBluetooth(modulBluetooth.getName(), modulBluetooth.getAddress(), modulBluetooth.getUuids()[0].toString());
                            } else {
                                builderInner.setTitle("Conexiune esuata");
                                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                            }
                            Message message = mHandler.obtainMessage(Message.CONTENTS_FILE_DESCRIPTOR, new Message());
                            message.sendToTarget();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();

            }
        });
        //</editor-fold>
    }

    /**
     * Trimite un sir de comenzi la Arduino, in principiu este folosita la conectarea/reconectarea cu modulul bluetooth ca sa reaplice
     * setarile utilizatorului.
     *
     * @param aprindereAutomata daca luminile se vor aprinde la deschiderea usii (d0{10} - 1 daca setarea este aprobata, 0 in caz contrar)
     * @param oprireAutomata    daca luminile se vor stinge la inchiderea usii (d1{10} -1 daca setarea este aprobata, 0 in caz contrar)
     * @param color             becurile se vor deschide la culoarea aleasa
     *                          comanda trimisa : dt red green blue -->de exemplu daca vrei sa setezi culoarea la deschiderea usii sa fie albastra ai trimite
     *                          comanda dt 0 0 255
     */
    void sendDoorCommands(boolean aprindereAutomata, boolean oprireAutomata, int color) {
        //<editor-fold desc="commands" defaultstate="collapsed">
        try {
            Thread.sleep(150);
            Log.println(Log.ASSERT, "Send", "Trimitem comanda :" + "d0" + (aprindereAutomata ? "1" : "0"));

            write("J0" + (aprindereAutomata ? "1" : "0") + "\n");
            Thread.sleep(300);
        } catch (IOException e) {
            Log.println(Log.ASSERT, "Send Error", "Aprindere automata");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            Log.println(Log.ASSERT, "Send", "Trimitem comanda :" + "d1" + (oprireAutomata ? "1" : "0"));
            write("J1" + (oprireAutomata ? "1" : "0") + "\n");
            Thread.sleep(150);
        } catch (IOException e) {
            Log.println(Log.ASSERT, "Send Error", "Oprire automata");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //aplicarea setarilor la startup nu are rost sa fie trimisa la arduino
        //trecem la trimiterea culorii la deschiderea usii

        int red = 255;
        int green = 255;
        int blue = 255;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //double r=Color.valueOf(color).red();
            if (color != 0) {
                red = map(Color.valueOf(color).red(), 0, 1, 0, 255);
                green = map(Color.valueOf(color).green(), 0, 1, 0, 255);
                blue = map(Color.valueOf(color).blue(), 0, 1, 0, 255);
            } else {
                red = 255;
                green = 255;
                blue = 255;
            }

        }
        Log.println(Log.ASSERT, "Send", "Trimitem comanda :" + "dt " + red + " " + green + " " + blue);
        try {
            //write("JH " + red + " " + green + " " + blue + "\n");
            Values.set(red, green, blue);

            write(Values.impachetatBytes((byte) 'J', (byte) 'H'));
            Thread.sleep(70);
        } catch (IOException e) {
            Log.println(Log.ASSERT, "Send Error", "doorEvents(sendDoorCommands)");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            MainActivity.PC_FRAGMENT.pcOutputStream.write((App.COMMAND_PC_REQUEST_RESPONSE + "\n").getBytes());
            Log.println(Log.ASSERT, "AM TRIMIS", "anus");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //</editor-fold>
    }

    /**
     * functie ajutatoare pentru sendDoorCommands, mai este campul aplicareStartup care determina daca setarile se vor trimite la Arduino la conectare/reconectare
     * iar prima data el este verificat inainte sa se apeleze aceasta functie din {@link #startUpApplier()}.
     *
     * @param door lista cu setarile usii (aprindere automata a luminii, oprire automata, culoarea)
     */
    void applyDoorEvents(List<String> door) {
        //<editor-fold desc="body" defaultstate="collapsed">
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            boolean aprindereAutomata = Boolean.valueOf(door.get(0));
            boolean oprireAutomata = Boolean.valueOf(door.get(1));
            boolean aplicareStartup = Boolean.valueOf(door.get(2));
            int color = Integer.valueOf(door.get(3));
            on_color = color;
            sendDoorCommands(aprindereAutomata, oprireAutomata, color);
            Thread.sleep(60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //</editor-fold>
    }


    /**
     * Functie generala care apeleaza toate functiile ce trimit date legate de configuratia Arduino, folosita in principiu doar la
     * pornirea programului (cu o conexiune realizata) sau la reconectarea cu Arduino
     * Momentan au fost implementate doar setari legate de automatizarea usii, ar mai putea fi implementate setari ale senzorului
     * cu ultrasunete... etc.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    void startUpApplier() throws IOException, ClassNotFoundException {
        //<editor-fold desc="body" defaultstate="collapsed">
        List<String> doorSaves = getDoorEvents();
        if (Boolean.valueOf(doorSaves.get(2))) {
            applyDoorEvents(doorSaves);
        } else {
            Log.println(Log.ASSERT, "CONNECT", "Setarile usii nu au fost aplicate ");
        }
        //</editor-fold>
    }


    /**
     * Functie apelata atunci cand este realizata conexiunea cu Arduino prin intermediul connectionCheckThread-ului sau a utilizatorului.
     */
    private void connectEvent() {
        //<editor-fold desc="body" defaultstate="collapsed">
        Log.println(Log.ASSERT, "Connect", "NE AM CONECTAT");

        try {
            startUpApplier();
            if (connectionCheckThread == null) {
                Log.println(Log.ASSERT, "connectEvent", "Pornim thread-ul connectionCheckThread si setam connected=true ca sa nu mai fie declansata functia connectionEstablished");
                connected = true;
                notificationManager.notify(App.NOTIFICATION_MANAGER_ID, notification);
                connectionChecker();
            }
        } catch (IOException e) {
            Log.println(Log.ASSERT, "Eroare", "startUpApplier");
        } catch (ClassNotFoundException e) {
            Log.println(Log.ASSERT, "Eroare", "startUpApplier");
        }
        //</editor-fold>
    }

    /**
     * Incearca conectarea automata la modulul la care a fost realizata o conexiune cu succes la ultima rulare a programului
     *
     * @param date Lista care contine numele,adresa si uuid-ul unui modul bluetooth, lista probabil creata cu ajutorul functiei loadBluetooth
     */
    void conectare_automata(final List<String> date) {
        //<editor-fold desc="body" defaultstate="collapsed">
        conectare_automata.setTitle("Se incearca conexiunea automata");
        final AlertDialog alert = conectare_automata.create();


        try {
            alert.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Thread thread = new Thread() {
            Handler mHandler = new Handler(Looper.getMainLooper()) {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void handleMessage(Message message) {
                    if (socket.isConnected()) {
                        builderInner.setPositiveButton("Aprinde luminile", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (socket.isConnected()) {
                                    connectButton.setBackground(getResources().getDrawable(R.drawable.connect));
                                    try {
                                        String ar[] = connected_color.split(" ");
                                        Values.set(Integer.valueOf(ar[1]), Integer.valueOf(ar[2]), Integer.valueOf(ar[3]));
                                        write(Values.impachetatBytes((byte) connected_color.charAt(0)));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    connectButton.setBackground(getResources().getDrawable(R.drawable.connect_off));
                                }
                                dialog.dismiss();
                            }
                        });
                        builderInner.setNeutralButton("Nu modifica luminile", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (socket.isConnected()) {
                                    connectButton.setBackground(getResources().getDrawable(R.drawable.connect));
                                } else {
                                    connectButton.setBackground(getResources().getDrawable(R.drawable.connect_off));
                                }
                                dialog.dismiss();
                            }
                        });
                        builderInner.setNegativeButton("Stinge luminile", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (socket.isConnected()) {
                                    connectButton.setBackground(getResources().getDrawable(R.drawable.connect));
                                    try {
                                        write("L 0 0 0\n");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    connectButton.setBackground(getResources().getDrawable(R.drawable.connect_off));
                                }
                                dialog.dismiss();
                            }
                        });
                        builderInner.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                connectButton.setBackground(getResources().getDrawable(R.drawable.connect));
                                dialog.dismiss();
                            }
                        });
                        builderInner.show();
                        if (PRIORITATE_STRIP == PRIORITATE_STRIP_ARGB) {
                            openFragment(argbFragment);
                        }
                    } else {
                        //daca conectarea automata da fail
                        try {
                            alert.dismiss();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                }
            };

            @Override
            public void run() {
                try {

                    connectTo(date.get(2));
                    if (socket.isConnected()) {
                        connectEvent();
                        builderInner.setTitle("Conexiune automată realizată");
                        alert.dismiss();
                        builderInner.setMessage(date.get(0) + " " + date.get(1));
                    } else {
                        builderInner.setTitle("Conexiune automată eșuată");

                    }
                    Message message = mHandler.obtainMessage(Message.CONTENTS_FILE_DESCRIPTOR, new Message());
                    message.sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        //</editor-fold>
    }

    /**
     * Apelata de sendDataEvent de fiecare data cand ColorPicker-ul este activ, aici practic este creat un timer care trimite
     * la fiecare {@link App#ARDUINO_TIMEOUT} milisecunde culoarea selectata a ColorPicker-ului, iar daca acceasi culoare este trimisa
     * de trei ori timer-ul este oprit automat de functia stopComms() prin intermediul {@link Values#same_counter}
     */
    @Deprecated
    void startComms() {
        //<editor-fold desc="body" defaultstate="collapsed">
        dataSender = new Timer();
        dataSender.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                synchronized (this) {

                    //Log.println(Log.ASSERT,"Culoare", String.valueOf(Values.pr)+" "+String.valueOf(Values.red));
                    Color c = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        c = Color.valueOf(colorPicker.getSelectedColor());
                        int red = map(c.red(), 0, 1, 0, 255);
                        int green = map(c.green(), 0, 1, 0, 255);
                        int blue = map(c.blue(), 0, 1, 0, 255);
                        Values.set(red, green, blue);
                        if (Values.same_counter > Values.SAME_THRESHOLD) {
                            stopComms();
                        }
                        try {
                            write(Values.impachetat());
                            Thread.sleep(40);
                            write(Values.impachetat());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                }
            }
        }, 0, 45);
        sending = true;
        //</editor-fold>
    }

    /**
     * Opreste dataSender-ul
     */
    void stopComms() {
        //<editor-fold desc="body" defaultstate="collapsed">
        dataSender.cancel();
        sending = false;
        //</editor-fold>
    }

    /**
     * Initializeaza variabile legate de ramura bluetooth, de aici este apelata si {@link MainActivity#conectare_automata}
     */
    void initialise_bluetooth() {
        //<editor-fold desc="body" defaultstate="collapsed">
        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();
        //BluetoothDevice[] devices = (BluetoothDevice[]) bondedDevices.toArray();
        for (BluetoothDevice device : bondedDevices) {
            arrayAdapter.add(device.getAddress() + " " + device.getName());
            nume.add(device.getName());
        }
        List<String> x = null;
        try {
            x = loadBluetooth();
            Log.println(Log.ASSERT, x.get(0), "Modulul salvat:-" + x.get(1) + " " + x.get(2));


        } catch (Exception ex) {
            Log.println(Log.ASSERT, "FUCK", "Nu am putut incarca fisierul save.svt");
        }
        if (x != null) {
            modulBluetooth = getDevice(x.get(1));
            conectare_automata(x);
        }
        //</editor-fold>
    }

    /**
     * Cand se schimba fragmentele, elementele din MainActivity nu vor sa dispara, asa ca le fortam cu {@link View#setVisibility(int)}
     *
     * @param state true daca vrem sa apara elementele din MainActivity, false in caz contrar
     */
    @SuppressLint("RestrictedApi")
    void setVisibility(boolean state) {
        //<editor-fold desc="body" defaulstate="collapsed">
        if (state) {
            connectButton.setVisibility(View.VISIBLE);
            colorPicker.setVisibility(View.VISIBLE);
            lightnessSlider.setVisibility(View.VISIBLE);
            disconnect.setVisibility(View.VISIBLE);
            light_on_fab.setVisibility(View.VISIBLE);
            light_off_fab.setVisibility(View.VISIBLE);
            asistent.setVisibility(View.VISIBLE);

        } else {
            connectButton.setVisibility(View.INVISIBLE);
            colorPicker.setVisibility(View.INVISIBLE);
            lightnessSlider.setVisibility(View.INVISIBLE);
            disconnect.setVisibility(View.INVISIBLE);
            light_on_fab.setVisibility(View.INVISIBLE);
            light_off_fab.setVisibility(View.INVISIBLE);
            asistent.setVisibility(View.INVISIBLE);
            setAsistentVisibility(false);
        }
        //</editor-fold>
    }

    /**
     * Initializeaza toate fragmentele folosite in aplicatie
     *
     * @param main majoritatea fragmentelor au nevoie de o referinta la main pentru indeplinirea diferitor functionalitati,ex {@link Toast#makeText(Context, int, int)},
     *             {@link MediaPlayer#create(Context, Uri)}
     */
    void initialiseFragments(MainActivity main) {
        //<editor-fold desc="body" defaultstate="collapsed">
        blankFragment = new BlankFragment();
        colorPickerSettings = new ColorPickerSettings(main);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        doorFragment = new doorEvents(main);
        musicFragment = new musicFragment(main);
        alarmFragment = new AlarmFragment(main);
        asistentFragment = new AsistentFragment(main);
        PC_FRAGMENT = new pcControl(main);
        pcSettings = new pcSettings(main);
        argbFragment = new ArgbFragment(main);
        argbCustomizeLightOnLightFragment = new Argb_customizeLightOnLightFragment(main);
        argbCustomizeLightOffFragment = new Argb_customizeLightOffFragment(main);
        argbCustomizeColorFragment = new Argb_customizeColorFragment(main);
        argbCustomizeRomanianFlagFragment = new Argb_customizeRomanianFlagFragment(main);
        argbCustomizeFastLedFragment = new Argb_customizeFastLedFragment(main);
        ledTypePriorityFragment = new LedTypePriorityFragment(main);
        onConnectionAutomationFragment = new FragmentOnConnectAutomation(main);
        //</editor-fold>
    }

    public static pcControl getPC() {
        return PC_FRAGMENT;
    }


    /**
     * Realizeaza tranzitia dintre fragmente
     *
     * @param fragment Fragmentul ce trebuie afisat
     */
    void openFragment(Fragment fragment) {
        //<editor-fold desc="body" defaulstate="collapsed">
        if (fragment != choosenFragment) {
            if (fragment != null) {
                FragmentTransaction manager = getSupportFragmentManager().beginTransaction();
                manager.replace(R.id.layout, fragment);
                setVisibility(false);
                manager.commit();
            } else {
                FragmentTransaction manager = getSupportFragmentManager().beginTransaction();
                Log.println(Log.ASSERT, "Fragment changer", "Deschide home");
                manager.replace(R.id.layout, blankFragment);
                manager.commit();
                setVisibility(true);
            }
            choosenFragment = fragment;
        }
        mDrawerLayout.closeDrawers();
        //</editor-fold>
    }

    /**
     * Salveaza momentat doar densitatea ColorPicker-ului in fisierul colorPicker.svt
     *
     * @throws IOException
     */
    void saveColorPicker() throws IOException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileOutputStream fos = this.openFileOutput("colorPicker.svt", Context.MODE_PRIVATE);
        List<Integer> x = new ArrayList<>();
        x.add(colorPicker.density);
        ObjectOutputStream objos = new ObjectOutputStream(fos);
        objos.writeObject(x);
        objos.close();
        fos.close();
        Log.println(Log.ASSERT, "Salvare", "Am salvat colorPicker.svt cu densitatea " + x.get(0));
        //</editor-fold>
    }

    /**
     * Reaplica densitatea ColorPicker-ului
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    void restoreColorPicker() throws IOException, ClassNotFoundException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileInputStream fis = this.openFileInput("colorPicker.svt");
        ObjectInputStream is = new ObjectInputStream(fis);
        List<Integer> simpleClass = (ArrayList<Integer>) is.readObject();
        colorPicker.setDensity(simpleClass.get(0));
        is.close();
        fis.close();
        //</editor-fold>
    }

    /**
     * Salveaza setarile legate de automatizarea usii cu ajutorul variabilelor din {@link doorEvents}, {@link doorEvents#aprindereAutomata},
     * {@link doorEvents#oprireAutomata doorEvents#aplicaStartup} si {@link doorEvents#alegeCuloare}
     * de asemenea aici se aplica schimbari si variabilei {@link #colorIntent} si {@link #colorPendingIntent} care seteaza actiunea
     * indeplinita de butonul home din notificarea principala
     *
     * @throws IOException
     */
    public void saveDoorEvents() throws IOException, NullPointerException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileOutputStream fos = this.openFileOutput("doorEvents.svt", Context.MODE_PRIVATE);
        List<String> x = new ArrayList<>();
        x.add(doorFragment.aprindereAutomata.isChecked() + "");
        x.add(doorFragment.oprireAutomata.isChecked() + "");
        x.add(doorFragment.aplicaStartup.isChecked() + "");
        ColorDrawable c = (ColorDrawable) (doorFragment.alegeCuloare.getBackground());
        x.add(c.getColor() + "");
        colorIntent.removeExtra(App.COLOR);
        colorIntent.putExtra(App.COLOR, c.getColor() + "");
        colorPendingIntent = PendingIntent.getBroadcast(this, 2, colorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        String whole = "";
        for (String s : x) {
            whole += s + " ";
        }
        ObjectOutputStream objos = new ObjectOutputStream(fos);
        objos.writeObject(x);
        objos.close();
        fos.close();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                PC_FRAGMENT.sendDoorInformation();
            } catch (Exception ex) {
                Log.println(Log.ASSERT, "Eroare", "Am incercat sa trimit update la pc " + ex.toString());
            }
        }

        Log.println(Log.ASSERT, "Salvare", "Am salvat doorEvents.svt cu proprietatile " + whole);
        //</editor-fold>
    }

    public void saveDoorEvents(boolean pa, boolean oa, boolean au, int culoare) throws IOException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileOutputStream fos = this.openFileOutput("doorEvents.svt", Context.MODE_PRIVATE);
        List<String> x = new ArrayList<>();
        x.add(pa + "");
        x.add(oa + "");
        x.add(au + "");
        x.add(culoare + "");
        colorIntent.removeExtra(App.COLOR);
        colorIntent.putExtra(App.COLOR, culoare + "");
        colorPendingIntent = PendingIntent.getBroadcast(this, 2, colorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        String whole = "";
        for (String s : x) {
            whole += s + " ";
        }
        ObjectOutputStream objos = new ObjectOutputStream(fos);
        objos.writeObject(x);
        objos.close();
        fos.close();

        Log.println(Log.ASSERT, "Salvare", "Am salvat doorEvents.svt cu proprietatile " + whole);
        //</editor-fold>
    }

    /**
     * Cauta setarile usii in fisierul doorEvents.svt
     *
     * @return Lista care contine caracteristicile usii
     * @throws IOException
     * @throws ClassNotFoundException
     */
    List<String> getDoorEvents() throws IOException, ClassNotFoundException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileInputStream fis = this.openFileInput("doorEvents.svt");
        ObjectInputStream is = new ObjectInputStream(fis);
        List<String> simpleClass = (ArrayList<String>) is.readObject();
        String whole = "";
        is.close();
        fis.close();
        return simpleClass;
        //</editor-fold>
    }

    /**
     * Aplica setarile vizuale din doorEvents din fisierul doorEvents.svt, este apelata de {@link doorEvents#mainInit(View)}
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    void restoreDoorEvents() throws IOException, ClassNotFoundException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileInputStream fis = this.openFileInput("doorEvents.svt");
        ObjectInputStream is = new ObjectInputStream(fis);
        List<String> simpleClass = (ArrayList<String>) is.readObject();
        String whole = "";
        for (String x : simpleClass)
            whole += x + " ";

        doorFragment.aprindereAutomata.setChecked(Boolean.valueOf(simpleClass.get(0)));
        doorFragment.oprireAutomata.setChecked(Boolean.valueOf(simpleClass.get(1)));
        doorFragment.aplicaStartup.setChecked(Boolean.valueOf(simpleClass.get(2)));
        int color = Integer.valueOf(simpleClass.get(3));
        doorFragment.alegeCuloare.setBackgroundColor(color);
        Log.println(Log.ASSERT, "Load door events", "Cu setarile :" + whole);
        is.close();
        fis.close();
        //</editor-fold>
    }

    /**
     * @param pa
     * @param oa
     * @param aa
     * @param culoare
     * @throws IOException
     * @throws ClassNotFoundException
     */
    void restoreDoorEvents(boolean pa, boolean oa, boolean aa, int culoare) throws IOException, ClassNotFoundException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileInputStream fis = this.openFileInput("doorEvents.svt");
        ObjectInputStream is = new ObjectInputStream(fis);
        List<String> simpleClass = (ArrayList<String>) is.readObject();
        String whole = "";
        for (String x : simpleClass)
            whole += x + " ";
        is.close();
        fis.close();
        try {
            doorFragment.aprindereAutomata.setChecked(pa);
            doorFragment.oprireAutomata.setChecked(oa);
            doorFragment.aplicaStartup.setChecked(aa);
            doorFragment.alegeCuloare.setBackgroundColor(culoare);
        } catch (Exception ex) {
            Log.println(Log.ASSERT, "eroare", "restoreDoorEvents");
            ex.printStackTrace();
        }

        Log.println(Log.ASSERT, "Load door events", "Cu setarile :" + whole);

        //</editor-fold>
    }

    /**
     * doar reseteaza alarma, nu si GUI-ul
     */
    void restoreAlarm() {
        //<editor-fold desc="body" defaultstate="collapsed">
        try {
            List<String> alarm = AlarmFragment.loadAlarmStatic();
            boolean b = Boolean.valueOf(alarm.get(3));
            if (b) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AlarmFragment.seteazaAlarmaMain(MainActivity.this);
                }
            } else {
                Log.println(Log.ASSERT, "Alarm", "Nu avem alarma");
            }
        } catch (IOException | NullPointerException ex) {
            ex.printStackTrace();
        }
        //</editor-fold>
    }

    /**
     * Functie generala pentru reaplicarea setarilor la inceput de program cu o exceptie la setarile usii
     */
    void restoreSettings() {
        //<editor-fold desc="body" defaultstate="collapsed">
        try {
            restoreColorPicker();
            Log.println(Log.ASSERT, "Load", "Incarcare date pentru colorPicker finalizata cu succes");
        } catch (IOException e) {
            Log.println(Log.ASSERT, "Eroare", "IOException (restoreSettings)");
        } catch (ClassNotFoundException e) {
            Log.println(Log.ASSERT, "Eroare", "ClassNotFoundException (restoreSettings)");
        }
        try {
            restoreAlarm();
        } catch (Exception ex) {
            Log.println(Log.ASSERT, "EROARE", "Nu am putut reseta alarma");
        }
        //</editor-fold>
        //nu poate fi strigat de aici pentru ca nu pot fi initializate switch-urile din doorEvents inainte de apelare, apelarea se face din doorEvenst{onCreateView}
       /* try {
            restoreDoorEvents();
        } catch (IOException e) {
            Log.println(Log.ASSERT,"Eroare","IOException (restoreSettings)");
        } catch (ClassNotFoundException e) {
            Log.println(Log.ASSERT,"Eroare","ClassNotFoundException (restoreSettings)");
        }*/
    }

    /**
     * {@link #handlerChangeBackground} actionat de connectionCheckThread prin intermediul {@link #connectionEstablished()}, reseteaza elementele vizuale la conectarea/deconectarea cu
     * Arduino
     */
    Handler handlerChangeBackground = new Handler(Looper.getMainLooper()) {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void handleMessage(Message message) {
            Log.println(Log.ASSERT, "Handler", connected + "");
            if (connected) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    connectButton.setBackground(getResources().getDrawable(R.drawable.connect));
                }
                Log.println(Log.ASSERT, "INTRA AICI", "FGM");
                notificationManager.notify(App.NOTIFICATION_MANAGER_ID, notification);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    connectButton.setBackground(getResources().getDrawable(R.drawable.connect_off));
                }
                notificationManager.cancel(App.NOTIFICATION_MANAGER_ID);
            }

        }
    };

    /**
     * Functie apelata de connectionCheckThread care incearca sa retrimita setarile la Arduino, apeland {@link #startUpApplier()}
     * si alte functii anale
     */
    void connectionEstablished() {
        //<editor-fold desc="body" defaultstate="collapsed">
        Log.println(Log.ASSERT, "Connection OK", "  ");
        connected = true;
        try {
            Log.println(Log.ASSERT, "Call applier", "Reincercam aplicarea setarilor");
            startUpApplier();
            onConnectionAutomationFragment.arduinoConnected();
        } catch (IOException e) {
            e.printStackTrace();
            Log.println(Log.ASSERT, "Call applier", "FAIL IOException");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.println(Log.ASSERT, "Call applier", "FAIL ClassNotFoundException");
        }
        Message message = handlerChangeBackground.obtainMessage(Message.CONTENTS_FILE_DESCRIPTOR, new Message());
        message.sendToTarget();
        //</editor-fold>
    }

    /**
     * Functie apelata de connectionCheckThread care notifica elementele vizuale de deconectarea de la Arduino
     */
    void connectionLost() {
        //<editor-fold desc="body" defaultstate="collapsed">
        Log.println(Log.ASSERT, "Connection LOST", "  ");
        onConnectionAutomationFragment.notifyConnectionLost();
        connected = false;
        Message message = handlerChangeBackground.obtainMessage(Message.CONTENTS_FILE_DESCRIPTOR, new Message());
        message.sendToTarget();
        //</editor-fold>
    }

    /**
     * Initializeaza si porneste thread-ul care verifica conectarea la arduino la fiecare {@link #CONNECTED_SLEEP_TIME} milisecunde cand
     * aplicatia are o conexiune cu Arduino si la {@link #DISCONNECTED_SLEEP_TIME} milisecunde in caz contrar, aceasta verificare consta in
     * trimiterea unui ping la fiecare {@link #SLEEP_TIME} milisecunde (fie {@link #DISCONNECTED_SLEEP_TIME}, fie {@link #CONNECTED_SLEEP_TIME})
     * !ATENTIE!
     * aceasta functie nu este apelata daca de la inceputul rularii programului nu s-a realizat nicio conexiune, asadar functionalitatea
     * de auto-reconnect nu mai este valabila decat dupa o conectare manuala
     */
    void connectionChecker() {
        //<editor-fold desc="body" defaultstate="collapsed">
        connectionCheckThread = new Thread() {
            @Override
            public void run() {
                while (runCheker) {
                    try {
                        Thread.sleep(SLEEP_TIME);

                        boolean conexiune;

                        if (!sending) {

                            try {
                                Thread.sleep(App.ARDUINO_TIMEOUT);
                                write(">\n");
                                conexiune = true;
                            } catch (Exception ex) {
                                conexiune = false;
                            }
                            //Log.println(Log.ASSERT,"Verificam conexiunea",conexiune+"");

                            if (conexiune && !connected) {
                                connectionEstablished();
                            }
                            if (!conexiune && connected) {
                                connectionLost();
                            }
                            if (!conexiune) {
                                SLEEP_TIME = DISCONNECTED_SLEEP_TIME;
                                try {
                                    connectTo(modulBluetooth.getUuids()[0].toString());
                                } catch (Exception ex) {
                                    SLEEP_TIME = CONNECTED_SLEEP_TIME;
                                }
                            } else {
                                SLEEP_TIME = CONNECTED_SLEEP_TIME;
                            }
                        }


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        connectionCheckThread.start();
        //</editor-fold>
    }

    /**
     * Initializarea componentelor legate de ramura notificarilor
     */
    void initNotification() {
        //<editor-fold desc="body" defaultstate="collapsed">
        NotificationReceiver.main = this;
        notificationManager = NotificationManagerCompat.from(this);
        Intent turnOnIntent = new Intent(this, NotificationReceiver.class);
        Intent turnOffIntent = new Intent(this, NotificationReceiver.class);
        Intent aprindereUsaIntent = new Intent(this, NotificationReceiver.class);
        Intent assistentIntent = new Intent(this, NotificationReceiver.class);


        colorIntent = new Intent(this, NotificationReceiver.class);
        List<String> optiuni = null;
        try {
            optiuni = getDoorEvents();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Salveaza din nou setarile in automatizare usa", Toast.LENGTH_LONG).show();
        }
        if (optiuni != null) {
            colorIntent.putExtra(App.COLOR, optiuni.get(3));
        } else {
            colorIntent.putExtra(App.COLOR, "#FFFFFF");
        }
        turnOnIntent.putExtra(App.LIGHT_ON, "L1\n");
        turnOffIntent.putExtra(App.LIGHT_OFF, "L0\n");
        assistentIntent.putExtra(App.ASSISTANT, "rec");


        aprindereUsaIntent.putExtra(App.APRINDERE, "M A ATINS");
        PendingIntent aprindereUsaPendingIntent = PendingIntent.getBroadcast(this, 3, aprindereUsaIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent turnOnPendingIntent = PendingIntent.getBroadcast(this, 0, turnOnIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent turnOffPendingIntent = PendingIntent.getBroadcast(this, 1, turnOffIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent assistantPendingIntent = PendingIntent.getBroadcast(this, 4, assistentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        colorPendingIntent = PendingIntent.getBroadcast(this, 2, colorIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        collapsedView = new RemoteViews(getPackageName(), R.layout.notification_collapse);
        expandedView = new RemoteViews(getPackageName(), R.layout.notification_expanded);

        collapsedView.setOnClickPendingIntent(R.id.notification_light_on, turnOnPendingIntent);
        collapsedView.setOnClickPendingIntent(R.id.notification_light_off, turnOffPendingIntent);
        collapsedView.setOnClickPendingIntent(R.id.home, colorPendingIntent);
        collapsedView.setOnClickPendingIntent(R.id.asistoff, turnOffPendingIntent);
        collapsedView.setOnClickPendingIntent(R.id.asiston, turnOnPendingIntent);


        expandedView.setOnClickPendingIntent(R.id.asistentul_expanded, assistantPendingIntent);
        expandedView.setOnClickPendingIntent(R.id.notification_light_on_expanded, turnOnPendingIntent);
        expandedView.setOnClickPendingIntent(R.id.notification_light_off_expanded, turnOffPendingIntent);
        expandedView.setOnClickPendingIntent(R.id.home_expanded, colorPendingIntent);
        expandedView.setOnClickPendingIntent(R.id.asistoff_expanded, turnOffPendingIntent);
        expandedView.setOnClickPendingIntent(R.id.asiston_expanded, turnOnPendingIntent);


        Intent openMainIntent = getPackageManager()
                .getLaunchIntentForPackage(getPackageName())
                .setPackage(null)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent openMainPendingIntent = PendingIntent.getActivity(this, 2, openMainIntent, 0);
        //collapsedView.setOnClickPendingIntent(R.id.home,openMainPendingIntent);

        notification = new NotificationCompat.Builder(this, App.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.connect)
                /*.setContentTitle("SMART HOME SYSTEM")
                .setContentText("Shortcuts")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
                .setAutoCancel(true)

                .setContentIntent(null)
                .addAction(R.drawable.light_off,"Stinge",turnOffPendingIntent)
                .addAction(R.drawable.light_on, "Aprinde", turnOnPendingIntent)*/
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
                .setChannelId(App.CHANNEL_1_ID)
                .setOngoing(true)
                .setCustomContentView(collapsedView)
                .setCustomBigContentView(expandedView)
                .setContentIntent(openMainPendingIntent)
                //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .build();


        //notificationManager.cancel(0);
        //</editor-fold>
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    /**
     * Ascunde toata interfata vizuala a asistentului
     *
     * @param delay dupa cate milisecunde sa fie timid asistentul
     */
    void hidePepe(int delay) {
        //<editor-fold desc="body">
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!ascultator.running) {
                    continutAsistent.setText("");
                    setAsistentVisibility(false);
                    if (!hasWindowFocus()) {
                        Log.println(Log.ASSERT, "ASISTENT", "SET TEXT");
                        expandedView.setTextViewText(R.id.text_notificare_asistent, "");
                        notificationManager.notify(App.NOTIFICATION_MANAGER_ID, notification);
                    }
                }
            }
        }, delay);
        //</editor-fold>
    }

    /**
     * Apelata de {@link Ascultator} atunci cand {@link MainActivity#sr} este initializat cu succecs si
     * cauta Speech
     */
    void asistentReady() {
        //<editor-fold desc="body">
        if (!ascultator.running) {
            asistent.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            if (choosenFragment == PC_FRAGMENT) {
                PC_FRAGMENT.asistent.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            }
            if (choosenFragment == argbFragment) {
                argbFragment.asistent.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            }
            //<editor-fold desc="notificare">
            if (!hasWindowFocus()) {
                Log.println(Log.ASSERT, "ASISTENT", "SET TEXT");
                expandedView.setTextViewText(R.id.text_notificare_asistent, traducator.LIMBA == App.LIMBA_ROMANA ? "Vorbeste" : "Talk");
                notificationManager.notify(App.NOTIFICATION_MANAGER_ID, notification);
            }
            //</editor-fold>

            setAsistentVisibility(true);
            ascultator.setState(true);
        }
        //</editor-fold>
    }

    /**
     * Apelata de {@link Ascultator} atunci cand {@link MainActivity#sr} detecteaza sunet
     */
    void asistentRecording() {
        //<editor-fold desc="body">
        asistent.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        if (choosenFragment == PC_FRAGMENT) {
            PC_FRAGMENT.asistent.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        }
        if (choosenFragment == argbFragment) {
            argbFragment.asistent.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        }
        setAsistentVisibility(true);
        ascultator.setState(true);
        //</editor-fold>
    }

    public int getAsistentRestingColor() {
        return App.NIGHT_MODE_ENABLED ? App.NIGHT_MODE_BUTTON_BACKGROUND : Color.WHITE;
    }

    /**
     * Apelata de {@link Ascultator} atunci cand {@link MainActivity#sr} a terminat cu succes detectarea
     */
    void endOfSpeech() {
        //<editor-fold desc="body">
        asistent.setBackgroundTintList(ColorStateList.valueOf(getAsistentRestingColor()));
        if (choosenFragment == PC_FRAGMENT) {
            PC_FRAGMENT.asistent.setBackgroundTintList(ColorStateList.valueOf(getAsistentRestingColor()));
        }
        if (choosenFragment == argbFragment) {
            argbFragment.asistent.setBackgroundTintList(ColorStateList.valueOf(getAsistentRestingColor()));
        }
        ascultator.setState(false);
        //</editor-fold>
    }

    void asistentPartialResult(Bundle results) {
        //<editor-fold desc="body">
        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        for (int i = 0; i < data.size(); i++) {
            Log.println(Log.ASSERT, "PART-RESULT", data.get(i) + "");
        }
        if (continutAsistent.getVisibility() == View.INVISIBLE)
            setAsistentVisibility(true);
        continutAsistent.setText(String.valueOf(data.get(0)));
        //<editor-fold desc="notificare">
        if (!hasWindowFocus()) {
            Log.println(Log.ASSERT, "ASISTENT", "SET TEXT");
            expandedView.setTextViewText(R.id.text_notificare_asistent, String.valueOf(data.get(0)));
            notificationManager.notify(App.NOTIFICATION_MANAGER_ID, notification);
        }
        //</editor-fold>
        //</editor-fold>
    }

    /**
     * @param culoare culoare sub forma string (mov, galben ... etc)
     * @return forma universala a culorii specificate de ex {@link App#MOV}
     */
    String getCuloare(String culoare) {
        //<editor-fold desc="body">
        if (culoare.equals("galben") || culoare.equals("galbena") || culoare.equals("galbene"))
            return App.GALBEN;
        if (culoare.equals("mov") || culoare.equals("mova"))
            return App.MOV;
        if (culoare.equals("rosu") || culoare.equals("rosie") || culoare.equals("rosii"))
            return App.ROSU;
        if (culoare.equals("portocaliu") || culoare.equals("portocalie") || culoare.equals("portocalii") || culoare.equals("portocaliul"))
            return App.PORTOCALIU;
        if (culoare.equals("verde") || culoare.equals("verzi"))
            return App.VERDE;
        if (culoare.equals("albastru") || culoare.equals("albastre") || culoare.equals("albastra") || culoare.equals("albastrul"))
            return App.ALBASTRU;
        return App.ROSU;
        //</editor-fold>
    }

    Traducator.Intentie ultimaIntentie;

    /**
     * Primeste o {{@link Traducator.Intentie}} de la {{@link Traducator}} care contine comenzile utilizatorului
     * si toate datele legate de acestea si le executa
     *
     * @param intentie Toate actiunile se salveaza in final in {{@link Traducator.Intentie#pachet}}
     */
    void proceseazaComanda(final Traducator.Intentie intentie) {
        //<editor-fold desc="body">

        for (int intentIterator = 0; intentIterator < intentie.pachet.length; intentIterator++) {
            switch (intentie.pachet[intentIterator].comanda) {
                case App.COMANDA_PORNESTE:
                    Log.println(Log.ASSERT, "COMANDA", "PORNIRE");
                    if (MainActivity.PRIORITATE_STRIP == MainActivity.PRIORITATE_STRIP_NORMAL) {
                        try {
                            write("L1\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (MainActivity.PRIORITATE_STRIP == MainActivity.PRIORITATE_STRIP_ARGB) {
                        try {
                            write(ARGB.TURN_ON_WHITE_LIGHT);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    break;
                case App.COMANDA_OPRIRE:
                    Log.println(Log.ASSERT, "COMANDA", "OPRIRE");
                    if (MainActivity.PRIORITATE_STRIP == MainActivity.PRIORITATE_STRIP_NORMAL) {
                        try {
                            write("L0\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (MainActivity.PRIORITATE_STRIP == MainActivity.PRIORITATE_STRIP_ARGB) {
                        try {
                            write(ARGB.TURN_OFF);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    break;
                case App.COMANDA_SETEAZA_CULOARE:
                    Log.println(Log.ASSERT, "culoare", intentie.pachet[intentIterator].detalii[1]);
                    try {
                        int color = loadCuloare(getCuloare(intentie.pachet[intentIterator].detalii[1]));
                        int r, g, b;
                        r = App.getRed(color);
                        g = App.getGreen(color);
                        b = App.getBlue(color);
                        Log.println(Log.ASSERT, "CULOARE", r + " " + g + " " + b);
                        Values.set(r, g, b);
                        if (MainActivity.PRIORITATE_STRIP == MainActivity.PRIORITATE_STRIP_NORMAL) {
                            write(Values.impachetatBytes((byte) 'K'));
                        } else {
                            write(ARGB.TURN_ON_CUSTOM_COLOR, new SystemColor(r, g, b));
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case App.COMANDA_ALARMA:
                    Log.println(Log.ASSERT, "BA", "cica vrea alarma " + intentie.pachet[intentIterator].alarma.tip + " ora:"
                            + intentie.pachet[intentIterator].alarma.ora + " minute:" + intentie.pachet[intentIterator].alarma.minut);
                    if (intentie.pachet[intentIterator].alarma.tip == App.ALARMA_LA) {
                        try {
                            AlarmFragment.alarmaAsistent_LA(intentie.pachet[intentIterator].alarma.ora,
                                    intentie.pachet[intentIterator].alarma.minut, this);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (intentie.pachet[intentIterator].alarma.tip == App.ALARMA_IN) {
                        try {
                            AlarmFragment.alarmaAsistent_IN(intentie.pachet[intentIterator].alarma.ora,
                                    intentie.pachet[intentIterator].alarma.minut, this);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case App.COMANDA_SETEAZA_INTENSITATE_CULOARE:
                    try {
                        int color = loadCuloare(getCuloare(intentie.pachet[intentIterator].detalii[0]));
                        int r, g, b;
                        r = g = b = 255;
                        int intensitate = intentie.pachet[intentIterator].intensitate;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            r = App.getRed(color);
                            g = App.getGreen(color);
                            b = App.getBlue(color);
                            r = r * intensitate / 100;
                            g = g * intensitate / 100;
                            b = b * intensitate / 100;
                            Log.println(Log.ASSERT, "CULOARE intensitate", r + " " + g + " " + b);
                            Values.set(r, g, b);
                            if (MainActivity.PRIORITATE_STRIP == PRIORITATE_STRIP_NORMAL) {
                                write(Values.impachetatBytes((byte) 'K'));
                            } else if (MainActivity.PRIORITATE_STRIP == PRIORITATE_STRIP_ARGB) {
                                write(ARGB.TURN_ON_CUSTOM_COLOR, new SystemColor(r, g, b));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case App.COMANDA_MUZICA:
                    Log.println(Log.ASSERT, "VREA MUZICA", "Trimitem w " + intentie.pachet[intentIterator].piesa.nume);
                    //FIX##
                    //BA POATE FACEM CV CU ASTA
                    //CAND NU E CONBECTAT LA BT
                    //SI UTIlizatoru vrea piesa, da networking on main thread exception
                    //cheap and easy repair
                    Message message = voiceSearchHandler.obtainMessage(Message.CONTENTS_FILE_DESCRIPTOR, new Message());
                    message.obj = intentie;
                    message.sendToTarget();
                    break;
                case App.COMANDA_APRINDE_LUMINA_MARE:
                    try {
                        write("RA1");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case App.COMANDA_STINGE_LUMINA_MARE:
                    try {
                        write("RA0");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            break;
        }

        //</editor-fold>
    }

    Handler voiceSearchHandler = new Handler(Looper.getMainLooper()) {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void handleMessage(Message message) {

            final Traducator.Intentie intentie = (Traducator.Intentie) message.obj;
            new Thread() {
                public void run() {
                    try {
                        if (App.CONNECTION_TYPE == App.ANDROID_MASTER) {
                            PC_FRAGMENT.pcOutputStream.write((App.COMMAND_YOUTUBE_PLAY + " " + intentie.pachet[intentIterator].piesa.nume + "\n").getBytes());
                        } else if (App.CONNECTION_TYPE == App.PC_MASTER) {
                            write((App.COMMAND_YOUTUBE_PLAY + " " + intentie.pachet[intentIterator].piesa.nume + "\n").getBytes());
                        }
                    } catch (IOException | NullPointerException e) {
                        try {
                            PC_FRAGMENT.connect();
                            Thread.sleep(1000);
                            Log.println(Log.ASSERT, "Conexiune", "cu succes");
                            PC_FRAGMENT.pcOutputStream.write((App.COMMAND_YOUTUBE_PLAY + " " + intentie.pachet[intentIterator].piesa.nume + "\n").getBytes());
                        } catch (Exception ex) {
                            /*Toast.makeText(MainActivity.this,"Conectare esuata",Toast.LENGTH_SHORT)
                                    .show();*/
                            Log.println(Log.ASSERT, "CONEXIUNE", "ESUATA " + ex.toString() + "\n");
                            ex.printStackTrace();
                        }
                    }
                }
            }.start();

        }
    };

    /**
     * Apelata de {{@link Ascultator}}
     *
     * @param results
     */
    void asistentFinalResult(Bundle results) {
        //<editor-fold desc="body">
        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        try {
            proceseazaComanda(traducator.tradu(data.get(0).toString()));
        } catch (Exception ex) {
            Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show();
        }
        Log.println(Log.ASSERT, "rezultat", data.get(0).toString());
        //</editor-fold>
    }

    /**
     * Apelata de  {{@link Ascultator}}
     *
     * @param error
     */
    void asistentError(int error) {
        //<editor-fold desc="body">
        asistent.setBackgroundTintList(ColorStateList.valueOf(getAsistentRestingColor()));
        if (choosenFragment == PC_FRAGMENT) {
            PC_FRAGMENT.asistent.setBackgroundTintList(ColorStateList.valueOf(getAsistentRestingColor()));
        }
        if (choosenFragment == argbFragment) {
            argbFragment.asistent.setBackgroundTintList(ColorStateList.valueOf(getAsistentRestingColor()));
        }
        hidePepe(0);
        ascultator.running = false;
        Log.println(Log.ASSERT, "error", "la asistent " + error);
        //</editor-fold>
    }

    /**
     * Salveaza referinta pentru fiecare culoare intr-un fisieri separat
     * Aici ai putea sa mai adaugi optiunea ca utilizatorul sa isi seteze nuanta de culoare pe care
     * o vrea cand ii spune asistentului sa aprinda acea culoare
     *
     * @throws IOException
     */
    void saveColors() throws IOException {
        //<editor-fold desc="body">
        int c = Color.parseColor("#a000fc");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            saveColor(App.MOV, c);
            Log.println(Log.ASSERT, "SALVAT MOV", App.getRed(c) + " " + App.getGreen(c) + " " + App.getBlue(c));
        }

        c = -14592;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            saveColor(App.GALBEN, c);
            Log.println(Log.ASSERT, "SALVAT MOV", App.getRed(c) + " " + App.getGreen(c) + " " + App.getBlue(c));
        }

        c = Color.parseColor("#ff0000");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            saveColor(App.ROSU, c);
            Log.println(Log.ASSERT, "SALVAT MOV", App.getRed(c) + " " + App.getGreen(c) + " " + App.getBlue(c));
        }

        c = -52224;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            saveColor(App.PORTOCALIU, c);
            Log.println(Log.ASSERT, "SALVAT MOV", App.getRed(c) + " " + App.getGreen(c) + " " + App.getBlue(c));
        }

        c = Color.parseColor("#00ff00");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            saveColor(App.VERDE, c);
            Log.println(Log.ASSERT, "SALVAT MOV", App.getRed(c) + " " + App.getGreen(c) + " " + App.getBlue(c));
        }

        c = Color.parseColor("#0000ff");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            saveColor(App.ALBASTRU, c);
            Log.println(Log.ASSERT, "SALVAT MOV", App.getRed(c) + " " + App.getGreen(c) + " " + App.getBlue(c));
        }
        //</editor-fold>
    }

    void saveColor(String nume, int culoare) throws IOException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileOutputStream fos = this.openFileOutput(nume + ".svt", Context.MODE_PRIVATE);
        List<String> x = new ArrayList<>();
        ObjectOutputStream os = new ObjectOutputStream(fos);
        x.add(culoare + "");
        os.writeObject(x);
        os.close();
        fos.close();
        Log.println(Log.ASSERT, getFilesDir().toString(), "Am salvat fisierul " + nume + ".svt");
        //</editor-fold>
    }

    int loadCuloare(String nume) throws IOException, ClassNotFoundException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileInputStream fis = this.openFileInput(nume + ".svt");
        ObjectInputStream is = new ObjectInputStream(fis);
        List<String> simpleClass = (ArrayList<String>) is.readObject();
        is.close();
        fis.close();
        return Integer.valueOf(simpleClass.get(0));
        //</editor-fold>
    }

    /**
     * O apelezi daca vrei sa activezi/opresti SpeechRecognizer
     * De exemplu iti faci un simplu buton
     * si de fiecare data cand e apasat, apelezi functia asta, ea are grija de restul
     */
    void listenEvent() {
        //<editor-fold desc="body">
        List<String> limba = AsistentFragment.loadAsistent(MainActivity.this);
        Log.println(Log.ASSERT, "limba asistent", String.valueOf(limba));
        if (ascultator.running) {
            sr.stopListening();
            hidePepe(3000);
            ascultator.setState(false);
            asistent.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            if (choosenFragment == PC_FRAGMENT) {
                PC_FRAGMENT.asistent.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            }
            if (choosenFragment == argbFragment) {
                argbFragment.asistent.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            }
            Log.println(Log.ASSERT, "ERROR", "alerg deja");
            return;
        }
        if (Integer.valueOf(limba.get(0)) == App.LIMBA_ENGLEZA) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            sr.startListening(intent);
            Log.println(Log.ASSERT, "SPEECH", "Am creat recognizer in engleza");
        } else if (isNetworkConnected()) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ro-RO");
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            sr.startListening(intent);
            Log.println(Log.ASSERT, "SPEECH", "Am creat recognizer in romana");
        } else {
            Toast.makeText(MainActivity.this, "Nu exista conexiune la internet, " +
                    "comenzile vor fi in engleza", Toast.LENGTH_LONG).show();
            limbaAsistent.setText(App.PEPE_ENGLEZ);
            try {
                AsistentFragment.saveAsistent(MainActivity.this, App.LIMBA_ENGLEZA);
            } catch (IOException e) {
                Log.println(Log.ASSERT, "ERROR", "Nu am putut salva asistentul");
            }
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            sr.startListening(intent);
            Log.println(Log.ASSERT, "SPEECH", "Am creat recognizer in engleza");
        }
        //</editor-fold>
    }

    public static void setDrawableFilterColor(Context context, int colorResource, Drawable drawable) {
        //noinspection ResourceType
        int filterColor = colorResource;
        drawable.setColorFilter(new PorterDuffColorFilter(filterColor, PorterDuff.Mode.MULTIPLY));
    }

    public void nightModePreset() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mDrawerLayout.setBackground(new ColorDrawable(App.NIGHT_MODE_BACKGROUND_MAIN));
            light_on_fab.setBackgroundTintList(ColorStateList.valueOf(App.NIGHT_MODE_BUTTON_BACKGROUND));
            light_off_fab.setBackgroundTintList(ColorStateList.valueOf(App.NIGHT_MODE_BUTTON_BACKGROUND));
            asistent.setBackgroundTintList(ColorStateList.valueOf(App.NIGHT_MODE_BUTTON_BACKGROUND));
            disconnect.setBackgroundTintList(ColorStateList.valueOf(App.NIGHT_MODE_BUTTON_BACKGROUND));

            findViewById(R.id.nav_view).setBackgroundColor(App.NIGHT_MODE_BACKGROUND_SECONDARY);

            setDrawableFilterColor(this, App.NIGHT_MODE_BUTTON_BACKGROUND, meniuButton.getBackground());
            meniuButton.setTextColor(App.NIGHT_MODE_TEXT);
            meniuButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_home_black_night_24dp, 0, 0, 0);

            ((TextView) (findViewById(R.id.textView))).setTextColor(App.NIGHT_MODE_TEXT);
            ((TextView) (findViewById(R.id.textView2))).setTextColor(App.NIGHT_MODE_TEXT);

            setDrawableFilterColor(this, App.NIGHT_MODE_BUTTON_BACKGROUND, colorSettingsButton.getBackground());
            colorSettingsButton.setTextColor(App.NIGHT_MODE_TEXT);
            colorSettingsButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_color_lens_black_night_24dp, 0, 0, 0);

            setDrawableFilterColor(this, App.NIGHT_MODE_BUTTON_BACKGROUND, openAlarmFragmentButton.getBackground());
            openAlarmFragmentButton.setTextColor(App.NIGHT_MODE_TEXT);
            openAlarmFragmentButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_alarm_black_night_24dp, 0, 0, 0);


            setDrawableFilterColor(this, App.NIGHT_MODE_BUTTON_BACKGROUND, door_settings.getBackground());
            door_settings.setTextColor(App.NIGHT_MODE_TEXT);
            door_settings.setCompoundDrawablesWithIntrinsicBounds(R.drawable.door_event_night, 0, 0, 0);

            setDrawableFilterColor(this, App.NIGHT_MODE_BUTTON_BACKGROUND, openAsistentFragmentButton.getBackground());
            openAsistentFragmentButton.setTextColor(App.NIGHT_MODE_TEXT);
            openAsistentFragmentButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pepeicon_night, 0, 0, 0);

            setDrawableFilterColor(this, App.NIGHT_MODE_BUTTON_BACKGROUND, openPCFragment.getBackground());
            openPCFragment.setTextColor(App.NIGHT_MODE_TEXT);
            openPCFragment.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pc_night, 0, 0, 0);

            setDrawableFilterColor(this, App.NIGHT_MODE_BUTTON_BACKGROUND, openArgb.getBackground());
            openArgb.setTextColor(App.NIGHT_MODE_TEXT);
            openArgb.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_argb_night, 0, 0, 0);

            setDrawableFilterColor(this, App.NIGHT_MODE_BUTTON_BACKGROUND, openOnConnectAutomation.getBackground());
            openOnConnectAutomation.setTextColor(App.NIGHT_MODE_TEXT);
            openOnConnectAutomation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_on_connect_automatizare_night, 0, 0, 0);
        } else {
            Log.println(Log.ASSERT, "Eroare", "Nu am putut seta presetup de night mode pe versiunea asta de android");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //<editor-fold desc="body" defaultstate="collapsed">
        super.onCreate(savedInstanceState);

        try {
            StoreClass store = loadPrioritateLedType();
            PRIORITATE_STRIP = store.value;
            App.NIGHT_MODE_ENABLED = store.darkMode;
            Log.println(Log.ASSERT, "am aplicat", "datele prioritate led");
        } catch (Exception ex) {
            Log.println(Log.ASSERT, "eroare", "nu am putut incarca prioritatea ledurilor,vezi debug");
            ex.printStackTrace();
        }

        try {
            FragmentOnConnectAutomation.ON_CONNECT_AUTOMATION_SETTINGS = loadOnConnectStore();
        } catch (IOException | ClassNotFoundException e) {
            FragmentOnConnectAutomation.ON_CONNECT_AUTOMATION_SETTINGS=FragmentOnConnectAutomation.Store.getDefault();
            Log.println(Log.ASSERT, "ERR", e.toString());
            e.printStackTrace();

        }

        pcReader = new PCReader(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        initialiseScreenComponents();
        pTime = System.currentTimeMillis();
        try {
            initialise_bluetooth();
        } catch (Exception ex) {
            Toast.makeText(this, "Bluetooth oprit!", Toast.LENGTH_SHORT).show();
            BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
            bAdapter.enable();
            try {
                Thread.sleep(1500);
                initialise_bluetooth();
            } catch (Exception exx) {
                Toast.makeText(this, "Nu am putut initializa setarile bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
        initialiseFragments(this);
        initNotification();
        restoreSettings();

        door_settings = findViewById(R.id.setari_usa);
        door_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.println(Log.ASSERT, "Change fragment", "Open door settings");
                openFragment(doorFragment);
            }
        });

        connectButton = findViewById(R.id.connection_status);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builderSingle.show();
            }
        });
        connectButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openFragment(ledTypePriorityFragment);
                return true;
            }
        });
        colorSettingsButton = findViewById(R.id.bColor);
        meniuButton = findViewById(R.id.meniuButton);
        meniuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.println(Log.ASSERT, "Change fragment", "Open main menu ");
                openFragment(null);
            }
        });
        colorSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.println(Log.ASSERT, "Change fragment", "Open color picker settings ");
                openFragment(colorPickerSettings);
            }
        });
        openAlarmFragmentButton = findViewById(R.id.alarm_button);
        openAlarmFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.println(Log.ASSERT, "Change fragment", "Open alarm fragment ");
                openFragment(alarmFragment);
            }
        });
        openMusicFragmentButton = findViewById(R.id.music_button);
        openMusicFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.println(Log.ASSERT, "Change fragment", "Open music fragment ");
                openFragment(musicFragment);
            }
        });
        openAsistentFragmentButton = findViewById(R.id.open_asistent);
        openAsistentFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.println(Log.ASSERT, "Change fragment", "Open asistent fragment");
                openFragment(asistentFragment);
            }
        });
        openPCFragment = findViewById(R.id.open_pc);
        openPCFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.println(Log.ASSERT, "Change fragment", "Open pc fragment");
                openFragment(PC_FRAGMENT);
            }
        });
        try {
            saveColors();
        } catch (IOException e) {
            e.printStackTrace();
        }

        openArgb = findViewById(R.id.open_argb);
        openArgb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(argbFragment);
            }
        });

        openOnConnectAutomation = findViewById(R.id.open_on_connect_automation);
        openOnConnectAutomation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(onConnectionAutomationFragment);
            }
        });
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        ascultator = new Ascultator(this);
        traducator = new Traducator();
        sr.setRecognitionListener(ascultator);

        asistentIcon = findViewById(R.id.icon_asistent);
        continutAsistent = findViewById(R.id.continut_asistent);
        limbaAsistent = findViewById(R.id.limba_asistent);

        asistent = findViewById(R.id.asistent);
        asistent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenEvent();
            }
        });
        //daca da eroare la initializarea vizualizatorului, uncomment asta
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1234);
        }

        try {
            ARGBProfile lightOnProfile = ARGBProfile.load(Argb_customizeLightOnLightFragment.PROFILE_NAME);
            argbCustomizeLightOnLightFragment.setTip(lightOnProfile.tip);
            argbCustomizeLightOnLightFragment.setIncrement(lightOnProfile.increment_or_decrement);
            argbCustomizeLightOnLightFragment.setSectiuni(lightOnProfile.sectiuni);
            argbCustomizeLightOnLightFragment.setViteza(lightOnProfile.viteza);
            Log.println(Log.ASSERT, "Am aplicat", "setarile la lightOn");
        } catch (Exception ex) {
            Log.println(Log.ASSERT, "Eroare", "Nu am putut aplica setarile pt lightOnProfile, vezi in debug");
            ex.printStackTrace();
        }

        try {
            ARGBProfile lightOffProfile = ARGBProfile.load(Argb_customizeLightOffFragment.PROFILE_NAME);
            argbCustomizeLightOffFragment.setTip(lightOffProfile.tip);
            argbCustomizeLightOffFragment.setDecrement(lightOffProfile.increment_or_decrement);
            argbCustomizeLightOffFragment.setSectiuni(lightOffProfile.sectiuni);
            argbCustomizeLightOffFragment.setViteza(lightOffProfile.viteza);
            Log.println(Log.ASSERT, "Am aplicat", "setarile la lightoff");
        } catch (Exception ex) {
            Log.println(Log.ASSERT, "Eroare", "Nu am putut aplica setarile pt lightOffProfile, vezi in debug");
            ex.printStackTrace();
        }

        try {
            ARGBProfile customColorProfile = ARGBProfile.load(Argb_customizeColorFragment.PROFILE_NAME);
            argbCustomizeColorFragment.setTip(customColorProfile.tip);
            argbCustomizeColorFragment.setIncrement(customColorProfile.increment_or_decrement);
            argbCustomizeColorFragment.setSectiuni(customColorProfile.sectiuni);
            argbCustomizeColorFragment.setViteza(customColorProfile.viteza);
            argbCustomizeColorFragment.setColor(customColorProfile.color);
            Log.println(Log.ASSERT, "Am aplicat", "setarile la customColor");
        } catch (Exception ex) {
            Log.println(Log.ASSERT, "Eroare", "Nu am putut aplica setarile pt customColorProfile, vezi in debug");
            ex.printStackTrace();
        }

        try {
            ARGBRomanianFlagProfile romanianFlag =
                    (ARGBRomanianFlagProfile) ARGBProfile.load(Argb_customizeRomanianFlagFragment.PROFILE_NAME);
            argbCustomizeRomanianFlagFragment.vitezaSteaguri = romanianFlag.vitezaAnimatie;
            argbCustomizeRomanianFlagFragment.orientare = romanianFlag.orientare;
            argbCustomizeRomanianFlagFragment.nrSteaguri = romanianFlag.nrSteaguri;
            argbCustomizeRomanianFlagFragment.intensitate = romanianFlag.intensitate;
            argbCustomizeRomanianFlagFragment.directie = romanianFlag.directie;
            argbCustomizeRomanianFlagFragment.latimeSteaguri = romanianFlag.latimeSteaguri;
            Log.println(Log.ASSERT, "Am aplicat", "datele pentru romanianflag");
        } catch (Exception ex) {
            Log.println(Log.ASSERT, "eroare", "nu am putut incarca fisierul pentru romanianflag, vezi in debug");
            ex.printStackTrace();
        }

        try {
            ARGBFastLedProfile fastLedProfile =
                    (ARGBFastLedProfile) ARGBProfile.load(Argb_customizeFastLedFragment.PROFILE_NAME);
            argbCustomizeFastLedFragment.viteza = fastLedProfile.viteza;
            argbCustomizeFastLedFragment.intensitate = fastLedProfile.intensitate;
            argbCustomizeFastLedFragment.tip = fastLedProfile.tip;
            Log.println(Log.ASSERT, "Am aplicat", "datele pentru fastled");
        } catch (Exception ex) {
            Log.println(Log.ASSERT, "eroare", "Nu am putut incarca datele pentru fastled, vezi in debug");
            ex.printStackTrace();
        }

        //</editor-fold>

        if (App.NIGHT_MODE_ENABLED) {
            nightModePreset();
            asistentIcon.setImageResource(R.drawable.ic_pepeicon_night);
            continutAsistent.setTextColor(App.NIGHT_MODE_TEXT);
            limbaAsistent.setTextColor(App.NIGHT_MODE_TEXT);
        }

        //showConnections();
    }

    /**
     * Fortam sa apara asistentul doar cand utilizatorul se uita la home
     *
     * @param visible Vizibil sau nu
     */
    void setAsistentVisibility(boolean visible) {
        //<editor-fold desc="body">
        if (visible) {
            if (choosenFragment == null) {
                continutAsistent.setVisibility(View.VISIBLE);
                limbaAsistent.setVisibility(View.VISIBLE);
                asistentIcon.setVisibility(View.VISIBLE);
                List<String> x = AsistentFragment.loadAsistent(this);
                int limba = Integer.valueOf(x.get(0));
                if (limba == App.LIMBA_ENGLEZA) {
                    limbaAsistent.setText(App.PEPE_ENGLEZ);
                } else {
                    limbaAsistent.setText(App.PEPE_ROMAN);
                }
            }
        } else {
            continutAsistent.setVisibility(View.INVISIBLE);
            limbaAsistent.setVisibility(View.INVISIBLE);
            asistentIcon.setVisibility(View.INVISIBLE);
        }
        //</editor-fold>
    }


    int counter = 0;

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Log.println(Log.ASSERT, "m a restartat", "of");
        super.onRestart();
    }

    protected void onResume() {
        Log.println(Log.ASSERT, "resume", "aa");
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.println(Log.ASSERT, "m a pornit", "ura" + (counter++));
    }

    @Override
    protected void onDestroy() {
        Log.println(Log.ASSERT, "destroy", "a distrus app");
        notificationManager.cancel(App.NOTIFICATION_MANAGER_ID);
        super.onDestroy();
    }
}


//<editor-fold desc="dump">
/*
    void connectToPC() {
        //out.append("\n...In onResume...\n...Attempting client connect...");
        Log.println(Log.ASSERT, "CONNECT", "entered");
        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = blueAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            pcSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            // Toast.makeText("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
            Log.println(Log.ASSERT, "anus", "BAI MARE1");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        blueAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        try {
            pcSocket.connect();
            Log.println(Log.ASSERT, "Con", "...Connection established and data link opened...");
        } catch (IOException e) {
            try {
                pcSocket.close();
            } catch (IOException e2) {
                Log.println(Log.ASSERT, "anus", "BAI MARE2");
                //AlertBox("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        //out.append("\n...Sending message to server...");
        Log.println(Log.ASSERT, "SEND", "trimitem masaj");
        try {
            pcOutputStream = pcSocket.getOutputStream();
            pcInputStream = pcSocket.getInputStream();
        } catch (IOException e) {
            Log.println(Log.ASSERT, "anus", "BAI MARE3");
            //AlertBox("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }

        String message = "Hello from Android.\n";
        byte[] msgBuffer = message.getBytes();
        try {
            pcOutputStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
            msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            Log.println(Log.ASSERT, "anus", msg);
            //AlertBox("Fatal Error", msg);
        }
        byte data[] = new byte[32];
        int length = 0;
        try {
            length = pcInputStream.read(data);

            String readMessage = new String(data, 0, data.length);
            //out.append("auzi ba, am primit masaj"+readMessage);
            Log.println(Log.ASSERT, "mesaj", readMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void CheckBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on

        // Emulator doesn't support Bluetooth and will return null
        if (blueAdapter == null) {

            // AlertBox("Fatal Error", "Bluetooth Not supported. Aborting.");
        } else {
            if (blueAdapter.isEnabled()) {
                Log.println(Log.ASSERT, "BTSTATE", "on");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(blueAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }
    */

                 /*
        AlarmManager am=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        test.main=this;
        Intent in=new Intent(this,test.class);

        PendingIntent pin=PendingIntent.getBroadcast(this,5,in,0);

        am.setExact(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+5000,pin);
                */
      /*  final Fragment blankFragment=new BlankFragment();
        final Fragment settingsFragment=new Settings();*/


//isBlowing();

        /*new Thread() {
            @Override
            public void run() {
                play();
                Visualizer visualizer = new Visualizer(player.getAudioSessionId());
                visualizer.setCaptureSize(128);
                visualizer.setEnabled(true);
                byte data[] = new byte[visualizer.getCaptureSize()];
                while(true){

                    visualizer.getFft(data);
                    String whole="";
                    for (byte b:data){
                        whole+=b+",";
                    }
                    Log.println(Log.ASSERT,"FFT",whole);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();*/



      /* new Thread(){
           public void run(){
               play();
               Visualizer v=new Visualizer(player.getAudioSessionId());
               v.setCaptureSize(512);
               v.setEnabled(true);

               byte data[]=new byte[v.getCaptureSize()];
               while(true){
                   try {
                       v.getFft(data);
                   }catch(Exception ex){

                   }
            /*String x="";
            for(byte b:data){
                x+=b+" ";
            }
            Log.println(Log.ASSERT,"BYTE DATA",x);

                   int n = data.length;
                   float[] magnitudes = new float[n / 2 + 1];
                   float[] phases = new float[n / 2 + 1];
                   magnitudes[0] = (float)Math.abs(data[0]);      // DC
                   magnitudes[n / 2] = (float)Math.abs(data[1]);  // Nyquist
                   phases[0] = phases[n / 2] = 0;
                   for (int k = 1; k < n / 2; k++) {
                       int i = k * 2;
                       magnitudes[k] = (float)Math.hypot(data[i], data[i + 1]);
                       phases[k] = (float)Math.atan2(data[i + 1], data[i]);
                   }
                   double s=0;
                   double min=99999;
                   double max=-99999;
                   for (float f:magnitudes){
                       s+=f;
                       if (f>max)
                           max=f;
                       if (f<min)
                           min=f;
                   }
                   s/=(n/2+1);
                   int val=map(s,min,max,0,255);
                    try{
                        outputStream.write((val+" 0 0\n").getBytes());
                    }catch(Exception ex){

                    }
                   Log.println(Log.ASSERT,"val",val+" ");
                   try {
                       Thread.sleep(40);
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
               }
           }
       }.start();*/
/*

    public void play() {
        if (player == null) {
            player = MediaPlayer.create(this, R.raw.bb);
        }
        player.start();
    }


    public boolean isBlowing() {
        new Thread() {
            @Override
            public void run() {
                boolean recorder = true;

                int minSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
                AudioRecord ar = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize);


                short[] buffer = new short[minSize];

                ar.startRecording();
                while (recorder) {

                    ar.read(buffer, 0, minSize);
                    int sum = 0;
                    int max = -999;
                    for (short s : buffer) {


                        Log.println(Log.ASSERT, "VAL", s + "");

                    }


                }

            }
        }.start();

        return false;
    }
*/
//</editor-fold>