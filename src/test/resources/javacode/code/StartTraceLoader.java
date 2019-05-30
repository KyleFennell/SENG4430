package com.dcaiti.traceloader;

import com.dcaiti.utilities.KMLWriter;
import com.dcaiti.utilities.Util;

public class StartTraceLoader {

    public static void main(String[] args)  {
        
        TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2(false, false);

        loader.loadDaimlerTracesByItefId(53200, "foba_daimler_traces_test_1_1"); 
      
//      String[] tileIds = {"16/34402/22594"};
//      String[] tileIds = {"17/68805/45184"};
      String[] tileIds = {"19/275219/180734", "19/275220/180734"};
//      String[] tileIds = {"18/137610/90367", "18/137610/90368", "18/137610/90369"};
//        String[] tileIds = {"18/137610/90371", "18/137610/90370", "18/137609/90370", "18/137609/90371"};
//      loader.loadDaimlerTracesByTileIds(tileIds, 0); 
      
        //only one trace
//        String vehId = "cmtcde2u062_25717";
//        String vehId = "cmtcde2u062_25798";
        String vehId = "cmtcde2u062_31896";  //with outliers roadBorderRT
//        String vehId = "cmtcde2u62_95674";
//        loader.loadDaimlerTracesByVehicleId(vehId,"foba_daimler_traces_test_50_1");


        //e.g. for checking attributes
        int totalTraces = 0;
        int totalPoints = 0;
        for (Trace trace : loader.traces){
            totalTraces++;
            totalPoints += trace.tracePts.size();
            System.out.println("vehId ---> " +trace.getVehicleId());
            for (TracePoint tp : trace.tracePts){
              System.out.println(tp.toString());
////                if (tp.speed == 0.0) {
////                    System.out.println(tp.toString());
////                }
////                if (tp.aldwLaneLtrlDistLt > 100.0) {
////                    System.out.println(tp.getParentTrace() + " at: " + Util.getTime(tp.time) +": laneBorderLt: "+tp.aldwLaneLtrlDistLt);
////                }
////                if (tp.aldwLaneLtrlDistRt > 100.0) {
////                    System.out.println(tp.getParentTrace() + " at: " + Util.getTime(tp.time) +": laneBorderRt: "+tp.aldwLaneLtrlDistRt);
////                }

            }
        }
        
        System.out.println("totalTraces: " +totalTraces +" totalPoints: " +totalPoints );
        //visualize
        TraceTools.visualizeTraces(loader.traces, "results/traces.kml", KMLWriter.GREEN);
//        TraceTools.visualizeTracePts(loader.traces, "results/tracePts.kml");
       
    }
    

 

}
