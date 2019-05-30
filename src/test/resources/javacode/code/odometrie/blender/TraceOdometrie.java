package com.dcaiti.traceloader.odometrie.blender;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.filter.KalmanFilter;

import com.dcaiti.traceloader.Trace;
import com.dcaiti.traceloader.TraceLoaderAccumulo2;
import com.dcaiti.traceloader.TracePoint;
import com.dcaiti.traceloader.TraceTools;
import com.dcaiti.utilities.KMLWriter;
import com.dcaiti.utilities.Point;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.math.Vector2D;

public class TraceOdometrie {

	public static void main(String args[]){		
		testKalman();
	}
	
	/** function to estimate the precision of the GPS-Data of the trace
	 * 
	 * @param trace 
	 * @return 
	 */
	public static float getTracePrecision(Trace trace){
		
		return 0;
	}
	
	private static DefaultProcessModel getProcessModel(int dim,double dt, double[] initialState){
		
		dim = 2*dim;
	
		double[][] arrST = new double[dim][dim];
		double[] arrC = new double[dim];
		double[] arrD = new double[dim];
		
		for(int i = 0; i < dim; ++i){
			for(int j = 0; j < dim; ++j){
				arrST[i][j] = 0;
			}
			arrST[i][i] = 1;
			arrD[i] = 1;
		}
		arrST[0][2] = dt;
		arrST[1][3] = dt;
		
		if(dim > 4){
			arrST[2][4] = dt;
			arrST[3][5] = dt;
		}
		
		arrC[0] = 1;
		arrC[1] = 1;
		arrC[2] = 1;
		arrC[3] = 1;
		

//		System.out.print(Arrays.deepToString(arrST));
		
		RealMatrix stateTransition = new BlockRealMatrix(arrST);		//A
		RealMatrix control = new DiagonalMatrix(arrD);					//B
		RealMatrix processNoise = new DiagonalMatrix(arrD);				//Q
		RealVector initialStateEstimate = new ArrayRealVector(initialState);
		RealMatrix initialErrorCovariance = new DiagonalMatrix(arrD);	//P -> ID
		
		return new DefaultProcessModel(stateTransition, control, processNoise, initialStateEstimate, initialErrorCovariance);
	}
	
	private static DefaultMeasurementModel getMeasurementModel(int dim){
		
		dim= 2*dim;
		
		double[] arr = new double[dim];
		double[] arrN = new double[dim];
		for(int i = 0; i < dim; ++i){
			arr[i] = 1;
		}
		
		arrN[0] = 20;
		arrN[1] = 20;
		arrN[2] = 1;
		arrN[3] = 1;
		
		RealMatrix measMatrix = new DiagonalMatrix(arr);
		RealMatrix measNoise = new DiagonalMatrix(arrN);
		
		return new DefaultMeasurementModel(measMatrix,measNoise);
	}
	
	public static Trace KalmanFilter(double dt, Trace trace, boolean delete){
		
		int dim = 2;
		
		List<TracePoint> tracePtsBefore = trace.getTracePts();
		List<TracePoint> tracePts = new ArrayList<TracePoint>();
		
		
		if(delete){
			//delete all TracePoints with speed=0:  -> only a slight improvement
			for(TracePoint tp : tracePtsBefore){
				if(tp.getSpeed() > 2){
					tracePts.add(tp);
				}
			}
		}else{
			tracePts = tracePtsBefore;
		}
		
		TracePoint tpStart = tracePts.get(0);
		
		Trace test = new Trace();
		
		AbstractVehicleModel model = tpStart.getAbstractVehicleModel();
		double[] stateStart = getStateFromModel(model,dim);
		
		double[] state;
		
		KalmanFilter filter = new KalmanFilter(getProcessModel(dim,dt,stateStart),getMeasurementModel(dim));
		
		filter.predict();
		state = filter.getStateEstimation();
		test.add(buildNewTracePoint(tpStart,state[0],state[1]));
		
		tracePts.remove(0);
		for(TracePoint tp : tracePts){
			filter.correct(getStateFromModel(tp.getAbstractVehicleModel(),dim));
			filter.predict();
			state = filter.getStateEstimation();
			test.add(buildNewTracePoint(tp,state[0],state[1]));
		}
		
		return test;
	}
			
	public static double[] getStateFromModel(AbstractVehicleModel model,int dim){
		double[] state = new double[2*dim];
		state[0] = model.getPosition().getX();
		state[1] = model.getPosition().getY();
		if(dim > 1){
			state[2] = model.getSpeed().getX();
			state[3] = model.getSpeed().getY();
			if(dim > 2){
				state[4] = model.getAcceleration().getX();
				state[5] = model.getAcceleration().getY();
			}
		}
		return state;
	}
	
	public static Vector2D HeadingToVector(double heading){
		//heading is a float between 0 and 2Pi. If Heading is 0, the car looks to East or North?
		return new Vector2D(Math.cos(heading),Math.sin(heading));
	}
	
	public static double VectorToHeading(Vector2D vec){
		return vec.angle();
	}
	
	
	public static TracePoint buildNewTracePoint(TracePoint tp, double x, double y){
		return new TracePoint.Builder(tp).coordinate(new Coordinate(x,y)).build();
	}
	
	public static TracePoint guessNextTracePoint(TracePoint tp){
		
		double v = tp.getSpeed(); // in km/h
		v = v/3.6; // in m/s
		double h = tp.getHeading();	//in rad (0-2Pi)
		Coordinate coor = tp.getCoor();
		double x = coor.x;
		double y = coor.y;
		double h1 = Math.cos(h);
		double h2 = Math.sin(h);
		double w = tp.getYawRate();		//rad/s
		
		double dt = 1;		//in seconds
		
		
		//the step:
		x = x+h1*v*dt;
		y = y+h2*v*dt;
		h = h+w*dt;
		
		Coordinate new_coor = new Coordinate(coor);
		new_coor.x = x;
		new_coor.y = y;
		
		return new TracePoint.Builder(tp)
				.speed(v)
				.heading(h)
				.coordinate(new_coor)
				.build();
	}
	
	public static Trace createRandomTrace(){
		Trace trace = new Trace();
		TracePoint tp = new TracePoint.Builder(0,new Point(30,40)).build();
		double speed = 10;
		double heading = 0;
		double h1 = Math.cos(heading);
		double h2 = Math.sin(heading);
		
		tp = new TracePoint.Builder(tp).speed(speed).heading(heading).build();
		
		trace.add(tp);
		
		for(int i = 0; i < 50; ++i){
			trace.add(new TracePoint.Builder(tp).coordinate(new Coordinate(tp.getCoor().x + h1*speed*i + rand(10,0),tp.getCoor().y + h2*speed*i + rand(10,0))).build());
		}
		return trace;
	}
	
	public static double rand(double r,double mid){
		return Math.random()*r*2 - r + mid;
	}
	
	public static void KalmanScenario1(){
		TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2(false, false);

	       String vehId = "cmtcde2u062_32006";
	       loader.loadDaimlerTracesByVehicleId(vehId);
	       TraceTools.visualizeTraces(loader.traces, "results/traces2.kml", KMLWriter.BLUE);
	 
	       List<Trace> list = new ArrayList<Trace>();
	       List<Trace> pairs = new ArrayList<Trace>();
	       
	       for (Trace trace : loader.traces){
	           System.out.println("vehId ---> " +trace.getVehicleId());
	           TracePoint tp_old = null;
	           Trace traceEstimated = new Trace();
	           for (TracePoint tp : trace.tracePts){
//	              look at the diff between real and estimated heading and coor
	        	   if(tp_old != null){
	        		   TracePoint tp_estimated = guessNextTracePoint(tp_old);
	        		   traceEstimated.add(tp_estimated);
	        		   Trace pair = new Trace();
	        		   pair.add(tp_estimated);
	        		   pair.add(tp);
	        		   pairs.add(pair);
	        	   }
	        	   tp_old = tp;
//	        	   System.out.println(tp.toString());
	           }
	          
	           list.add(traceEstimated);
	           
	       }
	       
	       TraceTools.visualizeTraces(list, "results/est_traces2.kml",KMLWriter.GREEN);
	       TraceTools.visualizeTraces(pairs,"results/pair_traces2.kml",KMLWriter.RED);
//	       TraceTools.visualizeTraces(loader.traces, "results/traces.kml", KMLWriter.BLUE);
	}
	
	public static void KalmanScenario2(){
		TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2(false, false);
	    //String vehId = "cmtcde2u062_32006";
		String vehId = "Section_20150319_234701";
	    loader.loadDaimlerTracesByVehicleId(vehId);
	    TraceTools.visualizeTraces(loader.traces, "results/original_traces.kml", KMLWriter.BLUE);
//	    TraceTools.visualizeTracePts(loader.traces, "results/tracepoints.kml");
	    
//	    Trace test = deleteStopError(loader.traces.get(0)); 
	    Trace test = KalmanFilter(1,loader.traces.get(0),true);
	    
	    List<Trace> traces = new ArrayList<Trace>();
	    traces.add(test);
	    TraceTools.visualizeTraces(traces,"results/odometrie_stopErrorTraces.kml", KMLWriter.RED);
//	    TraceTools.visualizeTracePts(traces, "results/odometrie_tracepoints.kml");
	}
	
	public static void testKalman(){
		
		List<Trace> traces = new ArrayList<Trace>();
		traces.add(createRandomTrace());
		
	    TraceTools.visualizeTraces(traces, "results/kalman/test_original2.kml", KMLWriter.BLUE);
//	    TraceTools.visualizeTracePts(traces, "results/tracepoints.kml");
	     
	    Trace test = KalmanFilter(1,traces.get(0),false);
	    
	    List<Trace> ftraces = new ArrayList<Trace>();
	    ftraces.add(test);
	    TraceTools.visualizeTraces(ftraces,"results/kalman/test_kalman2.kml", KMLWriter.RED);
//	    TraceTools.visualizeTracePts(traces, "results/odometrie_tracepoints.kml");
	}
	
	
		
	public static Trace deleteStopError(Trace trace){
		
		boolean stopped = false;
		boolean calculating = false;
		List<TracePoint> tracePts = trace.getTracePts();
		Trace improved = new Trace();
		TracePoint temp;
//		List<Integer> startedBy = new ArrayList<Integer>();
		TracePoint stoppedBy = null;
		
		for(int i = 0; i < trace.size(); ++i){
			temp = tracePts.get(i);
			if(temp.getSpeed() == 0){
				if(stopped == false){
					stoppedBy = temp;
				}
				stopped = true;
			}else{
//				improved.add(temp); //and do nothing else here -> speed=0 is deleted
				if(stopped || calculating){
					stopped = false;
//					startedBy.add(improved.size()-1);
					if(Math.abs(temp.getHeading() - stoppedBy.getHeading()) < 0.2){
						//caluclate
						calculating = true;
						TracePoint estimated = buildNewTracePoint(
								temp,
								Math.cos(stoppedBy.getHeading())*temp.getSpeed() + stoppedBy.getCoor().x,
								Math.sin(stoppedBy.getHeading())*temp.getSpeed() + stoppedBy.getCoor().y);
						improved.add(estimated);
						//calculate = false if paths intersect
						
					}else{
						calculating = false;
						improved.add(temp);
						//end calculating, car does a turn
					}
				}else{
					improved.add(temp);
				}
				
			}
		}
		
		return improved;
	} 
	
	/** Test if the segments AB and CD intersects
	 * 
	 * @param boolean bool: true if you want to test if a line intersects an endpoint - if you can do not use this option
	 */
	public static boolean crosses(Coordinate a, Coordinate b, Coordinate c, Coordinate d, boolean bool){
		
		double aN = (d.x - c.x)*(a.y -c.y) - (d.y -c.y)*(a.x - c.x);
		double bN = (d.x - c.x) * (b.y - c.y) - (d.y - c.y) * (b.x - c.x);
		double cN = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
		double dN = (b.x - a.x) * (d.y - a.y) - (b.y - a.y) * (d.x - a.x);
		
		boolean aSide = aN > 0;
		boolean bSide = bN > 0;
		boolean cSide = cN > 0;
		boolean dSide = dN > 0;
				
		if(bool){
//			System.out.println(aN);
//			System.out.println(bN);
//			System.out.println(cN);
//			System.out.println(dN);
			if(aN * bN * cN * dN == 0){
				//point with 0 is on the (infinite) line -> test if the point is on the segment!
				if(aN == 0){
					return (c.x <= a.x && a.x <= d.x) || (c.x >= a.x && a.x >= d.x);
				}else if (bN == 0){
					return (c.x <= b.x && b.x <= d.x) || (c.x >= b.x && b.x >= d.x);
				}else if(cN == 0){
					return (a.x <= c.x && c.x <= b.x) || (a.x >= c.x && c.x >= b.x);
				}else if(dN == 0){
					return (a.x <= d.x && d.x <= b.x) || (a.x >= d.x && d.x >= b.x);
				}
			}
		}
			
//		System.out.println(aSide);
//		System.out.println(bSide);
//		System.out.println(cSide);
//		System.out.println(dSide);
		
		return aSide != bSide && cSide != dSide;
	}
	
	public static boolean crosses(Coordinate a, Coordinate b, Coordinate c, Coordinate d){
		return crosses(a,b,c,d,false);
	}
	
	
	
	
	
}
