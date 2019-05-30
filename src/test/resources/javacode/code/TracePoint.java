package com.dcaiti.traceloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.dcaiti.traceloader.enums.LaneMarkColEnum;
import com.dcaiti.traceloader.enums.LaneMarkType2Enum;
import com.dcaiti.traceloader.enums.LaneMarkTypeEnum;
import com.dcaiti.traceloader.odometrie.SensorVehicleModel;
import com.dcaiti.traceloader.odometrie.blender.AbstractVehicleModel;
import com.dcaiti.utilities.GeoPosition;
import com.dcaiti.utilities.Point;
import com.dcaiti.utilities.Util;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * data class for trace points.
 *
 */
public class TracePoint implements GeoPosition {

    private String vehId;
    private Trace parentTrace;

    private long time;
    /**
     * in lat/lon
     */
    private Point point;

    /**
     * in UTM coordinates(WGS84)
     */
    private Coordinate coor;

    /**
     * in meters
     */
    private double altitude = Double.NaN;
    /**
     * in rad 0..2PI
     */
    private double heading = Double.NaN;

    /**
     * in degree 0..360 northern clockwise
     */
    private double wgsHead = Double.NaN;

    /**
     * in km/h
     */
    private double speed = Double.NaN;
    
    /**
     *  in m/s
     */
    private double speedSI = Double.NaN;
    
    /**
     * 
     */
    private double gpsSpeed = Double.NaN;

    /**
     * distance to left and right lane respectively
     */
    public double aldwLaneLtrlDistLt = Double.NaN;
    public double aldwLaneLtrlDistRt = Double.NaN;

    /**
     * These values are used to infer the total number of lanes on a trace
     */
    private int lanes2Left = 0;
    private int lanes2Right = 0;

    private LaneMarkTypeEnum laneMarkTypeLeft = LaneMarkTypeEnum.NDEFO;
    private LaneMarkTypeEnum laneMarkTypeRight = LaneMarkTypeEnum.NDEFO;
    private LaneMarkType2Enum laneMarkTypeLeft2 = LaneMarkType2Enum.NO_INFORMATION;
    private LaneMarkType2Enum laneMarkTypeRight2 = LaneMarkType2Enum.NO_INFORMATION;

    private double laneMarkTypeLeftDouble = Double.NaN;
    private double laneMarkTypeRightDouble = Double.NaN;

    private LaneMarkColEnum laneMarkColLeft = LaneMarkColEnum.NDEFO;
    private LaneMarkColEnum laneMarkColRight = LaneMarkColEnum.NDEFO;
    private double laneMarkColLeftDouble = Double.NaN;
    private double laneMarkColRightDouble = Double.NaN;

    /**
     * Width of left and right lane marker
     */
    private double aldw_LaneMarkWidth_lt = Double.NaN;
    private double aldw_LaneMarkWidth_rt = Double.NaN;

    public double roadBorderLt = Double.NaN;
    public double roadBorderRt = Double.NaN;

    /**
     *  in rad/s
     */
    private double yawRate = Double.NaN;
    private double yawRateRq = Double.NaN;
    private double yawRateOff = Double.NaN;
    private double vehicleXAcc = Double.NaN;
    private double vehicleYAcc = Double.NaN;
    private double vehicleXAccOff = Double.NaN;
    private double vehicleYAccOff = Double.NaN;
    private double pitchAngle = Double.NaN;

    private double stwhlAngleSpeed = Double.NaN;
    private double stwhlAngle = Double.NaN;
    private double stwhlSw = Double.NaN;

    private double dispSpeed = Double.NaN;

    boolean containsALDW = false;
    boolean containsDISTRONIC = false;
    boolean containsNullSpeed = false;
    boolean containsWgsHead = false;
    boolean containsNullGPSSpeed = false;

    // used for corrections
    public boolean corrected = false;

    // AbstractVehicleModel (now only used for Odometrie and only initialised if
    // the getter method is called)
    private SensorVehicleModel sensorModel = null;
    private AbstractVehicleModel abstractModel = null;
    private boolean trustworthy = true;

    private TracePoint(Builder builder) {
        time = builder.time;
        point = builder.lonLatPoint;
        parentTrace = builder.parentTrace;
        coor = builder.coor;
        altitude = builder.altitude;
        heading = builder.heading;
        wgsHead = builder.wgsHead;
        speed = builder.speed;
        speedSI = speed == 0 ? 0 : (builder.speed / 3.6);
        aldwLaneLtrlDistLt = builder.aldwLaneLtrlDistLt;
        aldwLaneLtrlDistRt = builder.aldwLaneLtrlDistRt;
        laneMarkTypeLeft = builder.laneMarkTypeLeft;
        laneMarkTypeRight = builder.laneMarkTypeRight;
        laneMarkTypeLeft2 = builder.laneMarkTypeLeft2;
        laneMarkTypeRight2 = builder.laneMarkTypeRight2;
        aldw_LaneMarkWidth_lt = builder.aldw_LaneMarkWidth_lt;
        aldw_LaneMarkWidth_rt = builder.aldw_LaneMarkWidth_rt;
        laneMarkColLeft = builder.laneMarkColLeft;
        laneMarkColRight = builder.laneMarkColRight;
        yawRate = builder.yawRate;
        yawRateRq = builder.yawRateRq;
        yawRateOff = builder.yawRateOff;
        pitchAngle = builder.pitchAngle;
        vehicleXAcc = builder.vehicleXAcc;
        vehicleYAcc = builder.vehicleYAcc;
        vehicleXAccOff = builder.vehicleXAccOff;
        vehicleYAccOff = builder.vehicleYAccOff;
        roadBorderLt = builder.roadBorderLt;
        roadBorderRt = builder.roadBorderRt;
        stwhlAngleSpeed = builder.stwhlAngleSpeed;
        stwhlAngle = builder.stwhlAngle;
        stwhlSw = builder.stwhlSw;
        dispSpeed = builder.dispSpeed;
        containsALDW = builder.containsALDW;
        containsDISTRONIC = builder.containsDISTRONIC;
        containsNullSpeed = builder.containsNullSpeed;
    }

    /**
     * use {@link TracePoint.Builder} <br>
     * Constructor. heading in rad!!!
     */
    @Deprecated
    public TracePoint(double utmX, double utmY, long timestamp, double head) {
        this.coor = new Coordinate(utmX, utmY);
        double[] latLong = Util.utmToLatLong(this.coor);
        this.point = new Point(latLong[1], latLong[0]);
        this.time = timestamp;
        heading = head;
    }

    /**
     * use {@link TracePoint.Builder} <br>
     * heading is computed later when complete trace was loaded
     * 
     * @param timestamp
     * @param lat
     * @param lng
     */
    @Deprecated
    public TracePoint(long timestamp, double lat, double lng) {
        this.time = timestamp;
        this.point = new Point(lng, lat);
        this.coor = Util.latLongToUtm(lat, lng);
    }

    /**
     * use {@link TracePoint.Builder} <br>
     * heading is computed later when complete trace was loaded
     * 
     */
    @Deprecated
    public TracePoint(double utmX, double utmY, long timestamp) {
        this.coor = new Coordinate(utmX, utmY);
        double[] latLong = Util.utmToLatLong(this.coor);
        this.point = new Point(latLong[1], latLong[0]);
        this.time = timestamp;
    }

    /**
     * when loading from Accumulo
     * 
     * @param timestamp
     * @param probeData
     */
    public TracePoint(long timestamp, Map<String, String> probeData) {

        double lat = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.LAT));
        double lng = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.LNG));

        time = timestamp;
        point = new Point(lng, lat);
        coor = Util.latLongPointToUtm(point);
        // check if key exists in Map
        if (probeData.get(TraceLoaderAccumulo2.ALT) != null) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.ALT));
            if (isInRange(TraceLoaderAccumulo2.ALT, v)) {
                altitude = v;
            }
        }
        if (probeData.containsKey(TraceLoaderAccumulo2.HEADING)) {
            double degree = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.HEADING));
            if (isInRange(TraceLoaderAccumulo2.ALT, degree)) {
                wgsHead = degree;
                heading = Util.degreeClockwiseNorthern2mathRad(degree);
                // System.out.println("---> "+wgsHead + " "+heading);
            }
        }

        if (probeData.containsKey(TraceLoaderAccumulo2.SPEED)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.SPEED));
            if (isInRange(TraceLoaderAccumulo2.SPEED, v)) {
                speed = v;
                speedSI = speed == 0 ? 0 : (v / 3.6);
                if (speed == 0.0) {
                    containsNullSpeed = true;
                }
            }

        }
        
        if (probeData.containsKey(TraceLoaderAccumulo2.GPS_SPEED)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.GPS_SPEED));
            if (isInRange(TraceLoaderAccumulo2.SPEED, v)) {
                gpsSpeed = v;
            }
        }else{
            this.containsNullGPSSpeed = true;
        }

        if (probeData.containsKey(TraceLoaderAccumulo2.VEHICLE_X_ACC)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.VEHICLE_X_ACC));
            if (isInRange(TraceLoaderAccumulo2.VEHICLE_X_ACC, v)) {
                vehicleXAcc = v;
            }
        }

        if (probeData.containsKey(TraceLoaderAccumulo2.VEHICLE_Y_ACC)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.VEHICLE_Y_ACC));
            if (isInRange(TraceLoaderAccumulo2.VEHICLE_Y_ACC, v)) {
                vehicleYAcc = v;
            }
        }

        if (probeData.containsKey(TraceLoaderAccumulo2.VEHICLE_X_ACC_OFFSET)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.VEHICLE_X_ACC_OFFSET));
            if (isInRange(TraceLoaderAccumulo2.VEHICLE_X_ACC_OFFSET, v)) {
                vehicleXAccOff = v;
            }
        }

        if (probeData.containsKey(TraceLoaderAccumulo2.VEHICLE_Y_ACC_OFFSET)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.VEHICLE_Y_ACC_OFFSET));
            if (isInRange(TraceLoaderAccumulo2.VEHICLE_Y_ACC_OFFSET, v)) {
                vehicleYAccOff = v;
            }
        }

        if (probeData.containsKey(TraceLoaderAccumulo2.YAW_RATE)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.YAW_RATE));
            if (isInRange(TraceLoaderAccumulo2.YAW_RATE, v)) {
                yawRate = v *Math.PI /180.0;
            }
        }

        if (probeData.containsKey(TraceLoaderAccumulo2.YAW_RATE_OFFSET)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.YAW_RATE_OFFSET));
            if (isInRange(TraceLoaderAccumulo2.YAW_RATE_OFFSET, v)) {
                yawRateOff = v;
            }
        }

        if (probeData.containsKey(TraceLoaderAccumulo2.PITCH_ANGLE)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.PITCH_ANGLE));
            if (isInRange(TraceLoaderAccumulo2.PITCH_ANGLE, v)) {
                pitchAngle = v;
            }

        }

        if (probeData.containsKey(TraceLoaderAccumulo2.YAW_RATE_RQ)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.YAW_RATE_RQ));
            if (isInRange(TraceLoaderAccumulo2.YAW_RATE_RQ, v)) {
                yawRateRq = v;
            }

        }
        if (probeData.containsKey(TraceLoaderAccumulo2.ALDW_LANE_LTR_DIST_LT)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.ALDW_LANE_LTR_DIST_LT));
            containsALDW = true;
            if (isInRange(TraceLoaderAccumulo2.ALDW_LANE_LTR_DIST_LT, v)) {
                aldwLaneLtrlDistLt = v;
            }
        }
        if (probeData.containsKey(TraceLoaderAccumulo2.ALDW_LANE_LTR_DIST_RT)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.ALDW_LANE_LTR_DIST_RT));
            containsALDW = true;
            if (isInRange(TraceLoaderAccumulo2.ALDW_LANE_LTR_DIST_RT, v)) {
                aldwLaneLtrlDistRt = v;
            }
        }

        if (probeData.containsKey(TraceLoaderAccumulo2.ALDW_LANE_MARK_TYPE_LT)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.ALDW_LANE_MARK_TYPE_LT));
            if (isInRange(TraceLoaderAccumulo2.ALDW_LANE_MARK_TYPE_LT, v)) {
                laneMarkTypeLeftDouble = v;
            }
        }
        if (probeData.containsKey(TraceLoaderAccumulo2.ALDW_LANE_MARK_TYPE_RT)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.ALDW_LANE_MARK_TYPE_RT));
            // System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
            // FOUND" + v);
            if (isInRange(TraceLoaderAccumulo2.ALDW_LANE_MARK_TYPE_RT, v)) {
                laneMarkTypeRightDouble = v;
            }
        }
        // Lane Marker Width left
        if (probeData.containsKey(TraceLoaderAccumulo2.ALDW_LANE_MARK_WIDTH_LT)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.ALDW_LANE_MARK_WIDTH_LT));
            if (isInRange(TraceLoaderAccumulo2.ALDW_LANE_MARK_WIDTH_LT, v)) {
                aldw_LaneMarkWidth_lt = v;
            }
        }
        // Lane Marker Width right
        if (probeData.containsKey(TraceLoaderAccumulo2.ALDW_LANE_MARK_WIDTH_RT)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.ALDW_LANE_MARK_WIDTH_RT));
            if (isInRange(TraceLoaderAccumulo2.ALDW_LANE_MARK_WIDTH_RT, v)) {
                aldw_LaneMarkWidth_rt = v;
            }
        }

        if (probeData.containsKey(TraceLoaderAccumulo2.ALDW_LANE_MARK_COL_LT)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.ALDW_LANE_MARK_COL_LT));
            if (isInRange(TraceLoaderAccumulo2.ALDW_LANE_MARK_COL_LT, v)) {
                laneMarkColLeftDouble = v;
            }
        }
        if (probeData.containsKey(TraceLoaderAccumulo2.ALDW_LANE_MARK_COL_RT)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.ALDW_LANE_MARK_COL_RT));
            if (isInRange(TraceLoaderAccumulo2.ALDW_LANE_MARK_COL_RT, v)) {
                laneMarkColRightDouble = v;
            }
        }

        // set road border
        if (probeData.containsKey(TraceLoaderAccumulo2.ROAD_BORDER_LEFT)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.ROAD_BORDER_LEFT));
            containsDISTRONIC = true;
            if (isInRange(TraceLoaderAccumulo2.ROAD_BORDER_LEFT, v)) {
                roadBorderLt = v;
            }
        }
        if (probeData.containsKey(TraceLoaderAccumulo2.ROAD_BORDER_RIGHT)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.ROAD_BORDER_RIGHT));
            containsDISTRONIC = true;
            if (isInRange(TraceLoaderAccumulo2.ROAD_BORDER_RIGHT, v)) {
                roadBorderRt = v;
            }
        }

        if (probeData.containsKey(TraceLoaderAccumulo2.STWHL_ANGLESPEED)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.STWHL_ANGLESPEED));
            if (isInRange(TraceLoaderAccumulo2.STWHL_ANGLESPEED, v)) {
                stwhlAngleSpeed = v;
            }
        }

        if (probeData.containsKey(TraceLoaderAccumulo2.STWHL_ANGLE)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.STWHL_ANGLE));
            if (isInRange(TraceLoaderAccumulo2.STWHL_ANGLE, v)) {
                stwhlAngle = v;
            }
        }

        if (probeData.containsKey(TraceLoaderAccumulo2.STWHL_SW)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.STWHL_SW));
            if (isInRange(TraceLoaderAccumulo2.STWHL_SW, v)) {
                stwhlSw = v;
            }
        }

        if (probeData.containsKey(TraceLoaderAccumulo2.DISP_SPEED)) {
            double v = Double.parseDouble(probeData.get(TraceLoaderAccumulo2.DISP_SPEED));
            if (isInRange(TraceLoaderAccumulo2.DISP_SPEED, v)) {
                dispSpeed = v;
            }
        }

        // set Lane Mark type
        if (!Double.isNaN(laneMarkTypeLeftDouble)) {
            this.laneMarkTypeLeft = LaneMarkTypeEnum.getEnumForInt((int) laneMarkTypeLeftDouble);
            this.laneMarkTypeLeft2 = LaneMarkType2Enum.getEnumForInt((int) laneMarkTypeLeftDouble);
        }
        if (!Double.isNaN(laneMarkTypeRightDouble)) {
            this.laneMarkTypeRight = LaneMarkTypeEnum.getEnumForInt((int) laneMarkTypeRightDouble);
            this.laneMarkTypeRight2 = LaneMarkType2Enum.getEnumForInt((int) laneMarkTypeRightDouble);
        }
        if (!Double.isNaN(laneMarkColLeftDouble)) {
            this.laneMarkColLeft = LaneMarkColEnum.getEnumForInt((int) laneMarkColLeftDouble);
        }
        if (!Double.isNaN(laneMarkColRightDouble)) {
            this.laneMarkColRight = LaneMarkColEnum.getEnumForInt((int) laneMarkColRightDouble);
        }

    }
    
    public Map<String, String> buildMap(){
        Map<String, String> map = new HashMap<String,String>();
        map.put(TraceLoaderAccumulo2.LNG, Double.toString(this.point.lng()));
        map.put(TraceLoaderAccumulo2.LAT,Double.toString(this.point.lat()));
        if(!Double.isNaN(this.altitude)){
            map.put(TraceLoaderAccumulo2.ALT, Double.toString(this.altitude));
        }
        if(!Double.isNaN(this.wgsHead)){
            //TODO: update wgsHead if heading is updated!
            map.put(TraceLoaderAccumulo2.HEADING, Double.toString(this.wgsHead));
        }
        if(!Double.isNaN(this.speed)){
            map.put(TraceLoaderAccumulo2.SPEED, Double.toString(this.speed));
        }
        if(!Double.isNaN(this.vehicleXAcc)){
            map.put(TraceLoaderAccumulo2.VEHICLE_X_ACC, Double.toString(this.vehicleXAcc));
        }
        if(!Double.isNaN(this.vehicleYAcc)){
            map.put(TraceLoaderAccumulo2.VEHICLE_Y_ACC, Double.toString(this.vehicleYAcc));
        }
        if(!Double.isNaN(this.vehicleXAccOff)){
            map.put(TraceLoaderAccumulo2.VEHICLE_X_ACC_OFFSET, Double.toString(this.vehicleXAccOff));
        }
        if(!Double.isNaN(this.vehicleYAccOff)){
            map.put(TraceLoaderAccumulo2.VEHICLE_Y_ACC_OFFSET, Double.toString(this.vehicleYAccOff));
        }
        if(!Double.isNaN(this.yawRate)){
            map.put(TraceLoaderAccumulo2.YAW_RATE, Double.toString(this.yawRate *180.0/Math.PI));
        }
        if(!Double.isNaN(this.yawRateOff)){
            map.put(TraceLoaderAccumulo2.YAW_RATE_OFFSET, Double.toString(this.yawRateOff));
        }
        if(!Double.isNaN(this.pitchAngle)){
            map.put(TraceLoaderAccumulo2.PITCH_ANGLE, Double.toString(this.pitchAngle));
        }
        if(!Double.isNaN(this.yawRateRq)){
            map.put(TraceLoaderAccumulo2.YAW_RATE_RQ, Double.toString(this.yawRateRq));
        }
        if(!Double.isNaN(this.aldwLaneLtrlDistLt)){
            map.put(TraceLoaderAccumulo2.ALDW_LANE_LTR_DIST_LT, Double.toString(this.aldwLaneLtrlDistLt));
        }
        if(!Double.isNaN(this.aldwLaneLtrlDistRt)){
            map.put(TraceLoaderAccumulo2.ALDW_LANE_LTR_DIST_RT, Double.toString(this.aldwLaneLtrlDistRt));
        }
        if(!Double.isNaN(this.laneMarkTypeLeftDouble)){
            map.put(TraceLoaderAccumulo2.ALDW_LANE_MARK_TYPE_LT, Double.toString(this.laneMarkTypeLeftDouble));
        }
        if(!Double.isNaN(this.laneMarkTypeRightDouble)){
            map.put(TraceLoaderAccumulo2.ALDW_LANE_MARK_TYPE_RT, Double.toString(this.laneMarkTypeRightDouble));
        }        
        if(!Double.isNaN(this.aldw_LaneMarkWidth_lt)){
            map.put(TraceLoaderAccumulo2.ALDW_LANE_MARK_WIDTH_LT, Double.toString(this.aldw_LaneMarkWidth_lt));
        }
        if(!Double.isNaN(this.aldw_LaneMarkWidth_rt)){
            map.put(TraceLoaderAccumulo2.ALDW_LANE_MARK_WIDTH_RT, Double.toString(this.aldw_LaneMarkWidth_rt));
        }
        if(!Double.isNaN(this.laneMarkColLeftDouble)){
            map.put(TraceLoaderAccumulo2.ALDW_LANE_MARK_COL_LT, Double.toString(this.laneMarkColLeftDouble));
        }
        if(!Double.isNaN(this.laneMarkColRightDouble)){
            map.put(TraceLoaderAccumulo2.ALDW_LANE_MARK_COL_RT, Double.toString(this.laneMarkColRightDouble));
        }
        if(!Double.isNaN(this.roadBorderLt)){
            map.put(TraceLoaderAccumulo2.ROAD_BORDER_LEFT, Double.toString(this.roadBorderLt));
        }
        if(!Double.isNaN(this.roadBorderRt)){
            map.put(TraceLoaderAccumulo2.ROAD_BORDER_RIGHT, Double.toString(this.roadBorderRt));
        }
        if(!Double.isNaN(this.stwhlAngleSpeed)){
            map.put(TraceLoaderAccumulo2.STWHL_ANGLESPEED, Double.toString(this.stwhlAngleSpeed));
        }
        if(!Double.isNaN(this.stwhlAngle)){
            map.put(TraceLoaderAccumulo2.STWHL_ANGLE, Double.toString(this.stwhlAngle));
        }
        if(!Double.isNaN(this.stwhlSw)){
            map.put(TraceLoaderAccumulo2.STWHL_SW, Double.toString(this.stwhlSw));
        }
        if(!Double.isNaN(this.dispSpeed)){
            map.put(TraceLoaderAccumulo2.DISP_SPEED, Double.toString(this.dispSpeed));
        }
//        // set Lane Mark type
//        if (!Double.isNaN(laneMarkTypeLeftDouble)) {
//            this.laneMarkTypeLeft = LaneMarkTypeEnum.getEnumForInt((int) laneMarkTypeLeftDouble);
//            this.laneMarkTypeLeft2 = LaneMarkType2Enum.getEnumForInt((int) laneMarkTypeLeftDouble);
//        }
//        if (!Double.isNaN(laneMarkTypeRightDouble)) {
//            this.laneMarkTypeRight = LaneMarkTypeEnum.getEnumForInt((int) laneMarkTypeRightDouble);
//            this.laneMarkTypeRight2 = LaneMarkType2Enum.getEnumForInt((int) laneMarkTypeRightDouble);
//        }
//        if (!Double.isNaN(laneMarkColLeftDouble)) {
//            this.laneMarkColLeft = LaneMarkColEnum.getEnumForInt((int) laneMarkColLeftDouble);
//        }
//        if (!Double.isNaN(laneMarkColRightDouble)) {
//            this.laneMarkColRight = LaneMarkColEnum.getEnumForInt((int) laneMarkColRightDouble);
//        }
        return map;
    }

    private boolean isInRange(String fieldOrLogId, double value) {

        TracePointFieldProperties prop = new TracePointFieldProperties(fieldOrLogId);

        return prop.isSensorValueValid(value);

    }

    /**
     * Set the tracePoint values when loading traces from local dataBase.
     * 
     * @param line
     * @deprecated
     */
    public void setValues(String line) {
        StringTokenizer tok = new StringTokenizer(line, ",");

        long t = Long.parseLong(tok.nextToken());
        String logId = tok.nextToken();

        if (logId.equals("NAVI_Pos1_AR")) {
            time = t;
            double lat = Double.parseDouble(tok.nextToken());
            double lon = Double.parseDouble(tok.nextToken());
            point = new Point(lon, lat);
            return;
        }

        if ((time > 0) && (t != time)) { // time is already set but belongs to
                                         // another pos
            // System.out.println("False order in trace file...");
            return;
        }

        if (logId.equals("NAVI_Pos2_AR")) {
            speed = Double.parseDouble(tok.nextToken());
            heading = Double.parseDouble(tok.nextToken());
            return;
        }

        if (logId.equals("IC_BasicInfo_AR2")) {
            speed = Double.parseDouble(tok.nextToken());
            return;
        }

        // if (logId.equals("LaneInfo")) {
        // laneNo = Integer.parseInt(tok.nextToken());
        // currLane = Integer.parseInt(tok.nextToken());
        // }

    }

    public String toString() {

        String res = "------> \n";
        // if (parentTrace != null) res += "vehicle: " +
        // parentTrace.getVehicleId() +"\n";
        res += "(" + coor.x + ", " + coor.y + ")\n";
        res += "time: " + Util.getTime(time) + "\n";
        res += " latLng: (" + lat() + ", " + lng() + ")\n";
        res += " hdg: " + heading + "\n";
        // res += " wgsHead: "+Util.degreeClockwiseNorthern2mathRad(wgsHead) +
        // "\n";
        res += " speed: " + speed + ", displayedSpeed: " + dispSpeed + "\n";
        res += " yaw rate: " + yawRate + ", " + yawRateRq + ", " + yawRateRq + "\n";
        // res += " pitch angle: " +pitchAngle +")\n";
        // res += " acceleration(X,Y): (" +vehicleXAcc +", " + vehicleYAcc
        // +")\n";
        // res += " accelerationOffset(X,Y): (" +vehicleXAccOff +", " +
        // vehicleYAccOff +")\n";
        // res += " aldw(L,R): (" +aldwLaneLtrlDistLt + ", " +aldwLaneLtrlDistRt
        // + ")\n";
        // res += " distronic(L,R): " +roadBorderLt + ", " +roadBorderRt + "\n";
        // res += " stwhl(AngleSpeed,Angle,stwhlSw): " +stwhlAngleSpeed + ", "
        // +stwhlAngle + ", " + stwhlSw +"\n";
        //
        // res += " LaneMarkColor(" + laneMarkColLeft + ", " + laneMarkColRight
        // + ")\n";
        // res += " LaneMarkType (" + laneMarkTypeLeft + ", " +
        // laneMarkTypeRight + ")\n";
        // res += " LaneMarkType2(" + laneMarkTypeLeft2 + ", " +
        // laneMarkTypeRight2 +")\n";

        // if (!Double.isNaN(roadBorderLt) && !Double.isNaN(roadBorderRt)) {
        // double width = roadBorderLt + roadBorderRt;
        // res += " total width: " +width + "\n";
        // }
        //
        // res += containsALDW + ", " +containsDISTRONIC + "\n";
        if (corrected)
            res += " CORRECTED";

        return res;

        // double h = Double.NaN;
        // if (!Double.isNaN(heading)) {
        // h = Math.round(heading * 100) / 100.0;
        // }
        // return "" + h;
    }

    public AbstractVehicleModel getAbstractVehicleModel() {
        if (this.abstractModel == null) {
            // initialise the model //throw error if some of the necessary data
            // are not avaible?
            this.abstractModel = new AbstractVehicleModel(this.coor, this.heading, this.speed, this.yawRate,
                    this.getVehicleXAcc(), this.getVehicleYAccOff());
        }
        return this.abstractModel;
    }

    public SensorVehicleModel getSensorVehicleModel() {
        if (this.sensorModel == null) {
            this.sensorModel = new SensorVehicleModel(this.time, this.coor, this.heading, this.speed, this.yawRate,
                    this.getVehicleXAcc(), this.getVehicleYAccOff());
        }
        return this.sensorModel;
    }

    /**
     * a new tracePoint is created with new coordinates, heading and time; TODO
     * currently other attributes are copied from this!
     * 
     * @param anotherTP
     * @return
     */
    public TracePoint mergeTracePoint(TracePoint anotherTP) {

        double x = (this.coor.x + anotherTP.coor.x) / 2.0;
        double y = (this.coor.y + anotherTP.coor.y) / 2.0;
        Coordinate coor = new Coordinate(x, y);

        double headDiff = Util.signedHeadingDiff(this.heading, anotherTP.heading);
        double headFrac = headDiff / 2.0;
        double head = Util.clampPi(this.heading + headFrac);

        long t = (this.time + anotherTP.time) / 2;

        TracePoint tp = new TracePoint.Builder(this).time(t).coordinate(coor).heading(head).build();

        return tp;

    }

    /**
     * new tracePoints between this and anotherTP are created with new
     * coordinates and time; for all new tracePts the heading is set as average
     * of this and another other attributes are copied from this!
     * 
     * @param anotherTP
     * @return
     */
    public ArrayList<TracePoint> getTracePoints(TracePoint anotherTP, double averageDistance, double minSpeed) {

        // System.out.println("this --->"+this.toString());
        // System.out.println("another --->" +anotherTP.toString());
        ArrayList<TracePoint> newPoints = new ArrayList<TracePoint>();
        // determine how many points must be inserted
        double distance = this.distance(anotherTP);
        averageDistance = Math.max(averageDistance, 5.0);
        int no = (int) (distance / averageDistance);
        if (no < 2) { // add only one
            TracePoint tp = this.mergeTracePoint(anotherTP);
            tp.speed = minSpeed;
            newPoints.add(tp);
            // System.out.println("one added" +tp.toString());
            return newPoints;
        }
        // System.out.println(distance +", " +averageDistance +", "+no);

        long timeFrac = (anotherTP.time - this.time) / no;

        double fracX = (anotherTP.coor.x - this.coor.x) / no;
        double fracY = (anotherTP.coor.y - this.coor.y) / no;

        double headDiff = Util.signedHeadingDiff(this.heading, anotherTP.heading);
        double headFrac = headDiff / 2.0;
        double heading = Util.clampPi(this.heading + headFrac);

        // omit first(this) and last(anotherTP
        for (int i = 1; i < no - 1; i++) {
            double x = this.coor.x + i * fracX;
            double y = this.coor.y + i * fracY;
            long time = this.time + i * timeFrac;

            TracePoint tp = new TracePoint(x, y, time, heading);
            tp.copyAttributes(this);
            tp.speed = minSpeed;
            newPoints.add(tp);
            // System.out.println("Added: "+ tp.toString());
        }
        return newPoints;

    }

    /**
     * see {@link #TracePoint(Builder)}
     */
    @Deprecated
    public void copyAttributes(TracePoint anotherTP) {

        this.wgsHead = anotherTP.wgsHead;
        this.speed = anotherTP.speed;
        this.aldwLaneLtrlDistLt = anotherTP.aldwLaneLtrlDistLt;
        this.aldwLaneLtrlDistRt = anotherTP.aldwLaneLtrlDistRt;
        this.aldw_LaneMarkWidth_lt = anotherTP.aldw_LaneMarkWidth_lt;
        this.aldw_LaneMarkWidth_rt = anotherTP.aldw_LaneMarkWidth_rt;
        this.setLaneMarkTypeLeft(anotherTP.getLaneMarkTypeLeft());
        this.setLaneMarkTypeRight(anotherTP.getLaneMarkTypeRight());
        this.yawRate = anotherTP.yawRate;
        this.vehicleXAcc = anotherTP.vehicleXAcc;
        this.roadBorderLt = anotherTP.roadBorderLt;
        this.roadBorderRt = anotherTP.roadBorderRt;
        this.parentTrace = anotherTP.parentTrace;
        this.containsALDW = anotherTP.containsALDW;
        this.containsDISTRONIC = anotherTP.containsDISTRONIC;
        if (anotherTP.parentTrace != null)
            this.parentTrace = anotherTP.parentTrace;

    }

    /**
     * computes the UTM coordinate of left lane from the current position
     * (this.coor) if distance == NaN, then the current position is returned.
     * 
     * @return the UTM coordinate of left lane
     */
    public Coordinate getAldwLaneDistLeftAsUTM() {

        if (Double.isNaN(this.aldwLaneLtrlDistLt))
            return this.coor;

        double angle = this.heading + Math.PI / 2.0;
        return new Coordinate(this.coor.x + (this.aldwLaneLtrlDistLt * Math.cos(angle)),
                this.coor.y + (this.aldwLaneLtrlDistLt * Math.sin(angle)));
    }

    /**
     * computes the UTM coordinate of right lane from the current position
     * (this.coor) if distance == NaN, then the current position is returned.
     * 
     * @return the UTM coordinate of right lane
     */
    public Coordinate getAldwLaneDistRightAsUTM() {

        if (Double.isNaN(this.aldwLaneLtrlDistRt))
            return this.coor;

        double angle = this.heading - Math.PI / 2.0;
        return new Coordinate(this.coor.x + (this.aldwLaneLtrlDistRt * Math.cos(angle)),
                this.coor.y + (this.aldwLaneLtrlDistRt * Math.sin(angle)));
    }

    /**
     * computes the UTM coordinate of left road border from the current position
     * (this.coor) if distance == NaN, then the current position is returned.
     * 
     * @return the UTM coordinate of left road border
     */
    public Coordinate getRoadBorderDistLeftAsUTM() {

        if (Double.isNaN(this.roadBorderLt))
            return this.coor;

        double angle = this.heading + Math.PI / 2.0;
        return new Coordinate(this.coor.x + (this.roadBorderLt * Math.cos(angle)),
                this.coor.y + (this.roadBorderLt * Math.sin(angle)));
    }

    /**
     * computes the UTM coordinate of right road border from the current
     * position (this.coor) if distance == NaN, then the current position is
     * returned.
     * 
     * @return the UTM coordinate of right road border
     */
    public Coordinate getRoadBorderDistRightAsUTM() {

        if (Double.isNaN(this.roadBorderRt))
            return this.coor;

        double angle = this.heading - Math.PI / 2.0;
        return new Coordinate(this.coor.x + (this.roadBorderRt * Math.cos(angle)),
                this.coor.y + (this.roadBorderRt * Math.sin(angle)));
    }

    /**
     * Calculates the coordinates of a point to the left of this TracePoint,
     * lying on a perpendicular going through this Tracepoint, with distance
     * distance
     * 
     * @param distance
     *            Desired distance to this TracePoint in meters
     * @return Coordinates for the unique point that
     */
    public Coordinate getPointOnLeftPerpendicularWithDistance(double distance) {
        double angle = this.heading + Math.PI / 2.0;
        return new Coordinate(this.coor.x + (distance * Math.cos(angle)), this.coor.y + (distance * Math.sin(angle)));
    }

    /**
     * Calculates the coordinates of a point to the right of this TracePoint,
     * lying on a perpendicular going through this Tracepoint, with distance
     * distance
     * 
     * @param distance
     *            Desired distance to this TracePoint in meters
     * @return Coordinates for the unique point that
     */
    public Coordinate getPointOnRightPerpendicularWithDistance(double distance) {
        double angle = this.heading - Math.PI / 2.0;
        return new Coordinate(this.coor.x + (distance * Math.cos(angle)), this.coor.y + (distance * Math.sin(angle)));
    }

    /**
     * Calculates second point needed to create LocationIndexedLine
     * 
     * @return
     */
    public TracePoint getPointBehindWithDistance(double distance) {
        distance = -1 * distance;
        TracePoint result = new TracePoint(this.coor.x + Math.cos(heading) * distance,
                this.coor.y + Math.sin(heading) * distance, 0);
        result.heading = this.heading;
        return result;
    }

    public double distance(TracePoint another) {
        return this.coor.distance(another.coor);
    }

    @Override
    public double lat() {
        return point.lat();
    }

    @Override
    public double lng() {
        return point.lng();
    }

    public Trace getParentTrace() {
        return parentTrace;
    }

    public void setParentTrace(Trace parentTrace) {
        this.parentTrace = parentTrace;
    }

    public long getTime() {
        return time;
    }
    
    //has to return seconds
    public double getTimeSI(){
        return time/1000.0;
    }

    public Point getPoint() {
        return point;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getHeading() {
        return heading;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    //has to return m/s
    public double getSpeedSI(){
        return this.speedSI;
    }
        
    public double getGPSSpeed() {
        return gpsSpeed;
    }
    
    //has to return m/s
    public double getGPSSpeedSI(){
        return gpsSpeed/3.6;
    }

    public Coordinate getCoor() {
        return coor;
    }

    public double getWgsHead() {
        return wgsHead;
    }

    public double getAldwLaneDistLeft() {
        return this.aldwLaneLtrlDistLt;
    }

    public double getAldwLaneDistRight() {
        return this.aldwLaneLtrlDistRt;
    }

    public double getRoadBorderLt() {
        return roadBorderLt;
    }

    public double getRoadBorderRt() {
        return roadBorderRt;
    }

    public void setLaneMarkTypeLeft(LaneMarkTypeEnum type) {
        this.laneMarkTypeLeft = type;
    }

    public void setLaneMarkTypeRight(LaneMarkTypeEnum type) {
        this.laneMarkTypeRight = type;
    }

    public LaneMarkTypeEnum getLaneMarkTypeLeft() {
        return this.laneMarkTypeLeft;
    }

    public LaneMarkTypeEnum getLaneMarkTypeRight() {
        return this.laneMarkTypeRight;
    }

    public void setLaneMarkTypeLeft2(LaneMarkType2Enum type) {
        this.laneMarkTypeLeft2 = type;
    }

    public void setLaneMarkTypeRight2(LaneMarkType2Enum type) {
        this.laneMarkTypeRight2 = type;
    }

    public LaneMarkType2Enum getLaneMarkTypeLeft2() {
        return this.laneMarkTypeLeft2;
    }

    public LaneMarkType2Enum getLaneMarkTypeRight2() {
        return this.laneMarkTypeRight2;
    }

    public void setLaneMarkColLeft(LaneMarkColEnum col) {
        this.laneMarkColLeft = col;
    }

    public void setLaneMarkColRight(LaneMarkColEnum col) {
        this.laneMarkColRight = col;
    }

    public LaneMarkColEnum getLaneMarkColLeft() {
        return this.laneMarkColLeft;
    }

    public LaneMarkColEnum getLaneMarkColRight() {
        return this.laneMarkColRight;
    }

    public double getAldw_LaneMarkWidth_lt() {
        return aldw_LaneMarkWidth_lt;
    }

    public double getAldw_LaneMarkWidth_rt() {
        return aldw_LaneMarkWidth_rt;
    }

    public double getVehicleXAcc() {
        return vehicleXAcc;
    }

    public double getVehicleYAcc() {
        return vehicleYAcc;
    }

    public double getVehicleXAccOff() {
        return vehicleXAccOff;
    }

    public double getVehicleYAccOff() {
        return vehicleYAccOff;
    }

    public double getYawRate() {
        return yawRate;
    }
    
    //has to return rad/s
    public double getYawRateDeg(){
        return yawRate *180.0 /Math.PI;
    }

    public double getYawRateOff() {
        return yawRateOff;
    }

    public double getPitchAngle() {
        return pitchAngle;
    }

    public double getYawRateRq() {
        return yawRateRq;
    }

    public double getStwhlAngleSpeed() {
        return stwhlAngleSpeed;
    }

    public double getStwhlAngle() {
        return stwhlAngle;
    }

    public double getStwhlSw() {
        return stwhlSw;
    }

    public double getDispSpeed() {
        return dispSpeed;
    }

    public boolean isContainsALDW() {
        return containsALDW;
    }

    public boolean isContainsDISTRONIC() {
        return containsDISTRONIC;
    }

    public boolean isContainsNullSpeed() {
        return containsNullSpeed;
    }

    /**
     * Getter and setter for lanes2Left/lanes2Right property
     */
    public int getLanes2Left() {
        return this.lanes2Left;
    }

    public void setLanes2Left(int n) {
        this.lanes2Left = n;
    }

    public void increaseLanes2Left() {
        this.lanes2Left++;
    }

    public int getLanes2Right() {
        return this.lanes2Right;
    }

    public void setLanes2Right(int n) {
        this.lanes2Right = n;
    }

    public void increaseLanes2Right() {
        this.lanes2Right++;
    }
	
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (time ^ (time >>> 32));
        result = prime * result + ((this.parentTrace == null || this.parentTrace.getBaseVehId() == null) ? 0 : this.parentTrace.getBaseVehId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TracePoint other = (TracePoint) obj;
        if (time != other.time)
            return false;
        if (this.parentTrace == null || this.parentTrace.getBaseVehId() == null) {
            if (other.parentTrace != null && other.parentTrace.getBaseVehId() != null)
                return false;
        } else if(other.parentTrace == null || other.parentTrace.getBaseVehId() == null){
            return false;
        } else if (!this.parentTrace.getVehicleId().equals(other.parentTrace.getVehicleId()))
            return false;
        return true;
    }
    
    public TracePoint clone(){
        return new TracePoint.Builder(this).build();
    }

    public void noTrust(){
        this.trustworthy = false;
    }
    
    public boolean isTrustworthy(){
        return this.trustworthy;
    }

    public static class Builder {
        // required parameters
        private long time;
        private Point lonLatPoint;

        // optional parameters
        private Trace parentTrace;
        private Coordinate coor; // is calculated by lonlatpoint
        private double altitude = Double.NaN;
        private double heading = Double.NaN;
        private double wgsHead = Double.NaN;
        private double speed = Double.NaN;
        private double aldwLaneLtrlDistLt = Double.NaN;
        private double aldwLaneLtrlDistRt = Double.NaN;
        private LaneMarkTypeEnum laneMarkTypeLeft = null;
        private LaneMarkTypeEnum laneMarkTypeRight = null;
        private LaneMarkType2Enum laneMarkTypeLeft2 = null;
        private LaneMarkType2Enum laneMarkTypeRight2 = null;
        private LaneMarkColEnum laneMarkColLeft = null;
        private LaneMarkColEnum laneMarkColRight = null;
        private double aldw_LaneMarkWidth_lt = Double.NaN;
        private double aldw_LaneMarkWidth_rt = Double.NaN;
        private double yawRate = Double.NaN;
        private double yawRateRq = Double.NaN;
        private double yawRateOff = Double.NaN;
        private double pitchAngle = Double.NaN;
        private double vehicleXAcc = Double.NaN;
        private double vehicleYAcc = Double.NaN;
        private double vehicleXAccOff = Double.NaN;
        private double vehicleYAccOff = Double.NaN;
        private double roadBorderLt = Double.NaN;
        private double roadBorderRt = Double.NaN;
        private double stwhlAngleSpeed = Double.NaN;
        private double stwhlAngle = Double.NaN;
        private double stwhlSw = Double.NaN;
        private double dispSpeed = Double.NaN;
        private boolean containsALDW = false;
        private boolean containsDISTRONIC = false;
        private boolean containsNullSpeed = false;

        public Builder(long time, Point lonLatPoint) {
            this.time = time;
            this.lonLatPoint = lonLatPoint;
            this.coor = Util.latLongPointToUtm(lonLatPoint);
        }

        /**
         * Use this constructor to copy all field to the new {@link TracePoint}
         * and additionally update ur desired fields of ur choice. <br>
         * Either lon/lat OR coor should be updated. If u do so at the same time
         * the other will be updated as well.
         * 
         * @param tp
         *            Current {@link TracePoint} U want to copy fields of.
         */
        public Builder(TracePoint tp) {
            this.time = tp.getTime();
            this.lonLatPoint = tp.getPoint();
            this.coor = tp.getCoor();
            this.parentTrace = tp.getParentTrace();
            this.altitude = tp.getAltitude();
            this.heading = tp.getHeading();
            this.wgsHead = tp.getWgsHead();
            this.speed = tp.getSpeed();
            this.aldwLaneLtrlDistLt = tp.getAldwLaneDistLeft();
            this.aldwLaneLtrlDistRt = tp.getAldwLaneDistRight();
            this.laneMarkTypeLeft = tp.getLaneMarkTypeLeft();
            this.laneMarkTypeRight = tp.getLaneMarkTypeRight();
            this.laneMarkTypeLeft2 = tp.getLaneMarkTypeLeft2();
            this.laneMarkTypeRight2 = tp.getLaneMarkTypeRight2();
            this.aldw_LaneMarkWidth_lt = tp.getAldw_LaneMarkWidth_lt();
            this.aldw_LaneMarkWidth_rt = tp.getAldw_LaneMarkWidth_rt();
            this.laneMarkColLeft = tp.getLaneMarkColLeft();
            this.laneMarkColRight = tp.getLaneMarkColRight();
            this.yawRate = tp.getYawRate();
            this.yawRateRq = tp.getYawRateRq();
            this.yawRateOff = tp.getYawRateOff();
            this.pitchAngle = tp.getPitchAngle();
            this.vehicleXAcc = tp.getVehicleXAcc();
            this.vehicleYAcc = tp.getVehicleYAcc();
            this.vehicleXAccOff = tp.getVehicleXAccOff();
            this.vehicleYAccOff = tp.getVehicleYAccOff();
            this.roadBorderLt = tp.getRoadBorderLt();
            this.roadBorderRt = tp.getRoadBorderRt();
            this.stwhlAngleSpeed = tp.getStwhlAngleSpeed();
            this.stwhlAngle = tp.getStwhlAngle();
            this.stwhlSw = tp.getStwhlSw();
            this.dispSpeed = tp.getDispSpeed();

            this.containsALDW = tp.isContainsALDW();
            this.containsDISTRONIC = tp.isContainsDISTRONIC();
            this.containsNullSpeed = tp.isContainsNullSpeed();
        }

        public Builder time(long val) {
            time = val;
            return this;
        }

        public Builder lonLatPoint(Point val) {
            this.lonLatPoint = val;
            this.coor = Util.latLongPointToUtm(val);
            return this;
        }

        public Builder coordinate(Coordinate val) {
            this.coor = val;
            this.lonLatPoint = Util.utmToLatLongPoint(val);
            return this;
        }

        public Builder parentTrace(Trace val) {
            parentTrace = val;
            return this;
        }

        public Builder altitude(double val) {
            altitude = val;
            return this;
        }

        public Builder heading(double val) {
            //TODO: change wgsHead if heading is changes and the other way around
            heading = val;
            return this;
        }

        public Builder wgsHead(double val) {
            wgsHead = val;
            return this;
        }

        public Builder speed(double val) {
            speed = val;
            return this;
        }
        
        public Builder speedSI(double val) {
            speed = val * 3.6;
            return this;
        }

        public Builder aldwLaneLtrlDistLt(double val) {
            aldwLaneLtrlDistLt = val;
            return this;
        }

        public Builder aldwLaneLtrlDistRt(double val) {
            aldwLaneLtrlDistRt = val;
            return this;
        }

        public Builder laneMarkTypeLeft(LaneMarkTypeEnum val) {
            laneMarkTypeLeft = val;
            return this;
        }

        public Builder laneMarkTypeLeft2(LaneMarkType2Enum val) {
            laneMarkTypeLeft2 = val;
            return this;
        }

        public Builder laneMarkTypeRight(LaneMarkTypeEnum val) {
            laneMarkTypeRight = val;
            return this;
        }

        public Builder laneMarkTypeRight2(LaneMarkType2Enum val) {
            laneMarkTypeRight2 = val;
            return this;
        }

        public Builder aldw_LaneMarkWidth_lt(double val) {
            aldw_LaneMarkWidth_lt = val;
            return this;
        }

        public Builder aldw_LaneMarkWidth_rt(double val) {
            aldw_LaneMarkWidth_rt = val;
            return this;
        }

        public Builder laneMarkColLeft(LaneMarkColEnum val) {
            laneMarkColLeft = val;
            return this;
        }

        public Builder laneMarkColRight(LaneMarkColEnum val) {
            laneMarkColRight = val;
            return this;
        }

        public Builder yawRate(double val) {
            yawRate = val;
            return this;
        }
        
        public Builder yawRateDeg(double val){
            yawRate = val * Math.PI / 180.0;
            return this;
        }

        public Builder yawRateRq(double val) {
            yawRateRq = val;
            return this;
        }

        public Builder yawRateOff(double val) {
            yawRateOff = val;
            return this;
        }

        public Builder pitchAngle(double val) {
            pitchAngle = val;
            return this;
        }

        public Builder vehicleXAcc(double val) {
            vehicleXAcc = val;
            return this;
        }

        public Builder vehicleYAcc(double val) {
            vehicleYAcc = val;
            return this;
        }

        public Builder vehicleXAccOff(double val) {
            vehicleXAccOff = val;
            return this;
        }

        public Builder vehicleYAccOff(double val) {
            vehicleYAccOff = val;
            return this;
        }

        public Builder roadBorderLt(double val) {
            roadBorderLt = val;
            return this;
        }

        public Builder roadBorderRt(double val) {
            roadBorderRt = val;
            return this;
        }

        public Builder stwhlAngleSpeed(double val) {
            stwhlAngleSpeed = val;
            return this;
        }

        public Builder stwhlAngle(double val) {
            stwhlAngle = val;
            return this;
        }

        public Builder stwhlSw(double val) {
            stwhlSw = val;
            return this;
        }

        public Builder dispSpeed(double val) {
            dispSpeed = val;
            return this;
        }

        public Builder containsALDW(boolean val) {
            containsALDW = val;
            return this;
        }

        public Builder containsDISTRONIC(boolean val) {
            containsDISTRONIC = val;
            return this;
        }

        public Builder containsNullSpeed(boolean val) {
            containsNullSpeed = val;
            return this;
        }

        public TracePoint build() {
            return new TracePoint(this);
        }

        public long getTime() {
            return time;
        }

        public Point getLonLatPoint() {
            return lonLatPoint;
        }

    }
}
