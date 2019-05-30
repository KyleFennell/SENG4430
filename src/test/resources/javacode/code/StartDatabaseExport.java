package com.dcaiti.traceloader;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.dcaiti.utilities.FileHandler;

import jline.internal.Log;

/**
 * Generates csv file for ANN dataset.
 * @author Marius Hauschild
 *
 */
public class StartDatabaseExport {

    public static void main(String[] args) {

        startExtractionForDaimlerCityANNData();
        
    }

    private static void startExtractionForDaimlerCityANNData() {
        TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2(false, false);
        
        FileHandler.writeLogDataToCSV("daimlerCity", verifySensorValues(loader.loadDaimlerTracesByItefId(53800)));
//        FileHandler.writeLogDataToCSV("daimlerCountry", verifySensorValues(loader.loadDaimlerTracesByItefId(53805)));
//        FileHandler.writeLogDataToCSV("daimlerMotorway", verifySensorValues(loader.loadDaimlerTracesByItefId(53806)));
    }
    
    private static Map<String, Map<Long, Map<String, String>>> verifySensorValues(Map<String, Map<Long, Map<String, String>>> data) {
        for (Map.Entry<String, Map<Long, Map<String, String>>> vehicle : data.entrySet()) {
            for (Map.Entry<Long, Map<String, String>> time : vehicle.getValue().entrySet()) {
                for(Iterator<Map.Entry<String, String>> it = time.getValue().entrySet().iterator(); it.hasNext(); ) {
                    Entry<String, String> next = it.next();
                    
                    TracePointFieldProperties properties = new TracePointFieldProperties(next.getKey());
                    if (!properties.isSensorValueValid(Double.parseDouble(next.getValue()))) {
                        it.remove();
                    }
                }
            }
        }
        return data;
    }
    
}
