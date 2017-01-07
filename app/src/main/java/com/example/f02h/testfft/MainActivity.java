package com.example.f02h.testfft;

import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.Menu;
import android.view.MenuItem;
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
import com.example.f02h.testfft.analysis.calcSpec3;
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
    private static Context Actcontext;

    public static Context getAppContext() {
        return MainActivity.context;
    }

    public static Context getActContext() {
        return MainActivity.Actcontext;
    }

    private RecordButton mRecordButton = null;
    private PlayButton   mPlayButton = null;
    public static PlayButton  mPlayButton1 = null;
    public static PlayButton mPlayButton2 = null;
    private Button spectroButton = null;
    public static String mFileName = null;
    public static final String LOG_TAG = "AudioRecordTest";
    public static float samples3[];
    public static GraphView graph;
    public static GraphView graph2;
    public static View mainView;
//    public static float [] audioBuf;

    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
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
    public static Template currSearch = null;
    public static double[][][] spectrogramView = new double[2][0][0];
    public static String[] spectrogramViewFilename = new String[2];
    public static List<Template> templatesList = new ArrayList<Template>();
    public static List<Template> templatesListCache = new ArrayList<Template>();
    public static String [] listTemplates = {"41mb.wav","41lj.wav", "41ce.wav", "41kp.wav","42lj.wav"};
    public static int templateNbr = listTemplates.length;
    public static int workers = templateNbr;
    public static int spectrogramViewWorkers = 2;
//    public static String [] listTemplates = {"42lj.wav","42lj.wav", "42lj.wav", "42lj.wav","42lj.wav"};
    public static float[][] audioSamples = new float[templateNbr][];
    public static FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity.context = getApplicationContext();
        MainActivity.Actcontext = this;

        setContentView(R.layout.activity_main);
        Toolbar toolbar =
                (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn = (Button)findViewById(R.id.RecordButton);
                btn.performClick();
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
        // build templates
        templates = new Template[templateNbr];
        left = (ImageView) findViewById(R.id.imageView);
        left2 = (ImageView) findViewById(R.id.imageView2);
        textleft2 = (TextView) findViewById(R.id.leftText2);
        textleft = (TextView) findViewById(R.id.leftText);

        spectroButton = (Button) findViewById(R.id.Spectro);
        mPlayButton1 = (PlayButton) findViewById(R.id.PlayButton1);
        mPlayButton2 = (PlayButton) findViewById(R.id.PlayButton2);

        read();
        if (templatesListCache.size() != 0) {
            templatesList = templatesListCache;
//            setup(1,0);
            String a = "tesdt";
        }

        spectroButton.setOnClickListener( new OnClickListener() {
            public void onClick(View v) {
                Log.i("spectro button click", "******");
                rebuildCache();
//                for (int i = 0; i < templateNbr; i++) {
//                    try {
//                        audioSamples[i] = WaveTools.wavread(listTemplates[i], MainActivity.getAppContext());
//                        String dummy = "test";
//
//                        new calcSpec2(i, listTemplates[i]).execute(dummy);
//                    } catch (Exception e) {
//                        Log.d("SpecGram2", "Exception= " + e);
//                    }
//                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_rebuild) {
            rebuildCache();
        }

        return super.onOptionsItemSelected(item);
    }

    public static Handler rebuildCacheHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    workers --;
                    if (workers == 0) {
                        setup(0,1);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public static Handler spectogramViewHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    spectrogramViewWorkers --;
                    if (spectrogramViewWorkers == 0) {
                        setSpectogramView();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public static Handler myHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                        setup(0,0);
                    break;
                default:
                    break;
            }
        }
    };

    public static void rebuildCache() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File f = new File(filepath,AUDIO_RECORDER_FOLDER);
        File tmpfile[] = f.listFiles();
        if (tmpfile.length != 0) {
            File file[] = new File[tmpfile.length - 1];
            int index = 0;
            for (int i = 0; i < tmpfile.length; i++) {
                if (!tmpfile[i].getName().equals("cache.srl")) {
                    file[index] = tmpfile[i];
                    index++;
                }
            }
            workers = file.length;
            audioSamples = new float[file.length][];
            for (int i = 0; i < file.length; i++) {
                try {
                    audioSamples[i] = WaveTools.wavread(file[i].getPath(), MainActivity.getAppContext());
                    String dummy = "test";

                    new calcSpec2(i, file[i].getPath()).execute(dummy);
                } catch (Exception e) {
                    Log.d("SpecGram2", "Exception= " + e);
                }
            }
        }
    }

    public static void setup(int useCache, int rebuildCache) {
//        if (useCache == 1) {
//            templatesList = templatesListCache;
//        }

        String a = "a";
        if (rebuildCache != 1) {
            if (templatesList.size() != 0) {
//                @TODO add notification to not recognize dtw
                double result = recognize_dtw(currSearch, templatesList, "Cosine");
            }
            templatesList.add(currSearch);
            write();
        }

        if (useCache != 1 && rebuildCache == 1) {
            write();
            Snackbar.make(fab, "Rebuild cache. DONE", Snackbar.LENGTH_LONG)
                    .setAction("Rebuild cache. DONE", null).show();
            Log.d("Rebuild cache: ","Done");
        }
    }

    public static void writeData(double[][] result, double[][] spec, String filename) {
        Template tmp = new Template(result, filename, spec);
        templatesList.add(tmp);
    }

    public static void writeDataSep(double[][] result, double[][] spec, String filename) {
        Template tmp = new Template(result, filename, spec);
        currSearch = tmp;
//        templatesList.add(tmp);
    }

    public static void writeDataSpectrogram(double[][] spec, String filename,int position) {
        spectrogramView[position-1] = spec;
        spectrogramViewFilename[position-1] = filename;
    }

    public static void setSpectogramView(){
        spectrogramViewWorkers = 2;
        for (int i = 0; i < spectrogramViewWorkers; i++) {
            if (i == 0) {
                Bitmap spectro = calcSpec2.bitmapFromArray(spectrogramView[i]);
                MainActivity.left.setImageBitmap(spectro);

            } else if (i == 1) {
                Bitmap spectro2 = calcSpec2.bitmapFromArray(spectrogramView[i]);
                MainActivity.left2.setImageBitmap(spectro2);

            }
        }

        mPlayButton1.setVisibility(View.VISIBLE);
        mPlayButton2.setVisibility(View.VISIBLE);
    }


    public static double recognize_dtw(Template unknown_template,List<Template> templates,String distance_f) {
        String a = "test";
        int nbrOfTemplates = templates.size();
        String izpis = "";
        List<Double> values = new ArrayList<Double>();

        double[][] SM_lj;

        for (int i = 0; i < nbrOfTemplates; i++) {
            SM_lj = calcSpec3.simmx(unknown_template.spectro, templates.get(i).spectro, distance_f);
            double sim = calcSpec3.dp(calcSpec2.subMatrix(SM_lj, 1.0));
            templates.get(i).similarity = sim;
            values.add(sim);
        }
        double[] valuesdouble = new double[values.size()];

        double sum = 0.0;
        for (int i = 0; i < values.size(); i++) {
            valuesdouble[i] = values.get(i);
            sum += valuesdouble[i];
        }
        double[] result = calcSpec3.FindSmallest(valuesdouble);
        double min = result[0];
        int pos = (int) result[1];
        valuesdouble[pos] = 0;

        double ave = sum / (nbrOfTemplates - 1);

        double conf = (ave - min) / ave * 100;

        String dummy = "upperGraph";
        new calcSpec3(1,unknown_template.filename, 1).execute(dummy);
        MainActivity.textleft.setText(unknown_template.filename);

        dummy = "lowerGraph";
        new calcSpec3(1,templates.get(pos).filename, 2).execute(dummy);
        MainActivity.textleft2.setText(templates.get(pos).filename);


        return 0.0;
    }

    public static void write(){
        ObjectOutput out = null;

        try {
            out = new ObjectOutputStream(new FileOutputStream(new File(Environment.getExternalStorageDirectory().getPath(),"AudioRecorder")+File.separator+"cache.srl"));
            out.writeObject(templatesList);
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
