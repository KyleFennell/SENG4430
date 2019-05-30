package com.dcaiti.traceloader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import com.dcaiti.traceloader.odometrie.InterpolationTools;
import com.dcaiti.traceloader.odometrie.PathAugmenter;
import com.dcaiti.utilities.GeoPosition;
import com.dcaiti.utilities.Util;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * This class provides data structures and methods for one trace. Each trace
 * consists of all its tracePoints and its surrounding box in UTM representation.
 */
public class Trace implements GeoPosition, Comparable<Trace>, Iterable<TracePoint>, Cloneable {

    /**
     * list of all tracePoints
     */
    public ArrayList<TracePoint> tracePts;

    /**
     * name of trace
     */
    private String vehicleId;
    
    /**
     * PathAugmenter estimate position of the Vehicle
     */
    private PathAugmenter augmenter;
    
    /**
     * if the data are original or if the trace was corrected with redoWithPathAugmenter -> then the gps-lag and the 0.5second lag of the headings are not present anymore!
     */
    private boolean corrected = false;
    
    /**
     * Surrounding box in utm coordinates. 
     */
    public BoundingBox boundingBox;

    /**
     * specifies if the trace contains these sensor values
     */
    public boolean containsALDW = false;
    public boolean containsDISTRONIC = false;
    public boolean containsNullSpeed = false;
    public boolean containsWgsHead = true;
    public boolean containsNullGPSSpeed = false;
    public boolean is50Hz = false;

    LineString aldwLeft;
    LineString aldwRight;

    /**
     * Constructor
     */
    public Trace() {
        tracePts = new ArrayList<TracePoint>();
    }

    public Trace(String vehicleId) {
        this.vehicleId = vehicleId;
        tracePts = new ArrayList<TracePoint>();
    }
    
    /*
     * is used when loading traces from Accumulo
     */
    public Trace(String vehicleId, Map<Long, Map<String, String>> result) {
        
        this.vehicleId = vehicleId;
        tracePts = new ArrayList<TracePoint>();
        
        boundingBox = new BoundingBox();

//        System.out.println("vehicleId: "+vehicleId);
        for (long timestamp : result.keySet()) {
            
            TracePoint tp = new TracePoint(timestamp, result.get(timestamp));
            tp.setParentTrace(this);
            
            if (Double.isNaN(tp.getWgsHead())){ 
                containsWgsHead = false;
            }
            
            if (tp.getHeading() == -10.0) {
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" +tp.getParentTrace().getVehicleId());
            }
            if (tp.containsALDW) 
                this.containsALDW = true;

            if (tp.containsDISTRONIC)
                this.containsDISTRONIC = true;
            
            if (tp.containsNullSpeed)
                this.containsNullSpeed = true;
            
            if(tp.containsNullGPSSpeed)
                this.containsNullGPSSpeed = true;

//            System.out.println(tp.toString());
            this.add(tp);
            
            //for determination of boundingBox
            boundingBox.expandBoundingBox(tp.getCoor());
        }

       //only if no GPS heading information available
        // after correcting the longtudinal error the first two trace points
        // do not have a heading value
        if (!containsWgsHead) {
//            System.out.println("Check Heading for " +this.getVehicleId() +"  " +Util.getTime(this.tracePts.get(0).time));
            checkHeading();
        }
        
//        System.out.println("Trace "+vehicleId +" ALDW " +containsALDW +" DISTRONIC "+containsDISTRONIC);
        
    }
    
    public Map<Long,Map<String,String>> buildMap(){
        Map<Long,Map<String,String>> map = new TreeMap<Long,Map<String,String>>();
        for(TracePoint tp : this.tracePts){
            map.put(tp.getTime(), tp.buildMap());
        }
        return map;
    }
    
    public void add(TracePoint tp) {
        tracePts.add(tp);
    }

    public void addAll(List<TracePoint> newTps) {
        tracePts.addAll(newTps);
    }

    public int size(){
        return tracePts.size();
    }
    
    //if any point is corrected, the whole trace becomes corrected
    public boolean isCorrected(){
        
        for (TracePoint tp : this.tracePts){
            if (tp.corrected) return true;
        }
        
        return false;
    }


    public TracePoint getNext(TracePoint tracePoint) {
        
        for (int i = 0; i < tracePts.size()-1; i++) {
            TracePoint tp = tracePts.get(i); 
            if (tp.equals(tracePoint))
                return tracePts.get(i+1);            
        }
        return null;
    }
    
    public LineString getAldwLeft() {
        if (aldwLeft == null) setALDWLines();
        return aldwLeft;
    }

    public LineString getAldwRight() {
        if (aldwRight == null) setALDWLines();
        return aldwRight;
    }


    public void setCorrectedALDWLines() {

        ArrayList<Coordinate> leftList = new ArrayList<Coordinate>();
        ArrayList<Coordinate> rightList = new ArrayList<Coordinate>();

        // debugging
        ArrayList<Double> correctedLaneWidthList = new ArrayList<Double>();

        for (TracePoint tp : tracePts) {
            double laneWidth = tp.getAldwLaneDistLeft() + tp.getAldwLaneDistRight();
            if ((laneWidth > 3.5) && (laneWidth < 4.1)) {
                leftList.add(tp.getAldwLaneDistLeftAsUTM());
                rightList.add(tp.getAldwLaneDistRightAsUTM());
                correctedLaneWidthList.add(laneWidth);
            }
        }

        if (leftList.size() < 2) {
            aldwLeft = getLineString(new ArrayList<Coordinate>());
        } else {
            aldwLeft = getLineString(leftList);
        }
        if (rightList.size() < 2) {
            aldwRight = getLineString(new ArrayList<Coordinate>());
        } else {
            aldwRight = getLineString(rightList);
        }

    }
    
    public void setALDWLines() {

        Coordinate[] lefts = new Coordinate[tracePts.size()];
        Coordinate[] rights = new Coordinate[tracePts.size()];

        int j = 0;
        for (TracePoint tp : tracePts) {
            lefts[j] = tp.getAldwLaneDistLeftAsUTM();
            rights[j] = tp.getAldwLaneDistRightAsUTM();
            j++;
        }

        aldwLeft = new GeometryFactory().createLineString(lefts);
        aldwRight = new GeometryFactory().createLineString(rights);

    }


    public LineString getLineString() {
        
        return getLineString(this.getCoordinates());
    }
    
    LineString getLineString(ArrayList<Coordinate> coorList) {

        Coordinate[] coors = new Coordinate[coorList.size()];
        for (int i = 0; i < coorList.size(); i++) {
            coors[i] = coorList.get(i);
        }

        return new GeometryFactory().createLineString(coors);
    }

    public ArrayList<Coordinate> getCoordinates() {
        ArrayList<Coordinate> coors = new ArrayList<Coordinate>();
        for (TracePoint tp : tracePts) {
            coors.add(tp.getCoor());
        }
        return coors;

    }

    public String getVehicleId() {
        return vehicleId;
    }
    
    public String getBaseVehId() {
        
        String[] idParts = vehicleId.split("-");
        if (idParts.length != 2){
            return vehicleId;
        }
        return idParts[0];
        
    }

    public TracePoint getTracePointAtIndex(int index){
        return this.tracePts.get(index);
    }
    
    /** function to return Tracepoint with given time
     * 
     * @param time
     * @return the first TracePoint which has a bigger or equal time stamp than the param time.
     * if the time is outside the whole time sectino of the trace, null is returned
     */
    public TracePoint getTracePointAtTime(long time){
        int index = getIndexAtTime(time);
        if(index == -1){
            return null;
        }
        return this.tracePts.get(index);
    }
    
    /** function to return the next index with a timesstamp bigger than the given time
     * 
     * @param time
     * @return -1 if time is outside the time window of the trace
     */
    public int getIndexAtTime(long time){
        int min = 0;
        int max = this.tracePts.size()-1;
        //special cases
        if(this.tracePts.get(min).getTime() > time){
            return -1;
        }else if(this.tracePts.get(max).getTime() < time){
            return -1;
        }else if(this.tracePts.get(min).getTime() == time){
            return min;
        }else if(this.tracePts.get(max).getTime() == time){
            return max;
        }
        //binary search
        while((max-min)/2 != 0){
            int mid = (min+max)/2;
            if(this.tracePts.get(mid).getTime() > time){
                max = mid;
            }else if(this.tracePts.get(mid).getTime() == time){
                return mid;
            }else{
                min = mid;
            }
        }
        return max;
    }
    
    public ArrayList<TracePoint> getTracePts() {
        return tracePts;
    }

    public String toString(){
        String str = "vehId: " + vehicleId + " with " +tracePts.size() +" points";
                
        str += " initialHeading: " + tracePts.get(0).getHeading();
        
        return str;        
    }
    
    public double lat(){
        return tracePts.get(0).lat();
    }

    public double lng(){
        return tracePts.get(0).lng();
    }

    /**
     * check heading and calculate missing values
     */ 
    public void checkHeading() {

        double heading = Double.NaN;
        TracePoint prev = tracePts.get(0);
        for (int i = 1; i < tracePts.size(); i++) {
            TracePoint tp = tracePts.get(i);
            
            //check
            if (Double.isNaN(prev.getHeading())) {
                Coordinate from = prev.getCoor();
                Coordinate to = tp.getCoor();
                heading = Util.clampPi(Util.getHeading(from, to));
//                prev.heading = heading;
                tracePts.set(i-1, new TracePoint.Builder(prev).heading(heading).build());
            }
            prev = tp;
        }
        
//        tracePts.get(tracePts.size()-1).heading = heading;
        tracePts.set(tracePts.size()-1, new TracePoint.Builder(tracePts.get(tracePts.size()-1)).heading(heading).build());
        
        
    }

    

    /**
     * 
     * @param biggerThan Threshold for heading change in rad
     * @return
     */
    public boolean containsUnusualHeadingChanges(double biggerThan){
            boolean result = false;
            if(this.tracePts.size() <= 1){return false;}
            TracePoint prev = this.getTracePointAtIndex(0);
            for(int i = 1; i < this.tracePts.size(); i++) {
                TracePoint curr = this.tracePts.get(i);
                double diff = Util.headingDiff(curr.getHeading(), prev.getHeading());
                if(diff > biggerThan){
//                    System.out.println(prev.heading +" ---> " +curr.heading +" at " +i);
                    System.out.println("[containsUnusualHeadingChanges] " +diff +" at trace point " +i);
                    return true;
                }
                //update prev
                prev = curr;
            }
                    
            return result;
    }

    @Override
    public int compareTo(Trace trace) {
//        return this.vehicleId.compareTo(trace.vehicleId);
        //trace with most tracePoints first
        return Integer.compare(trace.tracePts.size(), this.tracePts.size());
    }
    
    public void setTracePts(ArrayList<TracePoint> tracePts) {
        this.tracePts = tracePts;
    }

    @Override
    public Iterator<TracePoint> iterator() {
        return this.tracePts.iterator();
    }
    
    public ListIterator<TracePoint> listIterator(){
        return this.tracePts.listIterator();
    }
    
    public ListIterator<TracePoint> listIterator(int index){
        return this.tracePts.listIterator(index);
    }
    
    public double lengthFromCoor(){
        double sum = 0;
        for(int i = 1; i < tracePts.size(); ++i){
            sum += (tracePts.get(i).getCoor().distance(tracePts.get(i-1).getCoor()));
        }
        return sum;
    }
    
    public double lengthFromCoor(int start, int end){
        double sum = 0;
        start = Math.max(start, 0);
        end = Math.min(end, this.tracePts.size());
        for(int i = start+1; i < end; ++i){
            sum += (tracePts.get(i).getCoor().distance(tracePts.get(i-1).getCoor()));
        }
        return sum;
    }
    
    public double lengthFromSpeed(){
        double sum = 0;
        for(int i = 1; i < tracePts.size(); ++i){
            TracePoint tp1 = tracePts.get(i-1);
            TracePoint tp2 = tracePts.get(i);
            double timediff = tp2.getTimeSI() - tp1.getTimeSI();
            double midSpeed = (tp2.getSpeedSI() + tp1.getSpeedSI()) / 2.0;
            sum += timediff*midSpeed;
        }
        return sum;
    }
    
    public double lengthFromSpeed(int start, int end){
        double sum = 0;
        start = Math.max(start, 0);
        end = Math.min(end, this.tracePts.size());
        for(int i = start +1; i < end; ++i){
            TracePoint tp1 = tracePts.get(i-1);
            TracePoint tp2 = tracePts.get(i);
            double timediff = tp2.getTimeSI() - tp1.getTimeSI();
            double midSpeed = (tp2.getSpeedSI() + tp1.getSpeedSI()) / 2.0;
            sum += timediff*midSpeed;
        }
        return sum;
    }
    
    
    /** interpolating GPS Coordinates with PathAugmenter.
     * Use PathAugmenter every time when you want a estimate of Coordinates for a specific time.
     * 
     * The best thing would be, that this method will be unnecessary, because every method uses PathAugmenter!
     * 
     * The old version of linearInterpolation is at Revision 853
     * 
     * @return
     */
    public boolean interpolateGPS(){
        if(this.tracePts.size() < 2){
            return false;
        }
        //test if it has to be interpolated -> this test is mundane! 
        if(!this.tracePts.get(0).getCoor().equals(tracePts.get(1).getCoor())){
            return false;
        }
        
        if(this.augmenter == null){
            this.initPathAugmenter();
        }
        
        //interpolating
        Coordinate coor = this.tracePts.get(0).getCoor();
        for(int i = 1; i < this.tracePts.size(); ++i){
            TracePoint tp = this.tracePts.get(i);
            if(coor.equals(tp.getCoor())){
                //change coor
                long time = tp.getTime();
                this.tracePts.set(i, new TracePoint.Builder(tp).coordinate(this.augmenter.getCoorByTime(time)).build());
            }else{                
                coor = tp.getCoor();
            }
        }
         this.is50Hz = true;
        return true;
    }
    
    public PathAugmenter getPathAugmenter(){
        if(this.augmenter == null){
            this.initPathAugmenter();
        }
        return this.augmenter;
    }
    
    private void initPathAugmenter(){
        if(this.corrected){
            List<Coordinate> coors = new ArrayList<Coordinate>(this.tracePts.size());
            List<Long> times = new ArrayList<Long>(this.tracePts.size());
            List<Double> headings = new ArrayList<Double>(this.tracePts.size());
            for(int i = 0; i < this.tracePts.size(); ++i){
                coors.add(this.tracePts.get(i).getCoor());
                times.add(this.tracePts.get(i).getTime());
                headings.add(this.tracePts.get(i).getHeading());
            }
            this.augmenter = new PathAugmenter(coors,times);
            this.augmenter.setHeadingTokens(headings, times);
        }else{
            this.initPathAugmenterOriginal();
        }
    }
    
    /**
     *  init PathAugmenter with respect to to original data -> in the original data there was a 0.5 seconds "lag" of gps-heading
     */
    private void initPathAugmenterOriginal(){
        //create coor-list and time-list for PathAugmenter
        List<Coordinate> coors = new ArrayList<Coordinate>(this.tracePts.size());
        List<Long> timesCoor = new ArrayList<Long>(this.tracePts.size());
        List<Double> headings = new ArrayList<Double>(this.tracePts.size());
        List<Long> timesHeading = new ArrayList<Long>(this.tracePts.size());
        Coordinate coor = this.tracePts.get(0).getCoor();
        coors.add(coor);
        timesCoor.add(this.tracePts.get(0).getTime());
        double heading = this.tracePts.get(0).getHeading();
        headings.add(heading);
        long timediff = this.tracePts.get(1).getTime() -  this.tracePts.get(0).getTime();
        long time = this.tracePts.get(0).getTime();
        long lastTime = time;
        timesHeading.add(time);//-timediff/2); 
        for(int i = 1; i < this.tracePts.size(); ++i){
            //only add tracePts with different coors
            Coordinate now = this.tracePts.get(i).getCoor();
            if(!coor.equals(now)){
                coors.add(now);
                timesCoor.add(this.tracePts.get(i).getTime());
                coor = now;
                //heading is average from the last to this points -> set time as midTime
                timediff = this.tracePts.get(i).getTime() - lastTime;
                timesHeading.add(lastTime+timediff/2);
                headings.add(this.tracePts.get(i).getHeading());
                lastTime = this.tracePts.get(i).getTime();
            }
        }
        this.augmenter = new PathAugmenter(coors,timesCoor);
        this.augmenter.setHeadingTokens(headings, timesHeading);                //timesHeading instead of timesCoor change the offset of the heading (-500l)
    }
    
    /** function is used to rebuild the Trace with data from the augmenter (TraceFilter.correctGPSLag())
     * 
     * @return corrected Trace which is consistent with the augmenter
     */
    public Trace redoWithAugmenter(){
        Trace trace = new Trace(this.getVehicleId());
        this.augmenter.closeOffset();
        trace.augmenter = this.augmenter;
        for(int i = 0; i < this.tracePts.size(); ++i){
            TracePoint tp = this.tracePts.get(i);
            long time = tp.getTime();
            //only CoorMinTime and CoorMaxTime because heading will just be copied of the border-heading -> this is acceptable (at least for now)
            if(this.augmenter.getCoorMinTime() <= time && time <= this.augmenter.getCoorMaxTime()){
                Coordinate coor = this.augmenter.getCoorByTime(time);
                double heading = this.augmenter.getHeadingByTime(time);
                trace.add(new TracePoint.Builder(tp).coordinate(coor).heading(heading).build()); 
            }
        }
        trace.corrected = true;
        return trace;
    }
    
    /** function is used to rebuild the Trace with data shifted. It also uses the PathAugmenter!
     * (the main reason to use also the pathAugmenter: the heading data has also static lag, and the PathAugmenter correct this (by initPathAugmenter))
     * 
     * @return
     */
    public Trace redoDataWithOffset(long speedOffset, long yawOffset){
        Trace trace = new Trace(this.getVehicleId());
        this.augmenter.closeOffset();
        trace.augmenter = this.augmenter;
        for(int i = 0; i < this.tracePts.size(); ++i){
            TracePoint tp = this.tracePts.get(i);
            long time = tp.getTime();
            double speed = this.getSpeedSIByTime(time-speedOffset);
            double yaw = this.getYawRateByTime(time-yawOffset);
            if(!Double.isNaN(speed) && !Double.isNaN(yaw)){
                trace.add(new TracePoint.Builder(tp)
                        .coordinate(this.augmenter.getCoorByTime(time))
                        .heading(this.augmenter.getHeadingByTime(time))
                        .speedSI(speed)
                        .yawRate(yaw)
                        .build());
            }
        }
        trace.corrected = true;
        return trace;
    }
    
    public double getSpeedSIByTime(long time){
        int rightIndex = this.getIndexAtTime(time);
        if(rightIndex == -1){
            return Double.NaN;
        }
        if(this.tracePts.get(rightIndex).getTime() == time){
            return this.tracePts.get(rightIndex).getSpeedSI();
        }
        
        TracePoint right = this.tracePts.get(rightIndex);
        TracePoint left = this.tracePts.get(rightIndex-1);
        //linear interpolation for now
        long timediff = right.getTime()-left.getTime();
        double valueRight = (double)(time -left.getTime())/timediff;
        double valueLeft = 1-valueRight;
        double speed = InterpolationTools.weightedMean(new double[]{left.getSpeedSI(),right.getSpeedSI()}, new double[]{valueLeft,valueRight});
        return speed;
    }
    
    public double getYawRateByTime(long time){
        int rightIndex = this.getIndexAtTime(time);
        if(rightIndex == -1){
            return Double.NaN;
        }
        if(this.tracePts.get(rightIndex).getTime() == time){
            return this.tracePts.get(rightIndex).getYawRate();
        }
        
        TracePoint right = this.tracePts.get(rightIndex);
        TracePoint left = this.tracePts.get(rightIndex-1);
        //linear interpolation for now
        long timediff = right.getTime()-left.getTime();
        double valueRight = (double)(time -left.getTime())/timediff;
        double valueLeft = 1-valueRight;
        double speed = InterpolationTools.weightedMeanOfRad(new double[]{left.getYawRate(),right.getYawRate()}, new double[]{valueLeft,valueRight})[0];
        return speed;
    }
    
    /** Method to cut the Trace.
     * 
     * @param start included
     * @param end excluded
     * @return
     */
    public Trace subTrace(int start, int end){
        Trace trace = new Trace(this.vehicleId);
        end = Math.min(end, this.size());
        start = Math.max(0, start);
        
        for(int i = start; i < end; ++i){
            trace.add(this.tracePts.get(i));
        }
        
        trace.augmenter = this.augmenter;
        
        //TODO: set the Variables boundingBox etc! -> do it in the add functions!?
        
        return trace;
    }
    
    /** Method to cut the Trace with two TimeStamps
     * 
     * @param startTime
     * @param endTime
     * @return
     */
    public Trace subTrace(long startTime, long endTime){
        return this.subTrace(this.getIndexAtTime(startTime), this.getIndexAtTime(endTime));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tracePts == null) ? 0 : tracePts.hashCode());
        result = prime * result + ((vehicleId == null) ? 0 : vehicleId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Trace other = (Trace) obj;
        if (tracePts == null) {
            if (other.tracePts != null)
                return false;
        } else if (!tracePts.equals(other.tracePts))
            return false;
        if (vehicleId == null) {
            if (other.vehicleId != null)
                return false;
        } else if (!vehicleId.equals(other.vehicleId))
            return false;
        return true;
    }
    
    public void reverse(){
        Collections.reverse(this.tracePts);
    }

    /** method to clone the Trace. Used to ensure, that some Trace gotten from somewhere will not be changed anymore!
     * 
     */
    public Trace clone(){
        Trace trace = new Trace(this.getVehicleId());
        for(int i = 0; i < this.tracePts.size(); ++i){
            trace.add(this.tracePts.get(i).clone());
        }
        if(this.augmenter == null){
            trace.augmenter = null;
        }else{
            trace.augmenter = this.augmenter.clone();       //we also clone the augmenter!             
        }
        //-> this way the filter does not change the offset of the original trace
        return trace;
    }

}
