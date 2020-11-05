package com.example.smarthomesystem;

import java.io.Serializable;

/**
 * Clasa basic pentru a stoca culorile selectate si manipularea acestora
 *
 * @author Manel
 * @since 06/02/2020-16:26
 */
public class SystemColor implements Serializable {

    int red,green,blue;

    public int getRed(){
        return red;
    }
    public int getGreen(){
        return green;
    }
    public int getBlue(){
        return blue;
    }

    public SystemColor(int red, int green, int blue) {
        this.red=red;
        this.green=green;
        this.blue=blue;
    }
    public void set(int red,int green,int blue){
        this.red=red;
        this.green=green;
        this.blue=blue;
    }
    public SystemColor(){

    }
}
