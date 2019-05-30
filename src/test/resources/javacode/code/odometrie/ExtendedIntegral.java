package com.dcaiti.traceloader.odometrie;

import java.util.ArrayList;
import java.util.List;

/** Class which gives some features related to integrals
 * 
 * @author nkl - Nicolas Klenert
 *
 */
public class ExtendedIntegral {

    private IntegralMethod method;
    
    public ExtendedIntegral(IntegralMethod method){
        this.method = method;
    }
    
    public void setIntegralMethod(IntegralMethod method){
        this.method = method;
    }
    
    public double crossCorrelation(double[] values, long[] times, double[] gpsValues, TimeSectionHelper helper){
        if(helper.getMaxId() != 1){
            System.out.println("__ExtendedIntegral__: CrossCorrelation (and other methods) are only defined for two values: the default values and the ignored values!");
            return Double.NaN;
        }
        List<double[]> val = new ArrayList<double[]>();
        val.add(this.getCalculatedValues(values, gpsValues));
        val.add(this.getIgnoredValues(values, gpsValues));
        return extendedIntegral(val,times,helper);
    }
    
    private double[] getIgnoredValues(double[] val, double[] gpsVal){
        double[] ignoreValues = new double[val.length];
        
        //IMPORTANT: The value of val*val is there, so it does not matter that much in an integral. Another Possibility is simply: 0
        //Other Alternatives: get some estimates, which point should be there (instead of the outlier) 
        //(if you smooth out the gpsValue enough, you do not need any ignored Points)
        if(method == IntegralMethod.CROSSCORRELATION){
            for(int i = 0; i < ignoreValues.length; ++i){
                ignoreValues[i] = val[i]*val[i];
            }
        }else if(method == IntegralMethod.MIN){
            for(int i = 0; i < ignoreValues.length; ++i){
                ignoreValues[i] = 0;
            }
        }else if(method == IntegralMethod.MAX){
            for(int i = 0; i < ignoreValues.length; ++i){
                ignoreValues[i] = 0;
            }
        }else if(method == IntegralMethod.MINMAX){
            for(int i = 0; i < ignoreValues.length; ++i){
                ignoreValues[i] = 0;
            }
        }
        
        return ignoreValues;
    }
    
    private double[] getCalculatedValues(double[] val, double[] gpsVal){
        double[] calculatedValue = new double[val.length];
        //IMPORTANT: The value val*gpsVal is there, because we use CrossCorrelation. 
        //We could use some other things, like the min of both functions
        if(method == IntegralMethod.CROSSCORRELATION){
            for(int i = 0; i < calculatedValue.length; ++i){
                calculatedValue[i] = val[i] * gpsVal[i];
            }
        }else if(method == IntegralMethod.MIN){
            for(int i = 0; i < calculatedValue.length; ++i){
                calculatedValue[i] = Math.min(val[i],gpsVal[i]);
            }
        }else if(method == IntegralMethod.MAX){
            for(int i = 0; i < calculatedValue.length; ++i){
                calculatedValue[i] = Math.max(val[i],gpsVal[i]);
            }
        }else if(method == IntegralMethod.MINMAX){
            for(int i = 0; i < calculatedValue.length; ++i){
                calculatedValue[i] = Math.max(val[i],gpsVal[i]) - Math.min(val[i],gpsVal[i]);
            }
        }
        return calculatedValue;
    }
    
    /** function to ignore some sections of the integral (set to 0, so nothing will be added)
     * 
     * uses extendedIntegral
     * 
     * @param values if null, we assume that it is an array with 1s
     * @param times
     * @param helper
     * @return
     */
    public static double integralIgnore(double[] values, long[] times, TimeSectionHelper helper){
        if(values == null){
            values = new double[times.length];
            for(int i = 0; i < values.length; ++i){
                values[i] = 1;
            }
        }
        double[] ign = new double[values.length];
        List<double[]> list = new ArrayList<double[]>(2);
        list.add(values);
        list.add(ign);
        return extendedIntegral(list,times,helper);
    }
    
    /** function which calculate the "mean" of some values.
     * 
     * @param values
     * @param times
     * @param helper
     * @return
     */
    public static double meanValue(double[] values, long[] times, TimeSectionHelper helper){
        double value = ExtendedIntegral.integralIgnore(values, times, helper);
        double timeSpan = ExtendedIntegral.integralIgnore(null, times, helper);
        return value / timeSpan;
    }
    
    /** very abstract class. Calculates the integral from different values. Which value is used is determined by the helper and the id
     * 
     * uses linear interplation
     * 
     * @param values
     * @param times
     * @param helper
     * @return
     */
    public static double extendedIntegral(List<double[]> values, long[] times, TimeSectionHelper helper){
        
        if(!helper.isStrict()){
            System.out.println("__ExtendedIntegral__: Be careful! CrossCorrelation only works correctly"
                    + " with a TimeSectionsHelper in strict mode (there are no selections allowed with a double border)");
            //e.g.: 0 to 50 and 50 to 100 as sections are forbidden!
        }
        
        if(helper.getMaxId() != values.size()-1){
            System.out.println("__ExtendedIntegral__: number of the given alternative values and number of ids in helper does not match!");
            return Double.NaN;
        }
        
        List<List<Double>> val = new ArrayList<List<Double>>(values.size());
        for(int i = 0; i < values.size(); ++i){
            val.add(new ArrayList<Double>(times.length + helper.size() * 2));
        }
        List<Long> tim = new ArrayList<Long>(times.length + helper.size() *2);
        
        //copy arrays
        for(int i = 0; i < times.length; ++i){
            tim.add(times[i]);
            for(int j = 0; j < values.size(); ++j){
                val.get(j).add(values.get(j)[i]);
            }
        }
        
        //get list of all time stamps from helper
        List<Long> timeStamps = helper.getTimeStamps();
        
        //add timestamps-value to the value fields
        long timeBegin = times[0];
        long timeEnd = times[times.length-1];
        int insertingIndex = 0;
        int inserts = 0;
        for(int i = 0; i < timeStamps.size(); ++i){
            long time = timeStamps.get(i);
            if(timeBegin < time && time < timeEnd){
                //get Neighbors
                int j = insertingIndex;
                while(j < times.length && times[j] < time){
                    ++j;
                }
                int right = j;
                int left = j-1;
                //calculate!
                long timediff = times[right] - times[left];
                double share = (double)(time - times[left]) / (double) timediff;
                if(tim.get(right+inserts) != time || tim.get(right+inserts-1) != time){
                    //only add a border, if there are less than two of them
                    tim.add(right+inserts, time);
                    double[] value = new double[values.size()];
                    for(int k = 0; k < values.size(); ++k){
                        value[k] = InterpolationTools.weightedMean(new double[]{values.get(k)[left],values.get(k)[right]}, new double[]{1-share,share});
                        val.get(k).add(right+inserts, value[k]);
                    }
                    ++inserts;
                    if(time != (times[right])){
                        //is the last one a border. If not, add the second border!
                        tim.add(right+inserts, time);
                        for(int k = 0; k < values.size(); ++k){
                            val.get(k).add(right+inserts,value[k]);
                        }
                        ++inserts;
                    }
                }
            }
        }
        
        //generate array of values
        
        double[] cross = new double[tim.size()];
  
        //the first and last one can be a border (without two equal timestamps)
        int index = 0;
        int id = helper.inSection(tim.get(index));
        if(id == 0){
            id = helper.beginOfSection(tim.get(index));
        }
        cross[index] = val.get(id).get(index);
        
        for(int i = 1; i < tim.size() -1; ++i){
            if(!tim.get(i).equals(tim.get(i+1))){
                id = helper.inSection(tim.get(i));
                cross[i] = val.get(id).get(i);
            }else{
                id = helper.endOfSection(tim.get(i));
                cross[i] = val.get(id).get(i);
                ++i;
                id = helper.beginOfSection(tim.get(i));
                cross[i] = val.get(id).get(i);
                
            }
        }
        
        //the first and last one can be a border (without two equal timestamps)
        index = tim.size()-1;
        id = helper.inSection(tim.get(index));
        if(id == 0){
            id =helper.endOfSection(tim.get(index));
        }
        cross[index] = val.get(id).get(index);
        

        //print it out
//        String string = "";
//        for(int  i = 0 ; i< cross.length; ++i){
//            string += " ["+tim.get(i)+"] "+cross[i]+" ;";
//        }
//        System.out.println(string);
        
        return integral(cross, tim);
    }
    
    private static double integralForPositive(double[] values, long[] times) {
        double sum = 0;
        for (int i = 0; i < values.length - 1; ++i) {
            if (Double.isNaN(values[i]) || Double.isNaN(values[i + 1])) {
                System.out.println("__ExtendedIntegral__: Something is going wrong. There should not be any NaN!");
            }
            sum += ((values[i] + values[i + 1])) * (double) (times[i + 1] - times[i]);
        }
        return sum;
    }
    
    public static double integral(double[] values, long[] times){
        //copy of the other integral function
      //add the min to the values to get only semi-positive values and use integralForPositive
        double min = Double.MAX_VALUE;
        double[] minArray = new double[values.length];
        double[] posValues = new double[values.length];
        double substract = 0;
        for(int i = 0; i < values.length; ++i){
            if(values[i] < min){
                min = values[i];
            }
        }
        if(min < 0){
            for(int i = 0; i < values.length; ++i){
                posValues[i] = values[i]-min;
                minArray[i] = -min;
            }
            substract = integralForPositive(minArray,times);
        }else{
            posValues = values;
        }
        
        double posInt = integralForPositive(posValues,times);
        return posInt - substract;
    }
    
    private static double integral(double[] values, List<Long> times) {
        //add the min to the values to get only semi-positive values and use integralForPositive
        double min = Double.MAX_VALUE;
        double[] minArray = new double[values.length];
        double[] posValues = new double[values.length];
        double substract = 0;
        for(int i = 0; i < values.length; ++i){
            if(values[i] < min){
                min = values[i];
            }
        }
        if(min < 0){
            for(int i = 0; i < values.length; ++i){
                posValues[i] = values[i]-min;
                minArray[i] = -min;
            }
            substract = integralForPositive(minArray,times);
        }else{
            posValues = values;
        }
        
        double posInt = integralForPositive(posValues,times);
        return posInt - substract;
        
    }
    
    private static double integralForPositive(double[] values, List<Long> times) {
        double sum = 0;
        for (int i = 0; i < values.length - 1; ++i) {
            if (Double.isNaN(values[i]) || Double.isNaN(values[i + 1])) {
                System.out.println("__ExtendedIntegral__: Something is going wrong. There should not be any NaN!");
            }
            sum += ((values[i] + values[i + 1])) * (double) (times.get(i+1) - times.get(i));
        }
        return sum;
    }
    
    @SuppressWarnings("unused")
    private static double integralForPositive(List<Double> values, List<Long> times) {
        double sum = 0;
        for (int i = 0; i < values.size() - 1; ++i) {
            if (Double.isNaN(values.get(i)) || Double.isNaN(values.get(i+1))) {
                System.out.println("__ExtendedIntegral__: Something is going wrong. There should not be any NaN!");
            }
            sum += ((values.get(i) + values.get(i+1))) * (double) (times.get(i+1) - times.get(i));
        }
        return sum;
    }
    
    /** Different methods to define a function. There are 4 implemented. CrossCorrelation, Max, Min and MinMax.
     * 
     * These Methods have one thing in common: They define a function, which is created by two other function. 
     * 
     * <p>
     * In Context with the TraceFilter it decide how the offset of the data is calculated. 
     * For more information about the math going on, please look at the "Praktikumsbericht" from the author of this class
     * 
     * @author nkl - Nicolas Klenert
     *
     */
    public enum IntegralMethod {
        CROSSCORRELATION, MAX, MIN, MINMAX;
    }
}
