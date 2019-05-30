package com.dcaiti.traceloader.odometrie;

public class InterpolationTools {

    public static double radShortestAngle(double start, double end){
        return ((((end - start) %(2*Math.PI)) + 3*Math.PI) % (2*Math.PI)) - Math.PI;
    }
    
    public static double radInterpolation(double start, double end, double amount){
        double shortest_angle = radShortestAngle(start,end);
        return start + shortest_angle * amount;
    }
    
    public static double degShortestAngle(double start, double end){
        return ((((end - start) % 360) + 540) % 360) - 180;
    }
    
    public static double degInterpolation(double start, double end, double amount){
        double shortest_angle = degShortestAngle(start,end);
        return start + shortest_angle * amount;
    }
    
    public static double scalarInterpolation(double start, double end, double amount){
        return start*(1-amount) + end*amount;
    }
    
    /** function to calculate the mean of circular quantities (in rad)
     * 
     * @param angles, the angles in rad (with mathematic orientation)
     * @return the mean angle and the variance of the angles
     */
    public static double[] meanOfRad(double[] angles){
        double x = 0;
        double y = 0;
        for(int i = 0; i < angles.length; ++i){
            x += Math.cos(angles[i]);
            y += Math.sin(angles[i]);
        }
        
        if(x != 0)
            x /= angles.length;
        if( y != 0)
            y /= angles.length;
        
        if(x == 0 && y == 0){
            return new double[]{Double.NaN, 1};
        }
        
        //variance is the length of the vector
        double length = Math.sqrt(x*x + y*y);
        double variance = 1 - length;
        double angle = Math.atan2(y, x);
        
        return new double[]{angle,variance};
    }
    
    public static double[] weightedMeanOfRad(double[] angles, double[] weight){
        double x = 0;
        double y = 0;
        for(int i = 0; i < angles.length; ++i){
            x += Math.cos(angles[i]) * weight[i];
            y += Math.sin(angles[i]) * weight[i];
        }
        
        if(x == 0 && y == 0){
            return new double[]{Double.NaN, 1};
        }
        
        //variance is the length of the vector
        double length = Math.sqrt(x*x + y*y);
        double variance = 1 - length;
        double angle = Math.atan2(y, x);
        
        return new double[]{angle,variance};
    }
    
    public static double weightedMean(double[] values, double[] weight){
        double value = 0;
        for(int i = 0; i < values.length; ++i){
            value += values[i] * weight[i];
        }
        return value;
    }
        
}
