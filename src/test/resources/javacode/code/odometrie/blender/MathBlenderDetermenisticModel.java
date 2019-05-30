package com.dcaiti.traceloader.odometrie.blender;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.math.Vector2D;

public class MathBlenderDetermenisticModel extends MathBlenderModule{

	public double frequency = 0;
	public double threshold = 0;
	public double drift = 0;
	
	public MathBlenderDetermenisticModel(){
		this.frequency = 0;
	}
	
	public MathBlenderDetermenisticModel(double frequency){
		this.frequency = frequency;
	}
	
	@Deprecated
	public MathBlenderDetermenisticModel(double threshold, double drift){
	    this.threshold = threshold;
	    this.drift = drift;
	    this.frequency = 1;
	}
	
	@Deprecated
	public MathBlenderDetermenisticModel(double threshold, double drift, double frequency){
	    this.threshold = threshold;
            this.drift = drift;
            this.frequency = frequency;
	}
	
	@Override
	public void correct(double[] param){
	    this.threshold = param[0];
	    this.drift = param[1];
	}
	
	@Override
	public double[] predict(double[] lastPrediction, double[] measurement) {
	    return this.mathPredict(lastPrediction, measurement);
	}
	
	@Override
	public boolean hasToBeOptimised(){
	    return true;
	}
	
	@Override
	public double[][] optimisedParamSpace(){
	    double[][] params = new double[3][2];
	    //1. threshold; 2. drift
	    params[0] = new double[] {0, -0.1};
	    params[1] = new double[] {0.01, 0};
	    params[2] = new double[] {0.5, +0.1};
	    return params;
	}
	
	private double[] mathPredict(double[] lastPrediction, double[] measurement){
		double[] pred = new double[lastPrediction.length];
		//x,y,speed,heading,yaw-rate
		double midSpeed = (lastPrediction[2]+measurement[2])/2.0;		//midSpeed
//		double midSpeed = measurement[2];
		
		double h1 = lastPrediction[3];			//because heading is unsure like gps
		double h2 = lastPrediction[4];
		double speed = measurement[2];
	
		double dt = measurement[6] - lastPrediction[6];
		if(this.frequency > 0){
                    dt = this.frequency;
                }
		
		double yaw = measurement[5];
                //->change "mid"Speed with the formula: speed * cos(drift*dt) = real_speed
//                midSpeed = speed * Math.cos(this.drift*dt*42);          //42 is hacking...why is this effect so weak?
                
		double heading = new Vector2D(h1,h2).angle();
		//bad practice? i dont know. but there should be no yaw, if speed = 0 (or midSpeed = 0?)
		if(speed != 0){
		    double midYaw = (yaw+lastPrediction[5])/2.0;
	            midYaw += this.drift;               //0.003
	            if(Math.abs(midYaw) < this.threshold){              //0.012
	                midYaw = 0;
	            }
//	            heading += midYaw * this.frequency;
	            heading += midYaw * dt;
//	            heading += yaw * this.frequency;
		}else{
		    yaw = 0;
		}
		
		h1 = Math.cos(heading);
		h2 = Math.sin(heading);
				
//		pred[0] = lastPrediction[0] + h1*midSpeed*this.frequency;
//		pred[1] = lastPrediction[1] + h2*midSpeed*this.frequency;
		
		pred[6] = measurement[6];
		pred[0] = lastPrediction[0] + h1*midSpeed*dt;
		pred[1] = lastPrediction[1] + h2*midSpeed*dt;
		pred[2] = speed;		//speed measurement is good!
		pred[3] = h1;
		pred[4] = h2;
		pred[5] = yaw;        //yaw measurement is (hopefully) good!
		
		return pred;
	}
		
	public static double calculateMeanAngle(double one, double two){
		List<Double> list = new ArrayList<Double>(); 
		list.add(one);
		list.add(two);
		return calculateMeanAngle(list);
	}
	
	/**
	 * calculate average of all given angles
	 * 
	 * @param angles
	 *            angles (0..2PI) that will be averaged
	 * @return the average of the angles (0..2PI)
	 */
	public static double calculateMeanAngle(List<Double> angles) {
		Vector2D vec = null;
		for (Double bearingStart : angles) {
			Vector2D yVec = new Vector2D(1d, 0d);
			Vector2D rotated = yVec.rotate(bearingStart);
			if (vec == null) {
				vec = rotated;
			} else {
				vec = vec.add(rotated);
			}
		}

		return clampAngle(vec.angle());
	}
		
	/**
	 * clamp to 0..2PI
	 * 
	 * @param angle
	 *            the angle that can be smaller than 0 or larger than 360
	 *            degrees
	 * 
	 * @return the angle in 0..2Pi
	 */
	public static double clampAngle(double angle) {
		double twoPi = 2*Math.PI;
		while (angle < 0) {
			angle += twoPi;
		}
		while (angle >= twoPi) {
			angle -= twoPi;
		}
		return angle;
	}

    @Override
    public double[] getParam() {
        return new double[]{this.threshold, this.drift};
    }
	
}
