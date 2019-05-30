package com.dcaiti.traceloader.odometrie.blender;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import com.dcaiti.traceloader.Trace;
import com.dcaiti.traceloader.TraceLoaderAccumulo2;
import com.dcaiti.traceloader.TraceTools;
import com.dcaiti.traceloader.odometrie.LineChart;
import com.dcaiti.traceloader.odometrie.TraceAnalyser;
import com.dcaiti.traceloader.odometrie.TraceFilter;
import com.dcaiti.traceloader.odometrie.TraceFilter.Output;
import com.dcaiti.utilities.KMLWriter;

public class TestTraceBlender {
    
    public static void main(String[] args) {
//       testDetermenisticTrace();
//        testErrorFunction();
//        test();
//        visualizeAll();
        testOptimisingModule();
   }
    
   static Trace loadTrace(String vehId){
       TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2(false, false);
       loader.loadDaimlerTracesByVehicleId(vehId);
       return loader.traces.get(0);
   }
   
   static Trace loadTrace(){
       return loadTrace("Section_20150319_234701");
   }
   
   public static void testOptimisingModule(){
       //print error function
       //print trace belonging to param
       String vehId = "Section_20150319_234701";
       Trace trace = loadTrace(vehId);
       
//       TraceFilter filter = new TraceFilter(trace);
//       trace = filter.correct();
       
       trace = TraceFilter.correct(trace);
       
//       trace = TraceFilter.correctSensorLag(trace);
//       trace = TraceFilter.correctTrustworthy(trace);
//       trace = TraceFilter.correctSpeedError(trace);
       
       int index = 100;
       
       TraceBlender blend = new TraceBlender();
       List<double[]> list = blend.optimiseModule(1, index, trace);
       Trace det = blend.getPredTrace(1, index, trace, blend.blend.modules.get(1).getParam(), 50);
       
       LineChart chart = new LineChart("Optimising Module");
       double paramFromError = visualizeErrorAsLineChart(trace,new double[]{0,0.01},chart);
       Trace det_brute = blend.getPredTrace(1, index, trace, new double[]{0,paramFromError}, 50);
       
       double[] values = new double[list.size()];
       double[] step = new double[list.size()];
       double[] error = new double[list.size()];
       double highestStep = 0;
       double highestError = 0;
       
       for(int i = 0; i < list.size(); ++i){
           values[i] = list.get(i)[0];
           step[i] = list.get(i)[1];
           error[i] = list.get(i)[2];
           if(step[i] > highestStep){
               highestStep = step[i];
           }
           if(error[i] > highestError){
               highestError = error[i];
           }
       }
       
       double[] norm_step = new double[step.length];
       double[] norm_error = new double[error.length];
       for(int i = 0; i < norm_step.length; ++i){
           norm_step[i] = step[i] / highestStep;
           norm_error[i] = error[i] / highestError;
       }
       
       chart.addNonLinearData(norm_step,values,"Algo-searching");
       chart.addNonLinearData(norm_error, values, "Algo-Error");
       chart.setLine(true, 2);
       chart.initChart("Error","Drift");
       chart.showChart();
//       chart.pack();
//       RefineryUtilities.centerFrameOnScreen(chart);
//       chart.setVisible(true);
       
       TraceTools.visualizeTrace(det,"results/optimising/"+vehId+"/det_optim.kml",KMLWriter.GREEN);
       TraceTools.visualizeTrace(det_brute,"results/optimising/"+vehId+"/det_brute.kml",KMLWriter.RED);
       TraceTools.visualizeTrace(trace,"results/optimising/"+vehId+"/orig.kml",KMLWriter.BLUE);
   }
   
   /**
    * 
    * @param list
    * @param bitmask flag speedchart|errorchart|traceprinting
    */
   public static void visualizeAll(String vehId,List<int[]> list, int flag){
       if(vehId == null){
           vehId = "Section_20150319_234701";
       }
       Trace trace = loadTrace(vehId);
       
       int showSpeed = 4;
       int showError = 2;
       int showTrace = 1;
       
       TraceFilter filter = new TraceFilter(trace);
       
//       trace = TraceFilter.correctSensorLag();
//       trace = TraceFilter.correctTrustworthy();
       filter.correctSensorLag();
       
       if((flag & showSpeed) == showSpeed){
           trace = filter.correctSpeedError(TraceFilter.Output.SHOW);
       }else{
           trace = filter.correctSpeedError(EnumSet.noneOf(TraceFilter.Output.class));
       }
       
              
       MathBlenderModule det = new MathBlenderDetermenisticModel();
       int index = 100;
       int length = 50;
       
       double[] range = new double[]{-1,1};
       int steps = 10000;
       double step = (range[1] - range[0]) / steps;
       

       
       double[] val = new double[steps];
       for(int i = 0; i < val.length; ++i){
           val[i] = step*i + range[0];
       }
       
       if(list == null){
           list = new ArrayList<int[]>();
           list.add(new int[]{1,0,0});
           list.add(new int[]{0,1,0});
           list.add(new int[]{0,0,1});
           list.add(new int[]{2,1,0});
       }

       ArrayList<Double> drift = new ArrayList<Double>();
       
       LineChart chart = new LineChart("Fehlerfunktionen");
       
       for(int j = 0; j < list.size(); ++j){
           double[] err = new double[val.length];
           for(int i = 0; i < err.length; ++i){
               err[i] = TraceBlender.error(det, index, trace, new double[]{0,val[i]}, length,list.get(j));
           }
           
           Point2D.Double best = new Point2D.Double(err[0],val[0]);
           Point2D.Double worst = new Point2D.Double(err[0],val[0]);
           
           for(int i = 0; i < err.length; ++i){
               if(err[i] < best.getX()){
                   best = new Point2D.Double(err[i],val[i]);
               }else if(err[i] > worst.getX()){
                   worst = new Point2D.Double(err[i], val[i]);
               }
           }
           
           System.out.println(best.toString());
           drift.add(best.getY());
           
           double[] norm_err = new double[err.length];
           if(list.size() > 1){
               for(int i = 0; i < norm_err.length; ++i){
                   norm_err[i] = err[i] / worst.getX();
               }
           }else{
               //do not normalize if only one error-function is displayed
               norm_err = err;
           }

           chart.addLinearData(norm_err, range[0], step ,"Fehler: "+Arrays.toString(list.get(j)));
       }
       
       ArrayList<Color> color = new ArrayList<Color>();
       color.add(Color.RED);
       color.add(Color.BLACK);
       color.add(Color.GREEN);
       color.add(Color.ORANGE);
      
       if((flag & showTrace) == showTrace){
           //print traces
           trace = trace.subTrace(index, index+length);
           TraceTools.visualizeTrace(trace,"results/error/"+vehId+"/orig.kml",KMLWriter.BLUE);
           
           for(int i = 0; i < list.size(); ++i){
               Trace det_trace = TraceAnalyser.detModelTrace(trace, new double[]{0,drift.get(i)},1);
//               String hex = Integer.toHexString(color.get(i).getRGB()).substring(2);
               String hex = String.format("%02x%02x%02x%02x", color.get(i).getAlpha() ,color.get(i).getBlue(), color.get(i).getGreen(), color.get(i).getRed());
               TraceTools.visualizeTrace(det_trace,"results/error/"+vehId+"/det_"+Arrays.toString(list.get(i))+".kml",hex);
           }
       }
       
       if((flag & showError) == showError){
           chart.setAllColor(color);
           chart.initChart("Error - normalised","Drift");
           chart.showChart();
       }
   }
   
   public static double visualizeErrorAsLineChart(Trace trace, double[] range, LineChart chart){
       MathBlenderModule det = new MathBlenderDetermenisticModel();
       int index = 200;
       int length = 50;
       
       if(range == null){
           range = new double[]{-1,1};
       }
       int steps = 10000;
       double step = (range[1] - range[0]) / steps;
       

       
       double[] val = new double[steps];
       for(int i = 0; i < val.length; ++i){
           val[i] = step*i + range[0];
       }
       
       double[] err = new double[val.length];
       for(int i = 0; i < err.length; ++i){
           err[i] = TraceBlender.error(det, index, trace, new double[]{0,val[i]}, length);
       }
       
       Point2D.Double best = new Point2D.Double(err[0],val[0]);
       Point2D.Double worst = new Point2D.Double(err[0], val[0]);
       
       for(int i = 0; i < err.length; ++i){
           if(err[i] < best.getX()){
               best = new Point2D.Double(err[i],val[i]);
           }else if(err[i] > worst.getX()){
               worst = new Point2D.Double(err[i], val[i]);
           }
       }
       
       System.out.println(best.toString());
       
       if(chart == null){
           chart = new LineChart("Line Chart");
           chart.addLinearData(err,"Det-Model");
           chart.initChart("Error","Drift");
           chart.showChart();
       }else{
           //normilse error
           double[] norm_err = new double[err.length];
           for(int i = 0; i < norm_err.length; ++i){
               norm_err[i] = err[i] / worst.getX();
           }
           chart.addLinearData(norm_err, range[0], step, "Error Det-Model");
       }
       return best.getY();
   }
   
   static void test(){
       TraceBlender blend = new TraceBlender();
       Trace trace = loadTrace();
       
//       blend.optimiseModule(1, 10, trace);
//       blend.test(1, 300, trace);
       blend.test(1, 200, trace);
   }
       
   static void testDetermenisticTrace(){
       TraceBlender blend = new TraceBlender();
       
       TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2(false, false);
       String vehId = "Section_20150319_234701";
       loader.loadDaimlerTracesByVehicleId(vehId);
       TraceTools.visualizeTraces(loader.traces, "results/deterministic/TraceBlender/orig.kml", KMLWriter.BLUE);
       
       Trace trace = loader.traces.get(0);
       Trace det = blend.getPredTrace(1, 10, trace,new double[] {0.1,0}, 100);
       
       TraceTools.visualizeTrace(det, "results/deterministic/TraceBlender/det.kml", KMLWriter.RED);
   }
   
   static void testErrorFunction(){
       Trace trace = loadTrace();
       TraceBlender blend = new TraceBlender();
       //use some traces and their error-value
       ArrayList<Trace> det = new ArrayList<Trace>();
       
//       det.add(blend.getPredTrace(1, 10, trace,new double[] {0.01, 4.00}, 200));
//       det.add(blend.getPredTrace(1, 10, trace,new double[] {0.01, 6.264}, 200));
//       det.add(blend.getPredTrace(1, 10, trace,new double[] {0.01, 6.294}, 200));
       
       det.add(blend.getPredTrace(1, 300, trace,new double[] {0, 0}, 50));
       det.add(blend.getPredTrace(1, 300, trace,new double[] {0, 0.205}, 50));
       det.add(blend.getPredTrace(1, 300, trace,new double[] {0.2, 0.05}, 50));
       
       ArrayList<Trace> orig = new ArrayList<Trace>();
       
//       orig.add(blend.getPredTrace(0, 10, trace,new double[] {0.01, 4.00}, 200));
//       orig.add(blend.getPredTrace(0, 10, trace,new double[] {0.01, 6.264}, 200));
//       orig.add(blend.getPredTrace(0, 10, trace,new double[] {0.01, 6.294}, 200));
       
       orig.add(blend.getPredTrace(0, 300, trace,new double[] {0, 0}, 50));
       orig.add(blend.getPredTrace(0, 300, trace,new double[] {0, 0.205}, 50));
       orig.add(blend.getPredTrace(0, 300, trace,new double[] {0.2, 0.05}, 50));
       
       for(int i = 0; i < det.size(); ++i){
           System.out.println("Fehler ist: "+TraceBlender.errorBetweenTraces(orig.get(i), det.get(i),new int[]{1,0,0}));
           String color = "";
           switch(i){
           case 0: case 3: color = "#000000"; break;
           case 1: case 4: color = KMLWriter.RED; break;
           case 2: case 5: color = KMLWriter.GREEN; break;
           default: color = KMLWriter.GRAY;
           }
           TraceTools.visualizeTrace(det.get(i), "results/deterministic/TraceBlender/special/1.1/det_"+i+".kml", color);
//           TraceTools.visualizeTracePts(det.get(i),"results/deterministic/TraceBlender/special/1.1/det_TPs_"+i+".kml" );
           TraceTools.visualizeTrace(orig.get(i), "results/deterministic/TraceBlender/special/1.1/orig_"+i+".kml", KMLWriter.BLUE);
//           TraceTools.visualizeTracePts(orig.get(i),"results/deterministic/TraceBlender/special/1.1/orig_TPs_"+i+".kml" );
       }
   }
    
}
