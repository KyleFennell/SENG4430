package com.dcaiti.traceloader.odometrie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import com.dcaiti.traceloader.Trace;
import com.dcaiti.traceloader.TracePoint;
import com.vividsolutions.jts.geom.Coordinate;

/** Deprecated - use TraceFilter3 instead
 * 
 *  class to improve odometrie data on the traces. Improvements here are called "filter".
 * 
 *  All filter-methods start with "correct". Use method correct() to use all improvements in the intended order.
 *  All methods to improve a Trace with a specific error-value (offset,drift etc.) start with "traceWith"
 * 
 * @author nkl - Nicolas Klenert
 *
 */
@Deprecated
public class TraceFilter {
    
    EnumSet<Output> defaultFlag;
    SpeedData speed;
    Trace originalTrace;
    Trace trace;
    List<Trace> leftover;
    double offsetSpeed;
    double offsetYaw;
    double relSpeedError;
    double drift;
    
    /** Constructor is only needed if you do not want to run all filters, run filters with different setting, or if you want to visualize some filters
     *  Otherwise just use the static function correct(Trace) which does everything you need (or if you want 50Hz improvement correct50Hz(Trace1Hz,Trace50Hz))
     * 
     * @param trace on which the improvements should be calculated and used
     */
    public TraceFilter(Trace trace){
        this(trace,false);
    }
    
    
    public TraceFilter(Trace trace, boolean toSplit){
        this.defaultFlag = EnumSet.noneOf(Output.class);
        this.speed = SpeedData.ExtractGPSSPeed;
        this.originalTrace = trace.clone();
        if(toSplit){
            List<Trace> split = split(trace);
            if(split.size() == 1){
                this.trace = split.get(0);
                this.leftover = new ArrayList<Trace>();
            }else{
                Trace max = null;
                double maxlength = 0;
                for(int i = 0; i < split.size(); ++i){
                    double length = split.get(i).lengthFromSpeed();
                    if(length > maxlength){
                        maxlength = length;
                        max = split.get(i);
                    }
                }
                this.trace = max;
                split.remove(max);
                this.leftover = split;
            }
        }else{
            this.leftover = new ArrayList<Trace>();
            this.trace = trace;
        }
        
    }
    
    /** set flags to debug methods or to visualize something.
     * 
     * @param flag
     */
    public void setFlag(EnumSet<Output> flag){
        this.defaultFlag = flag;
    }
    
    /** method to use GPSSpeed instead of extracting it from Coordinates
     * works only if TracePoints do have the data
     * 
     * @param speed
     */
    public void setSpeedData(SpeedData speed){
        if(speed == SpeedData.GPSSpeed && this.trace.containsNullGPSSpeed){
            System.out.println("__TraceFilter__: GPSSpeed is not available");
        }else{
            this.speed = speed;
        }
    }
    
    /** compact method to use all improvements in the order it was intended
     * 
     * @return the improved trace
     */
    public TraceFilter correct(){
//        this.smoothTraceData();
        this.correctSensorLag();
        this.correctSpeedError();
        this.correctDrift();
//        this.smoothTraceData();
        return this;
    }
    
    /** static function helper. Most often this is the only function you will need.
     * 
     * @param trace
     * @return
     */
    public static Trace correct(Trace trace){
        TraceFilter filter = new TraceFilter(trace);
        filter.correct();
        return filter.getCorrectedTrace();
    }
    
    public static List<Trace> correctAll(Trace trace){
        TraceFilter filter = new TraceFilter(trace);
        return filter.correct().getAllCorrectedTraces();
    }
    
    public double[] getData(){
        return new double[]{this.offsetSpeed, this.offsetYaw, this.relSpeedError, this.drift};
    }
    
    /** used to correct trace with known errors
     * 
     * @param trace
     * @param data
     * @param Hz
     * @return
     */
    public static Trace correctWithKnownData(Trace trace, double[] data, boolean Hz){
        if(Hz){
            trace = traceWithOffset50Hz(trace, data[0], data[1]);
        }else{
            trace = traceWithOffset(trace, data[0], data[1]);
        }
        trace = traceWithoutSpeedError(trace, data[2]);
        trace = traceWithoutDrift(trace, data[3]);
        trace = smoothTraceData(trace, EnumSet.noneOf(Output.class));
        return trace;
    }
        
    /** static function helper to get a improved 50Hz trace.
     * 
     * @param trace1Hz
     * @param trace50Hz
     * @return
     */
    public static Trace correct50Hz(Trace trace1Hz, Trace trace50Hz){
        TraceFilter filter = new TraceFilter(trace1Hz);
        filter.correct();
        return filter.correct50Hz(trace50Hz);
    }
    
    public static List<Trace> correctAll50Hz(Trace trace1Hz, Trace trace50Hz){
        TraceFilter filter = new TraceFilter(trace1Hz);
        filter.correct();
        return split(filter.correct50Hz(trace50Hz));
    }
    
    /** function to improve a 50Hz trace with filter-data of the correction of a 1Hz trace.
     *  Only add improvements which were used by the 1Hz trace.
     *  
     *  If you want to add only some (and not all) filters, use some filters on the 1Hz trace (Object oriented)
     *  and then use this function to add the improvements to your 50Hz trace
     * 
     * @param trace
     * @return
     */
    public Trace correct50Hz(Trace trace){
        if(this.offsetSpeed != 0 || this.offsetYaw != 0){
            trace = traceWithOffset50Hz(trace,this.offsetSpeed, this.offsetYaw);
        }
        if(this.relSpeedError != 0){
            trace = traceWithoutSpeedError(trace,this.relSpeedError);
        }
        if(this.drift != 0){
            trace = traceWithoutDrift(trace, this.drift);
        }
//        trace = smoothTraceData(trace,this.defaultFlag);
        return trace;
    }
    
    /** get the leftover from the split trace corrected (use: correct(trace) for the main trace corrected and AFTERWARDS correctLeftOver)
     * If you want to have split and corrected 50Hz data, use correct50Hz and split it afterwards with the split function
     * 
     * @return
     */
    private List<Trace> correctLeftover(){
        List<Trace> list = new ArrayList<Trace>();
        for(int i = 0; i < this.leftover.size(); ++i){
            list.add(correctWithKnownData(this.leftover.get(i),this.getData(), false));
        }
        return list;
    }
    
    public List<Trace> getAllCorrectedTraces(){
        List<Trace> list = correctLeftover();
        list.add(0, getCorrectedTrace());
        return list;
    }
    
    public Trace getCorrectedTrace(){
        return this.trace;
    }
    
    public Trace getOriginalTrace(){
        return this.originalTrace;
    }
    
    public double[] getOffsetParams(){
        return new double[]{this.offsetSpeed, this.offsetYaw};
    }
    
    public double getRelSpeedError(){
        return this.relSpeedError;
    }
    
    public double getDrift(){
        return this.drift;
    }
    
    //function to smooth out all unexpected data -> where the yaw rate is much bigger than possible and such
    public Trace smoothTraceData(){
        this.trace = smoothTraceData(this.trace,this.defaultFlag);
        return this.trace;
    }
    
    public static Trace smoothTraceData(Trace trace, EnumSet<Output> flags){
        Trace tr = new Trace(trace.getVehicleId());
        tr.add(trace.getTracePointAtIndex(0));
        int counter = 0;
        
        double YawTreshold = 1;
        
        for(int i = 1; i < trace.size(); ++i){
            TracePoint left = trace.getTracePointAtIndex(i-1);
            TracePoint mid = trace.getTracePointAtIndex(i);
//            double heading = mid.getHeading() - left.getHeading();
            double heading = InterpolationTools.radShortestAngle(left.getHeading(), mid.getHeading());
            double timediff = mid.getTimeSI() - left.getTimeSI();
            double yaw = heading / timediff;
            if(Math.abs(yaw) > YawTreshold){
                //we want to replace this data -> TODO: for now we assume that this only happens at one time step
                tr.add(new TracePoint.Builder(mid).heading(getNextHeading(left,mid)).build());
//                System.out.println(i);
                ++counter;
            }else{
                tr.add(mid);
            }
            
        }

        System.out.println("__TraceFilter__: "+counter+" Places were smoothed out!");
        
        return tr;
    }
    
    public static double getNextHeading(TracePoint last, TracePoint now){
        double midYaw = (last.getYawRate() + now.getYawRate()) / 2.0;
        return last.getHeading() + midYaw;
    }
    
    /** function to split the trace, if there are any jumps
     * 
     * @param trace
     * @return
     */
    public static List<Trace> split(Trace trace){
        List<Trace> list = new ArrayList<Trace>();
        double timejump = 1.05;
        double minTime = 30;
        //TODO: split the trace here! -> by now only if time jumps...
        int lastCut = 0;
        for(int i = 1; i < trace.size(); ++i){
            TracePoint last = trace.getTracePointAtIndex(i-1);
            TracePoint now = trace.getTracePointAtIndex(i);
            if(Math.abs(now.getTimeSI() - last.getTimeSI())> timejump){
                //cut trace here
                list.add(trace.subTrace(lastCut, i));
                lastCut = i;
            }
        }
        list.add(trace.subTrace(lastCut, trace.size()));
        
        //remove all pieces which are too small
        List<Trace> split = new ArrayList<Trace>();
        for(int i = 0; i < list.size(); ++i){
            Trace tr = list.get(i);
            if(tr.getTracePointAtIndex(tr.size()-1).getTimeSI() - tr.getTracePointAtIndex(0).getTimeSI() > minTime){
                split.add(tr);
            }
        }
        
        System.out.println("__TraceFilter__: split Trace in "+split.size()+" pieces.");
        
        return split;
    }
    
    public Trace correctSensorLag(){
        return correctSensorLag(this.defaultFlag);
    }
        
    public Trace correctSensorLag(EnumSet<Output> flags){
        //copy of correctSpeedError
        Object[] obj;
        if(this.speed == SpeedData.GPSSpeed){
            obj = GPSSpeed(trace);
        }else{
            obj = extractGPSSpeed(trace);
        }
        double[] gpsSpeed = (double[]) obj[0];
        double[] midSpeed = (double[]) obj[1];
        long[] timesSpeed = (long[]) obj[2];
                
        //Funktion zum Minimieren ist diesmal nicht die Integraldifferenz sondern Maximiere Math.min
        //algo -> move to left until the max of "Integral of Math.min" is reached
        //TODO: interpoliere die Geschwindigkeiten, damit der Versatz hier besser wird -> double zahl als Versatz...
//        double[] min = new double[trace.size()-1];
        
        this.offsetSpeed = correctSensorLag(gpsSpeed, midSpeed, timesSpeed);
        
        obj = extractYawRate(trace);
        double[] headingYaw = (double[]) obj[0];
        double[] midYaw = (double[]) obj[1];
        long[] timesYaw = (long[]) obj[2];
        
        double[] headingYawPos = Arrays.copyOf(headingYaw, headingYaw.length);
        double[] midYawPos = Arrays.copyOf(midYaw, midYaw.length);
        
        //it is better to not mess with the scalar, because it will be easier to find not trustworthy points!
        double min_value = 0;
        for(int i = 0; i < headingYaw.length; ++i){
            if(headingYaw[i] < min_value){
                min_value = headingYaw[i];
            }
            if(midYaw[i] < min_value){
                min_value = midYaw[i];
            }
        }
        //add value to both sides
        double value = Math.abs(min_value);
        for(int i = 0; i < headingYaw.length; ++i){
            headingYawPos[i] += value;
            midYawPos[i] += value;
        }
        
        //getTrustedPoints
        //TODO: improve it! data is without better than with
        boolean[] ignore = reverse(trustworthySpikes(headingYaw, 1, 2, 0.001));
//        boolean[] ignore = new boolean[headingYaw.length];
        
        this.offsetYaw = correctSensorLag(headingYawPos,midYawPos,timesYaw,ignore);
        
        //maxPoint give you the steps you have to set!
        System.out.println("__TraceFilter__: Offest from GPS-Coor is: "+this.offsetSpeed);
        System.out.println("__TraceFilter__: Offest from GPS-Heading is: "+this.offsetYaw);
        
        
        //calculate the new trace with offset
        Trace tr = traceWithOffset(trace, this.offsetSpeed, this.offsetYaw);
        
        //show diff in GPS-Speed
        if(flags.contains(Output.SHOW_GPS) || flags.contains(Output.PRINT_GPS)){
            List<String> names = new ArrayList<String>();
            names.add("Speed from Sensor");
            names.add("GPS-Speed (calculated with Coor)");
            names.add("GPS-Speed with corrected Offset");
            
            List<double[]> data = new ArrayList<double[]>();
            data.add(midSpeed);
            data.add(gpsSpeed);
            data.add(interpolationLinear(gpsSpeed,-this.offsetSpeed));
            
            showDataLine(data,names,"Tracepoint","m/s","Speed of Trace "+trace.getBaseVehId()+" - Lag", flags);
        }
        
        //show diff in yaw-rate
        if(flags.contains(Output.SHOW_YAW) || flags.contains(Output.PRINT_YAW)){
          //create data for ignore
            double[] ign = new double[ignore.length];
            for(int i = 0; i < ign.length; ++i){
                if(ignore[i]){
                    ign[i] = 0.25;
                }else{
                    ign[i] = 0;
                }
            }
            
            List<String> names = new ArrayList<String>();
            names.add("Yaw-Rate from Sensor");
            names.add("Yaw-Rate from Heading");
            names.add("Yaw-Rate with corrected Offset");
            names.add("Ignored Points");

            List<double[]> data = new ArrayList<double[]>();
            data.add(midYaw);
            data.add(headingYaw);
            data.add(interpolationLinear(headingYaw, -this.offsetYaw));
            data.add(ign);
            
            showDataLine(data,names,"TracePoint","rad/s","Yaw-Rate of Trace "+trace.getBaseVehId(), flags);
        }
        
        this.trace = tr;
        return tr;
    }
    
    private static Object[] extractGPSSpeed(Trace trace){
        double[] gpsSpeed = new double[trace.size()-1];
        double[] midSpeed = new double[trace.size()-1];
        long[] times = new long[trace.size()-1];
        
        for(int i = 0; i < trace.size() -1; ++i){
            TracePoint one = trace.getTracePointAtIndex(i);
            TracePoint two = trace.getTracePointAtIndex(i+1);
            double timediff = (two.getTime() - one.getTime()) / 1000.0;
            gpsSpeed[i] = one.getCoor().distance(two.getCoor()) / timediff;
            midSpeed[i] = (one.getSpeedSI()+two.getSpeedSI()) /2.0;
            times[i] = one.getTime() + (two.getTime() - one.getTime()) / 2;
        }
        
        return new Object[]{gpsSpeed,midSpeed,times};
    }
    
    private static Object[] GPSSpeed(Trace trace){
        double[] gpsSpeed = new double[trace.size()];
        double[] midSpeed = new double[trace.size()];
        long[] times = new long[trace.size()];
        
        for(int i = 0; i < trace.size(); ++i){
            gpsSpeed[i] = trace.getTracePointAtIndex(i).getGPSSpeedSI();
            midSpeed[i] = trace.getTracePointAtIndex(i).getSpeedSI();
            times[i] = trace.getTracePointAtIndex(i).getTime();
        }
        
        return new Object[]{gpsSpeed,midSpeed,times};
    }
    
    private static boolean[] reverse(boolean[] trust){
        boolean[] ignore = new boolean[trust.length];
        for(int i = 0; i < ignore.length; ++i){
            ignore[i] = !trust[i];
        }
        return ignore;
    }
    
    private static double correctSensorLag(double[] gpsSpeed, double[] midSpeed, long[] times, boolean[] ignore){
        
        double minBorder = 0;
        double maxBorder = 3;
        double step = 0.05;
        int maxIteration = (int) Math.ceil((maxBorder-minBorder) /step);
        double[] values = new double[maxIteration];
        double point = minBorder;
        double maxValue = 0;
        double maxPoint = minBorder;
        boolean toLeft = true;
        
        /**
         * This piece of Code is really important!
         * If we do not have something to ignore on the integral (error in gpsSpeed), we can move the gpsSpeed to calculate the integral
         * But if we have to ignore some error in gpsSpeed, we have to move the other function, because we can not move the ignored place.
         * Moving the error-prone data gives better results!
         */
        for(int i = 0; i < ignore.length; ++i){
            if(ignore[i]){
                toLeft = false;
            }
        }
                
        for(int i = 0; i < values.length; ++i){
            point = minBorder + i * step;
            if(toLeft){
                values[i] = integral(cut(defineMin(gpsSpeed,midSpeed,-point),-maxBorder),times,ignore);
            }else{
                values[i] = integral(cut(defineMin(midSpeed,gpsSpeed,point),maxBorder),times,ignore);
            }
            if(values[i] > maxValue){
                maxValue = values[i];
                maxPoint = point;
            }
        }
        
        return maxPoint;
    }
    
    private static double correctSensorLag(double[] gpsSpeed, double[] midSpeed, long[] times){
        return correctSensorLag(gpsSpeed,midSpeed,times, new boolean[gpsSpeed.length]);
    }
    
    public static Trace traceWithOffset50Hz(Trace trace, double offsetCoor, double offsetHeading){
        return traceWithOffset(trace,offsetCoor*50.0,offsetHeading*50.0);
    }
    
    public static Trace traceWithOffset(Trace trace, double offsetCoor, double offsetHeading){
        Trace tr = new Trace(trace.getVehicleId());
        double[] xCoor = new double[trace.size()];
        double[] yCoor = new double[trace.size()];
        double[] heading = new double[trace.size()];
        
        for(int i = 0; i < trace.size(); ++i){
            xCoor[i] = trace.getTracePointAtIndex(i).getCoor().x;
            yCoor[i] = trace.getTracePointAtIndex(i).getCoor().y;
            heading[i] = trace.getTracePointAtIndex(i).getHeading();
        }
        
        double[] linXCoor = interpolationLinear(xCoor,-offsetCoor);
        double[] linYCoor = interpolationLinear(yCoor,-offsetCoor);
        double[] linHead = interpolationLinear(heading,-offsetHeading,true);
        
        for(int i = 0; i < linXCoor.length; ++i){
            //interpolate linear
            Coordinate coor = new Coordinate(linXCoor[i],linYCoor[i]);
            tr.add(new TracePoint.Builder(trace.getTracePointAtIndex(i))
                    .coordinate(coor)
                    .heading(linHead[i])
                    .build()
                    );
        }
        
        double offset = Math.max(Math.abs(offsetCoor), Math.abs(offsetHeading));
        int cut = (int)Math.ceil(offset);
        tr = tr.subTrace(cut, tr.size() -cut);
        return tr;
    }
    
    /** function to set the min-function of two functions (represented as array), with the first param shifted to the left
     * 
     * @param gps
     * @param speed
     * @param stepToLeft
     * @param cut  cut is there to return only arrays with at least #cut tiles at the end removed -> therefore integral function will be fair
     * @return
     */
    private static double[] defineMin(double[] gps, double[] speed, double offset){        
        //the interpolation is linear -> use splines etc to have better results
        double[] interpolated = interpolationLinear(gps, offset);
//        double[] min = new double[(int) Math.min(interpolated.length, speed.length - cut)];
        double[] min = new double[interpolated.length];
        for(int i = 0; i < min.length; ++i){
            min[i] = Math.min(speed[i], interpolated[i]);
        }
        return min;
    }
    
    private static double[] cut(double[] values, double maxOffset){
        int cut = (int)Math.ceil(Math.abs(maxOffset));
        double[] cutted = new double[values.length - cut];
        int start = cut;
        int end = values.length - cut;
//        if(maxOffset > 0){
//            //cut at the beginning
//            start += cut;
//        }else{
//            //cut at the end
//            end -= cut;
//        }
        for(int i = start; i < end; ++i){
            cutted[i-start] = values[i];
        }
        return cutted;
    }
    
    private static double[] interpolationLinear(double[] values, double offset, boolean rad){
        if(offset < 0){
            return interpolationLinearToLeft(values,-offset,rad);
        }else if(offset > 0){
            return interpolationLinearToRight(values,offset,rad);
        }else{
            return values;
        }
    }
    
    private static double[] interpolationLinearToRight(double[] values, double offset, boolean rad){
        int start = (int)Math.ceil(offset);
        double[] interpolated = new double[values.length];
        int fullStep = (int)Math.floor(offset);
        for(int i = 0; i < start; ++i){
            interpolated[i] = Double.NaN;
        }
        double rest = offset - fullStep;
        double interpolation;
        for(int i = start; i < interpolated.length; ++i){
            if(rest == 0){
                interpolation = values[i-fullStep];
            }else{
                double left = values[i-fullStep-1];
                double right = values[i-fullStep];
                //the interpolation is linear -> use splines etc to have better results
                if(rad){
                    interpolation = InterpolationTools.radInterpolation(left,right,rest);
                }else{
                    interpolation = InterpolationTools.scalarInterpolation(left,right,rest);
                }
            }
            interpolated[i] = interpolation;
        }
        return interpolated;
    }
    
    private static double[] interpolationLinearToLeft(double[] values, double offset, boolean rad){
        int length = values.length - (int)Math.ceil(offset);
        double[] interpolated = new double[values.length];
        int fullStep = (int)Math.floor(offset);
        for(int i = length; i < interpolated.length; ++i){
            interpolated[i] = Double.NaN;
        }
        for(int i = 0; i < length; ++i){
            double rest = offset - fullStep;
            double interpolation;
            if(rest == 0){
                interpolation = values[i+fullStep];
            }else{
                double left = values[i+fullStep];
                double right = values[i+fullStep+1];
                //the interpolation is linear -> use splines etc to have better results
                if(rad){
                    interpolation = InterpolationTools.radInterpolation(left,right,rest);
                }else{
                    interpolation = InterpolationTools.scalarInterpolation(left,right,rest);
                }
            }
            interpolated[i] = interpolation;
        }
        return interpolated;
    }
    
    private static double[] interpolationLinear(double[] values, double offset){
        return interpolationLinear(values,offset,false);
    }
    
    public Trace correctSpeedError(){
        return correctSpeedError(this.defaultFlag);
    }
    
    public Trace correctSpeedError(EnumSet<Output> flags){               
        Object[] obj;
        if(this.speed == SpeedData.GPSSpeed){
            obj = GPSSpeed(trace);
        }else{
            obj = extractGPSSpeed(trace);
        }
        double[] gpsSpeed = (double[]) obj[0];
        double[] midSpeed = (double[]) obj[1];
        long[] times = (long[]) obj[2];
        boolean[] trust = trustworthyDet(trace, 0.1, 1);
        if(this.speed == SpeedData.ExtractGPSSPeed){
            trust = sizeDown(trust);
        }
        
       boolean[] ignore = reverse(trust);
                
//        System.out.println("Integral von Speed ist: "+integral(midSpeed,times));
//        System.out.println("Integral von GPS ist: "+integral(gpsSpeed,times));
        
        //algo
        double threshold = 0.1;
        int max = 100;
        int counter = 0;
        double error = 1;
        double step = 0.1;
        double diff = integral(midSpeed,times,ignore)-integral(gpsSpeed,times,ignore);
        double[] speed = midSpeed.clone();
        
        while(Math.abs(diff) > threshold && counter < max){
            if(diff > 0){
                error -= step;
            }else{
                error += step;
            }
            speed = addRelativeError(midSpeed,error);
            diff = integral(speed,times,ignore)-integral(gpsSpeed,times,ignore);
            ++counter;
            step /= 2.0;
        }
        
//        System.out.println("Algo lief "+counter+" mal durch");
        System.out.println("__TraceFilter__: The relativistic Speed Error is: "+error);
        this.relSpeedError = error;
        
        if(flags.contains(Output.SHOW_SPEED) || flags.contains(Output.PRINT_SPEED)){
            //create data for ignore
            double[] ign = new double[ignore.length];
            for(int i = 0; i < ign.length; ++i){
                if(ignore[i]){
                    ign[i] = 5;
                }else{
                    ign[i] = 0;
                }
            }
            
            List<String> names = new ArrayList<String>();
            names.add("Speed from Sensor");
            names.add("GPS-Speed");
//            names.add("Speed corrected");
//            names.add("Ignored Points");
            
            List<double[]> data = new ArrayList<double[]>();
            data.add(midSpeed);
            data.add(gpsSpeed);
//            data.add(speed);
//            data.add(ign);
            
            showDataLine(data,names,"TracePoint","m/s","Speed of Trace "+trace.getBaseVehId()+" - rel. Error", flags);
        }
        
        this.trace = traceWithoutSpeedError(trace,error);
        return this.trace;
    }
    
    public static Trace traceWithoutSpeedError(Trace trace, double error){
        Trace corr = new Trace(trace.getVehicleId());
        for(int i = 0; i < trace.size(); ++i){
            double speed = trace.getTracePointAtIndex(i).getSpeedSI() * error;
            corr.add(new TracePoint.Builder(trace.getTracePointAtIndex(i)).speedSI(speed).build());
        }
        return corr;
    }
    
    public Trace correctDrift(){
        return this.correctDrift(this.defaultFlag);
    }
    
    public Trace correctDrift(EnumSet<Output> flags){
        Object[] obj = extractYawRate(trace);
        double[] headingYaw = (double[]) obj[0];
        double[] midYaw = (double[]) obj[1];
        long[] timesYaw = (long[]) obj[2];
        
//        boolean[] trust = trustworthyDet(trace, 0.1, 1);
//        boolean[] ignore = reverse(trust);
        boolean[] ignore = new boolean[midYaw.length];
        double diff = integral(headingYaw,timesYaw,ignore)-integral(midYaw,timesYaw,ignore);
        double[] ones = new double[midYaw.length];
        for(int i = 0; i < ones.length; ++i){
            ones[i] = 1;
        }
        double timediff = integral(ones,timesYaw,ignore);
//        double timediff = timesYaw[timesYaw.length -1]-timesYaw[0]; 
        //-> this is wrong! because not all time steps are counted by the integral! (and the integral is 2000 times bigger than normal)
        
        this.drift = diff/timediff;
        
        System.out.println("__TraceFilter__: The drift error is: "+this.drift);
        
        if(flags.contains(Output.SHOW_DRIFT) || flags.contains(Output.PRINT_DRIFT)){
            //create data for ignore
            double[] ign = new double[ignore.length];
            for(int i = 0; i < ign.length; ++i){
                if(ignore[i]){
                    ign[i] = 0.1;
                }else{
                    ign[i] = 0;
                }
            }
            
            //create data for corrected
            double[] corrected = new double[midYaw.length];
            for(int i = 0; i < corrected.length; ++i){
                corrected[i] = midYaw[i] +this.drift;
            }
            
            List<String> names = new ArrayList<String>();
            names.add("Yaw-Rate from Sensor");
            names.add("Yaw-Rate from GPS-heading");
            names.add("Yaw-Rate corrected / Drift corrected");
//            names.add("Ignored Points");
            
            List<double[]> data = new ArrayList<double[]>();
            data.add(midYaw);
            data.add(headingYaw);
            data.add(corrected);
//            data.add(ign);
            
            showDataLine(data,names,"TracePoint","rad/s","Yaw-Rate of Trace "+trace.getBaseVehId()+" - Drift", flags);
            
//            List<double[]> error = new ArrayList<double[]>();
//            List<String> errorName = new ArrayList<String>();
//            errorName.add("Yaw-rate difference");
//            double[] err = new double[midYaw.length];
//            error.add(err);
//            for(int i = 0; i < err.length; ++i){
//                err[i] = midYaw[i] - headingYaw[i];
//            }
//            
//            showDataLine(error,errorName,"TracePoint","rad/s","Yaw-Rate Difference of Trace"+trace.getBaseVehId()+" - Drift", flags);
        }
        
        this.trace = traceWithoutDrift(this.trace,this.drift);
        return this.trace;
    }
    
    public static Trace traceWithoutDrift(Trace trace, double drift){
        Trace corr = new Trace(trace.getVehicleId());
        for(int i = 0; i < trace.size(); ++i){
            double yaw = trace.getTracePointAtIndex(i).getYawRate() + drift;
            corr.add(new TracePoint.Builder(trace.getTracePointAtIndex(i)).yawRate(yaw).build());
        }
        return corr;
    }
    
    private static double[] addRelativeError(double[] values, double error){
        double[] val = new double[values.length];
        for(int i = 0; i < val.length; ++i){
            val[i] = values[i]*error;
        }
        return val;
    }
        
    @SuppressWarnings("unused")
    private static double integral(double[] values, long[] times){
        return integral(values,times,new boolean[values.length]);
    }
    
    //function returns an value as integral which is 2000 times bigger as it should be
    private static double integral(double[] values, long[] times, boolean[] ignore){
        double sum = 0;
        for(int i = 0; i < values.length -1; ++i){
            if(!(ignore[i] || ignore[i+1])){
                if(Double.isNaN(values[i]) || Double.isNaN(values[i+1])){
                    System.out.println("__TraceFilter__: Something is going wrong. There should not be any NaN!");
                }
                sum += ((values[i] + values[i+1])) * (double)(times[i+1] - times[i]);   // there should be / 2.0 and times should be used in seconds
            }
        }
        return sum;
    }
    
    @Deprecated
    public Trace correctTrustworthy(){       
        for(int i = 1; i < trace.size(); ++i){
            TracePoint tp = trace.getTracePointAtIndex(i);
            TracePoint last_tp = trace.getTracePointAtIndex(i-1);
            //not trustworthy if speed = 0
            if(tp.getSpeed() == 0){
                tp.noTrust();
            }
            //not trustworthy if gps jumps  -> jumps should be filtered out after this filters or use the filters here with a list of traces
            Coordinate det_coor = last_tp.getSensorVehicleModel().nextPos(tp.getTime());
            double dist = last_tp.getCoor().distance(det_coor);
            double error = tp.getCoor().distance(det_coor);
            double standardError = 1;
            double threshold = 0.1;
            if(error > 1 && (error-standardError)/dist > threshold){
                tp.noTrust();
            }
        }
        return trace;
    }
    
    private static boolean[] trustworthyDet(Trace trace,double threshold, double standardError){
        boolean[] trust = new boolean[trace.size()];
        for(int i = 0; i < trust.length; ++i){
            trust[i] = true;
        }
        
        for(int i = 1; i < trace.size(); ++i){
            TracePoint tp = trace.getTracePointAtIndex(i);
            TracePoint last_tp = trace.getTracePointAtIndex(i-1);
            //not trustworthy if speed = 0
            if(tp.getSpeed() == 0){
                trust[i] = false;
            }
            //not trustworthy if gps jumps  -> jumps should be filtered out after this filters or use the filters here with a list of traces
            Coordinate det_coor = last_tp.getSensorVehicleModel().nextPos(tp.getTime());
            double dist = last_tp.getCoor().distance(det_coor);
            double error = tp.getCoor().distance(det_coor);
            if(error > 1 && (error-standardError)/dist > threshold){
                trust[i] = false;
            }
        }
        return trust;
    }
    
    private static boolean[] sizeDown(boolean[] arr){
        boolean[] ret = new boolean[arr.length -1];
        for(int i = 0; i < ret.length; ++i){
            ret[i] = (arr[i] && arr[i+1]);
        }
        return ret;
    }
    
    private static boolean[] trustworthySpikes(double[] values, double max, double tolerance, double min){
        boolean[] trust = new boolean[values.length];
        for(int i = 0; i < trust.length; ++i){
            trust[i] = true;
        }
        //prepare list for calculating median and throw all values out
        List<Double> listForMed= new ArrayList<Double>();
        for(int i = 0; i < trust.length; ++i){
            double value = Math.abs(values[i]);
            if(value > max){
                trust[i] = false;
            }else{
                if(value > min){
                    listForMed.add(value);
                }
            }
        }
        //get median
        Collections.sort(listForMed);
        double med = 0;
        if(listForMed.size() % 2 == 0){
            int indexLast = listForMed.size() / 2;
            int indexFirst = indexLast-1;
            med = (listForMed.get(indexFirst) + listForMed.get(indexLast)) / 2.0;
        }else{
            int index = (listForMed.size() -1 )/2;
            med = listForMed.get(index);
        }
        
        //throw out all values which are above median
        for(int i = 0; i < trust.length; ++i){
            if(Math.abs(values[i]) > med * tolerance){
                trust[i] = false;
            }
        }
        
        return trust;
    }
    
    private static Object[] extractYawRate(Trace trace){
        double[] headingYaw = new double[trace.size()-1];
        double[] midYaw = new double[trace.size()-1];
        long[] times = new long[trace.size()-1];
        
        for(int i = 0; i < trace.size() -1; ++i){
           TracePoint one = trace.getTracePointAtIndex(i);
           TracePoint two = trace.getTracePointAtIndex(i+1);
           double timediff = (two.getTime() - one.getTime()) / 1000.0;
           headingYaw[i] = InterpolationTools.radShortestAngle(one.getHeading(),two.getHeading()) / timediff;
           midYaw[i] = InterpolationTools.radInterpolation(one.getYawRate(),two.getYawRate(),0.5);
           times[i] = one.getTime() + (two.getTime() - one.getTime()) / 2;
        }
        
        return new Object[]{headingYaw,midYaw,times};
    }
    
    public static void showDataLine(List<double[]> data, List<String> names, String x_axis, String y_axis, String title, EnumSet<TraceFilter.Output> flags){
        LineChart chart = new LineChart(title);
        chart.addAllLinearData(data, names);
        chart.initChart(x_axis,y_axis);
        for(int i = 0; i < data.size(); ++i){
            chart.setShape(true, i);
        }
        
        EnumSet<Output> checkPrint = Output.PRINT.clone();
        EnumSet<Output> checkShow = Output.SHOW.clone();
        checkPrint.retainAll(flags);
        checkShow.retainAll(flags);
        
        if(!checkPrint.isEmpty()){
            chart.printChart();
        }
        if(!checkShow.isEmpty()){
            chart.showChart();
        }
    }
    
    public enum Output {
        SHOW_SPEED, PRINT_SPEED, SHOW_YAW, PRINT_YAW, SHOW_GPS, PRINT_GPS, SHOW_DRIFT, PRINT_DRIFT;
        public static final EnumSet<Output> SHOW = EnumSet.of(SHOW_SPEED, SHOW_YAW, SHOW_GPS, SHOW_DRIFT);
        public static final EnumSet<Output> PRINT = EnumSet.of(PRINT_SPEED, PRINT_YAW, PRINT_GPS, PRINT_DRIFT);
    }
    
    public enum SpeedData{
        GPSSpeed, ExtractGPSSPeed;
    }
    
}
