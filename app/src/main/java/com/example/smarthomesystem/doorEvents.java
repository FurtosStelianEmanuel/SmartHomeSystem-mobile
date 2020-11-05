package com.example.smarthomesystem;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.IOException;


/**
 * Interfata pentru sectiunea 'Setari automatizare usa'
 *
 * @author Manel
 * @since 06/02/2020-16:08
 */
public class doorEvents extends Fragment {
    //<editor-fold desc="Variabile" defaultstate="collapsed">
    int color = 0;
    MainActivity main;
    ImageView iv;
    Button alegeCuloare;
    Button save;
    Switch aprindereAutomata;
    Switch oprireAutomata;
    Switch aplicaStartup;
    //</editor-fold>

    public doorEvents() {

    }


    public doorEvents(MainActivity main) {
        this.main = main;
    }

    int map(double x, double in_min, double in_max, double out_min, double out_max) {
        return (int) ((x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
    }

    /**
     * Aceeasi functionalitate cu {@link MainActivity#sendDoorCommands(boolean, boolean, int)}
     */
    void sendDoorCommands() {
        //<editor-fold desc="body" defaultstate="collapsed">
        new Thread() {
            public void run() {
                //<editor-fold desc="commands" defaultstate="collapsed">
                try {
                    Log.println(Log.ASSERT, "Send", "Trimitem comanda :" + "d0" + (aprindereAutomata.isChecked() ? "1" : "0"));
                    main.write("J0" + (aprindereAutomata.isChecked() ? "1" : "0") + "\n");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Log.println(Log.ASSERT, "Send", "Trimitem comanda :" + "d1" + (oprireAutomata.isChecked() ? "1" : "0"));
                    main.write("J1" + (oprireAutomata.isChecked() ? "1" : "0") + "\n");
                    Thread.sleep(100);
                } catch (IOException e) {
                    Log.println(Log.ASSERT, "Send Error", "Oprire automata");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //aplicarea setarilor la startup nu are rost sa fie trimisa la arduino
                //trecem la trimiterea culorii la deschiderea usii

                ColorDrawable c = (ColorDrawable) (alegeCuloare.getBackground());
                int color = c.getColor();
                int red = 255;
                int green = 255;
                int blue = 255;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    //double r=Color.valueOf(color).red();
                    if (color != 0) {
                        red = map(Color.valueOf(color).red(), 0, 1, 0, 255);
                        green = map(Color.valueOf(color).green(), 0, 1, 0, 255);
                        blue = map(Color.valueOf(color).blue(), 0, 1, 0, 255);
                    } else {
                        red = 255;
                        green = 255;
                        blue = 255;
                    }

                }
                Log.println(Log.ASSERT, "Send", "Trimitem comanda :" + "dt " + red + " " + green + " " + blue);
                try {
                    main.write("JH " + red + " " + green + " " + blue + "\n");
                    Thread.sleep(70);
                } catch (IOException e) {
                    Log.println(Log.ASSERT, "Send Error", "doorEvents(sendDoorCommands)");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //</editor-fold>
            }
        }.start();
        //</editor-fold>
    }


    /**
     * Initializeaza variabilele si apeleaza functia {@link MainActivity#restoreDoorEvents()}
     *
     * @param view view view-ul creat in {@link doorEvents#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}
     */
    void mainInit(View view) {
        //<editor-fold desc="body" defaultstate="collapsed">
        alegeCuloare = view.findViewById(R.id.buton_culoare);
        alegeCuloare.setBackgroundColor(main.on_color);
        iv = view.findViewById(R.id.icon);
        iv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (save.getVisibility() == View.INVISIBLE) {
                    save.setVisibility(View.VISIBLE);
                } else if (save.getVisibility() == View.VISIBLE) {
                    save.setVisibility(View.INVISIBLE);
                }
                return false;
            }
        });
        alegeCuloare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialogBuilder
                        .with(main)
                        .setTitle("Culoarea afișată la deschiderea ușii")
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
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    int red = App.getRed(selectedColor);
                                    int green = App.getGreen(selectedColor);
                                    int blue = App.getBlue(selectedColor);
                                    MainActivity.Values.set(red, green, blue);
                                    try {
                                        main.write(MainActivity.Values.impachetatBytes((byte) 'G'));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Log.println(Log.ASSERT, "ERROR", "Incompatibilitate android, versiune minima: OREO");
                                }

                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                Log.println(Log.ASSERT, "On click", "#" + selectedColor);
                                color = selectedColor;
                                alegeCuloare.setBackgroundColor(selectedColor);
                                int red = 255, green = 255, blue = 255;
                                try {
                                    if (selectedColor != 0) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            red = map(Color.valueOf(selectedColor).red(), 0, 1, 0, 255);
                                            green = map(Color.valueOf(selectedColor).green(), 0, 1, 0, 255);
                                            blue = map(Color.valueOf(selectedColor).blue(), 0, 1, 0, 255);
                                        }
                                    } else {
                                        red = 255;
                                        green = 255;
                                        blue = 255;
                                    }
                                    MainActivity.Values.set(red, green, blue);
                                    main.write(MainActivity.Values.impachetatBytes((byte) 'J', (byte) 'H'));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    main.saveDoorEvents();
                                } catch (IOException | NullPointerException e) {
                                    e.printStackTrace();
                                }
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


        aprindereAutomata = view.findViewById(R.id.aprindere_automata);
        oprireAutomata = view.findViewById(R.id.oprire_automata);
        aplicaStartup = view.findViewById(R.id.aplica_setari_startup);
        save = view.findViewById(R.id.save_door_events);

        /*try {
            main.restoreDoorEvents();
        } catch (IOException e) {
            Log.println(Log.ASSERT, "Eroare", "IOException");
        } catch (ClassNotFoundException e) {
            Log.println(Log.ASSERT, "Eroare", "ClassNotFoundException");
        }*/

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendDoorCommands();
                    main.saveDoorEvents();

                } catch (IOException | NullPointerException e) {
                    Log.println(Log.ASSERT, "Error", "Salvare door events IOException");
                }
            }
        });

        aprindereAutomata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aprindereAutomata.isChecked()) {
                    try {
                        main.write("J01\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        main.write("J00\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    main.saveDoorEvents();
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }
                Log.println(Log.ASSERT, "Aprindere automata", aprindereAutomata.isChecked() + "");
            }
        });


        oprireAutomata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (oprireAutomata.isChecked()) {
                    try {
                        main.write("J11\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        main.write("J10\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    main.saveDoorEvents();
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }
                Log.println(Log.ASSERT, "Oprire automata", oprireAutomata.isChecked() + "");
            }
        });

        aplicaStartup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    main.saveDoorEvents();
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });

        //</editor-fold>
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        //<editor-fold desc="body" defaultstate="collapsed">
        super.onViewStateRestored(savedInstanceState);
        try {
            main.restoreDoorEvents();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //</editor-fold>
    }

    public static void setDrawableFilterColor(Context context, int colorResource, Drawable drawable) {
        //noinspection ResourceType
        int filterColor = colorResource;
        drawable.setColorFilter(new PorterDuffColorFilter(filterColor, PorterDuff.Mode.MULTIPLY));
    }

    public void nightModePreset(View v) {
        ((ImageView) (v.findViewById(R.id.icon))).setImageResource(R.drawable.door_event_night);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            aprindereAutomata.setBackground(new ColorDrawable(App.NIGHT_MODE_BUTTON_BACKGROUND));
        }
        aprindereAutomata.setTextColor(App.NIGHT_MODE_TEXT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            oprireAutomata.setBackground(new ColorDrawable(App.NIGHT_MODE_BUTTON_BACKGROUND));
        }
        oprireAutomata.setTextColor(App.NIGHT_MODE_TEXT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            aplicaStartup.setBackground(new ColorDrawable(App.NIGHT_MODE_BUTTON_BACKGROUND));
        }
        aplicaStartup.setTextColor(App.NIGHT_MODE_TEXT);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //<editor-fold desc="body" defaultstate="collapsed">
        Log.println(Log.ASSERT, "Fragment", "Nou doorfragment");
        View view = inflater.inflate(R.layout.fragment_door_events, container, false);
        mainInit(view);
        if (App.NIGHT_MODE_ENABLED){
            nightModePreset(view);
        }
        return view;
        //</editor-fold>
    }

}
