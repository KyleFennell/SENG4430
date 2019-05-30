package com.dcaiti.traceloader.odometrie.onepartition;

/** defines a distribution of unity with linear components. Only the whereabouts of the peaks can be changed.
 * 
 * @author nkl - Nicolas Klenert
 *
 */
public class LinearOnePartition extends OnePartition {

    double[] maximas;
    
    public LinearOnePartition(int size){
        super(size);
        this.maximas = this.getUniformPeaksClipped();
    }
    
    public LinearOnePartition(double[] maximas){
        super(maximas == null ? 2 : maximas.length);
        this.maximas = OnePartition.filterPeaks(maximas);
    }
    
    @Override
    public double[] getDistribution(double value) {
//        if(this.maximas == null){
//            return new double[]{1-value,value};
//        }
        value = Math.max(value, 0);
        value = Math.min(value, 1);
        //find left and right maxima
        int i = 0;
        while(i < maximas.length && maximas[i] < value){
            ++i;
        }
        if(maximas[i] == value){
            double[] weight = new double[this.size];
            weight[i] = 1;
            return weight;
        }else{
            int left = i-1;
            int right = i;
            double w = (value - maximas[left])/(maximas[right] - maximas[left]);
            double[] weight = new double[this.size];
            weight[left] = 1-w;
            weight[right] = w;
            return weight;
        }
    }

}
