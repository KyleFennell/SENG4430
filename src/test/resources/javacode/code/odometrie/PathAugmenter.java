package com.dcaiti.traceloader.odometrie;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;

/** Class to pretend the GPS-data to be continuous. 
 * 
 * This class should be used if you need GPS data between the real ones. (e.g. you want to know the coordinates between two "real" points)
 * 
 * @author nkl - Nicolas Klenert
 *
 */
public class PathAugmenter {

    //tokens are the originalData gotten form the first Trace which created this PathAugmenter
    private List<PathToken> pathTokens;
    private List<DoubleToken> speedTokens;
    private List<DoubleToken> headingTokens;
    private List<DoubleToken> yawTokens;
    
    //maps to use to get a performance boost
    private Map<Long,Coordinate> pathMap;
    private Map<Long, Double> speedMap;
    private Map<Long, Double> headingMap;
    private Map<Long, Double> yawMap;
    
    //offset is used for simple lag-corrections. This field is relatively special.
    //If you do not know what it does, do not change this!
    private long offset;
    private long closedOffset;
    private long openOffset;
    
    private static final Comparator<AbstractToken> COMP = new Comparator<AbstractToken>(){
        @Override
        public int compare(AbstractToken arg0, AbstractToken arg1) {
            return (int)(arg0.time - arg1.time);
        }
    };
    
    private PathAugmenter(){
        this.offset = 0;
        this.openOffset = 0;
        this.closedOffset = 0;
    }
    
    public PathAugmenter(List<Coordinate> originalCoor, List<Long> time){
        this.pathTokens = new ArrayList<PathToken>(originalCoor.size());
        for(int i = 0; i < originalCoor.size(); ++i){
            this.pathTokens.add(new PathToken(originalCoor.get(i),time.get(i)));
        }
        this.pathTokens.sort(COMP);
        this.offset = 0;
        this.openOffset = 0;
        this.closedOffset = 0;
    }
    
    /** function to activate the map function of PathAugmenter (all calculated values are saved)
     * can be called more than once (it only triggers the use of it, if maps are already used, this function does nothing)
     * 
     */
    public void useMaps(){
        if(this.pathMap == null)
            this.pathMap = new HashMap<Long, Coordinate>();
        if(this.speedMap == null)
            this.speedMap = new HashMap<Long, Double>();
        if(this.headingMap == null)
            this.headingMap = new HashMap<Long, Double>();
        if(this.yawMap == null)
            this.yawMap = new HashMap<Long, Double>();
    }
    
    public void clearMaps(){
        this.pathMap.clear();
        this.speedMap.clear();
        this.headingMap.clear();
        this.yawMap.clear();
    }
    
    public void setOffset(long offset){
        this.openOffset = offset;
        this.updateOffset();
    }
    
    public long getOffset(){
        return this.openOffset;
    }
    
    public void closeOffset(){
        this.closedOffset += this.openOffset;
        this.openOffset = 0l;
    }
    
    private void updateOffset(){
        this.offset = this.openOffset + this.closedOffset;
    }
    
    public long getCoorMinTime(){
        return this.pathTokens.get(0).time + this.offset;
    }
    
    public long getCoorMaxTime(){
        return this.pathTokens.get(this.pathTokens.size()-1).time + this.offset;
    }
    
    public long getSpeedMinTime(){
        if(this.speedTokens == null || this.speedTokens.size() == 0){
            this.initSpeedTokens();
        }
        return this.speedTokens.get(0).time + this.offset;
    }
    
    public long getSpeedMaxTime(){
        if(this.speedTokens == null || this.speedTokens.size() == 0){
            this.initSpeedTokens();
        }
        return this.speedTokens.get(this.speedTokens.size()-1).time + this.offset;
    }
    
    public long getHeadingMinTime(){
        if(this.headingTokens == null || this.headingTokens.size() == 0){
            this.initHeadingTokens();
        }
        return this.headingTokens.get(0).time + this.offset;
    }
    
    public long getHeadingMaxTime(){
        if(this.headingTokens == null || this.headingTokens.size() == 0){
            this.initHeadingTokens();
        }
        return this.headingTokens.get(this.headingTokens.size()-1).time + this.offset;
    }
    
    public long getYawRateMinTime(){
        if(this.yawTokens == null || this.yawTokens.size() == 0){
            this.initYawTokens();
        }
        return this.yawTokens.get(0).time + this.offset;
    }
    
    public long getYawRateMaxTime(){
        if(this.yawTokens == null || this.yawTokens.size() == 0){
            this.initYawTokens();
        }
        return this.yawTokens.get(this.yawTokens.size()-1).time + this.offset;
    }
    
    public Coordinate getCoorByTime(long time){
        //"add" offset to the time
        time -= this.offset;
        
        Coordinate coor = null;
        //search for it in the map
        if(this.pathMap != null){
            coor = this.pathMap.get(time);
        }
        if(coor != null){
            return coor;
        }
        
        //search for the neighbors of the timestamp
        int temp = firstBiggerOrEqualEncounter(time, this.pathTokens);
        
        //test for borders
        if(temp == 0){
            return this.pathTokens.get(temp).getCoor();
        }else if(temp == this.pathTokens.size()){
            return this.pathTokens.get(temp-1).getCoor();
        }
        
        PathToken right = this.pathTokens.get(temp);
        PathToken left = this.pathTokens.get(temp-1);
        
        //linear interpolation for now
        long timediff = right.time-left.time;
        double valueRight = (double)(time-left.time)/timediff;
        double valueLeft = 1-valueRight;
        double x = InterpolationTools.weightedMean(new double[]{left.coor.x,right.coor.x}, new double[]{valueLeft,valueRight});
        double y = InterpolationTools.weightedMean(new double[]{left.coor.y,right.coor.y}, new double[]{valueLeft,valueRight});
        coor = new Coordinate(x,y);
        
        if(this.pathMap != null){
            this.pathMap.put(time, coor);
        }
        return coor;
    }
    
    public double getSpeedByTime(long time){
        //"add" offset to the time
        time -= this.offset;
        
        Double speedD = null;
        //search for it in the map
        if(this.speedMap != null){
            speedD = this.speedMap.get(time);
        }
        if(speedD != null){
            return speedD;
        }
        
        //init speedTokens if necessary
        if(this.speedTokens == null || this.speedTokens.size() == 0){
            this.initSpeedTokens();
        }
        //search for the neighbors of the timestamp
        int temp = firstBiggerOrEqualEncounter(time,this.speedTokens);
        
        //test for borders
        if(temp == 0){
            return this.speedTokens.get(temp).getValue();
        }else if(temp == this.speedTokens.size()){
            return this.speedTokens.get(temp-1).getValue();
        }
        
        DoubleToken right = this.speedTokens.get(temp);
        DoubleToken left = this.speedTokens.get(temp-1);
        
        //linear interpolation for now
        long timediff = right.time-left.time;
        double valueRight = (double)(time-left.time)/timediff;
        double valueLeft = 1-valueRight;
        double speed = InterpolationTools.weightedMean(new double[]{left.getValue(),right.getValue()}, new double[]{valueLeft,valueRight});
        
        if(this.speedMap != null){
            this.speedMap.put(time, speed);
        }
        
        return speed;
    }
    
    public double getHeadingByTime(long time){
        //"add" offset to the time
        time -= this.offset;
        
        Double headingD = null;
        //search for it in the map
        if(this.headingMap != null){
            headingD = this.headingMap.get(time);
        }
        if(headingD != null){
            return headingD;
        }
        
        //init headingTokens if necessary
        if(this.headingTokens == null || this.headingTokens.size() == 0){
            this.initHeadingTokens();
        }
        //search for the neighbors of the timestamp
        int temp = firstBiggerOrEqualEncounter(time,this.headingTokens);
        
        //test for borders
        if(temp == 0){
            return this.headingTokens.get(temp).getValue();
        }else if(temp == this.headingTokens.size()){
            return this.headingTokens.get(temp-1).getValue();
        }
        
        DoubleToken right = this.headingTokens.get(temp);
        DoubleToken left = this.headingTokens.get(temp-1);
        
        //linear interpolation for now
        long timediff = right.time-left.time;
        double valueRight = (double)(time-left.time)/timediff;
        double valueLeft = 1-valueRight;
        double heading = InterpolationTools.weightedMeanOfRad(new double[]{left.getValue(),right.getValue()}, new double[]{valueLeft,valueRight})[0];
        
        if(this.headingMap != null){
            this.headingMap.put(time, heading);
        }
        
        return heading;
    }
    
    public long getMinGapToOriginalHeading(long time){
        //search for the neighbors of the timestamp
        int temp = firstBiggerOrEqualEncounter(time,this.headingTokens);
        
        //test for borders
        if(temp == 0){
            return Math.abs(this.headingTokens.get(temp).time - time);
        }else if(temp == this.headingTokens.size()){
            return Math.abs(this.headingTokens.get(temp-1).time - time);
        }
        
        //return min gap
        long timeRight = this.headingTokens.get(temp).time;
        long timeLeft = this.headingTokens.get(temp-1).time;
        
        return Math.min(Math.abs(time - timeRight), Math.abs(timeLeft - time));
    }
    
    public double getYawRateByTime(long time){
        //"add" offset to the time
        time -= this.offset;
        
        Double yawD = null;
        //search for it in the map
        if(this.yawMap != null){
            yawD = this.yawMap.get(time);
        }
        if(yawD != null){
            return yawD;
        }
        
        //init headingTokens if necessary
        if(this.yawTokens == null || this.yawTokens.size() == 0){
            this.initYawTokens();
        }
        //search for the neighbors of the timestamp
        int temp = firstBiggerOrEqualEncounter(time,this.yawTokens);
        
        //test for borders
        if(temp == 0){
            return this.yawTokens.get(temp).getValue();
        }else if(temp == this.yawTokens.size()){
            return this.yawTokens.get(temp-1).getValue();
        }
        
        DoubleToken right = this.yawTokens.get(temp);
        DoubleToken left = this.yawTokens.get(temp-1);
        
        //linear interpolation for now
        long timediff = right.time-left.time;
        double valueRight = (double)(time-left.time)/timediff;
        double valueLeft = 1-valueRight;
        double yaw = InterpolationTools.weightedMeanOfRad(new double[]{left.getValue(),right.getValue()}, new double[]{valueLeft,valueRight})[0];
        
        if(this.yawMap != null){
            this.yawMap.put(time, yaw);
        }
        
        return yaw;
    }
    
    /** function to set speedTokens for the augmenter. Have to be GPS-Data (e.g.: GPS_Speed)
     * 
     * @param speed has to be in m/s (in SI)
     * @param time
     */
    public void setSpeedTokens(List<Double> speed, List<Long> time){
        this.speedTokens = new ArrayList<DoubleToken>();
        for(int i = 0; i < speed.size(); ++i){
            this.speedTokens.add(new DoubleToken(speed.get(i),time.get(i)));
        }
    }
    
    private void initSpeedTokens(){
        //write the midSpeed from every Coordinate
        this.speedTokens = new ArrayList<DoubleToken>();
        for(int i = 0; i < this.pathTokens.size() -1; ++i){
            PathToken first = this.pathTokens.get(i);
            PathToken second = this.pathTokens.get(i+1);
            long timediff = (second.time-first.time)/2;
            long timestamp = first.time + timediff;
            double speed = second.coor.distance(first.coor)*500 / (double)timediff;
            this.speedTokens.add(new DoubleToken(speed,timestamp));
        }
    }
    
    public void setHeadingTokens(List<Double> heading, List<Long> time){
        this.headingTokens = new ArrayList<DoubleToken>();
        for(int i = 0; i < heading.size(); ++i){
            this.headingTokens.add(new DoubleToken(heading.get(i),time.get(i)));
        }
    }
    
    private void initHeadingTokens(){
        this.headingTokens = new ArrayList<DoubleToken>();
        for(int i = 0; i < this.pathTokens.size() -1; ++i){
            PathToken first = this.pathTokens.get(i);
            PathToken second = this.pathTokens.get(i+1);
            long timediff = (second.time-first.time)/2;
            long timestamp = first.time + timediff;
            double heading = Math.atan2(second.getCoor().y - first.getCoor().y, second.getCoor().x - first.getCoor().x);
            this.headingTokens.add(new DoubleToken(heading,timestamp));
        }
    }
    
    public void setYawTokens(List<Double> yaw, List<Long> time){
        this.yawTokens = new ArrayList<DoubleToken>();
        for(int i = 0; i < yaw.size(); ++i){
            this.yawTokens.add(new DoubleToken(yaw.get(i), time.get(i)));
        }
    }
   
    private void initYawTokens(){
        //needs headingTokens to calculate this
        if(this.headingTokens == null || this.headingTokens.size() == 0){
            this.initHeadingTokens();
        }
        
        this.yawTokens = new ArrayList<DoubleToken>();
        for(int i = 0; i < this.headingTokens.size() -1; ++i){
            DoubleToken first = this.headingTokens.get(i);
            DoubleToken second = this.headingTokens.get(i+1);
            long timediff = (second.time-first.time);
            long timestamp = first.time + timediff/2;
            double yaw = InterpolationTools.radShortestAngle(first.getValue(), second.getValue())*1000 / (double) timediff;
            this.yawTokens.add(new DoubleToken(yaw,timestamp));
        }
    }
    
    /**
     * 
     * @param time
     * @param tokens
     * @return index of the first token which is bigger or equal to the timestamp. If no token is bigger, tokens.size() is returned
     */
    protected static int firstBiggerOrEqualEncounter(long time, List<? extends AbstractToken> tokens){
        int min = 0;
        int max = tokens.size()-1;
        //special cases
        if(tokens.get(min).time >= time){
            return min;
        }else if(tokens.get(max).time < time){
            return tokens.size();
        }
        //binary search
        while((max-min)/2 != 0){
            int mid = (min+max)/2;
            if(tokens.get(mid).time > time){
                max = mid;
            }else if(tokens.get(mid).time == time){
                return mid;
            }else{
                min = mid;
            }
        }
        return max;
    }
    
    /** function which is called by Trace.redoWithPathAugmenter
     * 
     * used to merge the offset with the tokens (what are expected to be the real data) -> maps can not be used any more!
     * 
     * 
     * Deprecated: use closeOffset() instead -> it does not lose any information about the original tokens and it can use his maps!
     */
    @Deprecated
    public PathAugmenter insertOffsetInData(){
          List<PathToken> path = new ArrayList<PathToken>(this.pathTokens.size());
          for(int i = 0; i < this.pathTokens.size(); ++i){
              path.add((PathToken)this.pathTokens.get(i).insertOffsetInToken(this.offset));
          }
          
          PathAugmenter augm = new PathAugmenter();
          augm.pathTokens = path;
          
          //use maps like before
          if(this.pathMap != null)
              augm.pathMap = new HashMap<Long, Coordinate>();
          if(this.speedMap != null)
              augm.speedMap = new HashMap<Long, Double>();
          if(this.headingMap != null)
              augm.headingMap = new HashMap<Long, Double>();
          if(this.yawMap != null)
              augm.yawMap = new HashMap<Long, Double>();
          
          //update all data
          if(this.speedTokens != null && this.speedTokens.size() != 0){
              List<DoubleToken> speed = new ArrayList<DoubleToken>(this.speedTokens.size());
              for(int i = 0; i < this.speedTokens.size(); ++i){
                  speed.add((DoubleToken)this.speedTokens.get(i).insertOffsetInToken(this.offset));
              }
              augm.speedTokens = speed;
          }
          if(this.headingTokens != null && this.headingTokens.size() != 0){
              List<DoubleToken> heading = new ArrayList<DoubleToken>(this.headingTokens.size());
              for(int i = 0; i < this.headingTokens.size(); ++i){
                  heading.add((DoubleToken)this.headingTokens.get(i).insertOffsetInToken(this.offset));
              }
              augm.headingTokens = heading;
          }
          if(this.yawTokens != null && this.yawTokens.size() != 0){
              List<DoubleToken> yaw = new ArrayList<DoubleToken>(this.yawTokens.size());
              for(int i = 0; i < this.yawTokens.size(); ++i){
                  yaw.add((DoubleToken)this.yawTokens.get(i).insertOffsetInToken(this.offset));
              }
              augm.yawTokens = yaw;
          }
          return augm;
    }
    
    private static long[] getListOfTime(List<? extends AbstractToken> tokens, long offset){
        long[] times = new long[tokens.size()];
        for(int i = 0; i < tokens.size(); ++i){
            times[i] = tokens.get(i).time + offset;
        }
        return times;
    }
    
    public long[] getCoorTimes(){
        return getListOfTime(this.pathTokens, this.offset);
    }
    
    public long[] getSpeedTimes(){
        return getListOfTime(this.speedTokens, this.offset);
    }
    
    public long[] getHeadingTimes(){
        return getListOfTime(this.headingTokens, this.offset);
    }
    
    public long[] getYawRateTimes(){
        return getListOfTime(this.yawTokens, this.offset);
    }
    
    public long[] getCoorTimesOriginal(){
        return getListOfTime(this.pathTokens, 0);
    }
    
    public long[] getSpeedTimesOriginal(){
        return getListOfTime(this.speedTokens, 0);
    }
    
    public long[] getHeadingTimesOriginal(){
        return getListOfTime(this.headingTokens, 0);
    }
    
    public long[] getYawRateTimesOriginal(){
        return getListOfTime(this.yawTokens, 0);
    }
    
    public PathAugmenter clone(){
        //tokens should not be modified in any way -> we can just copy the reference
        PathAugmenter path = new PathAugmenter();
        path.closedOffset = this.closedOffset;
        path.openOffset = this.openOffset;
        path.offset = this.offset;
        path.pathTokens = this.pathTokens;
        path.pathMap = this.pathMap;
        path.speedTokens = this.speedTokens;
        path.speedMap = this.speedMap;
        path.headingTokens = this.headingTokens;
        path.headingMap = this.headingMap;
        path.yawMap = this.yawMap;
        path.yawTokens = this.yawTokens;
        return path;
    }
    
    private abstract class AbstractToken{
        protected long time;
        
        public AbstractToken(long time){
            this.time = time;
        }
        
        protected abstract AbstractToken clone();
        
        protected AbstractToken insertOffsetInToken(long offset){
            AbstractToken token = this.clone();
            token.time += offset;
            return token;
        }
    }
    
    private class PathToken extends AbstractToken{
        protected Coordinate coor;
        
        public PathToken(Coordinate coor, long time){
            super(time);
            this.coor = new Coordinate(coor.x,coor.y);
        }
        
        public Coordinate getCoor(){
            return this.coor;
        }
        
        protected PathToken clone(){
            return new PathToken(this.coor, this.time);
        }
    }
    
    private class DoubleToken extends AbstractToken{
        protected double value;
        
        public DoubleToken(double speed, long time){
            super(time);
            this.value = speed;
        }
        
        public double getValue(){
            return this.value;
        }
        
        protected DoubleToken clone(){
            return new DoubleToken(this.value, this.time);
        }
    }
    
}
