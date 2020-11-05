package com.example.smarthomesystem;


import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Interfata pentru sectiunea pcSettings (vizibila la longclick pe iconita de conectare la pc)
 */
public class pcSettings extends Fragment {

    //<editor-fold desc="Variabile">
    RadioButton touchScreenRadioButton, touchPadRadioButton;
    Switch autoConnect, shareApp, newTab, killChrome, youtubeNewTab, youtubeKillChrome;
    String CURSOR_TYPE = App.TOUCH_SCREEN_CURSOR_TYPE;
    boolean AUTO_CONNECT = false;
    boolean OPEN_SHARE_APP = false;
    boolean NEW_TAB = false;
    boolean KILL_CHROME = true;
    boolean YOUTUBE_KILL_CHROME = true;
    boolean YOUTUBE_NEW_TAB = false;
    MainActivity main;
    //</editor-fold>

    public pcSettings() {
        // Required empty public constructor
    }

    public pcSettings(MainActivity main) {
        //<editor-fold desc="body">
        this.main = main;
        try {
            List<String> date = loadPC();
            try {
                CURSOR_TYPE = date.get(0);
                AUTO_CONNECT = Boolean.valueOf(date.get(1));
                OPEN_SHARE_APP = Boolean.valueOf(date.get(2));
                NEW_TAB = Boolean.valueOf(date.get(3));
                KILL_CHROME = Boolean.valueOf(date.get(4));
                YOUTUBE_NEW_TAB = Boolean.valueOf(date.get(5));
                YOUTUBE_KILL_CHROME = Boolean.valueOf(date.get(6));
            } catch (Exception ex) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //</editor-fold>
    }

    void cursorTypeChanged(String type) {
        //<editor-fold desc="body">
        Log.println(Log.ASSERT, "cursor change", type);
        try {
            savePcSettings();
            main.PC_FRAGMENT.CURSOR_TYPE = type;
        } catch (IOException e) {
            Log.println(Log.ASSERT, "EROARE", "Nu am putut salva pcSettings.svt");
        }
        //</editor-fold>
    }

    void savePcSettings() throws IOException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileOutputStream fos = main.openFileOutput("pcSettings.svt", Context.MODE_PRIVATE);
        List<String> x = new ArrayList<>();
        x.add(CURSOR_TYPE);
        x.add(Boolean.toString(AUTO_CONNECT));
        x.add(Boolean.toString(OPEN_SHARE_APP));
        x.add(Boolean.toString(NEW_TAB));
        x.add(Boolean.toString(KILL_CHROME));
        x.add(Boolean.toString(YOUTUBE_NEW_TAB));
        x.add(Boolean.toString(YOUTUBE_KILL_CHROME));
        ObjectOutputStream objos = new ObjectOutputStream(fos);
        objos.writeObject(x);
        objos.close();
        fos.close();
        Log.println(Log.ASSERT, "Salvare", "Am salvat pcSettings.svt cu cursorType " + x.get(0));
        //</editor-fold>
    }

    void autoConnectStateChanged(boolean state) {
        //<editor-fold desc="body">
        AUTO_CONNECT = state;
        try {
            savePcSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //</editor-fold>
    }

    void shareAppStateChanged(boolean state) {
        //<editor-fold desc="body">
        OPEN_SHARE_APP = state;
        try {
            savePcSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //</editor-fold>
    }

    void newTabStateChanged(boolean state) {
        //<editor-fold desc="body">
        NEW_TAB = state;
        KILL_CHROME = !state;
        try {
            savePcSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //</editor-fold>
    }

    void killChromeStateChanged(boolean state) {
        //<editor-fold desc="body">
        KILL_CHROME = state;
        NEW_TAB = !state;
        try {
            savePcSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //</editor-fold>
    }

    void youtubeKillChromeStateChanged(boolean state) {
        //<editor-fold desc="body">
        YOUTUBE_KILL_CHROME = state;
        YOUTUBE_NEW_TAB = !state;
        try {
            savePcSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //</editor-fold>
    }

    void youtubeNewTabStateChanged(boolean state) {
        //<editor-fold desc="body">
        YOUTUBE_NEW_TAB = state;
        YOUTUBE_KILL_CHROME = !state;
        try {
            savePcSettings();
        } catch (IOException e) {
            e.printStackTrace();
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

    public void nightModePreset(View v) {
        ((TextView) (v.findViewById(R.id.textcursor))).setTextColor(App.NIGHT_MODE_TEXT);

        v.findViewById(R.id.classic_mouse).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((RadioButton)(v.findViewById(R.id.classic_mouse))).setTextColor(App.NIGHT_MODE_TEXT);

        v.findViewById(R.id.touchpad_mouse).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((RadioButton)(v.findViewById(R.id.touchpad_mouse))).setTextColor(App.NIGHT_MODE_TEXT);

        ((TextView) (v.findViewById(R.id.text_con_aut))).setTextColor(App.NIGHT_MODE_TEXT);
        v.findViewById(R.id.switch_auto_connect).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((Switch)(v.findViewById(R.id.switch_auto_connect))).setTextColor(App.NIGHT_MODE_TEXT);

        ((TextView) (v.findViewById(R.id.text_sharing))).setTextColor(App.NIGHT_MODE_TEXT);
        v.findViewById(R.id.switch_open_sharing_app).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((Switch)(v.findViewById(R.id.switch_open_sharing_app))).setTextColor(App.NIGHT_MODE_TEXT);

        ((TextView) (v.findViewById(R.id.text_sharing))).setTextColor(App.NIGHT_MODE_TEXT);
        v.findViewById(R.id.switch_tab_nou).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((Switch)(v.findViewById(R.id.switch_tab_nou))).setTextColor(App.NIGHT_MODE_TEXT);
        v.findViewById(R.id.switch_kill_chrome).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((Switch)(v.findViewById(R.id.switch_kill_chrome))).setTextColor(App.NIGHT_MODE_TEXT);

        ((TextView) (v.findViewById(R.id.textView9))).setTextColor(App.NIGHT_MODE_TEXT);
        v.findViewById(R.id.youtube_new_tab).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((Switch)(v.findViewById(R.id.youtube_new_tab))).setTextColor(App.NIGHT_MODE_TEXT);
        v.findViewById(R.id.youtube_kill_chrome).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((Switch)(v.findViewById(R.id.youtube_kill_chrome))).setTextColor(App.NIGHT_MODE_TEXT);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //<editor-fold desc="body">
        View v = inflater.inflate(R.layout.fragment_pc_settings, container, false);
        List<String> date = null;
        try {
            date = loadPC();
            String x = "";
            for (String caca : date) {
                x += caca + " ";
            }
            Log.println(Log.ASSERT, "Date pcSETTINGS", x);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        touchScreenRadioButton = v.findViewById(R.id.classic_mouse);
        try {
            if (date.get(0).equals(App.TOUCH_SCREEN_CURSOR_TYPE)) {
                touchScreenRadioButton.setChecked(true);
            }
        } catch (Exception ex) {

        }
        touchScreenRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursorTypeChanged(App.TOUCH_SCREEN_CURSOR_TYPE);
            }
        });

        touchPadRadioButton = v.findViewById(R.id.touchpad_mouse);
        try {
            touchPadRadioButton.setChecked(date.get(0).equals(App.TOUCH_PAD_CURSOR_TYPE));
        } catch (Exception ex) {

        }
        touchPadRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursorTypeChanged(App.TOUCH_PAD_CURSOR_TYPE);
            }
        });

        autoConnect = v.findViewById(R.id.switch_auto_connect);
        try {
            autoConnect.setChecked(Boolean.valueOf(date.get(1)));
        } catch (Exception ex) {

        }
        autoConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoConnectStateChanged(autoConnect.isChecked());
                main.PC_FRAGMENT.autoConnect = autoConnect.isChecked();
            }
        });

        shareApp = v.findViewById(R.id.switch_open_sharing_app);
        try {
            shareApp.setChecked(Boolean.valueOf(date.get(2)));
        } catch (Exception ex) {

        }
        shareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareAppStateChanged(shareApp.isChecked());
            }
        });

        newTab = v.findViewById(R.id.switch_tab_nou);
        try {
            newTab.setChecked(Boolean.valueOf(date.get(3)));
        } catch (Exception ex) {

        }
        newTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                killChrome.setChecked(!newTab.isChecked());
                newTabStateChanged(newTab.isChecked());
            }
        });

        killChrome = v.findViewById(R.id.switch_kill_chrome);
        try {
            killChrome.setChecked(Boolean.valueOf(date.get(4)));
        } catch (Exception ex) {

        }
        killChrome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newTab.setChecked(!killChrome.isChecked());
                killChromeStateChanged(killChrome.isChecked());
            }
        });

        youtubeKillChrome = v.findViewById(R.id.youtube_kill_chrome);
        try {
            youtubeKillChrome.setChecked(Boolean.valueOf(date.get(6)));
        } catch (Exception ex) {

        }
        youtubeKillChrome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                youtubeNewTab.setChecked(!youtubeKillChrome.isChecked());
                youtubeKillChromeStateChanged(youtubeKillChrome.isChecked());
            }
        });

        youtubeNewTab = v.findViewById(R.id.youtube_new_tab);
        try {
            youtubeNewTab.setChecked(Boolean.valueOf(date.get(5)));
        } catch (Exception ex) {

        }
        youtubeNewTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                youtubeKillChrome.setChecked(!youtubeNewTab.isChecked());
                youtubeNewTabStateChanged(youtubeNewTab.isChecked());
            }
        });
        if (App.NIGHT_MODE_ENABLED){
            nightModePreset(v);
        }
        return v;
        //</editor-fold>
    }

}
