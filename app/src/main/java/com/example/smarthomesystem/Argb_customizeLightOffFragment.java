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
 * Interfata pentru a customiza animatia de oprire a ledurilor argb
 *
 * @author Manel
 * @since 06/02/2020-15:39
 */
public class Argb_customizeLightOffFragment extends Fragment {

    //<editor-fold desc="Variabile" defaultstate="collapsed">
    private MainActivity main;
    private AlertDialog.Builder dialogTipAnimatii;
    private ArrayAdapter<String> tipuri;
    private Dialog vitezaDialog, sectiuniDialog, decrementDialog;
    private int CURRENT_TIP = 0;
    private int CURRENT_VITEZA = 255;
    private int CURRENT_SECTIUNI = 3;
    private int CURRENT_DECREMENT = 1;
    static final String PROFILE_NAME = "lightoff";
    private static final String TIPURI_ANIMATII[] = {"Fade out", "Retracție centrală", "Retracție secțiuni stânga-dreapta",
            "Retracție secțiuni dreapta", "Retracție secțiuni stânga"};
    //</editor-fold>

    public Argb_customizeLightOffFragment() {
        // Required empty public constructor
    }

    Argb_customizeLightOffFragment(MainActivity main) {
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

    void setDecrement(int decrement) {
        this.CURRENT_DECREMENT = decrement;
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

    int getDecrement() {
        return CURRENT_DECREMENT;
    }

    private void updateSelectedTip(final int indexSelected) {
        //<editor-fold desc="body" defaultstate="collapsed">
        dialogTipAnimatii.setSingleChoiceItems(tipuri, indexSelected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                //aici o sa trimiti un preview pentru fiecare tip de animatie
                try {
                    main.write(ARGB.TURN_ON_WHITE_LIGHT_NO_ANIMATION);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        byte[] byteData = new byte[7];
                        byteData[0] = 'j';
                        byteData[1] = '-';
                        byteData[2] = MainActivity.getByte(getAnimationViteza());
                        byteData[3] = MainActivity.getByte(getDecrement());
                        byteData[4] = MainActivity.getByte(which + 1);
                        byteData[5] = MainActivity.getByte(getSectiuni());

                        byte suma = byteData[0];
                        for (int i = 1; i < 6; i++) {
                            suma += byteData[i];
                        }
                        byteData[6] = suma;

                        try {
                            main.write(byteData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, 300);

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
            v.findViewById(R.id.argb_light_off_sectiuni_button).setVisibility(View.GONE);
            v.findViewById(R.id.argb_light_off_decrement_button).setVisibility(View.VISIBLE);
        } else if (choice == 1) {
            v.findViewById(R.id.argb_light_off_sectiuni_button).setVisibility(View.GONE);
            v.findViewById(R.id.argb_light_off_decrement_button).setVisibility(View.VISIBLE);
        } else {
            v.findViewById(R.id.argb_light_off_sectiuni_button).setVisibility(View.VISIBLE);
            v.findViewById(R.id.argb_light_off_decrement_button).setVisibility(View.GONE);
        }
        //</editor-fold>
    }


    public void profileChanged() {
        //<editor-fold desc="body" defaultstate="collapsed">
        ARGBProfile profile = new ARGBProfile(PROFILE_NAME, CURRENT_TIP, CURRENT_VITEZA, CURRENT_SECTIUNI, CURRENT_DECREMENT);
        try {
            profile.save();
        } catch (IOException e) {
            e.printStackTrace();
            Log.println(Log.ASSERT, "Eroare", "Nu am putut salva " + profile.name + ".svt");
        }
        //</editor-fold>
    }

    public void nightModeProfile(View v) {
        ((TextView) (v.findViewById(R.id.textView12))).setTextColor(App.NIGHT_MODE_TEXT);
        MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.argb_light_off_tip_button).getBackground());
        ((Button) (v.findViewById(R.id.argb_light_off_tip_button))).setTextColor(App.NIGHT_MODE_TEXT);

        MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.argb_light_off_viteza_button).getBackground());
        ((Button) (v.findViewById(R.id.argb_light_off_viteza_button))).setTextColor(App.NIGHT_MODE_TEXT);

        MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.argb_light_off_sectiuni_button).getBackground());
        ((Button) (v.findViewById(R.id.argb_light_off_sectiuni_button))).setTextColor(App.NIGHT_MODE_TEXT);

        MainActivity.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.argb_light_off_decrement_button).getBackground());
        ((Button) (v.findViewById(R.id.argb_light_off_decrement_button))).setTextColor(App.NIGHT_MODE_TEXT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //<editor-fold desc="body" defaultstate="collapsed">
        final View v = inflater.inflate(R.layout.fragment_argb_customize_light_off, container, false);
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

        v.findViewById(R.id.argb_light_off_tip_button).setOnClickListener(new View.OnClickListener() {
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
        View vitezaDialogView = inflater.inflate(R.layout.light_off_viteza_dialog, (ViewGroup) v.findViewById(R.id.light_off_viteza_root));
        vitezaDialog.setContentView(vitezaDialogView);
        v.findViewById(R.id.argb_light_off_viteza_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SeekBar) vitezaDialog.findViewById(R.id.argb_light_off_viteza_slider)).setProgress(CURRENT_VITEZA - 1);
                vitezaDialog.show();
            }
        });

        sectiuniDialog = new Dialog(v.getContext(), AlarmFragment.getTimePickerDialogType());
        View sectiuniDialogView = inflater.inflate(R.layout.light_off_sectiuni_dialog, (ViewGroup) v.findViewById(R.id.light_off_sectiuni_root));
        sectiuniDialog.setContentView(sectiuniDialogView);
        v.findViewById(R.id.argb_light_off_sectiuni_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SeekBar) sectiuniDialog.findViewById(R.id.argb_light_off_sectiuni_slider)).setProgress(CURRENT_SECTIUNI - 1);
                sectiuniDialog.show();
            }
        });

        decrementDialog = new Dialog(v.getContext(), AlarmFragment.getTimePickerDialogType());
        View decrementDialogView = inflater.inflate(R.layout.light_off_decrement_dialog, (ViewGroup) v.findViewById(R.id.light_off_decrement_root));
        decrementDialog.setContentView(decrementDialogView);
        v.findViewById(R.id.argb_light_off_decrement_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SeekBar) decrementDialog.findViewById(R.id.argb_light_off_decrement_slider)).setProgress(CURRENT_DECREMENT - 1);
                decrementDialog.show();
            }
        });

        ///
        vitezaDialog.findViewById(R.id.argb_light_off_viteza_test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    main.write(ARGB.TURN_ON_WHITE_LIGHT_NO_ANIMATION);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        byte[] byteData = new byte[7];
                        byteData[0] = 'j';
                        byteData[1] = '-';
                        byteData[2] = MainActivity.getByte(((SeekBar) vitezaDialog.findViewById(R.id.argb_light_off_viteza_slider)).getProgress() + 1);
                        byteData[3] = MainActivity.getByte(getDecrement());
                        byteData[4] = MainActivity.getByte(getTip() + 1);
                        byteData[5] = MainActivity.getByte(getSectiuni());
                        byte suma = byteData[0];
                        for (int i = 1; i < 6; i++) {
                            suma += byteData[i];
                        }
                        byteData[6] = suma;
                        try {
                            main.write(byteData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, 200);
            }
        });
        vitezaDialog.findViewById(R.id.argb_light_off_viteza_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CURRENT_VITEZA = ((SeekBar) (vitezaDialog.findViewById(R.id.argb_light_off_viteza_slider))).getProgress() + 1;
                profileChanged();
                vitezaDialog.dismiss();
            }
        });

        sectiuniDialog.findViewById(R.id.argb_light_off_sectiuni_test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    main.write(ARGB.TURN_ON_WHITE_LIGHT_NO_ANIMATION);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        byte[] byteData = new byte[7];
                        byteData[0] = 'j';
                        byteData[1] = '-';
                        byteData[2] = MainActivity.getByte(getAnimationViteza());
                        byteData[3] = MainActivity.getByte(getDecrement());
                        byteData[4] = MainActivity.getByte(getTip() + 1);
                        byteData[5] = MainActivity.getByte(
                                ((SeekBar) sectiuniDialog.findViewById(R.id.argb_light_off_sectiuni_slider)).getProgress() + 1);
                        byte suma = byteData[0];
                        for (int i = 1; i < 6; i++) {
                            suma += byteData[i];
                        }
                        byteData[6] = suma;
                        try {
                            main.write(byteData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, 200);
            }
        });

        sectiuniDialog.findViewById(R.id.argb_light_off_sectiuni_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CURRENT_SECTIUNI = ((SeekBar) sectiuniDialog.findViewById(R.id.argb_light_off_sectiuni_slider)).getProgress() + 1;
                profileChanged();
                sectiuniDialog.dismiss();
            }
        });

        decrementDialog.findViewById(R.id.argb_light_off_decrement_test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    main.write(ARGB.TURN_ON_WHITE_LIGHT_NO_ANIMATION);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        byte[] byteData = new byte[7];
                        byteData[0] = 'j';
                        byteData[1] = '-';
                        byteData[2] = MainActivity.getByte(getAnimationViteza());
                        byteData[3] = MainActivity.getByte(
                                ((SeekBar) decrementDialog.findViewById(R.id.argb_light_off_decrement_slider)).getProgress() + 1);
                        byteData[4] = MainActivity.getByte(getTip() + 1);
                        byteData[5] = MainActivity.getByte(getSectiuni());
                        byte suma = byteData[0];
                        for (int i = 1; i < 6; i++) {
                            suma += byteData[i];
                        }
                        byteData[6] = suma;
                        try {
                            main.write(byteData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, 200);
            }
        });
        decrementDialog.findViewById(R.id.argb_light_off_decrement_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CURRENT_DECREMENT = ((SeekBar) decrementDialog.findViewById(R.id.argb_light_off_decrement_slider)).getProgress() + 1;
                profileChanged();
                decrementDialog.dismiss();
            }
        });
        if (App.NIGHT_MODE_ENABLED) {
            nightModeProfile(v);

            ((TextView) (vitezaDialogView.findViewById(R.id.textView11))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(vitezaDialogView.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, vitezaDialogView.findViewById(R.id.argb_light_off_viteza_test_button).getBackground());
            ((Button)(vitezaDialogView.findViewById(R.id.argb_light_off_viteza_test_button))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(vitezaDialogView.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, vitezaDialogView.findViewById(R.id.argb_light_off_viteza_ok_button).getBackground());
            ((Button)(vitezaDialogView.findViewById(R.id.argb_light_off_viteza_ok_button))).setTextColor(App.NIGHT_MODE_TEXT);

            ((TextView) (sectiuniDialogView.findViewById(R.id.textView11))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(sectiuniDialogView.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, sectiuniDialogView.findViewById(R.id.argb_light_off_sectiuni_test_button).getBackground());
            ((Button)(sectiuniDialogView.findViewById(R.id.argb_light_off_sectiuni_test_button))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(sectiuniDialogView.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, sectiuniDialogView.findViewById(R.id.argb_light_off_sectiuni_ok_button).getBackground());
            ((Button)(sectiuniDialogView.findViewById(R.id.argb_light_off_sectiuni_ok_button))).setTextColor(App.NIGHT_MODE_TEXT);

            ((TextView) (decrementDialogView.findViewById(R.id.textView11))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(decrementDialogView.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, decrementDialogView.findViewById(R.id.argb_light_off_decrement_test_button).getBackground());
            ((Button)(decrementDialogView.findViewById(R.id.argb_light_off_decrement_test_button))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(decrementDialogView.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, decrementDialogView.findViewById(R.id.argb_light_off_decrement_ok_button).getBackground());
            ((Button)(decrementDialogView.findViewById(R.id.argb_light_off_decrement_ok_button))).setTextColor(App.NIGHT_MODE_TEXT);
        }
        return v;
        //</editor-fold>
    }

}
