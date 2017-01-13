package com.example.f02h.testfft.analysis;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.example.f02h.testfft.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by f02h on 3. 12. 2016.
 */
public class calcSpec3 extends AsyncTask<String, Integer, String> {
    int fs = 0; // Sampling frequency
    int nshift = 0;// Initialise frame shift
    int nlen = 0;// Initialise frame length
    float nsegs = 0 ; //Initialise the total number of frames
    int templateNumber;
    String file;
    float[] samples;
    double [][] spec;
    double [][] spec1;
    int seg_len;
    float n_segs;
    int n_shift;
    double smin = (double) 1e35;
    double smax = (double) -1e35;
    float [][] framed;
    float [] time_array;
    double[][] result;
    int spectOnly = 0; // draw spectrogram to upper/lower graph

    public calcSpec3(int templateNbumber1, String file1, int spectOnly) {
        super();
        this.templateNumber = templateNbumber1;
        this.file = file1;
        this.samples = WaveTools.wavread(file1, MainActivity.getAppContext());;
        this.spectOnly = spectOnly;

    }

    @Override
    protected String doInBackground(String... params) {
        fs = WaveTools.getFs();
        nshift = (int) Math.floor(MainActivity.tshift*fs/1000); // frame shift in samples
        nlen = (int) Math.floor(MainActivity.tlen*fs/1000);	// frame length in samples
        nsegs = 1+(float) (Math.ceil((samples.length-(nlen))/(nshift)));
        spec = new double[(int)nlen][(int)nsegs];
        spec1 = new double[(int)nlen][(int)nsegs];
        seg_len = nlen;
        n_segs = nsegs;
        n_shift = nshift;
        framed = new float [seg_len][(int)n_segs];
        time_array = new float[samples.length];
        specGram(samples,nsegs,nshift,nlen);

        if (spectOnly != 0) {
            MainActivity.writeDataSpectrogram(spec,file,spectOnly);
        } else {
            calculateMfcc();
            MainActivity.writeDataSep(result, spec, file);
        }
            return null;
    }

    @Override
    protected void onPostExecute(String result1) {
        if (spectOnly == 0) {
            MainActivity.myHandler.sendEmptyMessage(0);
        } else {
            MainActivity.spectogramViewHandler.sendEmptyMessage(0);
        }
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


        float [] array2 = new float[seglen];
//        int seg_len = seglen;
//        float n_segs = nsegs;
//        int n_shift = nshift;
        time_array = data;


        framed = FrameSig();
        minmax(framed,seg_len,(int)n_segs);
        meansig((int)n_segs);

        float [] array = new float[seg_len*2];


        float [] res=new float[seg_len];
        float [] fmag = new float[seg_len];
        float [] flogmag = new float[seg_len];

        float [] mod_spec =new float[seg_len];
        float [] real_mod = new float[seg_len];
        float [] imag_mod = new float[seg_len];
        double [] real = new double[seg_len];
        double [] imag= new double[seg_len];
        double [] mag = new double[seg_len];
        double [] magTmp = new double[seg_len];
        double [] phase = new double[seg_len];
        double [] logmag = new double[seg_len];
        double [] nmag = new double[seg_len];
        for (int i = 0;i<seg_len*2;i++){
            array[i] = 0;
        }


        for (int j=0;j<nsegs; j++){
            FFT fft = new FFT(seg_len*2, 8000);
            for (int i = 0;i<seg_len;i++){
                array[i] = framed [i][j];
            }
            fft.forward(array);
            float [] fft_cpx=fft.getSpectrum();
            float [] tmpi = fft.getImaginaryPart();
            float [] tmpr = fft.getRealPart();


            for(int i=0;i<seg_len;i++)
            {

                real[i] = (double)tmpr[i];
                imag[i] = (double) tmpi[i];

                mag[i] = Math.sqrt((real[i]*real[i]) + (imag[i]*imag[i]));
                magTmp[i] = mag[i];
                mag[i] = Math.abs(mag[i]/seg_len);


                logmag[i] = 20*Math.log10(mag[i]);
                phase[i]=Math.atan2(imag[i],real[i]);

                /****Reconstruction****/
                //real_mod[i] = (float) (mag[i] * Math.cos(phase[i]));
                //imag_mod[i] = (float) (mag[i] * Math.sin(phase[i]));
                spec[(seg_len-1)-i][j] = logmag[i];
                spec1[(seg_len-1)-i][j] = (1.0/512)*(magTmp[i] * magTmp[1]); // TODO research

//                Log.d("SpecGram","log= "+logmag[i]);
            }
        }
        minmaxspec(spec,seg_len,(int)nsegs);
        meanspec((int)nsegs);
    }

    private void calculateMfcc() {
        int nbrOfMelFilters = 40;
        int startMelFreq = 0;
        double endMelFreq = 2595 * Math.log10(1 + (8000 / 2.0) / 700.0);
        double [] melFilters = linearScale(startMelFreq, endMelFreq, nbrOfMelFilters + 2);
        double [] mel2Hz = new double[melFilters.length];
        for (int i = 0; i < melFilters.length; i++) {
            mel2Hz[i] = (700 * (Math.pow(10, melFilters[i] / 2595) - 1));
        }

        double []  bin = new double[melFilters.length];
        for (int i = 0; i < melFilters.length; i++) {
            bin[i] = Math.floor((512 + 1) * mel2Hz[i] / 8000);
        }

        double [][] fbank = new double[nbrOfMelFilters][(int)(512 / 2)];

        for (int i = 0; i < fbank.length; i++) {
            for (int j = 0; j < fbank[0].length; j++) {
                fbank[i][j] = 0.0;
            }
        }

        for (int i = 1; i < nbrOfMelFilters+1; i++) {
            int fleft = (int)bin[i-1];
            int fcenter = (int)bin[i];
            int fright = (int)bin[i+1];

            for (int j = fleft; j < fcenter; j++) {
                fbank[i - 1][j] = ((j - bin[i - 1]) / (bin[i] - bin[i - 1]));
            }
            for (int j = fcenter; j < fright; j++) {
                fbank[i - 1][j] = (((bin[i + 1] - j) / (bin[i + 1] - bin[i])));
            }
        }

        spec1 = multiplyByMatrix(transposeMatrix(spec1), transposeMatrix(fbank));

        for (int i = 0; i < spec1.length; i++) {
            for (int j = 0; j < spec1[0].length; j++) {
                if (spec1[i][j] == 0) {
                    spec1[i][j] = 2.22e-16;
                }
                spec1[i][j] = 20 * Math.log10(spec1[i][j]);
            }
        }

        double [] test = dct(spec1[0]);
        double [][] test2dct = dct2d(spec1);
        double [][] mfcc = new double[test2dct.length][12];
        for (int i = 0; i < test2dct.length; i++) {
            for (int j = 1; j < 12+1; j++) {
                mfcc[i][j-1] = test2dct[i][j];
            }
        }

        double [][] deltas = new double[mfcc.length][mfcc[0].length];

        for (int i = 0; i < mfcc.length; i++) {
            deltas[i] = delta(mfcc[i], 2);
        }

        double [][] deltasdeltas = new double[deltas.length][deltas[0].length];

        for (int i = 0; i < deltas.length; i++) {
            deltasdeltas[i] = delta(deltas[i], 2);
        }

        // TODO merge mfcc + delta + deltadelta into result

        result = new double[mfcc.length][36];

        for (int i = 0; i < mfcc.length; i++) {
            for (int j = 0; j < 36; j++) {
                if (0 <= j && j < 12) {
                    result[i][j] = mfcc[i][j];
                } else if (12 <= j && j < 24) {
                    result[i][j] = deltas[i][j-12];
                } else if (24 <= j && j < 36) {
                    result[i][j] = deltasdeltas[i][j-24];
                }
            }
        }
    }

    public static double recognize_dtw(Template unknown_template,Template[] templates,String distance_f) {


        int nbrOfTemplates = templates.length;
        String izpis ="";
        List<Double> values = new ArrayList<Double>();

        double[][] SM_lj;

        for (int i = 0; i < nbrOfTemplates; i++) {
            SM_lj = simmx(unknown_template.spectro, templates[i].spectro, distance_f);
            double sim = dp(subMatrix(SM_lj, 1.0));
            templates[i].similarity = sim;
            values.add(sim);
        }
        double [] valuesdouble = new double[values.size()];

        double sum = 0.0;
        for (int i = 0; i < values.size(); i++) {
            valuesdouble[i] = values.get(i);
            sum += valuesdouble[i];
        }
        double [] result = FindSmallest(valuesdouble);
        double min = result[0];
        int pos = (int)result[1];
        valuesdouble[pos] = 0;

        double ave = sum/(nbrOfTemplates-1);

        double conf = (ave-min)/ave*100;


//        for i = 1:ntemplat,
//                SM_lj = simmx(unknown_template(2), templates(i) (2), distance_f);
//        [p_lj, q_lj, D_lj, Dtw_lj]=dp(1 - SM_lj);
//        templates(i) (4) = Dtw_lj;
//        result(i) = list(templates(i) (3), Dtw_lj, p_lj, q_lj, D_lj, SM_lj);
//        values =[values;
//        Dtw_lj];
//        izpis = izpis + msprintf("|%s :%.2f", templates(i) (1), Dtw_lj);
//
//        end;
//        [minim, pos]=min(values);
//        values(pos) = 0;
//        ave = sum(values) / (ntemplat - 1);
//        conf = (ave - minim) / ave * 100;
//
//        if (unknown_template(3) == templates(pos) (3))then
//                result = '+++++';
//        ret = 1;
//        else
//        result = '.....';
//        ret = 0;
//        end;
//
//
//        final=
//        msprintf("\n%sRecognized: %s [%.2f] Confidence %.1f%% [%.2f vs %.2f]\n\n\n", result, templates(pos)
//        (3), minim, conf, minim, ave);
//        mprintf("DTW: %s || %s", izpis, final);
//        mprintf("\n%sRecognized: %s [%.2f] Confidence %.1f%% [%.2f vs %.2f]\n\n\n", result, templates(pos)
//        (3), minim, conf, minim, ave);
        return 0.0;
    }

    public static double[][] simmx(double[][] A,double[][] B,String Distance) {
        double[] EA = new double[A.length];
        double[] EB = new double[B.length];

        double[][] tempA = A;
        for (int i = 0; i < tempA.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < tempA[0].length; j++) {
                sum += (tempA[i][j] * tempA[i][j]);
            }
            EA[i] = Math.sqrt(sum);
        }
        double[][] tempB = B;
        for (int i = 0; i < tempB.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < tempB[0].length; j++) {
                sum += tempB[i][j] * tempB[i][j];
            }
            EB[i] = Math.sqrt(sum);
        }

        int ncA = A.length;
        int ncB = B.length;

        double[][] M = new double[ncA][ncB];
        for (int i = 0; i < ncA; i++) {
            for (int j = 0; j < ncB; j++) {
                M[i][j] = 0.0;
            }
        }
        double [][] tempEAB = multiplyByMatrix(EA,EB);
        double [][] tempM = multiplyByMatrix(A, transposeMatrix(B));

        for (int i = 0; i < ncA; i++) {
            for (int j = 0; j < ncB; j++) {
                if (Distance == "Cosine") {
                    M[i][j] = tempM[i][j] / tempEAB[i][j];
                } else if (Distance == "Euclidean") {
                } else if (Distance == "Chebyshev") {
                }
            }
        }
        return M;
    }

    public static double dp (double[][] M) {

        int r = M.length;
        int c = M[0].length;
//// costs
        double[][] D = new double[r+1][c+1];

        for (int i = 0; i < r + 1; i++) {
            if (i == 0) {
                for (int j = 0; j < c + 1; j++) {
                    D[i][j] = Double.POSITIVE_INFINITY;
                }
            }
            D[i][0] = Double.POSITIVE_INFINITY;
        }

        D[0][0] = 0;
        for (int i = 1; i < r+1; i++ ) {
            for (int j = 1; j < c+1; j++) {
                D[i][j] = M[i-1][j-1];
            }
        }

        double[][] phi = new double[r][c];

//// traceback
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                double[] input = {D[i][j], D[i][j + 1], D[i + 1][j]};
                double [] result = FindSmallest(input);
                double dmax = result[0];
                double tb = result[1]+1;
                D[i+1][j+1] = D[i+1][j+1]+dmax;
                phi[i][j] = tb;
            }
        }

//// Traceback from top left
        int i = r;
        int j = c;
        double startp = i;
        double startq = j;
        List<Double> p = new ArrayList<Double>();
        p.add((double)i);
        List<Double> q = new ArrayList<Double>();
        q.add((double)j);

        while ((i > 1) && j > 1) {
            double tb = phi[i-1][j-1];
            if (tb == 1) {
                i = i-1;
                j = j-1;
            } else if (tb == 2) {
                i = i-1;
            } else if (tb == 3) {
                j = j-1;
            }

            p.add((double)i);
            q.add((double)j);
        }

        double[][] result = new double[D.length-1][D[0].length-1];
        for (int o = 1; o < r+1; o++) {
            for (int u = 1; u < c+1; u++) {
                result[o-1][u-1] = D[o][u];
            }
        }
        double toReturn = result[r-1][c-1];
        return toReturn;
    }

    public static double FastDTW(double[][] input, double[][] template)
    {
        int rows = input.length;
        int columns = template.length;
        if (rows < (double)(columns / 2) || columns < (double)(rows / 2))
        {
            return Double.MAX_VALUE;
        }
        double[][] DTW = new double[rows][columns];
        DTW[0][0] = 0.0;
        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < columns; j++)
            {
                double cost = distance(input[i], template[j]);
                if (i == 0 && j == 0)
                    DTW[i][j] = cost;
                else if (i == 0)
                DTW[i][j] = cost + DTW[i][j - 1];
                else if (j == 0)
                DTW[i][j] = cost + DTW[i - 1][j];
                else
                DTW[i][j] = (cost + Math.min(DTW[i - 1][j], DTW[i - 1][j - 1]));// insert ,match
            }
        }
        return DTW[rows - 1][columns - 1];
    }

    public static double PrunedDTW(double[][] input, double[][] template)
    {
        int rows = input.length;
        int columns = template.length;
        if (rows < (double)(columns / 2) || columns < (double)(rows / 2))
        {
            return Double.MAX_VALUE;
        }
        double cost;
        double[][] DTW = new double[rows+1][columns+1];
//        int w = Math.abs(columns - rows);// window length -> |rows - columns|<= w
        double percent = 0.1;
        int tmpd = columns - rows;
        int w = Math.abs((int)(tmpd - (tmpd*percent)));
        for (int i = 1; i <= rows; i++)
        {
            for (int j = Math.max(1, i - w); j <= Math.min(columns, i + w); j++)
            {
                if (DTW[i - 1][j] == 0)
                DTW[i - 1][j] = Double.MAX_VALUE;
                if (DTW[i - 1][j - 1] == 0)
                DTW[i - 1][j - 1] = Double.MAX_VALUE;
                DTW[0][0] = 0;
                cost = distance(input[i - 1], template[j - 1]);// frames 0 based
                DTW[i][j] = (cost + Math.min(DTW[i - 1][j], DTW[i - 1][j - 1]));// insert ,match
            }
        }
        return DTW[rows][columns];
    }

    public static float distance(double[] frame, double[] frame2)
    {
        double tempSum = 0;
        for (int i = 0; i < frame.length; i++)
            tempSum += Math.pow(Math.abs(frame[i] - frame2[i]), 2);
        return (float)(Math.sqrt(tempSum)/1000);
    }

    public static double[] FindSmallest (double [] input){//start method

        int index = 0;
        double min = input[index];
        for (int i=1; i<input.length; i++){

            if (input[i] < min ){
                min = input[i];
                index = i;
            }
        }
        double[] toReturn = {min,index};
        return toReturn ;
    }

    public static double[][] dct2d (double[][] input) {
        int width = input.length;
        int height = input[0].length;


        double [][] tmp = new double[width][height];
        for (int i = 0; i < width; i++) {
            tmp[i] = dct(input[i]);
        }

//        double[][] ttmp = transposeMatrix(tmp);
//        width = ttmp.length;
//        for (int i = 0; i < width; i++) {
//            ttmp[i] = dct(ttmp[i]);
//        }

//        return transposeMatrix(ttmp);

        return tmp;
    }

    public static double[] delta( double[] input, int N) {

        int nbrFrames = input.length+ 2*N;
        double[] result = new double[nbrFrames+2*N];

        for (int i = 0; i < nbrFrames; i++) {

            if (i == 0 || i == 1) {
                result[i] = input[0];
            } else if (i == nbrFrames-1 || i == nbrFrames-2) {
                result[i] = input[input.length-1];
            } else {
                result[i] = input[i-2];
            }
        }

        double denom = 0.0;
        for (int i = 1; i < N+1; i++) {
            denom += 2 * i * i;
        }

        double [] deltas = new double[nbrFrames -2*N];

        for (int i = 0; i < nbrFrames-2*N; i++) {
            double sum = 0.0;
            for (int j = -1*N ; j < N+1; j++) {
                sum += j * result[N + i + j];
            }
            deltas[i] = sum / denom;
        }

        return deltas;
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

    public static double[][] subMatrix(double[][] matrix, double sub) {

        double[][] returnMatrix = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                returnMatrix[i][j] = sub - matrix[i][j];
            }
        }
        return returnMatrix;
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

    public static double[][] multiplyByMatrix(double[] matrix1, double[] matrix2) {
        int mRRowLength = matrix1.length;    // m result rows length
        int mRColLength = matrix2.length; // m result columns length
        double[][] mResult = new double[mRRowLength][mRColLength];
        for(int i = 0; i < mRRowLength; i++) {         // rows from m1
            for(int j = 0; j < mRColLength; j++) {     // columns from m2
                mResult[i][j] += matrix1[i] * matrix2[j];
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
            for (int i = 0;i<seg_len;i++){

                sum += spec[i][j];
            }
        }


        sum = sum/(nsegs*seg_len);
        float mux = sum;

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

        double smin = (double) 1e35;
        double smax = (double) -1e35;
        for (int j=1; j<nsegs; j++) {
            for (int i = 0;i<seglen;i++){

                if (smax < spec[i][j]) {
                    smax =  spec[i][j];  // new maximum
                }else if(smin > spec[i][j]) {
                    smin=spec[i][j];   // new maximum
                }
            }
        }
        return smax;
    }
    /**
     * Calculates the min and max value of the framed signal
     * @param spec
     * @param seglen
     * @param nsegs
     * @return
     */
    public static float minmax(float[][] spec, int seglen, int nsegs) {

        float min = (float) 1e35;
        float max = (float) -1e35;
        for (int j=1; j<nsegs; j++) {
            for (int i = 0;i<seglen;i++){

                if (max < spec[i][j]) {
                    max =  spec[i][j];  // new maximum
                }else if(min > spec[i][j]) {
                    min=spec[i][j];   // new maximum
                }
            }
        }
        return max;
    }

    /**
     * Calculates the mean of the framed signal
     * @param nsegs
     */
    private void meansig(int nsegs) {
        float smux;
        float sum = 0;
        for (int j=1; j<(int)nsegs; j++) {
            for (int i = 0;i<seg_len;i++){

                sum += framed[i][j];
            }
        }


        sum = sum/(nsegs*seg_len);
        smux = sum;
    }

    /**
     * Frames up input audio
     * @return
     */

    public float[][] FrameSig(){
        float [][] temp = new float [seg_len][(int)n_segs];
        float [][] frame = new float [seg_len][(int)n_segs];
        float padlen = (n_segs-1)*n_shift+seg_len;

        float [] wn = hamming(seg_len);
        for (int i = 0; i < n_segs;i++){

            for (int j = 0;j<seg_len;j++){
                temp[j][i] = time_array[i*n_shift+j];//*wn[i];
            }
        }
        for (int i = 0; i < n_segs;i++){			// Windowing
            for (int j = 0;j<seg_len;j++){
                frame[j][i] = temp[j][i]*wn[j];
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