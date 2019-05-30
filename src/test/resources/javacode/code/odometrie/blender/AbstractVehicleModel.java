package com.dcaiti.traceloader.odometrie.blender;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.vividsolutions.jts.geom.Coordinate;

/** Class to abstract the sensor data in TracePoints
 * 
 * @author nkl
 *
 */
public class AbstractVehicleModel {

	private Vector2D position;
	private Vector2D speed;
	private Vector2D acceleration;
	
	// 1/number of sensor-measurements per second
	private double hz;
	
	public AbstractVehicleModel(Coordinate coor, double heading, double speed, double yawRate, double xAccl, double yAccl){
		this.position = new Vector2D(coor.x,coor.y);
		this.speed = new Vector2D(Math.cos(heading)*speed,Math.sin(heading)*speed);
		//TODO: calculate acceleration with yawRate and this.speed
		this.acceleration = new Vector2D(xAccl,yAccl);
		this.hz = 1;	//default value
	}
	
	public AbstractVehicleModel(Coordinate coor, double heading, double speed, double yawRate, double xAccl, double yAccl, double hz){
		this(coor,heading,speed,yawRate,xAccl, yAccl);
		this.hz = hz;
	}
	
	public Vector2D getPosition(){
		return this.position;
	}
	
	public Vector2D getSpeed(){
		return this.speed;
	}
	
	public Vector2D getAcceleration(){
		return this.acceleration;
	}
	
	public double getTimeInterval(){
		return this.hz;
	}
	
}
