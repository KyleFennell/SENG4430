package com.dcaiti.traceloader.odometrie;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.apache.commons.math3.linear.RealVector;
import com.dcaiti.traceloader.Trace;
import com.dcaiti.traceloader.TraceLoaderAccumulo2;
import com.dcaiti.traceloader.TracePoint;
import com.dcaiti.traceloader.TraceTools;
import com.dcaiti.traceloader.odometrie.blender.MathBlender;
import com.dcaiti.traceloader.odometrie.blender.MathBlenderDetermenisticModel;
import com.dcaiti.traceloader.odometrie.blender.MathBlenderModule;
import com.dcaiti.traceloader.odometrie.blender.TraceBlender;
import com.dcaiti.utilities.KMLWriter;
import com.vividsolutions.jts.geom.Coordinate;
import com.dcaiti.traceloader.odometrie.TraceFilter3.Output;

public class TraceAnalyser {
    
    public static void main(String[] args) {
//                
           String vehId = "Section_20150319_234701";// "cmtcde2u062_86540";     //"Section_20150319_234701" //cmtcde2u062_20395     //cmtcde2u062_59012
          Trace trace = loadTrace(vehId, false);
          
//          trace = trace.subTrace(60, 150);
          
          
          
//          TraceFilter3 filter = new TraceFilter3(trace);
//          filter.setFlag(EnumSet.of(Output.SHOW_COOR_LAG,Output.SHOW_SPEED_ERROR));
//          trace = filter.correctSensorError();
          
//          trace = trace.subTrace(1426810682980l, 1426810782980l);      //1426810622980           //60-150
          
          TraceFilter3 filter2 = new TraceFilter3(trace,true);
//          filter2.setFlag(EnumSet.of(Output.SHOW_COOR_LAG, Output.SHOW_COOR_OFFSET));
//          filter2.setFlag(EnumSet.of(Output.SHOW_HEADING_LAG, Output.SHOW_HEADING_OFFSET));
          filter2.setFlag(EnumSet.of(Output.SHOW_SPEED_ERROR, Output.SHOW_DRIFT));
          filter2.setIntegralMethod(ExtendedIntegral.IntegralMethod.MIN);
          filter2.setTimesToUse(TraceFilter3.TimeStamp.NORMAL);
//          filter2.setFlag(EnumSet.of(Output.SHOW_MERGED_OFFSET,Output.SHOW_COOR_OFFSET, Output.SHOW_HEADING_OFFSET));
//          trace = filter2.correctSensorLag();
//          trace = filter2.correctSensorError();
//          trace = filter2.correctWithKnownData(-1500l, 0.94455596446991, -1000l, 0.00862747982519951).getCorrectedTrace();
          trace = filter2.correct().getCorrectedTrace();
          
//          TraceTools.visualizeTrace(odometrieTrace2(trace,0), "results/odometrie/kinds/"+trace.getBaseVehId()+"/testing/odometrieTrace_notGoodFilter.kml", KMLWriter.RED);
          
//          visualizeHeading(trace);
//          visualizeDifferentOdometrieTraces(trace);
//          visualizeHeading(trace);
//        visualizeFilterImprovement(trace);
//          visualizeTimeDiff(trace);
//        testPathAugmenter();
//            writeStatisticAboutLength();
          
////          long start = System.nanoTime();                   
          
//          TraceMerger merge = new TraceMerger(trace);
//          trace = merge.improve();
//          long end = System.nanoTime();
//          merge.visualizeAllTraces(150);
//          System.out.println("Time elapsed: "+((end-start)/1000000)+" milliseconds by a Trace of size "+trace.size());
          
//            TraceTools.visualizeTrace(createOdometrieTrace(trace,10), "results/odometrie/trace.kml", KMLWriter.getColorString(Color.ORANGE));
//            TraceTools.visualizeTrace(createOdometrieTrace(trace,30), "results/odometrie/trace2.kml", KMLWriter.getColorString(Color.ORANGE));
          
//          SplineOnePartition one = new SplineOnePartition(new double[]{0,0,0,0,0.8,1,1,1,1},3);
//          SplineOnePartition one = new SplineOnePartition(new double[]{0,0,0,0.2,0.4,0.9,1,1,1},3);
//          SplineOnePartition one = new SplineOnePartition(new double[]{0.2,0.4},2);
//        SplineOnePartition one = new SplineOnePartition(5,3);
//          LinearOnePartition one = new LinearOnePartition(new double[]{0,0.2,0.4,0.9,1});
//          GaussianOnePartition one = new GaussianOnePartition(new double[]{0,0.2,0.4,0.9,1},null,null);
//          GaussianOnePartition one = new GaussianOnePartition(2);
//          one.visualize();
//          one2.visualize();
//          System.out.println(Arrays.toString(OnePartition.filterPeaks(new double[]{3,2,6,7,8})));
          
          
//          Trace detLag = detModelTrace(traceLag,1);
//          TraceTools.visualizeTrace(detLag,"results/deterministic/"+vehId+"/det_lag_upgraded.kml",KMLWriter.GREEN);
////        visualizeTraceHz("Section_20150319_234701",true);
//        writeStatisticAboutLength();
//          
                       
//          TraceFilter filter = new TraceFilter(trace);
//          filter.setFlag(EnumSet.of(TraceFilter.Output.SHOW_YAW));
//          trace = filter.correct();
              
//              trace = trace.subTrace(200, 250);
            
    }
        
    public static Trace odometrieTrace(Trace trace, int start){
        OdometriePrediction pred = new OdometriePrediction(trace.getTracePointAtIndex(start));
        Trace tr = new Trace(trace.getVehicleId());
        tr.add(trace.getTracePointAtIndex(start));
        for(int i = start; i < trace.size(); ++i){
            pred.predict(trace.getTracePointAtIndex(i));
            tr.add(pred.getState(trace.getTracePointAtIndex(i)));
        }
        System.out.println("Counter ist "+pred.counter);
        return tr;
    }
    
    public static Trace odometrieTrace2(Trace trace, int start){
        OdometriePrediction2 pred = new OdometriePrediction2(trace);
        pred.startBy(trace.getTracePointAtIndex(start));
        Trace tr = new Trace(trace.getVehicleId());
        tr.add(trace.getTracePointAtIndex(start));
        for(int i = start; i < trace.size(); ++i){
            pred.predict(trace.getTracePointAtIndex(i));
            tr.add(pred.getState(trace.getTracePointAtIndex(i)));
        }
        return tr;
    }
    
    public static void visualizeHeading(Trace trace){
        List<Trace> list = new ArrayList<Trace>(trace.size());
        for(int i = 0; i < trace.size(); ++i){
            Trace head = new Trace();
            TracePoint tp = trace.getTracePointAtIndex(i);
            head.add(tp);
            double h1 = Math.cos(tp.getHeading());
            double h2 = Math.sin(tp.getHeading());
            double x = tp.getCoor().x + h1*20;
            double y = tp.getCoor().y + h2*20;
            head.add(new TracePoint.Builder(trace.getTracePointAtIndex(i)).coordinate(new Coordinate(x,y)).build());
            list.add(head);
        }
        TraceTools.visualizeTrace(trace, "results/odometrie/heading/"+trace.getBaseVehId()+"/trace.kml", KMLWriter.BLUE);
        TraceTools.visualizeTraces(list, "results/odometrie/heading/"+trace.getBaseVehId()+"/trace_headings.kml", KMLWriter.RED);
    }
    
    /**
     * 
     * @param trace
     */
    public static void visualizeTimeDiff(Trace trace){
        Trace diff = new Trace(trace.getVehicleId());
        int counter = 0;
        double allowedTimediff = 0.05;
        for(int i = 1; i < trace.size(); ++i){
            double timediff = trace.getTracePointAtIndex(i).getTimeSI() - trace.getTracePointAtIndex(i-1).getTimeSI();
            timediff = Math.abs(timediff);
            timediff -= 1;
            timediff = Math.abs(timediff);
            if(timediff > allowedTimediff){
                diff.add(trace.getTracePointAtIndex(i));
                diff.add(trace.getTracePointAtIndex(i-1));
                ++counter;
            }
        }
        System.out.println(counter+" TimeJumps were found");
        TraceTools.visualizeTracePts(diff, "results/odometrie/tracePts_time.kml");
        TraceTools.visualizeTrace(trace, "results/odometrie/trace_time.kml", KMLWriter.BLUE);
    }
    
    public static void visualizeFilterImprovement(Trace trace){
        //start for every 100 TP
        int count = (trace.size() - 200) / 100;
        List<Integer> starts = new ArrayList<Integer>();
        for(int i = 0; i < count; ++i){
            starts.add(i*100+50);
        }
        //get colors
        List<Color> colors = KMLWriter.getColorsInRange(Color.GREEN, Color.RED, count);
        for(int i = 0; i < count; ++i){
            TraceTools.visualizeTrace(odometrieTrace(trace,starts.get(i)), "results/odometrie/notImproved/odometrieTrace_"+i+".kml", KMLWriter.getColorString(colors.get(i)));
        }
        TraceTools.visualizeTrace(trace, "results/odometrie/notImproved/origTrace.kml", KMLWriter.BLUE);
        
        //filter the trace
        TraceFilter filter = new TraceFilter(trace);
        filter.setFlag(EnumSet.of(TraceFilter.Output.SHOW_DRIFT));
        trace = filter.correct().getCorrectedTrace();
        
        for(int i = 0; i < count; ++i){
            TraceTools.visualizeTrace(odometrieTrace(trace,starts.get(i)), "results/odometrie/improved/odometrieTrace_"+i+".kml", KMLWriter.getColorString(colors.get(i)));
        }
        TraceTools.visualizeTrace(trace, "results/odometrie/improved/origTrace.kml", KMLWriter.BLUE);
        
        
        //visualize every start point
        Trace startPoints = new Trace();
        for(int start : starts){
            startPoints.add(trace.getTracePointAtIndex(start));
        }
        TraceTools.visualizeTracePts(startPoints, "results/odometrie/improved/startPoints.kml");
        TraceTools.visualizeTracePts(startPoints, "results/odometrie/notImproved/startPoints.kml");
    }
    
    public static void printTracesDiffOfFilter(Trace trace){
        TraceFilter filter = new TraceFilter(trace);
        filter.setFlag(TraceFilter.Output.SHOW);
        filter.correctSensorLag();
        Trace trace_corr = filter.correctSpeedError();
        Trace det = detModelTrace(trace,new double[]{0,0.001},0);
        Trace det_corr = detModelTrace(trace_corr,new double[]{0,0.001},0);
        String vehId = trace.getBaseVehId();
        TraceTools.visualizeTrace(trace,"results/filter/"+vehId+"/trace.kml",KMLWriter.BLUE);
        TraceTools.visualizeTrace(trace_corr,"results/filter/"+vehId+"/trace_corr.kml",KMLWriter.TRANS_BLUE);
        TraceTools.visualizeTrace(det,"results/filter/"+vehId+"/det.kml",KMLWriter.RED);
        TraceTools.visualizeTrace(det_corr,"results/filter/"+vehId+"/det_corr.kml",KMLWriter.GREEN);
    }
    
    public static void visualizeTraceHz(String vehId, boolean points){
        String tableName50 = "foba_daimler_traces_test_50_1";
        TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2(false, false);
        loader.loadDaimlerTracesByVehicleId(vehId, tableName50, true);
        Trace trace50 = loader.traces.get(0);
        String tableName1 = "foba_daimler_traces_test_1_1";
        loader.loadDaimlerTracesByVehicleId(vehId, tableName1, true);
        Trace trace1 = loader.traces.get(0);
        
        System.out.println(testIfTraceCorrect(trace1));
        System.out.println(testIfTraceCorrect(trace50));
        
        String file_path = "results/orig/upgraded/"+vehId+"/";
        
        TraceTools.visualizeTrace(trace50, file_path+"trace50.kml", KMLWriter.GREEN);
        TraceTools.visualizeTrace(trace1, file_path+"trace1.kml", KMLWriter.RED);
        if(points){
            TraceTools.visualizeTracePts(trace50, file_path+"trace50_tp.kml");
            TraceTools.visualizeTracePts(trace1, file_path+"trace1_tp.kml");
        }
    }
    
    //testet ob traces von den time-stamps richtig sortiert sind
    public static boolean testIfTraceCorrect(Trace trace){
        long time = 0;
        
        for(TracePoint tp : trace){
            if(tp.getTime() < time){
                return false;
            }else{
                time = tp.getTime();
            }
        }
        
        return true;
    }
    
    public static void writeStatisticAboutPlaces(){
        String[] heading = new String[]{"Hz","Traces","Standing","Spinning","neg. Spinning","pos. Spinning","Resting"};
        int[] ids = new int[]{
                49450,
//                50602,
                51201,
                52708,
                52709,
                53500,
//                53600,
                53700,
                54050
        };
        int[][] values = new int[ids.length][heading.length];
        
        TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2(false, false);
        double threshold = 0.1;
        
        for(int i = 0; i < ids.length; ++i){
            loader.loadDaimlerTracesByItefId(ids[i]);
            values[i][0] = ids[i];
            int[] src = countPlacesStanding(loader.traces,threshold);
            System.arraycopy(src, 0, values[i], 1, src.length);
//            values[i] = countPlacesStanding(loader.traces,threshold);
//            values[i][heading.length-1] = threshold;
        }
        
        String filename = "results/statistics/places/stops_tab.txt";
        
        writeInCSV(heading,values,filename);
    }
    
    public static Trace loadTrace(String vehId,boolean Hz){
        String table = "foba_daimler_traces_1";
        if(Hz){
            table = "foba_daimler_traces_test_50_1";
        }
        TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2(false, false);
//        String vehId = "Section_20150319_234701";
        loader.loadDaimlerTracesByVehicleId(vehId,table, false);     //"foba_daimler_traces_2"
        
        if(loader.traces.size() == 0){
            if(Hz){
                table = "foba_daimler_traces_2";
            }else{
                table ="foba_daimler_traces_1";
            }
            loader.loadDaimlerTracesByVehicleId(vehId,table,false);
        }
        
        
        return loader.traces.get(0);
    }
        
    public static void writeStatisticAboutLength(){
        String[] heading = new String[]{"vehId","Frequenz","orig_Coor","orig_Speed","det_time_Coor","det_time_Speed","det_freq_Coor","det_freq_Speed"};
        
        TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2(false, false);
//        loader.loadDaimlerTracesByItefId(fobaId, "foba_daimler_traces_test_1_1");
        ArrayList<Trace> traces1 = new ArrayList<Trace>();
        ArrayList<Trace> traces50 = new ArrayList<Trace>();
        String[] vehId = new String[] {
                "cmtcde2u062_144",
                "cmtcde2u062_160",
                "cmtcde2u062_1618",
                "cmtcde2u062_1640",
                "cmtcde2u062_173",
                "cmtcde2u062_20395",
                "cmtcde2u062_23572",
                "cmtcde2u062_24503",
                "cmtcde2u062_26476",
                "cmtcde2u062_29075",
                "cmtcde2u062_32488",
                "cmtcde2u062_36200",
                "cmtcde2u062_41731",
                "cmtcde2u062_46001",
                "cmtcde2u062_52278",
                "cmtcde2u062_59012",
                "cmtcde2u062_64729",
                "cmtcde2u062_69486",
                "cmtcde2u062_76641",
                "cmtcde2u062_80272",
                "cmtcde2u062_86540",
                "cmtcde2u062_90130",
                "cmtcde2u062_95042",
                "cmtcde2u062_97385"
        };
                
        for(int i = 0; i < vehId.length; ++i){
            loader.loadDaimlerTracesByVehicleId(vehId[i], "foba_daimler_traces_test_1_1", true);
//            traces1.add(TraceFilter.correct(loader.traces.get(0)));
            traces1.add(loader.traces.get(0));
            loader.loadDaimlerTracesByVehicleId(vehId[i], "foba_daimler_traces_test_50_1", true);
//            traces50.add(TraceFilter.correct(loader.traces.get(0)));
            traces50.add(loader.traces.get(0));
        }
        
        String[][] first = new String[traces1.size()][heading.length];
        double freq = 1;
        
        for(int i = 0; i < traces1.size(); ++i){            
            Trace orig = traces1.get(i);
            first[i][0] = orig.getVehicleId();
            first[i][1] = Double.toString(freq);
            first[i][2] = String.format("%1$,.2f",orig.lengthFromCoor());
            first[i][3] = String.format("%1$,.2f",orig.lengthFromSpeed());
            Trace det_time = detModelTrace(orig,0);
            first[i][4] = String.format("%1$,.2f",det_time.lengthFromCoor());
            first[i][5] = String.format("%1$,.2f",det_time.lengthFromSpeed());
            Trace det_freq = detModelTrace(orig,freq);
            first[i][6] = String.format("%1$,.2f",det_freq.lengthFromCoor());
            first[i][7] = String.format("%1$,.2f",det_freq.lengthFromSpeed());
        }
        
//        loader.loadDaimlerTracesByItefId(fobaId, "foba_daimler_traces_test_50_1");
        String[][] second = new String[traces50.size()][heading.length];
        freq = 0.02;
        
        for(int i = 0; i < traces50.size(); ++i){            
            Trace orig = traces50.get(i);
            second[i][0] = orig.getVehicleId();
            second[i][1] = Double.toString(freq);
            second[i][2] = String.format("%1$,.2f",orig.lengthFromCoor());
            second[i][3] = String.format("%1$,.2f",orig.lengthFromSpeed());
            Trace det_time = detModelTrace(orig,0);
            second[i][4] = String.format("%1$,.2f",det_time.lengthFromCoor());
            second[i][5] = String.format("%1$,.2f",det_time.lengthFromSpeed());
            Trace det_freq = detModelTrace(orig,freq);
            second[i][6] = String.format("%1$,.2f",det_freq.lengthFromCoor());
            second[i][7] = String.format("%1$,.2f",det_freq.lengthFromSpeed());
        }
        
        String filename50 = "results/statistics/length/upgraded3/length50Hz_speedError_4.csv";
        String filename1 = "results/statistics/length/upgraded3/length1Hz_speedError.csv";
        String filename = "results/statistics/length/upgraded3/length_speedError.csv";   
        
        writeInCSV(heading,second,filename50);
        writeInCSV(heading,first,filename1);
        if(traces1.size() == traces50.size()){
            writeInCSV(heading, merge(first,second), filename);            
        }else{
            System.out.print("Traces von 1Hz und 50Hz Tabelle sind nicht identisch!");
        }
        
    }
    
    public static String[][] merge(String[][] first, String[][] second){
        if(first.length == 0){
            return null;
        }
        String[][] merge = new String[first.length + second.length][first[0].length];
        int j = 0, k = 0, l = 0;
        int max = Math.max(first.length, second.length);
        for (int i = 0; i < max; i++) {
            if (j < first.length)
                merge[l++] = first[j++];
            if (k < second.length)
                merge[l++] = second[k++];
        }
        return merge;
    }
    
    public static void writeInCSV(String[] heading, int[][] values, String filename){
        try {
            PrintWriter pw = new PrintWriter(new File(filename));
            StringBuilder sb = new StringBuilder();
            char enter = '\n';
            char comma = ';';
//            char enter = '\\';
//            char comma = '&';
            
            for(int i = 0; i < heading.length - 1; ++i){
                sb.append(heading[i]);
                sb.append(comma);
            }
            sb.append(heading[heading.length-1]);
            sb.append(enter);
            
            for(int i = 0; i < values.length; ++i){
                for(int j = 0; j < values[i].length -1; ++j){
                    sb.append(values[i][j]); sb.append(comma);
                }
                sb.append(values[i][values[i].length-1]); sb.append(enter);
            }
            
            pw.write(sb.toString()); pw.close();
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static void writeInCSV(String[] heading, String[][] values, String filename){
        
        if(values == null){
            return;
        }
        
        try {
            PrintWriter pw = new PrintWriter(new File(filename));
            StringBuilder sb = new StringBuilder();
            char enter = '\n';
            char comma = ';';
//            char enter = '\\';
//            char comma = '&';
            
            for(int i = 0; i < heading.length - 1; ++i){
                sb.append(heading[i]);
                sb.append(comma);
            }
            sb.append(heading[heading.length-1]);
            sb.append(enter);
            
            for(int i = 0; i < values.length; ++i){
                for(int j = 0; j < values[i].length -1; ++j){
                    sb.append(values[i][j]); sb.append(comma);
                }
                sb.append(values[i][values[i].length-1]); sb.append(enter);
            }
            
            pw.write(sb.toString()); pw.close();
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Deprecated
    public static Trace detModelTrace(Trace trace, double[] param, double frequency){
        
        MathBlender blend = new MathBlender();
        MathBlenderModule mod = new MathBlenderDetermenisticModel(frequency);
        mod.correct(param);
        blend.subscribe(mod);
        Trace det = new Trace();
        
        for (int i = 0; i < trace.size(); i++) {
            
            TracePoint detTP = null;
            
            try {
                RealVector pred = blend.predict(trace.getTracePointAtIndex(i).getSensorVehicleModel().getMathState());
                detTP = TraceBlender.createTracePoint(trace.getTracePointAtIndex(i),pred.toArray());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                detTP = trace.getTracePointAtIndex(i);
                e.printStackTrace();
            }
            det.add(detTP);
//            sum += det.getCoor().distance(trace.getTracePointAtIndex(i).getCoor());
            
        }
        return det;
    }
    
    @Deprecated
    public static Trace detModelTrace(Trace trace, double frequency){
        return detModelTrace(trace,new double[]{0,0},frequency);
    }
   
    
    /** Method which counts the number of places, in which the vehicle stands still but has a yaw-rate
     * 
     * @return
     */
    @Deprecated
    public static int countStandingButSpinning(List<Trace> traces, double threshold){
        
        int counter = 0;
        boolean spinning = false;
        
        for(Trace trace : traces){
            for(TracePoint tp : trace){
                if(tp.getSpeed() > 0 && spinning){
                    spinning = false;
                }
                if(tp.getSpeed() == 0 && Math.abs(tp.getYawRate()) > threshold && !spinning){
                    spinning = true;
                    ++counter;
                }
            }
            spinning = false;
        }
        return counter;
    }
    
    public static int[] countPlacesStanding(List<Trace> traces, double threshold){
        
        int counterStanding = 0;
        int counterSpinning = 0;
        int counterSpinningResting = 0;
        int counterSpinningPos = 0;
        int counterSpinningNeg = 0;
        
        boolean standing = false;
        boolean spinning = false;
        
        for(Trace trace : traces){
            for(TracePoint tp : trace){
                //standing
                if(tp.getSpeed() == 0){
                    if(standing == false){
                        ++counterStanding;
                    }
                    standing = true;
                    //spinning
                    if(Math.abs(tp.getYawRate()) > threshold){
                        if(spinning == false){
                            ++counterSpinning;
                            if(tp.getYawRate() > 0){
                                ++counterSpinningPos;
                            }else{
                                ++counterSpinningNeg;
                            }
                        }
                        spinning = true;
                    }
                    //resting
                    if(tp.getYawRate() == 0 && spinning){
                        spinning = false;
                        ++counterSpinningResting;
                    }
                }else{
                    standing = false;
                    spinning = false;
                }
            }
            standing = false;
            spinning = false;
        }
        return new int[]{traces.size(),counterStanding,counterSpinning,counterSpinningNeg,counterSpinningPos,counterSpinningResting};
    }
    
}
