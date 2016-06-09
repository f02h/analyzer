package com.example.f02h.testfft;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import android.content.Context;

import com.example.f02h.testfft.analysis.FFT;
import com.example.f02h.testfft.analysis.FourierTransform;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final float frequency = 440; // Note A
        float increment = (float)(2*Math.PI) * frequency / 44100;
        float angle = 0;
        float samples[] = new float[1024];
        FFT fft = new FFT( 1024, 44100 );



        for( int i = 0; i < samples.length; i++ )
        {
            samples[i] = (float)Math.sin( angle );
            angle += increment;
        }

        fft.forward( samples );
        float[] testSpectrum = fft.getSpectrum();
        DataPoint[] test = new DataPoint[testSpectrum.length];
        DataPoint[] test2 = new DataPoint[samples.length];
        for( int i = 0; i < testSpectrum.length; i++ )
        {
            test[i] = new DataPoint(i,testSpectrum[i]);
        }
        for( int i = 0; i < samples.length; i++ )
        {
            test2[i] = new DataPoint(i,samples[i]);
        }


//        Plot plot = new Plot( "Note A Spectrum", 512, 512);
//        plot.plot(fft.getSpectrum(), 1, Color.BLUE );

        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(test);
        graph.addSeries(series);
        GraphView graph2 = (GraphView) findViewById(R.id.graph2);
        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>(test2);
        graph2.addSeries(series2);
    }

}
