package com.example.f02h.testfft.analysis;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.example.f02h.testfft.MainActivity;

import java.util.Arrays;

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
        calculateMel();
        int test = 1;
    }

    public static double scale(final double valueIn, final double baseMin, final double baseMax, final double limitMin, final double limitMax) {
        return ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;
    }

    public static double[] linearScale(final double baseMin, final double baseMax, int nbrOfValues) {
        double[] result = new double[nbrOfValues];
        double step = (baseMax - baseMin) / ((double)nbrOfValues - 1);
        int index = 0;
        double value = 0.0;
        for (int i = 0; i < nbrOfValues; i++) {
            result[i] = value;
            value += step;
        }
        return result;
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

    public static Bitmap bitmapFromArray(double[][] pixels2d){
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

        MainActivity.spec = new double[seglen][(int)nsegs];
        MainActivity.spec1 = new double[seglen][(int)nsegs];
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
        MainActivity.magTmp = new double[MainActivity.seg_len];
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
                MainActivity.magTmp[i] = MainActivity.mag[i];
                MainActivity.mag[i] = Math.abs(MainActivity.mag[i]/MainActivity.seg_len);


                MainActivity.logmag[i] = 20*Math.log10(MainActivity.mag[i]);
                MainActivity.phase[i]=Math.atan2(MainActivity.imag[i],MainActivity.real[i]);

                /****Reconstruction****/
                //real_mod[i] = (float) (mag[i] * Math.cos(phase[i]));
                //imag_mod[i] = (float) (mag[i] * Math.sin(phase[i]));
                MainActivity.spec[(MainActivity.seg_len-1)-i][j] = MainActivity.logmag[i];
                MainActivity.spec1[(MainActivity.seg_len-1)-i][j] = (1.0/512)*(MainActivity.magTmp[i] * MainActivity.magTmp[1]); // TODO research

//                Log.d("SpecGram","log= "+logmag[i]);
            }
        }
        minmaxspec(MainActivity.spec,MainActivity.seg_len,(int)nsegs);
        meanspec((int)nsegs);
        //fft.inverse(real_mod,imag_mod,res);

    }

    private void calculateMel() {
        int nbrOfMelFilters = 40;
        int startMelFreq = 0;
        double endMelFreq = 2595 * Math.log10(1 + (8000 / 2.0) / 700.0);
        MainActivity.melFilters = linearScale(startMelFreq, endMelFreq, nbrOfMelFilters + 2);
        MainActivity.mel2Hz = new double[MainActivity.melFilters.length];
        for (int i = 0; i < MainActivity.melFilters.length; i++) {
            MainActivity.mel2Hz[i] = (700 * (Math.pow(10, MainActivity.melFilters[i] / 2595) - 1));
        }

        MainActivity.bin = new double[MainActivity.melFilters.length];
        for (int i = 0; i < MainActivity.melFilters.length; i++) {
            MainActivity.bin[i] = Math.floor((512 + 1) * MainActivity.mel2Hz[i] / 8000);
        }

//        double [][] fbank = new double[40][(int)(512 / 2) + 1]; TODO double check why +1
        double [][] fbank = new double[40][(int)(512 / 2)];

        for (int i = 0; i < fbank.length; i++) {
            for (int j = 0; j < fbank[0].length; j++) {
                fbank[i][j] = 0.0;
            }
        }

        for (int i = 1; i < 41; i++) {
            int fleft = (int)MainActivity.bin[i-1];
            int fcenter = (int)MainActivity.bin[i];
            int fright = (int)MainActivity.bin[i+1];

            for (int j = fleft; j < fcenter; j++) {
                fbank[i - 1][j] = ((j - MainActivity.bin[i - 1]) / (MainActivity.bin[i] - MainActivity.bin[i - 1]));
            }
            for (int j = fcenter; j < fright; j++) {
                fbank[i - 1][j] = (((MainActivity.bin[i + 1] - j) / (MainActivity.bin[i + 1] - MainActivity.bin[i])));
            }
        }
//        MainActivity.spec = multiplyByMatrix(transposeMatrix(MainActivity.spec), transposeMatrix(fbank));
        MainActivity.spec1 = multiplyByMatrix(transposeMatrix(MainActivity.spec1), transposeMatrix(fbank));



//        filter_banks = numpy.where(filter_banks == 0, numpy.finfo(float).eps, filter_banks)  # Numerical Stability
//        filter_banks = 20 * numpy.log10(filter_banks)  # dB

        for (int i = 0; i < MainActivity.spec1.length; i++) {
            for (int j = 0; j < MainActivity.spec1[0].length; j++) {
                if (MainActivity.spec1[i][j] == 0) {
                    MainActivity.spec1[i][j] = 2.22e-16;
                }
                MainActivity.spec1[i][j] = 20 * Math.log10(MainActivity.spec1[i][j]);
            }
        }
        double [] test = dct(MainActivity.spec1[0]);

        double a = 0.0;
//        double [][] doubleFbank = forwardDCT(MainActivity.spec1);


//        for (int i = 0; i < MainActivity.spec1.length; i++) {
//            for (int j = 0; j < MainActivity.spec1[0].length; j++) {
//                MainActivity.spec1[i][j] = (float)(20 * Math.log10((double)MainActivity.spec1[i][j]));
//            }
//        }

    }

    /*

    Rewrite TODO

    */
    public final double[][] initCoefficients(double[][] c)
    {
        final int N = c.length;
        final double value = 1/Math.sqrt(2.0);

        for (int i=1; i<N; i++)
        {
            for (int j=1; j<N; j++)
            {
                c[i][j]=1;
            }
        }

        for (int i=0; i<N; i++)
        {
            c[i][0] = value;
            c[0][i] = value;
        }
        c[0][0] = 0.5;
        return c;
    }

    /*

    Rewrite TODO

    */
    public final double[][] forwardDCT(double[][] input)
    {


        final int N = input.length;
        double [][] tmpInput = new double[N][N];

        final double mathPI = Math.PI;
        final int halfN = N/2;
        final double doubN = 2.0*N;

        double[][] c = new double[N][N];
        c = initCoefficients(c);

        double[][] output = new double[N][N];

        for (int u=0; u<N; u++)
        {
            double temp_u = u*mathPI;
            for (int v=0; v<N; v++)
            {
                double temp_v = v*mathPI;
                double sum = 0.0;
                for (int x=0; x<N; x++)
                {
                    int temp_x = 2*x+1;
                    for (int y=0; y<N; y++)
                    {
                        sum += input[x][y] * Math.cos((temp_x/doubN)*temp_u) * Math.cos(((2*y+1)/doubN)*temp_v);
                    }
                }
                sum *= c[u][v]/ halfN;
                output[u][v] = sum;
            }
        }
        return output;
    }

    public static void dct2d (double[][] input) {
        int width = input.length;
        int height = input[0].length;

        double [][] tmp = new double[width][height];
        for (int i = 0; i < width; i++) {
            tmp[i] = dct(input[i]);
        }
    }

    public static double[] dct (double[] input)
    {
        final int N = input.length;
        double half = Math.sqrt(1.0/(2*N));
        double quad = Math.sqrt(1.0/(4*N));
        double[] result = new double[N];
        for (int k = 0; k < N ; k++) {
            double sum = 0.0;
            for (int n= 0; n < N; n++) {
                sum += input[n] * Math.cos(Math.PI * (double) k * (2*(double)n+1)/(2*N));
            }

            if (k == 0) {
                result[k] = 2 * sum;
            } else {
                result[k] = 2 * sum;
            }

//            if (k == 0) {
//                result[k] = 2 * sum * quad;
//            } else {
//                result[k] = 2 * sum * half;
//            }
        }

        return result;
    }

    public static double[][] multiplyByMatrix(double[][] matrix1, double[][] matrix2) {
        int m1ColLength = matrix1[0].length; // m1 columns length
        int m2RowLength = matrix2.length;    // m2 rows length
        if(m1ColLength != m2RowLength) return null; // matrix multiplication is not possible
        int mRRowLength = matrix1.length;    // m result rows length
        int mRColLength = matrix2[0].length; // m result columns length
        double[][] mResult = new double[mRRowLength][mRColLength];
        for(int i = 0; i < mRRowLength; i++) {         // rows from m1
            for(int j = 0; j < mRColLength; j++) {     // columns from m2
                for(int k = 0; k < m1ColLength; k++) { // columns from m1
                    mResult[i][j] += matrix1[i][k] * matrix2[k][j];
                }
            }
        }
        return mResult;
    }

    public static double[][] transposeMatrix (double[][] matrix) {
        double[][] result = new double[matrix[0].length][matrix.length];
        if (matrix.length > 0) {
            for (int i = 0; i < matrix[0].length; i++) {
                for (int j = 0; j < matrix.length; j++) {
                    result[i][j] = matrix[j][i];
                }
            }
        }
        return result;
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
    public static double minmaxspec(double[][] spec, int seglen, int nsegs) {

        MainActivity.smin = (double) 1e35;
        MainActivity.smax = (double) -1e35;
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