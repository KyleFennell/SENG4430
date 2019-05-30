package com.dcaiti.traceloader.odometrie.numbermapping;

import org.apache.commons.math3.analysis.UnivariateFunction;

/** Interface which extends UnivariateFunction. It gives them some borders of interest (which section of numbers).
 * 
 * @author nkl - Nicolas Klenert
 *
 */
public interface UnivariateBorderFunction extends UnivariateFunction{

    public double[] getFromBorder();
    public double[] getToBorder();
    
}
