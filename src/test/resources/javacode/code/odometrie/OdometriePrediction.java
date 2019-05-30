package com.dcaiti.traceloader.odometrie;

import com.dcaiti.traceloader.TracePoint;
import com.vividsolutions.jts.geom.Coordinate;

/** class to calculate the next step of the vehicle. Use this Class in all Application where you want to use odometrie data!
 * 
 * @author nkl
 *
 */
public class OdometriePrediction {

    private double speed;
    private Coordinate coor;
    private double yaw;
    private double heading;
    private double measuredHeading;
    private double time;
    //temp variable
    protected int counter = 0;
    
    public OdometriePrediction(TracePoint tp){
        this.startBy(tp);
    }
    
    public void predict(TracePoint measurement){
        double heading = getGoodHeading(measurement);
        double h1 = Math.cos(heading);
        double h2 = Math.sin(heading);
        
        double time = measurement.getTimeSI();
        double dt =  time - this.time;
        //We assume but do not(!) know if this yaw rate is the average yaw rate.
        double yaw = measurement.getYawRate();
        double speed = measurement.getSpeedSI();
        if(speed == 0){
            //the vehicle do not turn around if it stands still!
            yaw = 0;
        }
        
        this.heading = heading;
        this.measuredHeading = measurement.getHeading();
        this.coor = new Coordinate(this.coor.x + h1*speed*dt, this.coor.y + h2*speed*dt);
        this.speed = measurement.getSpeedSI();
        this.yaw = yaw;
        this.time = time;
    }
    
    private double getGoodHeading(TracePoint measurement){
        double threshold = 0.1;
        double dt = measurement.getTimeSI() - this.time;
        //the heading of measurement.getHeading() gives the average heading (see it as heading for the midpoint between this and measurement)
        //We assume but do not(!) know if the same applies for the yawRate!
        double heading = this.measuredHeading + this.yaw * dt;
        if(Math.abs(InterpolationTools.radShortestAngle(heading,measurement.getHeading())) < threshold*dt){
            //measurement heading has no big error. We assume this value is better than our estimation
            return measurement.getHeading();
        }else{
            //otherwise return our estimation!
//            System.out.println(" Diff :  "+Math.abs(InterpolationTools.radShortestAngle(heading,measurement.getHeading())));
            ++this.counter;
            return this.heading + this.yaw * dt;
        }
    }
    
    public TracePoint getState(TracePoint measurement){
        return new TracePoint.Builder(measurement)
                .coordinate(this.coor)
                .speedSI(this.speed)
                .yawRate(this.yaw)
                .heading(this.heading)
                .build();
    }
    
    public double getHeading(){
        return this.heading;
    }
    
    public double getHeadingDiff(double heading){
        return Math.abs(InterpolationTools.radShortestAngle(this.heading, heading));
    }
    
    public double getHeadingDiff(TracePoint tp){
        return this.getHeadingDiff(tp.getHeading());
    }
    
    public Coordinate getCoor(){
        return this.coor;
    }
    
    public double getDistance(Coordinate coor){
        return this.coor.distance(coor);
    }
    
    public double getDistance(TracePoint tp){
        return this.getDistance(tp.getCoor());
    }
    
    public void startBy(TracePoint tp){
        this.speed = tp.getSpeedSI();
//        this.coor = new Coordinate(tp.getCoor().x,tp.getCoor().y);
        this.coor = tp.getCoor();
        this.yaw = tp.getYawRate();
        this.heading = tp.getHeading();
        this.time = tp.getTimeSI();
        this.measuredHeading = this.heading;
    }
    
}
