package com.cameralib.view;


import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.abs;

/**
 * Created by littt0 on 2018/6/22.
 */

public class Utilsnew {

    private static double m_cannyLowTh;  /* !< the lower threshold for Canny. */
    private static double m_cannyHighTh; /* !< the higher threshold for Canyy. */

    public void Utilsnew() {

    }


    public double getM_cannyLowTh () {
        return m_cannyLowTh;
    }


    public double getM_cannyHighTh() {
        return m_cannyHighTh;
    }


    /**
     * Find thresholds for Canny detector.
     * @param src input image.
     * @param aperture_size the window size for Canny detector.
     * @param PercentOfPixelsNotEdges the precision of pixels which not belong to edge.
     */



    /**
     *  Find thresholds for Canny detector (core function).
     * @param dx gradient of x orientation.
     * @param dy gradient of y orientation.
     * @param PercentOfPixelsNotEdges the precision of pixels which not belong to edge.
     */

}
