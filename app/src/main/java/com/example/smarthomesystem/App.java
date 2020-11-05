package com.example.smarthomesystem;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

/**
 * In aceasta clasa sunt definitii comune cu aplicatia pentru desktop, poate ar trebui clarificat sistemul de comunicare
 * si refactorizat
 * @author Manel
 * @since 06/02/2020-14:29
 */
public class App extends Application {
    /**
     * Canalele de notificari necesare pentru versiunile noi de Android
     */
    public static final String CHANNEL_1_ID = "bar_lum";
    public static final String CHANNEL_2_ID = "alarm";

    public static final String ASSISTANT = "ass";
    public static final int NOTIFICATION_MANAGER_ID = 0;
    public static String MUSIC = "M";
    public static String GALBEN = "galben";
    public static String MOV = "mov";
    public static String ROSU = "rosu";
    public static String PORTOCALIU = "portocaliu";
    public static String VERDE = "verde";
    public static String ALBASTRU = "albastru";
    static String PEPE_ROMAN = "Pepe broscoiul";
    static String PEPE_ENGLEZ = "Pepe the frog";
    static String LIGHT_ON = "LON";
    static String LIGHT_OFF = "LOFF";
    static String CONFIRMATION_PING = "spp-1";
    final static String TOUCH_SCREEN_CURSOR_TYPE = "tsc";
    final static String TOUCH_PAD_CURSOR_TYPE = "tpc";

    public static final int COMANDA_PORNESTE = 0;
    public static final int COMANDA_OPRIRE = 1;
    public static final int COMANDA_SETEAZA_CULOARE = 2;
    public static final int COMANDA_ALARMA = 3;
    public static final int COMANDA_NEGATA = -1;
    public static final int COMANDA_AMBIGUA = -2;
    public static final int COMANDA_NESUPORTATA = -3;
    public static final int COMANDA_SETEAZA_INTENSITATE_CULOARE = 4;
    public static final int COMANDA_MUZICA = 5;
    public static final int COMANDA_APRINDE_LUMINA_MARE=6;
    public static final int COMANDA_STINGE_LUMINA_MARE=7;

    public static final int ALARMA_IN = 0;
    public static final int ALARMA_LA = 1;

    static String TAGS[] = {
            LIGHT_ON,
            LIGHT_OFF,
    };
    static int ARDUINO_TIMEOUT = 21;
    static final int LIMBA_ROMANA = 1;
    static final int LIMBA_ENGLEZA = 2;
    static String COLOR = "clr";
    static String COLOR_TAGS[] = {
            COLOR
    };
    static String APRINDERE = "aprindere";
    static String STINGERE = "stingere";
    static String SWITCH_TAGS[] = {
            APRINDERE,
            STINGERE
    };
    static String PC_CONNECTION_BLUETOOTH = "pcb";
    static String PC_CONNECTION_IP = "pcip";

    public static boolean NIGHT_MODE_ENABLED=false;

    public static final int NIGHT_MODE_BACKGROUND_MAIN=0xff121212;
    public static final int NIGHT_MODE_BACKGROUND_SECONDARY=0xff222222;
    public static final int NIGHT_MODE_BUTTON_BACKGROUND=0xff343434;
    public static final int NIGHT_MODE_TEXT=0xffffffff;

    //<editor-fold desc="Varibile comune pt desktop si android">
    static final char COMMAND_MOUSE_MOVE_CLASSIC = 'm';
    static final char COMMAND_MOUSE_MOVE_TOUCH_PAD = 'n';
    static final char COMMAND_MOUSE_PRESS = 'b';
    static final char COMMAND_MOUSE_RELEASE = 'v';
    static final char COMMAND_MOUSE_WHEEL_UP = 'c';
    static final char COMMAND_MOUSE_WHEEL_DOWN = 'x';
    static final char COMMAND_MOUSE_WHEEL_PRESS = 'z';
    static final char COMMAND_MOUSE_WHEEL_RELEASE = ';';
    static final char COMMAND_MOUSE_RIGHT_PRESS = 'l';
    static final char COMMAND_MOUSE_RIGHT_RELEASE = 'k';
    static final char COMMAND_YOUTUBE_PLAY = 'j';
    static final char COMMAND_KEY_PRESS = 'h';
    static final char COMMAND_KEY_RELEASE = 'g';
    static final char COMMAND_VOLUME = 'f';
    static final char COMMAND_SHUTDOWN_PC = 'd';
    static final char COMMAND_IP_ALIVE = 's';
    static final char COMMAND_OPEN_LINK = 'a';
    static final char COMMAND_OPEN_LINK_NEW_TAB = 'p';
    static final char COMMAND_KILL_CHROME = 'o';
    static final char COMMAND_YOUTUBE_PLAY_NEW_TAB = 'i';
    static final char COMMAND_YOUTUBE_RESULTS = 'u';
    static final char COMMAND_YOUTUBE_RESULTS_NEW_TAB = 'y';
    static final char COMMAND_YOUTUBE_RANDOM = 't';
    static final char COMMAND_YOUTUBE_RANDOM_NEW_TAB = 'r';
    /**
     * daca comanda este precedata de acest caracter inseamna ca acel mesaj nu este menit pentru arduino
     * si trebuie procesat de android
     */
    public static final char COMMAND_ARDUINO_REDIRECT = '>';
    public static final char COMMAND_TURN_MID_LIGHT_ON = 'e';
    public static final char COMMAND_PC_APPLY = 'w';
    public static final char COMMAND_ANDROID_APPLY = 'M';
    public static final char COMMAND_PC_REQUEST_RESPONSE = 'N';
    public static final char DOOR_COMMAND = 'J';

    public static final char CHECK_CONNECTION_TYPE_TOKEN = 'B';
    public static final char CONFIRM_CONNECTION_TYPE_TOKEN = 'V';
    public static final char PC_MASTER = 'C';
    public static final char ANDROID_MASTER = 'X';
    public static final char SWITCH_CONNECTION_TYPE = 'Z';

    static char CONNECTION_TYPE = App.ANDROID_MASTER;

    /**
     * d01 daca setarea e activa, d00 daca setarea e inactiva
     */
    public static final String ARDUINO_AUTOMATIC_LIGHT_ON_DOOR = "d0";
    /**
     * d11 daca setarea e activa, d10 daca setarea e inactiva
     */
    public static final String ARDUINO_AUTOMATIC_LIGHT_OFF_DOOR = "d1";
    /**
     * Aceasta comanda nu este trimisa la arduino, ea tine doar de android si de interfata de la pc, dar ca sa nu mai
     * caut dupa ea cand am implementat ca setarile de la android sa apara si la pc si sa poata fi schimbate de la pc
     * i am dat prefix-ul ARDUINO
     */
    public static final String ARDUINO_AUTOMATIC_DOOR_APPLIER = "d2";
    /**
     * dt 0 0 255 pentru a seta culoare la deschiderea usii sa fie albastra
     */
    public static final String ARDUINO_DOOR_ON_COLOR = "dt";


    static final char KEY_ESCAPE = (char) 27;
    static final char KEY_BACKSPACE = (char) 8;
    static final char KEY_TAB = (char) 9;
    static final char KEY_SHIFT = (char) 16;
    static final char KEY_SPACE = (char) 32;
    static final char KEY_ENTER = (char) 10;
    static final char KEY_CAPSLOCK = (char) 20;
    static final char KEY_WINDOWS = (char) 524;
    static final char KEY_CTRL = (char) 17;
    static final char KEY_DEL = (char) 127;
    static final char KEY_ALT = (char) 18;
    static final char KEY_A = (char) 65;
    static final char KEY_B = (char) 66;
    static final char KEY_C = (char) 67;
    static final char KEY_D = (char) 68;
    static final char KEY_E = (char) 69;
    static final char KEY_F = (char) 70;
    static final char KEY_G = (char) 71;
    static final char KEY_H = (char) 72;
    static final char KEY_I = (char) 73;
    static final char KEY_J = (char) 74;
    static final char KEY_K = (char) 75;
    static final char KEY_L = (char) 76;
    static final char KEY_M = (char) 77;
    static final char KEY_N = (char) 78;
    static final char KEY_O = (char) 79;
    static final char KEY_P = (char) 80;
    static final char KEY_Q = (char) 81;
    static final char KEY_R = (char) 82;
    static final char KEY_S = (char) 83;
    static final char KEY_T = (char) 84;
    static final char KEY_U = (char) 85;
    static final char KEY_V = (char) 86;
    static final char KEY_W = (char) 87;
    static final char KEY_X = (char) 88;
    static final char KEY_Y = (char) 89;
    static final char KEY_Z = (char) 90;
    static final char KEY_LEFT_ARROW = (char) 37;
    static final char KEY_RIGHT_ARROW = (char) 39;
    static final char KEY_UP_ARROW = (char) 38;
    static final char KEY_DOWN_ARROW = (char) 40;
    static final char KEY_F4 = (char) 115;
    static final String SHUTDOWN_CODE = "shutdown";
    //</editor-fold>

    @Override
    public void onCreate() {
        super.onCreate();

        Log.println(Log.ASSERT, "Creat", "App");
        createNotificationChannels();
    }

    static int map(double x, double in_min, double in_max, double out_min, double out_max) {
        return (int) ((x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
    }

    static double mapd(double x, double in_min, double in_max, double out_min, double out_max) {
        return (int) ((x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
    }

    static int getRed(int color) {
        int red = (color >> 16) & 0xff;

        return red;
    }

    static int getGreen(int color) {
        int green = (color >> 8) & 0xff;
        return green;
    }

    static int getBlue(int color) {
        int blue = (color) & 0xff;
        return blue;
    }


    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Shortcuts",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel1.setDescription("Acest canal de notificari este folosit pentru bara " +
                    "cu shortcut la lumini si asistent");

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Alarma",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel2.setDescription("Canalul alarmei");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
        }
    }
}