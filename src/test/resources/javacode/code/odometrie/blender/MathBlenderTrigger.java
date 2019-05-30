package com.dcaiti.traceloader.odometrie.blender;

import org.apache.commons.math3.linear.BlockRealMatrix;

public class MathBlenderTrigger {

    int index;
    double[] initialWeight;
    int stopChangeWeight;
    
    public MathBlenderTrigger(int index, double[] weight, int stopChangeWeight){
        this.index = index;
        this.initialWeight = weight;
        this.stopChangeWeight = stopChangeWeight;
    }
    
    public double[] getInitialWeight(){
        return this.initialWeight;
    }
    
    public int size(){
        if(this.initialWeight == null){
            return 0;
        }
        return this.initialWeight.length;
    }
    
    public int getStartPoint(){
        return this.index;
    }
    
    public double[] changeWeight(double[] weight){
        if(this.stopChangeWeight > 0){
            --this.stopChangeWeight;
            return weight;
        }else{
            double[][] w = new double[weight.length][weight.length];
            for(int i = 0; i < weight.length; ++i){
                    w[0][i] = 0.2;
                    w[i][i] += 0.8;
            }
            BlockRealMatrix changeOnWeight = new BlockRealMatrix(w);
            return changeOnWeight.operate(weight);
        }
    }
    
}
