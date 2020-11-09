package com.example.smarthomesystem;


import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentOnConnectAutomation extends Fragment {

    private static class Time implements Serializable {
        int ora, minut;

        public Time(int ora, int minut) {
            set(ora, minut);
        }

        /**
         * @param time format HH:mm
         */
        public Time(String time) {
            String ar[] = time.split(":");
            this.ora = Integer.valueOf(ar[0]);
            this.minut = Integer.valueOf(ar[1]);
        }

        public final void set(int ora, int minut) {
            this.ora = ora;
            this.minut = minut;
        }

        public String toString() {
            return (ora < 10 ? "0" + ora : ora + "") + ":" + (minut < 10 ? "0" + minut : minut + "");
        }

    }

    private Time disconnectTime;

    public void notifyConnectionLost() {
        disconnectTime = new Time(FragmentOnConnectAutomation.CURRENT_TIME());
    }

    public static class Store implements Serializable {

        Time oraStart, oraStop;
        /**
         * 'aprinde luminile doar daca am fost plecat mai mult de 20 de minute'
         */
        int timeout;
        boolean enabled;

        public Store(Time oraStart, Time oraStop, int timeout, boolean enabled) {
            this.oraStart = oraStart;
            this.oraStop = oraStop;
            this.timeout = timeout;
            this.enabled = enabled;
        }

        static Store getDefault() {
            return new Store(new Time(22, 00), new Time(23, 59), 20, false);
        }

    }

    public static int getMinuteDifference(String t1, String t2) throws ParseException {

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(new SimpleDateFormat("HH:mm").parse(t1));

        Calendar stopCalendar = Calendar.getInstance();
        stopCalendar.setTime(new SimpleDateFormat("HH:mm").parse(t2));

        Calendar diffCalendar = Calendar.getInstance();
        diffCalendar.setTime(new Date(stopCalendar.getTimeInMillis() - startCalendar.getTimeInMillis() - 7200000));

        int ore = diffCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = diffCalendar.get(Calendar.MINUTE);
        int total = minute;
        while (ore > 0) {
            total += 60;
            ore--;
        }
        return total;
    }

    public static boolean timeoutChecked(String disconnectTime, String currentTime, int timeout) throws ParseException {
        return getMinuteDifference(disconnectTime, currentTime) >= timeout;
    }


    public static boolean isSecondDay(String startTime, String stopTime) throws ParseException {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(new SimpleDateFormat("HH:mm").parse(startTime));
        Calendar stopCalendar = Calendar.getInstance();
        stopCalendar.setTime(new SimpleDateFormat("HH:mm").parse(stopTime));
        return stopCalendar.before(startCalendar);
    }

    public static boolean isBetweenHours(String startTime, String stopTime, String currentTime) throws ParseException {

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(new SimpleDateFormat("HH:mm").parse(startTime));
        startCalendar.add(Calendar.DATE, 1);

        Calendar stopCalendar = Calendar.getInstance();
        stopCalendar.setTime(new SimpleDateFormat("HH:mm").parse(stopTime));
        stopCalendar.add(Calendar.DATE, 1);

        Calendar calendarCurrent = Calendar.getInstance();
        calendarCurrent.setTime(new SimpleDateFormat("HH:mm").parse(currentTime));
        calendarCurrent.add(Calendar.DATE, 1);

        Date x = calendarCurrent.getTime();

        if (isSecondDay(startTime, stopTime)) {
            return x.after(startCalendar.getTime()) || x.compareTo(startCalendar.getTime()) == 0
                    || x.before(stopCalendar.getTime()) || x.compareTo(stopCalendar.getTime()) == 0;
        } else {
            return (x.after(startCalendar.getTime()) || x.compareTo(startCalendar.getTime()) == 0)
                    && (x.before(stopCalendar.getTime()) || x.compareTo(stopCalendar.getTime()) == 0);
        }
    }

    public static String CURRENT_TIME() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    //<editor-fold desc="Varibaible" defaultstate="collapsed">
    /**
     * Initializat in {@link MainActivity}
     */
    public static Store ON_CONNECT_AUTOMATION_SETTINGS;
    MainActivity main;
    TimePickerDialog intervalStartDialog, intervalStopDialog;
    Switch enabledSwitch;
    TextView intervalOrarTextView, timeoutTextView;
    //</editor-fold>

    public FragmentOnConnectAutomation() {
        // Required empty public constructor
    }

    public FragmentOnConnectAutomation(MainActivity main) {
        this.main = main;
    }

    public void nightModePreset(View v) {
        ((ImageView) (v.findViewById(R.id.icon2))).setImageResource(R.drawable.icon_on_connect_automatizare_night);
        ((TextView) (v.findViewById(R.id.textView15))).setTextColor(App.NIGHT_MODE_TEXT);
        ((TextView) (v.findViewById(R.id.textView18))).setTextColor(App.NIGHT_MODE_TEXT);

        v.findViewById(R.id.reconectare_lumini_switch).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((Switch) (v.findViewById(R.id.reconectare_lumini_switch))).setTextColor(App.NIGHT_MODE_TEXT);

        v.findViewById(R.id.modifica_interval_orar).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((Button) (v.findViewById(R.id.modifica_interval_orar))).setTextColor(App.NIGHT_MODE_TEXT);

        v.findViewById(R.id.modifica_minute_plecate).setBackgroundColor(App.NIGHT_MODE_BUTTON_BACKGROUND);
        ((Button) (v.findViewById(R.id.modifica_minute_plecate))).setTextColor(App.NIGHT_MODE_TEXT);
    }


    public void setIntervalOrarText(Time oraStart, Time oraStop) {
        intervalOrarTextView.setText("Aprinde luminile doar in intervalul orar " + oraStart.toString() + "-" + oraStop.toString());
    }

    public void setTimeoutText(int timeout) {
        timeoutTextView.setText("Aprinde luminile doar daca am fost plecat mai mult de " + timeout + " " + (timeout == 1 ? "minut" : "minute"));
    }

    /**
     * Apelat cand UI-ul a fost actualizat sub vreo forma (switch,timepicker), actualizeaza si textul pentru textView-uri
     * Actualizeaza {@link #ON_CONNECT_AUTOMATION_SETTINGS} si apeleaza {@link MainActivity#saveOnConnectAutomationSettings()}
     */
    public void onChange(Time startTime, Time stopTime, int timeout) {
        if (startTime != null) {
            ON_CONNECT_AUTOMATION_SETTINGS.oraStart = startTime;
        }
        if (stopTime != null) {
            ON_CONNECT_AUTOMATION_SETTINGS.oraStop = stopTime;
        }
        if (timeout >= 0) {
            ON_CONNECT_AUTOMATION_SETTINGS.timeout = timeout;
        }
        ON_CONNECT_AUTOMATION_SETTINGS.enabled = enabledSwitch.isChecked();
        //inca pentru timeout nu este un UI

        setIntervalOrarText(ON_CONNECT_AUTOMATION_SETTINGS.oraStart, ON_CONNECT_AUTOMATION_SETTINGS.oraStop);
        setTimeoutText(ON_CONNECT_AUTOMATION_SETTINGS.timeout);

        try {
            main.saveOnConnectAutomationSettings();
        } catch (IOException e) {
            Log.println(Log.ASSERT, "EROARE", "Nu am putut salva setarile pentru onConnectAutomation");
            e.printStackTrace();
        }
    }

    public boolean shouldAutoLightOn() {
        try {
            return
                    ON_CONNECT_AUTOMATION_SETTINGS.enabled
                            && FragmentOnConnectAutomation.isBetweenHours(ON_CONNECT_AUTOMATION_SETTINGS.oraStart.toString(), ON_CONNECT_AUTOMATION_SETTINGS.oraStop.toString(), FragmentOnConnectAutomation.CURRENT_TIME())
                            && FragmentOnConnectAutomation.timeoutChecked(disconnectTime.toString(), FragmentOnConnectAutomation.CURRENT_TIME(), ON_CONNECT_AUTOMATION_SETTINGS.timeout);
        } catch (ParseException ex) {
            Log.println(Log.ASSERT, "eroare", ex.toString());
            ex.printStackTrace();
            return false;
        }
    }

    public void arduinoConnected() {
        //Log.println(Log.ASSERT,"interval",ON_CONNECT_AUTOMATION_SETTINGS.ora);
        if (shouldAutoLightOn()) {
            main.pcReader.aprindeCuloareUsa();
            Log.println(Log.ASSERT, "ATENTIE", "A fost pornita lumina din OnConnectAutomation");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_fragment_on_connect_automation, container, false);

        final NumberPickerDialog numberPickerDialog = new NumberPickerDialog();
        numberPickerDialog.setValueChangeListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                onChange(null,null,newVal);
            }
        });

        intervalStartDialog = new TimePickerDialog(v.getContext(), AlarmFragment.getTimePickerDialogType(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Toast.makeText(view.getContext(), "Setează ora de final", Toast.LENGTH_SHORT).show();
                        intervalStopDialog.show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            onChange(new Time(view.getHour(), view.getMinute()), null, -1);
                        }
                    }
                },
                0, 0, true);
        intervalStopDialog = new TimePickerDialog(v.getContext(), AlarmFragment.getTimePickerDialogType(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            onChange(null, new Time(view.getHour(), view.getMinute()), -1);
                        }
                    }
                },
                0, 0, true);
        v.findViewById(R.id.modifica_interval_orar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _v) {
                Toast.makeText(_v.getContext(), "Setează ora de început", Toast.LENGTH_SHORT).show();
                intervalStartDialog.show();
            }
        });
        v.findViewById(R.id.modifica_minute_plecate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFragmentManager() != null) {
                    numberPickerDialog.show(getFragmentManager(),"numberpicker");
                }else{
                    Log.println(Log.ASSERT,"eroare","afisare numberpicker");
                }
            }
        });

        enabledSwitch = v.findViewById(R.id.reconectare_lumini_switch);
        intervalOrarTextView = v.findViewById(R.id.textView15);
        timeoutTextView = v.findViewById(R.id.textView18);

        enabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onChange(null, null, -1);
            }
        });

        enabledSwitch.setChecked(ON_CONNECT_AUTOMATION_SETTINGS.enabled);
        setIntervalOrarText(ON_CONNECT_AUTOMATION_SETTINGS.oraStart, ON_CONNECT_AUTOMATION_SETTINGS.oraStop);
        setTimeoutText(ON_CONNECT_AUTOMATION_SETTINGS.timeout);

        if (App.NIGHT_MODE_ENABLED) {
            nightModePreset(v);
        }
        return v;
    }

}
