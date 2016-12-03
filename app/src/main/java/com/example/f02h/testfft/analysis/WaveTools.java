package com.example.f02h.testfft.analysis;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.content.Context;
import android.util.Log;

public class WaveTools {

	static byte[] myData = null;
	public static byte[] myData2 = null;
	static int mySampleRate;
	
	
public static float [] wavread(String path, Context mCtx) {
		
		String strThrow = "Error";
		InputStream inFile = null;
		byte[] tmpLong = new byte[4];
		byte[] tmpInt = new byte[2];

		long myChunkSize;
		long mySubChunk1Size;
		int myFormat;
		long myChannels;
	
		long myByteRate;
		int myBlockAlign;
		int myBitsPerSample;
		long myDataSize = 0;
		float [] buffer = null;
		
		myData = null;
			try{
		
					InputStream is2 = null;					
			
					is2 =mCtx.getAssets().open(path);
					inFile = new DataInputStream(is2);
			
				

			
			String chunkID ="" + (char)inFile.read() + (char)inFile.read() + (char)inFile.read() + (char)inFile.read();
		
			inFile.read(tmpLong); // read the ChunkSize
			myChunkSize = byteArrayToLong(tmpLong);
	
			
			String format = "" + (char)inFile.read() + (char)inFile.read() + (char)inFile.read() + (char)inFile.read();
		
			if (!format.equals("WAVE")){
				strThrow="File format is not .wav";
				throw new IllegalStateException(strThrow);
			}
			//Log.d("WAVE","format = "+format);
		    String subChunk1ID = "" + (char)inFile.read() + (char)inFile.read() + (char)inFile.read() + (char)inFile.read();
		    
			inFile.read(tmpLong); // read the SubChunk1Size
			mySubChunk1Size = byteArrayToLong(tmpLong);
			
			inFile.read(tmpInt); // read the audio format.  This should be 1 for PCM
			myFormat = byteArrayToInt(tmpInt);
			//Log.d("WAVE","myFormat = "+myFormat);
			
			inFile.read(tmpInt); // read the # of channels (1 or 2)
			myChannels = byteArrayToInt(tmpInt);

			if (myChannels > 1){
				strThrow = "File format is not mono";
				throw new IllegalStateException(strThrow);
			}
			inFile.read(tmpLong); // read the samplerate
			mySampleRate = (int)byteArrayToLong(tmpLong);
			//Log.d("WAVE","channels = "+myChannels);
			if (mySampleRate > mySampleRate){
				strThrow = "File format is not 8kHz";
				throw new IllegalStateException(strThrow);
			}
			Log.d("WAVE","Fs = "+mySampleRate);
			inFile.read(tmpLong); // read the byterate
			myByteRate = byteArrayToLong(tmpLong);

			
			inFile.read(tmpInt); // read the blockalign
			myBlockAlign = byteArrayToInt(tmpInt);

			
			inFile.read(tmpInt); // read the bitspersample
			myBitsPerSample = byteArrayToInt(tmpInt);
	
			String dataChunkID = "" + (char)inFile.read() + (char)inFile.read() + (char)inFile.read() + (char)inFile.read();

			inFile.read(tmpLong); // read the size of the data
			myDataSize = byteArrayToLong(tmpLong);


			// read the data chunk
			myData = new byte[(int)myDataSize];
			myData2 = new byte[(int)myDataSize];
			
			Short [] shortVal = new Short[(int) myDataSize/2];
		
			ByteBuffer bb = ByteBuffer.allocateDirect(2);
			int max = 0;
			buffer = new float[(int) myDataSize/2];
			bb.order(ByteOrder.LITTLE_ENDIAN);
			int count = 0;
			for (int i = 0; i<myDataSize;i+=2){
				inFile.read(tmpInt);
				myData[i]= tmpInt[0];
				myData[i+1]= tmpInt[1];
				bb.position(0);			
				bb.put(tmpInt[0]);
				bb.put(tmpInt[1]);
				buffer[count] = (float)  bb.getShort(0);
				shortVal[count] = bb.getShort(0);
				//Log.d("Audio Read","myFormat = "+shortVal[count]);
				if (shortVal[count] > max){
					max = shortVal[count];
				}else if (-shortVal[count] > max){
					max =  -shortVal[count];
				}
				
				count++;
				
			}
			int inc = 0;
			ByteBuffer bb2 = ByteBuffer.allocateDirect(2);
			bb2.order(ByteOrder.LITTLE_ENDIAN);
			for (int i=0; i<((int) myDataSize/2) ;i++){
				
				shortVal[i] = (short) (((int)shortVal[i]*32767)/max);
				bb2.putShort(0,shortVal[i]);
				myData2[inc] = bb2.get(0);
				myData2[inc+1] = bb2.get(1);
				inc = inc +2;
				
			}
			// close the input stream
			inFile.close();
			}catch(Exception e){
				
			Log.d("WAVE", "EXCEPTION ",e);
			
			}
		
		  return buffer;
	
		}

public static long byteArrayToLong(byte[] b)
{
	int start = 0;
	int i = 0;
	int len = 4;
	int cnt = 0;
	byte[] tmp = new byte[len];
	for (i = start; i < (start + len); i++)
	{
		tmp[cnt] = b[i];
		cnt++;
	}
	long accum = 0;
	i = 0;
	for ( int shiftBy = 0; shiftBy < 32; shiftBy += 8 )
	{
		accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
		i++;
	}
	return accum;
}

public static int byteArrayToInt(byte[] b)
{
	int start = 0;
	int low = b[start] & 0xff;
	int high = b[start+1] & 0xff;
	return (int)( high << 8 | low );
}

public static byte [] getByteArray(){

	return myData2;
}

public static int getFs() {
	
	return mySampleRate;
}

}
