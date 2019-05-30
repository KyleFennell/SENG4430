package com.dcaiti.traceloader.odometrie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import com.dcaiti.traceloader.Trace;
import com.dcaiti.traceloader.TracePoint;
import com.vividsolutions.jts.geom.Coordinate;

/**  Deprecated - use TraceFilter3 instead
 * 
 * Big improvement above TraceFilter (Version 1)
 *      - stable against time jumps
 *      - uses the PathAugmenter "Interface"
 *      - uses CrossCorrelation or Minimum Function (easy to implement more)
 *      - Performance boost (because it is using PathAugmenter with useMaps() and because we only update the Trace once)
 * 
 * 
 * class to improve odometrie data on the traces. Improvements here are called
 * "filter".
 * 
 * All filter-methods start with "correct". Use method correct() to use all
 * improvements in the intended order. All methods to improve a Trace with a
 * specific error-value (offset,drift etc.) start with "traceWith"
 * 
 * @author nkl - Nicolas Klenert
 *
 */
@Deprecated
public class TraceFilter2 {

    private EnumSet<Output> flags;
    public LagFilter lagFilter;
    private Trace originalTrace;
    private Trace trace;
    private List<Trace> leftover;
    private double relSpeedError;
    private double drift;

    /**
     * Constructor is only needed if you do not want to run all filters, run
     * filters with different setting, or if you want to visualize some filters
     * Otherwise just use the static function correct(Trace) which does
     * everything you need (or if you want 50Hz improvement
     * correct50Hz(Trace1Hz,Trace50Hz))
     * 
     * @param trace
     *            on which the improvements should be calculated and used
     */
    public TraceFilter2(Trace trace) {
        this(trace, false);
    }
    
    /** Only used for debugging! Do not use this class!
     * 
     */
    public TraceFilter2(){
        this.lagFilter = new LagFilter(Integral.CROSSCORRELATION);
    }

    public TraceFilter2(Trace trace, boolean toSplit) {
        this.flags = EnumSet.noneOf(Output.class);
//        this.speed = SpeedData.ExtractGPSSPeed;
        this.originalTrace = trace.clone();
        if (toSplit) {
            List<Trace> split = split(trace);
            if (split.size() == 1) {
                this.trace = split.get(0);
                this.leftover = new ArrayList<Trace>();
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
                this.trace = max;
                split.remove(max);
                this.leftover = split;
            }
        } else {
            this.leftover = new ArrayList<Trace>();
            this.trace = trace;
        }
        this.lagFilter = new LagFilter(Integral.CROSSCORRELATION);
    }

    /**
     * set flags to debug methods or to visualize something.
     * 
     * @param flag
     */
    public void setFlag(EnumSet<Output> flag) {
        this.flags = flag;
    }

    /**
     * compact method to use all improvements in the order it was intended
     * 
     * @return the improved trace
     */
    public TraceFilter2 correct() {
        // this.smoothTraceData();
        this.correctSensorLag();
        this.correctSpeedError();
        this.correctDrift();
        // this.smoothTraceData();
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
        TraceFilter2 filter = new TraceFilter2(trace);
        filter.correct();
        return filter.getCorrectedTrace();
    }

    public static List<Trace> correctAll(Trace trace) {
        TraceFilter2 filter = new TraceFilter2(trace);
        return filter.correct().getAllCorrectedTraces();
    }

    public double[] getData() {
        return new double[] { lagFilter.offsetSpeed, lagFilter.offsetYaw, this.relSpeedError, this.drift };
    }

    /**
     * used to correct trace with known errors
     * 
     * @param trace
     * @param data
     * @param Hz
     * @return
     */
    public static Trace correctWithKnownData(Trace trace, double[] data, boolean Hz) {
        if (Hz) {
            trace = traceWithOffset50Hz(trace, data[0], data[1]);
        } else {
            trace = traceWithOffset(trace, data[0], data[1]);
        }
        trace = traceWithoutSpeedError(trace, data[2]);
        trace = traceWithoutDrift(trace, data[3]);
        trace = smoothTraceData(trace, EnumSet.noneOf(Output.class));
        return trace;
    }

    /**
     * static function helper to get a improved 50Hz trace.
     * 
     * @param trace1Hz
     * @param trace50Hz
     * @return
     */
    public static Trace correct50Hz(Trace trace1Hz, Trace trace50Hz) {
        TraceFilter2 filter = new TraceFilter2(trace1Hz);
        filter.correct();
        return filter.correct50Hz(trace50Hz);
    }

    public static List<Trace> correctAll50Hz(Trace trace1Hz, Trace trace50Hz) {
        TraceFilter2 filter = new TraceFilter2(trace1Hz);
        filter.correct();
        return split(filter.correct50Hz(trace50Hz));
    }

    /**
     * function to improve a 50Hz trace with filter-data of the correction of a
     * 1Hz trace. Only add improvements which were used by the 1Hz trace.
     * 
     * If you want to add only some (and not all) filters, use some filters on
     * the 1Hz trace (Object oriented) and then use this function to add the
     * improvements to your 50Hz trace
     * 
     * @param trace
     * @return
     */
    public Trace correct50Hz(Trace trace) {
        if (lagFilter.offsetSpeed != 0 || lagFilter.offsetYaw != 0) {
            trace = traceWithOffset50Hz(trace, lagFilter.offsetSpeed, lagFilter.offsetYaw);
        }
        if (this.relSpeedError != 0) {
            trace = traceWithoutSpeedError(trace, this.relSpeedError);
        }
        if (this.drift != 0) {
            trace = traceWithoutDrift(trace, this.drift);
        }
        // trace = smoothTraceData(trace,this.defaultFlag);
        return trace;
    }

    /**
     * get the leftover from the split trace corrected (use: correct(trace) for
     * the main trace corrected and AFTERWARDS correctLeftOver) If you want to
     * have split and corrected 50Hz data, use correct50Hz and split it
     * afterwards with the split function
     * 
     * @return
     */
    private List<Trace> correctLeftover() {
        List<Trace> list = new ArrayList<Trace>();
        for (int i = 0; i < this.leftover.size(); ++i) {
            list.add(correctWithKnownData(this.leftover.get(i), this.getData(), false));
        }
        return list;
    }

    public List<Trace> getAllCorrectedTraces() {
        List<Trace> list = correctLeftover();
        list.add(0, getCorrectedTrace());
        return list;
    }

    public Trace getCorrectedTrace() {
        return this.trace;
    }

    public Trace getOriginalTrace() {
        return this.originalTrace;
    }

    public double[] getOffsetParams() {
        return new double[] { lagFilter.offsetSpeed, lagFilter.offsetYaw };
    }

    public double getRelSpeedError() {
        return this.relSpeedError;
    }

    public double getDrift() {
        return this.drift;
    }

    // function to smooth out all unexpected data -> where the yaw rate is much
    // bigger than possible and such
    public Trace smoothTraceData() {
        this.trace = smoothTraceData(this.trace, this.flags);
        return this.trace;
    }

    public static Trace smoothTraceData(Trace trace, EnumSet<Output> flags) {
        Trace tr = new Trace(trace.getVehicleId());
        tr.add(trace.getTracePointAtIndex(0));
        int counter = 0;

        double YawTreshold = 1;

        for (int i = 1; i < trace.size(); ++i) {
            TracePoint left = trace.getTracePointAtIndex(i - 1);
            TracePoint mid = trace.getTracePointAtIndex(i);
            // double heading = mid.getHeading() - left.getHeading();
            double heading = InterpolationTools.radShortestAngle(left.getHeading(), mid.getHeading());
            double timediff = mid.getTimeSI() - left.getTimeSI();
            double yaw = heading / timediff;
            if (Math.abs(yaw) > YawTreshold) {
                // we want to replace this data -> TODO: for now we assume that
                // this only happens at one time step
                tr.add(new TracePoint.Builder(mid).heading(getNextHeading(left, mid)).build());
                // System.out.println(i);
                ++counter;
            } else {
                tr.add(mid);
            }

        }

        System.out.println("__TraceFilter__: " + counter + " Places were smoothed out!");

        return tr;
    }

    public static double getNextHeading(TracePoint last, TracePoint now) {
        double midYaw = (last.getYawRate() + now.getYawRate()) / 2.0;
        return last.getHeading() + midYaw;
    }

    /**
     * function to split the trace, if there are any jumps
     * 
     * @param trace
     * @return
     */
    public static List<Trace> split(Trace trace) {
        List<Trace> list = new ArrayList<Trace>();
        double timejump = 1.05;
        double minTime = 30;
        int lastCut = 0;
        for (int i = 1; i < trace.size(); ++i) {
            TracePoint last = trace.getTracePointAtIndex(i - 1);
            TracePoint now = trace.getTracePointAtIndex(i);
            if (Math.abs(now.getTimeSI() - last.getTimeSI()) > timejump) {
                // cut trace here
                list.add(trace.subTrace(lastCut, i));
                lastCut = i;
            }
        }
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

    public Trace correctSensorLag() {
        //just to be sure that the pathAugmenter uses maps -> lagCorrection does need a lot of points more than once
        this.trace.getPathAugmenter().useMaps();
        
        this.lagFilter.correctCoorLag();
        System.out.println("__TraceFilter__: Offset of GPS Coor is "+lagFilter.offsetSpeed);
        this.lagFilter.correctHeadingLag();
        System.out.println("__TraceFilter__: Offset of GPS Heading is "+lagFilter.offsetYaw);
        //update trace
        this.trace.getPathAugmenter().setOffset((lagFilter.offsetSpeed + lagFilter.offsetYaw) /2);
        this.trace = this.trace.redoWithAugmenter();
        return this.trace;
    }

    private static Object[] extractGPSSpeed(Trace trace) {
        double[] gpsSpeed = new double[trace.size() - 1];
        double[] midSpeed = new double[trace.size() - 1];
        long[] times = new long[trace.size() - 1];

        for (int i = 0; i < trace.size() - 1; ++i) {
            TracePoint one = trace.getTracePointAtIndex(i);
            TracePoint two = trace.getTracePointAtIndex(i + 1);
            double timediff = (two.getTime() - one.getTime()) / 1000.0;
            gpsSpeed[i] = one.getCoor().distance(two.getCoor()) / timediff;
            midSpeed[i] = (one.getSpeedSI() + two.getSpeedSI()) / 2.0;
            times[i] = one.getTime() + (two.getTime() - one.getTime()) / 2;
        }

        return new Object[] { gpsSpeed, midSpeed, times };
    }

    private static boolean[] reverse(boolean[] trust) {
        boolean[] ignore = new boolean[trust.length];
        for (int i = 0; i < ignore.length; ++i) {
            ignore[i] = !trust[i];
        }
        return ignore;
    }
    
    
    
    

    public static Trace traceWithOffset50Hz(Trace trace, double offsetCoor, double offsetHeading) {
        return traceWithOffset(trace, offsetCoor * 50.0, offsetHeading * 50.0);
    }

    public static Trace traceWithOffset(Trace trace, double offsetCoor, double offsetHeading) {
        Trace tr = new Trace(trace.getVehicleId());
        double[] xCoor = new double[trace.size()];
        double[] yCoor = new double[trace.size()];
        double[] heading = new double[trace.size()];

        for (int i = 0; i < trace.size(); ++i) {
            xCoor[i] = trace.getTracePointAtIndex(i).getCoor().x;
            yCoor[i] = trace.getTracePointAtIndex(i).getCoor().y;
            heading[i] = trace.getTracePointAtIndex(i).getHeading();
        }

        double[] linXCoor = interpolationLinear(xCoor, -offsetCoor);
        double[] linYCoor = interpolationLinear(yCoor, -offsetCoor);
        double[] linHead = interpolationLinear(heading, -offsetHeading, true);

        for (int i = 0; i < linXCoor.length; ++i) {
            // interpolate linear
            Coordinate coor = new Coordinate(linXCoor[i], linYCoor[i]);
            tr.add(new TracePoint.Builder(trace.getTracePointAtIndex(i)).coordinate(coor).heading(linHead[i]).build());
        }

        double offset = Math.max(Math.abs(offsetCoor), Math.abs(offsetHeading));
        int cut = (int) Math.ceil(offset);
        tr = tr.subTrace(cut, tr.size() - cut);
        return tr;
    }

    private static double[] interpolationLinear(double[] values, double offset, boolean rad) {
        if (offset < 0) {
            return interpolationLinearToLeft(values, -offset, rad);
        } else if (offset > 0) {
            return interpolationLinearToRight(values, offset, rad);
        } else {
            return values;
        }
    }

    private static double[] interpolationLinearToRight(double[] values, double offset, boolean rad) {
        int start = (int) Math.ceil(offset);
        double[] interpolated = new double[values.length];
        int fullStep = (int) Math.floor(offset);
        for (int i = 0; i < start; ++i) {
            interpolated[i] = Double.NaN;
        }
        double rest = offset - fullStep;
        double interpolation;
        for (int i = start; i < interpolated.length; ++i) {
            if (rest == 0) {
                interpolation = values[i - fullStep];
            } else {
                double left = values[i - fullStep - 1];
                double right = values[i - fullStep];
                // the interpolation is linear -> use splines etc to have better
                // results
                if (rad) {
                    interpolation = InterpolationTools.radInterpolation(left, right, rest);
                } else {
                    interpolation = InterpolationTools.scalarInterpolation(left, right, rest);
                }
            }
            interpolated[i] = interpolation;
        }
        return interpolated;
    }

    private static double[] interpolationLinearToLeft(double[] values, double offset, boolean rad) {
        int length = values.length - (int) Math.ceil(offset);
        double[] interpolated = new double[values.length];
        int fullStep = (int) Math.floor(offset);
        for (int i = length; i < interpolated.length; ++i) {
            interpolated[i] = Double.NaN;
        }
        for (int i = 0; i < length; ++i) {
            double rest = offset - fullStep;
            double interpolation;
            if (rest == 0) {
                interpolation = values[i + fullStep];
            } else {
                double left = values[i + fullStep];
                double right = values[i + fullStep + 1];
                // the interpolation is linear -> use splines etc to have better
                // results
                if (rad) {
                    interpolation = InterpolationTools.radInterpolation(left, right, rest);
                } else {
                    interpolation = InterpolationTools.scalarInterpolation(left, right, rest);
                }
            }
            interpolated[i] = interpolation;
        }
        return interpolated;
    }

    private static double[] interpolationLinear(double[] values, double offset) {
        return interpolationLinear(values, offset, false);
    }

    public Trace correctSpeedError() {
        Object[] obj;
//        if (this.speed == SpeedData.GPSSpeed) {
//            obj = GPSSpeed(trace);
//        } else {
            obj = extractGPSSpeed(trace);
//        }
        double[] gpsSpeed = (double[]) obj[0];
        double[] midSpeed = (double[]) obj[1];
        long[] times = (long[]) obj[2];
        boolean[] trust = trustworthyDet(trace, 0.1, 1);
//        if (this.speed == SpeedData.ExtractGPSSPeed) {
            trust = sizeDown(trust);
//        }

        boolean[] ignore = reverse(trust);

        // System.out.println("Integral von Speed ist:
        // "+integral(midSpeed,times));
        // System.out.println("Integral von GPS ist:
        // "+integral(gpsSpeed,times));

        // algo
        double threshold = 0.1;
        int max = 100;
        int counter = 0;
        double error = 1;
        double step = 0.1;
        TimeSectionsHelper helper = createTimeSections(ignore, times);
        double diff = integral(midSpeed, times, helper) - integral(gpsSpeed, times, helper);
        double[] speed = midSpeed.clone();

        while (Math.abs(diff) > threshold && counter < max) {
            if (diff > 0) {
                error -= step;
            } else {
                error += step;
            }
            speed = addRelativeError(midSpeed, error);
            diff = integral(speed, times, helper) - integral(gpsSpeed, times, helper);
            ++counter;
            step /= 2.0;
        }

        // System.out.println("Algo lief "+counter+" mal durch");
        System.out.println("__TraceFilter__: The relativistic Speed Error is: " + error);
        this.relSpeedError = error;

        if (flags.contains(Output.SHOW_SPEED_ERROR) || flags.contains(Output.PRINT_SPEED_ERROR)) {
            // create data for ignore
            double[] ign = new double[ignore.length];
            for (int i = 0; i < ign.length; ++i) {
                if (ignore[i]) {
                    ign[i] = 5;
                } else {
                    ign[i] = 0;
                }
            }

            List<String> names = new ArrayList<String>();
            names.add("Speed from Sensor");
            names.add("GPS-Speed");
            names.add("Speed corrected");
            names.add("Ignored Points");

            List<double[]> data = new ArrayList<double[]>();
            data.add(midSpeed);
            data.add(gpsSpeed);
            data.add(speed);
            data.add(ign);

            showDataLine(data,null, names, "TracePoint", "m/s", "Speed of Trace " + trace.getBaseVehId() + " - rel. Error",
                    flags);
        }

        this.trace = traceWithoutSpeedError(trace, error);
        return this.trace;
    }

    public static Trace traceWithoutSpeedError(Trace trace, double error) {
        Trace corr = new Trace(trace.getVehicleId());
        for (int i = 0; i < trace.size(); ++i) {
            double speed = trace.getTracePointAtIndex(i).getSpeedSI() * error;
            corr.add(new TracePoint.Builder(trace.getTracePointAtIndex(i)).speedSI(speed).build());
        }
        return corr;
    }

    public Trace correctDrift() {
        Object[] obj = extractYawRate(this.trace);
        double[] headingYaw = (double[]) obj[0];
        double[] midYaw = (double[]) obj[1];
        long[] timesYaw = (long[]) obj[2];

        boolean[] trust = trustworthyDet(trace, 0.1, 1);
        boolean[] ignore = reverse(trust);
        TimeSectionsHelper helper = createTimeSections(ignore, timesYaw);
        double diff = integral(headingYaw, timesYaw, helper) - integral(midYaw, timesYaw, helper);
        double[] ones = new double[midYaw.length];
        for (int i = 0; i < ones.length; ++i) {
            ones[i] = 1;
        }
        double timediff = integral(ones, timesYaw, helper);
        // double timediff = timesYaw[timesYaw.length -1]-timesYaw[0];
        // -> this is wrong! because not all time steps are counted by the
        // integral! (and the integral is 2000 times bigger than normal)

        this.drift = diff / timediff;

        System.out.println("__TraceFilter__: The drift error is: " + this.drift);

        if (flags.contains(Output.SHOW_DRIFT) || flags.contains(Output.PRINT_DRIFT)) {
            // create data for ignore
            double[] ign = new double[ignore.length];
            for (int i = 0; i < ign.length; ++i) {
                if (ignore[i]) {
                    ign[i] = 0.1;
                } else {
                    ign[i] = 0;
                }
            }

            // create data for corrected
            double[] corrected = new double[midYaw.length];
            for (int i = 0; i < corrected.length; ++i) {
                corrected[i] = midYaw[i] + this.drift;
            }

            List<String> names = new ArrayList<String>();
            names.add("Yaw-rate from Sensor");
            names.add("Yaw-rate from GPS-heading");
            names.add("Yaw-rate corrected / Drift corrected");
            names.add("Ignored Points");

            List<double[]> data = new ArrayList<double[]>();
            data.add(midYaw);
            data.add(headingYaw);
            data.add(corrected);
            data.add(ign);

            showDataLine(data,null, names, "TracePoint", "rad/s", "Yaw-Rate of Trace " + trace.getBaseVehId() + " - Drift",
                    flags);
        }

        this.trace = traceWithoutDrift(this.trace, this.drift);
        return this.trace;
    }

    public static Trace traceWithoutDrift(Trace trace, double drift) {
        Trace corr = new Trace(trace.getVehicleId());
        for (int i = 0; i < trace.size(); ++i) {
            double yaw = trace.getTracePointAtIndex(i).getYawRate() + drift;
            corr.add(new TracePoint.Builder(trace.getTracePointAtIndex(i)).yawRate(yaw).build());
        }
        return corr;
    }

    private static double[] addRelativeError(double[] values, double error) {
        double[] val = new double[values.length];
        for (int i = 0; i < val.length; ++i) {
            val[i] = values[i] * error;
        }
        return val;
    }

    private static double integral(double[] values, long[] times) {
        double sum = 0;
        for (int i = 0; i < values.length - 1; ++i) {
            if (Double.isNaN(values[i]) || Double.isNaN(values[i + 1])) {
                System.out.println("__TraceFilter__: Something is going wrong. There should not be any NaN!");
            }
            sum += ((values[i] + values[i + 1])) * (double) (times[i + 1] - times[i]);
        }
        return sum;
    }
    
    private static double integral(double[] values, List<Long> times) {
        double sum = 0;
        for (int i = 0; i < values.length - 1; ++i) {
            if (Double.isNaN(values[i]) || Double.isNaN(values[i + 1])) {
                System.out.println("__TraceFilter__: Something is going wrong. There should not be any NaN!");
            }
            sum += ((values[i] + values[i + 1])) * (double) (times.get(i+1) - times.get(i));
        }
        return sum;
    }
    
    private static double integral(List<Double> values, List<Long> times) {
        double sum = 0;
        for (int i = 0; i < values.size() - 1; ++i) {
            if (Double.isNaN(values.get(i)) || Double.isNaN(values.get(i+1))) {
                System.out.println("__TraceFilter__: Something is going wrong. There should not be any NaN!");
            }
            sum += ((values.get(i) + values.get(i+1))) * (double) (times.get(i+1) - times.get(i));
        }
        return sum;
    }

    // function returns an value as integral which is 2000 times bigger as it
    // should be
    private static double integral(double[] values, long[] times, TimeSectionsHelper helper) {
        double sum = 0;
        for (int i = 0; i < values.length - 1; ++i) {
            if (helper.intersectSection(times[i], times[i+1])) {
                if (Double.isNaN(values[i]) || Double.isNaN(values[i + 1])) {
                    System.out.println("__TraceFilter__: Something is going wrong. There should not be any NaN!");
                }
                sum += ((values[i] + values[i + 1])) * (double) (times[i + 1] - times[i]);
            }
        }
        return sum;
    }

    @Deprecated
    public Trace correctTrustworthy() {
        for (int i = 1; i < trace.size(); ++i) {
            TracePoint tp = trace.getTracePointAtIndex(i);
            TracePoint last_tp = trace.getTracePointAtIndex(i - 1);
            // not trustworthy if speed = 0
            if (tp.getSpeed() == 0) {
                tp.noTrust();
            }
            // not trustworthy if gps jumps -> jumps should be filtered out
            // after this filters or use the filters here with a list of traces
            Coordinate det_coor = last_tp.getSensorVehicleModel().nextPos(tp.getTime());
            double dist = last_tp.getCoor().distance(det_coor);
            double error = tp.getCoor().distance(det_coor);
            double standardError = 1;
            double threshold = 0.1;
            if (error > 1 && (error - standardError) / dist > threshold) {
                tp.noTrust();
            }
        }
        return trace;
    }

    private static boolean[] trustworthyDet(Trace trace, double threshold, double standardError) {
        boolean[] trust = new boolean[trace.size()];
        for (int i = 0; i < trust.length; ++i) {
            trust[i] = true;
        }

        for (int i = 1; i < trace.size(); ++i) {
            TracePoint tp = trace.getTracePointAtIndex(i);
            TracePoint last_tp = trace.getTracePointAtIndex(i - 1);
            // not trustworthy if speed = 0
            if (tp.getSpeed() == 0) {
                trust[i] = false;
            }
            // not trustworthy if gps jumps -> jumps should be filtered out
            // after this filters or use the filters here with a list of traces
            Coordinate det_coor = last_tp.getSensorVehicleModel().nextPos(tp.getTime());
            double dist = last_tp.getCoor().distance(det_coor);
            double error = tp.getCoor().distance(det_coor);
            if (error > 1 && (error - standardError) / dist > threshold) {
                trust[i] = false;
            }
        }
        return trust;
    }

    private static boolean[] sizeDown(boolean[] arr) {
        boolean[] ret = new boolean[arr.length - 1];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = (arr[i] && arr[i + 1]);
        }
        return ret;
    }

    private static boolean[] trustworthySpikes(double[] values, double max, double tolerance, double min) {
        boolean[] trust = new boolean[values.length];
        for (int i = 0; i < trust.length; ++i) {
            trust[i] = true;
        }
        // prepare list for calculating median and throw all values out
        List<Double> listForMed = new ArrayList<Double>();
        for (int i = 0; i < trust.length; ++i) {
            double value = Math.abs(values[i]);
            if (value > max) {
                trust[i] = false;
            } else {
                if (value > min) {
                    listForMed.add(value);
                }
            }
        }
        // get median
        Collections.sort(listForMed);
        double med = 0;
        if (listForMed.size() % 2 == 0) {
            int indexLast = listForMed.size() / 2;
            int indexFirst = indexLast - 1;
            med = (listForMed.get(indexFirst) + listForMed.get(indexLast)) / 2.0;
        } else {
            int index = (listForMed.size() - 1) / 2;
            med = listForMed.get(index);
        }

        // throw out all values which are above median
        for (int i = 0; i < trust.length; ++i) {
            if (Math.abs(values[i]) > med * tolerance) {
                trust[i] = false;
            }
        }

        return trust;
    }

    private static Object[] extractYawRate(Trace trace) {
        double[] headingYaw = new double[trace.size() - 1];
        double[] midYaw = new double[trace.size() - 1];
        long[] times = new long[trace.size() - 1];

        for (int i = 0; i < trace.size() - 1; ++i) {
            TracePoint one = trace.getTracePointAtIndex(i);
            TracePoint two = trace.getTracePointAtIndex(i + 1);
            double timediff = (two.getTime() - one.getTime()) / 1000.0;
            headingYaw[i] = InterpolationTools.radShortestAngle(one.getHeading(), two.getHeading()) / timediff;
            midYaw[i] = InterpolationTools.radInterpolation(one.getYawRate(), two.getYawRate(), 0.5);
            times[i] = one.getTime() + (two.getTime() - one.getTime()) / 2;
        }

        return new Object[] { headingYaw, midYaw, times };
    }
    
    private static TimeSectionsHelper createTimeSections(boolean[] flags, long[] times){
        if(flags.length != times.length){
            flags = Arrays.copyOf(flags, times.length);
        }
        TimeSectionsHelper helper = new TimeSectionsHelper();
        boolean started = false;
        for(int i = 0; i < times.length; ++i){
            if(flags[i] && !started){
                started = true;
                helper.setStart(times[i]);
            }else if(!flags[i] && started){
                started = false;
                helper.setEnd(times[i-1]);
            }
        }
        if(started){
            helper.setEnd(times[times.length-1]);
        }
        helper.closeBuild();
        return helper;
    }

    public static void showDataLine(List<double[]> data, long[] times , List<String> names, String x_axis, String y_axis, String title,
            EnumSet<TraceFilter2.Output> flags) {
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
    
    public static void showDataLine(double[] date, long[] times ,String name, String x_axis, String y_axis, String title,
            EnumSet<TraceFilter2.Output> flags) {
        
        List<double[]> data = new ArrayList<double[]>();
        List<String> names = new ArrayList<String>();
        data.add(date);
        names.add(name);
        showDataLine(data,times,names,x_axis,y_axis,title,flags);
    }

    public enum Output {
        SHOW_SPEED_ERROR, PRINT_SPEED_ERROR,
        SHOW_HEADING_LAG, PRINT_HEADING_LAG,
        SHOW_COOR_LAG, PRINT_COOR_LAG,
        SHOW_DRIFT, PRINT_DRIFT,
        SHOW_COOR_OFFSET, PRINT_COOR_OFFSET,
        SHOW_HEADING_OFFSET, PRINT_HEADING_OFFSET;
        public static final EnumSet<Output> SHOW = EnumSet.of(SHOW_SPEED_ERROR, SHOW_HEADING_LAG, SHOW_COOR_LAG, SHOW_DRIFT, SHOW_COOR_OFFSET, SHOW_HEADING_OFFSET);
        public static final EnumSet<Output> PRINT = EnumSet.of(PRINT_SPEED_ERROR, PRINT_HEADING_LAG, PRINT_COOR_LAG, PRINT_DRIFT, PRINT_COOR_OFFSET, PRINT_HEADING_OFFSET);
    }

    public enum Integral {
        CROSSCORRELATION, MAX, MIN, MINMAX;
    }
    
    public class LagFilter{
        private Integral method;
        private long offsetSpeed;
        private long offsetYaw;
        
        public LagFilter(Integral method){
            this.method = method;
        }
        
        public void setMethod(Integral method){
            this.method = method;
        }
        
        private long correctCoorLag(){
            this.offsetSpeed = this.calculateOffset(true, -3000l, 1000l);
            return this.offsetSpeed;
        }
        
        private long correctHeadingLag(){
            this.offsetYaw = this.calculateOffset(false, -3000l, 1000l);
            return this.offsetYaw;
        }
        
        private long calculateOffset(boolean coor, long minOffset, long maxOffset){     

            PathAugmenter path = trace.getPathAugmenter();
            
            //get min and max timestamp (with PathAugmenter)
            path.setOffset(minOffset);
            long minTime = coor ? path.getSpeedMinTime() : path.getYawRateMinTime();
            long maxTime = coor ? path.getSpeedMaxTime() : path.getYawRateMaxTime();
            path.setOffset(maxOffset);
            minTime = Math.max(minTime, coor ? path.getSpeedMinTime() : path.getYawRateMinTime());
            maxTime = Math.min(maxTime, coor ? path.getSpeedMaxTime() : path.getYawRateMaxTime());
            path.setOffset(0);
            
            int min = 0;
            int max = trace.size()-1;
            int index = 0;
            while(index < trace.size() && trace.getTracePointAtIndex(index).getTime() < minTime){
                ++index;
            }
            min = index;
            index = trace.size()-1;
            while(index >= 0 && trace.getTracePointAtIndex(index).getTime() > maxTime){
                --index;
            }
            max = index+1; //max is exclusive
            
            double[] values = new double[max-min];
            long[] times = new long[max-min];
            double[] gpsValues = new double[max-min];
            
            for(int i = min; i < max; ++i){
                times[i-min] = trace.getTracePointAtIndex(i).getTime();
                values[i-min] = coor ? trace.getTracePointAtIndex(i).getSpeedSI() : trace.getTracePointAtIndex(i).getYawRate();
                gpsValues[i-min] = coor ? path.getSpeedByTime(times[i-min]) : path.getYawRateByTime(times[i-min]);
            }
            
            //ignore some TimeSections if you want
            TimeSectionsHelper helper = new TimeSectionsHelper();
            helper.closeBuild();
            
            //find offset
            long step = 10l;
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
                //if you use the method Integral.Max or Integral.MINMAX you have to search for the minimum NOT the maximum of the offsets
                offsets[i] = crossCorrelation(values,times,gpsValues,helper);
                
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
                    //create ignoredPoint array
                    double[] ign = new double[values.length];
                    helper.setOffset(maxPoint);
                    for(int i = 0; i < ign.length; ++i){
                        if(helper.onSection(times[i])){
                            ign[i] = 5;
                        }
                    }
                    data.add(ign);
                }

                showDataLine(data,times, names, "Tracepoint", "m/s", "Speed of Trace " + trace.getBaseVehId() + " - Lag", flags);
            }
            
            if(coor){
                show = flags.contains(Output.SHOW_COOR_OFFSET) || flags.contains(Output.PRINT_COOR_OFFSET);
            }else{
                show = flags.contains(Output.SHOW_HEADING_OFFSET) || flags.contains(Output.PRINT_HEADING_OFFSET);
            }
            
            if(show) {
                LineChart chart = new LineChart("Values of CrossCorrelation of Trace " + trace.getBaseVehId() + " - CoorLag");
                //edit offsets
                double[] offsetsEdited = new double[offsets.length];
                double minOffsetValue = offsets[0];
                for(int i = 0; i < offsets.length; ++i){
                    if(offsets[i] < minOffsetValue){
                        minOffsetValue = offsets[i];
                    }
                }
                for(int i = 0; i < offsets.length; ++i){
                    offsetsEdited[i] = offsets[i] - minOffsetValue;
                }
                String name = method.name();
                name = name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase(); 
                chart.addLinearData(offsetsEdited, minOffset, step, name);
                chart.initChart("Value of CrossCorrelation-Integral", "Offset-Value");
                chart.setRange(minOffset,maxOffset);
                if(flags.contains(Output.SHOW_COOR_OFFSET) || flags.contains(Output.SHOW_HEADING_OFFSET)){
                    chart.showChart();
                }
                if(flags.contains(Output.PRINT_COOR_OFFSET) || flags.contains(Output.PRINT_HEADING_OFFSET)){
                    chart.printChart();
                }
            }

            return maxPoint;
        }
               
        public double crossCorrelation(double[] values, long[] times, double[] gpsValues, TimeSectionsHelper helper){
            
            if(!helper.isStrict()){
                System.out.println("__TraceFilter__: Be careful! CrossCorrelation only works correctly"
                        + " with a TimeSectionsHelper in strict mode (there are no selections allowed with a double border)");
                //e.g.: 0 to 50 and 50 to 100 as sections are forbidden!
            }
            
            List<Double> gpsVal = new ArrayList<Double>(values.length + helper.size() *2);
            List<Long> tim = new ArrayList<Long>(values.length + helper.size() *2);
            List<Double> val = new ArrayList<Double>(values.length + helper.size() *2);
            
            //copy arrays
            for(int i = 0; i < times.length; ++i){
                gpsVal.add(gpsValues[i]);
                val.add(values[i]);
                tim.add(times[i]);
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
                    double value = InterpolationTools.weightedMean(new double[]{values[left],values[right]}, new double[]{1-share,share});
                    double gps = InterpolationTools.weightedMean(new double[]{gpsValues[left],gpsValues[right]}, new double[]{1-share,share});
                    tim.add(right+inserts, time);
                    val.add(right+inserts,value);
                    gpsVal.add(right+inserts, gps);
                    ++inserts;
                    if(time != (times[right])){
                        //we need two of them!
                        tim.add(right+inserts, time);
                        val.add(right+inserts,value);
                        gpsVal.add(right+inserts, gps);
                        ++inserts;
                    }
                }
            }
            
            //generate crossCorrelation
            
            double[] cross = new double[tim.size()];
            double[] ignoreValues = getIgnoredValues(val,gpsVal,method);
            double[] calculatedValue = getCalculatedValues(val,gpsVal,method);
      
            //the first and last one can be a border (without two equal timestamps)
            int index = 0;
            if(helper.inSection(tim.get(index)) || helper.beginOfSection(tim.get(index))){
                cross[index] = ignoreValues[index];
            }else{
                cross[index] = calculatedValue[index];
            }
            
            for(int i = 1; i < tim.size() -1; ++i){
                if(!tim.get(i).equals(tim.get(i+1))){
                    if(helper.inSection(tim.get(i))){
                        cross[i] = ignoreValues[i];
                    }else{
                        cross[i] = calculatedValue[i];
                    }
                }else{
                    //look what has to be val^2 and was has to be the normal correlation
                    if(helper.beginOfSection(tim.get(i))){
                        cross[i] = calculatedValue[i];
                        ++i;
                        cross[i] = ignoreValues[i];
                    }else{
                        cross[i] = ignoreValues[i];
                        ++i;
                        cross[i] = calculatedValue[i];
                    }
                    
                }
            }
            
            //the first and last one can be a border (without two equal timestamps)
            index = tim.size()-1;
            if(helper.inSection(tim.get(index)) || helper.endOfSection(tim.get(index))){
                cross[index] = ignoreValues[index];
            }else{
                cross[index] = calculatedValue[index];
            }

            //print it out
//            String string = "";
//            for(int  i = 0 ; i< cross.size(); ++i){
//                string += " ["+tim.get(i)+"] "+cross.get(i)+" ;";
//            }
//            System.out.println(string);
            
            return integral(cross, tim);
        }
        
        private double[] getIgnoredValues(List<Double> val, List<Double> gpsVal, Integral method){
            double[] ignoreValues = new double[val.size()];
            
            //IMPORTANT: The value of val*val is there, so it does not matter that much in an integral. Another Possibility is simply: 0
            //Other Alternatives: get some estimates, which point should be there (instead of the outlier) 
            //(if you smooth out the gpsValue enough, you do not need any ignored Points)
            if(method == Integral.CROSSCORRELATION){
                for(int i = 0; i < ignoreValues.length; ++i){
                    ignoreValues[i] = val.get(i)*val.get(i);
                }
            }else if(method == Integral.MIN){
                for(int i = 0; i < ignoreValues.length; ++i){
                    ignoreValues[i] = 0;
                }
            }else if(method == Integral.MAX){
                for(int i = 0; i < ignoreValues.length; ++i){
                    ignoreValues[i] = 0;
                }
            }else if(method == Integral.MINMAX){
                for(int i = 0; i < ignoreValues.length; ++i){
                    ignoreValues[i] = 0;
                }
            }
            
            return ignoreValues;
        }
        
        private double[] getCalculatedValues(List<Double> val, List<Double> gpsVal, Integral method){
            double[] calculatedValue = new double[val.size()];
            //IMPORTANT: The value val*gpsVal is there, because we use CrossCorrelation. 
            //We could use some other things, like the min of both functions
            if(method == Integral.CROSSCORRELATION){
                for(int i = 0; i < calculatedValue.length; ++i){
                    calculatedValue[i] = val.get(i) * gpsVal.get(i);
                }
            }else if(method == Integral.MIN){
                for(int i = 0; i < calculatedValue.length; ++i){
                    calculatedValue[i] = Math.min(val.get(i),gpsVal.get(i));
                }
            }else if(method == Integral.MAX){
                for(int i = 0; i < calculatedValue.length; ++i){
                    calculatedValue[i] = Math.max(val.get(i),gpsVal.get(i));
                }
            }else if(method == Integral.MINMAX){
                for(int i = 0; i < calculatedValue.length; ++i){
                    calculatedValue[i] = Math.max(val.get(i),gpsVal.get(i)) - Math.min(val.get(i),gpsVal.get(i));
                }
            }
            return calculatedValue;
        }
        
        private boolean toMaximise(Integral method){
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
    }
    
    public static class ErrorFilter{
        double speedError;
        double drift;
        
    }

    public static class TimeSectionsHelper extends ArrayList<TimeSection>{
        private static final long serialVersionUID = 1L;
//        private List<TimeSection> sections;
        private boolean hasStarted;
        private boolean strict;
        private boolean closeBuild;
        private long lastTime;
        private long offset;
        
        private final static String NO_CLOSED_BUILD = "__TimeSectionsHelper__: Building Phase is not completed. Call closeBuild() before you want to use this helper!";

        public TimeSectionsHelper() {
            super();
//            this.sections = new ArrayList<TimeSection>();
            this.lastTime = Long.MIN_VALUE;
            this.hasStarted = false;
            this.strict = true;
            this.offset = 0;
            this.closeBuild = false;
        }

        public boolean setStart(long start) {
            if(closeBuild){
                return false;
            }
            if (this.strict && start < this.lastTime) {
                return false;
            }
            if (this.hasStarted) {
                return false;
            }
            if(this.strict && start == this.lastTime){
                //combine the two sections
                this.hasStarted = true;
                if(!this.isEmpty()){
                    this.lastTime = this.remove(this.size()-1).startTime;
                }
                return true;
            }
            
            this.hasStarted = true;
            this.lastTime = start;
            return true;
        }

        public boolean setEnd(long end) {
            if(closeBuild){
                return false;
            }
            if (this.strict && end < this.lastTime) {
                return false;
            }
            if (!this.hasStarted) {
                return false;
            }
            if(this.strict && end == this.lastTime){
                //do not add this section
                this.hasStarted = false;
                if(this.isEmpty()){
                    this.lastTime = Long.MIN_VALUE;
                }else{
                    this.lastTime = this.get(this.size()-1).endTime;                    
                }
                return true;
            }
            this.hasStarted = false;
            this.add(new TimeSection(this.lastTime, end));
            this.lastTime = end;
            return true;
        }

        public boolean setSection(TimeSection section) {
            if(closeBuild){
                return false;
            }
            if (this.strict && section.startTime < this.lastTime) {
                return false;
            } else if (this.strict && section.endTime < section.startTime) {
                return false;
            } else {
                this.add(section);
                this.lastTime = section.endTime;
                return true;
            }
        }
        
        public void closeBuild(){
            this.closeBuild = true;
        }
        
        public boolean isStrict(){
            return this.strict;
        }
        
        public void setOffset(long offset){
            this.offset = offset;
        }

        public boolean inSection(long time) {
            if(this.strict && !closeBuild){
                System.out.println(NO_CLOSED_BUILD);
            }
            if (this.strict) {
                // TODO: do binary search here and return true or false (remind the offset)
            }
            for (int i = 0; i < this.size(); ++i) {
                if (this.get(i).inSection(time-this.offset)) {
                    return true;
                }
            }
            return false;
        }
        
        public boolean onSection(long time) {
            if(this.strict && !closeBuild){
                System.out.println(NO_CLOSED_BUILD);
            }
            if (this.strict) {
                // TODO: do binary search here and return true or false (remind the offset)
            }
            for (int i = 0; i < this.size(); ++i) {
                if (this.get(i).onSection(time-this.offset)) {
                    return true;
                }
            }
            return false;
        }
        
        public boolean intersectSection(long start, long end){
            if(this.strict && !closeBuild){
                System.out.println(NO_CLOSED_BUILD);
            }
            if (this.strict) {
                // TODO: do binary search here and return true or false (remind the offset)
            }
            for (int i = 0; i < this.size(); ++i) {
                if (this.get(i).intersectSection(start-this.offset,end-this.offset)) {
                    return true;
                }
            }
            return false;
        }
        
        public boolean beginOfSection(long time){
            if (this.strict) {
                // TODO: do binary search here and return true or false (remind the offset)
            }
            for (int i = 0; i < this.size(); ++i) {
                if (this.get(i).startTime + this.offset == time) {
                    return true;
                }
            }
            return false;
        }
        
        public boolean endOfSection(long time){
            if (this.strict) {
                // TODO: do binary search here and return true or false (remind the offset)
            }
            for (int i = 0; i < this.size(); ++i) {
                if (this.get(i).endTime + this.offset == time) {
                    return true;
                }
            }
            return false;
        }
        
        public List<Long> getTimeStamps(){
            if(!this.strict){
                System.out.println("__TimeSectionsHelper__: this time stamp list is not sorted! Please be aware of that!");
            }
            List<Long> times = new ArrayList<Long>(this.size() * 2);
            for(int i = 0; i < this.size(); ++i){
                times.add(this.get(i).startTime + this.offset);
                times.add(this.get(i).endTime + this.offset);
            }
            return times;
        }
        
        public void clear(){
            super.clear();
            this.hasStarted = false;
            this.closeBuild = false;
            this.lastTime = Long.MIN_VALUE;
            this.offset = 0l;
        }

    }

    private static class TimeSection {
        private long startTime;
        private long endTime;

        TimeSection(long startTime, long endTime) {
            if (startTime > endTime) {
                this.startTime = endTime;
                this.endTime = startTime;
            } else {
                this.startTime = startTime;
                this.endTime = endTime;
            }
        }

        public boolean inSection(long time) {
            return (this.startTime < time && time < this.endTime);
        }

        public boolean onSection(long time) {
            return (this.startTime <= time && time <= this.endTime);
        }
        
        public boolean atSection(long time){
            return (this.startTime == time || time == this.endTime);
        }
        
        public boolean intersectSection(long start, long end){
            if(this.onSection(start)|| this.onSection(end)){
                return true;
            }else if(start < this.startTime && this.endTime < end){
                return true;
            }else{
                return false;
            }
        }
        
        public boolean beforeSection(long time){
            return this.startTime > time;
        }
        
        public boolean afterSection(long time){
            return this.endTime < time;
        }
    }

}
