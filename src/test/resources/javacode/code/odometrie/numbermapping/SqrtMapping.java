package com.dcaiti.traceloader.odometrie.numbermapping;

import org.apache.commons.math3.analysis.function.Sqrt;

/** the standard mapping of a square root. Can be used for NumberMapping.
 * 
 * @see com.dcaiti.traceloader.odometrie.numbermapping.NumberMapping NumberMapping
 * @author nkl - Nicolas Klenert
 *
 */
public class SqrtMapping extends Sqrt implements UnivariateBorderFunction{

    @Override
    public double[] getFromBorder() {
        return new double[]{0,1};
    }

    @Override
    public double[] getToBorder() {
        return new double[]{0,1};
//        double[] fromBorder = this.getFromBorder();
//        return new double[]{this.value(fromBorder[0]),this.value(fromBorder[1])};
    }

}
