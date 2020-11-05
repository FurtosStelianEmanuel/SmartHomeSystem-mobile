package com.example.smarthomesystem;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import static com.example.smarthomesystem.MainActivity.ARGB;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;


/**
 * Interfata pentru a customiza animatiile FastLed
 *
 * @author Manel
 * @since 06/02/2020-14:44
 */
public class Argb_customizeFastLedFragment extends Fragment {

    //<editor-fold desc="Variabile" defaultstate="collapsed">
    MainActivity main;
    private AlertDialog.Builder dialogTipAnimatii;
    private ArrayAdapter<String> tipuri;
    int viteza = 255, tip = 0, intensitate = 255;
    private Dialog vitezaDialog, intensitateDialog;

    public static final String PROFILE_NAME = "fastled";

    private static final String TIPURI_ANIMATII[] = {"Random-LinearBlend", "RainbowStripeColors-NoBlend"
            , "RainbowStripeColors-LinearBlend", "Green and purple-LinearBlend", "Random-LinearBlend",
            "Black&White-NoBlend", "Black&White-LinearBlend", "CloudColors-LinearBlend", "PartyColors-LinearBlend"};
    //</editor-fold>

    public Argb_customizeFastLedFragment() {
        // Required empty public constructor
    }

    public Argb_customizeFastLedFragment(MainActivity main) {
        this.main = main;
    }


    public int getViteza() {
        return viteza;
    }

    public int getTip() {
        return tip;
    }

    public int getIntensitate() {
        return intensitate;
    }

    private void updateSelectedTip(final int indexSelected) {
        //<editor-fold desc="body" defaultstate="collapsed">
        dialogTipAnimatii.setSingleChoiceItems(tipuri, indexSelected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                //aici o sa trimiti un preview pentru fiecare tip de animatie
                try {
                    main.write(ARGB.TURN_OFF_NO_ANIMATION);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        byte[] byteData = new byte[6];
                        byteData[0] = 'j';
                        byteData[1] = '1';
                        byteData[2] = MainActivity.getByte(getViteza());
                        byteData[3] = MainActivity.getByte(which + 1);
                        byteData[4] = MainActivity.getByte(getIntensitate());
                        byte suma = byteData[0];
                        for (int i = 1; i < 5; i++) {
                            suma += byteData[i];
                        }
                        byteData[5] = suma;
                        try {
                            main.write(byteData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, 100);
            }
        });
        //</editor-fold>
    }

    private void updateAdapters() {
        //<editor-fold desc="body" defaultstate="collapsed">
        tipuri.clear();
        for (String s : TIPURI_ANIMATII) {
            tipuri.add(s);
        }
        updateSelectedTip(tip);
        //</editor-fold>
    }


    public void profileChanged() {
        //<editor-fold desc="body" defaultstate="collapsed">
        ARGBFastLedProfile profile = new ARGBFastLedProfile(viteza, tip, intensitate);
        try {
            profile.save();
        } catch (IOException e) {
            e.printStackTrace();
            Log.println(Log.ASSERT, "Eroare", "Nu am putut salva " + profile.name + ".svt");
        }
        //</editor-fold>
    }

    public void nightModePreset(View v) {
        ((TextView) (v.findViewById(R.id.textView14))).setTextColor(App.NIGHT_MODE_TEXT);

        MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.argb_fast_led_tip_button).getBackground());
        ((Button) (v.findViewById(R.id.argb_fast_led_tip_button))).setTextColor(App.NIGHT_MODE_TEXT);

        MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.argb_fast_led_viteza_button).getBackground());
        ((Button) (v.findViewById(R.id.argb_fast_led_viteza_button))).setTextColor(App.NIGHT_MODE_TEXT);

        MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.argb_fast_led_intensitate_button).getBackground());
        ((Button) (v.findViewById(R.id.argb_fast_led_intensitate_button))).setTextColor(App.NIGHT_MODE_TEXT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //<editor-fold desc="body" defaultstate="collapsed">
        View v = inflater.inflate(R.layout.fragment_argb_customize_fast_led, container, false);

        dialogTipAnimatii = new AlertDialog.Builder(v.getContext(), AlarmFragment.getTimePickerDialogType());
        dialogTipAnimatii.setTitle("Alege tipul animației");
        dialogTipAnimatii.setPositiveButton("Alege", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //aici salvezi ce a selectat
                ListView lw = ((AlertDialog) dialog).getListView();
                //Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
                updateSelectedTip(lw.getCheckedItemPosition());
                tip = lw.getCheckedItemPosition();
                profileChanged();
            }
        });
        tipuri = new ArrayAdapter<>(v.getContext(), android.R.layout.select_dialog_singlechoice);
        //tipuri.setDropDownViewTheme(R.style.AppTheme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Resources.Theme theme=v.getContext().getResources().newTheme();
            theme.applyStyle(AlarmFragment.getTimePickerDialogType(),true);
            tipuri.setDropDownViewTheme(theme);
        }
        updateAdapters();

        v.findViewById(R.id.argb_fast_led_tip_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialogTipAnimatii.show();
                    }
                }, 100);
            }
        });


        vitezaDialog = new Dialog(v.getContext(), AlarmFragment.getTimePickerDialogType());
        View fastledVitezaDialogView = inflater.inflate(R.layout.fast_led_viteza_dialog, (ViewGroup) v.findViewById(R.id.fast_led_viteza_root));
        vitezaDialog.setContentView(fastledVitezaDialogView);
        v.findViewById(R.id.argb_fast_led_viteza_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SeekBar) vitezaDialog.findViewById(R.id.argb_fast_led_viteza_slider)).setProgress(viteza + 1);
                vitezaDialog.show();
            }
        });

        intensitateDialog = new Dialog(v.getContext(), AlarmFragment.getTimePickerDialogType());
        View fastledIntensitateDialogView=inflater.inflate(R.layout.fast_led_intensitate_dialog, (ViewGroup) v.findViewById(R.id.fast_led_intensitate_root));
        intensitateDialog.setContentView(fastledIntensitateDialogView);
        v.findViewById(R.id.argb_fast_led_intensitate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SeekBar) intensitateDialog.findViewById(R.id.argb_fast_led_intensitate_slider)).setProgress(intensitate + 1);
                intensitateDialog.show();
            }
        });


        vitezaDialog.findViewById(R.id.argb_fast_led_viteza_test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] byteData = new byte[6];
                byteData[0] = 'j';
                byteData[1] = '1';
                byteData[2] = MainActivity.getByte(((SeekBar) vitezaDialog.findViewById(R.id.argb_fast_led_viteza_slider)).getProgress() + 1);
                byteData[3] = MainActivity.getByte(getTip());
                byteData[4] = MainActivity.getByte(getIntensitate());
                byte suma = byteData[0];
                for (int i = 1; i < 5; i++) {
                    suma += byteData[i];
                }
                byteData[5] = suma;
                try {
                    main.write(byteData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        vitezaDialog.findViewById(R.id.argb_fast_led_viteza_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viteza = ((SeekBar) vitezaDialog.findViewById(R.id.argb_fast_led_viteza_slider)).getProgress() + 1;
                profileChanged();
                vitezaDialog.dismiss();
            }
        });

        intensitateDialog.findViewById(R.id.argb_fast_led_intensitate_test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] byteData = new byte[6];
                byteData[0] = 'j';
                byteData[1] = '1';
                byteData[2] = MainActivity.getByte(getViteza());
                byteData[3] = MainActivity.getByte(getTip());
                byteData[4] = MainActivity.getByte(((SeekBar) intensitateDialog.findViewById(R.id.argb_fast_led_intensitate_slider)).getProgress() + 1);
                byte suma = byteData[0];
                for (int i = 1; i < 5; i++) {
                    suma += byteData[i];
                }
                byteData[5] = suma;
                try {
                    main.write(byteData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        intensitateDialog.findViewById(R.id.argb_fast_led_intensitate_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intensitate = ((SeekBar) intensitateDialog.findViewById(R.id.argb_fast_led_intensitate_slider)).getProgress() + 1;
                profileChanged();
                intensitateDialog.dismiss();
            }
        });
        if (App.NIGHT_MODE_ENABLED) {
            nightModePreset(v);
            ((TextView) (fastledVitezaDialogView.findViewById(R.id.textView11))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(fastledVitezaDialogView.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, fastledVitezaDialogView.findViewById(R.id.argb_fast_led_viteza_test_button).getBackground());
            ((Button)(fastledVitezaDialogView.findViewById(R.id.argb_fast_led_viteza_test_button))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(fastledVitezaDialogView.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, fastledVitezaDialogView.findViewById(R.id.argb_fast_led_viteza_ok_button).getBackground());
            ((Button)(fastledVitezaDialogView.findViewById(R.id.argb_fast_led_viteza_ok_button))).setTextColor(App.NIGHT_MODE_TEXT);

            ((TextView) (fastledIntensitateDialogView.findViewById(R.id.textView11))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(fastledIntensitateDialogView.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, fastledIntensitateDialogView.findViewById(R.id.argb_fast_led_intensitate_test_button).getBackground());
            ((Button)(fastledIntensitateDialogView.findViewById(R.id.argb_fast_led_intensitate_test_button))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(fastledIntensitateDialogView.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, fastledIntensitateDialogView.findViewById(R.id.argb_fast_led_intensitate_ok_button).getBackground());
            ((Button)(fastledIntensitateDialogView.findViewById(R.id.argb_fast_led_intensitate_ok_button))).setTextColor(App.NIGHT_MODE_TEXT);

        }
        return v;
        //</editor-fold>
    }

}
