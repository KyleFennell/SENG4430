package com.dcaiti.traceloader.odometrie.numbermapping;

import org.apache.commons.math3.analysis.function.Sigmoid;

/** the sigmoid function as a mapping. Can be used for NumberMapping.
 * 
 * @see com.dcaiti.traceloader.odometrie.numbermapping.NumberMapping NumberMapping
 * @author nkl - Nicolas Klenert
 *
 */
public class SigmoidMapping extends Sigmoid implements UnivariateBorderFunction{
    
    double toMin;
    double toMax;
    
    public SigmoidMapping(){
        this(0,1);
    }
    
    public SigmoidMapping(double lo, double hi){
        super(lo,hi);
        this.toMax = hi;
        this.toMin = lo;
    }

    @Override
    public double[] getFromBorder() {
        return new double[]{-5,5};
    }

    @Override
    public double[] getToBorder() {
        return new double[]{this.toMin, this.toMax};
    }
    
}
