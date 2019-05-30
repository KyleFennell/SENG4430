package com.dcaiti.traceloader.odometrie;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import com.dcaiti.traceloader.Trace;
import com.dcaiti.traceloader.TracePoint;
import com.dcaiti.traceloader.TraceTools;
import com.dcaiti.traceloader.odometrie.Trigger.TriggerType;
import com.dcaiti.traceloader.odometrie.numbermapping.LogMapping;
import com.dcaiti.traceloader.odometrie.numbermapping.NumberMapping;
import com.dcaiti.traceloader.odometrie.numbermapping.UnivariateBorderFunction;
import com.dcaiti.utilities.GeomVector;
import com.dcaiti.utilities.KMLWriter;
import com.vividsolutions.jts.geom.Coordinate;

/** Class to find Triggers. Triggers describe Section of Traces which have to be corrected.
 * 
 * This class ensures, that his Trace and Triggers will not be mutated! This is done with cloning by every Input and Output!
 * So if you call some getters, please be aware that it is best to call the method only one time and save the result!
 * 
 * <p>
 * IMPORTANT: if you want to add functionalities to this class (add a new TriggerType) you have to make these steps:
 * <ul>
 * <li> write the new {@link com.dcaiti.traceloader.odometrie.Trigger.TriggerType TriggerType Enum} in the Class Trigger (e.g. FOO)</li>
 * <li> write a new method "findTriggerBy#TriggerTypeName#" (e.g. findTriggerByFoo) in this class</li>
 * <li> add a new case by the switch statement of {@link com.dcaiti.traceloader.odometrie.TriggerFinder#findTriggerByType(TriggerType, boolean) findTriggerByType} (e.g. case:FOO list = getTriggerByFoo(); break;)</li>
 * <li> now the new trigger will be found. To incorporate the new trigger to improve some traces, see {@link com.dcaiti.traceloader.odometrie.TraceMerger TraceMerger} </li>
 * </ul>
 * 
 * @see com.dcaiti.traceloader.odometrie.TraceMerger TraceMerger
 * 
 * @author nkl - Nicolas Klenert
 *
 */
public class TriggerFinder {

    private Trace trace;
    private Trace origTrace;
    private List<Trigger> triggers;
    private List<Trigger> origTriggers;
    private int nestedLevel;
    private NumberMapping numberMap;
    
    
    public TriggerFinder(Trace trace){
        this.trace = trace.clone();     //ensure that no one tempers with this trace
        this.origTrace = trace.clone();
        this.triggers = new ArrayList<Trigger>();
        this.origTriggers = new ArrayList<Trigger>();
        this.nestedLevel = -1;
    }
    
    /** function to clear Triggerfinder and populate it with a new Trace
     * 
     * @param trace
     * @return
     */
    public TriggerFinder setTrace(Trace trace){
        this.trace = trace.clone();
        this.origTrace = trace.clone();
        this.triggers.clear();
        this.origTriggers.clear();
        this.nestedLevel = -1;
        return this;
    }
    
    public TriggerFinder clear(){
        this.trace = null;
        this.origTrace = null;
        this.triggers.clear();
        this.origTriggers.clear();
        this.nestedLevel = -1;
        return this;
    }
    
    /** function to get the nested Level of Triggers
     * 
     * @return the highest level of nested Triggers
     */
    public int getNestedLevel(){
        if(this.triggers.size() != 0 && this.nestedLevel == -1){
            //nestedLevel should be at least 0 -> nestedLevel was never updated
            this.updateNestedLevel();
        }
        return this.nestedLevel;
    }
    
    /** get a clone of the trace
     * 
     * @return a cloned trace
     */
    public Trace getTrace(){
        return this.trace.clone();
    }
    
    /** get a clone of the list of triggers
     * 
     * @return cloned list of triggers
     */
    public List<Trigger> getTriggers(){
        return copyTriggers(this.triggers);
    }
    
    /** get a clone of the list of original triggers - the triggers we found at the first search
     * 
     * @return clones list of first founded triggers
     */
    protected List<Trigger> getOrigTriggers(){
        return copyTriggers(this.origTriggers);
    }
    
    /** returns the first trigger it finds for a TracePoint as Startpoint
     * 
     * @param tp TracePoint 
     * @return Trigger which has as Startpoint the given TracePoint
     */
    public Trigger getTriggerForStartPoint(TracePoint tp){
        for(Trigger trigger : triggers){
            if(trigger.getStart().equals(tp)){
                return trigger.clone();
            }
        }
        return null;
    }
    
    /** find all available triggers
     * 
     */
    public void findTrigger(){
        this.findTrigger(Trigger.TriggerType.ALL_OPT);
    }
    
    /** find all triggers with the given types
     * 
     * @see com.dcaiti.traceloader.odometrie.TriggerFinder#findTriggerByType(TriggerType) findTriggerByType
     * 
     * @param types EnumSet of the types we want to find
     */
    private void findTrigger(EnumSet<Trigger.TriggerType> types){
        for(Trigger.TriggerType type : types){
            this.findTriggerByType(type, false);
        }
        System.out.println("__TriggerFinder__: I found "+this.triggers.size()+" Triggers");   
        this.tidyUp();
        System.out.println("__TriggerFinder__: After cleaning I have "+this.triggers.size()+" Triggers");
        //clone triggers, so we have the original ones to visualize it
        if(this.origTriggers == null || this.origTriggers.isEmpty()){
            for(Trigger trigger : this.triggers){
                this.origTriggers.add(trigger.clone());
            }
        }
    }
    
    /** find all triggers of one type
     * 
     * @param type the type we want to find
     * @return
     */
    public List<Trigger> findTriggerByType(Trigger.TriggerType type){
       return this.findTriggerByType(type, true);
    }
    
    /** used for {@link com.dcaiti.traceloader.odometrie.TriggerFinder#findTriggerByType(TriggerType) findTriggerByType(type)}
     * 
     * IMPORTANT: if you want to add functionalities to TriggerFinder, please read the Class Description
     * If a TriggerType is not yet implemented, this function returns null
     * 
     * @param type the type we want to find
     * @param calledByExtern boolean to indicate if used extern or intern (intern we do not need to return a copied list of triggers)
     * @return
     */
    private List<Trigger> findTriggerByType(Trigger.TriggerType type, boolean calledByExtern){
        List<Trigger> list;
        switch(type) {
        case SPEEDZERO:
            list = this.findTriggerBySpeedZero(); break;
        case GPSMISALGINMENT:
            //not yet working -> see word document about improvement
//            list = this.findTriggersByGPSMisalignment2(); break;
        default:
            list = null; break;
        } 
        
        if(list == null){
            return null;
        }else{
            this.triggers.addAll(list);
            if(calledByExtern){
                return copyTriggers(list);
            }else{
                return null;
            }
        }
       
    }
    
    /** method to find the TriggerType SPEEDZERO
     * 
     * @see com.dcaiti.traceloader.odometrie.Trigger.TriggerType TriggerType
     * 
     * @return list of Trigger
     */
    private List<Trigger> findTriggerBySpeedZero(){
      //TODO: look if trace segment is bulky/zackig. If thats not the case, delete the trigger (function tidyUpTrigger()? (also with switching by type?))
        List<Trigger> list = new ArrayList<Trigger>();
        //find trigger
        for(int i=0; i < trace.size(); ++i){
            TracePoint point = trace.getTracePointAtIndex(i);
            if(point.getSpeedSI() == 0){
                //get start point -> where speed was bigger than a specific threshold
                double threshold = 5.0;
                int start = -1;
                for(int j = i; j > 0; --j){
                    if(trace.getTracePointAtIndex(j).getSpeedSI() > threshold){
                        start = j;
                        break;
                    }
                }
                //get end point -> just use a specific distance the car has to travel
                //TODO: use offset of gps to determine how long this distance should be.
                Coordinate coor_start = point.getCoor();
                int end = -1;
                double distance = 100;
                
                //estimate distance with distance the gps traveled
                Coordinate coor_accel = null;
                int accel = i;
                for(int j = i; j < trace.size(); ++j){
                    double speed = trace.getTracePointAtIndex(j).getSpeed();
                    if(speed > threshold){
                        coor_accel = trace.getTracePointAtIndex(j).getCoor();
                        accel = j;
                        break;
                    }
                }
                double dis_gps = 10;
                if(coor_accel !=  null){
                    dis_gps = coor_accel.distance(coor_start);
                }
                distance = getDistanceForTraveledGPS(dis_gps);
//                System.out.println(dis_gps+" ergibt "+distance);
                
                //get end point
                for(int j = accel; j < trace.size(); ++j){
                    Coordinate coor = trace.getTracePointAtIndex(j).getCoor();
                    if(coor.distance(coor_start) > distance){
                        end = j;
                        i = j; //update i
                        break;
                    }
                }
                if(start == -1){
                    //trace starts with a not moving vehicle
                }else if(end == -1){
                    //trace ends with a stopping vehicle
                    list.add(new Trigger(this.trace,start,this.trace.size()-1,Trigger.TriggerType.SPEEDZERO));
                }else{
                    list.add(new Trigger(this.trace,start,end,Trigger.TriggerType.SPEEDZERO));
                }
            }
        }
        
        return list;
    }
    
    /** helper method for {@link com.dcaiti.traceloader.odometrie.TriggerFinder#findTriggerBySpeedZero() findTriggerBySpeedZero}
     * 
     *  correlates distance of the gps-error with the distance the trigger should have
     * 
     * @param disGPS
     * @return
     */
    private double getDistanceForTraveledGPS(double disGPS){
        if(this.numberMap == null){
            double minDis = 10;
            double maxDis = 150;
                    
            double minValueGPS = 0;
            double maxValueGPS = 40;
            
//            UnivariateBorderFunction func = new SigmoidMapping(minDis,maxDis);
            UnivariateBorderFunction func = new LogMapping();
            this.numberMap = new NumberMapping(minValueGPS,maxValueGPS,minDis,maxDis,func);
        }

        return this.numberMap.value(disGPS);
    }
    
    @SuppressWarnings("unused")
    @Deprecated
    private List<Trigger> findTriggersByGPSMisalignment(){
        List<Trigger> list = new ArrayList<Trigger>();
        
        //double headingdiff = 0.175; //10°
        double headingdiff = 0.4;
        double distance = 100;
        boolean misalignment = false;
        int start = -1;
        OdometriePrediction predict = new OdometriePrediction(this.trace.getTracePointAtIndex(0));
        //just mark all places where heading misalignment is bigger than headingdiff
        for(int i = 1; i < this.trace.size(); ++i){
            TracePoint tp = this.trace.getTracePointAtIndex(i);
            predict.startBy(this.trace.getTracePointAtIndex(i-1));
            predict.predict(tp);
            if(predict.getHeadingDiff(tp) > headingdiff){
                if(!misalignment){
                    misalignment = true;
                    start = i;
                }
            }
            if(misalignment && tp.getCoor().distance(this.trace.getTracePointAtIndex(start).getCoor()) > distance){
                list.add(new Trigger(this.trace,start,i,Trigger.TriggerType.GPSMISALGINMENT));
                misalignment = false;
            }
        }
        if(misalignment){
            list.add(new Trigger(this.trace,start,this.trace.size()-1,Trigger.TriggerType.GPSMISALGINMENT));
            misalignment = false;
        }
        
        return list;
    }
    
    @SuppressWarnings("unused")
    private List<Trigger> findTriggersByGPSMisalignment2(){
       List<Trigger> list = new ArrayList<Trigger>();
       PathAugmenter path = this.trace.getPathAugmenter();
//       double allowedHeadingDiff = 0.2;
       double allowedHeadingDiff = 0.15;         //1 is enough for some things
       double headingDiff = 0;
       int start = -1;
       boolean misalignment = false;
       
       //temp variables -> only to debug
       int counter = 0;
       int maxCounter = 100;
       double recording[] = new double[maxCounter+1];
       
       //GeomVector to record the difference
       GeomVector realStartpoint = null;
       GeomVector realEndpoint = null;
       GeomVector calculatedStartpoint = null;
       GeomVector calculatedEndpoint = null;
       double diff = 0;
       double minDiff = 0.1;
       double maxDiff = 100;
       
       for(int i = 1; i < this.trace.size(); ++i){
           TracePoint last = this.trace.getTracePointAtIndex(i-1);
           TracePoint now = this.trace.getTracePointAtIndex(i);
           //get the heading from the coor
           double xdiff = now.getCoor().x - last.getCoor().x; 
           double ydiff = now.getCoor().y - last.getCoor().y;
           long timeLast = last.getTime();
           long timediff = now.getTime() - timeLast;
           long time = timeLast + timediff/2;
           double heading = path.getHeadingByTime(time);
           
           double coorHeading = FastMath.atan2(ydiff, xdiff);
           headingDiff = InterpolationTools.radShortestAngle(heading, coorHeading);
           
           //startCondition
           if(Math.abs(headingDiff) > allowedHeadingDiff){
               
               //if headings are too uncorrelated -> start trigger 
               realStartpoint = new GeomVector(last.getCoor().x, last.getCoor().y);
               calculatedStartpoint = realStartpoint;
               calculatedEndpoint = new GeomVector(heading);
               realEndpoint = new GeomVector(xdiff,ydiff);
               diff = calculatedEndpoint.getOrhtogonalDistance(realEndpoint);
               recording[0] = diff;
               counter = 1;
               list.add(new Trigger(this.trace, i, i+10, Trigger.TriggerType.GPSMISALGINMENT));
               
           }
           
           
           //TODO: add the endCondition!
       }
       return list;
    }
    
    /** helper method to clone a trigger list
     * 
     * @param triggers
     * @return
     */
    private static List<Trigger> copyTriggers(List<Trigger> triggers){
        List<Trigger> list = new ArrayList<Trigger>();
        for(int i = 0; i < triggers.size(); ++i){
            list.add(triggers.get(i).clone());
        }
        return list;
    }
    
    /** method which tries to tidy up messed up trigger lists. If the findTriggerBy*** methods are good enough, this method should not change anything.
     * 
     */
    public void tidyUp(){
        
        //destroy all trigger with same start and end index
        //IMPORTTANT! Otherwise visualizeTraceWithTrigger() does not work correctly! (the level of trigger is nor determined right)
        List<Trigger> corr = new ArrayList<Trigger>();
        for(Trigger trigger : this.triggers){
            if(trigger.start_index != trigger.end_index){
                corr.add(trigger);
            }
        }
        this.triggers = corr;
        
        
        //sort all triggers with their start_index
        Collections.sort(this.triggers,new Comparator<Trigger>(){
            @Override
            public int compare(Trigger arg0, Trigger arg1) {
                return (int) Math.signum(arg0.start_index - arg1.start_index);
            }
        });
        //triggers are now sorted after start_index!
        
        //merge triggers with the same type
        corr = new ArrayList<Trigger>();
        EnumSet<TriggerType> types = this.getTriggerTypes();
        for(TriggerType type : types){
            List<Trigger> triggers = this.getTriggersByType(EnumSet.of(type));
            //triggers should be sorted
            while(!triggers.isEmpty()){
                Trigger head = triggers.remove(0);
                int end = head.end_index;
                List<Trigger> merge = new ArrayList<Trigger>();
                for(int i = 0; i < triggers.size(); ++i){
                    Trigger now = triggers.get(i);
                    if(now.start_index <= end){
                        end = now.end_index > end ? now.end_index : end;
                        merge.add(now);
                    }else{
                        break;
                    }
                }
                if(merge.size() == 0){
                    corr.add(head);
                }else{
                    triggers.removeAll(merge);
                    //merge head and merge to one Trigger
                    Trigger t = new Trigger(this.trace,head.start_index, end, type);
                    corr.add(t);
                }
            }
        }
        this.triggers = corr;
    }
    
    /** update the private variable nestedLevel
     * 
     * @return
     */
    private int updateNestedLevel(){
        if(this.triggers.size() == 0){
            this.nestedLevel = -1;
            return this.nestedLevel;
        }
        this.nestedLevel = nestedLevel(this.trace, this.triggers, TriggerType.ALL_OPT);
        return this.nestedLevel;
    }
    
    /** static function to calculate the nested Level of triggers on a trace with the defined TriggerTypes
     * 
     * @param trace the trace the triggers live in
     * @param triggers the triggers which nested level we want to know
     * @param types only trigger with one of these types will be not ignored
     * @return
     */
    public static int nestedLevel(Trace trace,List<Trigger> triggers, EnumSet<TriggerType> types){
        int count = 0;
        for(int i= 0; i < trace.size(); ++i){
            int counter = -1;
            for(Trigger trigger : triggers){
                if(types.contains(trigger.getType()) && trigger.betweenIndex(i)){
                    ++counter;
                }
            }
            if(counter > count){
                count = counter;
            }
        }
        return count;
    }
    
    /** update the trace and the trigger in the changed sections (TriggerType says which Sections were changed)
     *  should only be called by TraceMerger after a successful merge
     * 
     * @param trace
     * @param type
     */
    protected void updateTrigger(Trace trace, EnumSet<Trigger.TriggerType> type){
        //if you want to update with the last triggerType, there is nothing to do
        EnumSet<Trigger.TriggerType> TriggerTypes = this.getTriggerTypes();
        TriggerTypes.removeAll(type);
        //oldTriggerTypes.equals(type) does not work...why?!
        if(TriggerTypes.size() == 0){
            this.trace = trace.clone();
            this.triggers = new ArrayList<Trigger>();
            this.nestedLevel = -1;
            return;
        }
        //TODO: check if Trace was only changed in the sections defined by TriggerType
        List<Trigger> toDelete = this.getTriggersByType(type);
        boolean updated = true;
        if(updated){
            boolean bruteForce = true;
            //update the Trace (it would be better if we just change our trace)
            this.trace = trace.clone();
            //brute force for now
            if(bruteForce){
                this.triggers = new ArrayList<Trigger>();
                this.findTrigger(TriggerTypes);
            }else{
                //delete trigger with this type
                this.triggers.removeAll(toDelete);
                
                //update all other triggers
                //TODO: do something fancy here (if you want to have a performance boos use not bruteForce.
                //Otherwise is BruteForce less error prone)
            }
            this.updateNestedLevel();
        }else{
            System.out.println("__TriggerFinder__: Trace is not the updated Trace!");
        }
        
    }
    
    /** method to remove trigger of a specific type
     * 
     * @param type
     */
    protected void deleteTriggerByType(Trigger.TriggerType type){
        this.triggers.remove(this.getTriggersByType(EnumSet.of(type)));
    }
    
    /** returns a list of TriggerTypes which can be found in our triggers
     * 
     * @return
     */
    public EnumSet<Trigger.TriggerType> getTriggerTypes(){
        EnumSet<Trigger.TriggerType> set = EnumSet.noneOf(Trigger.TriggerType.class);
        for(Trigger trigger : this.triggers){
            set.add(trigger.getType());
        }
        return set;
    }
    
    /** method to get only specific trigger. filter the triggers by their type
     * 
     * @param type
     * @return
     */
    private List<Trigger> getTriggersByType(EnumSet<Trigger.TriggerType> type){
        return filterTriggerByType(this.triggers,type);
    }
    
    /** static function which filters triggers by their given types. Only triggers which have a type declared as parameter will be returned
     * 
     * @param trig
     * @param type
     * @return
     */
    private static List<Trigger> filterTriggerByType(List<Trigger> trig,EnumSet<Trigger.TriggerType> type){
        if(TriggerType.ALL_OPT.equals(type)){
            return trig;
        }
        List<Trigger> triggers = new ArrayList<Trigger>();
        for(int i = 0; i < trig.size(); ++i){
            Trigger trigger = trig.get(i);
            if(type.contains(trigger.getType())){
                triggers.add(trigger);
            }
        }
        return triggers;
    }
        
    /** Visualize function. visualize all triggers as pieces of traces with different kind of color. Which trigger has which type cannot(!) be seen
     * 
     */
    public void visualizeTraceWithTriggers(){
        this.visualizeTraceWithTriggers("results/trigger/"+this.trace.getBaseVehId()+"/", TriggerType.ALL_OPT);
    }
    
    /** same as {@link com.dcaiti.traceloader.odometrie.TriggerFinder#visualizeTraceWithTriggers() visualizeTraceWithTriggers} only with a TriggerType filter.
     * only triggers with the declared type will not be ignored
     * 
     * @param type
     */
    public void visualizeTraceWithTriggerType(TriggerType type){
        this.visualizeTraceWithTriggers("results/trigger/"+this.trace.getBaseVehId()+"/", EnumSet.of(type));
    }
    
    /** same as {@link com.dcaiti.traceloader.odometrie.TriggerFinder#visualizeTraceWithTriggerType(TriggerType) visualizeTraceWithTriggers(TriggerType)}
     * only with the difference, that the path of the resulting .kml files can be declared and that the triggers can be filtered by more than one type
     * 
     * @param file_path
     * @param types
     */
    public void visualizeTraceWithTriggers(String file_path,EnumSet<TriggerType> types){
        //find out how nested the triggers are
        int count = nestedLevel(this.origTrace, this.origTriggers, types);
        List<Trigger> trig = filterTriggerByType(this.origTriggers, types);
        //count is the nested level of the triggers -> get so many colors
        System.out.println("__TriggerFinder__: Triggers visualized: "+trig.size());
        System.out.println("__TriggerFinder__: Nested Level of Triggers: "+count);
//        for(int i = 0; i < this.triggers.size(); ++i){
//            System.out.println(this.triggers.get(i).toString());
//        }
        List<Color> colors = KMLWriter.getColorsInRange(Color.GREEN, Color.RED, count);
        colors.add(0, Color.blue);
//        System.out.println("__TriggerFinder__: Amount of Colors: "+colors.size());
        
        //cut the Trace
        List<Trace> pieces = new ArrayList<Trace>();
        List<Integer> level = new ArrayList<Integer>();
        int counter = 0;
        int lastCut = 0;
        for(int i = 0; i < this.origTrace.size(); ++i){
            TracePoint point = this.origTrace.getTracePointAtIndex(i);
            for(Trigger trigger: trig){
                if(trigger.getStart().equals(point)){           //trigger.getStart().equals(point)
                    pieces.add(this.origTrace.subTrace(lastCut, i+1));
                    level.add(counter);
                    ++counter;
                    lastCut = i;
                }else if(trigger.getEnd().equals(point)){             //trigger.getEnd().equals(point)
                    pieces.add(this.origTrace.subTrace(lastCut, i+1));
                    level.add(counter);
                    --counter;
                    lastCut = i;
                }
            }
        }
        pieces.add(this.origTrace.subTrace(lastCut, this.origTrace.size()));
        level.add(0);
        //visualize all traces
        System.out.println("__TriggerFinder__: There are "+pieces.size()+" pieces of the trace. The trace is "+this.origTrace.size()+" long.");
        for(int i = 0; i < pieces.size(); ++i){
            TraceTools.visualizeTrace(pieces.get(i), file_path+"part_"+i+".kml", KMLWriter.getColorString(colors.get(level.get(i))));
        }
    }
        
}
