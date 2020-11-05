package com.example.smarthomesystem;

import java.io.IOException;
import java.io.Serializable;

/**
 * Clasa pentru serializarea animatiei fastled
 * @author Manel
 * @since 06/02/2020-15:51
 */
public class ARGBFastLedProfile extends ARGBProfile implements Serializable {

    //<editor-fold desc="Variabile specifice" defaultstate="collapsed">
    int intensitate=255;
    //</editor-fold>

    public ARGBFastLedProfile(){}

    public ARGBFastLedProfile(int viteza,int tip,int intensitate){
        this.viteza=viteza;
        this.tip=tip;
        this.intensitate=intensitate;
    }
    @Override
    public void save() throws IOException {
        this.name=Argb_customizeFastLedFragment.PROFILE_NAME;
        super.save();
    }
}
