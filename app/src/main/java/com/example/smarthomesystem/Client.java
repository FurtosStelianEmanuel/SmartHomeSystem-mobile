package com.example.smarthomesystem;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


/**
 * Clasa folosita pentru a trimite date prin intermediul unui socket la PC
 * @author Manel
 * @since 06/02/2020-16:06
 */
public class Client {

    //<editor-fold desc="Variabile" defaultstate="collapsed">
    private Socket clientSocket;
    public OutputStream out;
    public InputStream in;
    public BufferedReader reader;
    //</editor-fold>

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Client(String ip, int port) throws IOException {
        //<editor-fold desc="body" defaultstate="collapsed">
        clientSocket = new Socket(ip, port);
        out = clientSocket.getOutputStream();
        in = clientSocket.getInputStream();
        reader= new BufferedReader(
                new InputStreamReader(new BufferedInputStream(in), StandardCharsets.UTF_8));
        //</editor-fold>
    }

    @Deprecated
    public void sendRequest(){
        //<editor-fold desc="body" defaultstate="collapsed">
        /* try {
            Log.println(Log.ASSERT,"strigare","requst");
            out.write(("q\n").getBytes());
            Log.println(Log.ASSERT,"asteptam","requst");
            String x=reader.readLine();

            return x;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;*/
        try {
            out.write(("q\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String line=reader.readLine();
            Log.println(Log.ASSERT,"citit",line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //</editor-fold>
    }

    /*public static void main(String[] args) throws IOException {
        client c=new client("127.0.0.1",153);
        String response = c.sendMessage("hello server");
        System.out.println("am raspuns de la serverina "+response);
    }*/
}
