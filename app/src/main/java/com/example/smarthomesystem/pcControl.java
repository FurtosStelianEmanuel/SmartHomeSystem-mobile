package com.example.smarthomesystem;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
import android.text.InputType;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.intellij.lang.annotations.RegExp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


/**
 * Interfata pentru sectiunea 'Control PC'
 *
 * @author Manel
 * @since 06/02/2020
 */
public class pcControl extends Fragment {

    //<editor-fold desc="Variabile">
    ImageView connectImageView;
    MainActivity main;
    ArrayAdapter<String> arrayAdapter;
    ArrayAdapter<String> internetConnArrayAdapter;
    List<String> nume;
    AlertDialog.Builder builderSingle;
    AlertDialog.Builder builderConnectingDialog;
    AlertDialog.Builder internetConnectionsBuilder;
    AlertDialog internetConnectionsAlertDialog;
    ImageView touchPad;
    ImageView leftButton;
    ImageView rightButton;
    ImageView midButton;
    int buttonPressColor = 0xe0f47521;
    String CURSOR_TYPE = App.TOUCH_SCREEN_CURSOR_TYPE;
    public FloatingActionButton asistent;
    private BluetoothSocket pcSocket = null;
    public OutputStream pcOutputStream = null;
    public BufferedOutputStream pcBufferedOutputStream = null;
    public int SCROLL_INTERVAL = 6;

    private String ping_response;
    static boolean receivedError = false;

    String SOUTHPARK_LINK = "https://www.southparkstudios.com/random-episode";
    String YOUTUBE_LINK = "https://www.youtube.com";
    String GOOGLE_LINK = "https://www.google.com";
    String GOOGLE_SEARCH = "https://www.google.com/search?q=";
    InputStream pcInputStream = null;
    Client client;
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static String address = "00:1A:7D:DA:71:15";

    FloatingActionButton keyboardFloatingActionButton;
    FloatingActionButton movieFloatingActionButton, pcOperationsFloatingActionButton;
    ConstraintLayout keyboard;
    ConstraintLayout movie, pcControls;

    Button play_pause, skip_left, skip_right, vol_up, vol_down, mute, fullscreen, aspect_ratio, shutdown, winTab,
            closeApp, openSouthPark, openChrome, closeChrome, openYoutube;
    SeekBar volumTeatru;
    Button esc, b1, b2, b3, b4, b5, b6, b7, b8, b9, b0, backspace, tab, q, w, e, r, t, y, u, i, o, p, caps, a, s, d, f, g, h, j, k, l,
            shift, z, x, c, vButton, b, n, m, enter, space, ctrl, win, alt;

    AlertDialog connectedAlertDialog;
    View selectedLayout;
    Timer IP_keepAliveTimer = new Timer();
    boolean autoConnect = false;
    Thread pingSender;
    boolean runPinger = false;
    boolean shouldStartPinger = true;
    AlertDialog dialogConectare;
    int TOUCHPAD_SENSITIVITY = 3;
    private int SIGNUM_SENSITIVITY = 2;
    //</editor-fold>

    public pcControl() {

    }


    /**
     * Functie folosita pentru a trimite date la PC
     * Prima data trebuie realizata conexiunea la PC cu ajutorul {@link #connectIP(String)} sau
     * {@link #connectToBluetooth(String)} sau {@link #connectToBluetooth(int)}
     *
     * @param data
     * @throws IOException antetu spune ca functia arunca IOException, dar de fapt nu mai arunca pentru ca
     *                     trimiterea se face intr-un alt thread. Scoate cand ai timp throws IOException si toate catch-urile
     *                     legate de el
     */
    void write(final byte[] data) throws IOException {
        //<editor-fold desc="body">
        if (App.CONNECTION_TYPE == App.ANDROID_MASTER) {
            new Thread() {
                Handler mHandler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message message) {
                        Toast.makeText(main, "Conexiune pierduta...  te rog reconecteaza te", Toast.LENGTH_SHORT)
                                .show();
                    }
                };

                public void run() {
                    try {
                        pcOutputStream.write(data);

                    } catch (IOException | NullPointerException ex) {
                        Log.println(Log.ASSERT, "BA", "n am la ce sa trimit");
                        if (!receivedError) {
                            Log.println(Log.ASSERT, "eroare la scriere",
                                    "Nu am putut trimite date, te rog conecteaza te din nou si verifica " +
                                            "serverul java");
                            //<editor-fold desc="doar pentru toast message">
                            Message m = mHandler.obtainMessage(Message.CONTENTS_FILE_DESCRIPTOR, new Message());
                            m.sendToTarget();
                            //</editor-fold>
                            receivedError = true;
                        }
                    }
                }
            }.start();
        } else if (App.CONNECTION_TYPE == App.PC_MASTER) {
            main.write(data);

        }
        //</editor-fold>
    }

    /**
     * Se conecteaza la adresa specificata ex ( 1A:53:F5:26:53)
     * Dupa ce conexiunea este realizata se porneste un nou thread care asteapta un
     * ping de confirmare de la server, acest thread este activ 10 secunde
     * iar daca in acele 10 secunde a primit ping-ul, face sa dispara dialogul
     * "Conexiune realizata"
     *
     * @param address forma 1A:53:F5:26:53
     * @throws IOException Daca conexiunea nu se poate realiza
     */
    void connectToBluetooth(String address) throws IOException {
        //<editor-fold desc="body">
        Log.println(Log.ASSERT, "CONNECT", "connectToBluetooth");

        BluetoothDevice device = main.blueAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        pcSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        main.blueAdapter.cancelDiscovery();
        // Establish the connection.  This will block until it connects.
        try {
            pcSocket.connect();
            saveconnectionTypeAndAddress(App.PC_CONNECTION_BLUETOOTH, address);
            Log.println(Log.ASSERT, "Con", "...Connection established and data link opened...");
        } catch (IOException e) {
            pcSocket.close();
            throw new IOException("Conexiune esuata");
        }
        pcOutputStream = pcSocket.getOutputStream();
        pcInputStream = pcSocket.getInputStream();


        pcBufferedOutputStream = new BufferedOutputStream(pcOutputStream);
        Log.println(Log.ASSERT, "BLUETOOTH-con", "Se asteapta ping-ul de confirmare a conexiunii");
        final Handler ugabuga = new Handler(Looper.getMainLooper()) {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void handleMessage(Message message) {
                try {
                    connectedAlertDialog.dismiss();
                } catch (NullPointerException ex) {
                    Log.println(Log.ASSERT, "eroare in ugabuga", "...");
                }

                Log.println(Log.ASSERT, "Ne am conectat ", "resetam receivedError=false");

            }
        };
        final Thread thread = new Thread() {


            public void run() {
                byte data[] = new byte[32];
                try {
                    int length = pcInputStream.read(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ping_response = new String(data, 0, data.length);
                Log.println(Log.ASSERT, "DATA-RECEIVE", "Ping primit " + ping_response);
                if (ping_response.contains(App.CONFIRMATION_PING)) {
                    Log.println(Log.ASSERT, "DATA-RECEIVE", "Ping match");

                    Message m = ugabuga.obtainMessage(Message.CONTENTS_FILE_DESCRIPTOR, new Message());
                    m.sendToTarget();
                }
                userConnectedSuccessfully();
            }
        };
        thread.start();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                thread.interrupt();
                if (ping_response != null) {
                    if (ping_response.contains(App.CONFIRMATION_PING)) {
                        Log.println(Log.ASSERT, "DATA-RECEIVE", "Ping match");
                        Message m = ugabuga.obtainMessage(Message.CONTENTS_FILE_DESCRIPTOR, new Message());
                        if (!runPinger) {
                            startPinger();
                        }
                        m.sendToTarget();
                    }
                }

            }
        }, 10000);
        try {
            main.pcReader.set(pcInputStream);
        } catch (Exception ex) {
            Log.println(Log.ASSERT, "cica ex la ", "pcReader.set");
            ex.printStackTrace();
        }
        startPinger();
        //</editor-fold>
    }

    /**
     * Polimorfism cu {@link #connectToBluetooth(String)}, functie la indemana cand utilizatorul
     * selecteaza adresa dintr-un array adapter
     *
     * @param which Index-ul adresei selectate de utilizator in ArrayAdapter
     * @throws IOException inherit de la {@link #connectToBluetooth(String)}
     */
    void connectToBluetooth(int which) throws IOException {
        //<editor-fold desc="body">
        String address = arrayAdapter.getItem(which);
        address = (address.contains(nume.get(which)) ? address.replace(" " + nume.get(which), "") : address);
        connectToBluetooth(address);
        //</editor-fold>
    }


    void sendDoorInformation() throws IOException, ClassNotFoundException {
        Log.println(Log.ASSERT, "SENDDOORINFO", "am fost chemat");
        String changes = App.COMMAND_PC_APPLY + "" + App.DOOR_COMMAND;
        List<String> date = main.getDoorEvents();
        String pornireAutomata = Boolean.valueOf(date.get(0)) ? "1" : "0";
        String oprireAutomata = Boolean.valueOf(date.get(1)) ? "1" : "0";
        String aplicareAutomata = Boolean.valueOf(date.get(2)) ? "1" : "0";
        int culoare = Integer.valueOf(date.get(3));
        changes += pornireAutomata;
        changes += oprireAutomata;
        changes += aplicareAutomata;
        changes += " ";
        changes += culoare;
        changes += "\n";
        //pcOutputStream.write(changes.getBytes());
        write(changes.getBytes());
    }

    void pcSettingsSender() {
        try {
            write((App.SWITCH_CONNECTION_TYPE + "" + App.ANDROID_MASTER).getBytes());
            App.CONNECTION_TYPE = App.ANDROID_MASTER;
            Log.println(Log.ASSERT, "Trimis", "Am trimis change connection type");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            sendDoorInformation();
        } catch (IOException ex) {
            Log.println(Log.ASSERT, "ERoare", ex.toString());
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            Log.println(Log.ASSERT, "ERoare", ex.toString());
        }

        /*Log.println(Log.ASSERT,"Apelare","connectionApplier");
        try {
            pcOutputStream.write("w2d4fg\n".getBytes());
            BufferedReader reader=new BufferedReader(new InputStreamReader(pcInputStream));
            Log.println(Log.ASSERT,"raspuns",reader.readLine());

        } catch (IOException ex) {
            ex.printStackTrace();
        }*/
    }

    /**
     * Functie apelata de fiecare data cand se realizeaza o conexiune buna
     * de aici se apeleaza functia ce comunica spre pc setarile SmartHomeSystem
     */
    void userConnectedSuccessfully() {
        //<editor-fold desc="body">
        try {
            AlertDialog.Builder succes = new AlertDialog.Builder(main);
            succes.setTitle("Conexiune realizata");
            succes.setMessage("Se asteapta confirmarea de la PC . . .");
            succes.setIcon(R.drawable.ic_pc);
            connectedAlertDialog = succes.show();
        } catch (Exception ex) {
            Log.println(Log.ASSERT, "Eroare", "la dialog boxes");
        }
        receivedError = false;
        Log.println(Log.ASSERT, "Conexiune", "Realizata");
        pcSettingsSender();
        //</editor-fold>
    }


    /**
     * Folosita pentru a schimba meniul afisat
     *
     * @param l ce meniu vrei sa apara de ex {@link #movie}
     */
    void selectLayout(View l) {
        //<editor-fold desc="body">
        if (l != null) {
            movie.setVisibility(View.INVISIBLE);
            keyboard.setVisibility(View.INVISIBLE);
            pcControls.setVisibility(View.INVISIBLE);
            selectedLayout = l;
            selectedLayout.setVisibility(View.VISIBLE);
        } else {
            movie.setVisibility(View.INVISIBLE);
            keyboard.setVisibility(View.INVISIBLE);
            pcControls.setVisibility(View.INVISIBLE);
        }
        //</editor-fold>
    }

    /**
     * @param connectionType {@link App#PC_CONNECTION_BLUETOOTH sau App#PC_CONNECTION_IP}
     * @param address        fie adresa MAC a modulului bluetooth daca connectionType este App#PC_CONNECTION_BLUETOOTH
     *                       fie adresa IP a calculatorului daca connectionType este App#PC_CONNECTION_IP
     */
    void saveconnectionTypeAndAddress(String connectionType, String address) throws IOException {
        //<editor-fold desc="body">
        FileOutputStream fos = main.openFileOutput("autoConnectPC.svt", Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        List<String> xdate = new ArrayList<>();
        xdate.add(connectionType);
        xdate.add(address);
        os.writeObject(xdate);
        os.close();
        fos.close();
        Log.println(Log.ASSERT, main.getFilesDir().toString(), "Am salvat fisierul autoConnectPC.svt cu" +
                " " + xdate.get(0) + " " + xdate.get(1));
        //</editor-fold>
    }

    /**
     * Incarca fisierul autoConnectPC.svt
     *
     * @return ArrayList care teoretic contine in primul element tipul conexiunii si in al doilea element
     * adresa
     * @throws IOException
     * @throws ClassNotFoundException
     */
    List<String> loadConnectionTypeAndAddress() throws IOException, ClassNotFoundException {
        //<editor-fold desc="body">
        FileInputStream fis = main.openFileInput("autoConnectPC.svt");
        ObjectInputStream is = new ObjectInputStream(fis);
        List<String> simpleClass = (ArrayList<String>) is.readObject();
        is.close();
        fis.close();
        return simpleClass;
        //</editor-fold>
    }

    /**
     * Se uita in fisierul autoConnectPC.svt si incearca sa se conecteze in functie de ce e scris acolo
     */
    void connect() throws IOException, ClassNotFoundException {
        //<editor-fold desc="body">
        List<String> date = loadConnectionTypeAndAddress();
        Log.println(Log.ASSERT, "connect", "a fost apelata conectarea automata");
        if (date.get(0).equals(App.PC_CONNECTION_BLUETOOTH)) {
            Log.println(Log.ASSERT, "connect", "Se incearca conectarea prin bluetooth");
            connectToBluetooth(date.get(1));
        } else if (date.get(0).equals(App.PC_CONNECTION_IP)) {
            Log.println(Log.ASSERT, "connect", "Se incearca conectarea prin IP");
            connectIP(date.get(1));
        }
        //</editor-fold>
    }

    /**
     * Functie folosita pentru a da clear fisierului ips.svt
     * salvand un arraylist gol in fisier
     *
     * @throws IOException
     */
    void saveIP() throws IOException {
        //<editor-fold desc="body">
        FileOutputStream fos = main.openFileOutput("ips.svt", Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        List<String> xdate = new ArrayList<>();
        os.writeObject(xdate);
        os.close();
        fos.close();
        Log.println(Log.ASSERT, main.getFilesDir().toString(), "Am salvat fisierul ips.svt");
        //</editor-fold>
    }

    /**
     * Adauga un IP nou in libraria de IP-uri
     * Fisier folosit : ips.svt
     *
     * @param ip
     * @throws IOException
     */
    void saveIP(String ip) throws IOException {
        //<editor-fold desc="body" defaultstate="collapsed">
        List<String> xdata = new ArrayList<>();
        try {
            List<String> data = loadIP();
            xdata.addAll(data);
        } catch (ClassNotFoundException ex) {
            Log.println(Log.ASSERT, "eroare", "cacat123");
        }
        FileOutputStream fos = main.openFileOutput("ips.svt", Context.MODE_PRIVATE);

        ObjectOutputStream os = new ObjectOutputStream(fos);
        xdata.add(ip);
        for (String st : xdata) {
            Log.println(Log.ASSERT, "DATE DIN X", st);
        }
        os.writeObject(xdata);
        os.close();
        fos.close();
        Log.println(Log.ASSERT, main.getFilesDir().toString(), "Am salvat fisierul ips.svt");
        //</editor-fold>
    }

    /**
     * @return ArrayList care contine adresele IP salvate in ips.svt
     * @throws IOException
     * @throws ClassNotFoundException
     */
    List<String> loadIP() throws IOException, ClassNotFoundException {
        //<editor-fold desc="body">
        FileInputStream fis = main.openFileInput("ips.svt");
        ObjectInputStream is = new ObjectInputStream(fis);
        List<String> simpleClass = (ArrayList<String>) is.readObject();
        is.close();
        fis.close();
        return simpleClass;
        //</editor-fold>
    }

    /**
     * Se conecteaza la IP-ul specificat daca serverul java ruleaza pe PC
     * este folosit portul 153
     *
     * @param ip ex 192.168.1.3
     */
    void connectIP(final String ip) {
        //<editor-fold desc="body">
        //Log.println(Log.ASSERT,"vrea conectare","la "+ip);
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void run() {
                try {
                    client = new Client(ip.replace("/", ""), 153);
                    Log.println(Log.ASSERT, "Conn", "ne am conectat la pc");
                    pcOutputStream = client.out;
                    pcInputStream = client.in;
                    IP_keepAliveTimer = new Timer();
                    IP_keepAliveTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                //pcOutputStream.write((App.COMMAND_IP_ALIVE + "\n").getBytes());
                                write((App.COMMAND_IP_ALIVE + "\n").getBytes());

                                //FIX PORNIRE PINGER LA PRIMA CONECTARE
                                startPinger();
                                main.pcReader.set(pcInputStream);

                                receivedError = false;
                                saveconnectionTypeAndAddress(App.PC_CONNECTION_IP, ip);
                                Thread.sleep(25);
                                userConnectedSuccessfully();
                                //Log.println(Log.ASSERT,"PING-SENT","Am trimis ping spre IP "+
                                //      ip);
                            } catch (Exception ex) {
                                Log.println(Log.ASSERT, "Eroare5", "nU AM putut trimite ping-ul de mentinere");
                                IP_keepAliveTimer.cancel();
                            }
                        }
                    }, 0, 1000 * 60);
                } catch (IOException ex) {
                    Log.println(Log.ASSERT, "err", ex.toString());
                }
            }
        }.start();
        //</editor-fold>
    }

    public pcControl(MainActivity main) {
        //<editor-fold desc="body">
        this.main = main;
        arrayAdapter = new ArrayAdapter<>(main, android.R.layout.select_dialog_singlechoice);
        nume = new ArrayList<>();
        builderSingle = new AlertDialog.Builder(main);
        builderSingle.setIcon(R.drawable.ic_pc);
        builderSingle.setTitle("Cauta PC-ul la care vrei sa te conectezi");
        builderSingle.setNegativeButton("Conexiune prin internet", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogConectare.dismiss();
                internetConnectionsBuilder = new AlertDialog.Builder(pcControl.this.main);
                internetConnectionsBuilder.setTitle("Selecteaza IP-ul cu care vrei sa te conectezi");
                internetConnectionsBuilder.setIcon(R.drawable.ic_wifi_black_24dp);
                internetConnArrayAdapter = new ArrayAdapter<>(pcControl.this.main,
                        android.R.layout.select_dialog_singlechoice);
                try {
                    List<String> ipUri = loadIP();
                    internetConnArrayAdapter.addAll(ipUri);
                    Log.println(Log.ASSERT, "AM ADAUGAT ", internetConnArrayAdapter.getCount() + " ");
                    for (int i = 0; i < internetConnArrayAdapter.getCount(); i++) {
                        Log.println(Log.ASSERT, "IP VECHI", internetConnArrayAdapter.getItem(i));
                    }
                } catch (IOException ex) {
                    Log.println(Log.ASSERT, "eroare1", ex.toString());
                } catch (ClassNotFoundException ex) {
                    Log.println(Log.ASSERT, "eroare2", ex.toString());
                }


                internetConnectionsBuilder.setAdapter(internetConnArrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        connectIP(internetConnArrayAdapter.getItem(which));
                        Log.println(Log.ASSERT, "ebun", "s a conectat");
                    }
                });
                internetConnectionsBuilder.setNegativeButton("Curata istoric", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            saveIP();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                internetConnectionsBuilder.setPositiveButton("Adauga", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(pcControl.this.main);
                        builder.setTitle("IP");

                        final EditText input = new EditText(pcControl.this.main);
                        input.setInputType(InputType.TYPE_CLASS_PHONE);
                        builder.setView(input);

                        builder.setPositiveButton("Adauga", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    saveIP(input.getText().toString());
                                } catch (IOException ex) {
                                    Log.println(Log.ASSERT, "BAG pl ", "nu am putut salva" + ex.toString());
                                    try {
                                        saveIP();
                                        saveIP(input.getText().toString());
                                    } catch (IOException ex1) {
                                        Log.println(Log.ASSERT, "eroare4", "ceva e bai naspa" + ex1.toString());
                                    }
                                }
                            }
                        });


                        builder.show();
                    }
                });
                internetConnectionsAlertDialog = internetConnectionsBuilder.show();
            }
        });
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            Handler mHandler = new Handler(Looper.getMainLooper()) {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void handleMessage(Message message) {
                    switch (message.arg1) {
                        case 1:
                            builderConnectingDialog.setTitle("Conexiune esuata ");
                            builderConnectingDialog.setMessage(message.obj + "\nSigur e pornita aplicatia pentru desktop?");
                            builderConnectingDialog.show();
                            break;
                        case 2:
                            AlertDialog d = (AlertDialog) message.obj;
                            userConnectedSuccessfully();
                            d.dismiss();
                            break;
                    }

                }
            };

            @Override
            public void onClick(DialogInterface dialog, final int which) {
                builderConnectingDialog.setTitle("Conectare . . . ");
                builderConnectingDialog.setMessage(arrayAdapter.getItem(which));
                final AlertDialog show = builderConnectingDialog.show();
                new Thread() {
                    public void run() {
                        try {
                            connectToBluetooth(which);
                            Message message = mHandler.obtainMessage(Message.CONTENTS_FILE_DESCRIPTOR, new Message());
                            message.obj = show;
                            message.arg1 = 2;
                            message.sendToTarget();
                        } catch (IOException e) {
                            Message message = mHandler.obtainMessage(Message.CONTENTS_FILE_DESCRIPTOR, new Message());
                            show.dismiss();
                            message.arg1 = 1;
                            message.obj = arrayAdapter.getItem(which);
                            message.sendToTarget();

                        }
                    }
                }.start();
            }
        });


        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            arrayAdapter.add(device.getAddress() + " " + device.getName());
            nume.add(device.getName());
        }
        builderConnectingDialog = new AlertDialog.Builder(main);

        try {
            List<String> date = loadPC();
            CURSOR_TYPE = date.get(0);
            autoConnect = Boolean.valueOf(date.get(1));
        } catch (Exception ex) {
            Log.println(Log.ASSERT, "eroare", "la citirea setarilor pc");
        }
        //</editor-fold>
    }

    List<String> loadPC() throws IOException, ClassNotFoundException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileInputStream fis = main.openFileInput("pcSettings.svt");
        ObjectInputStream is = new ObjectInputStream(fis);
        List<String> simpleClass = (ArrayList<String>) is.readObject();
        is.close();
        fis.close();
        return simpleClass;
        //</editor-fold>
    }

    void showConnectionMenu() {
        dialogConectare = builderSingle.show();
    }

    void print(String text) {
        Log.println(Log.ASSERT, "key", text);
    }

    /**
     * folosita pentru a impacheta comenzi inainte de a trimite la PC
     *
     * @param c1 de exemplu {@link App#COMMAND_KEY_PRESS}
     * @param c2 de exemplu {@link App#KEY_DEL}
     * @return byte[] a concatenarii celor doua variabile
     */
    byte[] pack(char c1, char c2) {
        return (Character.toString(c1) + Character.toString(c2) + "\n").getBytes();
    }

    /**
     * O foloseam ca sa setez efecte la apasarea butoanelor
     *
     * @param button
     */
    @Deprecated
    public static void buttonEffect(View button) {
        //<editor-fold desc="body">
        button.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.getBackground().clearColorFilter();
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
        //</editor-fold>
    }

    public void nightModePreset(View v) {
        v.findViewById(R.id.fullscreen).setBackgroundResource(R.drawable.ic_fullscreen_black_night_24dp);
        v.findViewById(R.id.vol_up).setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_night_24dp);
        v.findViewById(R.id.vol_down).setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_night_24dp);
        v.findViewById(R.id.skip_right).setBackgroundResource(R.drawable.ic_chevron_right_black_night_24dp);
        v.findViewById(R.id.skip_left).setBackgroundResource(R.drawable.ic_chevron_left_black_night_24dp);
        v.findViewById(R.id.mute).setBackgroundResource(R.drawable.ic_volume_mute_black_night_24dp);
        v.findViewById(R.id.close_app).setBackgroundResource(R.drawable.ic_close_black_night_24dp);
        v.findViewById(R.id.play_pause).setBackgroundResource(R.drawable.ic_pause_black_night_24dp);
        v.findViewById(R.id.aspect_ratio).setBackgroundResource(R.drawable.ic_aspect_ratio_black_night_24dp);
        v.findViewById(R.id.win_tab).setBackgroundResource(R.drawable.ic_tab_black_night_24dp);
        ((FloatingActionButton) (v.findViewById(R.id.keyboard_floatingbutton))).setBackgroundTintList(ColorStateList.valueOf(App.NIGHT_MODE_BUTTON_BACKGROUND));
        ((FloatingActionButton) (v.findViewById(R.id.movie_floatingbutton))).setBackgroundTintList(ColorStateList.valueOf(App.NIGHT_MODE_BUTTON_BACKGROUND));
        ((FloatingActionButton) (v.findViewById(R.id.pc_operations_floatingbutton))).setBackgroundTintList(ColorStateList.valueOf(App.NIGHT_MODE_BUTTON_BACKGROUND));
        ((FloatingActionButton) (v.findViewById(R.id.asistent_pc))).setBackgroundTintList(ColorStateList.valueOf(App.NIGHT_MODE_BUTTON_BACKGROUND));
        ((ImageView) (v.findViewById(R.id.imageView4))).setImageResource(R.drawable.ic_pc_night);
        ((FloatingActionButton) (v.findViewById(R.id.keyboard_floatingbutton))).setImageResource(R.drawable.ic_keyboard_black_night_24dp);
        ((FloatingActionButton) (v.findViewById(R.id.movie_floatingbutton))).setImageResource(R.drawable.ic_movie_black_night_24dp);
        ((FloatingActionButton) (v.findViewById(R.id.pc_operations_floatingbutton))).setImageResource(R.drawable.ic_pc_night);
        ((FloatingActionButton) (v.findViewById(R.id.asistent_pc))).setImageResource(R.drawable.ic_mic_black_night_24dp);

        ((SeekBar)(v.findViewById(R.id.volum_teatru))).setThumb(getResources().getDrawable(R.drawable.ic_volume_down_black_night_24dp));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //<editor-fold desc="body">
        View v = inflater.inflate(R.layout.fragment_pc_control, container, false);

        connectImageView = v.findViewById(R.id.imageView4);
        connectImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConnectionMenu();
            }
        });
        connectImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.println(Log.ASSERT, "LONGCLICK", "open pc fragment");
                main.openFragment(main.pcSettings);
                return false;
            }
        });

        touchPad = v.findViewById(R.id.touchpad);

        touchPad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* sending = false;
                if (t != null)
                    t.cancel();
                Log.println(Log.ASSERT, "HARRAS", "M A ATINS");*/

            }
        });
        leftButton = v.findViewById(R.id.leftbutton);
        leftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.println(Log.ASSERT, "PRESS", "buton stanga apasat");
                    try {
                        write((App.COMMAND_MOUSE_PRESS + "\n").getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.println(Log.ASSERT, "DEPRESS", "buton stanga eliberat");
                    try {
                        write((App.COMMAND_MOUSE_RELEASE + "\n").getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });

        touchPad.setOnTouchListener(new View.OnTouchListener() {
            int x, y, px = -1, py = -1;
            double dx, dy;
            int degete = 0;
            int scrollCounter = 0;

            @Override
            public boolean onTouch(View v, final MotionEvent event) {


                if (event.getPointerCount() == 1) {
                    switch (CURSOR_TYPE) {
                        case App.TOUCH_SCREEN_CURSOR_TYPE:
                            x = (int) event.getAxisValue(MotionEvent.AXIS_X);
                            y = (int) event.getAxisValue(MotionEvent.AXIS_Y);

                            if (x != px || y != py) {
                                try {

                                    write
                                            ((App.COMMAND_MOUSE_MOVE_CLASSIC + " " +
                                                    x + " " +
                                                    y + "\n").getBytes());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            px = x;
                            py = y;
                            break;
                        case App.TOUCH_PAD_CURSOR_TYPE:
                            x = (int) event.getAxisValue(MotionEvent.AXIS_X);
                            y = (int) event.getAxisValue(MotionEvent.AXIS_Y);

                            dx = (Math.abs(x - px) >= SIGNUM_SENSITIVITY ? Math.signum(x - px) * TOUCHPAD_SENSITIVITY : 0);
                            dy = (Math.abs(y - py) >= SIGNUM_SENSITIVITY ? Math.signum(y - py) * TOUCHPAD_SENSITIVITY : 0);

                            try {
                                write((
                                                App.COMMAND_MOUSE_MOVE_TOUCH_PAD + " " +
                                                        (int) dx + " " +
                                                        (int) dy + "\n"
                                        ).getBytes()
                                );
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            px = x;
                            py = y;
                            break;
                    }
                } else {
                    if (scrollCounter % SCROLL_INTERVAL == 0) {
                        y = (int) event.getAxisValue(MotionEvent.AXIS_Y);
                        if (y < py) {
                            try {
                                write((App.COMMAND_MOUSE_WHEEL_UP + " -1\n").getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (y > py) {
                            try {
                                write((App.COMMAND_MOUSE_WHEEL_DOWN + " 1\n").getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        py = y;
                    }
                    scrollCounter++;
                }

                return true;
            }
        });

        midButton = v.findViewById(R.id.midbutton);
        midButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.println(Log.ASSERT, "PRESS", "buton centru apasat");
                    try {
                        write((App.COMMAND_MOUSE_WHEEL_PRESS + "\n").getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.println(Log.ASSERT, "DEPRESS", "buton centru eliberat");
                    try {
                        write((App.COMMAND_MOUSE_WHEEL_RELEASE + "\n").getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });

        rightButton = v.findViewById(R.id.rightbutton);
        rightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.println(Log.ASSERT, "PRESS", "buton dreapta apasat");
                    /*new Thread(){
                        public void run(){

                            client.sendRequest();
                        }
                    }.start();*/

                    try {
                        write((App.COMMAND_MOUSE_RIGHT_PRESS + "\n").getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.println(Log.ASSERT, "DEPRESS", "buton dreapta eliberat");
                    try {
                        write((App.COMMAND_MOUSE_RIGHT_RELEASE + "\n").getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });

        keyboard = v.findViewById(R.id.keyboard_layout);

        keyboardFloatingActionButton = v.findViewById(R.id.keyboard_floatingbutton);
        keyboardFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //keyboard.setVisibility((keyboard.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE));
                if (keyboard.getVisibility() == View.VISIBLE) {
                    selectLayout(null);
                } else {
                    selectLayout(keyboard);
                }
            }
        });

        movie = v.findViewById(R.id.movie_layout);
        play_pause = v.findViewById(R.id.play_pause);
        skip_left = v.findViewById(R.id.skip_left);
        skip_right = v.findViewById(R.id.skip_right);
        volumTeatru = v.findViewById(R.id.volum_teatru);

        volumTeatru.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    write((App.COMMAND_VOLUME + " " + progress + "\n").getBytes());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        skip_right.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        try {
                            write(pack(App.COMMAND_KEY_PRESS, App.KEY_RIGHT_ARROW));
                            v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        try {
                            write(pack(App.COMMAND_KEY_RELEASE, App.KEY_RIGHT_ARROW));
                            v.getBackground().clearColorFilter();
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                }
                return false;
            }
        });

        play_pause.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        try {
                            write(pack(App.COMMAND_KEY_PRESS, App.KEY_SPACE));
                            v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        try {
                            write(pack(App.COMMAND_KEY_RELEASE, App.KEY_SPACE));
                            v.getBackground().clearColorFilter();
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                }
                return false;
            }
        });

        skip_left.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        try {
                            write(pack(App.COMMAND_KEY_PRESS, App.KEY_LEFT_ARROW));
                            v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        try {
                            write(pack(App.COMMAND_KEY_RELEASE, App.KEY_LEFT_ARROW));
                            v.getBackground().clearColorFilter();
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                }
                return false;
            }
        });

        vol_down = v.findViewById(R.id.vol_down);
        vol_down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        try {
                            write(pack(App.COMMAND_KEY_PRESS, App.KEY_DOWN_ARROW));
                            v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        try {
                            write(pack(App.COMMAND_KEY_RELEASE, App.KEY_DOWN_ARROW));
                            v.getBackground().clearColorFilter();
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                }
                return false;
            }
        });
        vol_up = v.findViewById(R.id.vol_up);
        vol_up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        try {
                            write(pack(App.COMMAND_KEY_PRESS, App.KEY_UP_ARROW));
                            v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        try {
                            write(pack(App.COMMAND_KEY_RELEASE, App.KEY_UP_ARROW));
                            v.getBackground().clearColorFilter();
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                }
                return false;
            }
        });

        mute = v.findViewById(R.id.mute);
        mute.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        try {
                            write(pack(App.COMMAND_KEY_PRESS, App.KEY_M));
                            v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        try {
                            write(pack(App.COMMAND_KEY_RELEASE, App.KEY_M));
                            v.getBackground().clearColorFilter();
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                }
                return false;
            }
        });
        fullscreen = v.findViewById(R.id.fullscreen);
        fullscreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        try {
                            write(pack(App.COMMAND_KEY_PRESS, App.KEY_F));
                            v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        try {
                            write(pack(App.COMMAND_KEY_RELEASE, App.KEY_F));
                            v.getBackground().clearColorFilter();
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                }
                return false;
            }
        });
        aspect_ratio = v.findViewById(R.id.aspect_ratio);
        aspect_ratio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        try {
                            write(pack(App.COMMAND_KEY_PRESS, App.KEY_A));
                            v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        try {
                            write(pack(App.COMMAND_KEY_RELEASE, App.KEY_A));
                            v.getBackground().clearColorFilter();
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                }
                return false;
            }
        });

        movieFloatingActionButton = v.findViewById(R.id.movie_floatingbutton);
        movieFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (movie.getVisibility() == View.VISIBLE) {
                    selectLayout(null);
                } else {
                    selectLayout(movie);
                }
            }
        });
        pcControls = v.findViewById(R.id.pc_controls);
        pcOperationsFloatingActionButton = v.findViewById(R.id.pc_operations_floatingbutton);
        pcOperationsFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pcControls.getVisibility() == View.VISIBLE) {
                    selectLayout(null);
                } else {
                    selectLayout(pcControls);
                }
            }
        });

        shutdown = v.findViewById(R.id.shutdown_button);
        shutdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(main, AlarmFragment.getTimePickerDialogType())
                        .setTitle("Atenție !")
                        .setMessage("Sigur vrei sa stingi calculatorul?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                try {
                                    write((App.COMMAND_SHUTDOWN_PC + " " + App.SHUTDOWN_CODE + "\n").getBytes());
                                    Toast.makeText(main, "Comanda trimisa cu succes", Toast.LENGTH_SHORT).show();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("Nu", null).show();
            }
        });

        asistent = v.findViewById(R.id.asistent_pc);
        asistent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.listenEvent();
            }
        });

        winTab = v.findViewById(R.id.win_tab);
        winTab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        try {
                            write(pack(App.COMMAND_KEY_PRESS, App.KEY_WINDOWS));
                            Thread.sleep(5);
                            write(pack(App.COMMAND_KEY_PRESS, App.KEY_TAB));
                            v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        try {
                            write(pack(App.COMMAND_KEY_RELEASE, App.KEY_WINDOWS));
                            Thread.sleep(5);
                            write(pack(App.COMMAND_KEY_RELEASE, App.KEY_TAB));
                            v.getBackground().clearColorFilter();
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                }
                return false;
            }
        });

        closeApp = v.findViewById(R.id.close_app);
        closeApp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        try {
                            write(pack(App.COMMAND_KEY_PRESS, App.KEY_ALT));
                            Thread.sleep(5);
                            write(pack(App.COMMAND_KEY_PRESS, App.KEY_F4));
                            v.getBackground().setColorFilter(0xe0f47521, PorterDuff.Mode.SRC_ATOP);
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        try {
                            write(pack(App.COMMAND_KEY_RELEASE, App.KEY_ALT));
                            Thread.sleep(5);
                            write(pack(App.COMMAND_KEY_RELEASE, App.KEY_F4));
                            v.getBackground().clearColorFilter();
                            v.invalidate();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                }
                return false;
            }
        });

        //<editor-fold desc="butoane tastatura">

        esc = v.findViewById(R.id.esc);
        b1 = v.findViewById(R.id.but1);
        b2 = v.findViewById(R.id.but2);
        b3 = v.findViewById(R.id.but3);
        b4 = v.findViewById(R.id.but4);
        b5 = v.findViewById(R.id.but5);
        b6 = v.findViewById(R.id.but6);
        b7 = v.findViewById(R.id.but7);
        b8 = v.findViewById(R.id.but8);
        b9 = v.findViewById(R.id.but9);
        b0 = v.findViewById(R.id.but0);
        backspace = v.findViewById(R.id.backspace);
        tab = v.findViewById(R.id.tab);
        q = v.findViewById(R.id.q);
        w = v.findViewById(R.id.w);
        e = v.findViewById(R.id.e);
        r = v.findViewById(R.id.r);
        t = v.findViewById(R.id.t);
        y = v.findViewById(R.id.y);
        u = v.findViewById(R.id.u);
        i = v.findViewById(R.id.i);
        o = v.findViewById(R.id.o);
        p = v.findViewById(R.id.p);
        caps = v.findViewById(R.id.caps);
        a = v.findViewById(R.id.a);
        s = v.findViewById(R.id.s);
        d = v.findViewById(R.id.d);
        f = v.findViewById(R.id.f);
        g = v.findViewById(R.id.g);
        h = v.findViewById(R.id.h);
        j = v.findViewById(R.id.j);
        k = v.findViewById(R.id.k);
        l = v.findViewById(R.id.l);
        shift = v.findViewById(R.id.shift);
        z = v.findViewById(R.id.z);
        x = v.findViewById(R.id.x);
        c = v.findViewById(R.id.c);
        vButton = v.findViewById(R.id.v);
        b = v.findViewById(R.id.b);
        n = v.findViewById(R.id.n);
        m = v.findViewById(R.id.m);
        enter = v.findViewById(R.id.enter);
        space = v.findViewById(R.id.space);

        ctrl = v.findViewById(R.id.ctrl);
        win = v.findViewById(R.id.win);
        alt = v.findViewById(R.id.del);
        esc.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    print("esc press");
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_ESCAPE));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    print("esc release");
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_ESCAPE));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });

        b1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, '1'));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, '1'));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });

        b2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, '2'));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, '2'));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        b3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, '3'));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, '3'));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        b4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, '4'));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, '4'));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        b5.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, '5'));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, '5'));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        b6.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, '6'));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, '6'));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        b7.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, '7'));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, '7'));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        b8.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, '8'));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, '8'));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        b9.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, '9'));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, '9'));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        b0.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, '0'));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, '0'));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });

        backspace.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_BACKSPACE));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_BACKSPACE));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        tab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_TAB));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_TAB));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        q.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_Q));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_Q));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        w.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_W));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_W));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        e.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_E));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_E));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        r.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_R));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_R));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        t.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_T));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_T));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        y.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_Y));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_Y));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        u.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_U));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_U));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        i.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_I));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_I));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        o.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_O));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_O));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        p.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_P));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_P));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        caps.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_CAPSLOCK));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_CAPSLOCK));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        a.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_A));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_A));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        s.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_S));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_S));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        d.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_D));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_D));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        f.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_F));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_F));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        g.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_G));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_G));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        h.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_H));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_H));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        j.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_J));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_J));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        k.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_K));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_K));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        l.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_L));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_L));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        shift.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_SHIFT));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_SHIFT));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        z.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_Z));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_Z));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        x.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_X));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_X));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        c.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_C));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_C));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        vButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_V));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_V));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_B));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_B));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        n.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_N));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_N));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        m.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_M));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_M));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        enter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_ENTER));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_ENTER));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        space.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_SPACE));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_SPACE));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        ctrl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_CTRL));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_CTRL));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        win.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_WINDOWS));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_WINDOWS));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        alt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        write(pack(App.COMMAND_KEY_PRESS, App.KEY_ALT));
                        v.getBackground().setColorFilter(buttonPressColor, PorterDuff.Mode.MULTIPLY);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        write(pack(App.COMMAND_KEY_RELEASE, App.KEY_ALT));
                        v.getBackground().clearColorFilter();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                return true;
            }
        });
        //</editor-fold>


        openSouthPark = v.findViewById(R.id.open_southpark);
        int rand = new Random().nextInt(3);
        Drawable d;
        switch (rand) {
            case 0:
                d = getResources().getDrawable(R.drawable.southpark_icon_1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    openSouthPark.setBackground(d);
                }
                break;
            case 1:
                d = getResources().getDrawable(R.drawable.southpark_icon_2);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    openSouthPark.setBackground(d);
                }
                break;
            case 2:
                d = getResources().getDrawable(R.drawable.southpark_icon_3);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    openSouthPark.setBackground(d);
                }
                break;
        }
        openSouthPark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    write((App.COMMAND_OPEN_LINK_NEW_TAB + " " + SOUTHPARK_LINK + "\n").getBytes());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        openYoutube = v.findViewById(R.id.open_youtube);
        openYoutube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder youtubeMenu = new AlertDialog.Builder(main);
                final EditText input = new EditText(pcControl.this.main);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                youtubeMenu.setView(input);
                youtubeMenu.setTitle("Caută pe youtube");
                youtubeMenu.setMessage((main.pcSettings.YOUTUBE_KILL_CHROME ? "Actiunea v-a închide prima data chrome" :
                        "Acțiunea v-a deschide un tab nou"));
                youtubeMenu.setPositiveButton("Vezi primul rezultat", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!input.getText().toString().equals("")) {
                            if (main.pcSettings.YOUTUBE_KILL_CHROME) {
                                try {
                                    write((App.COMMAND_YOUTUBE_PLAY + " " + input.getText().toString() + "\n").getBytes());
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                try {
                                    write((App.COMMAND_YOUTUBE_PLAY_NEW_TAB + " " + input.getText().toString() + "\n").getBytes());
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        } else {
                            Toast.makeText(main, "Scrie ceva in bara de cautare", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                youtubeMenu.setNegativeButton("Arată rezultatele", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!input.getText().toString().equals("")) {
                            if (main.pcSettings.YOUTUBE_KILL_CHROME) {
                                try {
                                    write((App.COMMAND_YOUTUBE_RESULTS + " " + input.getText().toString() + "\n").getBytes());
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                try {
                                    write((App.COMMAND_YOUTUBE_RESULTS_NEW_TAB + " " + input.getText().toString() + "\n").getBytes());
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        } else {
                            Toast.makeText(main, "Scrie ceva in bara de cautare", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                youtubeMenu.setNeutralButton("Piesa random", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (main.pcSettings.YOUTUBE_KILL_CHROME) {
                            try {
                                write((App.COMMAND_YOUTUBE_RANDOM + " " + input.getText().toString() + "\n").getBytes());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            try {
                                write((App.COMMAND_YOUTUBE_RANDOM_NEW_TAB + " " + input.getText().toString() + "\n").getBytes());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                });
                AlertDialog dialog = youtubeMenu.show();
                final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                final Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                final Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
                positiveButtonLL.gravity = Gravity.CENTER;

                neutralButton.setLayoutParams(positiveButtonLL);
                positiveButton.setLayoutParams(positiveButtonLL);
                negativeButton.setLayoutParams(positiveButtonLL);
            }
        });
        openChrome = v.findViewById(R.id.open_chrome);
        openChrome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderChromeOptions = new AlertDialog.Builder(main);
                final EditText input = new EditText(pcControl.this.main);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builderChromeOptions.setView(input);
                builderChromeOptions.setTitle("Căutare in browser");
                builderChromeOptions.setNegativeButton("Caută într-un tab nou", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!input.getText().toString().equals("")) {
                            try {
                                if (isLink(input.getText().toString())) {
                                    /**
                                     * vezi ca java nu iti deschide nimic daca nu pui http://
                                     */
                                    write((App.COMMAND_OPEN_LINK_NEW_TAB + " http://" + input.getText().toString()
                                            + "\n").getBytes());
                                    Toast.makeText(main, "ddeschidem link", Toast.LENGTH_SHORT).show();
                                } else {
                                    String querry = convertQuerry(input.getText().toString());
                                    write((App.COMMAND_OPEN_LINK_NEW_TAB + " " + GOOGLE_SEARCH + querry
                                            + "\n").getBytes());
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            Toast.makeText(main, "Scrie ceva in bara de cautare", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builderChromeOptions.setPositiveButton("Închide browser și caută", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!input.getText().toString().equals("")) {
                            try {
                                if (isLink(input.getText().toString())) {
                                    write((App.COMMAND_OPEN_LINK + " http://" + input.getText().toString()
                                            + "\n").getBytes());
                                } else {
                                    String querry = convertQuerry(input.getText().toString());
                                    write((App.COMMAND_OPEN_LINK + " " + GOOGLE_SEARCH + querry
                                            + "\n").getBytes());
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            Toast.makeText(main, "Scrie ceva in bara de cautare", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                AlertDialog dialog = builderChromeOptions.show();
                final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                final Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
                positiveButtonLL.gravity = Gravity.CENTER;

                positiveButton.setLayoutParams(positiveButtonLL);
                negativeButton.setLayoutParams(positiveButtonLL);
                /*try {
                    write((App.COMMAND_OPEN_LINK_NEW_TAB+" "+GOOGLE_LINK+"\n").getBytes());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }*/
            }
        });
        closeChrome = v.findViewById(R.id.exit_chrome);
        closeChrome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    write((App.COMMAND_KILL_CHROME + "\n").getBytes());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        if (App.NIGHT_MODE_ENABLED){
            nightModePreset(v);
        }
        return v;

        //</editor-fold>

    }

    boolean isLink(String text) {
        return (text.contains("www.") || text.contains("http:") || text.contains("https:"));
    }

    /**
     * Converteste un string sub format querry pt google search
     * schimba + cu %2B
     * schimba spatiile cu +
     *
     * @param x ana are mere 2+2
     * @return ana+are+mere+2%2B2
     */
    String convertQuerry(String x) {
        //<editor-fold desc="body">
        char ar[] = x.toCharArray();
        String news = "";
        for (int i = 0; i < ar.length; i++)
            if (ar[i] == '+') {
                news += "%2B";
            } else {
                news += (ar[i] != ' ' ? ar[i] : "+");
            }
        return news;
        //</editor-fold>
    }

    /**
     * Porneste thread-ul ce trimite ping-uri la server
     * functioneaza si cu ambele tipuri de conexiuni
     */
    @Deprecated
    void startPinger() {
        /*
        //<editor-fold>
        Log.println(Log.ASSERT, "PINGER", "pinger a pornit");
        try{
            pingSender.interrupt();
        }catch(Exception ex){

        }
        pingSender = new Thread() {
            public void run() {
                while (runPinger) {
                    try {
                        pcOutputStream.write((App.COMMAND_IP_ALIVE + "\n").getBytes());
                        Log.println(Log.ASSERT, "Trimitem ping", "PING PING");
                        Thread.sleep(10000);
                    } catch (IOException ex) {
                        runPinger = false;
                    } catch (InterruptedException ex) {
                        runPinger = false;
                    } catch (NullPointerException ex) {
                        runPinger = false;
                    }
                }
                Log.println(Log.ASSERT, "pingSender", "signing off");
            }
        };
        runPinger = true;
        pingSender.start();
        //</editor-fold>
    */
    }

    /**
     * Functie onResume e apelata de Android de fiecare data cand intram in pcControl
     * daca {@link #autoConnect} e {@link Boolean#TRUE} atunci de fiecare data cand este apelata
     * aceasta functie, se trimite un ping spre server, daca trimiterea a esuat, se incearca conectarea
     * automata, de aici este pornit pinger-ul
     */
    @Override
    public void onResume() {
        //<editor-fold desc="body">
        super.onResume();
        if (App.CONNECTION_TYPE == App.ANDROID_MASTER) {
            shouldStartPinger = true;
            if (autoConnect) {
                try {
                    try {
                        Log.println(Log.ASSERT, "iNCERCAM sa trimitem ", "bluetooth");
                        pcOutputStream.write((App.COMMAND_IP_ALIVE + "\n").getBytes());

                        Log.println(Log.ASSERT, "am reusit sa scriu bt", "BAAA");
                        //startPinger(); // am renuntat la trimiterea de ping-uri
                    } catch (NetworkOnMainThreadException ex) {
                        new Thread() {
                            public void run() {
                                Log.println(Log.ASSERT, "HELLO", "BAAA");
                                try {
                                    Log.println(Log.ASSERT, "trimitem la ip", "text");
                                    pcOutputStream.write((App.COMMAND_IP_ALIVE + "\n").getBytes());

                                    Log.println(Log.ASSERT, "sss", "cica a trimis ok");
                                    if (!runPinger) {
                                        startPinger();
                                    }
                                } catch (Exception exc) {
                                    Log.println(Log.ASSERT, "BAG PL", "FAil LA TRIMITERE ");
                                    new Thread() {
                                        /*Handler hand = new Handler(Looper.getMainLooper()) {
                                            @Override
                                            public void handleMessage(@NonNull Message msg) {
                                                try {
                                                    Toast.makeText(main, "Se incearca conectarea automata ", Toast.LENGTH_SHORT).show();
                                                    connect();
                                                    Toast.makeText(main, "Conexiune reusita", Toast.LENGTH_SHORT).show();
                                                    shouldStartPinger = true;
                                                } catch (IOException | ClassNotFoundException ex) {
                                                    ex.printStackTrace();
                                                    Toast.makeText(main, "Conexiune esuata \n" + ex.toString(), Toast.LENGTH_SHORT).show();
                                                    shouldStartPinger = false;
                                                }
                                            }
                                        };
    */
                                        public void run() {

                                            try {
                                                //Toast.makeText(main, "Se incearca conectarea automata ", Toast.LENGTH_SHORT).show();
                                                Log.println(Log.ASSERT, "COnnect", "se incearca conectarea");
                                                connect();
                                                Log.println(Log.ASSERT, "COnnect", "conexiune reusita");
                                                //Toast.makeText(main, "Conexiune reusita", Toast.LENGTH_SHORT).show();
                                                shouldStartPinger = true;
                                            } catch (IOException | ClassNotFoundException ex) {
                                                ex.printStackTrace();
                                                Log.println(Log.ASSERT, "COnnect", "conexiune esuata");
                                                shouldStartPinger = false;
                                            }

                                      /*  Message m = hand.obtainMessage();
                                        m.sendToTarget();*/
                                            Log.println(Log.ASSERT, "stari", shouldStartPinger + " " + runPinger + " ");
                                            if (shouldStartPinger) {
                                                if (!runPinger) {
                                                    startPinger();
                                                }
                                            }

                                        }
                                    }.start();
                                }
                            }
                        }.start();
                    }
                    shouldStartPinger = true;
                } catch (Exception ex) {
                    new Thread() {
                        Handler h = new Handler(Looper.getMainLooper()) {
                            @Override
                            public void handleMessage(@NonNull Message msg) {
                                try {
                                    Toast.makeText(main, "Se incearca conectarea automata ", Toast.LENGTH_SHORT).show();
                                    connect();
                                    Toast.makeText(main, "Conexiune reusita", Toast.LENGTH_SHORT).show();
                                    shouldStartPinger = true;
                                } catch (IOException | ClassNotFoundException ex) {
                                    ex.printStackTrace();
                                    Toast.makeText(main, "Conexiune esuata \n" + ex.toString(), Toast.LENGTH_SHORT).show();
                                    shouldStartPinger = false;
                                }
                            }
                        };

                        public void run() {

                            Message m = h.obtainMessage();
                            m.sendToTarget();
                            if (shouldStartPinger) {
                                if (!runPinger) {
                                    startPinger();
                                }
                            }

                        }
                    }.start();
                }

            }
        }
        //</editor-fold>
    }

}


//<editor-fold desc="dump">

        /*new Thread(){
            public void run(){
                try {
                    Client client=new Client("192.168.1.107",153);
                    Log.println(Log.ASSERT,"Conn","ne am conectat la pc");
                    pcOutputStream=client.out;

                } catch (IOException ex) {
                    Log.println(Log.ASSERT,"err",ex.toString());
                }
            }
        }.start();*/

/*
        String message = "Hello from Android.\n";
        byte[] msgBuffer = message.getBytes();
        try {
            write(msgBuffer);
        } catch (IOException e) {
            String msg = e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
            msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            Log.println(Log.ASSERT, "anus", msg);
            //AlertBox("Fatal Error", msg);
        }
*/
        /*
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

        */
                /*
            double limit(double value) {
        if (value < -1d)
            return -1d;
        else if (value > 1d)
            return 1d;
        return value;
    }
    double px, py;
    double sensitivity = 0.5d;
    int xMove = 0;
    int yMove = 0;

BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();
        //BluetoothDevice[] devices = (BluetoothDevice[]) bondedDevices.toArray();
        for (BluetoothDevice device : bondedDevices) {
            arrayAdapter.add(device.getAddress() + " " + device.getName());
            nume.add(device.getName());
        }
         double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }
 */
//</editor-fold>