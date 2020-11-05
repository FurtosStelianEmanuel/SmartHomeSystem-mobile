package com.example.smarthomesystem;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.AlarmClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.Context.AUDIO_SERVICE;


/**
 * Interfata cu componentele vizuale ale sectiunii 'Alarma'
 * @author Manel
 * @since 06/02/2020-14:15
 */
public class AlarmFragment extends Fragment {

    //<editor-fold desc="Variabile">
    static MainActivity main;
    Switch alarma_switch;
    Switch quotes_switch;
    Switch sound_switch;
    Button ringtoneButton;
    FloatingActionButton setari;
    TimePickerDialog dialog;
    Button butonCuloare;
    Switch alarmaLinistitaSwitch;
    int ora = 0;
    int minut = 0;
    SeekBar volumeSlider;
    int selectedSong = 0;
    View alarmView;
    ImageView imagine;
    AlertDialog.Builder builderSingle;
    ArrayAdapter<String> choices;
    String[] choicesString = {"Random", "Iron Maiden - Blood brothers", "Pantera - Hollow", "Ozzy Osbourne - No more tears", "RAGE - Paint it black",
            "Slayer - Raining blood", "Dmitri Shostakovich - Waltz No. 2", "Sepultura - Territory", "Dio - The last in line", "Judas Priest - Victim of changes",
            "Queen - We are the champions"};
    //</editor-fold>

    public AlarmFragment() {
        // Required empty public constructor
    }

    public AlarmFragment(MainActivity main) {
        AlarmFragment.main = main;
    }

    void saveAlarm() throws IOException {
        //<editor-fold desc="body">
        FileOutputStream fos = main.openFileOutput("alarma.svt", Context.MODE_PRIVATE);
        List<String> x = new ArrayList<>();
        ObjectOutputStream os = new ObjectOutputStream(fos);
        ColorDrawable c = (ColorDrawable) (butonCuloare.getBackground());
        x.add(ora + "");
        x.add(minut + "");
        x.add(c.getColor() + "");
        x.add(Boolean.toString(alarma_switch.isChecked()));
        x.add(Boolean.toString(quotes_switch.isChecked()));
        x.add(Boolean.toString(sound_switch.isChecked()));
        x.add(Integer.toString(volumeSlider.getProgress()));
        x.add(Integer.toString(selectedSong));
        x.add(Boolean.toString(alarmaLinistitaSwitch.isChecked()));
        os.writeObject(x);
        os.close();
        fos.close();
        Log.println(Log.ASSERT, main.getFilesDir().toString(), "Am salvat fisierul alarma.svt");
        //</editor-fold>
    }

    /**
     *
     * @return {@link List<String>} ce contine toate datele legate de o anumita alarma in urmatoarea ordine:
     *          (int)ora,(int)minut,(int)culoare,(boolean)alarma_activa,quotes_enabled,sound_enabled,(int)volum,(int)indexPiesaselectata,
     *          (boolean)alarmaLinistita
     * @throws IOException Daca nu esti baiat cuminte
     */
    List<String> loadAlarms() throws IOException {
        //<editor-fold desc="body">
        List<String> simpleClass = null;
        try {
            FileInputStream fis = main.openFileInput("alarma.svt");
            ObjectInputStream is = new ObjectInputStream(fis);
            simpleClass = (ArrayList<String>) is.readObject();

            is.close();
            fis.close();
        } catch (Exception ex) {
            Log.println(Log.ASSERT, "Eroare", "Bai la fisierul alarma.svt");
        }
        return simpleClass;
        //</editor-fold>
    }
    /**
     *
     * @return {@link List<String>} ce contine toate datele legate de o anumita alarma in urmatoarea ordine:
     *          (int)ora,(int)minut,(int)culoare,(boolean)alarma_activa,quotes_enabled,sound_enabled,(int)volum,(int)indexPiesaselectata,
     *          (boolean)alarmaLinistita
     * @throws IOException Daca nu esti baiat cuminte
     */
    static List<String> loadAlarmStatic() throws IOException {
        //<editor-fold desc="body">
        List<String> simpleClass = null;
        try {
            FileInputStream fis = main.openFileInput("alarma.svt");
            ObjectInputStream is = new ObjectInputStream(fis);
            simpleClass = (ArrayList<String>) is.readObject();

            is.close();
            fis.close();
        } catch (Exception ex) {
            Log.println(Log.ASSERT, "Eroare", "Bai la fisierul alarma.svt");
        }
        return simpleClass;
        //</editor-fold>
    }
    /**
     *
     * @return {@link List<String>} ce contine toate datele legate de o anumita alarma in urmatoarea ordine:
     *          (int)ora,(int)minut,(int)culoare,(boolean)alarma_activa,quotes_enabled,sound_enabled,(int)volum,(int)indexPiesaselectata,
     *          (boolean)alarmaLinistita
     * @throws IOException Daca nu esti baiat cuminte
     */
    static List<String> loadAlarmStatic(Context context) throws IOException {
        //<editor-fold desc="body">
        List<String> simpleClass = null;
        try {
            FileInputStream fis = context.openFileInput("alarma.svt");
            ObjectInputStream is = new ObjectInputStream(fis);
            simpleClass = (ArrayList<String>) is.readObject();
            is.close();
            fis.close();
        } catch (Exception ex) {
            Log.println(Log.ASSERT, "Eroare", "Bai la fisierul alarma.svt");
        }
        return simpleClass;
        //</editor-fold>
    }

    /**
     * Seteaza o alarma dupa ora si minutele specificate
     * @param ora
     * @param minut
     * @param context
     * @throws IOException
     */
    static void alarmaAsistent_LA(int ora, int minut, Context context) throws IOException {
        //<editor-fold desc="body">
        List<String> datePrev = loadAlarmStatic();
        FileOutputStream fos = main.openFileOutput("alarma.svt", Context.MODE_PRIVATE);
        List<String> x = new ArrayList<>();
        ObjectOutputStream os = new ObjectOutputStream(fos);
        x.add(ora + "");
        x.add(minut + "");
        x.add(datePrev.get(2));
        x.add(Boolean.toString(true));
        x.add(datePrev.get(4));
        x.add(datePrev.get(5));
        x.add(datePrev.get(6));
        x.add(datePrev.get(7));
        x.add(datePrev.get(8));
        os.writeObject(x);
        os.close();
        fos.close();
        Log.println(Log.ASSERT, main.getFilesDir().toString(), "Am salvat fisierul alarma.svt");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            seteazaAlarmaMain(context);
        } else {
            Log.println(Log.ASSERT, "ERROR", "Nu am putut seta alarma cu asistentul");
        }
        //</editor-fold>
    }

    /**
     * Seteaza o alarma la ora si minutele specificate
     * @param ora
     * @param minut
     * @param context
     * @throws IOException
     */
    static void alarmaAsistent_IN(int ora, int minut, Context context) throws IOException {
        //<editor-fold desc="body">
        List<String> datePrev = loadAlarmStatic();
        long milis = TimeUnit.HOURS.toMillis(ora) + TimeUnit.MINUTES.toMillis(minut);
        Calendar currentTime = Calendar.getInstance();
        int oraCurenta = currentTime.get(Calendar.HOUR_OF_DAY);
        int minutCurent = currentTime.get(Calendar.MINUTE);
        Log.println(Log.ASSERT, "TIMP", "ORA CURENTA : " + oraCurenta + " minute : " + minutCurent);
        ora += oraCurenta;
        minut += minutCurent;
        while (minut > 60) {
            Log.println(Log.ASSERT, "OV", ora + " " + minut);
            minut -= 60;
            ora++;
            Log.println(Log.ASSERT, "OV1", ora + " " + minut);
        }
        ora %= 24;
        FileOutputStream fos = main.openFileOutput("alarma.svt", Context.MODE_PRIVATE);
        List<String> x = new ArrayList<>();
        ObjectOutputStream os = new ObjectOutputStream(fos);
        x.add(ora + "");
        x.add(minut + "");
        x.add(datePrev.get(2));
        x.add(Boolean.toString(true));
        x.add(datePrev.get(4));
        x.add(datePrev.get(5));
        x.add(datePrev.get(6));
        x.add(datePrev.get(7));
        x.add(datePrev.get(8));
        os.writeObject(x);
        os.close();
        fos.close();
        Log.println(Log.ASSERT, main.getFilesDir().toString(), "Am salvat fisierul alarma.svt");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            seteazaAlarmaMain(context);
        } else {
            Log.println(Log.ASSERT, "ERROR", "Nu am putut seta alarma cu asistentul");
        }
        //</editor-fold>
    }

    /**
     * Ca sa arate textul sub format HH:MM
     * @param ora_
     * @param minut_
     */
    void setareText(int ora_, int minut_) {
        //<editor-fold desc="body">
        alarma_switch.setText((ora_ < 9 ? (ora_ == 0 ? "00" : "0" + ora_) : ora_) + ":" + (minut_ <= 9 ? (minut_ == 0 ? "00" : "0" + minut_) : minut_));
        //</editor-fold>
    }

    /**
     * Opreste PendingIntent-ul cu requestCode-ul 5 astfel, oprind alarma
     * @param context
     */
    void cancelAlarm(Context context) {
        //<editor-fold desc="body">
        AlarmReceiver alarmReceiver = null;
        AlarmManager am = (AlarmManager) main.getSystemService(Context.ALARM_SERVICE);
        AlarmReceiver.main = main;
        Intent in = new Intent(main, AlarmReceiver.class);
        PendingIntent pin = PendingIntent.getBroadcast(main, 5, in, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //am.setExact(AlarmManager.RTC_WAKEUP, TimeUnit.MINUTES.toMillis(TimeUnit.MILLISECONDS.toMinutes(totalWait + System.currentTimeMillis())), pin);
            am.cancel(pin);
            Toast.makeText(main, "Alarma anulata", Toast.LENGTH_SHORT).show();
        }
        //</editor-fold>
    }

    public static int getTimePickerDialogType(){
        return App.NIGHT_MODE_ENABLED ? R.style.ThemeOverlay_MaterialComponents_Dark:R.style.ThemeOverlay_AppCompat_Dialog;
    }

    Handler mHandler = new Handler(Looper.getMainLooper()) {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void handleMessage(Message message) {
            //<editor-fold desc="body">
            Log.println(Log.ASSERT, "Fragment", "Recreare AlarmFragment");
            alarma_switch = alarmView.findViewById(R.id.switch1);
            quotes_switch = alarmView.findViewById(R.id.quotes);
            sound_switch = alarmView.findViewById(R.id.sunet);
            imagine = alarmView.findViewById(R.id.imageView2);
            ringtoneButton = alarmView.findViewById(R.id.ringtone);
            choices = new ArrayAdapter<String>(main, android.R.layout.select_dialog_singlechoice);
            volumeSlider = alarmView.findViewById(R.id.volum_slider);
            builderSingle = new AlertDialog.Builder(main,getTimePickerDialogType());
            alarmaLinistitaSwitch = alarmView.findViewById(R.id.mild_alarm);
            AudioManager mAudioManager = (AudioManager) main.getSystemService(Context.AUDIO_SERVICE);
            volumeSlider.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            //builderSingle.setIcon(R.drawable.bluetooth);
            builderSingle.setTitle("Piesa la declanșarea alarmei");
            for (String s : choicesString)
                choices.add(s);
            builderSingle.setAdapter(choices, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ringtoneButton.setText(choices.getItem(which));
                    selectedSong = which;
                    try {
                        saveAlarm();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            volumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (am != null) {
                        am.setStreamVolume(
                                AudioManager.STREAM_MUSIC,
                                progress,
                                0);
                    }

                }

                AudioManager am;

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    try {
                        List<String> date = loadAlarms();
                        AlarmReceiver.play(date);
                        am = (AudioManager) main.getSystemService(AUDIO_SERVICE);
                        /**/
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    Log.println(Log.ASSERT, "Slider", seekBar.getProgress() + "");
                    try {
                        saveAlarm();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    AlarmReceiver.stopPlayer();
                }
            });

            ringtoneButton.setOnClickListener(new View.OnClickListener() {

                Handler handler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        builderSingle.show();
                    }
                };

                @Override
                public void onClick(View v) {
                    new Thread() {
                        public void run() {
                            Message message = handler.obtainMessage(Message.CONTENTS_FILE_DESCRIPTOR, new Message());
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            message.sendToTarget();
                        }
                    }.start();
                }
            });

            quotes_switch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        saveAlarm();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            alarmaLinistitaSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        saveAlarm();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            sound_switch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        saveAlarm();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            setari = alarmView.findViewById(R.id.floatingActionButton2);
            butonCuloare = alarmView.findViewById(R.id.buton_culoare_alarma);

            alarma_switch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (alarma_switch.isChecked()) {
                        seteazaAlarma(alarmView.getContext());
                    } else {
                        cancelAlarm(alarmView.getContext());
                    }
                    try {
                        saveAlarm();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            butonCuloare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.println(Log.ASSERT, "BA", "m ai clickarit");
                    ColorPickerDialogBuilder
                            .with(main,getTimePickerDialogType())
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
                                        MainActivity.Values.set(red,green,blue);
                                        try {
                                            main.write(MainActivity.Values.impachetatBytes((byte)'G'));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Log.println(Log.ASSERT, "ERROR", "Incompatibilitate versiune android, min OREO");
                                    }

                                }
                            })
                            .setPositiveButton("ok", new ColorPickerClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                    Log.println(Log.ASSERT, "On click", "#" + selectedColor);
                                    int color = selectedColor;
                                    butonCuloare.setBackgroundColor(selectedColor);
                                    try {
                                        saveAlarm();
                                    } catch (IOException e) {
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

            TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    Log.println(Log.ASSERT, "BA", "A ales " + hourOfDay + " " + minute);
                    ora = hourOfDay;
                    minut = minute;
                    setareText(ora, minut);
                    try {
                        saveAlarm();
                        alarma_switch.setChecked(true);
                        seteazaAlarma(alarmView.getContext());
                    } catch (IOException e) {
                        Log.println(Log.ASSERT, "Eroare", "La saveAlarm()");
                    }
                }
            };

            dialog = new TimePickerDialog(alarmView.getContext(),getTimePickerDialogType(), listener, 0, 0, true);


            setari.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.show();
                }
            });
            imagine.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.show();
                }
            });
            try {
                List<String> alarma = loadAlarms();
                if (alarma != null) {
                    Log.println(Log.ASSERT, "Am gasit alarma", "Marime: " + alarma.size() + " CONTINUT " + alarma);
                    ora = Integer.valueOf(alarma.get(0));
                    minut = Integer.valueOf(alarma.get(1));
                    butonCuloare.setBackgroundColor(Integer.valueOf(alarma.get(2)));
                    alarma_switch.setChecked(Boolean.valueOf(alarma.get(3)));
                    quotes_switch.setChecked(Boolean.valueOf(alarma.get(4)));
                    sound_switch.setChecked(Boolean.valueOf(alarma.get(5)));
                    volumeSlider.setProgress(Integer.valueOf(alarma.get(6)));
                    ringtoneButton.setText(choicesString[Integer.valueOf(alarma.get(7))]);
                    selectedSong = Integer.valueOf(alarma.get(7));
                    alarmaLinistitaSwitch.setChecked(Boolean.valueOf(alarma.get(8)));
                    setareText(ora, minut);
                } else {
                    //Toast.makeText(main,"Nu ai setat nicio alarma",Toast.LENGTH_SHORT);
                    Log.println(Log.ASSERT, "eroare", "alarma absenta");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.println(Log.ASSERT, "Eroare ", "Aplicare vizuala " + e.toString());
            }
            //</editor-fold>
        }
    };

    /**
     * Functie apelata la inceput de program pentru a asigura faptul ca pendingintent-ul este setat si ca alarmmanger o sa sune
     * ce trebuie
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    static void seteazaAlarmaMain(Context context) {
        //<editor-fold desc="scurt" defaultstate="collapsed">
        Calendar currentTime = Calendar.getInstance();

        int ora_curenta = currentTime.get(Calendar.HOUR_OF_DAY);
        int minut_curent = currentTime.get(Calendar.MINUTE);
        Log.println(Log.ASSERT, "Ora curent", " " + ora_curenta + ":" + minut_curent);
        CharSequence text = "";


        String dateStart = ora_curenta + ":" + minut_curent;

        int ora_ = 0, minut_ = 0;

        try {
            List<String> date = loadAlarmStatic();
            ora_ = Integer.valueOf(date.get(0));
            minut_ = Integer.valueOf(date.get(1));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String dateStop = ora_ + ":" + minut_;


        SimpleDateFormat format = new SimpleDateFormat("HH:mm");

        Date d1 = null;
        Date d2 = null;
        try {
            d1 = format.parse(dateStart);
            d2 = format.parse(dateStop);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diff = d2.getTime() - d1.getTime();
        try {
            Date d = format.parse("25:60");
            Log.println(Log.ASSERT, "DIF", diff + " " + d.getTime());
            if (diff < 0)
                diff = d.getTime() + diff;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diffMinutes = (diff / (60 * 1000)) % 60;
        long diffHours = (diff / (60 * 60 * 1000)) % 24;
        long totalWait = 0;
       /* if (diffMinutes<0)
            diffMinutes=60-Math.abs(diffMinutes);
        if (diffHours<0)
            diffHours=23-Math.abs(diffHours);*/
        if (diffHours == 0 && diffMinutes == 0) {
            text = " ACUMA E ALARMA";
        } else {
            text = (diffHours == 0 ? "" : (diffHours > 1 ? (" " + diffHours + " ore ") : "tr-o ora")) + (diffMinutes != 0 ? (diffHours != 0 ? " si " : "") : "") +
                    (diffMinutes == 0 ? "" : (diffMinutes == 1 ? "" : " ") + (diffMinutes > 1 ? diffMinutes + (diffMinutes > 19 ? " de" : "") + " minute " : (diffHours == 0 ? "tr-un minut" : " un minut")));
            totalWait = TimeUnit.HOURS.toMillis(diffHours) + TimeUnit.MINUTES.toMillis(diffMinutes);
        }
        Log.println(Log.ASSERT, "cacat", " " + diffHours + ":" + diffMinutes);

        int durationt = Toast.LENGTH_SHORT;


        AlarmReceiver alarmReceiver = null;
        AlarmManager am = (AlarmManager) main.getSystemService(Context.ALARM_SERVICE);
        AlarmReceiver.main = main;
        Intent in = new Intent(main, AlarmReceiver.class);

        PendingIntent pin = PendingIntent.getBroadcast(main, 5, in, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            //long s=System.currentTimeMillis()+diff;
            //Log.println(Log.ASSERT,"seucnde"," "+TimeUnit.MILLISECONDS.toSeconds(d2.getTime()));
            // Log.println(Log.ASSERT,"Timpi",System.currentTimeMillis()+" "+d2.getTime()+" alarma la "+System.currentTimeMillis()+diff+" milisecunde = diferenta in "+TimeUnit.MILLISECONDS.toMinutes(diff)+" " );
            Log.println(Log.ASSERT, "Tag", System.currentTimeMillis() + " " + totalWait + " " + (System.currentTimeMillis() + totalWait));
            long w = totalWait + System.currentTimeMillis();
            Log.println(Log.ASSERT, "Ba", " " + w);
            if (am != null) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, TimeUnit.MINUTES.toMillis(TimeUnit.MILLISECONDS.toMinutes(totalWait + System.currentTimeMillis())), pin);
                Toast toast = Toast.makeText(context, "Alarma in" + text, durationt);
                toast.show();
                /* AlarmManager.AlarmClockInfo inf=new AlarmManager.AlarmClockInfo(TimeUnit.MINUTES.toMillis(TimeUnit.MILLISECONDS.toMinutes(totalWait + System.currentTimeMillis())),pin);
                am.setAlarmClock(inf,pin);*/
            }
        }
        //</editor-fold>
    }

    void seteazaAlarma(Context context) {
        //<editor-fold desc="scurt" defaultstate="collapsed">
        Calendar currentTime = Calendar.getInstance();

        int ora_curenta = currentTime.get(Calendar.HOUR_OF_DAY);
        int minut_curent = currentTime.get(Calendar.MINUTE);
        Log.println(Log.ASSERT, "Ora curent", " " + ora_curenta + ":" + minut_curent);
        CharSequence text = "";


        String dateStart = ora_curenta + ":" + minut_curent;
        String dateStop = ora + ":" + minut;


        SimpleDateFormat format = new SimpleDateFormat("HH:mm");

        Date d1 = null;
        Date d2 = null;
        try {
            d1 = format.parse(dateStart);
            d2 = format.parse(dateStop);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long diff = d2.getTime() - d1.getTime();
        try {
            Date d = format.parse("25:60");
            Log.println(Log.ASSERT, "DIF", diff + " " + d.getTime());
            if (diff < 0)
                diff = d.getTime() + diff;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long diffMinutes = (diff / (60 * 1000)) % 60;
        long diffHours = (diff / (60 * 60 * 1000)) % 24;
        long totalWait = 0;
        /*if (diffMinutes<0)
            diffMinutes=60-Math.abs(diffMinutes);
        if (diffHours<0)
            diffHours=24-Math.abs(diffHours);*/
        if (diffHours == 0 && diffMinutes == 0) {
            text = " ACUMA E ALARMA";
        } else {
            text = (diffHours == 0 ? "" : (diffHours > 1 ? (" " + diffHours + " ore ") : "tr-o ora")) + (diffMinutes != 0 ? (diffHours != 0 ? " si " : "") : "") +
                    (diffMinutes == 0 ? "" : (diffMinutes == 1 ? "" : " ") + (diffMinutes > 1 ? diffMinutes + (diffMinutes > 19 ? " de" : "") + " minute " : (diffHours == 0 ? "tr-un minut" : " un minut")));
            totalWait = TimeUnit.HOURS.toMillis(diffHours) + TimeUnit.MINUTES.toMillis(diffMinutes);
        }
        Log.println(Log.ASSERT, "cacat", " " + diffHours + ":" + diffMinutes);

        int durationt = Toast.LENGTH_SHORT;


        AlarmReceiver alarmReceiver = null;
        AlarmManager am = (AlarmManager) main.getSystemService(Context.ALARM_SERVICE);
        AlarmReceiver.main = main;
        Intent in = new Intent(main, AlarmReceiver.class);

        PendingIntent pin = PendingIntent.getBroadcast(main, 5, in, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.println(Log.ASSERT, "Tag", System.currentTimeMillis() + " " + totalWait + " " + (System.currentTimeMillis() + totalWait));
            long w = totalWait + System.currentTimeMillis();
            Log.println(Log.ASSERT, "Ba", " " + w);
            if (am != null) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, TimeUnit.MINUTES.toMillis(TimeUnit.MILLISECONDS.toMinutes(totalWait + System.currentTimeMillis())), pin);
                Toast toast = Toast.makeText(context, "Alarma in" + text, durationt);
                toast.show();
            }

        }
        //</editor-fold>
    }

    public void nightModePreset(View v){
        ((ImageView)(v.findViewById(R.id.imageView2))).setImageResource(R.drawable.ic_access_alarm_black_night_24dp);
        v.findViewById(R.id.switch1).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((Switch)(v.findViewById(R.id.switch1))).setTextColor(App.NIGHT_MODE_TEXT);

        v.findViewById(R.id.quotes).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((Switch)(v.findViewById(R.id.quotes))).setTextColor(App.NIGHT_MODE_TEXT);

        v.findViewById(R.id.sunet).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((Switch)(v.findViewById(R.id.sunet))).setTextColor(App.NIGHT_MODE_TEXT);

        v.findViewById(R.id.mild_alarm).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((Switch)(v.findViewById(R.id.mild_alarm))).setTextColor(App.NIGHT_MODE_TEXT);

        ((TextView) (v.findViewById(R.id.textView3))).setTextColor(App.NIGHT_MODE_TEXT);

        v.findViewById(R.id.ringtone).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((Button)(v.findViewById(R.id.ringtone))).setTextColor(App.NIGHT_MODE_TEXT);
        ((FloatingActionButton)(v.findViewById(R.id.floatingActionButton2))).setBackgroundTintList(ColorStateList.valueOf(App.NIGHT_MODE_BUTTON_BACKGROUND));
        ((FloatingActionButton)(v.findViewById(R.id.floatingActionButton2))).setImageResource(R.drawable.ic_settings_black_night_24dp);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //<editor-fold desc="body">
        final View view = inflater.inflate(R.layout.fragment_alarma, container, false);
        alarmView = view;

        new Thread() {
            @Override
            public void run() {
                Message message = mHandler.obtainMessage(Message.CONTENTS_FILE_DESCRIPTOR, new Message());
                message.sendToTarget();
            }
        }.start();
        if (App.NIGHT_MODE_ENABLED) {
            nightModePreset(view);
        }
        return view;
        //</editor-fold>
    }

}
