package com.dcaiti.traceloader.odometrie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import com.dcaiti.traceloader.Trace;
import com.dcaiti.traceloader.TracePoint;

import com.dcaiti.traceloader.odometrie.ExtendedIntegral.IntegralMethod;
import com.dcaiti.traceloader.odometrie.ExtendedIntegral;

/** Class to improve Odometrie data. There are many settings that can be changed. <p>
 * 
 * <ul>
 * <li> visualize something (and decide if the vehId should be printed with it) (see {@link com.dcaiti.traceloader.odometrie.TraceFilter3.Output TraceFilter3.Output})</li>
 * <li> split trace and use only the part without time jumps (see {@link com.dcaiti.traceloader.odometrie.TraceFilter3#TraceFilter(Trace, boolean) TraceFilter-Constructor}) </li>
 * <li> which time stamps should be used to calculate the data (see {@link com.dcaiti.traceloader.odometrie.TraceFilter3.TimeStamp TraceFilter.TimeStamp}) </li>
 * <li> method to calculate offset (see {@link com.dcaiti.traceloader.odometrie.ExtendedIntegral.IntegralMethod ExtendedIntegral.IntegralMethod}) </li>
 * <li> possible min and max Offset (only within the code, see {@link com.dcaiti.traceloader.odometrie.TraceFiler3.LagFilter TraceFilter3.LagFilter}) </li>
 * <li> How small the time steps should be to calculate the offset (only within the code, see {@link com.dcaiti.traceloader.odometrie.TraceFilter3.LagFilter TraceFilter3.LagFilter}) </li>
 * <li> if we assume, that the GPS-Unit or the Sensor-Data has the lag. This does matter a lot:  
 * <ul>
 *      <li> if the GPS-Unit has the lag, we only move the gps-data with one calculated offset (see {@link com.dcaiti.traceloader.odometrie.TraceFilter3#shiftPath() shiftPath})</li>
 *      <li> if the other sensor data has the lag, we move the sensor data independently with different offsets (see {@link com.dcaiti.traceloader.odometrie.TraceFilter3#shiftData() shiftData})</li>
 * </ul>
 * </li>
 * <li> if we want to normalize the data by the offset calculation (in the hope, that with this, we can compensate the border-error). NOT IMPLEMENTED YET! </li>
 * </ul>
 * 
 * <p>
 * 
 * Big improvement above TraceFilter (Version 1)
 * <ul>
 * <li> stable against time jumps </li>
 * <li> uses the PathAugmenter "Interface" </li>
 * <li> uses CrossCorrelation or Minimum Function (easy to implement more) </li>
 * <li> Performance boost (because it is using PathAugmenter with useMaps() and because we only update the Trace once) </li>
 * </ul>
 * 
 * <p>
 * 
 * Improvements here are called "filter".
 * 
 * All filter-methods start with "correct". Use method correct() to use all
 * improvements in the intended order.
 * 
 * @author nkl - Nicolas Klenert
 *
 */
public class TraceFilter3 {

    private EnumSet<Output> flags;
    public LagFilter lagFilter;
    public ErrorFilter errorFilter;
    //the trace we will improve
    private Trace trace;
    //the time span we use to look at the data of the trace
    private long minTraceTime;
    private long maxTraceTime;
    //variable to determine if we should use the midPoints of the trace or the real points
    private TimeStamp time;
    //variable to determine if given trace should be splitted, if it has time jumps
    private boolean toSplit;
    //variable to determine if the vehId should be shown by showData
    private boolean vehId = false;
    

    /** default constructor.
     * 
     * @param trace which should be optimized
     */
    public TraceFilter3(Trace trace) {
        this(trace, false);
    }

    /** Constructor with the option the use only data without time jumps. The returned corrected Trace is nevertheless the whole Trace and not the splitted one!
     * 
     * @param trace which should be improved
     * @param toSplit if true, the trace is split and only the longest part of the trace without time jumps is used
     */
    public TraceFilter3(Trace trace, boolean toSplit) {
        this.toSplit = toSplit;
        this.initTrace(trace);
        this.initSettings();
    }
    
    /** function to initialize the trace. Important for the split-option
     * 
     * we do not really calculate something with the split trace. We just safe the min and max time of the section and calculate the needed values only there
     * 
     * @param trace
     */
    private void initTrace(Trace trace){
        this.trace = trace.clone();
        if (this.toSplit) {
            List<Trace> split = split(trace);
            if (split.size() < 2) {
                this.minTraceTime = this.trace.getTracePointAtIndex(0).getTime();
                this.maxTraceTime = this.trace.getTracePointAtIndex(this.trace.size()-1).getTime();
            } else {
                Trace max = null;
                double maxlength = 0;
                for (int i = 0; i < split.size(); ++i) {
                    double length = split.get(i).lengthFromSpeed();
                    if (length > maxlength) {
                        maxlength = length;
                        max = split.get(i);
                    }
                }
                this.minTraceTime = max.getTracePointAtIndex(0).getTime();
                this.maxTraceTime = max.getTracePointAtIndex(max.size()-1).getTime();
            }
        } else {
            this.minTraceTime = this.trace.getTracePointAtIndex(0).getTime();
            this.maxTraceTime = this.trace.getTracePointAtIndex(this.trace.size()-1).getTime();
        }
    }
    
    /** method to set the default settings
     * 
     */
    private void initSettings(){
        this.flags = EnumSet.noneOf(Output.class);
        this.lagFilter = new LagFilter(IntegralMethod.CROSSCORRELATION);
        this.lagFilter.shiftPath = false;
        this.lagFilter.normalize = false;
        this.time = TimeStamp.REALDATA_NORMAL;
        this.errorFilter = new ErrorFilter();
    }
    
    /** method to change the setting, which time stamps should be used to calculate the values.
     * 
     * @see com.dcaiti.traceloader.odometrie.TraceFilter3.TimeStamp TraceFilter.TimeStamp
     * 
     * @param stamp
     * @return
     */
    public TraceFilter3 setTimesToUse(TimeStamp stamp){
        this.time = stamp;
        return this;
    }
    
    /** method to change the method we use to calculate the offset
     * 
     * @see com.dcaiti.traceloader.odometrie.ExtendedIntegral.IntegralMethod ExtendedIntegral.IntegralMethod
     * 
     * @param method
     * @return
     */
    public TraceFilter3 setIntegralMethod(IntegralMethod method){
        this.lagFilter.setMethod(method);
        return this;
    }
    
    /** method to activate the normalization of the values by the calculation of the offset. At this Point it does not work!
     * 
     * @param bool
     * @return
     */
    public TraceFilter3 normalize(boolean bool){
        this.lagFilter.normalize = bool;
        return this;
    }
    
    /** Option which represent the assumption, that only the GPS-Unit has a lag.
     *  The consequence is, that we shift all GPS-Data after we calculate <b>one</b> Offset
     * 
     * @see com.dcaiti.traceloader.odometrie.TraceFilter3#shiftData() shiftData - the other possibility
     * 
     * @return
     */
    public TraceFilter3 shiftPath(){
        this.lagFilter.shiftPath = true;
        return this;
    }

    /** Option which represent the assumption, that all sensor data could have a lag. 
     * The consequence is, that we shift every data we know their offset. (speed and yaw-rate).
     * 
     * @see com.dcaiti.traceloader.odometrie.TraceFilter3#shiftPath() shiftPath - the other possibility
     * 
     * @return
     */
    public TraceFilter3 shiftData(){
        this.lagFilter.shiftPath = false;
        return this;
    }
    /**
     * set flags to visualize something (e.g. for debugging).
     * 
     * @see com.dcaiti.traceloader.odometrie.TraceFilter3.Output
     * 
     * @param flag
     */
    public TraceFilter3 setFlag(EnumSet<Output> flag) {
        this.flags = flag;
        return this;
    }
    
    /** function to set a new trace to be improved. Settings will not be deleted.
     * 
     * @param trace
     */
    public TraceFilter3 setTrace(Trace trace){
        this.lagFilter.clear();
        this.errorFilter.clear();
        this.initTrace(trace);
        return this;
    }
    
    /** function to use all possible correct-methods of the filter - in their preferred sequence
     * 
     * @return self
     */
    public TraceFilter3 correct(){
        this.correctSensorLag();
        this.correctSensorError();
        return this;
    }
    
    /**
     * static function helper. Most often this is the only function you will
     * need.
     * 
     * @param trace
     * @return
     */
    public static Trace correct(Trace trace) {
        TraceFilter3 filter = new TraceFilter3(trace,true);
        return filter.correct().getCorrectedTrace();
    }

    /**
     * used to correct trace with known errors
     * 
     * @param trace
     * @param data
     * @param Hz
     * @return
     */
    public TraceFilter3 correctWithKnownData(long speedLag, double speedError, long yawLag, double driftError) {
        this.lagFilter.offsetSpeed = speedLag;
        this.errorFilter.speedError = speedError;
        this.lagFilter.offsetYaw = yawLag;
        this.errorFilter.driftError = driftError;
        this.lagFilter.correctTrace();
        this.errorFilter.correctTrace();
        return this;
    }

    /** method to get the corrected Trace
     * 
     * @return
     */
    public Trace getCorrectedTrace() {
        return this.trace;
    }
    
    public double[] getOffsetParams() {
        return new double[] { lagFilter.offsetSpeed, lagFilter.offsetYaw };
    }

    public double getRelSpeedError() {
        return this.errorFilter.speedError;
    }

    public double getDrift() {
        return this.errorFilter.driftError;
    }

    /**
     * function to split the trace, if there are any jumps in the gps coordinates!
     * 
     * @param trace
     * @return
     */
    public static List<Trace> split(Trace trace) {
        List<Trace> list = new ArrayList<Trace>();
        long[] times = trace.getPathAugmenter().getCoorTimes();
        System.out.println(Arrays.toString(times));
        long timejump = 1050; //as ms
        double minTime = 30;    //as seconds
        int lastCut = 0;
        
        for(int i = 1; i < times.length; ++i){
            if (Math.abs(times[i]-times[i-1]) > timejump) {
                // cut trace here
                int nowIndex = trace.getIndexAtTime(times[i]);
                list.add(trace.subTrace(lastCut, nowIndex));
                lastCut = nowIndex;
            }
        }
        
//        for (int i = 1; i < trace.size(); ++i) {
//            TracePoint last = trace.getTracePointAtIndex(i - 1);
//            TracePoint now = trace.getTracePointAtIndex(i);
//            if (Math.abs(now.getTimeSI() - last.getTimeSI()) > timejump) {
//                // cut trace here
//                list.add(trace.subTrace(lastCut, i));
//                lastCut = i;
//            }
//        }
        list.add(trace.subTrace(lastCut, trace.size()));

        // remove all pieces which are too small
        List<Trace> split = new ArrayList<Trace>();
        for (int i = 0; i < list.size(); ++i) {
            Trace tr = list.get(i);
            if (tr.getTracePointAtIndex(tr.size() - 1).getTimeSI() - tr.getTracePointAtIndex(0).getTimeSI() > minTime) {
                split.add(tr);
            }
        }

        System.out.println("__TraceFilter__: split Trace in " + split.size() + " pieces.");

        return split;
    }

    /** method to use the LagFilter. Correct the sensor lag of the trace
     * 
     * @return
     */
    public Trace correctSensorLag() {
        //just to be sure that the pathAugmenter uses maps -> lagCorrection does need a lot of points more than once
        this.trace.getPathAugmenter().useMaps();
        
        this.lagFilter.correctCoorLag();
        System.out.println("__TraceFilter__: Offset of GPS Coor is "+lagFilter.offsetSpeed);
        this.lagFilter.correctHeadingLag();
        System.out.println("__TraceFilter__: Offset of GPS Heading is "+lagFilter.offsetYaw);
        //update trace
        this.lagFilter.correctTrace();
        return this.trace;
    }
    
    /** method to use the ErrorFilter. Correct the relative and absoulte error of speed and yaw-rate
     * 
     * @return
     */
    public Trace correctSensorError() {
        
        double error = this.errorFilter.correctSpeedError();
        System.out.println("__TraceFilter__: The relativistic Speed Error is: " + error);
        
        double drift = this.errorFilter.correctDriftError();
        System.out.println("__TraceFilter__: The drift error is: " + drift);

        this.errorFilter.correctTrace();
        return this.trace;
    }
    
    /** function to get the times we calculate our values for. 
     * TimeStamp.EXTRACTED (and REALDATA_EXTRACTED) simulates the original TraceFilter (midPoints between the other Points) 
     * 
     * Every(!) correction should use this function!
     * 
     * @see com.dcaiti.traceloader.odometrie.TraceFilter3.TimeStamp
     * 
     * @param String only needed intern for the different kinds of realdata-information
     * 
     * @return array of times we use for the integration
     */
    private long[] getTimesToCalculate(String value){
        if(this.time == TimeStamp.EXTRACTED){
            long[] times = new long[this.trace.size()-1];
            for(int i = 0; i < times.length; ++i){
                long time = this.trace.getTracePointAtIndex(i).getTime();
                long timediff = this.trace.getTracePointAtIndex(i+1).getTime()-time;
                times[i] = time + timediff/2;
            }
            return this.cutTimes(times,this.minTraceTime,this.maxTraceTime);
        }else if(this.time == TimeStamp.REALDATA_EXTRACTED){
            if(value == "yaw"){
                return this.cutTimes(this.trace.getPathAugmenter().getYawRateTimesOriginal(),this.minTraceTime,this.maxTraceTime);
            }else{
                return this.cutTimes(this.trace.getPathAugmenter().getSpeedTimesOriginal(),this.minTraceTime,this.maxTraceTime);
            }
        }else if(this.time == TimeStamp.REALDATA_NORMAL){
            return this.cutTimes(this.trace.getPathAugmenter().getCoorTimesOriginal(),this.minTraceTime,this.maxTraceTime);
        }else if(this.time == TimeStamp.NORMAL){
            long[] times = new long[this.trace.size()];
            for(int i = 0; i < times.length; ++i){
                times[i] = this.trace.getTracePointAtIndex(i).getTime();
            }
            return this.cutTimes(times,this.minTraceTime,this.maxTraceTime);
        }
        return null;
    }
    
    /** function to get the times we calculate our values for. 
     * TimeStamp.EXTRACTED (and REALDATA_EXTRACTED) simulates the original TraceFilter (midPoints between the other Points) 
     * 
     * Every(!) correction should use this function!
     * @see com.dcaiti.traceloader.odometrie.TraceFilter3.TimeStamp
     * 
     * @return array of times we use for the integration
     */
    private long[] getTimesToCalculate(){
        return getTimesToCalculate(null);
    }
    
    /** helper method to get all speeds by the given times
     * 
     * @param times we got from {@link com.dcaiti.traceloader.odometrie.TraceFilter3#getTimesToCalculate() getTimesToCalculate}
     * @return array of speed-values
     */
    private double[] getCalculatedSpeed(long[] times){
        double[] values = new double[times.length];
        for(int i = 0; i < times.length; ++i){
            values[i] = trace.getSpeedSIByTime(times[i]);
        }
        return values;
    }
    
    /** helper method to get all yaw-rates by the given times
     * 
     * @param times we got from {@link com.dcaiti.traceloader.odometrie.TraceFilter3#getTimesToCalculate() getTimesToCalculate}
     * @return array of yaw-rate values
     */
    private double[] getCalculatedYaw(long[] times){
        double[] values = new double[times.length];
        for(int i = 0; i < times.length; ++i){
            values[i] = trace.getYawRateByTime(times[i]);
        }
        return values;
    }
    
    /** helper method to create an array of values to display them in line with other double-values for the class LineChart
     * 
     * @see com.dcaiti.traceloader.odometrie.LineChart LineChart
     * 
     * @param times the times we are interested about. only there we ask us if they are ignored or not
     * @param error the double-value which represent the fact, that we ignore this time
     * @param helper the TimeSectionHelper which determine, which timespans we ignore
     * @return array of double-values which indicates which times we ignore. If we ignore nothing, null will be returned
     */
    private double[] visualizeIngoredPoints(long[] times, double error, TimeSectionHelper helper){
        if(helper.size() != 0){
            //create ignoredPoint array
            double[] ign = new double[times.length];
            for(int i = 0; i < ign.length; ++i){
                if(helper.onSection(times[i]) != 0){
                    ign[i] = error;
                }
            }
            return ign;
        }
        return null;
    }
    
    /** function to cut the times array with minTime and maxTime.
     *  If minTime or MaxTime are in the array, the cut array will also have them (included)
     * 
     * @param preTimes the time array we have to cut
     * @param minTime the minimal allowed time in the array
     * @param maxTime the maximal allowed time in the array
     * @return
     */
    private long[] cutTimes(long[] preTimes, long minTime, long maxTime){
        int index = 0;
        while(index < preTimes.length && preTimes[index] < minTime){
            ++index;
        }
        int min = index;
        index = preTimes.length-1;
        while(index >= 0 && preTimes[index] > maxTime){
            --index;
        }
        int max = index+1; //max is exclusive by copyOfRange
        long[] times = Arrays.copyOfRange(preTimes, min, max); 
        return times;
    }
    
    /** helper method to normalize some double-array, so that all values are between 0 and 1.
     * 
     * @param values the values that have to be normalized
     * @param reflect if true, we reflect the values after normalizing. this means the highest value will be the lowest and visa versa
     * @return
     */
    private double[] normalize(double[] values, boolean reflect){
        double[] norm = new double[values.length];
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for(int i = 0; i < values.length; ++i){
            if(values[i] < min){
                min = values[i];
            }
            if(values[i] > max){
                max = values[i];
            }
        }
        
        double diff = max-min;
        
        for(int i = 0; i < norm.length; ++i){
            norm[i] = (values[i] - min) / diff;
            if(reflect){
                norm[i] = 1 - norm[i];
            }
        }
        return norm;
    }

    /** helper method for LineChart
     * 
     * @see com.dcaiti.traceloader.odometrie.LineChart LineChart
     * 
     * @param data the data we want to visualize
     * @param times the time of the data
     * @param names a list of names for each data-array
     * @param x_axis the name of the x-axis
     * @param y_axis the name of the y-axis
     * @param title which title should be given to the window
     * @param flags the flags determine if something is printed or showed...or both
     */
    public static void showDataLine(List<double[]> data, long[] times , List<String> names, String x_axis, String y_axis, String title,
            EnumSet<TraceFilter3.Output> flags) {
        LineChart chart = new LineChart(title);
        
        if(times == null){
            chart.addAllLinearData(data, names);
        }else{
          //times has to be converted -> from long to double and in seconds
            List<Double> timeEdited = new ArrayList<Double>();
            long min = Long.MAX_VALUE;
            for(int i = 0; i < times.length; ++i){
                if(times[i] < min){
                    min = times[i];
                }
            }
            for(int i = 0; i < times.length; ++i){
                timeEdited.add((times[i]-min)/(double)1000);
            }
            chart.addAllNonLinearData(data, timeEdited,names);
        }
        chart.initChart("seconds", y_axis);
        for (int i = 0; i < data.size(); ++i) {
            chart.setShape(true, i);
        }

        EnumSet<Output> checkPrint = Output.PRINT.clone();
        EnumSet<Output> checkShow = Output.SHOW.clone();
        checkPrint.retainAll(flags);
        checkShow.retainAll(flags);

        if (!checkPrint.isEmpty()) {
            chart.printChart();
        }
        if (!checkShow.isEmpty()) {
            chart.showChart();
        }
    }
    
    /** same as {@link com.dcaiti.traceloader.odometrie.TraceFilter3#showDataLine(List, long[], List, String, String, String, EnumSet) showDataLine(List,...)}
     * but only with one line of data we want to visualize
     * 
     * @param date
     * @param times
     * @param name
     * @param x_axis
     * @param y_axis
     * @param title
     * @param flags
     */
    public static void showDataLine(double[] date, long[] times ,String name, String x_axis, String y_axis, String title,
            EnumSet<TraceFilter3.Output> flags) {
        
        List<double[]> data = new ArrayList<double[]>();
        List<String> names = new ArrayList<String>();
        data.add(date);
        names.add(name);
        showDataLine(data,times,names,x_axis,y_axis,title,flags);
    }
    
    /** helper method to get the title of the LineChart. Used to have the option, if the trace VehId should be displayed or not
     * 
     * @see com.dcaiti.traceloader.odometrie.TraceFilter3#showDataLine(List, long[], List, String, String, String, EnumSet) showDataLine()
     * 
     * @param end
     * @return
     */
    private String getMidOfTitle(String end){
        if(end != null){
            end = " - "+end;
        }
        if(this.vehId){
            return " of Trace "+trace.getBaseVehId()+end;
        }
        return end;
    }

    /** Enum to determine which step of the TraceFilter should be visualized.
     *  The prefix tells us, if we want to show the data with a Java-UI or just print a svg. File
     *  There are a few possible enums:
     *  <ul>
     *  <li> COOR_LAG - Shows or print the correction/calculation of the lag between GPS-Coor and speed </li>
     *  <li> HEADING_LAG - Shows or print the correction/calculation of the lag between GPS-Heading and yaw-rate </li>
     *  <li> COOR_OFFSET - Shows or print the calculated offset-values from the calculation from the lag between GPS-Coor and speed (the values we got from the Integration of the {@link com.dcaiti.traceloader.odometrie.ExtendedIntegral.IntegralMethod IntegralMethod}) </li>
     *  <li> HEADING_OFFSET - Shows or print the calculated offset-values from the calculation from the lag between GPS-Heaidng and yaw-rate (the values we got from the Integration of the {@link com.dcaiti.traceloader.odometrie.ExtendedIntegral.IntegralMethod IntegralMethod}) </li>
     *  <li> MERGED_OFFSET - Shows or print the merged offset-values from the coor-offset and heading-offset - only happens if option {@link com.dcaiti.traceloader.odometrie.TraceFilter3#shiftPath() shiftPath} is on </li>
     *  <li> SPEED_ERROR - Shows or print the correction/calculation of the relative speed error </li>
     *  <li> DRIFT - Shows or print the correction/calculation of the absolute yaw-rate error </li>
     *  </ul>
     *  
     *  @see com.dcaiti.traceloader.odometrie.TraceFilter3#setFlag(EnumSet) TraceFilter3.setFlag(EnumSet)
     * 
     * @author nkl - Nicolas Klenert
     *
     */
    public enum Output {
        SHOW_SPEED_ERROR, PRINT_SPEED_ERROR,
        SHOW_HEADING_LAG, PRINT_HEADING_LAG,
        SHOW_COOR_LAG, PRINT_COOR_LAG,
        SHOW_DRIFT, PRINT_DRIFT,
        SHOW_COOR_OFFSET, PRINT_COOR_OFFSET,
        SHOW_HEADING_OFFSET, PRINT_HEADING_OFFSET,
        SHOW_MERGED_OFFSET, PRINT_MERGED_OFFSET;
        public static final EnumSet<Output> SHOW = EnumSet.of(SHOW_SPEED_ERROR, SHOW_HEADING_LAG, SHOW_COOR_LAG, SHOW_DRIFT, SHOW_COOR_OFFSET, SHOW_HEADING_OFFSET, SHOW_MERGED_OFFSET);
        public static final EnumSet<Output> PRINT = EnumSet.of(PRINT_SPEED_ERROR, PRINT_HEADING_LAG, PRINT_COOR_LAG, PRINT_DRIFT, PRINT_COOR_OFFSET, PRINT_HEADING_OFFSET, PRINT_MERGED_OFFSET);
    }
    
    /** flags to define which data should be used to calculate values by the filters.
     * 
     * <ul>
     * <li> NORMAL - use the time stamps on the TracePoints/DataPoints </li>
     * <li> EXTRACTED - use the mid time between all TracePoints/DataPoints  </li>
     * </ul>
     * 
     * the prefix REALDATA has as effect, that we use only times which can be found as tokens in pathAugmenter. Because PathAugmenter got only the original GPS-Data
     * this times are the best to represent the "original" data we got. All Points without data ore which are interpolated are ignored
     * 
     * The effect of REALDATA_NORMAL on 50Hz traces is, that we get only every 50th TracePoint time (the TracePoint who got the real gps-data)
     * 
     * @see com.dcaiti.traceloader.odometrie.TraceFilter3#setTimesToUse(TimeStamp) TraceFilter3.setTimestoUse()
     * @see com.dcaiti.traceloader.odometrie.TraceFilter3#getTimesToCalculate() TraceFilter3.getTimesToCalculate()
     * 
     * @author nkl - Nicolas Klenert
     *
     */
    public enum TimeStamp {
        NORMAL, EXTRACTED, REALDATA_NORMAL, REALDATA_EXTRACTED;
    }
        
    /** inner class which calculate and improve the trace by the sensor-lag
     * 
     * @author nkl - Nicolas Klenert
     *
     */
    public class LagFilter{
        private IntegralMethod method;
        private ExtendedIntegral integral;
        
        private long minOffset = -4000l;
        private long maxOffset = 0l;
        private long step = 50l;
        
        private long offsetSpeed;
        private long offsetYaw;
        
        private double[] offsetsSpeed;
        private double[] offsetsYaw;
        
        //variable to determine if we should correct the data or the path
        private boolean shiftPath;
        //variable to determine if we should normalize the data
        private boolean normalize;
        
        public LagFilter(IntegralMethod method){
            this.method = method;
            this.integral = new ExtendedIntegral(method);
        }
        
        /** method called by TraceFilter3. 
         * 
         * @see com.dcaiti.traceloader.odometrie.TraceFilter3#setIntegralMethod(IntegralMethod) setIntegralMethod
         * 
         * @param method
         */
        public void setMethod(IntegralMethod method){
            this.method = method;
            this.integral.setIntegralMethod(method);
        }
        
        /** function to correct the trace with the information we have
         * 
         * use the shiftPath option to determine how we should shift the data
         * 
         * @see com.dcaiti.traceloader.odometrie.TraceFilter3#shiftPath() shiftPath()
         * @see com.dcaiti.traceloader.odometrie.TraceFilter3#shiftData() shiftData()
         * 
         */
        private void correctTrace(){
            if(this.shiftPath){
                if(this.offsetsSpeed != null && this.offsetsYaw != null){
                    //clash both offsets together and use the highest point
                    double max = -Double.MAX_VALUE;
                    int maxStep = -1;
                    double[] offsets = new double[this.offsetsSpeed.length];
                    for(int i = 0; i < this.offsetsSpeed.length; ++i){
                        double value = this.offsetsSpeed[i] + this.offsetsYaw[i];
                        offsets[i] = value;
                        if(max < value){
                            max = value;
                            maxStep = i;
                        }
                    }
                    //get offset
                    trace.getPathAugmenter().setOffset(minOffset + maxStep*step);
                    System.out.println("__TraceFilter__: choosed "+trace.getPathAugmenter().getOffset()+" as Offset!");
                    trace = trace.redoWithAugmenter();
                    if(flags.contains(Output.SHOW_MERGED_OFFSET) || flags.contains(Output.PRINT_MERGED_OFFSET)){                        
                        LineChart chart = new LineChart("Values of MergedOffset"+getMidOfTitle(null));
                        chart.addLinearData(offsets, minOffset, step, "Values");
                        chart.initChart("normalized Values", "Offset-Value");
                        if(flags.contains(Output.SHOW_MERGED_OFFSET)){
                            chart.showChart();
                            chart.setRange(minOffset,maxOffset);
                        }
                        if(flags.contains(Output.PRINT_MERGED_OFFSET)){
                            chart.setRange(minOffset,maxOffset);
                            chart.printChart();
                        }
                    }
                }else{
                    //old method
                    int counter = 0;
                    long offset = 0l;
                    if(this.offsetSpeed != 0){
                        ++counter;
                        offset += this.offsetSpeed;
                    }
                    if(this.offsetYaw != 0){
                        ++counter;
                        offset += this.offsetYaw;
                    }
                    if(offset != 0){
                        trace.getPathAugmenter().setOffset(offset/counter);
                        trace = trace.redoWithAugmenter();
                    }
                }

            }else{
                trace.getPathAugmenter().setOffset(0);
                //we have to negate the offset because it is the offset of the shifted path!
                trace = trace.redoDataWithOffset(-this.offsetSpeed, -this.offsetYaw);
            }
           
        }
        
        /** wrapper function for {@link com.dcaiti.traceloader.odometrie.TraceFilter3.LagFilter#calculateOffset(boolean) calculateOffset}
         * 
         * @return
         */
        private long correctCoorLag(){
            this.offsetSpeed = this.calculateOffset(true);
            return this.offsetSpeed;
        }
        
        /** wrapper function for {@link com.dcaiti.traceloader.odometrie.TraceFilter3.LagFilter#calculateOffset(boolean) calculateOffset}
         * 
         * @return
         */
        private long correctHeadingLag(){
            this.offsetYaw = this.calculateOffset(false);
            return this.offsetYaw;
        }
        
        /** function to calculate the offset of data
         * 
         * @param coor boolean which indicates which offset should be caluclated - speed or yaw-rate
         * @return
         */
        private long calculateOffset(boolean coor){     

            PathAugmenter path = trace.getPathAugmenter();
            
            //get min and max timestamp (with PathAugmenter)
            path.setOffset(0);
            long minTime = coor ? path.getSpeedMinTime() : path.getYawRateMinTime();
            minTime = Math.max(minTime, minTraceTime);
            minTime += maxOffset;
            long maxTime = coor ? path.getSpeedMaxTime() : path.getYawRateMaxTime();
            maxTime = Math.min(maxTime, maxTraceTime);
            maxTime += minOffset;
            
            long minTimeSpan = 50000l;  //50 seconds
            if(maxTime - minTime < minTimeSpan){
                System.out.println("__TraceFilter__: The given Trace is not long enough to calculate an offset!");
                return 0l;
            }
            
            String string = coor ? "speed" : "yaw";
            long[] preTimes = getTimesToCalculate(string);
            long[] times = cutTimes(preTimes,minTime,maxTime);
            
            double[] values = new double[times.length];  
            double[] gpsValues = new double[times.length];
            
            if(coor){
                for(int i = 0; i < times.length; ++i){
                    gpsValues[i] = path.getSpeedByTime(times[i]);
                }
                values = getCalculatedSpeed(times);
            }else{
                for(int i = 0; i < times.length; ++i){
                    gpsValues[i] = path.getYawRateByTime(times[i]);
                }
                values = getCalculatedYaw(times);
            }
            
            //ignore some TimeSections if you want
            TimeSectionHelper helper = new TimeSectionHelper();
            helper.closeBuild();
            
            //find offset
            int maxIteration = (int) Math.ceil((maxOffset - minOffset) / step) +1;
            double[] offsets = new double[maxIteration];
            long point = 0;
            //Double.MIN_VALUE ist the smallest POSITIVE(!) number, not the smallest overall
            double maxValue = toMaximise(method) ? -Double.MAX_VALUE : Double.MAX_VALUE;
            long maxPoint = 0;
            double[] maxGPSValues = gpsValues;
            double[] origGPSValues = gpsValues;

            for(int i = 0; i < offsets.length; ++i){
                point = minOffset + i*step;
                helper.setOffset(point);
                path.setOffset(point);
                //create gpsValues (again)
                gpsValues = new double[gpsValues.length];
                for(int j = 0; j < times.length; ++j){
                    gpsValues[j] = coor ? path.getSpeedByTime(times[j]) : path.getYawRateByTime(times[j]);
                }
                
                double[] gpsValuesN = new double[gpsValues.length];
                double[] valuesN = new double[values.length];
                if(this.normalize){
                    //TODO: find out why this do not work! The calculation is also here wrong!
                    //here we "normalize" the gpsValues -> because some values are thrown out and some values got in the integral, we want to normalize all
                    double gpsMean = ExtendedIntegral.meanValue(gpsValues, times, helper);
                    double mean = ExtendedIntegral.meanValue(values, times, helper);
                    for(int k = 0; k < gpsValues.length; ++k){
                        gpsValuesN[i] = gpsValues[i] / gpsMean;
                        valuesN[i] = values[i] / mean;
                    }
                }else{
                    gpsValuesN = gpsValues;
                    valuesN = values;
                }
                
                //if you use the method Integral.Max or Integral.MINMAX you have to search for the minimum NOT the maximum of the offsets
                offsets[i] = integral.crossCorrelation(valuesN,times,gpsValuesN,helper);
                
                if(toMaximise(method)){
                    if(offsets[i] > maxValue){
                        maxValue = offsets[i];
                        maxPoint = point;
                        maxGPSValues = gpsValues;
                    }
                }else{
                    if(offsets[i] < maxValue){
                        maxValue = offsets[i];
                        maxPoint = point;
                        maxGPSValues = gpsValues;
                    }
                }

            }
            
            if(coor)
                this.offsetsSpeed = normalize(offsets, !this.toMaximise(this.method));
            else
                this.offsetsYaw = normalize(offsets, !this.toMaximise(this.method));
            
            boolean show = false;
            List<String> names = new ArrayList<String>();
            if(coor){
                show = flags.contains(Output.SHOW_COOR_LAG) || flags.contains(Output.PRINT_COOR_LAG);
                names.add("Speed from Sensor");
                names.add("GPS-Speed (calculated from Coor)");
                names.add("GPS-Speed with corrected Offset");
            }else{
                show = flags.contains(Output.SHOW_HEADING_LAG) || flags.contains(Output.PRINT_HEADING_LAG);
                names.add("Yaw-Rate from Sensor");
                names.add("Yaw-Rate (calculated from GPS)");
                names.add("Yaw-Rate with corrected Offset");
            }
            
            if (show) {
                if(helper.size() != 0){
                    names.add("Ignored Points");
                }

                List<double[]> data = new ArrayList<double[]>();
                data.add(values);
                data.add(origGPSValues);
                data.add(maxGPSValues);
                if(helper.size() != 0){
                    helper.setOffset(maxPoint);
                    data.add(visualizeIngoredPoints(times,5,helper));
                }

                String title = coor ? "Speed" : "Yaw-Rate";
                showDataLine(data,times, names, "Time", "m/s", title + getMidOfTitle("Lag"), flags);
            }
            
            if(coor){
                show = flags.contains(Output.SHOW_COOR_OFFSET) || flags.contains(Output.PRINT_COOR_OFFSET);
            }else{
                show = flags.contains(Output.SHOW_HEADING_OFFSET) || flags.contains(Output.PRINT_HEADING_OFFSET);
            }
            
            if(show) {
                String name = method.name();
                name = name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase(); 
                
                LineChart chart = new LineChart("Values of "+name+ getMidOfTitle(coor ? "CoorLag" : "HeadingLag"));
                chart.addLinearData(normalize(offsets,false), minOffset, step, name);
                chart.initChart("Value of "+name+"-Integral", "Offset-Value");
                if(flags.contains(Output.SHOW_COOR_OFFSET) || flags.contains(Output.SHOW_HEADING_OFFSET)){
                    chart.showChart();
                    chart.setRange(minOffset,maxOffset);
                }
                if(flags.contains(Output.PRINT_COOR_OFFSET) || flags.contains(Output.PRINT_HEADING_OFFSET)){
                    chart.setRange(minOffset,maxOffset);
                    chart.printChart();
                }
            }

            return maxPoint;
        }
        
        /** method which say if we want to have the minimum or the maximum of the offset-value-array
         * 
         * @param method IntegralMethod we used
         * @return true, if we want the maximum, false if otherwise
         */
        private boolean toMaximise(IntegralMethod method){
            switch(method){
            case MIN:
            case CROSSCORRELATION:
                return true;
            case MAX:
            case MINMAX:
                return false;
            default: return true;
            }
        }

        private void clear() {
            this.offsetSpeed = 0;
            this.offsetYaw = 0;  
        }
    }
    
    public class ErrorFilter{
        private double speedError = 1d;
        private double driftError = 0d;
        
        /**
         * function to correct the trace with the information we have
         */
        private void correctTrace(){
            Trace corr = new Trace(trace.getVehicleId());
            for (int i = 0; i < trace.size(); ++i) {
                double speed = trace.getTracePointAtIndex(i).getSpeed() * this.speedError;
                double yaw = trace.getTracePointAtIndex(i).getYawRate() + this.driftError;
                corr.add(new TracePoint.Builder(trace.getTracePointAtIndex(i)).speed(speed).yawRate(yaw).build());
            }
            trace = corr;
        }
        
        /** method to get the absolute error of the yaw-rate
         * 
         * @return absolute error
         */
        private double correctDriftError(){
            
            PathAugmenter path = trace.getPathAugmenter();
            
            long[] preTimes = getTimesToCalculate();
            long[] times = cutTimes(preTimes,
                    Math.max(path.getYawRateMinTime(), trace.getTracePointAtIndex(0).getTime()),
                    Math.min(path.getYawRateMaxTime(), trace.getTracePointAtIndex(trace.size()-1).getTime()));
            
            
            double[] gpsValues = new double[times.length];
            double[] values;
            
            for(int i = 0; i < times.length; ++i){
                gpsValues[i] = path.getYawRateByTime(times[i]);
            }
            values = getCalculatedYaw(times);
            
            TimeSectionHelper helper = createTimeSections(trustworthyYaw(gpsValues,times),true,times);
            
            double diff = ExtendedIntegral.integralIgnore(gpsValues, times, helper) - ExtendedIntegral.integralIgnore(values, times, helper);
            double[] ones = new double[values.length];
            for (int i = 0; i < ones.length; ++i) {
                ones[i] = 1;
            }
            double timediff = ExtendedIntegral.integralIgnore(ones, times, helper);
            // double timediff = timesYaw[timesYaw.length -1]-timesYaw[0];
            // -> this is wrong! because not all time steps are counted by the
            // integral! (and the integral is 2000 times bigger than normal)

            this.driftError = diff / timediff;

            if (flags.contains(Output.SHOW_DRIFT) || flags.contains(Output.PRINT_DRIFT)) {
                // create data for corrected
                double[] corrected = new double[values.length];
                for (int i = 0; i < corrected.length; ++i) {
                    corrected[i] = values[i] + this.driftError;
                }

                List<String> names = new ArrayList<String>();
                names.add("Yaw-rate from Sensor");
                names.add("Yaw-rate from GPS-heading");
                names.add("Yaw-rate corrected / Drift corrected");
                if(helper.size() != 0){
                    names.add("Ignored Points");
                }

                List<double[]> data = new ArrayList<double[]>();
                data.add(values);
                data.add(gpsValues);
                data.add(corrected);
                if(helper.size() != 0){
                    data.add(visualizeIngoredPoints(times,0.1,helper));
                }

                showDataLine(data,times, names, "Time", "rad/s", "Yaw-Rate" +getMidOfTitle("Drift"),
                        flags);
            }
            return this.driftError;
        }
        
        /**
         *  function to get the relative error of the speed
         * @return the relative error: the value we have to multiply to the speed-sensor-data
         */
        private double correctSpeedError(){
            
            PathAugmenter path = trace.getPathAugmenter();
            
            long[] preTimes = getTimesToCalculate();
            long[] times = cutTimes(preTimes,
                    Math.max(path.getSpeedMinTime(), trace.getTracePointAtIndex(0).getTime()),
                    Math.min(path.getSpeedMaxTime(), trace.getTracePointAtIndex(trace.size()-1).getTime()));
            
            double[] gpsValues = new double[times.length];
            double[] values;
            
            for(int i = 0; i < times.length; ++i){
                gpsValues[i] = path.getSpeedByTime(times[i]);
            }
            values = getCalculatedSpeed(times);
            
            //possible improvement: do not only ignore places with speed == 0; ignore everything except a few high points (where gps and speed have the same structure) 
            TimeSectionHelper helper = createTimeSections(trustworthySpeed(times),false,times);
            
            double threshold = 0.1;
            int max = 100;
            int counter = 0;
            double error = 1;
            double step = 0.1;
            double diff = ExtendedIntegral.integralIgnore(values, times, helper) - ExtendedIntegral.integralIgnore(gpsValues, times, helper);
            double[] speed = values.clone();
            
            while (Math.abs(diff) > threshold && counter < max) {
                  if (diff > 0) {
                      error -= step;
                  } else {
                      error += step;
                  }
                  speed = addRelativeError(values, error);
                  diff = ExtendedIntegral.integralIgnore(speed, times, helper) - ExtendedIntegral.integralIgnore(gpsValues, times, helper);
                  ++counter;
                  step /= 2.0;
              }

              // System.out.println("Algo lief "+counter+" mal durch");
              this.speedError = error;
              
              
              if (flags.contains(Output.SHOW_SPEED_ERROR) || flags.contains(Output.PRINT_SPEED_ERROR)) {

                List<String> names = new ArrayList<String>();
                names.add("Speed from Sensor");
                names.add("GPS-Speed");
                names.add("Speed corrected");
                if(helper.size() != 0){
                    names.add("Ignored Points");
                }

                List<double[]> data = new ArrayList<double[]>();
                data.add(values);
                data.add(gpsValues);
                data.add(speed);
                if(helper.size() != 0){
                    data.add(visualizeIngoredPoints(times,5,helper));
                }

                showDataLine(data,times, names, "Time", "m/s", "Speed"+getMidOfTitle("rel. Error"),
                        flags);
            }
            
            return this.speedError;
        }
        
        /** helper method to add some relative error to an array
         * 
         * @param values
         * @param error
         * @return
         */
        private double[] addRelativeError(double[] values, double error) {
            double[] val = new double[values.length];
            for (int i = 0; i < val.length; ++i) {
                val[i] = values[i] * error;
            }
            return val;
        }
               
        /** function to determine, in which time span we trust the speed values
         * 
         * @param times
         * @return
         */
        private boolean[] trustworthySpeed(long[] times){
            boolean[] ignore = new boolean[times.length];
            double thresholdError = 0.1;
            
            for(int i = 0; i <times.length; ++i){
                //not trustworthy if speed is 0
                if(trace.getSpeedSIByTime(times[i]) == 0){
                    ignore[i] = true;
                }
                //not trustworthy if the data has more than 0.9 or 1.1 rel Error
                double gpsSpeed = trace.getPathAugmenter().getSpeedByTime(times[i]);
                double speed = trace.getSpeedSIByTime(times[i]);
                double relError = gpsSpeed / speed;
                if(Math.abs(relError - 1) > thresholdError){
                    ignore[i] = true;
                }
                
            }
            return ignore;
        }
        
        /** function to determine, in which time span we trust the yaw-rate values
         * 
         * @param gpsValues the headings
         * @param times
         * @return
         */
        private boolean[] trustworthyYaw(double[] gpsValues, long[] times){
            int minPoints = gpsValues.length/10;
            int points = 0;
            boolean[] trust = new boolean[times.length];
            
            double[] speed = getCalculatedSpeed(times);
            
            for(int i = 0; i < times.length; ++i){
                if(speed[i] == 0){
                    trust[i] = true;
                    ++points;
                }
            }
            
            if(points >= minPoints){
                return trust;
            }
            
            double threshold = 0;
            double step = 0.001;
            
            while(points < minPoints){
                trust = new boolean[gpsValues.length];
                points = 0;
                for(int i = 0; i < trust.length; ++i){
                    if(Math.abs(gpsValues[i]) <= threshold){
                        trust[i] = true;
                        ++points;
                    }
                }
                
                threshold += step;
            }
            return trust;
        }
        
        /** helper method which create time spans from a boolean array
         * 
         * @param flags the boolean array which indicates which times we trust
         * @param reverse if false, the flags are seen as "this times should be ignored"
         * if true, the flags are seen as "this times are trustworthy"
         * @param times the times we trust or not trust (indicate by flags)
         * @return
         */
        private TimeSectionHelper createTimeSections(boolean[] flags, boolean reverse, long[] times){
            //flags indicate the times we should ignore
            if(reverse){
                boolean[] ignore = new boolean[flags.length];
                for (int i = 0; i < ignore.length; ++i) {
                    ignore[i] = !flags[i];
                }
                flags = ignore;
            }
            
            //if times is null use times of trace
            if(times == null){
                times = new long[trace.size()];
                for(int i = 0; i < trace.size(); ++i){
                    times[i] = trace.getTracePointAtIndex(i).getTime();
                }
            }
            
            if(flags.length != times.length){
                flags = Arrays.copyOf(flags, times.length);
            }
            TimeSectionHelper helper = new TimeSectionHelper();
            boolean started = false;
            if(flags[0]){
                started = true;
                helper.setStart(times[0]);
            }
            for(int i = 1; i < flags.length; ++i){
                if(flags[i] && !started){
                    started = true;
                    helper.setStart(times[i-1]);
                }else if(!flags[i] && started){
                    started = false;
                    helper.setEnd(times[i]);
                }
            }
            if(started){
                helper.setEnd(times[times.length-1]);
            }
            helper.closeBuild();
            return helper;
        }

        private void clear() {
            this.speedError = 1d;
            this.driftError = 0d;
        }
        
    }

}

