package com.example.smarthomesystem;


import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
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

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.IOException;

import static com.example.smarthomesystem.MainActivity.ARGB;


/**
 * Interfata pentru a customiza culoarea 'speciala'
 *
 * @author Manel
 * @since 06/02/2020-14:30
 */
public class Argb_customizeColorFragment extends Fragment {

    //<editor-fold desc="Variabile" defaultstate="collapsed">
    private MainActivity main;
    private AlertDialog.Builder dialogTipAnimatii;
    private ArrayAdapter<String> tipuri;
    private Dialog vitezaDialog, sectiuniDialog, incrementDialog;

    private int CURRENT_TIP = 0;
    private int CURRENT_VITEZA = 255;
    private int CURRENT_SECTIUNI = 3;
    private int CURRENT_INCREMENT = 3;
    private SystemColor CURRENT_COLOR = new SystemColor(255, 144, 0);
    static final String PROFILE_NAME = "lightcustomcolor";

    private static final String TIPURI_ANIMATII[] = {"Fade in", "Extindere centrală 1", "Extindere centrală 2", "Extindere secțiuni stânga-dreapta",
            "Extindere secțiuni stânga", "Extindere secțiuni dreapta"};

    //</editor-fold>

    public Argb_customizeColorFragment() {

    }

    public Argb_customizeColorFragment(MainActivity main) {
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

    void setColor(SystemColor color) {
        this.CURRENT_COLOR = color;
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

    SystemColor getColor() {
        return CURRENT_COLOR;
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
                        byte[] byteData = new byte[10];
                        byteData[0] = 'j';
                        byteData[1] = '+';
                        byteData[2] = MainActivity.getByte(CURRENT_COLOR.getRed());
                        byteData[3] = MainActivity.getByte(CURRENT_COLOR.getGreen());
                        byteData[4] = MainActivity.getByte(CURRENT_COLOR.getBlue());
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
            v.findViewById(R.id.light_custom_sectiuni_button).setVisibility(View.GONE);
            v.findViewById(R.id.light_custom_increment_button).setVisibility(View.VISIBLE);
        } else if (choice == 1 || choice == 2) {
            v.findViewById(R.id.light_custom_sectiuni_button).setVisibility(View.GONE);
            v.findViewById(R.id.light_custom_increment_button).setVisibility(View.GONE);
        } else {
            v.findViewById(R.id.light_custom_sectiuni_button).setVisibility(View.VISIBLE);
            v.findViewById(R.id.light_custom_increment_button).setVisibility(View.GONE);
        }
        //</editor-fold>
    }


    public void profileChanged() {
        //<editor-fold desc="body" defaultstate="collapsed">
        ARGBProfile profile = new ARGBProfile(PROFILE_NAME, CURRENT_TIP, CURRENT_VITEZA, CURRENT_SECTIUNI, CURRENT_INCREMENT,
                CURRENT_COLOR);
        try {
            profile.save();
        } catch (IOException e) {
            e.printStackTrace();
            Log.println(Log.ASSERT, "Eroare", "Nu am putut salva " + profile.name + ".svt");
        }
        //</editor-fold>
    }

    public void nightModePreset(View v) {
        main.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.argb_light_custom_tip_button).getBackground());
        ((Button) (v.findViewById(R.id.argb_light_custom_tip_button))).setTextColor(App.NIGHT_MODE_TEXT);

        main.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.argb_light_custom_viteza_button).getBackground());
        ((Button) (v.findViewById(R.id.argb_light_custom_viteza_button))).setTextColor(App.NIGHT_MODE_TEXT);

        main.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.light_custom_sectiuni_button).getBackground());
        ((Button) (v.findViewById(R.id.light_custom_sectiuni_button))).setTextColor(App.NIGHT_MODE_TEXT);

        main.setDrawableFilterColor(v.getContext(), App.NIGHT_MODE_BUTTON_BACKGROUND, v.findViewById(R.id.light_custom_increment_button).getBackground());
        ((Button) (v.findViewById(R.id.light_custom_increment_button))).setTextColor(App.NIGHT_MODE_TEXT);

        ((TextView) (v.findViewById(R.id.textView10))).setTextColor(App.NIGHT_MODE_TEXT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //<editor-fold desc="body" defaultstate="collapsed">
        final View v = inflater.inflate(R.layout.fragment_argb_customize_color, container, false);
        tipSorter(v);

        v.findViewById(R.id.argb_light_custom_culoare_button).setBackgroundColor(
                Color.rgb(CURRENT_COLOR.getRed(), CURRENT_COLOR.getGreen(), CURRENT_COLOR.getBlue())
        );

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
        v.findViewById(R.id.argb_light_custom_tip_button).setOnClickListener(new View.OnClickListener() {
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
        View nightModeVitezaView = inflater.inflate(R.layout.light_custom_viteza_dialog, (ViewGroup) vitezaDialog.findViewById(R.id.light_custom_viteza_root));
        vitezaDialog.setContentView(nightModeVitezaView);

        v.findViewById(R.id.argb_light_custom_viteza_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SeekBar) vitezaDialog.findViewById(R.id.argb_custom_light_viteza_slider)).setProgress(CURRENT_VITEZA - 1);
                vitezaDialog.show();
            }
        });

        sectiuniDialog = new Dialog(v.getContext(), AlarmFragment.getTimePickerDialogType());
        View fastledSectiuniDialogView=inflater.inflate(R.layout.light_custom_sectiuni_dialog, (ViewGroup) sectiuniDialog.findViewById(R.id.light_custom_sectiuni_root));
        sectiuniDialog.setContentView(fastledSectiuniDialogView);
        v.findViewById(R.id.light_custom_sectiuni_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SeekBar) sectiuniDialog.findViewById(R.id.argb_custom_light_sectiuni_slider)).setProgress(CURRENT_SECTIUNI - 1);
                sectiuniDialog.show();
            }
        });

        incrementDialog = new Dialog(v.getContext(), AlarmFragment.getTimePickerDialogType());
        View fastledIncrementDialogView=inflater.inflate(R.layout.light_custom_increment_dialog, (ViewGroup) incrementDialog.findViewById(R.id.light_custom_increment_root));
        incrementDialog.setContentView(fastledIncrementDialogView);
        v.findViewById(R.id.light_custom_increment_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SeekBar) incrementDialog.findViewById(R.id.argb_custom_light_increment_slider)).setProgress(CURRENT_INCREMENT - 1);
                incrementDialog.show();
            }
        });

        vitezaDialog.findViewById(R.id.argb_custom_light_viteza_test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    main.write(ARGB.TURN_OFF_NO_ANIMATION);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        byte[] byteData = new byte[10];
                        byteData[0] = 'j';
                        byteData[1] = '+';
                        byteData[2] = MainActivity.getByte(CURRENT_COLOR.getRed());
                        byteData[3] = MainActivity.getByte(CURRENT_COLOR.getGreen());
                        byteData[4] = MainActivity.getByte(CURRENT_COLOR.getBlue());
                        byteData[5] = MainActivity.getByte(getTip() + 1);
                        byteData[6] = MainActivity.getByte(
                                ((SeekBar) (vitezaDialog.findViewById(R.id.argb_custom_light_viteza_slider))).getProgress() + 1
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
        vitezaDialog.findViewById(R.id.argb_custom_light_viteza_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CURRENT_VITEZA =
                        ((SeekBar) vitezaDialog.findViewById(R.id.argb_custom_light_viteza_slider)).getProgress() + 1;
                profileChanged();
                vitezaDialog.dismiss();
            }
        });

        sectiuniDialog.findViewById(R.id.argb_custom_light_sectiuni_test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    main.write(ARGB.TURN_OFF_NO_ANIMATION);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        byte[] byteData = new byte[10];
                        byteData[0] = 'j';
                        byteData[1] = '+';
                        byteData[2] = MainActivity.getByte(CURRENT_COLOR.getRed());
                        byteData[3] = MainActivity.getByte(CURRENT_COLOR.getGreen());
                        byteData[4] = MainActivity.getByte(CURRENT_COLOR.getBlue());
                        byteData[5] = MainActivity.getByte(getTip() + 1);
                        byteData[6] = MainActivity.getByte(getAnimationViteza());
                        byteData[7] = MainActivity.getByte(getIncrement());
                        byteData[8] = MainActivity.getByte(
                                ((SeekBar) sectiuniDialog.findViewById(R.id.argb_custom_light_sectiuni_slider)).getProgress() + 1);

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
        sectiuniDialog.findViewById(R.id.argb_custom_light_sectiuni_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CURRENT_SECTIUNI = ((SeekBar) sectiuniDialog.findViewById(R.id.argb_custom_light_sectiuni_slider)).getProgress() + 1;
                profileChanged();
                sectiuniDialog.dismiss();
            }
        });

        incrementDialog.findViewById(R.id.argb_custom_light_increment_test_button).setOnClickListener(new View.OnClickListener() {
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
                        byteData[2] = MainActivity.getByte(CURRENT_COLOR.getRed());
                        byteData[3] = MainActivity.getByte(CURRENT_COLOR.getGreen());
                        byteData[4] = MainActivity.getByte(CURRENT_COLOR.getBlue());
                        byteData[5] = MainActivity.getByte(getTip() + 1);
                        byteData[6] = MainActivity.getByte(getAnimationViteza());
                        byteData[7] = MainActivity.getByte(
                                ((SeekBar) incrementDialog.findViewById(R.id.argb_custom_light_increment_slider)).getProgress() + 1);
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
        incrementDialog.findViewById(R.id.argb_custom_light_increment_ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CURRENT_INCREMENT =
                        ((SeekBar) incrementDialog.findViewById(R.id.argb_custom_light_increment_slider)).getProgress() + 1;
                profileChanged();
                incrementDialog.dismiss();
            }
        });

        v.findViewById(R.id.argb_light_custom_culoare_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                ColorPickerDialogBuilder
                        .with(main, AlarmFragment.getTimePickerDialogType())
                        .setTitle("Culoarea custom")
                        .initialColor(Color.WHITE)
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                        .density(main.colorPicker.density)
                        .lightnessSliderOnly()
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                                Log.println(Log.ASSERT, "Culoare selectata", "#" + selectedColor);
                            }
                        })
                        .setOnColorChangedListener(new OnColorChangedListener() {
                            @Override
                            public void onColorChanged(int selectedColor) {
                                int red = App.getRed(selectedColor);
                                int green = App.getGreen(selectedColor);
                                int blue = App.getBlue(selectedColor);
                                try {
                                    main.write(ARGB.TURN_ON_COLOR_NO_ANIMATION, new SystemColor(red, green, blue));
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                Log.println(Log.ASSERT, "On click", "#" + selectedColor);
                                v.findViewById(R.id.argb_light_custom_culoare_button).setBackgroundColor(selectedColor);
                                int red = 255, green = 255, blue = 255;
                                if (selectedColor != 0) {
                                    red = App.getRed(selectedColor);
                                    green = App.getGreen(selectedColor);
                                    blue = App.getBlue(selectedColor);

                                } else {
                                    red = 255;
                                    green = 144;
                                    blue = 0;
                                }
                                CURRENT_COLOR.red = red;
                                CURRENT_COLOR.green = green;
                                CURRENT_COLOR.blue = blue;
                                profileChanged();
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();
            }
        });
        if (App.NIGHT_MODE_ENABLED) {
            nightModePreset(v);
            ((TextView) (nightModeVitezaView.findViewById(R.id.textView11))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(nightModeVitezaView.getContext(),
                    App.NIGHT_MODE_BUTTON_BACKGROUND,
                    nightModeVitezaView.findViewById(R.id.argb_custom_light_viteza_test_button).getBackground());
            ((Button)(nightModeVitezaView.findViewById(R.id.argb_custom_light_viteza_test_button))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(nightModeVitezaView.getContext(),
                    App.NIGHT_MODE_BUTTON_BACKGROUND,
                    nightModeVitezaView.findViewById(R.id.argb_custom_light_viteza_ok_button).getBackground());
            ((Button)(nightModeVitezaView.findViewById(R.id.argb_custom_light_viteza_ok_button))).setTextColor(App.NIGHT_MODE_TEXT);

            ((TextView) (fastledIncrementDialogView.findViewById(R.id.textView11))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(fastledIncrementDialogView.getContext(),
                    App.NIGHT_MODE_BUTTON_BACKGROUND,
                    fastledIncrementDialogView.findViewById(R.id.argb_custom_light_increment_test_button).getBackground());
            ((Button)(fastledIncrementDialogView.findViewById(R.id.argb_custom_light_increment_test_button))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(fastledIncrementDialogView.getContext(),
                    App.NIGHT_MODE_BUTTON_BACKGROUND,
                    fastledIncrementDialogView.findViewById(R.id.argb_custom_light_increment_ok_button).getBackground());
            ((Button)(fastledIncrementDialogView.findViewById(R.id.argb_custom_light_increment_ok_button))).setTextColor(App.NIGHT_MODE_TEXT);

            ((TextView) (fastledSectiuniDialogView.findViewById(R.id.textView11))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(fastledSectiuniDialogView.getContext(),
                    App.NIGHT_MODE_BUTTON_BACKGROUND,
                    fastledSectiuniDialogView.findViewById(R.id.argb_custom_light_sectiuni_test_button).getBackground());
            ((Button)(fastledSectiuniDialogView.findViewById(R.id.argb_custom_light_sectiuni_test_button))).setTextColor(App.NIGHT_MODE_TEXT);
            MainActivity.setDrawableFilterColor(fastledSectiuniDialogView.getContext(),
                    App.NIGHT_MODE_BUTTON_BACKGROUND,
                    fastledSectiuniDialogView.findViewById(R.id.argb_custom_light_sectiuni_ok_button).getBackground());
            ((Button)(fastledSectiuniDialogView.findViewById(R.id.argb_custom_light_sectiuni_ok_button))).setTextColor(App.NIGHT_MODE_TEXT);


        }
        return v;
        //</editor-fold>

    }

}
