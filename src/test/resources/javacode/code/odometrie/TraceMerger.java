package com.dcaiti.traceloader.odometrie;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.dcaiti.traceloader.Trace;
import com.dcaiti.traceloader.TracePoint;
import com.dcaiti.traceloader.TraceTools;
import com.dcaiti.traceloader.odometrie.Trigger.TriggerType;
import com.dcaiti.traceloader.odometrie.onepartition.OnePartition;
import com.dcaiti.utilities.KMLWriter;
import com.vividsolutions.jts.geom.Coordinate;

/** Class to erase some GPS-errors 
 * 
 * IMPORTANT: If you want to add some functionalities (like a new type of trigger or a new correction of a type of trigger) read the following steps:
 * <ul>
 * <li> To add or change a Type of Trigger, see {@link com.dcaiti.traceloader.odometrie.TriggerFinder TriggerFinder} </li>
 * <li> To add or change a correction of some triggers, see {@link com.dcaiti.traceloader.odometrie.PieceCreator PieceCreator} </li>
 * <li> To assemble everything together:
 * <ul>
 *      <li> (optional) change setDefaultCreators (add your new Creator to this) </li>
 *      <li> (optional) change setDefaultPriority (add your new TriggerType to this) </li>
 * </ul>
 *  </li>
 * </ul>
 * 
 * @see com.dcaiti.traceloader.odometrie.TriggerFinder TriggerFinder
 * @see com.dcaiti.traceloader.odometrie.PieceCreator PieceCreator
 * 
 * @author nkl - Nicolas Klenert
 *
 */
public class TraceMerger {

    private Trace trace;
    private Trace origTrace;
    private TriggerFinder finder;
    private Set<PieceCreator> creators;
    private Queue<Trigger.TriggerType> priority;
    
    /** Constructor
     * 
     * the given trace is cloned, so the original trace will not be altered!
     * 
     * @param trace the trace that should be improved
     */
    public TraceMerger(Trace trace){
        this.trace = trace.clone();
        this.origTrace = trace.clone();
        this.finder = new TriggerFinder(trace);
        this.creators = new HashSet<PieceCreator>();
    }
       
    /** function to clear TraceMerger and populate it with a new Trace
     *  Does not clear the PieceCreators (so you can use the old pieceCreators again)!
     *  Use the clear method if you want to change the PieceCreators
     * 
     * @param trace
     * @return
     */
    public TraceMerger setTrace(Trace trace){
        this.trace = trace.clone();
        this.origTrace = trace.clone();
        this.finder = finder.setTrace(trace);
        return this;
    }
    
    public TraceMerger clear(){
        this.trace = null;
        this.origTrace = null;
        this.finder.clear();
        this.creators.clear();
        return this;
    }
    
    /** method that return the default creators - called if no creators were given
     * 
     */
    private void setDefaultCreators(){
        this.creators.clear();
        this.addCreator(new DeadReckoningPieceCreator2());
    }
    
    /** method that return the default Priority - called if no priority queue was given
     * 
     */
    private void setDefaultPriority(){
        this.priority = new ArrayDeque<Trigger.TriggerType>();
        this.priority.offer(Trigger.TriggerType.SPEEDZERO);
        this.priority.offer(Trigger.TriggerType.DEFAULT);
    }
    
    /** function to add a PieceCreator to the listerner.
     *  Every PieceCreator will be called by a TriggerType and asked, how good they improve this trigger.
     *  The PieceCreator which promise the best result is choosed and asked to create alternative paths
     * 
     * @see com.dcaiti.traceloader.odometrie.PieceCreator PieceCreator
     * 
     * @param creator a PieceCreator which should be used to improve the trace
     * @return true if the PieceCreator could be successfully added to the subscription list 
     */
    public boolean addCreator(PieceCreator creator){
        creator.setFinder(this.finder);
        return this.creators.add(creator);
    }
    
    /** Set a new queue (list) of priorities. The gps-errors with the TriggerType, 
     * which has the highest priority will be improved first.
     * 
     * @param priority
     */
    public void setPrority(Queue<Trigger.TriggerType> priority){
        this.priority = priority;
    }
    
    /** static method which improve a trace - without some object oriented code
     * 
     * @param trace
     * @return
     */
    public static Trace improve(Trace trace){
        TraceMerger merger = new TraceMerger(trace);
        return merger.improve();
    }
    
    /** return the instance of triggerfinder we use
     * 
     * @see com.dcaiti.traceloader.odometrie.TriggerFinder
     * 
     * @return TriggerFinder
     */
    public TriggerFinder getTriggerFinder(){
        return this.finder;
    }
    
    /** returns the list of PieceCreator we use to improve the trace
     * 
     * @see com.dcaiti.traceloader.odometrie.TraceMerger#addCreator(PieceCreator) addCreator(PieceCreator)
     * 
     * @return list of PieceCreators
     */
    public Set<PieceCreator> getPieceCreators(){
        return this.creators;
    }
    
    /** method which starts the algorithm
     * 
     * the algorithm works like this:
     * <ol>
     * <li> find all Triggers of all Types (only the type we want to improve would be enqugh for now, but maybe in some future some PieceCreator want to now, where other gps-errors could be) </li>
     * <li> improve the Trace by the most important TriggerType (the PriorityQueue tells us which is the most important) </li>
     * <li> update TriggerFinder:
     * <ol> <li> update your trace (it was improved) </li>
     *      <li> delete all triggers you had with this type </li>
     *      <li> search for triggers with types you already found and you did not deleted </li>
     * </ol>
     * </li>
     * <li> PieceCreator get all Triggers and their Trace through TriggerFinder, so they are already updated </li>
     * </ol>
     * 
     * @return the improved Trace
     */
    public Trace improve(){
        if(this.creators.size() == 0){
            this.setDefaultCreators();
        }
        if(this.priority == null || this.priority.size() == 0){
            this.setDefaultPriority();
        }
        this.finder.findTrigger();
        
        EnumSet<TriggerType> triggerTypes = this.finder.getTriggerTypes();
        if(!this.priority.containsAll(triggerTypes)){
            EnumSet<TriggerType> toInsert = this.finder.getTriggerTypes();
            toInsert.removeAll(this.priority);
            this.priority.addAll(toInsert);
            System.out.println("__TraceMerger__: Not all TriggerTypes were in the priority queue. Missing ones were included at the end.");
        }
        this.priority.removeAll(EnumSet.complementOf(triggerTypes));
        
        //go through the queue
        while(!this.priority.isEmpty()){
            //improve Trace
            Trigger.TriggerType type = this.priority.poll();
            Trace trace = this.improveTraceByTriggerType(type);
            if(trace == null){
                //just delete trigger from TriggerFinder -> nothing was happening
                this.finder.deleteTriggerByType(type);
            }else{
                this.trace = trace;
                //update TriggerFinder -> because generatePieces() from PieceCreator get the triggers and trace from the finder
                this.finder.updateTrigger(trace, EnumSet.of(type));
            }
        }
        return this.trace;
    }
    
    /** function to improve a trace by one TriggerType. This does the actual merging!
     * 
     * @param type the type we want to improve
     * @return the improved Trace
     */
    public Trace improveTraceByTriggerType(Trigger.TriggerType type){
        //go trough the set and look which creator is the best
        int prio = -1;
        PieceCreator creator = null;
        for(PieceCreator cr : this.creators){
            int temp = cr.supports(type);
            if(temp > prio){
                prio = temp;
                creator = cr;
            }
        }
        if(creator == null){
            System.out.println("__TraceMerger__: There was no PieceCreator which supports TriggerType "+type);
            return null;
        }
        
        //create TracePieces
        creator.generatePieces();
        //generate improved Trace through merging -> write a more consistent version! (this code is kinda bad)
        Trace merge = new Trace(this.origTrace.getVehicleId());
        boolean merging = false;
        double maxlength = 0;
        int mergeCounter = 0;
        Trigger trigger = null;
        List<Trace> pieces = null;
        OnePartition distr = null;
        
        //go through the pieces and merge them (finder has the trigger, piece the pieces)
        for(int i = 0; i < this.trace.size(); ++i){
            TracePoint point = this.trace.getTracePointAtIndex(i);
            if(merging){
                if(trigger.getEnd().equals(point)){
                    //stop merging
                    merging = false;
                    trigger = null;
                    merge.add(new TracePoint.Builder(point).build());
                }else{
                  //do here some merging 
                    ++mergeCounter;
                    
                    //if we only have one piece -> add it
                    if(pieces.size() == 1){
                        merge.add(new TracePoint.Builder(point).coordinate(pieces.get(0).getTracePointAtIndex(mergeCounter).getCoor()).build());
                    }else{
                        TracePoint[] tps = new TracePoint[distr.getSize()];
                        for(int j = 0; j < pieces.size(); ++j){
                          tps[j]  = pieces.get(j).getTracePointAtIndex(mergeCounter);
                        }
                        //value is not the progress in TracePoints but the progress in distance!!
//                        double value = mergeCounter / (double)trigger.length;
                        double value = this.trace.lengthFromSpeed(trigger.start_index,i) / maxlength;
                        merge.add(this.mergeCoor(tps,distr.getDistribution(value),point));
                    }
                }
            }else{
                trigger = this.finder.getTriggerForStartPoint(point);
                if(trigger != null && trigger.getType() == type){
                    //begin merging
                    pieces = creator.getPiecesOfTrigger(trigger);
                    distr = creator.getDistrOfTrigger(trigger);
                    merging = true;
                    mergeCounter = 0;
                    //find maxlength!
                    for(int j = i; j < this.trace.size(); ++j){
                        if(trigger.getEnd().equals(this.trace.getTracePointAtIndex(j))){
                            maxlength = this.trace.lengthFromSpeed(i,j);
                            trigger.start_index = i;
                            trigger.end_index = j;
                            break;
                        }
                    }
                }
                merge.add(new TracePoint.Builder(point).build());
            }
            
        }
        return merge;
    }
            
    @SuppressWarnings("unused")
    @Deprecated
    private TracePoint mergeTP(TracePoint[] tps, double[] weights, TracePoint orig){
        double[] x  = new double[tps.length];
        double[] y  = new double[tps.length];
        double[] heading  = new double[tps.length];
        double[] yaw  = new double[tps.length];
        double[] speed  = new double[tps.length];
        
//        System.out.println(Arrays.toString(weights));
        
        for(int i = 0; i < tps.length; ++i){
            x[i] = tps[i].getCoor().x;
            y[i] = tps[i].getCoor().y;
            heading[i] = tps[i].getHeading();
            yaw[i] = tps[i].getYawRate();
            speed[i] = tps[i].getSpeedSI();
        }
        
        return new TracePoint.Builder(orig)
                .coordinate(new Coordinate
                        (InterpolationTools.weightedMean(x, weights),InterpolationTools.weightedMean(y, weights)))
                .speedSI(InterpolationTools.weightedMean(speed, weights))
                .heading(InterpolationTools.weightedMeanOfRad(heading, weights)[0])
                .yawRate(InterpolationTools.weightedMean(yaw, weights))
                .build();
        
    }
    
    /** We only change the GPS-Coordinates!
     *  (by now we use dead-reckoning concepts, so headings are already (more or less) consistent with this data)
     * 
     * @param tps
     * @param weights
     * @param orig
     * @return
     */
    private TracePoint mergeCoor(TracePoint[] tps, double[] weights, TracePoint orig){
        double[] x  = new double[tps.length];
        double[] y  = new double[tps.length];
        
        for(int i = 0; i < tps.length; ++i){
            x[i] = tps[i].getCoor().x;
            y[i] = tps[i].getCoor().y;
        }
        
        return new TracePoint.Builder(orig)
                .coordinate(new Coordinate
                        (InterpolationTools.weightedMean(x, weights),InterpolationTools.weightedMean(y, weights)))
                .build();
    }
    
    /** Method which offer one feature more than {@link com.dcaiti.traceloader.odometrie.TraceMerger#visualizeAllTraces() visualizeAllTraces}.
     *  
     * @see com.dcaiti.traceloader.odometrie.TraceMerger#visualizeAllTraces()
     * 
     * @param sectionsWrap if > 0, the method create also for every trigger kml-files of the original and the resulting trace.
     * the number tells how many TracePoints should we print before and after the trigger start/end
     */
    protected void visualizeAllTraces(int sectionsWrap){
        String path = "results/merger7/"+this.trace.getBaseVehId()+"/";
        finder.visualizeTraceWithTriggers(path+"trigger/", TriggerType.ALL_OPT);
        for(PieceCreator creator : this.creators){
            creator.visualizeTraceWithPieces(false,path+"pieces/"+creator.getClass().getSimpleName()+"/");
        }
        TraceTools.visualizeTrace(this.trace, path+"result/result.kml", KMLWriter.getColorString(Color.BLACK));
        TraceTools.visualizeTrace(this.origTrace, path+"result/orig.kml", KMLWriter.getColorString(Color.BLUE));
        if(sectionsWrap > 0){
            //create sections of origTrace and result (only necessary if you want nice svg images with indiemapper.com)
            List<Trigger> triggers = finder.getOrigTriggers();
            for(int i = 0; i < triggers.size(); ++i){
                Trigger trigger = triggers.get(i);
                Trace sec = this.trace.subTrace(trigger.start_index - sectionsWrap, trigger.end_index + sectionsWrap);
                Trace origSec = this.origTrace.subTrace(trigger.start_index - sectionsWrap, trigger.end_index + sectionsWrap);
                TraceTools.visualizeTrace(sec, path+"sections/result_"+i+".kml", KMLWriter.getColorString(Color.BLACK));
                TraceTools.visualizeTrace(origSec, path+"sections/orig_"+i+".kml", KMLWriter.getColorString(Color.BLUE));
            }
        }
    }
    
    /** this method shows a lot of traces, but not all (in general)
     * 
     *  It displays the resulting trace, the originalTrace,
     *  the originalTrace which is splitted into pieces from the triggers
     *  and the last pieces generated by the PieceCreators 
     *  (if a PieceCreator was used more than once to generate pieces, only the last generated pieces will be shown)
     * 
     */
    public void visualizeAllTraces(){
        this.visualizeAllTraces(0);
    }
    
        
}
