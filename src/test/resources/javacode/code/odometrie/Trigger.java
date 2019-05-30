package com.dcaiti.traceloader.odometrie;

import java.util.EnumSet;

import com.dcaiti.traceloader.Trace;
import com.dcaiti.traceloader.TracePoint;

/** Class which represents some GPS-Error on a Trace. What kind of GPS-Error is saved by the {@link com.dcaiti.traceloader.odometrie.Trigger.TriggerType TriggerType} 
 * 
 * @see com.dcaiti.traceloader.Trace Trace
 * @see com.dcaiti.traceloader.odometrie.TriggerFinder TriggerFinder
 * 
 * @author nkl
 *
 */
public class Trigger implements Cloneable{

    private TracePoint start;
    private TracePoint end;
    private TriggerType type;
    public int start_index;
    public int end_index;
    int length;
    
    @Deprecated
    private Trigger(TracePoint start, TracePoint end, TriggerType type){
        this.start = start.clone();
        this.end = end.clone();
        this.type = type;
    }
    
    public Trigger(Trace trace, int start, int end, TriggerType type){
        start = Math.max(0, start);
        end = Math.min(trace.size()-1, end);
        this.start = trace.getTracePointAtIndex(start).clone();
        this.end = trace.getTracePointAtIndex(end).clone();
        this.type = type;
        this.start_index = start;
        this.end_index = end;
        this.length = end-start;
    }
    
    public TracePoint getStart() {
        return start;
    }
    public void setStart(TracePoint start) {
        this.start = start;
    }
    public TracePoint getEnd() {
        return end;
    }
    public void setEnd(TracePoint end) {
        this.end = end;
    }
    public TriggerType getType() {
        return type;
    }
    public void setType(TriggerType type) {
        this.type = type;
    }

    /** this function can be buggy! Use it with caution! If the trace was cut or something else, the indexes are wrong!
     * 
     * @return true, if index is in the range of start and end
     */
    public boolean betweenIndex(int index){
        return (this.start_index <= index && index <= this.end_index);
    }
    
    @Override
    public String toString() {
        return "Trigger [type=" + type + ", start=" + start_index + ", end=" + end_index + "]";
    }
    
    @Override
    public Trigger clone(){
        Trigger trigger = new Trigger(this.start,this.end,this.type);
        trigger.end_index = this.end_index;
        trigger.start_index = this.start_index;
        trigger.length = this.length;
        return trigger;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Trigger other = (Trigger) obj;
        if (end == null) {
            if (other.end != null)
                return false;
        } else if (!end.equals(other.end))
            return false;
        if (start == null) {
            if (other.start != null)
                return false;
        } else if (!start.equals(other.start))
            return false;
        if (type != other.type)
            return false;
        return true;
    }



    /** Class to define which GPS-Error the Trigger has (or how it was found). If you want to add a TriggerType, just write a new name here.
     * 
     * @author nkl - Nicolas Klenert
     *
     */
    public enum TriggerType{
        DEFAULT,SPEEDZERO, GPSMISALGINMENT;
        public static final EnumSet<TriggerType> ALL_OPT = EnumSet.allOf(TriggerType.class);
    }
    
}
