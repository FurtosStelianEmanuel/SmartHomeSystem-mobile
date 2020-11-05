package com.example.smarthomesystem;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ddf.minim.analysis.BeatDetect;


/**
 * A simple {@link Fragment} subclass.
 */
public class musicFragment extends Fragment {

    BeatDetect bt;
    public musicFragment() {
        // Required empty public constructor
    }
    MainActivity main;

    public musicFragment(MainActivity main){
        this.main=main;

    }

    int map(double x, double in_min, double in_max, double out_min, double out_max) {
        return (int) ((x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
    }
    float mapf(float x, float in_min, float in_max, float out_min, float out_max) {
        return ((x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
    }
    byte data[];

    Visualizer visualizer;


    /**
     * float lerp(float v0, float v1, float t) {
     *   return (1 - t) * v0 + t * v1;
     * }
     * (1.0F - amount) * start + amount * stop
     * @param value valoarea de la care plecam
     * @param target valoarea finala
     * @param thr threshold de interpolare
     * @return
     */
    float lerp(float value,float target,float thr){
        //return ((1-th)*value+th*target);
        return (float)((value * (1.0 - th)) + (target * thr));
    }
    SeekBar seek;
    Button b;
    float[] magnitudes ;
    float[] pmag;
    float[] phases;
    static int AUDIBLE_FREQ=3;
    float bassThreshold=10;
    int minmag;
    int maxmag;
    class Canvas extends View{
        Paint paint;
        public Canvas(Context context) {
            super(context);
            paint=new Paint();
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.FILL);

            play();
            visualizer = new Visualizer(player.getAudioSessionId());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                visualizer.setScalingMode(Visualizer.SCALING_MODE_NORMALIZED);
            }
            visualizer.setCaptureSize(4096*8);
            visualizer.setEnabled(true);
            Log.println(Log.ASSERT,"Sampling rate",visualizer.getSamplingRate()+"");


            data = new byte[visualizer.getCaptureSize()];
            magnitudes=new float[data.length/2+1];
            pmag=new float[data.length/2+1];
            phases=new float[data.length/2+1];

            setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            setMinimumWidth(300);
            setMinimumHeight(300);
            paint.setStrokeWidth(10);
            spacing=20;
            bt=new BeatDetect(visualizer.getCaptureSize()/2,4410);
            final float dataf[]=new float[magnitudes.length-1];
            bt.detectMode(BeatDetect.FREQ_ENERGY);
            Log.println(Log.ASSERT,"dd",""+bt.dectectSize());
            new Thread() {
                boolean v=false;
                float sum=0;
                float avgBass=0;
                @Override
                public void run() {
                    int cc=0;
                    int counter=0;
                    while(true){

                        visualizer.getFft(data);

                        magnitudes[0] = lerp(magnitudes[0],(float)Math.abs(data[0]),th);
                        magnitudes[magnitudes.length / 2] = lerp(magnitudes[magnitudes.length/2],(float)Math.abs(data[1]),th);
                        phases[0] = phases[data.length / 2] = 0;
                      //  String x="";
                        for (int k = 1; k < magnitudes.length/ 2; k++) {
                            int i = k * 2;
                            float aux=magnitudes[k];
                            float magnitude=(float)Math.hypot(data[i], data[i + 1]);
                            float phase=(float)Math.atan2(data[i + 1], data[i]);
                            phase=(phase>=0 ? (float)Math.toDegrees(phase):(float)Math.toDegrees(Math.PI*2+phase));
                            magnitudes[k] = lerp(magnitudes[k],magnitude,th);
                            phases[k] = lerp(phases[k],phase,th);

                            if (magnitudes[k]>2){
                                AUDIBLE_FREQ=k;
                            }
                            //x+=magnitudes[k]+",";

                        }
                        for (int k=0;k<dataf.length;k++){
                            dataf[k]=magnitudes[k];
                        }
                        bt.detect(dataf);

                        //Log.println(Log.ASSERT,"ecv",bt.isHat() +" "+bt.isKick()+" "+bt.isSnare());
                        if (counter%100==0){
                            avgBass=sum/100;
                            sum=0;
                        }
                        counter++;
                        sum+=magnitudes[0];
                        boolean t=false;
                        if (bt.isSnare()){
                            t=true;
                            b.setBackgroundColor(Color.RED);
                            cc=0;
                        }else{
                            b.setBackgroundColor(Color.WHITE);
                        }
                        boolean r,g,b;
                        r=g=b=false;
                        if (bt.isKick()){
                            t=true;
                            r=true;
                        }else{
                            kick.setBackgroundColor(Color.WHITE);
                        }
                        if (bt.isHat()) {
                            t=true;
                            g=true;
                            hat.setBackgroundColor(Color.RED);
                        } else {
                            b=true;
                            hat.setBackgroundColor(Color.WHITE);
                        }
                        if (t) {
                            general.setBackgroundColor(Color.GREEN);
                        } else {
                            general.setBackgroundColor(Color.WHITE);
                        }

                        previousBass=magnitudes[0];
                        //Log.println(Log.ASSERT,"magnitudes",x);
                       /*
                        float mred=0;
                        float mgreen=0;
                        float mblue=0;
                            int redAmount=0;
                            for (int i=0;i<AUDIBLE_FREQ;i++){
                                redAmount+=magnitudes[i];
                                if (magnitudes[i]>mred)
                                    mred=magnitudes[i];
                            }
                            int greenAmount=0;
                            for (int i=(int)(((double)1/3)*(AUDIBLE_FREQ));i<AUDIBLE_FREQ*2/3;i++){
                                greenAmount+=magnitudes[i];
                                if (magnitudes[i]>mgreen)
                                    mgreen=magnitudes[i];
                            }
                            int blueAmount=0;
                            for (int i=(int)(((double)2/3)*(AUDIBLE_FREQ));i<AUDIBLE_FREQ;i++){
                                blueAmount+=magnitudes[i];
                                if (magnitudes[i]>mblue)
                                    mblue=magnitudes[i];
                            }

                            mred=map(mred,0,200,0,255);
                            mgreen=map(mgreen,0,200,0,255);
                            mblue=map(mblue,0,200,0,255);


                            redAmount/=AUDIBLE_FREQ/3.0;
                            greenAmount/=AUDIBLE_FREQ/3.0;
                            blueAmount/=AUDIBLE_FREQ/3.0;

                            redAmount=map(redAmount,0,200,0,255);
                            greenAmount=map(greenAmount,0,200,0,255);
                            blueAmount=map(blueAmount,0,200,0,255);
                            //main.write(redAmount+" "+greenAmount+" "+blueAmount+"\n");
*/

                        invalidate();
                        try {
                            Thread.sleep(App.ARDUINO_TIMEOUT);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();


        }
        int spacing=6;
        byte pdata[];

        @Override
        protected void onDraw(android.graphics.Canvas canvas) {
            super.onDraw(canvas);

            int n=0;

            try{
                n=visualizer.getCaptureSize();
            }catch(Exception ex){

            }
            int px=0;
           /* for (int i=0;i<n;i++)
            {
                try{
                    int v=data[i];
                   // Log.println(Log.ASSERT,"v",v+" ");
                    //Log.println(Log.ASSERT,"vaca",v+"");
                    int h=map(v,-128,127,0,getHeight());
                    paint.setColor(Color.BLACK);

                    canvas.drawLine(i*spacing,getHeight(),i*spacing,h,paint);
                }catch(Exception ex){

                }
            }*/
           for (int i=0;i<magnitudes.length;i++){
               float h=mapf(magnitudes[i],0,200,0,getHeight());
               /*
               //Log.println(Log.ASSERT,"hh",h+"");
               if (i<AUDIBLE_FREQ/3.0){
                   paint.setColor(Color.RED);
                   canvas.drawLine(i*spacing,getHeight(),i*spacing,getHeight()-h,paint);
               }
               if (i>=(int)(((double)1/3)*(AUDIBLE_FREQ)) && i<AUDIBLE_FREQ*2.0/3){
                   paint.setColor(Color.GREEN);
                   canvas.drawLine(i*spacing,getHeight(),i*spacing,getHeight()-h,paint);
               }
               if (i>=(int)((2.0/3)*(AUDIBLE_FREQ)) && i<AUDIBLE_FREQ){
                   paint.setColor(Color.BLUE);
                   canvas.drawLine(i*spacing,getHeight(),i*spacing,getHeight()-h,paint);
               }

*/

               float mf=map(bassThreshold,200,0,0,getHeight());
               paint.setColor(Color.GREEN);
               canvas.drawLine(0,mf,getWidth(),mf,paint);
               paint.setColor(Color.BLACK);
               canvas.drawLine(i*spacing+10,getHeight(),i*spacing+10,getHeight()-h,paint);
           }

        }
    }


    static MediaPlayer player;
    @Deprecated
    public void play() {
        /*if (player == null) {
            player = MediaPlayer.create(main, R.raw.z);
        }
        player.start();*/
        Log.println(Log.ASSERT,"Deprecated","vezi ca am scos R.raw.z, daca ai ceva eroare cand porneste musicFragment, incearca asta");
    }

    float th=0.1f;
    float previousBass=0;
    SeekBar bar;
    Button kick,hat,general;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_music, container, false);

        FrameLayout frameLayout =  v.findViewById(R.id.music_layout);
        frameLayout.addView(new Canvas(getActivity()));
        seek=v.findViewById(R.id.seekBar);
        b=v.findViewById(R.id.button2);
        hat=v.findViewById(R.id.hat);
        kick=v.findViewById(R.id.kick);
        general=v.findViewById(R.id.general);
        bar=v.findViewById(R.id.sensitivitate);
        bar.setMax(1000);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bt.setSensitivity(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                th=mapf(progress,0,seek.getMax(),0,1);

                // th=progress;
                Log.println(Log.ASSERT,"THRESHOLD",th+" ");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return v;
    }


}
