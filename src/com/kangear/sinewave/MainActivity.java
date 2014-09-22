/*
 * Copyright (C) 2014 kangear@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kangear.sinewave;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	final String LOG_TAG = "MainActivity";
    private final int sampleRate = 44100;
    
    WaveService mWaveService = new WaveService();
    TextView mTextViewLength = null;
    
    int currentVolume = 0;
    boolean isHeadsetOn;

    Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mTextViewLength = (TextView) this.findViewById(R.id.textview_length);
    }
    
    @SuppressWarnings("deprecation")
	@Override
    protected void onResume() {
        super.onResume();
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        isHeadsetOn = mAudioManager.isWiredHeadsetOn();
        currentVolume = mAudioManager
    			.getStreamVolume(AudioManager.STREAM_MUSIC);
        
        /* set headset stream music volume*/
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
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
    	byte[] dst = mWaveService.getWave((short)0x00ff, (byte)0x28);
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, dst.length,
                AudioTrack.MODE_STATIC);
        Log.d(LOG_TAG, "length=" + dst.length);
        mTextViewLength.setText(String.valueOf(dst.length));
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
