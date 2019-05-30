package com.dcaiti.traceloader.odometrie;

/** Class which represents a time section.
 * 
 * @see com.dcaiti.traceloader.odometrie.TimeSectionHelper TimeSectionHelper
 * 
 * @author nkl - Nicolas Klenert
 *
 */
public class TimeSection {
    private long startTime;
    private long endTime;
    private int id;

    TimeSection(long startTime, long endTime) {
        this(startTime,endTime,1);
    }
    
    TimeSection(long startTime, long endTime, int id) {
        if (startTime > endTime) {
            this.startTime = endTime;
            this.endTime = startTime;
        } else {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        this.id = id;
    }

    public boolean inSection(long time) {
        return (this.startTime < time && time < this.endTime);
    }

    public boolean onSection(long time) {
        return (this.startTime <= time && time <= this.endTime);
    }

    public boolean atSection(long time) {
        return (this.startTime == time || time == this.endTime);
    }

    public boolean intersectSection(long start, long end) {
        if (this.onSection(start) || this.onSection(end)) {
            return true;
        } else if (start < this.startTime && this.endTime < end) {
            return true;
        } else {
            return false;
        }
    }

    public boolean beforeSection(long time) {
        return this.startTime > time;
    }

    public boolean afterSection(long time) {
        return this.endTime < time;
    }
    
    public long getStartTime(){
        return this.startTime;
    }
    
    public long getEndTime(){
        return this.endTime;
    }
    
    public int getId(){
        return this.id;
    }

}
