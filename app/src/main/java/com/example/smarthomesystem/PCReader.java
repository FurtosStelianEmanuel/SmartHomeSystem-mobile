package com.example.smarthomesystem;

import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Clasa handler pentru a procesa comenzile ce vin de la calculator atunci cand aplicatia ruleaza in modul ANDROID MASTER
 *
 * @author Manel
 * @since 06/02/2020
 */
public class PCReader {

    //<editor-fold desc="Variabile" defaultstate="collapsed">
    Thread readerThread;
    MainActivity main;
    boolean run = false;
    InputStream rawInputStream;
    BufferedReader bufferedReader;
    //</editor-fold>

    PCReader(MainActivity main) {
        this.main = main;
    }

    public void set(InputStream in) {
        bufferedReader = new BufferedReader(new InputStreamReader(in));
        rawInputStream = in;
        start();
    }


    public void start() {
        //<editor-fold desc="body" defaultstate="collapsed">
        if (!run) {
            run = true;
            try {
                readerThread.interrupt();
            } catch (Exception ex) {

            }
            readerThread = new Thread() {
                int fromByteArray(byte[] bytes) {
                    return ByteBuffer.wrap(bytes).getInt();
                }

                @Override
                public void run() {
                    Log.println(Log.ASSERT, "Reader", "Pornit");
                    while (run) {
                        try {
                            //String line = bufferedReader.readLine();
                            //Log.println(Log.ASSERT, "as", Arrays.toString(line.getBytes()));
                            byte[] ar = new byte[9];
                            int res = rawInputStream.read(ar);
                            receivedPCData(ar);
                        } catch (IOException | StringIndexOutOfBoundsException ex) {
                            ex.printStackTrace();
                            run = ex instanceof IOException ? false : run;
                        }
                    }
                    Log.println(Log.ASSERT, "Reader", "Stopped");
                }
            };
            readerThread.start();
        } else {
            Log.println(Log.ASSERT, "atentie", "A fost apelat start() dar thread-ul este inca activ");
        }
        //</editor-fold>
    }

    void aprindeCuloareUsa() {
        //<editor-fold desc="body" defaultstate="collapsed">
        try {
            List<String> date = main.getDoorEvents();
            int selectedColor = Integer.valueOf(date.get(3));
            int red = 255, green = 255, blue = 255;
            if (selectedColor != 0) {
                red = App.getRed(selectedColor);
                green = App.getGreen(selectedColor);
                blue = App.getBlue(selectedColor);
            }
            try {
                MainActivity.Values.set(red, green, blue);
                main.write(MainActivity.Values.impachetatBytes((byte) 'K'));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
//</editor-fold>
    }

    void saveAndApplyDoorChanges(final byte[] changes) {
        //<editor-fold desc="body" defaultstate="collapsed">
        //Md111 -3123
        Handler h = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(@NonNull Message msg) {
                Log.println(Log.ASSERT,"A ajuns la proc", Arrays.toString(changes));
                final boolean aprindereAutomata = changes[3] == '1';
                final boolean oprireAutomata = changes[4] == '1';
                final boolean aplicareAutomata = changes[5] == '1';
                final int culoare = Color.rgb(changes[6], changes[7], changes[8]);
                Log.println(Log.ASSERT, "Am primit niste date ", "Poftim: " + aprindereAutomata + " "
                        + oprireAutomata + " " + aplicareAutomata + " " + culoare);

                try {
                    main.saveDoorEvents(aprindereAutomata, oprireAutomata, aplicareAutomata, culoare);
                    main.restoreDoorEvents(aprindereAutomata, oprireAutomata, aplicareAutomata, culoare);
                    new Thread() {
                        public void run() {
                            main.applyDoorEvents(Arrays.asList(Boolean.toString(aprindereAutomata),
                                    Boolean.toString(oprireAutomata),
                                    Boolean.toString(aplicareAutomata),
                                    Integer.toString(culoare)));
                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NullPointerException ex) {
                    Log.println(Log.ASSERT, "eroare", ex.toString());
                    ex.printStackTrace();
                }

            }
        };
        h.obtainMessage(Message.CONTENTS_FILE_DESCRIPTOR, new Message()).sendToTarget();

        Log.println(Log.ASSERT, "am primit", "line " + changes);
        //</editor-fold>
    }

    void receivedPCData(byte[] data) {
        //<editor-fold desc="body" defaultstate="collapsed">
        Log.println(Log.ASSERT, "primit", "Ce am citit " + Arrays.toString(data));

        if (data[0] != App.COMMAND_ARDUINO_REDIRECT) {
            try {
                main.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.println(Log.ASSERT, "vrea", "sa procesam in android");
            switch (data[1]) {
                case App.COMMAND_TURN_MID_LIGHT_ON:
                    aprindeCuloareUsa();
                    break;
                case App.COMMAND_ANDROID_APPLY:
                    saveAndApplyDoorChanges(data);
                    break;
            }
        }

        /*switch (line.charAt(0)) {
            case App.COMMAND_ARDUINO_REDIRECT:
                Log.println(Log.ASSERT, "aoco", "sopa de macacao");
                try {
                    main.write(line.substring(1));
                    Log.println(Log.ASSERT, "primit", "Ce am citit " + line);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
            case App.COMMAND_TURN_MID_LIGHT_ON:
                aprindeCuloareUsa();
                break;
            case App.COMMAND_ANDROID_APPLY:
                switch (line.charAt(1)) {
                    case App.DOOR_COMMAND:
                        saveAndApplyDoorChanges(line);
                        break;
                }
                break;
            default:
                try {
                    main.write(line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
*/
        //</editor-fold>
    }
}
