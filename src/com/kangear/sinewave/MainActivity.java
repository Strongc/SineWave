package com.kangear.sinewave;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class MainActivity extends Activity {

    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>
    private final int sampleRate = 44100;
    
    WaveService mWaveService = new WaveService();

    Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    public void onClick(View v) {
        // Use a new tread as this can take a while
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                handler.post(new Runnable() {

                    public void run() {
                        playSound();
                    }
                });
            }
        });
        thread.start();
    }


    void playSound(){
    	byte[] dst = mWaveService.getWave((short)0x0e0e, (byte)0x14);
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, dst.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(dst, 0, dst.length);
        audioTrack.play();
    }
    
//    void playSound2(){
//    	byte[] dst = mWaveService.getWave((short)0x707, (byte)0x05);
//        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
//                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
//                AudioFormat.ENCODING_PCM_16BIT, dst.length,
//                AudioTrack.MODE_STATIC);
//        audioTrack.write(dst, 0, dst.length);
//        audioTrack.play();
//    }
}
