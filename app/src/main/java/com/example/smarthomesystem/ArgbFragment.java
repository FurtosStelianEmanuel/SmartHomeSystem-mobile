package com.example.smarthomesystem;


import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

import static com.example.smarthomesystem.MainActivity.ARGB;


/**
 * Interfata pentru sectiunea 'ARGB'
 * @author Manel
 * @since 06/02/2020
 */
public class ArgbFragment extends Fragment {

    //<editor-fold desc="Variabile" defaultstate="collapsed">
    MainActivity main;
    FloatingActionButton lightOnButton, lightOffButton, customColorButton, romanianFlagButton, fastLedButton;
    ColorPickerView argbColorPicker;
    FloatingActionButton asistent;
    //</editor-fold>

    public ArgbFragment() {
        // Required empty public constructor
    }

    public ArgbFragment(MainActivity main) {
        this.main = main;
    }


    public void nightModePreset(View v){
        asistent.setBackgroundTintList(ColorStateList.valueOf(App.NIGHT_MODE_BUTTON_BACKGROUND));
        ((FloatingActionButton)(v.findViewById(R.id.argb_romanian_flag_button))).setBackgroundTintList(ColorStateList.valueOf(App.NIGHT_MODE_BUTTON_BACKGROUND));
        ((FloatingActionButton)(v.findViewById(R.id.argb_fast_led_button))).setBackgroundTintList(ColorStateList.valueOf(App.NIGHT_MODE_BUTTON_BACKGROUND));
        ((FloatingActionButton)(v.findViewById(R.id.argb_light_on_button))).setBackgroundTintList(ColorStateList.valueOf(App.NIGHT_MODE_BUTTON_BACKGROUND));
        ((FloatingActionButton)(v.findViewById(R.id.argb_light_off_button))).setBackgroundTintList(ColorStateList.valueOf(App.NIGHT_MODE_BUTTON_BACKGROUND));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //<editor-fold desc="body" defaultstate="collapsed">
        View v = inflater.inflate(R.layout.fragment_argb, container, false);


        argbColorPicker=v.findViewById(R.id.argb_color_picker);
        argbColorPicker.setDensity(main.colorPicker.density);
        argbColorPicker.addOnColorChangedListener(new OnColorChangedListener() {
            SystemColor color=new SystemColor(255,144,0);
            @Override
            public void onColorChanged(int selectedColor) {
                color.red=Color.red(selectedColor);
                color.green=Color.green(selectedColor);
                color.blue=Color.blue(selectedColor);
                try{
                    main.write(ARGB.TURN_ON_COLOR_NO_ANIMATION,color);
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        });

        lightOnButton = v.findViewById(R.id.argb_light_on_button);
        lightOnButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                main.openFragment(main.argbCustomizeLightOnLightFragment);
                return false;
            }
        });
        lightOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    main.write(ARGB.TURN_ON_WHITE_LIGHT);
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        });

        lightOffButton = v.findViewById(R.id.argb_light_off_button);
        lightOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    main.write(ARGB.TURN_OFF);
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        });
        lightOffButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                main.openFragment(main.argbCustomizeLightOffFragment);
                return false;
            }
        });

        customColorButton = v.findViewById(R.id.argb_custom_color_button);
        customColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    main.write(ARGB.TURN_ON_CUSTOM_COLOR);
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        });
        customColorButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                main.openFragment(main.argbCustomizeColorFragment);
                return false;
            }
        });
        SystemColor customColor = main.argbCustomizeColorFragment.getColor();
        customColorButton.setBackgroundTintList
        (
                ColorStateList.valueOf(Color.rgb(customColor.getRed(), customColor.getGreen(), customColor.getBlue()))
        );

        romanianFlagButton = v.findViewById(R.id.argb_romanian_flag_button);
        romanianFlagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    main.write(ARGB.ROMANIAN_FLAG_ANIMATION);
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        });
        romanianFlagButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                main.openFragment(main.argbCustomizeRomanianFlagFragment);
                return false;
            }
        });

        fastLedButton = v.findViewById(R.id.argb_fast_led_button);
        fastLedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    main.write(ARGB.FAST_LED_ANIMATION);
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        });
        fastLedButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                main.openFragment(main.argbCustomizeFastLedFragment);
                return false;
            }
        });

        asistent =v.findViewById(R.id.asistent3);
        asistent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.listenEvent();
            }
        });

        if (App.NIGHT_MODE_ENABLED){
            nightModePreset(v);
        }

        return v;
        //</editor-fold>
    }

}
