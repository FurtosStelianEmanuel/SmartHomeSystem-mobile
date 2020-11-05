package com.example.smarthomesystem;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;


/**
 * Interfata pentru a seta prioritatea ledurilor (apare la longclick pe iconita de conectare)
 *
 * @author Manel
 * @since 06/02/2020
 */
public class LedTypePriorityFragment extends Fragment {

    //<editor-fold desc="Variabile" defaultstate="collapsed">
    MainActivity main;
    RadioButton stripNormal, stripArgb;
    StoreClass store = new StoreClass();
    Switch darkMode;
    //</editor-fold>

    public LedTypePriorityFragment() {

    }

    public LedTypePriorityFragment(MainActivity main) {
        this.main = main;
    }

    public void nightModePreset(View v) {
        ((TextView) (v.findViewById(R.id.textView16))).setTextColor(App.NIGHT_MODE_TEXT);

        v.findViewById(R.id.prioritate_led_strip_normal).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((RadioButton) (v.findViewById(R.id.prioritate_led_strip_normal))).setTextColor(App.NIGHT_MODE_TEXT);

        v.findViewById(R.id.prioritate_led_strip_adresabil).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((RadioButton) (v.findViewById(R.id.prioritate_led_strip_adresabil))).setTextColor(App.NIGHT_MODE_TEXT);

        darkMode.setTextColor(App.NIGHT_MODE_TEXT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //<editor-fold desc="body" defaultstate="collapsed">
        View v = inflater.inflate(R.layout.fragment_led_type_priority_fragment, container, false);
        stripNormal = v.findViewById(R.id.prioritate_led_strip_normal);
        stripArgb = v.findViewById(R.id.prioritate_led_strip_adresabil);
        darkMode = v.findViewById(R.id.night_mode);


        if (MainActivity.PRIORITATE_STRIP == MainActivity.PRIORITATE_STRIP_ARGB) {
            stripArgb.setChecked(true);
            stripNormal.setChecked(false);
        } else {
            stripArgb.setChecked(false);
            stripNormal.setChecked(true);
        }

        if (App.NIGHT_MODE_ENABLED) {
            darkMode.setChecked(true);
        }

        darkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.println(Log.ASSERT,"asd",darkMode.isChecked()+"");
                store.darkMode = darkMode.isChecked();

                try {
                    main.savePrioritateLedType(store);
                    Toast.makeText(v.getContext(), "Repornește aplicația", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.println(Log.ASSERT, "eroare", "nu am putut  salva prioritatea ledurilor, vezi debug");
                    Toast.makeText(v.getContext(), "Eroare", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        stripNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.PRIORITATE_STRIP = MainActivity.PRIORITATE_STRIP_NORMAL;
                store.value = MainActivity.PRIORITATE_STRIP_NORMAL;
                try {
                    main.savePrioritateLedType(store);
                } catch (IOException e) {
                    Log.println(Log.ASSERT, "eroare", "nu am putut  salva prioritatea ledurilor, vezi debug");
                    e.printStackTrace();
                }
            }
        });

        stripArgb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.PRIORITATE_STRIP = MainActivity.PRIORITATE_STRIP_ARGB;
                store.value = MainActivity.PRIORITATE_STRIP_ARGB;
                try {
                    main.savePrioritateLedType(store);
                } catch (IOException e) {
                    Log.println(Log.ASSERT, "eroare", "nu am putut  salva prioritatea ledurilor, vezi debug");
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
