package com.example.f02h.testfft;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Message;
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
import com.example.f02h.testfft.analysis.Template;
import com.example.f02h.testfft.analysis.WaveTools;
//import com.example.f02h.testfft.analysis.calcSpec;
import com.example.f02h.testfft.analysis.calcSpec2;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.os.Handler;
import java.util.logging.LogRecord;


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
//    public static float [] audioBuf;


    public static float[] buff;
    public static float[] buff_audio;
    public static float[] new_sig;
    public static ImageView left;
    public static ImageView left2;
    public static TextView textleft2;
    public static TextView textleft;

    public static TextView right;
    public static TextView title;
    public static int tshift = 4; //frame shift in ms
    public static int tlen = 32; //frame length in ms
    static String inputPath;


    public static Template [] templates;
    public static List<Template> templatesList = new ArrayList<Template>();
    public static List<Template> templatesListCache = new ArrayList<Template>();
    public static String [] listTemplates = {"41mb.wav","41lj.wav", "41ce.wav", "41kp.wav","42lj.wav"};
    public static int templateNbr = listTemplates.length;
    public static int workers = templateNbr;
//    public static String [] listTemplates = {"42lj.wav","42lj.wav", "42lj.wav", "42lj.wav","42lj.wav"};
    public static float[][] audioSamples = new float[templateNbr][];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity.context = getApplicationContext();
        setContentView(R.layout.activity_main);
        // build templates
        templates = new Template[templateNbr];
        left = (ImageView) findViewById(R.id.imageView);
        left2 = (ImageView) findViewById(R.id.imageView2);
        textleft2 = (TextView) findViewById(R.id.leftText2);
        textleft = (TextView) findViewById(R.id.leftText);

        spectroButton = (Button) findViewById(R.id.Spectro);
        spectroButton.setOnClickListener( new OnClickListener() {
            public void onClick(View v) {
                Log.i("spectro button click", "******");
                read();
                if (templatesListCache.size() != 0) {
                    setup(1);
                } else {
                    for (int i = 0; i < templateNbr; i++) {
                        try {
//                        SetupUI();
                            audioSamples[i] = WaveTools.wavread(listTemplates[i], MainActivity.getAppContext());
                            String dummy = "test";

                            new calcSpec2(i, listTemplates[i]).execute(dummy);
                        } catch (Exception e) {
                            Log.d("SpecGram2", "Exception= " + e);
                        }
                    }
                }
            }
        });
    }

    public static Handler myHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    workers --;
                    if (workers == 0) {
                        setup(0);
                    }
                    // calling to this function from other pleaces
                    // The notice call method of doing things
                    break;
                default:
                    break;
            }
        }
    };

    public static void setup(int useCache) {
        if (useCache == 1) {
            templatesList = templatesListCache;
        }
        for (int i = 0; i < templateNbr; i++) {
            Log.i("Template", ""+templatesList.toString());
        }

        double result;
        if (useCache != 1) {
            write();
        }
        Template search = templatesList.get(4);
        templatesList.remove(4);
        result = recognize_dtw(search, templatesList, "Cosine");

        String a = "test";
    }

    public static void writeData(int templateNumber, double[][] result, double[][] spec) {
        Template tmp = new Template(result, listTemplates[templateNumber], spec);
        templatesList.add(tmp);
    }

//    private void SetupUI() {
//        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT,
//                (float) 1.0f);
//        LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT,
//                (float) 1.0f);
//        LinearLayout.LayoutParams param3 = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,
//                (float) 0.1f);
//        LinearLayout.LayoutParams param4 = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,
//                (float) 1.0f);
//
//        LinearLayout main = new LinearLayout(this);
//        LinearLayout secondary = new LinearLayout(this);
//        ScrollView scroll = new ScrollView(this);
//        title = new TextView(this);
//        left = new ImageView(this);
//
//
//        scroll.setLayoutParams(param4);
//        main.setLayoutParams(param4);
//        main.setOrientation(LinearLayout.VERTICAL);
//        secondary.setLayoutParams(param1);
//        secondary.setOrientation(LinearLayout.HORIZONTAL);
//
//        title.setLayoutParams(param3);
//        left.setLayoutParams(param2);
//
//
//        secondary.addView(left);
//        scroll.addView(secondary);
//
//        main.addView(title);
//        main.addView(scroll);
//
//        setContentView(main);
//        title.setText("FFT Spectrogram of speech example by DigiPhD");
//        title.setTextSize(12);
//        title.setTypeface(null, Typeface.BOLD);
//
//
//    }


    public static double recognize_dtw(Template unknown_template,List<Template> templates,String distance_f) {
        String a = "test";
        int nbrOfTemplates = templates.size();
        String izpis = "";
        List<Double> values = new ArrayList<Double>();

        double[][] SM_lj;

        for (int i = 0; i < nbrOfTemplates; i++) {
            SM_lj = calcSpec2.simmx(unknown_template.spectro, templates.get(i).spectro, distance_f);
            double sim = calcSpec2.dp(calcSpec2.subMatrix(SM_lj, 1.0));
            templates.get(i).similarity = sim;
            values.add(sim);
        }
        double[] valuesdouble = new double[values.size()];

        double sum = 0.0;
        for (int i = 0; i < values.size(); i++) {
            valuesdouble[i] = values.get(i);
            sum += valuesdouble[i];
        }
        double[] result = calcSpec2.FindSmallest(valuesdouble);
        double min = result[0];
        int pos = (int) result[1];
        valuesdouble[pos] = 0;

        double ave = sum / (nbrOfTemplates - 1);

        double conf = (ave - min) / ave * 100;

//        Bitmap spectro = calcSpec2.bitmapFromArray(unknown_template.realSpectro);
//        MainActivity.left.setImageBitmap(spectro);
        MainActivity.textleft.setText(unknown_template.filename);
//        Bitmap spectro2 = calcSpec2.bitmapFromArray(templates.get(pos).realSpectro);
//        MainActivity.left2.setImageBitmap(spectro2);
        MainActivity.textleft2.setText(templates.get(pos).filename);

        return 0.0;
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

    public static void write(){
        Template myPersonObject = new Template();
        myPersonObject.filename = "abc";
        ObjectOutput out = null;
        List<Template> templatesListClone = new ArrayList<Template>();
        templatesListClone.addAll(templatesList);
        for (int i = 0; i < templateNbr; i++) {
            templatesListClone.get(i).realSpectro = new double[0][0];
        }
        try {
            out = new ObjectOutputStream(new FileOutputStream(new File(Environment.getExternalStorageDirectory().getPath(),"AudioRecorder")+File.separator+"cache.srl"));
            out.writeObject(templatesListClone);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void read(){
        ObjectInputStream input;

        try {
            input = new ObjectInputStream(new FileInputStream(new File(new File(Environment.getExternalStorageDirectory().getPath(),"AudioRecorder")+File.separator+"cache.srl")));

            templatesListCache = (ArrayList<Template>) input.readObject();
//            Log.v("serialization","Person a="+myPersonObject.getA());
            input.close();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
