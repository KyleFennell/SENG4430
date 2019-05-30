package com.dcaiti.traceloader.odometrie.blender;

import java.util.ArrayList;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class MathBlender {

	RealVector state;
	RealVector weight;
	RealVector initialWeight = new ArrayRealVector();
	BlockRealMatrix changeOnWeight;
	double threshold = 0.01;
	ArrayList<MathBlenderModule> modules = new ArrayList<MathBlenderModule>();
	double[][] lastPredictedStates;
	boolean useBlendedState = false;
	MathBlenderTrigger trigger = null;
	
	public MathBlender(){
		
	}
	
	public MathBlender(double threshold){
		this.threshold = threshold;
	}
	
	public MathBlender(double threshold, boolean blendedState){
		this(threshold);
		this.useBlendedState = blendedState;
	}
	
	public MathBlender(boolean blendedState){
		this.useBlendedState = blendedState;
	}
	
	@Deprecated
	public MathBlender(double[] initialState){
		this.state = new ArrayRealVector(initialState);
	}
	
	@Deprecated
	public MathBlender(double[] initialState, double threshold){
		this(initialState);
		this.threshold = threshold;
	}
	
	public void reset(){
		this.state = null;
		this.trigger = null;
	}
	
	@Deprecated
	public void startWorking(){
		//test if initialWeight was given:
		if(this.initialWeight.getL1Norm() != 1){
			double[] w = new double[modules.size()];
			double v = 1.0/w.length;
			for(int i = 0; i < w.length ; ++i){
				w[i] = v;
			}
			this.initialWeight = new ArrayRealVector(w);
		}

		this.weight = new ArrayRealVector(this.initialWeight);
		this.initialiseChangeOnWeight();
	}
	
	@Deprecated
	public void startWorking(double[] weight){
		RealVector vecWeight = new ArrayRealVector(weight);
		if(vecWeight.getL1Norm() != 1){
			this.startWorking();
		}else{
			this.weight = vecWeight;
			this.initialiseChangeOnWeight();
		}
	}
	
	public void startWorking(MathBlenderTrigger trigger) throws IllegalArgumentException{
	    if(trigger.getInitialWeight() != null && trigger.size() != this.modules.size()){
	        throw new IllegalArgumentException("Trigger-Weight-Size ist not compatible to the Module-Size");
	    }
	    this.trigger = trigger;
	    //use weight of the trigger
	    if(this.trigger.initialWeight == null || new ArrayRealVector(this.trigger.initialWeight).getL1Norm() != 1){
	        //use initialWeight of MathBlender if trigger does not have a correct weight
	        if(this.initialWeight == null || this.initialWeight.getL1Norm() != 1){
                    double[] w = new double[modules.size()];
                    double v = 1.0/w.length;
                    for(int i = 0; i < w.length ; ++i){
                            w[i] = v;
                    }
                    this.initialWeight = new ArrayRealVector(w);
                }
	        this.weight = this.initialWeight;
	    }else{
	        this.weight = new ArrayRealVector(this.trigger.initialWeight);
	    }
	}
	
//	@Deprecated
//	public void startWorking(double[] initialState){
//		this.state = new ArrayRealVector(initialState);
//		//test if initialWeight was given:
//		if(this.initialWeight.getL1Norm() != 1){
//			double[] w = new double[modules.size()];
//			double v = 1.0/w.length;
//			for(int i = 0; i < w.length ; ++i){
//				w[i] = v;
//			}
//			this.initialWeight = new ArrayRealVector(w);
//		}
//
//		this.weight = new ArrayRealVector(this.initialWeight);
//		this.initialiseChangeOnWeight();
//	}
	
	public void subscribe(MathBlenderModule module){
		this.modules.add(module);
	}
	
	public void subscribe(MathBlenderModule module, double defaultWeight){
		this.modules.add(module);
		this.initialWeight = this.initialWeight.append(defaultWeight);
	}
	
	private void initialiseChangeOnWeight(){
		double[][] w = new double[this.modules.size()][this.modules.size()];
		for(int i = 0; i < this.modules.size(); ++i){
			w[0][i] = 0.2;
			w[i][i] += 0.8;
		}
		this.changeOnWeight = new BlockRealMatrix(w);
	}
	
	public double[] getPredictionOfModule(int index, double[] measurment){
		if(this.state == null){
			return measurment;
		}
		if(this.useBlendedState){
			return this.modules.get(index).predict(this.state.toArray(), measurment);
		}else{
			return this.modules.get(index).predict(this.lastPredictedStates[index], measurment);
		}
	}
	
	public RealVector predict(double[] measurment) throws Exception{
		if(this.state == null){
		    //this is the first time blender wants to predict something. just return the initialValue
			this.state = new ArrayRealVector(measurment);
			if(!this.useBlendedState){
				double[][] states = new double[this.modules.size()][this.state.getDimension()];
				for(int i = 0; i < this.modules.size(); ++i){
					states[i] = this.state.toArray();
				}
				this.lastPredictedStates = states.clone();
			}
			return this.state;
		}
		
		if(this.modules.size() == 0){
			throw new Exception("No modules found");
		}else if(this.trigger == null){
		    //does not blend - just return the prediction of the first module
			this.state = new ArrayRealVector(this.modules.get(0).predict(this.state.toArray(), measurment));
			if(!this.useBlendedState){
				double[][] states = new double[this.modules.size()][this.state.getDimension()];
				for(int i = 0; i < this.modules.size(); ++i){
					states[i] = this.state.toArray();
				}
				this.lastPredictedStates = states.clone();
			}
			return this.state;
		}
		
		double[][] states = new double[this.modules.size()][this.state.getDimension()];
		for(int i = 0; i < this.modules.size(); ++i){
			double[] lastPrediction;
			if(this.useBlendedState){
				lastPrediction = this.state.toArray();
			}else{
				lastPrediction = this.lastPredictedStates[i];
			}
			states[i] = this.modules.get(i).predict(lastPrediction, measurment);
		}
		if(!this.useBlendedState){
			this.lastPredictedStates = states.clone();
		}
		BlockRealMatrix statesM = new BlockRealMatrix(states).transpose();
		this.state = statesM.operate(this.weight);
		
//		System.out.println("statesM sieht so aus: "+ Arrays.deepToString(statesM.getData()));
//		System.out.println("Gewicht sieht so aus: "+ Arrays.toString(this.weight.toArray()));		
//		System.out.println("state sieht so aus: "+ Arrays.toString(this.state.toArray()));
//		System.out.println("-----------------------------");
		
		//change weight
		this.weight = new ArrayRealVector(this.trigger.changeWeight(this.weight.toArray()));
		//change weight
//		if(this.counting){
//			--this.counter;
//			if(this.counter == 0){
//				this.counting = false;
//			}
//		}else{
//			this.changeWeight();
////			this.weight = this.changeOnWeight.operate(this.weight);
//		}
		//if weight is under threshold, stop working and just give the first prediction (for us only the gps-coordinates) back
		if(Math.abs(this.weight.getEntry(0)-1) < this.threshold){
		    this.trigger = null;
//			this.counter = 0;
//			this.counting = false;
		}
		return this.state;
	}
	
	@SuppressWarnings("unused")
        private void changeWeight(){
		this.weight = this.changeOnWeight.operate(this.weight);
	}
	
	public void changeInitialWeight(double[] w) throws IllegalArgumentException{
		
		double sum = 0;
		//check the sum
		for(int i = 0; i < w.length; ++i){
			sum += w[i];
		}
		if(sum != 1){
			throw new IllegalArgumentException("Invalid Argument: the sum of all values has to be 1");
		}
		
		this.initialWeight = new ArrayRealVector(w);
	}
	
	public int getDim(){
		return this.state.getDimension();
	}
	
	public int getSize(){
		return this.modules.size();
	}
	
	public boolean isWorking(){
		return this.trigger != null;
	}
	
}
