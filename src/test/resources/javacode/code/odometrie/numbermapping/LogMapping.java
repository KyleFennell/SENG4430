package com.dcaiti.traceloader.odometrie.numbermapping;

import org.apache.commons.math3.analysis.function.Log;

/** the standard mapping of a logarithms. Can be used for NumberMapping.
 * 
 * @see com.dcaiti.traceloader.odometrie.numbermapping.NumberMapping NumberMapping
 * @author nkl - Nicolas Klenert
 *
 */
public class LogMapping extends Log implements UnivariateBorderFunction {
    
    @Override
    public double[] getFromBorder() {
        return new double[]{1,10};
    }

    @Override
    public double[] getToBorder() {
        double[] fromBorder = this.getFromBorder();
        return new double[]{this.value(fromBorder[0]),this.value(fromBorder[1])};
    }

}
