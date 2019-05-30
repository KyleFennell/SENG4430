package com.dcaiti.traceloader.odometrie;

import com.vividsolutions.jts.geom.Coordinate;

public class SensorVehicleModel {
		
	private Coordinate pos;
	private double speed;
	private double heading;
	private double yaw;
	private long time;
	
	public SensorVehicleModel(long time,Coordinate coor, double heading, double speed, double yawRate, double xAccl, double yAccl){
		this.pos = new Coordinate(coor);
		this.speed = speed /3.6;
		this.heading = heading;
		this.yaw = yawRate;
		this.time = time /1000;
	}
	
	public double[] getState(){
		double[] state = new double[5];
		state[0] = this.pos.x;
		state[1] = this.pos.y;
		state[2] = this.speed;
		state[3] = this.heading;
		state[4] = this.yaw;
		return state;
	}
	
	public double[] getMathState(){
		double[] state = new double[7];
		state[6] = this.time;
		state[0] = this.pos.x;
		state[1] = this.pos.y;
		state[2] = this.speed;
		state[3] = Math.cos(this.heading);
		state[4] = Math.sin(this.heading);
		state[5] = this.yaw;
		return state;
	}
	
	
	public Coordinate nextPos(long time){
	    time /= 1000;
	    double dt = time - this.time;
	    return new Coordinate(this.pos.x + Math.cos(this.heading) * speed * dt, this.pos.y + Math.sin(this.heading) * speed * dt);
	}
}