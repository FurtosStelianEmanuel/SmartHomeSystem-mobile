package com.example.smarthomesystem;

import java.io.IOException;
import java.io.Serializable;

/**
 * Clasa de stocare a profilelor ARGB
 * @author Manel
 * @since 06/02/2020-15:54
 */
public class ARGBProfile implements Serializable {

    //<editor-fold desc="Variabile" defaultstate="collapsed">
    String name = "default";
    int tip = 0, viteza = 255, sectiuni = 4, increment_or_decrement = 3;
    SystemColor color=new SystemColor(255,144,0);
    //</editor-fold>

    public ARGBProfile(){}

    public ARGBProfile(String name, int tip, int viteza, int sectiuni, int increment_or_decrement) {
        this.name = name;
        this.tip = tip;
        this.viteza = viteza;
        this.sectiuni = sectiuni;
        this.increment_or_decrement = increment_or_decrement;
    }
    public ARGBProfile(String name, int tip, int viteza, int sectiuni, int increment_or_decrement,SystemColor color) {
        this.name = name;
        this.tip = tip;
        this.viteza = viteza;
        this.sectiuni = sectiuni;
        this.increment_or_decrement = increment_or_decrement;
        this.color=color;
    }
    public void save() throws IOException {
        AlarmFragment.main.saveARGBProfile(this); //folosim referinta la main din AlarmFragment pentru ca nu am
        // vrut sa creez si aici o referinta proprie
    }

    public static ARGBProfile load(String name) throws IOException, ClassNotFoundException {
        return AlarmFragment.main.loadARGBProfile(name);
    }

}

