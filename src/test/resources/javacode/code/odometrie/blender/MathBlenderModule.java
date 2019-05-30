package com.dcaiti.traceloader.odometrie.blender;

public abstract class MathBlenderModule {

	public abstract double[] predict(double[] lastPrediction, double[] measurment);
	
	public boolean hasToBeOptimised(){
	    return false;
	}
	
	/** function to give the ParameterSpace in which the module should be optimised
	 * 
	 * @return double[3][#params] three arrays of params. The first array contains the params min-value,
	 *  second the "start"-Point, third the max-value
	 */
	public double[][] optimisedParamSpace(){
	    return null;
	}
	
	/**
	 * 
	 * @param param the params given by the optimisedParamSpace to optimise the module
	 */
	public void correct(double[] param){
	    
	}
	
	public abstract double[] getParam();
	
}
