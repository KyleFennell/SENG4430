package com.dcaiti.traceloader;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

/**
 * Gives details about each field uncluding units sensor boundaries.
 * 
 * TODO: expand sensor limits and units for each field.
 * 
 * @author Marius Hauschild
 *
 */
public class TracePointFieldProperties {

    private double lowerSensorLimit;
    private double upperSensorLimit;

    private double lowerHistLimit;
    private double upperHistLimit;
    private double histBucketSize;
    
    private long updateInterval; //Hz in ms

    private Unit<?> fieldUnit;
    private String fieldOrLogID;

    public TracePointFieldProperties(String fieldOrLogID) {
        super();
        this.fieldOrLogID = fieldOrLogID;
        setClazzFields(fieldOrLogID);
    }

    /**
     * Returns for the corresponding field the interval boundaries for the
     * histogram.
     * 
     * @param f
     *            TracePoint.<code>field</code>
     * @return Returns {@link Unit} for known fields. Otherwise
     *         <code>null</code>.
     */
    private void setClazzFields(String s) {

        switch (s) {

        case "time":
            this.fieldUnit = SI.MILLI(SI.SECOND);
            break;

        case "heading":  //
            this.lowerHistLimit = 0.0;
            this.upperHistLimit = 2 * Math.PI;
            this.histBucketSize = this.upperHistLimit / 8.0;

            this.lowerSensorLimit = 0;
            this.upperSensorLimit = 2 * Math.PI;
            this.fieldUnit = SI.RADIAN;

            this.updateInterval = 1000;
            break;

        case "wgsHead":
        case "1000000000100000004":
            this.lowerHistLimit = 0.0;
            this.upperHistLimit = 360.0;
            this.histBucketSize = 45.0;

            this.lowerSensorLimit = 0;
            this.upperSensorLimit = 359.9;
            this.fieldUnit = NonSI.DEGREE_ANGLE; // or degree_angle?

            this.updateInterval = 1000;
            break;

        case "speed": 
        case "1000000000100000005":
            this.lowerHistLimit = 0.0;
            this.upperHistLimit = 120.0;
            this.histBucketSize = 5.0;

            this.lowerSensorLimit = 0;
            this.upperSensorLimit = 409.4;
            this.fieldUnit = NonSI.KILOMETRES_PER_HOUR;

            this.updateInterval = 20;
            break;

        case "dispSpeed": 
        case "100035":
            this.lowerHistLimit = 0.0;
            this.upperHistLimit = 120.0;
            this.histBucketSize = 5.0;

            this.lowerSensorLimit = 0;
            this.upperSensorLimit = 409.4;
            this.fieldUnit = SI.METER;

            this.updateInterval = 20;
            break;

        case "aldwLaneLtrlDistLt": 
        case "100058":
            this.lowerHistLimit = -3.0;
            this.upperHistLimit = 7.0;
            this.histBucketSize = 1.0;

            this.lowerSensorLimit = -2.56;
            this.upperSensorLimit = 7.65;
            this.fieldUnit = SI.METER;

            this.updateInterval = 20;
            break;

        case "aldwLaneLtrlDistRt": 
        case "100059":
            this.lowerHistLimit = -3.0;
            this.upperHistLimit = 7.0;
            this.histBucketSize = 1.0;

            this.lowerSensorLimit = -2.56;
            this.upperSensorLimit = 7.65;
            this.fieldUnit = SI.METER;

            this.updateInterval = 20;
            break;

        case "aldw_LaneMarkWidth_lt":
        case "100050":
            this.lowerHistLimit = 0.0;
            this.upperHistLimit = 1.3;
            this.histBucketSize = 0.1;

            this.lowerSensorLimit = 0;
            this.upperSensorLimit = 1.26;
            this.fieldUnit = SI.METER;

            this.updateInterval = 20;
            break;

        case "aldw_LaneMarkWidth_rt": 
        case "100051":
            this.lowerHistLimit = 0.0;
            this.upperHistLimit = 1.3;
            this.histBucketSize = 0.1;

            this.lowerSensorLimit = 0;
            this.upperSensorLimit = 1.26;
            this.fieldUnit = SI.METER;

            this.updateInterval = 20;
            break;

        case "roadBorderLt": 
        case "100009":
            this.lowerHistLimit = 0.0;
            this.upperHistLimit = 20.0;
            this.histBucketSize = 1.0;

            this.lowerSensorLimit = 0;
            this.upperSensorLimit = 25.4;
            this.fieldUnit = SI.METER;

            this.updateInterval = 20;
            break;

        case "roadBorderRt":
        case "100010":
            this.lowerHistLimit = 0.0;
            this.upperHistLimit = 20.0;
            this.histBucketSize = 1.0;

            this.lowerSensorLimit = 0;
            this.upperSensorLimit = 25.4;
            this.fieldUnit = SI.METER;

            this.updateInterval = 20;
            break;

        /* -327.68..+327.66 °/s */
        case "yawRate": 
        case "100001":
            this.lowerHistLimit = -40.0;
            this.upperHistLimit = 40.0;
            this.histBucketSize = 5.0;
//            this.lowerHistLimit = -0.1;
//            this.upperHistLimit = 0.1;
//            this.histBucketSize = 0.01;

            this.lowerSensorLimit = -327.68;
            this.upperSensorLimit = 327.66;
            this.fieldUnit = null; // grad/sekunde??? NonSI.DEGREE_ANGLE

            this.updateInterval = 20;
            break;

        case "yawRateRq": 
        case "100024":
            this.lowerHistLimit = 0.0;
            this.upperHistLimit = 5.0;
            this.histBucketSize = 0.5;
            // TODO verify
            this.lowerSensorLimit = -327.68;
            this.upperSensorLimit = 327.66;
            this.fieldUnit = null; // grad/sekunde???

            this.updateInterval = 20;
            break;


        case "vehicleXAcc":
        case "100069":
            this.lowerHistLimit = -5.0;
            this.upperHistLimit = 5.0;
            this.histBucketSize = 1.0;
            // TODO verify
            this.lowerSensorLimit = -5.12;
            this.upperSensorLimit = 102.1;
            this.fieldUnit = SI.METRES_PER_SQUARE_SECOND;

            this.updateInterval = 20;
            break;

        case "vehicleYAcc": 
        case "100071":
            this.lowerHistLimit = -7.0;
            this.upperHistLimit = 7.0;
            this.histBucketSize = 1.0;
            // TODO verify
            this.lowerSensorLimit = -5.12;
            this.upperSensorLimit = 102.1;
            this.fieldUnit = SI.METRES_PER_SQUARE_SECOND;

            this.updateInterval = 20;
            break;


        case "stwhlAngleSpeed":
        case "100081":
            this.lowerHistLimit = -300.0;
            this.upperHistLimit = 300.0;
            this.histBucketSize = 50.0;
            // TODO verify
            this.lowerSensorLimit = -700.0;
            this.upperSensorLimit = 700.0;
            this.fieldUnit = null;

            this.updateInterval = 20;
            break;

        case "stwhlAngle":
        case "100082":
            this.lowerHistLimit = -300.0;
            this.upperHistLimit = 300.0;
            this.histBucketSize = 50.0;
            // TODO verify
            this.lowerSensorLimit = -500.0;
            this.upperSensorLimit = 500.0;
            this.fieldUnit = null;

            this.updateInterval = 20;
            break;

        case "stwhlSw": // in dd7 data always = 0
        case "100083":
            this.lowerHistLimit = 0.0;
            this.upperHistLimit = 0.1;
            this.histBucketSize = 0.01;
            // TODO verify
            this.lowerSensorLimit = 0.0;
            this.upperSensorLimit = 0.1;
            this.fieldUnit = null;

            this.updateInterval = 20;
            break;

        case "laneMarkTypeLeftDouble":
        case "100061":
            this.lowerHistLimit = 0.0;
            this.upperHistLimit = 7.0;
            this.histBucketSize = 1.0;

            this.lowerSensorLimit = 0.0;
            this.upperSensorLimit = 6.0;
            this.fieldUnit = null;

            this.updateInterval = 20;
            break;

        case "laneMarkTypeRightDouble":
        case "100062":
            this.lowerHistLimit = 0.0;
            this.upperHistLimit = 7.0;
            this.histBucketSize = 1.0;

            this.lowerSensorLimit = 0.0;
            this.upperSensorLimit = 6.0;
            this.fieldUnit = null;

            this.updateInterval = 20;
            break;

        case "laneMarkColLeftDouble":
        case "100052":
           this.lowerHistLimit = 0.0;
            this.upperHistLimit = 3.0;
            this.histBucketSize = 1.0;

            this.lowerSensorLimit = 0.0;
            this.upperSensorLimit = 2.0;
            this.fieldUnit = null;

            this.updateInterval = 20;
            break;

        case "laneMarkColRightDouble":
        case "100053":
            this.lowerHistLimit = 0.0;
            this.upperHistLimit = 3.0;
            this.histBucketSize = 1.0;

            this.lowerSensorLimit = 0.0;
            this.upperSensorLimit = 2.0;
            this.fieldUnit = null;

            this.updateInterval = 20;
            break;
            
            //REMAINING ******************************
        case "altitude":
        case "100038":
            this.lowerHistLimit = 0.0;
            this.upperHistLimit = 100.0;
            this.histBucketSize = 10;

            this.lowerSensorLimit = 0; // first valid
            this.upperSensorLimit = 65534; // last valid
            this.fieldUnit = SI.METER;

            this.updateInterval = 20;
            break;

            /*-6.96..+6.96 °/s */
            case "yawRateOff": 
            case "100000":
                this.lowerHistLimit = -40.0;
                this.upperHistLimit = 40.0;
                this.histBucketSize = 5.0;

                this.lowerSensorLimit = -6.96;
                this.upperSensorLimit = 6.96;
                this.fieldUnit = null; // ???
                break;

            /* -5.12..+5.1 ° */
            case "pitchAngle": 
            case "100048":
                this.lowerHistLimit = 0.0;
                this.upperHistLimit = 0.35;
                this.histBucketSize = 0.05;

                this.lowerSensorLimit = -5.12;
                this.upperSensorLimit = 5.1;
                this.fieldUnit = null; // ???
                break;

            case "vehicleXAccOff": // not found in dd7 data
            case "100070":
                this.lowerHistLimit = -50.0;
                this.upperHistLimit = 1050.0;
                this.histBucketSize = 50.0;

                this.lowerSensorLimit = -1.0;
                this.upperSensorLimit = 1.0;
                this.fieldUnit = null; // ???
                break;

            case "vehicleYAccOff": // not found in dd7 data
            case "100072":
                this.lowerHistLimit = -7.0;
                this.upperHistLimit = 6.0;
                this.histBucketSize = 1.0;

                this.lowerSensorLimit = -1.0;
                this.upperSensorLimit = 1.0;
                this.fieldUnit = null; // ???
                break;


        default:
            this.lowerHistLimit = 0.0;
            this.upperHistLimit = 100.0;
            this.histBucketSize = 10.0;

            this.lowerSensorLimit = 0.0;
            this.upperSensorLimit = 100.0;
            this.fieldUnit = null;
            this.fieldOrLogID = "";

            this.updateInterval = 20;

            break;
        }
    }
    
    
    public boolean isSensorValueValid(double logValue) {
        return logValue <= this.upperSensorLimit && logValue >= this.lowerSensorLimit;
    }

    public boolean isValidFrequency(long time) {
        return time == updateInterval;
    }

    public double getLowerHistLimit() {
        return lowerHistLimit;
    }

    public double getUpperHistLimit() {
        return upperHistLimit;
    }

    public double getHistBucketSize() {
        return histBucketSize;
    }

    public Unit<?> getFieldUnit() {
        return fieldUnit;
    }

    public double getLowerSensorLimit() {
        return lowerSensorLimit;
    }

    public double getUpperSensorLimit() {
        return upperSensorLimit;
    }

    public String getFieldOrLogId() {
        return fieldOrLogID;
    }

}
