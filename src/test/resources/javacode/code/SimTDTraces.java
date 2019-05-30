package com.dcaiti.traceloader;

import java.io.IOException;
import java.util.Map;

import com.dcaiti.bigdataconnector.helper.Helper;

public class SimTDTraces {
    
    
    public static void main(String[] args) throws IOException {
        
        loadSimTDTraces();
      
//        readSimTDTraces("BAB");
  }

    /**
     * save simTD traces to local cache
     */
    static void loadSimTDTraces(){
        
        // TestSite1: SIM_TD traces Frankfurt
//         String[] tileIDs = {"16/34355/22187", "16/34355/22188", "16/34356/22187", "16/34356/22188", "16/34356/22189", "16/34356/22190"};
                 
//        Autobahn
        String[] tileIDs = {"16/34346/22208"}; String s = "autobahn";//, "16/34342/22168", "16/34341/22169", "16/34342/22169"};
        
//        Stadt
//        String[] tileIDs = {"16/34342/22202", "16/34342/22201"}; String s = "stadt";
        
//        Landstrasse an der stadt (44)
//        String[] tileIDs = {"17/68688/44403", "17/68688/44404"}; String s = "land";
        
         TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2();
         loader.loadSimTDTracesByTileIds(tileIDs, s);
//         ArrayList<Trace> originalTraces = loader.traces;
         
//         Trace.writeTraces(originalTraces, "simTDTraces2.kml", KMLWriter.GREEN);
         
         
         
     }
     
    /**
     * read simTD traces from local cache
     * @param key
     */
     static void readSimTDTraces(String key) {
         
         Map<String, Map<Long, Map<String, String>>> result = Helper.loadFromCache(key);
         
         for (String vehicleID : result.keySet()) {
             System.out.println("--------------->>>> VehicleID: "+ vehicleID);
           Map<Long, Map<String, String>> veh = result.get(vehicleID);
     
           for (long timestamp : veh.keySet()) {
               System.out.println("------>>>> time: "+ timestamp);
               Map<String, String> probeData = veh.get(timestamp);
               
               for (String str : probeData.keySet()){
                   System.out.println(str +": "+probeData.get(str));
               }
               
           }
         }

        
     }
  

}
