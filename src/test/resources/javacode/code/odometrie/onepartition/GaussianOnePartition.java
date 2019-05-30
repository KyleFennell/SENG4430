package com.dcaiti.traceloader.odometrie.onepartition;

import java.util.Arrays;

import org.apache.commons.math3.analysis.function.Gaussian;

/** class to construct a partition of unity with gaussian distributions
 * 
 * it construct gaussian distributions with the help of sigma
 * 
 * @author nkl - Nicolas Klenert
 *
 */
public class GaussianOnePartition extends OnePartition{
    
    double[] peaks;
    double[] sigmas;
    double[] importance;
    //value to calculate the sigmas: distributions are crossing at half of their maximum
    static final double FWHM = 2.35482;
    //value to calculate the sigmas: distributions are crossing at tenth of their maximum
    static final double FWTM = 4.29195;
    
    Gaussian[] functions;

    /** create a GaussianOnePartition with equidistant peaks and a width,
     *  so that the distributions are crossing at half of their maximum
     * 
     * @param size the number of distributions
     */
    public GaussianOnePartition(int size){
        this(size, 1);
    }
    
    /** create a GaussianOnePartition with equidistant peaks and
     *  multiply their width with the given sigma
     *  
     * @see com.dcaiti.traceloader.odometrie.onepartition.GaussianOnePartition#GaussianOnePartition(double[], double[], double[]) Constructor(double[],double[],double[])
     * 
     * @param size number of distributions
     * @param sigma
     */
    public GaussianOnePartition(int size, double sigma){
        super(size);
        this.peaks = this.getUniformPeaksUnclipped();
        double[] sigmas = new double[this.size];
        for(int i = 0; i < this.size; ++i){
            sigmas[i] = sigma;
        }
        initSigmas(sigmas);
        initImportance(null);
        initGaussian();
    }

    /** Constructor for gaussian distribution.
     * 
     * @param peaks points of the peaks
     * @param sigmas values which are mutliplied to sigma (higher value -> more distributed)
     * @param importance lift the peak of a distribution -> higher influence of the specific distribution
     */
    public GaussianOnePartition(double[] peaks, double[] sigmas, double[] importance) {
        super(peaks == null ? 2 : peaks.length);
        if(this.allowedPeaks(peaks, false)){
            this.peaks = peaks;
        }else{
            this.peaks = OnePartition.filterPeaks(peaks);
        }
        initSigmas(sigmas);
        initImportance(importance);
        initGaussian();
    }
    
    private void initSigmas(double[] sigmas){
        if(sigmas == null){
            this.sigmas = new double[this.peaks.length];
        }else{
            this.sigmas = Arrays.copyOf(sigmas, this.peaks.length);
        }
        for(int i = 0; i < this.sigmas.length; ++i){
            if(this.sigmas[i] == 0){
                this.sigmas[i] = 1d;
            }
            double width;
            if(i == 0){
                width = this.peaks[0];
                width += (this.peaks[1] - this.peaks[0])/2d;
            }else if(i == this.size-1){
                width = 1 - this.peaks[this.size-1];
                width += (this.peaks[this.size-1] - this.peaks[this.size-2]) /2d;
            }else{
                width = this.peaks[i+1] - this.peaks[i-1];
                width /= 2;
            }
            this.sigmas[i] *= width;
            this.sigmas[i] /= FWHM;
        }
    }
    
    private void initImportance(double[] importance) {
        if(importance == null){
            importance = new double[this.size];
        }else{
            importance = Arrays.copyOf(importance, this.size);
        }
        for(int i = 0; i < this.size; ++i){
            if(importance[i] <= 0){
                importance[i] = 1;                
            }
        }
        this.importance = importance;
        //calculate the real importance
        for(int i = 0; i < this.size; ++i){
            this.importance[i] /= this.sigmas[i]*Math.sqrt(2*Math.PI);
        }
    }
    
    private void initGaussian(){
        this.functions = new Gaussian[this.size];
        for(int i = 0; i < this.size; ++i){
            this.functions[i] = new Gaussian(this.importance[i],this.peaks[i],this.sigmas[i]);
        }
    }

    @Override
    public double[] getDistribution(double value) {
        value = Math.max(value, 0);
        value = Math.min(value, 1);
        
        double[] weights = new double[this.size];
        for(int i = 0; i < this.size; ++i){
            weights[i] = this.functions[i].value(value);
        }
        
        //normalize
        return OnePartition.normalize(weights);
//        return weights;
    }
    
}
