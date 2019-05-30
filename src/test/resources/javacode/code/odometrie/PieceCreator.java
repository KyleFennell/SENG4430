package com.dcaiti.traceloader.odometrie;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dcaiti.traceloader.Trace;
import com.dcaiti.traceloader.TracePoint;
import com.dcaiti.traceloader.TraceTools;
import com.dcaiti.traceloader.odometrie.onepartition.OnePartition;
import com.dcaiti.utilities.KMLWriter;

/** abstract class to extend. This class is used, so that TraceMerger can improve some traces.
 * The TraceMerger can only use the PieceCreator which are given by the function {@link com.dcaiti.traceloader.odometrie.TraceMerger#addCreator(PieceCreator) addCreator()}
 * or if you add your new PieceCreator as a default creator (see optional step by {@link com.dcaiti.traceloader.odometrie.TraceMerger TraceMerger})
 * 
 * <p>
 * 
 * IMPORTANT: You HAVE to overwrite the method {@link com.dcaiti.traceloader.odometrie.PieceCreator#generatePieces() generatePieces}! This method does the main work!
 * 
 * <p>
 * 
 * The two main jobs PieceCreator does: 
 * <ol>
 * <li> create for every trigger a List of Traces (the alternative paths). This is done by populating the map! </li>
 * <li> gives for every trigger a OnePartition. This is done via a function. It allows the PieceCreator to say, which alternative path can be more trusted (at a specific time) </li>
 * </ol>
 * 
 * <p>
 * 
 * Look at {@link com.dcaiti.traceloader.odometrie.DeadReckoningPieceCreator2 DeadReckoningPieceCreator2} for an implementation example
 * 
 * @see com.dcaiti.traceloader.odometrie.TraceMerger TraceMerger
 * 
 * @author nkl
 *
 */
public abstract class PieceCreator {

    /** the TriggerFinder. From this class we get our trace and our 
     * 
     */
    protected TriggerFinder finder;
    /**
     *  the map you have to fill in your implementation -> every trigger gets a list of alternative routes
     */
    protected Map<Trigger,List<Trace>> map;
    /**
     *  Because the TriggerFindre clones the trace and the triggers, we get it only ones and save them
     */
    protected Trace trace;
    protected List<Trigger> triggers;
    
    //-----------------Methods you want to overwrite!---------------------------------------------------------------------
    /** function which returns to every trigger a OnePartition. 
     * If you only return 1 Piece by the trigger (so there is no merging needed), you can just return null
     * 
     * @param trigger 
     * @return OnePartition needed for merging the alternative Pieces got from the specified trigger
     */
    public abstract OnePartition getDistrOfTrigger(Trigger trigger);
    /** Method to generate pieces. You have to overwrite this. If you do, call as first argument: "super.generatePieces();"!
     */
    public void generatePieces(){
        if(this.map == null){
            this.map = new HashMap<Trigger,List<Trace>>();
        }
        if(this.finder == null){
            System.out.println("__"+this.getClass().getSimpleName()+"__: No TriggerFinder found. Can not generate any Pieces!");
            return;
        }
        this.trace = this.finder.getTrace();
        this.triggers = this.finder.getTriggers();
//        if(super.updateTriggerIndexes()){
//            System.out.println("__DeadReckoningPieceCreator__: The Indexes of the Triggers has to be updated!");
//        }
    }
    /** method which tells TraceMerger which PieceCreator supports the specified TriggerType.
     * 
     * @param type the specified TriggerType
     * @return int how good the PieceCreator supports this TriggerType. negative Number indicates no support!
     */
    public abstract int supports(Trigger.TriggerType type);
    
    
    //-----------------Methods which are already implemented for you. There is no need to overwrite them-----------------------------
    
    /** PieceCreator and finder has to have the same trace and finders. Therefore to ensure the similarity, PieceCreator get his data from the finder!
     * Because Finder clones his Input and Output, the data in trace and triggers are compatible
     * 
     * @param finder
     */
    public void setFinder(TriggerFinder finder) {
        this.finder = finder;
        this.map = null;
    }
    /** function to return the alternative routes created for some kind of trigger 
     * 
     * @param trigger the trigger of which we want the alternative routes
     * @return alternative routes
     */
    public List<Trace> getPiecesOfTrigger(Trigger trigger) {
        if(this.supports(trigger.getType()) < 0){
            System.out.println("__"+this.getClass().getSimpleName()+"__: TriggerType "+trigger.getType()+" is not supported by this PieceCreator!");
            return null;
        }
        if(this.map == null){
            this.generatePieces();
        }
        if (this.map.containsKey(trigger)){
            return this.map.get(trigger);
        }
        System.out.println("__"+this.getClass().getSimpleName()+"__: Can not find the Pieces for this trigger. Try again after using generatePieces()");
        return null;
    }
    /** This Method is deprecated! Now TriggerFinder and PieceCreator can enforce the triggers to be correct!
     * method to update the indexes of the trigger, so they will be consistent with the trace
     * 
     * @return false if there was no change, otherwise true
     */
    @Deprecated
    public boolean updateTriggerIndexes(){
        if(this.finder == null){
            return false;
        }
        boolean flag = false;
        for(int i = 0; i < this.trace.size(); ++i){
            TracePoint tp = this.trace.getTracePointAtIndex(i);
            for(Trigger trigger : this.triggers){
                if(tp.equals(trigger.getStart()) && trigger.start_index != i){
                    flag = true;
                    trigger.start_index = i;
                }
                if(tp.equals(trigger.getEnd()) && trigger.end_index != i){
                    flag = true;
                    trigger.end_index = i;
                }
            }
        }
        for(Trigger trigger : this.triggers){
            if(trigger.end_index - trigger.start_index != trigger.length){
                flag = true;
                trigger.length = trigger.end_index - trigger.start_index;
            }
        }
        return flag;
    }
    /** function to visualize the pieces. 
     * 
     * @param piecesOnly if true, the original trace will not be printed
     */
    public void visualizeTraceWithPieces(boolean piecesOnly){
        this.visualizeTraceWithPieces(piecesOnly,"results/pieces/"+this.trace.getBaseVehId()+"/");
    }
    /** function to visualize the pieces
     * 
     * @param piecesOnly if true, the original trace will not be printed
     * @param filePath the path we want to save the .kml files in
     */
    public void visualizeTraceWithPieces(boolean piecesOnly,String filePath){
        if(this.map == null){
            return;
        }
        if(!piecesOnly){
            TraceTools.visualizeTrace(this.trace, filePath+"trace.kml", KMLWriter.BLUE);
        }
        int counter = 0;
        for(Trigger trigger : this.triggers){
            if(this.supports(trigger.getType()) >= 0){
                List<Trace> list = this.getPiecesOfTrigger(trigger);
                if(list != null){
                    List<Color> colors = KMLWriter.getColorsInRange(Color.GREEN, Color.RED, 2);
                    for(int i = 0; i < list.size(); ++i){
                        TraceTools.visualizeTrace(list.get(i),filePath+"piece_"+counter+".kml", KMLWriter.getColorString(colors.get(i)));
                        ++counter;
                    }
                }
            }
        }
    }
    
 
    
}
