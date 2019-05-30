package com.dcaiti.traceloader;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * TODO this class is located here, since the Tile class is in this package and circle 
 * dependencies have to be avoided!!! put Tile in a new package to TraceLoader component!
 * put the Statistics package in TraceLoader; change the constructor of TileStatistics to ArrayList<Trace>
 * check every single tracePoint; use 50 hz values
 * since it is too much data to load at once, at first all vehicle ids are determined and 
 * then the analysis is performed on sinle traces 
 * @author bkw
 *
 */
public class TracePointVerification {
    
   public static void main(String[] args) {
       
       List<Tile> myTiles = TileArea.getTiles(16, TileArea.Area.BOEBLINGEN);

       TracePointVerification verify = new TracePointVerification();
       List<Trace> traces = verify.getAllTraces4Tiles(myTiles);
       verify.printInFile(traces);
   
 
    }
    
    private HashMap<Field, Counter> fieldCapacity = new HashMap<Field, Counter>();
    private boolean onlyNewTraces = false; 

    private List<Trace> getAllTraces4Tiles(List<Tile> myTiles) {
    
        List<Trace> allTraces = new ArrayList<Trace>();

        for (Tile currentTile : myTiles) {            
            List<Trace> traces = currentTile.getTraces();
            for (Trace trace : traces) {
                if (!allTraces.contains(trace)) allTraces.add(trace);
            }
        }
        System.out.println("No of traces: "+allTraces.size());
        return allTraces;

    }
    
    private void printInFile(List<Trace> traces) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter("Traces_Boeblingen.csv"));
            
            for (Trace trace : traces) {
                out.println(trace.getVehicleId());

            }
            out.flush();
            out.close();


        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    
    
    @SuppressWarnings("unused")
    private void verifyTraces(List<Trace> traces) {
 
            int aldw = 0;
            int distronic = 0;
            int nullSpeed = 0;
            int wgsHead = 0;
            int totalTraces = 0;
            int oldTraces = 0;

            for (Trace trace : traces) {
                //only do for new traces 
                if (onlyNewTraces && !trace.getVehicleId().startsWith("cmt")){
                    oldTraces++;
                    continue;
                }
                totalTraces++;
                if (trace.containsALDW) aldw++;
                if (trace.containsDISTRONIC) {
                    distronic++;
                }
                else {
                    System.out.println("NO DISTRONIC: "+trace.getVehicleId());
                }

                if (trace.containsNullSpeed) nullSpeed++;
                if (trace.containsWgsHead) {
                    wgsHead++;
                } 
                else {
                    System.out.println("NO WGSHEAD: "+trace.getVehicleId());
                }
                
                //and now the single tracepoints
                TracePoint last = null;
                for (TracePoint tp : trace.tracePts) {
                    verifyTracePoint(tp, last);
                    last = tp;
                }
            }
            
            if (onlyNewTraces) System.out.println("OldTraces: "+oldTraces);
            System.out.println("TotalTraces: "+ totalTraces + " ALDW: " +aldw 
                    + " DISTRONIC: " +distronic + " nullSpeed: " +nullSpeed
                    + " GPS heading: " +wgsHead);
            
            printCapacity();
                     
    }
    /**
     * each tracePoint consists of 21 attributes; therefrom 18 double
     * @param tp
     * @param last
     */
    private void verifyTracePoint(TracePoint tp, TracePoint last) {
        
        for (Field f : tp.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            if (f.getType().isPrimitive()) {
                if (f.getType().equals(double.class)) { // include only double
                    try {
                        Counter val;
                        if (this.fieldCapacity.containsKey(f)) {
                            val = fieldCapacity.get(f);
                        } else {
                            val = new Counter();
                        }
                        val.total++;
                        if ((f.get(tp).equals(Double.NaN))) {
                            val.nan++;
                        } else {
                            if (last != null) {
                                boolean ok = checkPlausibility(f, tp, last);
                                if (!ok) val.unusual++;
                            }
                        }
                        this.fieldCapacity.put(f, val);
                        
                      
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                       e.printStackTrace();
                    }
                }
            }
        }
    }
    
    private boolean checkPlausibility(Field f, TracePoint tp, TracePoint last) {
        
        double eps = 0.1;   //TODO eps dependent on field
        double val1;
        double val2;
        try {
            val1 = (Double)f.get(tp);
            val2 = (Double)f.get(last);
            if (Math.abs(val1 - val2) < eps) return true;

        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return false;
    }
    
    private void printCapacity(){
        
        System.out.println("FieldCapacity ... ");
        try {
            for (Map.Entry<Field, Counter> entry : this.fieldCapacity.entrySet()) {
               System.out.println(entry.getKey().getName() +": " + entry.getValue().toString());
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }


        
    }
    
    class Counter{
        int total = 0;
        int nan = 0;
        int unusual = 0;
        
        public String toString(){
            
            return "total: " +total + " NaN: " +nan +" unusual: "+unusual;
        }
               
    }

}
