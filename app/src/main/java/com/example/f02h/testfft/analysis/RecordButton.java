package com.example.f02h.testfft.analysis;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.AudioRecord;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.f02h.testfft.MainActivity;
import com.example.f02h.testfft.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public  class RecordButton extends Button {
    boolean mStartRecording = true;
    int minBufferSize = 2000;
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final int RECORDER_SAMPLERATE = 44100;

    private boolean isRecording = false;
    private int bufferSize = 0;
    private FileOutputStream os;
    private BufferedOutputStream bos;
    private DataOutputStream dos;
    private int result = 0;
    private Thread thread;
    private float[] sample;
    private float[] samples = new float[512];
    private float[][] finalAudioFloats;

    private AudioRecord mRecorder;

    OnClickListener clicker = new OnClickListener() {
        public void onClick(View v) {
            Log.i("button click", "******");
            if (mStartRecording) {
                mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100 ,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,2*minBufferSize );
                setText("Stop recording");
                thread = new Thread(new Runnable() {
                    public void run() {
                        isRecording = true;
                        startRecording();
                    }
                });
                thread.start();
                isRecording = true;
            } else {
                stopRecording();
//                if (finalAudioFloats != null) {
//                    MainActivity.setupDataWindowed(finalAudioFloats);
//                }
                setText("Start recording");

            }
            mStartRecording = !mStartRecording;
        }
    };

    public RecordButton(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        setText("Start recording");
        setOnClickListener(clicker);
    }


    private void startRecording() {

        File path = Environment.getExternalStorageDirectory();
        Log.v("file path", ""+path.getAbsolutePath());

        File file = new File(path, "test.wav");


        try {
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            os = new FileOutputStream(getTempFilename());
            bos = new BufferedOutputStream(os);
            dos = new DataOutputStream(bos);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        bufferSize = AudioRecord.getMinBufferSize(44100 ,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
//        float[] buffer = new float[bufferSize];
        byte buffer[] = new byte[bufferSize];
        if (mRecorder.getState() == 1) {
            mRecorder.startRecording();
            isRecording = true;
        }

        try{
            while (isRecording){
                result = mRecorder.read(buffer, 0, bufferSize);
                if (result > 0){
                }

                if (AudioRecord.ERROR_INVALID_OPERATION != result) {
                    try {
                        dos.write(buffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void stopRecording() {
        isRecording = false;
        mRecorder.stop();
        mRecorder.release();

        copyWaveFile(getTempFilename(),getFilename());
        deleteTempFile();


//        MainActivity.setGraphs(test, test2);

    }

    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        MainActivity.mFileName = file.getAbsolutePath() + "/" + df.format(new Date()) + AUDIO_RECORDER_FILE_EXT_WAV;
        return (MainActivity.mFileName);
    }

    private String getTempFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);

        if (tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    private void deleteTempFile() {
        File file = new File(getTempFilename());
        file.delete();

        File recordFile = new File(MainActivity.mFileName );

        int shortSizeInBytes = Short.SIZE / Byte.SIZE;
        int bufferSizeInBytes = (int) (recordFile.length() / shortSizeInBytes);
        short[] audioData = new short[bufferSizeInBytes];
        try
        {
            InputStream inputStream = new FileInputStream(recordFile);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

            int i = 0;
            while (dataInputStream.available() > 0)
            {
                audioData[i] = dataInputStream.readShort();
                i++;
            }

            dataInputStream.close();

            AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    bufferSizeInBytes, AudioTrack.MODE_STREAM);

//            track.play();
            track.write(audioData, 0, bufferSizeInBytes);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;

        int windowSize = 1024;

        byte[] data = new byte[bufferSize];

//        #start capture from Mic
//        while true:
//
//        #into values capture 2048 points from your mic
//        values=dataFromMic(Chunk);
//        #Apply Window hanning = multiply window function(hanning) over your 2048 points
//        for i 1:Chunk:
//        windowed[i] = values[i] * hanning[i]
//        #Apply FFT
//        fftData=fft(windowed);
//        #Get Magnitude (linear scale) of first half values
//        Mag=abs(fftData(1:Chunk/2))
//        # update/show results
//        plot(Mag)
//
//        end

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            Log.i("File size: ", ""+totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while(in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyWaveFileWindowed(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;
//
        byte[] data = new byte[bufferSize];



        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            Log.i("File size: ", ""+totalDataLen);

            int avgFactor = 44;
            int floatArraySize = ((int) totalDataLen)/2;

            float[] audioFloats = new float[floatArraySize];
//            finalAudioFloats = new float[floatArraySize/avgFactor];
            int index = 0;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while(in.read(data) != -1) {
                out.write(data);

                ShortBuffer sbuf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
                short[] audioShorts = new short[sbuf.capacity()];
                sbuf.get(audioShorts);

                for (int i = 0; i < audioShorts.length; i++) {
                    audioFloats[index] = ((float)audioShorts[i]/44100);
                    index++;
                }
            }

            index = 0;
            float avg = 0;
            for (int i = 0; i < floatArraySize; i++) {
                if (i%(avgFactor+1) == 0) {
//                    finalAudioFloats[index] = avg / avgFactor;
                    avg = 0;
                    index++;
                }
                avg += audioFloats[i];
            }

            in.close();
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException
    {
        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }
}