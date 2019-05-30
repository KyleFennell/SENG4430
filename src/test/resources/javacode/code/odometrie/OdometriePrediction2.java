package com.dcaiti.traceloader.odometrie;

import com.dcaiti.traceloader.Trace;
import com.dcaiti.traceloader.TracePoint;
import com.vividsolutions.jts.geom.Coordinate;

/** class to calculate the next step of the vehicle.
 *  Use this Class in all Application where you want to use odometrie data! -> it change coordinates and heading
 * 
 * @author nkl - Nicolas Klenert
 *
 */
public class OdometriePrediction2 {

    private Trace trace;
    private PathAugmenter path;
    
    private double measuredHeading;
    private long measuredTime;
    
    //variables for the predicted point
    private double speed;
    private Coordinate coor;
    private double yaw;
    private double heading;
    private long time;
    //temp variable
    protected int counter = 0;
    
    public OdometriePrediction2(Trace trace){
        this.trace = trace;
        this.path = trace.getPathAugmenter();
    }
    
    public void predict(TracePoint measurement){
        //times
        long time = measurement.getTime();
        long dtLong =  time - this.time;
        long midTime = this.time + dtLong/2;
        double dt = dtLong / 1000d;
        
        //speed and yaw
        double midSpeed = this.trace.getSpeedSIByTime(midTime);
        double midYaw = this.trace.getYawRateByTime(midTime);
        if(midSpeed == 0){
            midYaw = 0;
        }
        
        //heading
        double midHeading = this.heading + midYaw *dt/2;
        
        if(path.getMinGapToOriginalHeading(midTime) < 10l){
            //look at the heading from PathAugmenter
            double measMidHeading = path.getHeadingByTime(midTime);
            double threshold = 0.15;
            
            //TODO: look at the difference about measMidHeading and measureHeading
            midHeading = measMidHeading;
            
            this.measuredHeading = measMidHeading;
            this.measuredTime = midTime;
            
            this.heading = midHeading + midYaw*dt/2;
        }else{
            //Actualize measuredHeading -> it is our estimation of the heading with the last point of measurement
            
            this.heading += midYaw *dt;
        }
        
        double h1 = Math.cos(midHeading);
        double h2 = Math.sin(midHeading);
        
        
//        this.heading = midHeading + midYaw *dt/2;
        this.coor = new Coordinate(this.coor.x + h1*midSpeed*dt, this.coor.y + h2*midSpeed*dt);
        this.speed = measurement.getSpeedSI();
        this.yaw = measurement.getYawRate();
        this.time = measurement.getTime();
    }
    
    public TracePoint getState(TracePoint measurement){
        return new TracePoint.Builder(measurement)
                .coordinate(this.coor)
//                .speedSI(this.speed)
//                .yawRate(this.yaw)
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
        this.time = tp.getTime();
        this.measuredHeading = this.heading;
        this.measuredTime = this.time;
    }
    
}
