package com.example.smarthomesystem;

import java.io.Serializable;

/**
 * Clasa de stocare a prioritatii led-urilor
 *
 * @author Manel
 * @since 06/02/2020-16:25
 */
public class StoreClass implements Serializable {
    int value=MainActivity.PRIORITATE_STRIP_NORMAL;
    boolean darkMode=false;
}
