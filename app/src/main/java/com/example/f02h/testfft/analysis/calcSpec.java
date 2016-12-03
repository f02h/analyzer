package com.example.f02h.testfft.analysis;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.example.f02h.testfft.MainActivity;

/**
 * Created by f02h on 3. 12. 2016.
 */
public class calcSpec extends AsyncTask<String, Integer, String> {
    int fs = 0; // Sampling frequency
    int nshift = 0;// Initialise frame shift
    int nlen = 0;// Initialise frame length
    float nsegs = 0 ; //Initialise the total number of frames
    @Override
    protected String doInBackground(String... params) {
        fs = WaveTools.getFs();
        nshift = (int) Math.floor(MainActivity.tshift*fs/1000); // frame shift in samples
        nlen = (int) Math.floor(MainActivity.tlen*fs/1000);	// frame length in samples
        nsegs = 1+(float) (Math.ceil((MainActivity.audioBuf.length-(nlen))/(nshift)));
        specGram(MainActivity.audioBuf,nsegs,nshift,nlen);

        return null;

    }

    @Override
    protected void onPostExecute(String result) {
        Bitmap spectro = bitmapFromArray(MainActivity.spec);

//        Bitmap spectroCopy = spectro;

//        for (int j = 0; j < nsegs ; j++){
//            for (int i = 0; i < nlen; i++) {
//                MainActivity.left.append(Integer.toString((int) MainActivity.spec[i][j])+" ");
//            }
//        }
//        int[] pixels = new int[spectroCopy.getHeight()*spectroCopy.getWidth()];
//        int minvalue = 0;
//        int maxvalue = 0;
//        spectro.getPixels(pixels, 0, spectroCopy.getWidth(), 0, 0, spectroCopy.getWidth(), spectroCopy.getHeight());
//        for (int i=0; i<spectroCopy.getWidth()*spectroCopy.getHeight(); i++) {
////            pixels[i] = Color.rgb(scale(pixels[i],0,50,255,0),scale(pixels[i],0,50,255,100),scale(pixels[i],0,50,255,100));
//            if (pixels[i] > maxvalue) maxvalue = pixels[i];
//            if (pixels[i] < minvalue) minvalue = pixels[i];
//
//            pixels[i] =  Color.HSVToColor(getColor((double)scale(pixels[i],-50,50,0,80)));
//
//        }
//        spectroCopy.setPixels(pixels, 0, spectroCopy.getWidth(), 0, 0, spectroCopy.getWidth(), spectroCopy.getHeight());

        MainActivity.left.setImageBitmap(spectro);
    }

    public static double scale(final double valueIn, final double baseMin, final double baseMax, final double limitMin, final double limitMax) {
        return ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;
    }

    public static float[] getColor(double power)
    {
        double H = power * 0.9; // Hue (note 0.4 = Green, see huge chart below)
        double S = 0.9; // Saturation
        double B = 0.9; // Brightness

        float[] hsb = new float[3];
        hsb[0] = (float)H;
        hsb[1] = (float)S;
        hsb[2] = (float)B;
        return  new float[]{ (float)H, (float)S, (float)B };
    }

    public static Bitmap bitmapFromArray(float[][] pixels2d){
        int width = pixels2d[0].length;
        int height = pixels2d.length;
        int[] pixels = new int[width * height];
        int pixelsIndex = 0;
        for (int i = 0; i < height; i++)
        {
            for (int j = 0; j < width; j++)
            {
//                pixels[pixelsIndex] = (int)pixels2d[j][i];
                pixels[pixelsIndex] =  Color.HSVToColor(getColor((double)scale((int)pixels2d[i][j],-50,50,0,80)));
                pixelsIndex ++;
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }


    /**
     * Calculates the spectrogram or log spectrum of the
     * audio signal
     * @param data
     * @param nsegs
     * @param nshift
     * @param seglen
     */
    public void specGram(float [] data, float nsegs, int nshift, int seglen){

        MainActivity.spec = new float[seglen][(int)nsegs];
        MainActivity.array2 = new float[seglen];
        MainActivity.seg_len = seglen;
        MainActivity.n_segs = nsegs;
        MainActivity.n_shift = nshift;
        MainActivity.time_array = new float[data.length];
        MainActivity.time_array = data;

        MainActivity.framed = new float [MainActivity.seg_len][(int)MainActivity.n_segs];
        MainActivity.framed = FrameSig();
        minmax(MainActivity.framed,MainActivity.seg_len,(int)MainActivity.n_segs);
        meansig((int)MainActivity.n_segs);

        MainActivity.array = new float[MainActivity.seg_len*2];


        MainActivity.res=new float[MainActivity.seg_len];
        MainActivity.fmag = new float[MainActivity.seg_len];
        MainActivity.flogmag = new float[MainActivity.seg_len];

        MainActivity.mod_spec =new float[MainActivity.seg_len];
        MainActivity.real_mod = new float[MainActivity.seg_len];
        MainActivity.imag_mod = new float[MainActivity.seg_len];
        MainActivity.real = new double[MainActivity.seg_len];
        MainActivity.imag= new double[MainActivity.seg_len];
        MainActivity.mag = new double[MainActivity.seg_len];
        MainActivity.phase = new double[MainActivity.seg_len];
        MainActivity.logmag = new double[MainActivity.seg_len];
        MainActivity.nmag = new double[MainActivity.seg_len];
        for (int i = 0;i<MainActivity.seg_len*2;i++){
            MainActivity.array[i] = 0;
        }


        for (int j=0;j<nsegs; j++){
            FFT fft = new FFT(MainActivity.seg_len*2, 8000);
            for (int i = 0;i<MainActivity.seg_len;i++){
                MainActivity.array[i] = MainActivity.framed [i][j];
            }
            fft.forward(MainActivity.array);
            MainActivity.fft_cpx=fft.getSpectrum();
            MainActivity.tmpi = fft.getImaginaryPart();
            MainActivity.tmpr = fft.getRealPart();


            for(int i=0;i<MainActivity.seg_len;i++)
            {

                MainActivity.real[i] = (double)MainActivity.tmpr[i];
                MainActivity.imag[i] = (double) MainActivity.tmpi[i];

                MainActivity.mag[i] = Math.sqrt((MainActivity.real[i]*MainActivity.real[i]) + (MainActivity.imag[i]*MainActivity.imag[i]));
                MainActivity.mag[i] = Math.abs(MainActivity.mag[i]/MainActivity.seg_len);


                MainActivity.logmag[i] = 20*Math.log10(MainActivity.mag[i]);
                MainActivity.phase[i]=Math.atan2(MainActivity.imag[i],MainActivity.real[i]);

                /****Reconstruction****/
                //real_mod[i] = (float) (mag[i] * Math.cos(phase[i]));
                //imag_mod[i] = (float) (mag[i] * Math.sin(phase[i]));
                MainActivity.spec[(MainActivity.seg_len-1)-i][j] = (float) MainActivity.logmag[i];

                //Log.d("SpecGram","log= "+logmag[i]);
            }
        }
        minmaxspec(MainActivity.spec,MainActivity.seg_len,(int)nsegs);
        meanspec((int)nsegs);
        //fft.inverse(real_mod,imag_mod,res);

    }
    /**
     * Calculates the mean of the fft magnitude spectrum
     * @param nsegs
     */
    private void meanspec(int nsegs) {
        float sum = 0;
        for (int j=1; j<(int)nsegs; j++) {
            for (int i = 0;i<MainActivity.seg_len;i++){

                sum += MainActivity.spec[i][j];
            }
        }


        sum = sum/(nsegs*MainActivity.seg_len);
        MainActivity.mux = sum;

    }
    /**
     * Calculates the min and max of the fft magnitude
     * spectrum
     * @param spec
     * @param seglen
     * @param nsegs
     * @return
     */
    public static float minmaxspec(float[][] spec, int seglen, int nsegs) {

        MainActivity.smin = (float) 1e35;
        MainActivity.smax = (float) -1e35;
        for (int j=1; j<nsegs; j++) {
            for (int i = 0;i<seglen;i++){

                if (MainActivity.smax < spec[i][j]) {
                    MainActivity.smax =  spec[i][j];  // new maximum
                }else if(MainActivity.smin > spec[i][j]) {
                    MainActivity.smin=spec[i][j];   // new maximum
                }
            }
        }
        return MainActivity.smax;
    }
    /**
     * Calculates the min and max value of the framed signal
     * @param spec
     * @param seglen
     * @param nsegs
     * @return
     */
    public static float minmax(float[][] spec, int seglen, int nsegs) {

        MainActivity.min = (float) 1e35;
        MainActivity.max = (float) -1e35;
        for (int j=1; j<nsegs; j++) {
            for (int i = 0;i<seglen;i++){

                if (MainActivity.max < spec[i][j]) {
                    MainActivity.max =  spec[i][j];  // new maximum
                }else if(MainActivity.min > spec[i][j]) {
                    MainActivity.min=spec[i][j];   // new maximum
                }
            }
        }
        return MainActivity.max;
    }

    /**
     * Calculates the mean of the framed signal
     * @param nsegs
     */
    private void meansig(int nsegs) {
        float sum = 0;
        for (int j=1; j<(int)nsegs; j++) {
            for (int i = 0;i<MainActivity.seg_len;i++){

                sum += MainActivity.framed[i][j];
            }
        }


        sum = sum/(nsegs*MainActivity.seg_len);
        MainActivity.smux = sum;


    }

    /**
     * Frames up input audio
     * @return
     */

    public float[][] FrameSig(){
        float [][] temp = new float [MainActivity.seg_len][(int)MainActivity.n_segs];
        float [][] frame = new float [MainActivity.seg_len][(int)MainActivity.n_segs];
        float padlen = (MainActivity.n_segs-1)*MainActivity.n_shift+MainActivity.seg_len;


        MainActivity.wn = hamming(MainActivity.seg_len);
        for (int i = 0; i < MainActivity.n_segs;i++){

            for (int j = 0;j<MainActivity.seg_len;j++){

                temp[j][i] = MainActivity.time_array[i*MainActivity.n_shift+j];//*wn[i];

            }
        }
        for (int i = 0; i < MainActivity.n_segs;i++){			// Windowing

            for (int j = 0;j<MainActivity.seg_len;j++){

                frame[j][i] = temp[j][i]*MainActivity.wn[j];

            }
        }
        return frame;

    }
    /**
     * Calculates a hamming window to reduce
     * spectral leakage
     * @param len
     * @return
     */
    public float[] hamming(int len){
        float [] win = new float [len];
        for (int i = 0; i<len; i++){
            win[i] = (float) (0.54-0.46*Math.cos((2*Math.PI*i)/(len-1)));
        }
        return win;
    }

}