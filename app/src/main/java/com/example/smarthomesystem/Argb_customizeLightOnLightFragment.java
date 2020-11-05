package com.example.smarthomesystem;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

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

import static com.example.smarthomesystem.MainActivity.ARGB;

import java.io.IOException;


/**
 * Interfata pentru a customiza animatia de pornire a ledurilor argb
 *
 * @author Manel
 * @since 06/02/2020-15:43
 */
public class Argb_customizeLightOnLightFragment extends Fragment {

    //<editor-fold desc="Variabile" defaultstate="collapsed">
    private MainActivity main;
    private AlertDialog.Builder dialogTipAnimatii;
    private ArrayAdapter<String> tipuri;
    private Dialog vitezaDialog, sectiuniDialog, incrementDialog;
    private int CURRENT_TIP = 0;
    private int CURRENT_VITEZA = 255;
    private int CURRENT_SECTIUNI = 3;
    private int CURRENT_INCREMENT = 3;
    static final String PROFILE_NAME = "lighton";
    private static final String TIPURI_ANIMATII[] = {"Fade in", "Extindere centrală 1", "Extindere centrală 2", "Extindere secțiuni stânga-dreapta",
            "Extindere secțiuni stânga", "Extindere secțiuni dreapta"};
    //</editor-fold>


    public Argb_customizeLightOnLightFragment() {
        // Required empty public constructor
    }

    public Argb_customizeLightOnLightFragment(MainActivity main) {
        this.main = main;
    }

    void setTip(int tip) {
        this.CURRENT_TIP = tip;
    }

    void setSectiuni(int sectiuni) {
        this.CURRENT_SECTIUNI = sectiuni;
    }

    void setViteza(int viteza) {
        this.CURRENT_VITEZA = viteza;
    }

    void setIncrement(int increment) {
        this.CURRENT_INCREMENT = increment;
    }

    int getTip() {
        return CURRENT_TIP;
    }

    int getSectiuni() {
        return CURRENT_SECTIUNI;
    }

    int getAnimationViteza() {
        return CURRENT_VITEZA;
    }

    int getIncrement() {
        return CURRENT_INCREMENT;
    }


    private void updateSelectedTip(final int indexSelected) {
        //<editor-fold desc="Variabile" defaultstate="collapsed">
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
                        byte[] byteData = new byte[10];
                        byteData[0] = 'j';
                        byteData[1] = '+';
                        byteData[2] = MainActivity.getByte(255);
                        byteData[3] = MainActivity.getByte(255);
                        byteData[4] = MainActivity.getByte(255);
                        byteData[5] = MainActivity.getByte(which + 1);
                        byteData[6] = MainActivity.getByte(getAnimationViteza());
                        byteData[7] = MainActivity.getByte(getIncrement());
                        byteData[8] = MainActivity.getByte(getSectiuni());

                        byte suma = byteData[0];
                        for (int i = 1; i < 9; i++) {
                            suma += byteData[i];
                        }
                        byteData[9] = suma;
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
        updateSelectedTip(CURRENT_TIP);
        //</editor-fold>
    }

    void tipSorter(View v) {
        //<editor-fold desc="body" defaultstate="collapsed">
        int choice = getTip();
        if (choice == 0) {
            v.findViewById(R.id.light_on_sectiuni_button).setVisibility(View.GONE);
            v.findViewById(R.id.light_on_increment_button).setVisibility(View.VISIBLE);
        } else if (choice == 1 || choice == 2) {
            v.findViewById(R.id.light_on_sectiuni_button).setVisibility(View.GONE);
            v.findViewById(R.id.light_on_increment_button).setVisibility(View.GONE);
        } else {
            v.findViewById(R.id.light_on_sectiuni_button).setVisibility(View.VISIBLE);
            v.findViewById(R.id.light_on_increment_button).setVisibility(View.GONE);
        }
        //</editor-fold>
    }


    public void profileChanged() {
        //<editor-fold desc="body" defaultstate="collapsed">
        ARGBProfile profile = new ARGBProfile(PROFILE_NAME, CURRENT_TIP, CURRENT_VITEZA, CURRENT_SECTIUNI, CURRENT_INCREMENT);
        try {
            profile.save();
        } catch (IOException e) {
            e.printStackTrace();
            Log.println(Log.ASSERT, "Eroare", "Nu am putut salva " + profile.name + ".svt");
        }
        //</editor-fold>
    }

    public void nightModePreset(View v) {
        ((TextView) (v.findViewById(R.id.textView10))).setTextColor(App.NIGHT_MODE_TEXT);
        MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.argb_light_on_tip_button).getBackground());
        ((Button) (v.findViewById(R.id.argb_light_on_tip_button))).setTextColor(App.NIGHT_MODE_TEXT);

        MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.argb_light_on_viteza_button).getBackground());
        ((Button) (v.findViewById(R.id.argb_light_on_viteza_button))).setTextColor(App.NIGHT_MODE_TEXT);

        MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.light_on_sectiuni_button).getBackground());
        ((Button) (v.findViewById(R.id.light_on_sectiuni_button))).setTextColor(App.NIGHT_MODE_TEXT);

        MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.light_on_increment_button).getBackground());
        ((Button) (v.findViewById(R.id.light_on_increment_button))).setTextColor(App.NIGHT_MODE_TEXT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //<editor-fold desc="body" defaultstate="collapsed">
        final View v = inflater.inflate(R.layout.fragment_argb_customize_light_on, container, false);
        tipSorter(v);


        dialogTipAnimatii = new AlertDialog.Builder(v.getContext(), AlarmFragment.getTimePickerDialogType());
        dialogTipAnimatii.setTitle("Alege tipul animației");
        dialogTipAnimatii.setPositiveButton("Alege", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //aici salvezi ce a selectat
                ListView lw = ((AlertDialog) dialog).getListView();
                //Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
                updateSelectedTip(lw.getCheckedItemPosition());
                CURRENT_TIP = lw.getCheckedItemPosition();
                tipSorter(v);
                profileChanged();

            }
        });
        tipuri = new ArrayAdapter<>(v.getContext(), android.R.layout.select_dialog_singlechoice);
        updateAdapters();
        v.findViewById(R.id.argb_light_on_tip_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialogTipAnimatii.show();
                    }
                }, 100);
            }
        });

        vitezaDialog = new Dialog(v.getContext(), AlarmFragment.getTimePickerDialogType());
        View vitezaDialogView = inflater.inflate(R.layout.light_on_viteza_dialog, (ViewGroup) v.findViewById(R.id.light_on_viteza_root));
        vitezaDialog.setContentView(vitezaDialogView);
        v.findViewById(R.id.argb_light_on_viteza_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SeekBar) vitezaDialog.findViewById(R.id.argb_light_on_viteza_slider)).setProgress(CURRENT_VITEZA - 1);
                vitezaDialog.show();
            }
        });

        sectiuniDialog = new Dialog(v.getContext(), AlarmFragment.getTimePickerDialogType());
        View sectiuniDialogView = inflater.inflate(R.layout.light_on_sectiuni_dialog, (ViewGroup) v.findViewById(R.id.light_on_sectiuni_root));
        sectiuniDialog.setContentView(sectiuniDialogView);
        v.findViewById(R.id.light_on_sectiuni_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SeekBar) sectiuniDialog.findViewById(R.id.argb_light_on_sectiuni_slider)).setProgress(CURRENT_SECTIUNI - 1);
                sectiuniDialog.show();
            }
        });


        incrementDialog = new Dialog(v.getContext(), AlarmFragment.getTimePickerDialogType());
        View incrementDialogView = inflater.inflate(R.layout.light_on_increment_dialog, (ViewGroup) v.findViewById(R.id.light_on_increment_root));
        incrementDialog.setContentView(incrementDialogView);
        v.findViewById(R.id.light_on_increment_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SeekBar) incrementDialog.findViewById(R.id.argb_light_on_increment_slider)).setProgress(CURRENT_INCREMENT - 1);
                incrementDialog.show();
            }
        });


        vitezaDialog.findViewById(R.id.argb_light_on_viteza_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CURRENT_VITEZA = ((SeekBar) (vitezaDialog.findViewById(R.id.argb_light_on_viteza_slider))).getProgress() + 1;
                profileChanged();
                vitezaDialog.dismiss();
            }
        });

        vitezaDialog.findViewById(R.id.argb_light_on_viteza_test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    main.write(ARGB.TURN_OFF_NO_ANIMATION);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        byte[] byteData = new byte[10];
                        byteData[0] = 'j';
                        byteData[1] = '+';
                        byteData[2] = MainActivity.getByte(255);
                        byteData[3] = MainActivity.getByte(255);
                        byteData[4] = MainActivity.getByte(255);
                        byteData[5] = MainActivity.getByte(getTip() + 1);
                        byteData[6] = MainActivity.getByte(
                                ((SeekBar) (vitezaDialog.findViewById(R.id.argb_light_on_viteza_slider))).getProgress() + 1
                                //+1 pt ca nu putem avea viteza 0 si la seekbar nu se poate seta direct un minim, doar un maxim
                        );
                        byteData[7] = MainActivity.getByte(getIncrement());
                        byteData[8] = MainActivity.getByte(getSectiuni());

                        byte suma = byteData[0];
                        for (int i = 1; i < 9; i++) {
                            suma += byteData[i];
                        }
                        byteData[9] = suma;
                        try {
                            main.write(byteData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, 100);

            }
        });

        sectiuniDialog.findViewById(R.id.argb_light_on_sectiuni_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CURRENT_SECTIUNI = ((SeekBar) sectiuniDialog.findViewById(R.id.argb_light_on_sectiuni_slider)).getProgress() + 1;
                profileChanged();
                sectiuniDialog.dismiss();
            }
        });
        sectiuniDialog.findViewById(R.id.argb_light_on_sectiuni_test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    main.write(ARGB.TURN_OFF_NO_ANIMATION);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        byte[] byteData = new byte[10];
                        byteData[0] = 'j';
                        byteData[1] = '+';
                        byteData[2] = MainActivity.getByte(255);
                        byteData[3] = MainActivity.getByte(255);
                        byteData[4] = MainActivity.getByte(255);
                        byteData[5] = MainActivity.getByte(getTip() + 1);
                        byteData[6] = MainActivity.getByte(getAnimationViteza());
                        byteData[7] = MainActivity.getByte(getIncrement());
                        byteData[8] = MainActivity.getByte(
                                ((SeekBar) sectiuniDialog.findViewById(R.id.argb_light_on_sectiuni_slider)).getProgress() + 1);

                        byte suma = byteData[0];
                        for (int i = 1; i < 9; i++) {
                            suma += byteData[i];
                        }
                        byteData[9] = suma;
                        try {
                            main.write(byteData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, 100);
            }
        });

        incrementDialog.findViewById(R.id.argb_light_on_increment_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CURRENT_INCREMENT = ((SeekBar) incrementDialog.findViewById(R.id.argb_light_on_increment_slider)).getProgress() + 1;
                profileChanged();
                incrementDialog.dismiss();
            }
        });

        incrementDialog.findViewById(R.id.argb_light_on_increment_test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    main.write(ARGB.TURN_OFF_NO_ANIMATION);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        byte[] byteData = new byte[10];
                        byteData[0] = 'j';
                        byteData[1] = '+';
                        byteData[2] = MainActivity.getByte(255);
                        byteData[3] = MainActivity.getByte(255);
                        byteData[4] = MainActivity.getByte(255);
                        byteData[5] = MainActivity.getByte(getTip() + 1);
                        byteData[6] = MainActivity.getByte(getAnimationViteza());
                        byteData[7] = MainActivity.getByte(
                                ((SeekBar) incrementDialog.findViewById(R.id.argb_light_on_increment_slider)).getProgress() + 1);
                        byteData[8] = MainActivity.getByte(getSectiuni());

                        byte suma = byteData[0];
                        for (int i = 1; i < 9; i++) {
                            suma += byteData[i];
                        }
                        byteData[9] = suma;
                        try {
                            main.write(byteData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, 100);
            }
        });
        if (App.NIGHT_MODE_ENABLED) {
            nightModePreset(v);

            ((TextView) (vitezaDialogView.findViewById(R.id.textView11))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(vitezaDialogView.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, vitezaDialogView.findViewById(R.id.argb_light_on_viteza_test_button).getBackground());
            ((Button)(vitezaDialogView.findViewById(R.id.argb_light_on_viteza_test_button))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(vitezaDialogView.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, vitezaDialogView.findViewById(R.id.argb_light_on_viteza_ok_button).getBackground());
            ((Button)(vitezaDialogView.findViewById(R.id.argb_light_on_viteza_ok_button))).setTextColor(App.NIGHT_MODE_TEXT);

            ((TextView) (sectiuniDialogView.findViewById(R.id.textView11))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(sectiuniDialogView.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, sectiuniDialogView.findViewById(R.id.argb_light_on_sectiuni_test_button).getBackground());
            ((Button)(sectiuniDialogView.findViewById(R.id.argb_light_on_sectiuni_test_button))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(sectiuniDialogView.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, sectiuniDialogView.findViewById(R.id.argb_light_on_sectiuni_ok_button).getBackground());
            ((Button)(sectiuniDialogView.findViewById(R.id.argb_light_on_sectiuni_ok_button))).setTextColor(App.NIGHT_MODE_TEXT);

            ((TextView) (incrementDialogView.findViewById(R.id.textView11))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(incrementDialogView.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, incrementDialogView.findViewById(R.id.argb_light_on_increment_test_button).getBackground());
            ((Button)(incrementDialogView.findViewById(R.id.argb_light_on_increment_test_button))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(incrementDialogView.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, incrementDialogView.findViewById(R.id.argb_light_on_increment_ok_button).getBackground());
            ((Button)(incrementDialogView.findViewById(R.id.argb_light_on_increment_ok_button))).setTextColor(App.NIGHT_MODE_TEXT);

        }

        return v;
        //</editor-fold>
    }

}
