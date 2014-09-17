package com.kangear.sinewave;

import java.util.ArrayList;

public class WaveService {
    private final int duration = 10; // seconds
    private final int sampleRate = 44100;
    private int numSamples = duration * sampleRate;
    private final double sample[] = new double[numSamples];
    private final double freqOfTone = 200000; // hz  200000=>20khz(50us) 最高
	
    private final byte generatedSnd[] = new byte[2 * numSamples];
    /**
     * @param time unit:ms
     * @value 1 0
     * @return
     */
    public byte[] genTone(double time, int value){
    	numSamples = (int) (time/1000 * sampleRate);
    	double sample[] = new double[numSamples];
    	byte generatedSnd[] = new byte[2 * numSamples];
    	
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) (value * (dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
        
        return generatedSnd;
    }
    
    public byte[] genTone(){
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
        
        return generatedSnd;
    }
    
    private final float          INFRARED_NEC_1_HIGH_WIDTH = 0.56f ;
    private final float           INFRARED_NEC_1_LOW_WIDTH = 0.565f;
    private final float          INFRARED_NEC_0_HIGH_WIDTH = 1.69f ; // 2.25 - 0.56
    private final float           INFRARED_NEC_0_LOW_WIDTH = 0.56f ;
    private final float INFRARED_NES_LEADERCODE_HIGH_WIDTH = 9.0f  ;
    private final float  INFRARED_NES_LEADERCODE_LOW_WIDTH = 4.50f ;
    private final float    INFRARED_NES_STOPBIT_HIGH_WIDTH = 0.56f ;
    
    
    byte[] getHigh() {
    	//0.56ms + (1.125 - 0.56)
    	//INFRARED_NEC_1_HIGH_WIDTH  0.56
    	//INFRARED_NEC_1_LOW_WIDTH   0.565 // 1.125 - 0.56
    	byte[] one = genTone(0.56, 1);
    	byte[] two = genTone(1.125-0.56, 0);
    	byte[] combined = new byte[one.length + two.length];

    	System.arraycopy(one,0,combined,0         ,one.length);
    	System.arraycopy(two,0,combined,one.length,two.length);
    	return combined;
    }
    
    byte[] getLow() {
    	//(2.25-0.56) + 0.56
    	//INFRARED_NEC_0_HIGH_WIDTH  1.69 // 2.25 - 0.56
    	//INFRARED_NEC_0_LOW_WIDTH   0.56
    	byte[] one = genTone(2.25 - 0.56, 1);
    	byte[] two = genTone(0.56, 0);
    	byte[] combined = new byte[one.length + two.length];

    	System.arraycopy(one,0,combined,0         ,one.length);
    	System.arraycopy(two,0,combined,one.length,two.length);
    	return combined;
    }
    
    //byte[] getWave(float leaderCode, float space, int userCode ) {
    
    //                   0x0707         0x05
    byte[] getWave(short userCode, byte dataCode) {
    	ArrayList<byte[]> wave_list = new ArrayList<byte[]>();
    	int totalLength = 0;
    	
    	wave_list.add(getleaderCode());
    	wave_list.add(getUserCodeToWave(userCode));
    	wave_list.add(getDataCodeToWave(dataCode));
    	wave_list.add(getStopBit());
    	for( byte[] byteTmp : wave_list)
    		totalLength += byteTmp.length;

    	int currentPosition = 0;
    	byte totalWaveArray[] = new byte[totalLength];

    	for(byte[] byteArray : wave_list) {
    		System.arraycopy(byteArray,0,totalWaveArray,currentPosition        ,byteArray.length);
    		currentPosition += byteArray.length;
    	}
    	
    	return totalWaveArray;
    }
    
    
    /**
     * 1.leader code
     * @return
     */
    byte[] getleaderCode() {
    	//9.0ms + 4.50ms Infrared
    	//INFRARED_NES_LEADERCODE_HIGH_WIDTH  9.0
    	//INFRARED_NES_LEADERCODE_LOW_WIDTH   4.50
    	byte[] one = genTone(9.0, 1);
    	byte[] two = genTone(4.50, 0);
    	byte[] combined = new byte[one.length + two.length];

    	System.arraycopy(one,0,combined,0         ,one.length);
    	System.arraycopy(two,0,combined,one.length,two.length);
    	return combined;
    }
    
    /**
     * 2. user code
     * @param userCode
     * @return
     */
    byte[] getUserCodeToWave(short userCode) {
    	ArrayList<byte[]> wave_list = new ArrayList<byte[]>();
    	int totalLength = 0;
    	
    	for(int i=0; i<16; ++i) {
    		if(((userCode >> i) & 0x1) == 1) { // 1
    			wave_list.add(getHigh());
    		} else {                           // 0
    			wave_list.add(getLow());
    		}
    		totalLength += wave_list.get(i).length;	
    	}
    	
    	int currentPosition = 0;
    	byte userCodeWaveArray[] = new byte[totalLength];

    	for(byte[] byteArray : wave_list) {
    		System.arraycopy(byteArray,0,userCodeWaveArray,currentPosition        ,byteArray.length);
    		currentPosition += byteArray.length;
    	}
    	
    	return userCodeWaveArray;
    }
    /**
     * 3. data code: sign-and-magnitude+ones'complement
     * @param dataCode
     * @return
     */
    byte[] getDataCodeToWave(byte dataCode) {
    	ArrayList<byte[]> wave_list = new ArrayList<byte[]>();
    	int totalLength = 0;
    	
    	for(int i=0; i<8; ++i) {               // sign-and-magnitude
    		if(((dataCode >> i) & 0x1) == 1) { // 1
    			wave_list.add(getHigh());
    		} else {                           // 0
    			wave_list.add(getLow());
    		}
    		totalLength += wave_list.get(i).length;	
    	}
    	
    	for(int i=0; i<8; ++i) {               // ones'complement
    		if(((dataCode >> i) & 0x1) == 1) { // 1
    			wave_list.add(getLow());
    		} else {                           // 0
    			wave_list.add(getHigh());
    		}
    		totalLength += wave_list.get(i).length;	
    	}
    	
    	int currentPosition = 0;
    	byte userCodeWaveArray[] = new byte[totalLength];

    	for(byte[] byteArray : wave_list) {
    		System.arraycopy(byteArray,0,userCodeWaveArray,currentPosition        ,byteArray.length);
    		currentPosition += byteArray.length;
    	}
    	
    	return userCodeWaveArray;
    }
    
    /**
     * 4.stop bit
     * @return
     */
    byte[] getStopBit() {
    	//0.56ms
    	//INFRARED_NES_STOPBIT_HIGH_WIDTH    0.56
    	return genTone(0.56, 1);
    }
}
