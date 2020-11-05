package com.example.smarthomesystem;


import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
 * Interfata pentru sectiunea 'Asistent'
 *
 * @author Manel
 * @since 06/02/2020-15:59
 */
public class AsistentFragment extends Fragment {

    //<editor-fold desc="Variabile" defaultstate="collapsed">
    RadioButton romanaRadioButton, englezaRadioButton;
    Button[] comenziButtons;

    String traduceriEngleza[] = {
            "turn on the light",
            "turn off the light"
    };
    String traduceriRomana[] = {
            "aprinde luminile",
            "stinge luminile"};
    MainActivity main;
    //</editor-fold>

    public AsistentFragment() {
        // Required empty public constructor
    }

    public AsistentFragment(MainActivity main) {
        this.main = main;
    }

    static void saveAsistent(MainActivity main, int limba) throws IOException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileOutputStream fos = main.openFileOutput("asistent.svt", Context.MODE_PRIVATE);
        List<String> x = new ArrayList<>();
        x.add(limba + "");
        ObjectOutputStream objos = new ObjectOutputStream(fos);
        objos.writeObject(x);
        objos.close();
        fos.close();
        Log.println(Log.ASSERT, "Salvare", "Am salvat asistent.svt cu limba " + x.get(0));
        //</editor-fold>
    }

    void saveAsistent() throws IOException {
        //<editor-fold desc="body" defaultstate="collapsed">
        FileOutputStream fos = main.openFileOutput("asistent.svt", Context.MODE_PRIVATE);
        List<String> x = new ArrayList<>();
        x.add((englezaRadioButton.isChecked() ? App.LIMBA_ENGLEZA + "" : App.LIMBA_ROMANA + ""));
        ObjectOutputStream objos = new ObjectOutputStream(fos);
        objos.writeObject(x);
        objos.close();
        fos.close();
        Log.println(Log.ASSERT, "Salvare", "Am salvat asistent.svt cu limba " + x.get(0));
        //</editor-fold>
    }

    List<String> loadAsistent() throws IOException {
        //<editor-fold desc="body" defaultstate="collapsed">
        List<String> simpleClass = null;
        try {
            FileInputStream fis = main.openFileInput("asistent.svt");
            ObjectInputStream is = new ObjectInputStream(fis);
            simpleClass = (ArrayList<String>) is.readObject();
            is.close();
            fis.close();
        } catch (Exception ex) {
            Log.println(Log.ASSERT, "Eroare", "Bai la fisierul asistent.svt");
        }
        return simpleClass;
        //</editor-fold>
    }

    static List<String> loadAsistent(MainActivity mainActivity) {
        //<editor-fold desc="body" defaultstate="collapsed">
        List<String> simpleClass = null;
        try {
            FileInputStream fis = mainActivity.openFileInput("asistent.svt");
            ObjectInputStream is = new ObjectInputStream(fis);
            simpleClass = (ArrayList<String>) is.readObject();
            is.close();
            fis.close();
        } catch (Exception ex) {
            Log.println(Log.ASSERT, "Eroare", "Bai la fisierul asistent.svt");
        }
        return simpleClass;
        //</editor-fold>
    }

    /**
     * Schimba interfata asistentului in limba aleasa
     *
     * @param limba {@link App#LIMBA_ROMANA sau {@link App#LIMBA_ENGLEZA}}
     */
    void setLanguage(int limba) {
        //<editor-fold desc="body" defaultstate="collapsed">
        if (limba == App.LIMBA_ENGLEZA) {
            for (int i = 0; i < comenziButtons.length; i++) {
                comenziButtons[i].setText(traduceriEngleza[i]);
            }
        } else {
            for (int i = 0; i < comenziButtons.length; i++) {
                comenziButtons[i].setText(traduceriRomana[i]);
            }
        }
        //</editor-fold>
    }

    public void nightModePreset(View v) {
        ((ImageView) (v.findViewById(R.id.imageView3))).setImageResource(R.drawable.ic_pepeicon_night);
        v.findViewById(R.id.romana_button).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((RadioButton) (v.findViewById(R.id.romana_button))).setTextColor(App.NIGHT_MODE_TEXT);

        v.findViewById(R.id.engleza_button).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((RadioButton) (v.findViewById(R.id.engleza_button))).setTextColor(App.NIGHT_MODE_TEXT);

        ((TextView) (v.findViewById(R.id.textView5))).setTextColor(App.NIGHT_MODE_TEXT);

        MainActivity.setDrawableFilterColor(v.getContext(),App.NIGHT_MODE_BUTTON_BACKGROUND,v.findViewById(R.id.b1).getBackground());
        MainActivity.setDrawableFilterColor(v.getContext(),App.NIGHT_MODE_BUTTON_BACKGROUND,v.findViewById(R.id.b2).getBackground());
        ((Button)(v.findViewById(R.id.b1))).setTextColor(App.NIGHT_MODE_TEXT);
        ((Button)(v.findViewById(R.id.b2))).setTextColor(App.NIGHT_MODE_TEXT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //<editor-fold desc="body" defaultstate="collapsed">
        View v = inflater.inflate(R.layout.fragment_asistent, container, false);
        romanaRadioButton = v.findViewById(R.id.romana_button);
        englezaRadioButton = v.findViewById(R.id.engleza_button);


        comenziButtons = new Button[]{
                v.findViewById(R.id.b1),
                v.findViewById(R.id.b2)
        };
        try {
            List<String> prevAsistent = loadAsistent();
            if (prevAsistent != null) {
                try {
                    int limba = Integer.valueOf(prevAsistent.get(0));
                    if (limba == App.LIMBA_ENGLEZA) {
                        englezaRadioButton.setChecked(true);
                        romanaRadioButton.setChecked(false);
                    } else {
                        englezaRadioButton.setChecked(false);
                        romanaRadioButton.setChecked(true);
                    }
                    setLanguage(limba);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.println(Log.ASSERT, "ERROR", "problema la accesarea datelor din asistent.svt");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.println(Log.ASSERT, "ERROR", "problema la accesarea datelor din asistent.svt");

        }
        romanaRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                englezaRadioButton.setChecked(false);
                setLanguage(App.LIMBA_ROMANA);
                try {
                    saveAsistent();
                } catch (IOException e) {
                    Log.println(Log.ASSERT, "ERROR", "Nu am putut salva asistentul");
                    e.printStackTrace();
                }
            }
        });
        englezaRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                romanaRadioButton.setChecked(false);
                setLanguage(App.LIMBA_ENGLEZA);
                try {
                    saveAsistent();
                } catch (IOException e) {
                    Log.println(Log.ASSERT, "ERROR", "Nu am putut salva asistentul");
                    e.printStackTrace();
                }
            }
        });
        if (App.NIGHT_MODE_ENABLED) {
            nightModePreset(v);
        }
        return v;
        //</editor-fold>
    }

}
