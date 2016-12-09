package com.example.f02h.testfft.analysis;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.example.f02h.testfft.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
//        Log.d("SpecGram",Arrays.toString(MainActivity.spec1[1]));
        double [] test = dct(MainActivity.spec1[0]);
        double [][] test2dct = dct2d(MainActivity.spec1);
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

        double[][] A = { {1.0, 2.0} , { 3.0, 4.0} };
        double[][] B = { {3.0, 4.0} , { 1.0, 2.0} };
        double[][] testSimmx = simmx(A,B,"Cosine");

        dp(subMatrix(testSimmx, 1));

        double a = 0.0;
//        double [][] doubleFbank = forwardDCT(MainActivity.spec1);


//        for (int i = 0; i < MainActivity.spec1.length; i++) {
//            for (int j = 0; j < MainActivity.spec1[0].length; j++) {
//                MainActivity.spec1[i][j] = (float)(20 * Math.log10((double)MainActivity.spec1[i][j]));
//            }
//        }

    }

    public static double recognize_dtw(Template unknown_template,Template[] templates,String distance_f) {


        int nbrOfTemplates = templates.length;
        String izpis ="";

        double[][] SM_lj;

        for (int i = 0; i < nbrOfTemplates; i++) {
            SM_lj = simmx(unknown_template.getSpectro(), templates[i].getSpectro(), distance_f);


//            [p_lj, q_lj, D_lj, Dtw_lj]=dp(subMatrix(SM_lj, 1.0);
        }

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
//pause;
        double[] EA = new double[A.length];
        double[] EB = new double[B.length];


        double[][] tempA = transposeMatrix(A);
        for (int i = 0; i < tempA.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < tempA[0].length; j++) {
                sum += (tempA[i][j] * tempA[i][j]);
            }
            EA[i] = Math.sqrt(sum);
        }
        double[][] tempB = transposeMatrix(B);
        for (int i = 0; i < tempB.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < tempB[0].length; j++) {
                sum += tempB[i][j] * tempB[i][j];
            }
            EB[i] = Math.sqrt(sum);
        }
//        EA = sqrt(sum(A. ^ 2, 1));
//        EB = sqrt(sum(B. ^ 2, 1));


        int ncA = A.length;
        int ncB = B.length;
//        ncA = size(A, 2);
//        ncB = size(B, 2);

        double[][] M = new double[ncA][ncB];
//        M = zeros(ncA, ncB);
        for (int i = 0; i < ncA; i++) {
            for (int j = 0; j < ncB; j++) {
                M[i][j] = 0.0;
            }
        }
//        M = (A'*B) ./(EA'*EB);

        double [][] tempM = multiplyByMatrix(transposeMatrix(A), B);
        double [][] tempEAB = multiplyByMatrix(EA,EB);
        for (int i = 0; i < ncA; i++) {
            for (int j = 0; j < ncB; j++) {
                if (Distance == "Cosine") {
                    M[i][j] = tempM[i][j] / tempEAB[i][j];
                } else if (Distance == "Euclidean") {

                } else if (Distance == "Chebyshev") {

                }
            }
        }

//        for i = 1:ncA
//        for j = 1:ncB
//        if Distance == 'Cosine' then
//                // normalized inner product i.e. cos(angle between vectors)
//                //	M(i,j) = (A(:,i)'*B(:,j))/(EA(i)*EB(j));
//                // this is easier and probably faster
//                M = (A '*B) ./(EA' * EB);
//        elseif Distance=='Euclidean' then
////    M(i,j) =sqrt(sum((A(:,i)-B(:,j)).^2))/sqrt(sum((A(:,i)).^2));
//        M(i, j) = sqrt(sum((A(:,i)-B(:,j)).^2));
//        elseif Distance=='Chebyshev' then
//        M(i, j) = max(abs(A(:,i)-B(:,j)));
//        end;
//        end
//                end
//
////Normalize and reverse if measure is error and not similarity as Cosine is
//        if (Distance == 'Euclidean') then
//                M = 1 - (M / max(M));
//        elseif(Distance == 'Chebyshev') then
//                M = 1 - (M / max(M));
//        end;
        return M;
    }

   public static void dp (double[][] M) {



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
               double tb = result[1];
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

//// Strip off the edges of the D matrix before returning
//       D = D(2:r + 1, 2:c + 1);
       double[][] result = new double[D.length-1][D[0].length];
       for (int o = 1; i < r+1; o++) {
           for (int u = 1; u < c+1; u++) {
               result[o-1][u-1] = D[i][j];
           }

       }
       int qweq = 1;
//       result = D(r, c);
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
//        "" "Compute delta features from a feature vector sequence.
//
//        :param feat:A numpy array of size(NUMFRAMES by number of features) containing features.
//        Each row holds 1 feature vector.
//        :param N:For each frame, calculate delta features based on preceding and following N frames
//        :
//        returns:
//        A numpy array of size(NUMFRAMES by number of features) containing delta features.Each row
//        holds 1 delta feature vector.
//        "" "
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

//        denom = sum([2 * i * i for i in range(1, N + 1)])
        double [] deltas = new double[nbrFrames -2*N];

        for (int i = 0; i < nbrFrames-2*N; i++) {
            double sum = 0.0;
            for (int j = -1*N ; j < N+1; j++) {
                sum += j * result[N + i + j];
            }
            deltas[i] = sum / denom;
        }

//        for j in range(NUMFRAMES):
//        dfeat.append(numpy.sum([n * feat[N + j + n] for n in range(-1 * N, N + 1)],axis = 0)/denom)
//        return dfeat
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
        int m1ColLength = matrix1.length; // m1 columns length
        int m2RowLength = matrix2.length;    // m2 rows length
        if(m1ColLength != m2RowLength) return null; // matrix multiplication is not possible
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