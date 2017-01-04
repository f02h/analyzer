package com.example.f02h.testfft.analysis;

import java.io.Serializable;

/**
 * Created by f02h on 9. 12. 2016.
 */
public class Template implements Serializable {
    private static final long serialVersionUID = -29238982928391L;
    public  String filename;

    public  double[][] spectro;
    public  String represents;
    public  double similarity;
    public double[][] realSpectro;

    public Template(){}

    public Template(double [][] spectro1, String file, double[][] spec) {
        this.spectro = spectro1;
        this.filename = file;
        this.represents = file;
        this.realSpectro = spec;
    }
//
//    public static void setFilename(String name) {
//        this.filename = name;
//    }
//
//    public static String getFilename() {
//        return filename;
//    }
//
//    public static void setSpectro (double[][] matrix) {
//        spectro = matrix;
//    }
//
//    public static void setSimilarity (double sim) {similarity = sim;}
//
//    public static double getSimilarity() {
//        return similarity;
//    }
//
//    public static double[][] getSpectro() {
//        return spectro;
//    }
//
//    public static void setRepresents (String descr) {
//        represents = descr;
//    }
//
//    public static String getRepresents() {
//        return represents;
//    }
//
//    public String toString() {
//
//        return getFilename();
//    }
}
// templates je seznam besed, ki so vsaka novi seznam z naslednjimi elementi :
// filename
// template 	.. spektrogram oz. vektor z znacilkami)
// beseda		.. beseda, ki jo template predstavlja
// podobnost    .. zaenkrat 0