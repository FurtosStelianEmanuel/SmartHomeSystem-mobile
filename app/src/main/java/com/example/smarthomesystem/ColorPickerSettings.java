package com.example.smarthomesystem;


import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.OnColorSelectedListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;


/**
 * Interfata pentru sectiunea 'Setari color picker'
 *
 * @author Manel
 * @since 06/02/2020-16:07
 */
public class ColorPickerSettings extends Fragment {

    //<editor-fold desc="Variabile">
    MainActivity main;
    SeekBar densitate;
    FloatingActionButton backButton;
    ColorPickerView colorPicker;
    //</editor-fold>

    public ColorPickerSettings(MainActivity main) {
        this.main=main;
    }

    public void nightModePreset(View v){
        ((ImageView)(v.findViewById(R.id.imageView))).setImageResource(R.drawable.ic_color_lens_black_night_24dp);
        ((TextView) (v.findViewById(R.id.textView4))).setTextColor(App.NIGHT_MODE_TEXT);
        backButton.setBackgroundTintList(ColorStateList.valueOf(App.NIGHT_MODE_BUTTON_BACKGROUND));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //<editor-fold desc="body">
        View view=inflater.inflate(R.layout.fragment_color_picker_settings, container, false);
        colorPicker=view.findViewById(R.id.settings_colorPicker);

        densitate=view.findViewById(R.id.densitate);
        colorPicker.setDensity(main.colorPicker.density);
        densitate.setProgress(main.colorPicker.density);
        backButton=view.findViewById(R.id.floatingActionButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.openFragment(null);
            }
        });
        densitate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                colorPicker.setDensity(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                main.colorPicker.setDensity(seekBar.getProgress());
                try {
                    main.saveColorPicker();
                } catch (IOException e) {
                    Log.println(Log.ASSERT,"Eroare","Nu am putut salva colorPicker.svt");
                }
            }
        });
        //densitate.setProgress(main.colorPicker.density);
        //colorPicker.updateColorWheel();
        if (App.NIGHT_MODE_ENABLED){
            nightModePreset(view);
        }
        return view;
        //</editor-fold>
    }

}
