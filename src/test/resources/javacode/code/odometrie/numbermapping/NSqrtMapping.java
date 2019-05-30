package com.dcaiti.traceloader.odometrie.numbermapping;

import org.apache.commons.math3.util.FastMath;

/** the standard mapping of the nth root. Can be used for NumberMapping.
 * <p>
 * (yeah i know, the name is silly XD)
 * 
 * @see com.dcaiti.traceloader.odometrie.numbermapping.NumberMapping NumberMapping
 * @author nkl - Nicolas Klenert
 *
 */
public class NSqrtMapping implements UnivariateBorderFunction{

    private double root;
    
    public NSqrtMapping(double root){
        this.root = root;
    }
    
    @Override
    public double value(double x) {
        return FastMath.pow(FastMath.E, FastMath.log(x)/root);
    }

    @Override
    public double[] getFromBorder() {
        return new double[]{0,1};
    }

    @Override
    public double[] getToBorder() {
        return new double[]{0,1};
    }

}
