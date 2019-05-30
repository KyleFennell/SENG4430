package com.dcaiti.traceloader.odometrie.blender;

import java.util.ArrayList;
import java.util.List;

import com.dcaiti.traceloader.Trace;
import com.dcaiti.traceloader.TraceLoaderAccumulo2;
import com.dcaiti.traceloader.TracePoint;
import com.dcaiti.traceloader.TraceTools;
import com.dcaiti.utilities.KMLWriter;
import com.dcaiti.utilities.Point;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.math.Vector2D;

public class TestMathBlender {
	
    public static void main(String[] args) {
         testWithRealData();
//        testWithDetermenisticCurves2();
    }
	
    public static TracePoint buildTracePoint(TracePoint tp, double[] status) {
        if (status.length == 7) {
            return new TracePoint.Builder(tp).coordinate(new Coordinate(status[0], status[1])).speed(status[2] * 3.6)
                    .heading(new Vector2D(status[3], status[4]).angle())
                    .yawRate(new Vector2D(status[5], status[6]).angle()).build();
        } else {
            return new TracePoint.Builder(tp).coordinate(new Coordinate(status[0], status[1])).speed(status[2] * 3.6)
                    .heading(status[3]).yawRate(status[4]).build();
        }

    }

    public static void testWithRealData() {
        TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2(false, false);
        String vehId = "Section_20150319_234701";
        loader.loadDaimlerTracesByVehicleId(vehId);
        TraceTools.visualizeTraces(loader.traces, "results/deterministic/real_data6/orig.kml", KMLWriter.BLUE);
        // TraceTools.visualizeTracePts(loader.traces,
        // "results/deterministic/real_data4/orig_tp.kml");
        MathBlender blend = new MathBlender(true);
        blend.subscribe(new GenericMathBlenderModule(), 0);
        blend.subscribe(new MathBlenderDetermenisticModel(), 1);

        Trace blended = new Trace();
        Trace det = new Trace();
        List<Trace> pairs = new ArrayList<Trace>();

        int counter = 0;

        for (TracePoint tp : loader.traces.get(0).tracePts) {
            if (tp.getSpeed() < 10) {
                //index is not used by MathBlender, for the weight the initalWeight should be used,
                //6 stops till weight should be changed
                blend.startWorking(new MathBlenderTrigger(0, null, 6));
            }
            // getPredictionOfModule has to be called before the actual
            // prediction! -> only useful for debugging
            det.add(buildTracePoint(tp, blend.getPredictionOfModule(1, tp.getSensorVehicleModel().getMathState())));
            try {
                blended.add(buildTracePoint(tp, blend.predict(tp.getSensorVehicleModel().getMathState()).toArray()));
            } catch (Exception e) {
                blended.add(tp);
                e.printStackTrace();
            }
            Trace pair = new Trace();

            pair.add(loader.traces.get(0).tracePts.get(counter));
            ++counter;
            pair.add(blended.tracePts.get(blended.size() - 1));
            pair.add(det.tracePts.get(det.size() - 1));
            pairs.add(pair);
        }

        TraceTools.visualizeTrace(blended, "results/deterministic/real_data6/blended.kml", KMLWriter.RED);
        // TraceTools.visualizeTracePts(blended,"results/deterministic/real_data4/blended_tp.kml");
        TraceTools.visualizeTrace(det, "results/deterministic/real_data6/det.kml", KMLWriter.GREEN);
        TraceTools.visualizeTraces(pairs, "results/deterministic/real_data6/pairs.kml", KMLWriter.GRAY);

    }

    public static void testWithDetermenisticCurves2() {
        // String vehId = "Section_20150319_234701";
        boolean tuning = false;
        TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2(false, false);
        String vehId = "cmtcde2u062_25968";
        double hz = 1;
        if(tuning){
            hz = 0.02;
            loader.loadDaimlerTracesByVehicleId(vehId,"foba_daimler_traces_test_50_1", true);
        }else{
            loader.loadDaimlerTracesByVehicleId(vehId);
        }
        TraceTools.visualizeTraces(loader.traces, "results/deterministic/3/orig.kml", KMLWriter.BLUE);
        TraceTools.visualizeTracePts(loader.traces,"results/deterministic/3/orig_tp.kml");
        MathBlender blend = new MathBlender();
        blend.subscribe(new MathBlenderDetermenisticModel(hz));

        List<TracePoint> list = loader.traces.get(0).tracePts;
        // int start = 480;
        List<Trace> dets = new ArrayList<Trace>();
        int[] starts = new int[4];
        starts[0] = 1;
        starts[1] = 100;
        starts[2] = 250;
        starts[3] = 400;

        for (int j = 0; j < starts.length; ++j) {
            Trace det = new Trace();
            for (int i = starts[j]; i < list.size(); ++i) {
                TracePoint tp = list.get(i);
                try {
                    det.add(buildTracePoint(tp, blend.predict(tp.getSensorVehicleModel().getMathState()).toArray()));
                } catch (Exception e) {
                    det.add(tp);
                    e.printStackTrace();
                }
            }
            dets.add(det);
            blend.reset();
        }

        TraceTools.visualizeTraces(dets, "results/deterministic/3/det.kml", KMLWriter.GREEN);
        // TraceTools.visualizeTracePts(det,
        // "results/deterministic/det_tp.kml");
    }

    public static void testWithDetermenisticCurves() {
        TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2(false, false);
        String vehId = "cmtcde2u062_32006";
        loader.loadDaimlerTracesByVehicleId(vehId);
        TraceTools.visualizeTraces(loader.traces, "results/deterministic/orig.kml", KMLWriter.BLUE);
        TraceTools.visualizeTracePts(loader.traces, "results/deterministic/orig_tp.kml");
        MathBlender blend = new MathBlender();
        blend.subscribe(new MathBlenderDetermenisticModel());

        Trace det = new Trace();
        List<TracePoint> list = loader.traces.get(0).tracePts;
        list.remove(0);

        for (TracePoint tp : list) {
            try {
                det.add(buildTracePoint(tp, blend.predict(tp.getSensorVehicleModel().getState()).toArray()));
            } catch (Exception e) {
                det.add(tp);
                e.printStackTrace();
            }
        }

        TraceTools.visualizeTrace(det, "results/deterministic/det.kml", KMLWriter.RED);
        TraceTools.visualizeTracePts(det, "results/deterministic/det_tp.kml");
    }

    @SuppressWarnings("deprecation")
    public static void testWithTraces() {

        /**
         * Test with a real deterministic model and one with gps-error
         */

        Trace trace = new Trace();
        Trace tracePred = new Trace();
        Trace traceDet = new Trace();
        TracePoint tp = new TracePoint.Builder(0, new Point(13, 52)).build();
        double speed = 20;
        double heading = 0;
        double h1 = Math.cos(heading);
        double h2 = Math.sin(heading);

        tp = new TracePoint.Builder(tp).speed(speed).heading(heading).build();

        trace.add(tp);

        double[] init = new double[4];
        init[0] = tp.getCoor().x;
        init[1] = tp.getCoor().y;
        init[2] = heading;
        init[3] = speed;

        MathBlender blend = new MathBlender();
        blend.subscribe(new MathBlenderDetermenisticModel(), 0);
        blend.subscribe(new GenericMathBlenderModule(), 1);
        blend.startWorking(init);

        for (int i = 0; i < 100; ++i) {
            TracePoint meas = new TracePoint.Builder(tp)
                    .coordinate(new Coordinate(tp.getCoor().x + h1 * speed * i + TraceOdometrie.rand(100, 0),
                            tp.getCoor().y + h2 * speed * i + TraceOdometrie.rand(100, 0)))
                    .build();
            trace.add(meas);
            double[] measurement = new double[4];
            measurement[0] = meas.getCoor().x;
            measurement[1] = meas.getCoor().y;
            measurement[3] = meas.getHeading();
            measurement[2] = meas.getSpeed();
            traceDet.add(new TracePoint.Builder(tp)
                    .coordinate(new Coordinate(tp.getCoor().x + h1 * speed * i, tp.getCoor().y + h2 * speed * i)).build());
            double[] pred;
            try {
                pred = blend.predict(measurement).toArray();
            } catch (Exception e) {
                pred = new double[4];
                e.printStackTrace();
            }
            tracePred.add(new TracePoint.Builder(tp).coordinate(new Coordinate(pred[0], pred[1])).speed(pred[2])
                    .heading(pred[3]).build());
        }

        List<Trace> orig = new ArrayList<Trace>();
        List<Trace> det = new ArrayList<Trace>();
        List<Trace> pred = new ArrayList<Trace>();

        orig.add(trace);
        det.add(traceDet);
        pred.add(tracePred);

        TraceTools.visualizeTraces(orig, "results/MathBlender/orig.kml", KMLWriter.GREEN);
        TraceTools.visualizeTraces(det, "results/MathBlender/det.kml", KMLWriter.RED);
        TraceTools.visualizeTraces(pred, "results/MathBlender/pred.kml", KMLWriter.BLUE);

    }

    public static void testWithConstants() {
        int dim = 3;
        double[] state = new double[dim];
        double[] upState = new double[dim];
        double[] downState = new double[dim];
        for (int i = 0; i < state.length; ++i) {
            state[i] = 50;
            upState[i] = 100 * i;
            downState[i] = -10 * i;
        }
        @SuppressWarnings("deprecation")
        MathBlender blend = new MathBlender(state);
        MathBlenderModule upper = new GenericMathBlenderModule(upState);
        MathBlenderModule downer = new GenericMathBlenderModule(downState);

        blend.subscribe(upper);
        blend.subscribe(downer);

        OutputString(blend, 10);
    }

    public static void OutputString(MathBlender blend, int count) {
        double[][] meas = getRandomMeasurement(count, blend.getDim());
        double[][] prediction = new double[count][blend.getDim()];
        for (int i = 0; i < count; ++i) {
            try {
                prediction[i] = blend.predict(meas[i]).toArray();
                // System.out.println(Arrays.toString(prediction[i]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static double[][] getRandomMeasurement(int count, int dim) {
        double[][] meas = new double[count][dim];
        for (int i = 0; i < count; ++i) {
            for (int j = 0; j < dim; ++j) {
                meas[i][j] = Math.random();
            }
        }
        return meas;
    }

}
