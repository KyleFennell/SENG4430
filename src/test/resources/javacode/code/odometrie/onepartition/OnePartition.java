package com.dcaiti.traceloader.odometrie.onepartition;

import java.util.Arrays;

import com.dcaiti.traceloader.odometrie.LineChart;

/** class to define a distribution of unity. Used to get the weight-distribution over time.
 * 
 * @author nkl - Nicolas Klenert
 *
 */
public abstract class OnePartition {

    int size;
    double error = 0.0001;
    int steps = 100;
    
    /** OnePartitions has to be at least size of 2 (otherwise the partition is trivial)
     * 
     * @param size
     */
    public OnePartition(int size){
        size = Math.max(size, 2);
        this.size = size;
    }
    
    /** get the number of weights you get from this partition
     * 
     * @return
     */
    public int getSize(){
        return this.size;
    }
    
    /** function to test if this Partition is correct (the right size and small enough error)
     * 
     * @return
     */
    public boolean isCorrect(){
        double[] dis = this.getDistribution(0);
        if(dis.length != this.size){
            return false;
        }
        if(Math.abs(1 - this.sum(dis)) > this.error){
            return false;
        }
        for(int i = 1; i < steps; ++i){
            double val = (double)i/steps;
            if(Math.abs(1 - sum(val)) > this.error){
                return false;
            }
        }
        
        return true;
    }
    
    /** the accepted error for the difference between the sum of the distribution of unity and 1
     * 
     * @param error
     */
    public void setAcceptedError(double error){
        this.error = error;
    }
    
    public double getAcceptedError(){
        return this.error;
    }
    
    protected double sum(double[] values){
        double sum = 0;
        for(int i = 0; i < values.length; ++i){
            sum += values[i];
        }
        return sum;
    }
    
    protected double sum(double value){
        return this.sum(this.getDistribution(value));
    }

    public void visualize(){
        this.visualize(100);
    }
    
    /** function to plot the distribution with help of the LineChart
     * 
     * @param steps how many datapoints should be calculated
     */
    public void visualize(int steps){
        double[][] weights = new double[steps][this.size];
        double tick = 1.0/(steps-1);
        LineChart chart = new LineChart("WeightDistribution of "+this.getClass().getSimpleName());
        
        for(int i = 0; i < steps; ++i){
            weights[i] = this.getDistribution(i*tick);
        }
        
        double[][] w = new double[this.size][steps];
        for(int i = 0; i < steps; ++i){
            for(int j = 0; j < this.size; ++j){
                w[j][i] = weights[i][j];
            }
        }
        
        for(int i = 0; i < this.size; ++i){
            chart.addLinearData(w[i], 0, tick, "Weight of Piece Number "+i);
        }
        
        //show weigths
        chart.initChart("Progress", "Weight");
        chart.showChart();
        chart.setRange(0,1);
    }
    
    public static double[] normalize(double[] weight){
        double[] w = new double[weight.length];
        double sum = 0d;
        for(int i = 0; i < w.length; ++i){
            sum += weight[i];
        }
        for(int i = 0; i < w.length; ++i){
            w[i] = weight[i] / sum;
        }
        return w;
    }
    
    /** checks if peaks are allowed. They have to be sorted! 
     * 
     * @param peaks
     * @param borderPeaks if true, the peaks have to be clipped (first one by 0, last one by 1)
     * @return
     */
    public final boolean allowedPeaks(double[] peaks,boolean borderPeaks){
        if(peaks == null || peaks.length != this.size){
            return false;
        }
        double[] sortedPeaks = Arrays.copyOf(peaks, peaks.length);
        Arrays.sort(sortedPeaks);
        if(!sortedPeaks.equals(peaks)){
            return false;
        }
        if(borderPeaks){
            if(sortedPeaks[0] != 0 || sortedPeaks[peaks.length -1] != 1){
                return false;
            }
        }else{
            if(sortedPeaks[0] < 0 || sortedPeaks[peaks.length -1] > 1){
                return false;
            }
        }

        return true;
    }
    
    /**
     * function to filter array with moving, sorting and normalize
     * 
     * after sorting the peaks only their relative differences are important!
     * 
     * @param peaks
     * @return sorted array with first element 0 and last element 1
     *         size of array is Math.max(peaks.length, 2)
     */
    public static double[] filterPeaks(double[] peaks) {
        if(peaks == null){
            return new double[]{0d,1d};
        }
        
        Arrays.sort(peaks);
        double min = peaks[0];
        if (min != 0) {
            // move everthing, so the start is 0
            for (int i = 0; i < peaks.length; ++i) {
                peaks[i] -= min;
            }
        }
        double max = peaks[peaks.length - 1];
        if (max == 0) {
            max = 1d;
            // add one to the end
            peaks = Arrays.copyOf(peaks, peaks.length + 1);
            peaks[peaks.length - 1] = 1;
        }
        if (max != 1) {
            // normalize
            for (int i = 0; i < peaks.length; ++i) {
                peaks[i] = peaks[i] / max;
            }
        }
        return peaks;
    }
    
    /** helper method to get equidstant peaks. The first one is by 0, the last one by 1
     * 
     * @return
     */
    public final double[] getUniformPeaksClipped(){
      //equidistant distribution of the peaks
        double step = 1d/ (this.size-1);
        double[] peaks = new double[this.size];
        for(int i = 0; i < this.size; ++i){
            peaks[i] = i*step;
        }
        return peaks;
    }
    
    /** helper method to get equidstant peaks. The first one is NOT by 0, the last one is NOT by 1
     * 
     * @return
     */
    public final double[] getUniformPeaksUnclipped(){
        //equidistant distribution of the peaks
          double step = 1d/ this.size;
          double halfStep = step/2d;
          double[] peaks = new double[this.size];
          for(int i = 0; i < this.size; ++i){
              peaks[i] = i*step + halfStep;
          }
          return peaks;
      }
    
    /** method which returns a distribution of unity with the specific value/progress
     * 
     * @param value double between 0 and 1. Defines the progress of the distribution
     * @return
     */
    public abstract double[] getDistribution(double value);
    
}
