package com.dcaiti.traceloader.odometrie;

import java.util.ArrayList;
import java.util.List;

import com.dcaiti.traceloader.Trace;
import com.dcaiti.traceloader.odometrie.Trigger.TriggerType;
import com.dcaiti.traceloader.odometrie.onepartition.GaussianOnePartition;
import com.dcaiti.traceloader.odometrie.onepartition.OnePartition;

public class DeadReckoningPieceCreator2 extends PieceCreator { 
    
    private OnePartition distr;

    public DeadReckoningPieceCreator2() {
//        this.distr = new LinearOnePartition(2);
        this.distr = new GaussianOnePartition(2);
    }
    
    public DeadReckoningPieceCreator2(OnePartition distr){
        if(distr.getSize() != 2){
            this.distr = new GaussianOnePartition(2);
        }else{
            this.distr = distr;            
        }
    }

    @Override
    public void generatePieces() {
        super.generatePieces();
        //generate all Pieces and add them to the map
        for(Trigger trigger : this.triggers){
            List<Trace> list = new ArrayList<Trace>();
            list.add(this.generatePiece(trigger.start_index, trigger.end_index));
            list.add(this.generatePiece(trigger.end_index, trigger.start_index));
            this.map.put(trigger, list);
        }
    }

    @Override
    public OnePartition getDistrOfTrigger(Trigger trigger) {
        return this.distr;
    }
    
    private Trace generatePiece(int start, int end){
        Trace trace = new Trace(this.trace.getBaseVehId());
        OdometriePrediction2 pred = new OdometriePrediction2(this.trace);
        trace.add(this.trace.getTracePointAtIndex(start));
        pred.startBy(this.trace.getTracePointAtIndex(start));
        int step = (int) Math.signum(end-start);
        int index = start + step;
        while(index != end){
            pred.predict(this.trace.getTracePointAtIndex(index));
            trace.add(pred.getState(this.trace.getTracePointAtIndex(index)));
            index += step;
        }
        trace.add(this.trace.getTracePointAtIndex(end));
        if(step < 0)
            trace.reverse();
        return trace;
    }
       
    @Override
    public int supports(TriggerType type) {
        if(type == TriggerType.SPEEDZERO){
            return 1;
        }
        if(type == TriggerType.GPSMISALGINMENT){
            return -1;
        }
        return 0;
    }

}
