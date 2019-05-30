package com.dcaiti.traceloader.odometrie.blender;

import org.apache.commons.math3.linear.ArrayRealVector;

public class GenericMathBlenderModule extends MathBlenderModule{

	ArrayRealVector initialState;
	String type;
	
	/** Constructor to construct a Module with constant Output (initialState)
	 * 
	 * @param initialState
	 */
	public GenericMathBlenderModule(double[] initialState){
		this.initialState = new ArrayRealVector(initialState);
		this.type = "const";
	}
	
	public GenericMathBlenderModule(){
		this.type = "meas";
	}
	
	@Override
	public double[] predict(double[] lastPrediction, double[] measurment){
		switch (this.type){
		case "const": return this.initialState.toArray();
		case "meas" : return measurment;
		default: return null;
		}

		
	}
	
	@Override
	public boolean hasToBeOptimised(){
	    return false;
	}
	
	public double[] getParam(){
	    return null;
	}
}
