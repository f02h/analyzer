package com.example.f02h.testfft;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import android.content.Context;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Context;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.f02h.testfft.analysis.FFT;
import com.example.f02h.testfft.analysis.PlayButton;
import com.example.f02h.testfft.analysis.RecordButton;
import com.example.f02h.testfft.analysis.FourierTransform;
import com.example.f02h.testfft.analysis.WaveTools;
import com.example.f02h.testfft.analysis.calcSpec;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.File;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private static Context context;

    public static Context getAppContext() {
        return MainActivity.context;
    }

    private RecordButton mRecordButton = null;
    private PlayButton   mPlayButton = null;
    private Button spectroButton = null;
    public static String mFileName = null;
    public static final String LOG_TAG = "AudioRecordTest";
    public static float samples3[];
    public static GraphView graph;
    public static GraphView graph2;
    public static View mainView;
    public static float [] audioBuf;


    public static float[] buff;
    public static float[] buff_audio;
    public static float[] new_sig;
    public static ImageView left;
    public static TextView right;
    public static TextView title;
    public static int tshift = 4; //frame shift in ms
    public static int tlen = 32; //frame length in ms
    static String inputPath;

    public static float[] array_hat = null;
    public static float[] res=null;
    public static float[] fmag = null;
    public static float[] flogmag = null;
    public static float[] fft_cpx,tmpr,tmpi;
    public static float[] mod_spec =null;
    public static float[] real_mod = null;
    public static float[] imag_mod = null;
    public static double[] real =null;
    public static double[] imag= null;
    public static double[] mag =null;
    public static double[] magTmp =null;
    public static double[] phase = null;
    public static double[] logmag = null;
    public static float [][] framed;
    public static int n, seg_len,n_shift;
    public static float n_segs;
    public static float [] time_array;
    public static float [] array;
    public static float [] wn;
    public static double[] nmag;
    public static double [][] spec;
    public static double [][] spec1;
    public static float [] array2;
    public static float max;
    public static float min;
    public static double smax;
    public static double smin;
    public static float mux;
    public static float smux;

    public static double[] melFilters;
    public static double[] mel2Hz;
    public static double[] bin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity.context = getApplicationContext();
        setContentView(R.layout.activity_main);

        spectroButton = (Button) findViewById(R.id.Spectro);
        spectroButton.setOnClickListener( new OnClickListener() {
            public void onClick(View v) {
                Log.i("spectro button click", "******");
                try{
                    SetupUI();
                    audioBuf = WaveTools.wavread("sp11.wav", MainActivity.getAppContext());
                    String dummy = "test";
                    new calcSpec().execute(dummy);
                }catch(Exception e){
                    Log.d("SpecGram2","Exception= "+e);
                }
            }
        });

//        final float frequency = 440; // Note A
//        float increment = (float)(2*Math.PI) * frequency / 44100;
//        float angle = 0;
//        float samples[] = new float[1024];
//        FFT fft = new FFT( 1024, 44100 );
//
//
//        graph = (GraphView) findViewById(R.id.graph);
//        graph2 = (GraphView) findViewById(R.id.graph2);
//
////        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
////        mFileName += "/audiorecordtest.3gp";
//
//        Random rand = new Random();
//        for( int i = 0; i < samples.length; i++ )
//        {
//            samples[i] = (float)Math.sin( angle ) + rand.nextInt(10)+1;
//            angle += increment;
//        }
//
//        fft.forward( samples );
//        float[] testSpectrum = fft.getSpectrum();
//        DataPoint[] test = new DataPoint[testSpectrum.length];
//        DataPoint[] test2 = new DataPoint[samples.length];
//        for( int i = 0; i < testSpectrum.length; i++ )
//        {
//            test[i] = new DataPoint(i,testSpectrum[i]);
//        }
//        for( int i = 0; i < samples.length; i++ )
//        {
//            test2[i] = new DataPoint(i,samples[i]);
//        }
//
//        MainActivity.setGraphs(test,test2);


    }

    private void SetupUI() {
        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT,
                (float) 1.0f);
        LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT,
                (float) 1.0f);
        LinearLayout.LayoutParams param3 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,
                (float) 0.1f);
        LinearLayout.LayoutParams param4 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,
                (float) 1.0f);

        LinearLayout main = new LinearLayout(this);
        LinearLayout secondary = new LinearLayout(this);
        ScrollView scroll = new ScrollView(this);
        title = new TextView(this);
        left = new ImageView(this);


        scroll.setLayoutParams(param4);
        main.setLayoutParams(param4);
        main.setOrientation(LinearLayout.VERTICAL);
        secondary.setLayoutParams(param1);
        secondary.setOrientation(LinearLayout.HORIZONTAL);

        title.setLayoutParams(param3);
        left.setLayoutParams(param2);


        secondary.addView(left);
        scroll.addView(secondary);

        main.addView(title);
        main.addView(scroll);

        setContentView(main);
        title.setText("FFT Spectrogram of speech example by DigiPhD");
        title.setTextSize(12);
        title.setTypeface(null, Typeface.BOLD);


    }

    public static void setupData (float[] samples) {
        // one sec 1024 samples
        int samplePerSec = 1024;
        int sec = (int)Math.ceil((double)samples.length / samplePerSec);
        sec = (sec == 0) ? 1 : sec;

        float[] finalSamples = new float[samplePerSec];
        for (int i = 0; i < ((samplePerSec > samples.length) ? samples.length :samplePerSec ); i++) {
            finalSamples[i] = samples[i];
        }

        FFT fft = new FFT( samplePerSec, 44100 );
        fft.forward( finalSamples );
        float[] testSpectrum = fft.getSpectrum();
        DataPoint[] test = new DataPoint[testSpectrum.length];
        DataPoint[] test2 = new DataPoint[finalSamples.length];
        for( int i = 0; i < testSpectrum.length; i++ )
        {
            test[i] = new DataPoint(i,testSpectrum[i]);
        }
        for( int i = 0; i < finalSamples.length; i++ )
        {
            test2[i] = new DataPoint(i,finalSamples[i]);
        }
        graph.removeAllSeries();
        graph2.removeAllSeries();
        MainActivity.setGraphs(test,test2);
    }

    public static void setupDataWindowed (float[][] samples) {

        for (int i = 0; i < 1; i++) {
            DataPoint[] test2 = new DataPoint[1024];
            for (int j = 0; j < 1024; j++) {
                test2[j] = new DataPoint(i,samples[i][j]);
            }
            PointsGraphSeries<DataPoint> series = new PointsGraphSeries<DataPoint>(test2);
            graph2.addSeries(series);
        }
    }

    public static void setGraphs(DataPoint[] test, DataPoint[] test2) {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(test);
        graph.addSeries(series);
        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>(test2);
        graph2.addSeries(series2);
    }
}
