package com.example.smarthomesystem;


import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;


/**
 * Interfata pentru a customiza animatia cu steagul romaniei
 *
 * @author Manel
 * @since 06/02/2020-15:47
 */
public class Argb_customizeRomanianFlagFragment extends Fragment {

    //<editor-fold desc="Variabile" defaultstate="collapsed">
    MainActivity main;
    private Dialog vitezaDialog, nrSteaguriDialog, latimeDialog, directieDialog, orientareDialog, intensitateDialog;
    static final String PROFILE_NAME = "romanianflag";
    int vitezaSteaguri = 100, nrSteaguri = 1, latimeSteaguri = 60, directie = 0, orientare = 0, intensitate = 100;
    //</editor-fold>

    public Argb_customizeRomanianFlagFragment() {
    }

    public Argb_customizeRomanianFlagFragment(MainActivity main) {
        this.main = main;
    }

    int getViteza() {
        return vitezaSteaguri;
    }

    int getNrSteaguri() {
        return nrSteaguri;
    }

    public int getDirectie() {
        return directie;
    }

    public int getLatimeSteaguri() {
        return latimeSteaguri;
    }

    public int getOrientare() {
        return orientare;
    }

    public int getIntensitate() {
        return intensitate;
    }

    public void profileChanged() {
        //<editor-fold desc="body" defaultstate="collapsed">
        ARGBRomanianFlagProfile profile = new ARGBRomanianFlagProfile(vitezaSteaguri, nrSteaguri, latimeSteaguri, directie, orientare, intensitate);
        try {
            profile.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //</editor-fold>
    }

    public void nightModePreset(View v) {
        ((TextView) (v.findViewById(R.id.textView13))).setTextColor(App.NIGHT_MODE_TEXT);

        MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.argb_romanian_flag_viteza_button).getBackground());
        ((Button) (v.findViewById(R.id.argb_romanian_flag_viteza_button))).setTextColor(App.NIGHT_MODE_TEXT);

        MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.argb_romanian_flag_nr_steaguri_button).getBackground());
        ((Button) (v.findViewById(R.id.argb_romanian_flag_nr_steaguri_button))).setTextColor(App.NIGHT_MODE_TEXT);

        MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.argb_romanian_flag_latime_steaguri_button).getBackground());
        ((Button) (v.findViewById(R.id.argb_romanian_flag_latime_steaguri_button))).setTextColor(App.NIGHT_MODE_TEXT);

        MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.argb_romanian_flag_directie_button).getBackground());
        ((Button) (v.findViewById(R.id.argb_romanian_flag_directie_button))).setTextColor(App.NIGHT_MODE_TEXT);

        MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.argb_romanian_flag_orientare_button).getBackground());
        ((Button) (v.findViewById(R.id.argb_romanian_flag_orientare_button))).setTextColor(App.NIGHT_MODE_TEXT);

        MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.argb_romanian_flag_intensitate_button).getBackground());
        ((Button) (v.findViewById(R.id.argb_romanian_flag_intensitate_button))).setTextColor(App.NIGHT_MODE_TEXT);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //<editor-fold desc="body" defaultstate="collapsed">
        View v = inflater.inflate(R.layout.fragment_argb_customize_romanian_flag, container, false);

        vitezaDialog = new Dialog(v.getContext(), AlarmFragment.getTimePickerDialogType());
        View vitezaDialogView = inflater.inflate(R.layout.romanian_flag_viteza_dialog, (ViewGroup) v.findViewById(R.id.romanian_flag_viteza_root));
        vitezaDialog.setContentView(vitezaDialogView);
        v.findViewById(R.id.argb_romanian_flag_viteza_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SeekBar) vitezaDialog.findViewById(R.id.argb_romanian_flag_viteza_slider)).setProgress(vitezaSteaguri - 1);
                vitezaDialog.show();
            }
        });

        nrSteaguriDialog = new Dialog(v.getContext(), AlarmFragment.getTimePickerDialogType());
        View nrSteaguriView = inflater.inflate(R.layout.romanian_flag_nr_steaguri_dialog, (ViewGroup) v.findViewById(R.id.romanian_flag_nr_steaguri_root));
        nrSteaguriDialog.setContentView(nrSteaguriView);
        v.findViewById(R.id.argb_romanian_flag_nr_steaguri_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SeekBar) nrSteaguriDialog.findViewById(R.id.argb_romanian_flag_nr_steaguri_slider)).setProgress(nrSteaguri - 1);
                nrSteaguriDialog.show();
            }
        });

        latimeDialog = new Dialog(v.getContext(), AlarmFragment.getTimePickerDialogType());
        View latimeDialogView = inflater.inflate(R.layout.romanian_flag_latime_steaguri_dialog, (ViewGroup) v.findViewById(R.id.romanian_flag_latime_steaguri_root));
        latimeDialog.setContentView(latimeDialogView);
        v.findViewById(R.id.argb_romanian_flag_latime_steaguri_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SeekBar) latimeDialog.findViewById(R.id.argb_romanian_flag_latime_steaguri_slider)).setProgress(latimeSteaguri - 1);
                latimeDialog.show();
            }
        });

        directieDialog = new Dialog(v.getContext(), AlarmFragment.getTimePickerDialogType());
        View directieDialogView = inflater.inflate(R.layout.romanian_flag_directie_dialog, (ViewGroup) v.findViewById(R.id.romanian_flag_directie_root));
        directieDialog.setContentView(directieDialogView);
        v.findViewById(R.id.argb_romanian_flag_directie_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton directieStanga, directieDreapta;
                directieStanga = directieDialog.findViewById(R.id.argb_romanian_flag_directie_stanga_radio_button);
                directieDreapta = directieDialog.findViewById(R.id.argb_romanian_flag_directie_dreapta_radio_button);
                if (directie == 1) {
                    directieDreapta.setChecked(false);
                    directieStanga.setChecked(true);
                } else {
                    directieDreapta.setChecked(true);
                    directieStanga.setChecked(false);
                }
                directieDialog.show();
            }
        });

        orientareDialog = new Dialog(v.getContext(), AlarmFragment.getTimePickerDialogType());
        View orientareDialogView = inflater.inflate(R.layout.romanian_flag_orientare_dialog, (ViewGroup) v.findViewById(R.id.romanian_flag_orientare_root));
        orientareDialog.setContentView(orientareDialogView);
        v.findViewById(R.id.argb_romanian_flag_orientare_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton orientareStanga, orientareDreapta;
                orientareStanga = orientareDialog.findViewById(R.id.argb_romanian_flag_orientare_stanga_radio_button);
                orientareDreapta = orientareDialog.findViewById(R.id.argb_romanian_flag_orientare_dreapta_radio_button);
                if (orientare == 1) {
                    orientareDreapta.setChecked(false);
                    orientareStanga.setChecked(true);
                } else {
                    orientareDreapta.setChecked(true);
                    orientareStanga.setChecked(false);
                }
                orientareDialog.show();
            }
        });

        intensitateDialog = new Dialog(v.getContext(), AlarmFragment.getTimePickerDialogType());
        View intensitateDialogView = inflater.inflate(R.layout.romanian_flag_intensitate_dialog, (ViewGroup) v.findViewById(R.id.romanian_flag_intensitate_root));
        intensitateDialog.setContentView(intensitateDialogView);
        v.findViewById(R.id.argb_romanian_flag_intensitate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SeekBar) intensitateDialog.findViewById(R.id.argb_romanian_flag_intensitate_slider)).setProgress(intensitate - 1);
                intensitateDialog.show();
            }
        });

        vitezaDialog.findViewById(R.id.argb_romanian_flag_viteza_test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] byteData = new byte[9];
                byteData[0] = 'j';
                byteData[1] = '0';
                byteData[2] = MainActivity.getByte(getNrSteaguri());
                byteData[3] = MainActivity.getByte(getLatimeSteaguri());
                byteData[4] = MainActivity.getByte(((SeekBar) vitezaDialog.findViewById(R.id.argb_romanian_flag_viteza_slider)).getProgress() + 1);
                byteData[5] = MainActivity.getByte(getDirectie());
                byteData[6] = MainActivity.getByte(getOrientare());
                byteData[7] = MainActivity.getByte(getIntensitate());
                byte suma = byteData[0];
                for (int i = 1; i < 8; i++) {
                    suma += byteData[i];
                }
                byteData[8] = suma;
                try {
                    main.write(byteData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        vitezaDialog.findViewById(R.id.argb_romanian_flag_viteza_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vitezaSteaguri = ((SeekBar) vitezaDialog.findViewById(R.id.argb_romanian_flag_viteza_slider)).getProgress() + 1;
                profileChanged();
                vitezaDialog.dismiss();
            }
        });

        nrSteaguriDialog.findViewById(R.id.argb_romanian_flag_nr_steaguri_test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] byteData = new byte[9];
                byteData[0] = 'j';
                byteData[1] = '0';
                byteData[2] = MainActivity.getByte(((SeekBar) nrSteaguriDialog.findViewById(R.id.argb_romanian_flag_nr_steaguri_slider)).getProgress() + 1);
                byteData[3] = MainActivity.getByte(getLatimeSteaguri());
                byteData[4] = MainActivity.getByte(getViteza());
                byteData[5] = MainActivity.getByte(getDirectie());
                byteData[6] = MainActivity.getByte(getOrientare());
                byteData[7] = MainActivity.getByte(getIntensitate());
                byte suma = byteData[0];
                for (int i = 1; i < 8; i++) {
                    suma += byteData[i];
                }
                byteData[8] = suma;
                try {
                    main.write(byteData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        nrSteaguriDialog.findViewById(R.id.argb_romanian_flag_nr_steaguri_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nrSteaguri = ((SeekBar) nrSteaguriDialog.findViewById(R.id.argb_romanian_flag_nr_steaguri_slider)).getProgress() + 1;
                profileChanged();
                nrSteaguriDialog.dismiss();
            }
        });


        latimeDialog.findViewById(R.id.argb_romanian_flag_latime_steaguri_test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] byteData = new byte[9];
                byteData[0] = 'j';
                byteData[1] = '0';
                byteData[2] = MainActivity.getByte(getNrSteaguri());
                byteData[3] = MainActivity.getByte(((SeekBar) latimeDialog.findViewById(R.id.argb_romanian_flag_latime_steaguri_slider)).getProgress() + 1);
                byteData[4] = MainActivity.getByte(getViteza());
                byteData[5] = MainActivity.getByte(getDirectie());
                byteData[6] = MainActivity.getByte(getOrientare());
                byteData[7] = MainActivity.getByte(getIntensitate());
                byte suma = byteData[0];
                for (int i = 1; i < 8; i++) {
                    suma += byteData[i];
                }
                byteData[8] = suma;
                try {
                    main.write(byteData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        latimeDialog.findViewById(R.id.argb_romanian_flag_latime_steaguri_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                latimeSteaguri = ((SeekBar) latimeDialog.findViewById(R.id.argb_romanian_flag_latime_steaguri_slider)).getProgress() + 1;
                profileChanged();
                latimeDialog.dismiss();
            }
        });

        directieDialog.findViewById(R.id.argb_romanian_flag_directie_stanga_radio_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] byteData = new byte[9];
                byteData[0] = 'j';
                byteData[1] = '0';
                byteData[2] = MainActivity.getByte(getNrSteaguri());
                byteData[3] = MainActivity.getByte(getLatimeSteaguri());
                byteData[4] = MainActivity.getByte(getViteza());
                byteData[5] = MainActivity.getByte(1);
                byteData[6] = MainActivity.getByte(getOrientare());
                byteData[7] = MainActivity.getByte(getIntensitate());
                byte suma = byteData[0];
                for (int i = 1; i < 8; i++) {
                    suma += byteData[i];
                }
                byteData[8] = suma;
                try {
                    main.write(byteData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        directieDialog.findViewById(R.id.argb_romanian_flag_directie_dreapta_radio_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] byteData = new byte[9];
                byteData[0] = 'j';
                byteData[1] = '0';
                byteData[2] = MainActivity.getByte(getNrSteaguri());
                byteData[3] = MainActivity.getByte(getLatimeSteaguri());
                byteData[4] = MainActivity.getByte(getViteza());
                byteData[5] = MainActivity.getByte(0);
                byteData[6] = MainActivity.getByte(getOrientare());
                byteData[7] = MainActivity.getByte(getIntensitate());
                byte suma = byteData[0];
                for (int i = 1; i < 8; i++) {
                    suma += byteData[i];
                }
                byteData[8] = suma;
                try {
                    main.write(byteData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        directieDialog.findViewById(R.id.argb_romanian_flag_directie_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton directieStanga = directieDialog.findViewById(R.id.argb_romanian_flag_directie_stanga_radio_button);
                directie = directieStanga.isChecked() ? 1 : 0;
                profileChanged();
                directieDialog.dismiss();
            }
        });

        orientareDialog.findViewById(R.id.argb_romanian_flag_orientare_stanga_radio_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] byteData = new byte[9];
                byteData[0] = 'j';
                byteData[1] = '0';
                byteData[2] = MainActivity.getByte(getNrSteaguri());
                byteData[3] = MainActivity.getByte(getLatimeSteaguri());
                byteData[4] = MainActivity.getByte(getViteza());
                byteData[5] = MainActivity.getByte(getDirectie());
                byteData[6] = MainActivity.getByte(1);
                byteData[7] = MainActivity.getByte(getIntensitate());
                byte suma = byteData[0];
                for (int i = 1; i < 8; i++) {
                    suma += byteData[i];
                }
                byteData[8] = suma;
                try {
                    main.write(byteData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        orientareDialog.findViewById(R.id.argb_romanian_flag_orientare_dreapta_radio_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] byteData = new byte[9];
                byteData[0] = 'j';
                byteData[1] = '0';
                byteData[2] = MainActivity.getByte(getNrSteaguri());
                byteData[3] = MainActivity.getByte(getLatimeSteaguri());
                byteData[4] = MainActivity.getByte(getViteza());
                byteData[5] = MainActivity.getByte(getDirectie());
                byteData[6] = MainActivity.getByte(0);
                byteData[7] = MainActivity.getByte(getIntensitate());
                byte suma = byteData[0];
                for (int i = 1; i < 8; i++) {
                    suma += byteData[i];
                }
                byteData[8] = suma;
                try {
                    main.write(byteData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        orientareDialog.findViewById(R.id.argb_romanian_flag_orientare_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButton orientareStanga = orientareDialog.findViewById(R.id.argb_romanian_flag_orientare_stanga_radio_button);
                orientare = orientareStanga.isChecked() ? 1 : 0;
                profileChanged();
                orientareDialog.dismiss();
            }
        });

        intensitateDialog.findViewById(R.id.argb_romanian_flag_intensitate_test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] byteData = new byte[9];
                byteData[0] = 'j';
                byteData[1] = '0';
                byteData[2] = MainActivity.getByte(getNrSteaguri());
                byteData[3] = MainActivity.getByte(getLatimeSteaguri());
                byteData[4] = MainActivity.getByte(getViteza());
                byteData[5] = MainActivity.getByte(getDirectie());
                byteData[6] = MainActivity.getByte(getOrientare());
                byteData[7] = MainActivity.getByte(
                        ((SeekBar) intensitateDialog.findViewById(R.id.argb_romanian_flag_intensitate_slider)).getProgress() + 1);
                byte suma = byteData[0];
                for (int i = 1; i < 8; i++) {
                    suma += byteData[i];
                }
                byteData[8] = suma;
                try {
                    main.write(byteData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        intensitateDialog.findViewById(R.id.argb_romanian_flag_intensitate_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intensitate = ((SeekBar) intensitateDialog.findViewById(R.id.argb_romanian_flag_intensitate_slider)).getProgress() + 1;
                profileChanged();
                intensitateDialog.dismiss();
            }
        });

        if (App.NIGHT_MODE_ENABLED) {
            nightModePreset(v);
            ((TextView) (vitezaDialogView.findViewById(R.id.textView11))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, vitezaDialogView.findViewById(R.id.argb_romanian_flag_viteza_test_button).getBackground());
            ((Button) (vitezaDialogView.findViewById(R.id.argb_romanian_flag_viteza_test_button))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, vitezaDialogView.findViewById(R.id.argb_romanian_flag_viteza_ok_button).getBackground());
            ((Button) (vitezaDialogView.findViewById(R.id.argb_romanian_flag_viteza_ok_button))).setTextColor(App.NIGHT_MODE_TEXT);

            ((TextView) (nrSteaguriView.findViewById(R.id.textView11))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, nrSteaguriView.findViewById(R.id.argb_romanian_flag_nr_steaguri_test_button).getBackground());
            ((Button) (nrSteaguriView.findViewById(R.id.argb_romanian_flag_nr_steaguri_test_button))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, nrSteaguriView.findViewById(R.id.argb_romanian_flag_nr_steaguri_ok_button).getBackground());
            ((Button) (nrSteaguriView.findViewById(R.id.argb_romanian_flag_nr_steaguri_ok_button))).setTextColor(App.NIGHT_MODE_TEXT);

            ((TextView) (latimeDialogView.findViewById(R.id.textView11))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, latimeDialogView.findViewById(R.id.argb_romanian_flag_latime_steaguri_test_button).getBackground());
            ((Button) (latimeDialogView.findViewById(R.id.argb_romanian_flag_latime_steaguri_test_button))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, latimeDialogView.findViewById(R.id.argb_romanian_flag_latime_steaguri_ok_button).getBackground());
            ((Button) (latimeDialogView.findViewById(R.id.argb_romanian_flag_latime_steaguri_ok_button))).setTextColor(App.NIGHT_MODE_TEXT);

            ((TextView) (directieDialogView.findViewById(R.id.textView11))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, directieDialogView.findViewById(R.id.argb_romanian_flag_directie_ok_button).getBackground());
            ((Button) (directieDialogView.findViewById(R.id.argb_romanian_flag_directie_ok_button))).setTextColor(App.NIGHT_MODE_TEXT);
            directieDialogView.findViewById(R.id.argb_romanian_flag_directie_stanga_radio_button).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
            ((RadioButton)(directieDialogView.findViewById(R.id.argb_romanian_flag_directie_stanga_radio_button))).setTextColor(App.NIGHT_MODE_TEXT);
            directieDialogView.findViewById(R.id.argb_romanian_flag_directie_dreapta_radio_button).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
            ((RadioButton)(directieDialogView.findViewById(R.id.argb_romanian_flag_directie_dreapta_radio_button))).setTextColor(App.NIGHT_MODE_TEXT);

            ((TextView) (orientareDialogView.findViewById(R.id.textView11))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, orientareDialogView.findViewById(R.id.argb_romanian_flag_orientare_ok_button).getBackground());
            ((Button) (orientareDialogView.findViewById(R.id.argb_romanian_flag_orientare_ok_button))).setTextColor(App.NIGHT_MODE_TEXT);
            orientareDialogView.findViewById(R.id.argb_romanian_flag_orientare_stanga_radio_button).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
            ((RadioButton)(orientareDialogView.findViewById(R.id.argb_romanian_flag_orientare_stanga_radio_button))).setTextColor(App.NIGHT_MODE_TEXT);
            orientareDialogView.findViewById(R.id.argb_romanian_flag_orientare_dreapta_radio_button).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
            ((RadioButton)(orientareDialogView.findViewById(R.id.argb_romanian_flag_orientare_dreapta_radio_button))).setTextColor(App.NIGHT_MODE_TEXT);

            ((TextView) (intensitateDialogView.findViewById(R.id.textView11))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, intensitateDialogView.findViewById(R.id.argb_romanian_flag_intensitate_test_button).getBackground());
            ((Button) (intensitateDialogView.findViewById(R.id.argb_romanian_flag_intensitate_test_button))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, intensitateDialogView.findViewById(R.id.argb_romanian_flag_intensitate_ok_button).getBackground());
            ((Button) (intensitateDialogView.findViewById(R.id.argb_romanian_flag_intensitate_ok_button))).setTextColor(App.NIGHT_MODE_TEXT);


        }

        return v;
        //</editor-fold>
    }

}
