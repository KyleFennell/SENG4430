package com.dcaiti.traceloader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dcaiti.bigdataconnector.BigDataConnector;
import com.dcaiti.bigdataconnector.Config;
import com.dcaiti.bigdataconnector.ILogDataResultIterator;
import com.dcaiti.bigdataconnector.helper.Helper;
import com.dcaiti.bigdataconnector.remote.BigDataConnectorRemote;
import com.dcaiti.traceloader.traceupgrader.ResultUpgrader;
import com.dcaiti.utilities.Util;
import com.vividsolutions.jts.geom.Coordinate;

public class TraceLoaderAccumulo2 {

    /**
     * list of all traces
     */
    public List<Trace> traces;

    /**
     */
    public BoundingBox boundingBox;

    /**
     * LogIds defined in Accumulo.
     */
    public static final String LAT = "1000000000100000001";
    public static final String LNG = "1000000000100000002";
    public static final String ALT = "100038";
    public static final String HEADING = "1000000000100000004";
    public static final String SPEED = "1000000000100000005";

    public static final String ALDW_LANE_LTR_DIST_LT = "100058";// "ALDW_LaneLtrlDist_Lt";
    public static final String ALDW_LANE_LTR_DIST_RT = "100059";// "ALDW_LaneLtrlDist_Rt";
    public static final String ALDW_LANE_MARK_TYPE_LT = "100061"; // ALDW_LaneMarkType_Lt
    public static final String ALDW_LANE_MARK_TYPE_RT = "100062"; // ALDW_LaneMarkType_Rt
    public static final String ALDW_NUM_LANE = "100056"; // ALDW_NumLane
    public static final String ALDW_LANE_NUM = "100057"; // ALDW_LaneNum
    public static final String ALDW_LANE_MARK_WIDTH_LT = "100050"; // ALDW_LaneMarkWidth_Lt
    public static final String ALDW_LANE_MARK_WIDTH_RT = "100051"; // ALDW_LaneMarkWidth_Rt
    public static final String ALDW_LANE_MARK_COL_LT = "100052"; // ALDW_LaneMarkCol_Lt
    public static final String ALDW_LANE_MARK_COL_RT = "100053"; // ALDW_LaneMarkCol_Rt

    public static final String ROAD_BORDER_LEFT = "100009";
    public static final String ROAD_BORDER_RIGHT = "100010";

    public static final String STWHL_ANGLESPEED = "100081"; 
    public static final String STWHL_ANGLE = "100082";
    public static final String STWHL_SW = "100083";
    
    public static final String YAW_RATE_OFFSET = "100000";
    public static final String YAW_RATE = "100001";
    public static final String YAW_RATE_RQ = "100024";
    public static final String VEHICLE_X_ACC = "100069";
    public static final String VEHICLE_Y_ACC = "100071";
    public static final String VEHICLE_X_ACC_OFFSET = "100070";
    public static final String VEHICLE_Y_ACC_OFFSET = "100072";
    
    public static final String PITCH_ANGLE = "100048";  //ALDW_VehPitchAngl

    
    public static final String DISP_SPEED = "100035";
    public static final String GPS_SPEED = "100046";
    
    private boolean curveOpt;
    private boolean longitudeCorrection;

    /**
     * Coordinates for the Crash Barrier generation!
     */
    public ArrayList<double[]> crashBarrierCoors;

    /**
     * Default constructor;
     * @deprecated use instead TraceLoaderAccumulo2(boolean curveOpt, boolean longitudeCorrection)
     */
    public TraceLoaderAccumulo2() {
        
        curveOpt = false;
        longitudeCorrection = false;
    }
    
    /**
     *  Constructor.
     *  
     *  The following loadTraces methods not specifying an explicit tableName load the data from the default table 
     *  specified in BDC config.
     *  
     *  default tableName = "foba_daimler_traces_1"; // contains 1 hz data from all probe data deliveries 
     *          tableName = "foba_daimler_traces_test_1_1"; //contains 1 hz data only from cmt data delivery (DD7)
     *          tableName = "foba_daimler_traces_2"; // contains 50 hz data from all deliveries excluding cmt data
     *          tableName = "foba_daimler_traces_test_50_1"; //contains 50 hz data only from cmt data delivery (DD7)
     *
     * @param curveOpt
     * @param longitudeCorrection
     */
    public TraceLoaderAccumulo2(boolean curveOpt, boolean longitudeCorrection) {
        
        this.curveOpt = curveOpt;
        this.longitudeCorrection = longitudeCorrection;
    }

    

    /**
     * Loads all Daimler traces with tracePoints lying in the specified ITEF
     * run. logIds must explicitly added. TracePoints outside these tiles are omitted. The result may be split
     * in several single traces (for example to get only the tracePoints for one
     * direction, if the car was driving in both directions) using the helper
     * class of BDC.jar
     * 
     * 
     * @param itefId
     */

    public Map<String, Map<Long, Map<String, String>>> loadDaimlerTracesByItefId(int itefId) {
        
        return loadDaimlerTracesByItefId(itefId, "foba_daimler_traces_1");

    }
 
    /**
     * Loads all Daimler traces with tracePoints lying in the specified ITEF
     * run. logIds must explicitly added. TracePoints outside these tiles are omitted. The result may be split
     * in several single traces (for example to get only the tracePoints for one
     * direction, if the car was driving in both directions) using the helper
     * class of BDC.jar
     * 
     * 
     * @param itefId
     */
    public Map<String, Map<Long, Map<String, String>>> loadDaimlerTracesByItefId(int itefId, String tableName) {

        System.out.println("--------->"+itefId);
        long start = System.currentTimeMillis();
        com.dcaiti.bigdataconnector.remote.ConfigRemote conf = new com.dcaiti.bigdataconnector.remote.ConfigRemote();
        conf.tableName = tableName;

        BigDataConnectorRemote bdcr = new BigDataConnectorRemote(conf);
        
        List<String> logIDs = new ArrayList<String>();

        logIDs.add(LAT);  //mandatory
        logIDs.add(LNG);  //mandatory
        logIDs.add(ALT);
        logIDs.add(HEADING);
        logIDs.add(SPEED);
        logIDs.add(ALDW_LANE_LTR_DIST_LT);
        logIDs.add(ALDW_LANE_LTR_DIST_RT);
        logIDs.add(ALDW_LANE_MARK_WIDTH_LT);
        logIDs.add(ALDW_LANE_MARK_WIDTH_RT);
        logIDs.add(ALDW_LANE_MARK_TYPE_LT);
        logIDs.add(ALDW_LANE_MARK_TYPE_RT);
        logIDs.add(ALDW_LANE_MARK_COL_LT);
        logIDs.add(ALDW_LANE_MARK_COL_RT);
        logIDs.add(ROAD_BORDER_LEFT);
        logIDs.add(ROAD_BORDER_RIGHT);
        logIDs.add(VEHICLE_X_ACC);
        logIDs.add(VEHICLE_Y_ACC);
        logIDs.add(VEHICLE_X_ACC_OFFSET);
        logIDs.add(VEHICLE_Y_ACC_OFFSET);
        logIDs.add(YAW_RATE);
        logIDs.add(YAW_RATE_RQ);
        logIDs.add(YAW_RATE_OFFSET);
        logIDs.add(STWHL_ANGLESPEED);
        logIDs.add(STWHL_ANGLE);
        logIDs.add(STWHL_SW);
        logIDs.add(PITCH_ANGLE);
        logIDs.add(DISP_SPEED);
        
        Map<String, Map<Long, Map<String, String>>> result = bdcr.getLogDataByITEFlogDataBasketID(itefId, logIDs); //50404 //50850 //50702 //50900

        //sort and split is integrated in above function
//        result = Helper.sortResult(ldris, 4, true, logIDs);
//        result = Helper.splitResultByRun(result);


        result = Helper.filterResultByTime(result, com.dcaiti.utilities.Config.startTime, com.dcaiti.utilities.Config.endTime);
        System.out.println("TraceLoader --> results sorted");

        if (this.curveOpt) {
            ResultUpgrader.optimizeDaimlerGPS2(result);
            System.out.println("TraceLoader --> results curve optimized");
        }
        
        if (this.longitudeCorrection) {
            ResultUpgrader.optimizeLongitudinalError(result);
            ResultUpgrader.optimizeLongitudinalError(result);
            System.out.println("TraceLoader --> results longitude error corrected");
        }


        if (com.dcaiti.utilities.Config.mapInferrerApproach == 1) {
            // TODO
            double minWinkel = 0;
            double maxWinkel = 0;
            double refPointLat = 0;
            double refPointLng = 0;
            boolean set = false;

            if ((itefId == 50602) || (itefId == 50603) || (itefId == 50604) || (itefId == 50702) || (itefId == 50703)) {
                minWinkel = -Math.PI / 3;
                maxWinkel = Math.PI - Math.PI / 3;
                refPointLat = 48.211114;
                refPointLng = 7.941253;
                set = true;
            }
            if (itefId == 50404) {
                refPointLat = 48.800884;
                refPointLng = 8.712892;
                minWinkel = -Math.PI / 2;
                maxWinkel = Math.PI / 2;
                set = true;
            }

            if (set)
                crashBarrierCoors = ResultUpgrader.generateAggregatedRoadBorder(result, refPointLat, refPointLng,
                        minWinkel, maxWinkel, false);
            else {
                System.out.println("mapInferrerApproach not possible");
            }
        }

        traces = new ArrayList<Trace>();
        boundingBox = new BoundingBox();

        int traceCt = 0;
        int pointCt = 0;
        int notHeadCt = 0;
        for (String vehicleID : result.keySet()) {
//            System.out.println("VehicleID: "+ vehicleID);

            Trace trace = new Trace(vehicleID, result.get(vehicleID));
            traceCt++;
            pointCt += trace.tracePts.size();

//            if (trace.containsUnusualHeadingChanges(Math.toRadians(5))) System.out.println("----------> unusualHeading in " +vehicleID);
            if (!trace.containsWgsHead){
                notHeadCt++;
            }
            
            traces.add(trace);

            boundingBox.mergeBoundingBox(trace.boundingBox);
        }

        long stop = System.currentTimeMillis();
        long duration = (stop -start)/1000;
        System.out.println("Parse the data: traces=" + traceCt + " total points=" + pointCt);
        System.out.println("in " + duration + " sec");
//        System.out.println("therefrom with corrected heading information: "+notHeadCt);
        return result;
    }


    /**
     * load Daimler traces by tileIds;
     * all available data/logIds are fetched from Accumulo;
     * only returns traces with more than 10 points
     * @param tileIds
     */
    public Map<String, Map<Long, Map<String, String>>> loadDaimlerTracesByTileIds(String[] tileIds) {
        
        return loadDaimlerTracesByTileIds(tileIds, 10);
    }
    
    /**
     * load Daimler traces by tileIds;
     * all available data/logIds are fetched from Accumulo
     * 
     * @param tileIds
     * @param minPoints returns traces with more than minPoints points
     */
    public Map<String, Map<Long, Map<String, String>>> loadDaimlerTracesByTileIds(String[] tileIds, int minPoints) {

        return loadDaimlerTracesByTileIds(tileIds, "foba_daimler_traces_1", minPoints);
    }
    
    
        /**
         * load Daimler traces by tileIds;
         * all available data/logIds are fetched from Accumulo
         * 
         * @param tileIds
         * @param tableName 
         * @param minPoints returns traces with more than minPoints points
         */
    public Map<String, Map<Long, Map<String, String>>> loadDaimlerTracesByTileIds(String[] tileIds, String tableName, int minPoints) {
        Config conf = new Config();
        conf.tableName = tableName;

        long start = System.currentTimeMillis();
        BigDataConnector bdc = new BigDataConnector(conf);
        long stop = System.currentTimeMillis();
        System.out.println("LogDataManager in " + (stop - start) + " ms.");

        start = System.currentTimeMillis();
        ILogDataResultIterator[] ldris = bdc.getLogDataByTileID(tileIds, null);
//        ILogDataResultIterator[] ldris = bdc.getLogDataByTileIDANDTimeRange(tileIds, 1422752400, 1468504844, null);
        stop = System.currentTimeMillis();
        System.out.println("ILogDataResultIterator in " + (stop - start) + " ms.");

        start = System.currentTimeMillis();
        Map<String, Map<Long, Map<String, String>>> result = Helper.sortResult(ldris);
        result = Helper.splitResultByRun(result, 10000, minPoints);
        stop = System.currentTimeMillis();
        System.out.println("Helper in " + (stop - start) + " ms.");
        
        if (this.curveOpt) {
            ResultUpgrader.optimizeDaimlerGPS2(result);
            System.out.println("TraceLoader --> results curve optimized");
        }
        
        if (this.longitudeCorrection) {
            ResultUpgrader.optimizeLongitudinalError(result);
            ResultUpgrader.optimizeLongitudinalError(result);
            System.out.println("TraceLoader --> results longitude error optimized");
        }


        start = System.currentTimeMillis();

        traces = new ArrayList<Trace>();
        boundingBox = new BoundingBox();
        
        int traceCt = 0;
        int pointCt = 0;
        for (String vehicleID : result.keySet()) {
            // System.out.println("VehicleID: "+ vehicleID);

            Trace trace = new Trace(vehicleID, result.get(vehicleID));
            traceCt++;
            pointCt += trace.tracePts.size();

            traces.add(trace);

            boundingBox.mergeBoundingBox(trace.boundingBox);
        }

        stop = System.currentTimeMillis();

        System.out.println(
                "Parse the data: traces=" + traceCt + " total points=" + pointCt + " in " + (stop - start) + " ms.");
        return result;
    }

    /**
     * load simTD traces by tileIds;
     * all available data/logIds are fetched from Accumulo
     * @param tileIds
     */
    public void loadSimTDTracesByTileIds(String[] tileIds, String key) {

        Config conf = new Config();
        conf.tableName="simTD_1";
        
        if (key.isEmpty()) key = "key_1";

        BigDataConnector bdc = new BigDataConnector(conf);

//        Map<String, Map<Long, Map<String, String>>> result = Helper.loadFromCache(key);        
        Map<String, Map<Long, Map<String, String>>> result = null;

        if(result == null){
//            Autobahn no time limit.
            ILogDataResultIterator[] ldris = bdc.getLogDataByTileID(tileIds, null);
//            Stadt time range                          13.12.12 12:00 <-> 13.12.12 13:00
//            ILogDataResultIterator[] ldris = bdc.getLogDataByTileIDANDTimeRange(tileIds, 1355400000000L, 1355403600000L, null);
//            Land time range                                 10.12.12 09:00  <->14.12.12 17:00
//            ILogDataResultIterator[] ldris = bdc.getLogDataByTileIDANDTimeRange(tileIds, 1355130000000L, 1355504400000L, null);
            
            result = Helper.sortResult(ldris);                               
            result = Helper.splitResultByRun(result);
//            Helper.saveToCache(key, result);
//            FileHandler.writeLogDataToCSV(key, result);
        }

        if (this.curveOpt) {
            ResultUpgrader.optimizeDaimlerGPS2(result);
            System.out.println("TraceLoader --> results curve optimized");
        }
        
        if (this.longitudeCorrection) {
            ResultUpgrader.optimizeLongitudinalError(result);
            ResultUpgrader.optimizeLongitudinalError(result);
            System.out.println("TraceLoader --> results longitude error optimized");
        }


        traces = new ArrayList<Trace>();
        boundingBox = new BoundingBox();
        
        int traceCt = 0;
        int pointCt = 0;
        for (String vehicleID : result.keySet()) {
//            System.out.println("--------------->>>> VehicleID: "+ vehicleID);

            Trace trace = new Trace(vehicleID, result.get(vehicleID));
            traceCt++;
            pointCt += trace.tracePts.size();

            traces.add(trace);

            boundingBox.mergeBoundingBox(trace.boundingBox);
        }


        System.out.println(
                "Parse the data: traces=" + traceCt + " total points=" + pointCt);
    }

    /**
     * Constructor which loads the trace with specified vehId from default 
     * table foba_daimler_traces_1.
     * The result may be split in several single traces (for example to get only
     * the tracePoints for one direction, if the car was driving in both
     * directions) using the helper class of BDC.jar
     * 
     * @param vehId
     *            vehicleId (e.g. specified in properties)
     */            
    public Map<String, Map<Long, Map<String, String>>> loadDaimlerTracesByVehicleId(String vehId) {
        
        return loadDaimlerTracesByVehicleId(vehId, "foba_daimler_traces_1", false);
    }

    /**
     * Constructor which loads the trace with specified vehId from tableName.
     * The result may be split in several single traces (for example to get only
     * the tracePoints for one direction, if the car was driving in both
     * directions) using the helper class of BDC.jar
     * 
     * if 50Hz data is loaded it will be interpolated!
     * 
     * @param vehId
     *            vehicleId (e.g. specified in properties)
     * @param tableName
     *            proper tableName in Accumulo (see deliverable <i>Software
     *            Delivery.doc<\i>)
     * @param doInterpolation if true and 50Hz data is loaded it will be interpolated! 
     */
    
    public Map<String, Map<Long, Map<String, String>>> loadDaimlerTracesByVehicleId(String vehId, String tableName, boolean doInterpolation) {

        Config conf = new Config();
        conf.tableName = tableName;


        long start = System.currentTimeMillis();
        BigDataConnector bdc = new BigDataConnector(conf);
        long stop = System.currentTimeMillis();
        System.out.println("LogDataManager in " + (stop - start) + " ms.");

        start = System.currentTimeMillis(); //
        ILogDataResultIterator ldri = bdc.getLogDataByVehicleID(vehId, null);//

        stop = System.currentTimeMillis();//
        System.out.println("ILogDataResultIterator in " + (stop - start) + " ms.");//
        start = System.currentTimeMillis();
        Map<String, Map<Long, Map<String, String>>> result = Helper.sortResult(ldri);
        ldri.close();
        stop = System.currentTimeMillis();
        System.out.println("Helper in " + (stop - start) + " ms.");
        
        //TODO hier oder erst nach der correction
//        if (doInterpolation && (tableName.equalsIgnoreCase("foba_daimler_traces_2") || tableName.equalsIgnoreCase("foba_daimler_traces_test_50_1"))) {
//            ResultUpgrader.interpolate50HzLinear(result);
//        }

        if (this.curveOpt) {
            ResultUpgrader.optimizeDaimlerGPS2(result);
            System.out.println("TraceLoader --> results curve optimized");
        }
        
        if (this.longitudeCorrection) {
            ResultUpgrader.optimizeLongitudinalError(result);
            ResultUpgrader.optimizeLongitudinalError(result);
            System.out.println("TraceLoader --> results longitude error optimized");
        }


        traces = new ArrayList<Trace>();
        boundingBox = new BoundingBox();
        
        start = System.currentTimeMillis();

        int traceCt = 0;
        int pointCt = 0;
        for (String vehicleID : result.keySet()) {
            // System.out.println("VehicleID: "+ vehicleID);

            Trace trace = new Trace(vehicleID, result.get(vehicleID));
            traceCt++;
            pointCt += trace.tracePts.size();

            traces.add(trace);

            boundingBox.mergeBoundingBox(trace.boundingBox);
      }
        
        
        if (doInterpolation && (tableName.equalsIgnoreCase("foba_daimler_traces_2") || tableName.equalsIgnoreCase("foba_daimler_traces_test_50_1"))) {
          int notInterpolated = 0;
          System.out.print("INTERPOLATING....");
          for(int i = 0; i< traces.size(); ++i){
             if(!traces.get(i).interpolateGPS()){
                 ++notInterpolated;
             }
          }
          System.out.println("finished!");
          if(notInterpolated > 0){
              System.out.println("WARNING: "+notInterpolated+" Trace were not interpolated!");
          }
      }
      
      stop = System.currentTimeMillis();
      System.out.println(
                "Parse the data: traces=" + traceCt + " total points=" + pointCt + " in " + (stop - start) + " ms.");
      return result;
    }

    
    /**
     * 
     * @param itefId
     * @deprecated
     */
    public void loadDaimlerTracesByItefIdInLocalCache(int itefId) {
        
        System.out.println("--------->"+itefId);
        com.dcaiti.bigdataconnector.Config conf = new com.dcaiti.bigdataconnector.Config();

        BigDataConnector bdc = new BigDataConnector(conf);
        
        String cacheKey = Helper.getCacheKeyFromITEFlogDataBasketID(itefId, bdc);
        Map<String, Map<Long, Map<String, String>>> result = Helper.loadFromCache(cacheKey);

        if (result == null) {
                
                List<String> logIDs = new ArrayList<String>();

            logIDs.add(LAT);
            logIDs.add(LNG);
            logIDs.add(HEADING);
            logIDs.add(SPEED);
            logIDs.add(ALDW_LANE_LTR_DIST_LT);
            logIDs.add(ALDW_LANE_LTR_DIST_RT);
            logIDs.add(ROAD_BORDER_LEFT);
            logIDs.add(ROAD_BORDER_RIGHT);
            logIDs.add(ALDW_LANE_MARK_WIDTH_LT);
            logIDs.add(ALDW_LANE_MARK_WIDTH_RT);
            logIDs.add(VEHICLE_X_ACC);
            logIDs.add(YAW_RATE);
                
            ILogDataResultIterator[] ldris = bdc.getLogDataByITEFlogDataBasketID(itefId, logIDs);

            

            result = Helper.sortResult(ldris, 4, true, logIDs);

            result = Helper.splitResultByRun(result);

            Helper.saveToCache(cacheKey, result);

        }

        result = Helper.filterResultByTime(result, com.dcaiti.utilities.Config.startTime,
                    com.dcaiti.utilities.Config.endTime);

        
        if (this.curveOpt)
            ResultUpgrader.optimizeDaimlerGPS2(result);

        if (com.dcaiti.utilities.Config.mapInferrerApproach == 1) {
            // TODO
            double minWinkel = 0;
            double maxWinkel = 0;
            double refPointLat = 0;
            double refPointLng = 0;
            boolean set = false;

            if ((itefId == 50602) || (itefId == 50603) || (itefId == 50604) || (itefId == 50702) || (itefId == 50703)) {
                minWinkel = -Math.PI / 3;
                maxWinkel = Math.PI - Math.PI / 3;
                refPointLat = 48.211114;
                refPointLng = 7.941253;
                set = true;
            }
            if (itefId == 50404) {
                refPointLat = 48.800884;
                refPointLng = 8.712892;
                minWinkel = -Math.PI / 2;
                maxWinkel = Math.PI / 2;
                set = true;
            }

            if (set)
                crashBarrierCoors = ResultUpgrader.generateAggregatedRoadBorder(result, refPointLat, refPointLng,
                        minWinkel, maxWinkel, false);
            else {
                System.out.println("mapInferrerApproach not possible");
            }
        }

        traces = new ArrayList<Trace>();
        boundingBox = new BoundingBox();

        int traceCt = 0;
        int pointCt = 0;
        for (String vehicleID : result.keySet()) {
//            System.out.println("VehicleID: "+ vehicleID);

            Trace trace = new Trace(vehicleID, result.get(vehicleID));
            traceCt++;
            pointCt += trace.tracePts.size();

//            if (trace.containsUnusualHeadingChanges(Math.toRadians(5))) System.out.println("----------> unusualHeading in " +vehicleID);

            traces.add(trace);

            boundingBox.mergeBoundingBox(trace.boundingBox);
        }

        
        System.out.println("Parse the data: traces=" + traceCt + " total points=" + pointCt);
    }

    /**
     * Loads all PhabMacs Traces.
     */
    public void loadPhabMacsTraces() {

        Config conf = new Config();
        conf.tableName = "fobaPhabmacs3";

        long start = System.currentTimeMillis();
        BigDataConnector bdc = new BigDataConnector(conf);
        long stop = System.currentTimeMillis();
        System.out.println("LogDataManager in " + (stop - start) + " ms.");

        start = System.currentTimeMillis();
        ILogDataResultIterator ldri = bdc.getLogDataByTimeRange(0l, 999999999999999999l, null);
        stop = System.currentTimeMillis();
        System.out.println("ILogDataResultIterator in " + (stop - start) + " ms.");

        start = System.currentTimeMillis();
        Map<String, Map<Long, Map<String, String>>> result = Helper.sortResult(ldri);
        result = Helper.splitResultByRun(result);
        stop = System.currentTimeMillis();
        System.out.println("Helper in " + (stop - start) + " ms.");

        traces = new ArrayList<Trace>();
        boundingBox = new BoundingBox();
        start = System.currentTimeMillis();

        int traceCt = 0;
        int pointCt = 0;
        for (String vehicleID : result.keySet()) {
            // System.out.println("VehicleID: "+ vehicleID);

            Trace trace = new Trace(vehicleID, result.get(vehicleID));
            traceCt++;
            pointCt += trace.tracePts.size();

            traces.add(trace);

            boundingBox.mergeBoundingBox(trace.boundingBox);
        }

        // TODO determine boundingBox automatically !!!
        // boundingBox[0] = new Coordinate(503125.8243413004,
        // 5394493.6338526355);
        // boundingBox[1] = new Coordinate(510825.5097475725,
        // 5397185.751381927);

        Coordinate bl;
        Coordinate ur;
        switch (com.dcaiti.utilities.Config.itefId) {
        case 50004:
            bl = new Coordinate(492392.01454493665, 5383733.4220367605);
            ur = new Coordinate(493360.4930141257, 5385184.8301931);
            break;
        case 50404:
            bl = new Coordinate(516513.79917577136, 5391224.24586833);
            ur = new Coordinate(525719.8950131269, 5393918.7327489285);
            break;
        case 50702:
            bl = new Coordinate(501760.6923915057, 5394111.47462954);
            ur = new Coordinate(504686.38633875985, 5396780.985116951);
            break;

        default:
            bl = new Coordinate(504409.0, 5396674.0);
            ur = new Coordinate(505450.0, 5397235.0);
        }
        
        boundingBox = new BoundingBox(bl, ur);

        stop = System.currentTimeMillis();
        System.out.println(
                "Parse the data: traces=" + traceCt + " total points=" + pointCt + " in " + (stop - start) + " ms.");

    }

    // ------------------ Helper functions
    // -----------------------------------------------
    

    //TODO remove or shift to TraceUpgrader or TraceTools

    /**
     * Retrieves Traces of only the direction of the first parsed trace in
     * this.traces
     * 
     * @return An ArrayList containing only traces with similiar heading to the
     *         first trace in this.traces
     */
    public ArrayList<ArrayList<Trace>> getByDirectionSeparatedTraces() {
        if (this.traces.size() < 1 || this.traces.equals(null)) {
            System.out.println("Make sure to load traces first");
           return null;
        }
        double delta = Math.PI / 4;
        ArrayList<Trace> oneWayTraces = new ArrayList<Trace>();
        ArrayList<Trace> otherWayTraces = new ArrayList<Trace>();

        // use first trace as reference
        double referenceHeading = this.traces.get(0).tracePts.get(0).getHeading();

        // loop over all traces and add them only to the resulting ArrayList of
        // traces, if their respective heading doesn't vary too much
        for (Trace currTrace : this.traces) {
            double currentHeading = currTrace.tracePts.get(0).getHeading();
            if (Util.headingDiff(referenceHeading, currentHeading) < delta) {
                // add current trace
                oneWayTraces.add(currTrace);
            } else {
                otherWayTraces.add(currTrace);
            }
        }

        // put both ArrayLists of traces into another one
        ArrayList<ArrayList<Trace>> rst = new ArrayList<ArrayList<Trace>>();
        rst.add(oneWayTraces);
        rst.add(otherWayTraces);

        return rst;
    }

    /**
     * for Hotspot Identifier
     * Return two directions of traces
     * 
     * @param list
     *            All traces
     * @param maxheadingDiff
     * @return ArrayList containing two ArrayLists with the two different
     *         directions
     */
    public static List<List<Trace>> divideTracesByDirection(List<Trace> list, double maxheadingDiff) {
        List<List<Trace>> result = new ArrayList<List<Trace>>();
        ArrayList<Trace> oneDir = new ArrayList<Trace>();
        ArrayList<Trace> anotherDir = new ArrayList<Trace>();
        oneDir.add(list.get(0));
        double refHeading = list.get(0).getTracePointAtIndex(0).getHeading();
        System.out.println("oneDir heading: " + refHeading);
        for (int i = 1; i < list.size(); i++) {
            Trace currTrace = list.get(i);
            TracePoint currFirstPoint = currTrace.getTracePointAtIndex(0);

            if (Util.headingDiff(refHeading, currTrace.getTracePointAtIndex(0).getHeading()) < maxheadingDiff) {
                oneDir.add(currTrace);
            } else {
                anotherDir.add(currTrace);
            }
        }
        // add both directions to the result
        result.add(oneDir);
        result.add(anotherDir);
        return result;
    }


    /**
     * get max no of traces
     */
    public ArrayList<Trace> getMaxNoOfTraces(ArrayList<Trace> allTraces) {

        ArrayList<Trace> limTraces = new ArrayList<Trace>();

        int ct = 0;
        while ((ct < allTraces.size()) && (ct < com.dcaiti.utilities.Config.maxNoOfTraces)) {
            limTraces.add(allTraces.get(ct));
            ct++;
        }

        return limTraces;
    }

}
