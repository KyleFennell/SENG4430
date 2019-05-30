package com.dcaiti.traceloader.odometrie;

import java.util.ArrayList;
import java.util.List;

/** Class to create and manage TimeSections. Is used by the TraceFilter and/or ExtentedIntegral
 * 
 * @see com.dcaiti.traceloader.odometrie.TimeSection TimeSection
 * @see com.dcaiti.traceloader.odometrie.ExtendedIntegral ExtendedIntegral
 * 
 * @author nkl
 *
 */
public class TimeSectionHelper extends ArrayList<TimeSection>{
    
        private static final long serialVersionUID = 1L;
//        private List<TimeSection> sections;
        private boolean hasStarted;
        private boolean strict;
        private boolean closeBuild;
        private long lastTime;
        private long offset;
        private int maxId;
        private int lastId;
        
        private final static String NO_CLOSED_BUILD = "__TimeSectionsHelper__: Building Phase is not completed. Call closeBuild() before you want to use this helper!";

        public TimeSectionHelper() {
            super();
//            this.sections = new ArrayList<TimeSection>();
            this.lastTime = Long.MIN_VALUE;
            this.hasStarted = false;
            this.strict = true;
            this.offset = 0;
            this.closeBuild = false;
            this.maxId = 1;
        }

        public boolean setStart(long start){
            return this.setStart(start,1);
        }
        
        public boolean setStart(long start, int id) {
            if(closeBuild){
                return false;
            }
            if (this.strict && start < this.lastTime) {
                return false;
            }
            if (this.hasStarted) {
                return false;
            }
            if(this.strict && start == this.lastTime && id == this.lastId){
                //combine the two sections if they have the same id
                this.hasStarted = true;
                if(!this.isEmpty()){
                    this.lastTime = this.remove(this.size()-1).getStartTime();
                }
                return true;
            }
            
            this.hasStarted = true;
            this.lastTime = start;
            this.lastId = id;
            if(id > this.maxId){
                this.maxId = id;
            }
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
                    this.lastTime = this.get(this.size()-1).getEndTime();                    
                }
                return true;
            }
            this.hasStarted = false;
            this.add(new TimeSection(this.lastTime, end, this.lastId));
            this.lastTime = end;
            return true;
        }

        public boolean setSection(TimeSection section) {
            if(closeBuild){
                return false;
            }
            if (this.strict && section.getStartTime() < this.lastTime) {
                return false;
            } else if (this.strict && section.getEndTime() < section.getStartTime()) {
                return false;
            } else {
                this.add(section);
                this.lastTime = section.getEndTime();
                return true;
            }
        }
        
        public void closeBuild(){
            //TODO: test if there are any id that are not used! (print error)
            this.closeBuild = true;
        }
        
        public boolean isStrict(){
            return this.strict;
        }
        
        public void setOffset(long offset){
            this.offset = offset;
        }

        public int inSection(long time) {
            if(this.strict && !closeBuild){
                System.out.println(NO_CLOSED_BUILD);
            }
            if (this.strict) {
                // TODO: do binary search here and return id (remind the offset)
            }
            for (int i = 0; i < this.size(); ++i) {
                if (this.get(i).inSection(time-this.offset)) {
                    return this.get(i).getId();
                }
            }
            return 0;
        }
        
        public int onSection(long time) {
            if(this.strict && !closeBuild){
                System.out.println(NO_CLOSED_BUILD);
            }
            if (this.strict) {
                // TODO: do binary search here and return id (remind the offset)
            }
            for (int i = 0; i < this.size(); ++i) {
                if (this.get(i).onSection(time-this.offset)) {
                    return this.get(i).getId();
                }
            }
            return 0;
        }
        
        public int intersectSection(long start, long end){
            if(this.strict && !closeBuild){
                System.out.println(NO_CLOSED_BUILD);
            }
            if (this.strict) {
                // TODO: do binary search here and return id (remind the offset)
            }
            for (int i = 0; i < this.size(); ++i) {
                if (this.get(i).intersectSection(start-this.offset,end-this.offset)) {
                    return this.get(i).getId();
                }
            }
            return 0;
        }
        
        public int beginOfSection(long time){
            if (this.strict) {
                // TODO: do binary search here and return id (remind the offset)
            }
            for (int i = 0; i < this.size(); ++i) {
                if (this.get(i).getStartTime() + this.offset == time) {
                    return this.get(i).getId();
                }
            }
            return 0;
        }
        
        public int endOfSection(long time){
            if (this.strict) {
                // TODO: do binary search here and return id (remind the offset)
            }
            for (int i = 0; i < this.size(); ++i) {
                if (this.get(i).getEndTime() + this.offset == time) {
                    return this.get(i).getId();
                }
            }
            return 0;
        }
        
        public List<Long> getTimeStamps(){
            if(!this.strict){
                System.out.println("__TimeSectionsHelper__: this time stamp list is not sorted! Please be aware of that!");
            }
            List<Long> times = new ArrayList<Long>(this.size() * 2);
            for(int i = 0; i < this.size(); ++i){
                times.add(this.get(i).getStartTime() + this.offset);
                times.add(this.get(i).getEndTime() + this.offset);
            }
            return times;
        }
        
        public int getMaxId(){
            return this.maxId;
        }
        
        public void clear(){
            super.clear();
            this.hasStarted = false;
            this.closeBuild = false;
            this.lastTime = Long.MIN_VALUE;
            this.offset = 0l;
            this.maxId = 1;
        }
 
}
