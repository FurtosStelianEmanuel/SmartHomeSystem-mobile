package com.example.smarthomesystem;

import java.io.IOException;
import java.io.Serializable;

/**
 * Clasa pentru stocarea animatiei cu steagul romaniei
 */
public class ARGBRomanianFlagProfile extends ARGBProfile implements Serializable {
    //<editor-fold desc="Variabile" defaultstate="collapsed">
    int vitezaAnimatie, nrSteaguri, latimeSteaguri, directie, orientare, intensitate;
    //</editor-fold>

    public ARGBRomanianFlagProfile(){}


    public ARGBRomanianFlagProfile(
            int vitezaAnimatie, int nrSteaguri, int latimeSteaguri, int directie, int orientare, int intensitate
    ) {
        this.vitezaAnimatie = vitezaAnimatie;
        this.nrSteaguri = nrSteaguri;
        this.latimeSteaguri = latimeSteaguri;
        this.directie = directie;
        this.orientare = orientare;
        this.intensitate = intensitate;
    }
    @Override
    public void save() throws IOException {
        name=Argb_customizeRomanianFlagFragment.PROFILE_NAME;
        super.save();
    }
}
