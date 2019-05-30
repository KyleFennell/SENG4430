package com.dcaiti.traceloader.odometrie;

import java.util.ArrayList;
import java.util.List;

import com.dcaiti.traceloader.Trace;
import com.dcaiti.traceloader.TracePoint;
import com.dcaiti.traceloader.odometrie.Trigger.TriggerType;
import com.dcaiti.traceloader.odometrie.onepartition.GaussianOnePartition;
import com.dcaiti.traceloader.odometrie.onepartition.OnePartition;
import com.vividsolutions.jts.geom.Coordinate;

public class DeadReckoningPieceCreator extends PieceCreator { 
    
    private OnePartition distr;
    private double frequency;
    private double drift;
    private double threshold;

    public DeadReckoningPieceCreator() {
//        this.distr = new LinearOnePartition(2);
        this.distr = new GaussianOnePartition(2);
    }
    
    public DeadReckoningPieceCreator(OnePartition distr){
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
        trace.add(this.trace.getTracePointAtIndex(start));
        int step = (int) Math.signum(end-start);
        int index = start + step;
        while(index != end){
            trace.add(this.predict(trace.getTracePointAtIndex(trace.size()-1), this.trace.getTracePointAtIndex(index)));
            index += step;
        }
        trace.add(this.trace.getTracePointAtIndex(end));
        if(step < 0)
            trace.reverse();
        return trace;
    }
       
    public TracePoint predict(TracePoint last, TracePoint meas){
        double midSpeed = (last.getSpeedSI()+meas.getSpeedSI())/2.0;               //midSpeed
        double heading = last.getHeading();
        double h1 = Math.cos(heading);
        double h2 = Math.sin(heading);

        double dt = meas.getTimeSI() - last.getTimeSI();                //is negative if reverse ("change" last and meas)
        if(this.frequency != 0){
            dt = this.frequency * Math.signum(dt);
        }
        
        double yaw = meas.getYawRate();
        //->change "mid"Speed with the formula: speed * cos(drift*dt) = real_speed
//        midSpeed = speed * Math.cos(this.drift*dt*42);          //42 is hacking...why is this effect so weak?
        
        //bad practice? i dont know. but there should be no yaw, if speed = 0 (or midSpeed = 0?)
        if(meas.getSpeed() != 0){
            double midYaw = (yaw+last.getYawRate())/2.0;
            midYaw += this.drift;               //0.003
            if(Math.abs(midYaw) < this.threshold){              //0.012
                midYaw = 0;
            }
            heading += midYaw * dt;
        }else{
            yaw = 0;
        }
          
        return new TracePoint.Builder(meas)
                .coordinate(new Coordinate(last.getCoor().x + h1*midSpeed*dt,last.getCoor().y + h2*midSpeed*dt))
                .heading(heading)
                .yawRate(yaw)
                .build();
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
