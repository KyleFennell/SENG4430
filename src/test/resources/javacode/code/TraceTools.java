package com.dcaiti.traceloader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.dcaiti.stargazer.transformation.Transformator;
import com.dcaiti.utilities.KMLWriter;
import com.dcaiti.utilities.Point;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class TraceTools {

    
//  String[] tileIds = {"18/137609/90370",  "18/137610/90370", "18/137609/90371",  "18/137610/90371"};
//  String[] tileIds = {"18/137610/90367",  "18/137610/90368", "18/137610/90369"};
String[] tileIds = {"18/137619/90350",  "18/137620/90350", "18/137619/90351",  "18/137620/90351"};

    public static void main(String[] args) {
        
        
//        TraceTools.visualizeTracePts("19/275568/180542"); //NO POINTS WITHIN!?
//        TraceTools.visualizeTracePts("19/275478/180550");
//        TraceTools.visualizeTracePts("18/137784/90262");
        TraceTools.visualizeTraces(53700);
    }

    
    public static void visualizeTraces(String tileId){
        
        String[] tileIds = {tileId};

        TraceTools.visualizeTraces(tileIds);
     }

    public static void visualizeTraces(String[] tileIds){
        
        TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2();
        loader.loadDaimlerTracesByTileIds(tileIds); 
        
        TraceTools.visualizeTraces(loader.traces);
      }

    
    public static void visualizeTraces(int itefId){
        
        TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2();
        loader.loadDaimlerTracesByItefId(itefId);
        
        TraceTools.visualizeTraces(loader.traces);

      }

    public static void visualizeTraces(List<Trace> traces){
    
        List<List<Coordinate>> allCoors = new ArrayList<List<Coordinate>>();
        for (Trace oneTrace : traces) {
            allCoors.add(oneTrace.getCoordinates());
        }
        
        KMLWriter.writePolyLines(allCoors, "results/allTraces.kml",  KMLWriter.GREEN);
    }

    public static void visualizeTraces(List<Trace> traces, String path, String col){

        List<List<Coordinate>> allCoors = new ArrayList<List<Coordinate>>();
        for(Trace trace: traces){
            allCoors.add(trace.getCoordinates());
        }
        KMLWriter.writePolyLines(allCoors, path, col);

    }
    
    public static void visualizeTrace(Trace trace){
    	List<Trace> traces = new ArrayList<Trace>();
    	traces.add(trace);
    	visualizeTraces(traces);
    }
    
    public static void visualizeTrace(Trace trace, String path, String col){
    	List<Trace> traces = new ArrayList<Trace>();
    	traces.add(trace);
    	visualizeTraces(traces,path,col);
    }
    
    public static void visualizeTracePts(String tileId){
        
        String[] tileIds = {tileId};

        TraceTools.visualizeTracePts(tileIds);
     }

    public static void visualizeTracePts(String[] tileIds){
        
        TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2();
        loader.loadDaimlerTracesByTileIds(tileIds); 
        
        TraceTools.visualizeTracePts(loader.traces);
      }

    
    public static void visualizeTracePts(int itefId){
        
        TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2();
        loader.loadDaimlerTracesByItefId(itefId);
        
        TraceTools.visualizeTracePts(loader.traces);

      }

    public static void visualizeTracePts(List<Trace> traces){
    
        List<TracePoint> pts = new ArrayList<TracePoint>();
        for(Trace trace: traces){
            pts.addAll(trace.tracePts);
        }
        KMLWriter.writePlacemarks(pts, "results/allTracePts.kml");
    }



    public static void visualizeTracePts(List<Trace> traces, String path){

        List<TracePoint> pts = new ArrayList<TracePoint>();
        for(Trace trace: traces){
            pts.addAll(trace.tracePts);
        }
        KMLWriter.writePlacemarks(pts, path);

    }
    
    public static void visualizeTracePts(Trace trace, String path){
    	List<Trace> traces = new ArrayList<Trace>();
    	traces.add(trace);
    	visualizeTracePts(traces,path);
    }
    
    public static void visualizeTilePolygon(String tileId, String path){

        Tile tile = new Tile(tileId);
        List<Polygon> polys = new ArrayList<>();
        polys.add(tile.getPolygon());
        KMLWriter.writeJTSPolygons(polys, path, KMLWriter.TRANS_GREEN);

    }

    public static void visualizeTilePolygons(String[] tileIds, String path){

        List<Polygon> polys = new ArrayList<>();

        for (String id : tileIds){
            Tile tile = new Tile(id);
            polys.add(tile.getPolygon());
        }
        KMLWriter.writeJTSPolygons(polys, path, KMLWriter.TRANS_GREEN);

    }

    public static void visualizeTilePolygons(List<Tile> tiles, String path){

        List<Polygon> polys = new ArrayList<>();

        for (Tile tile : tiles){
            polys.add(tile.getPolygon());
        }
        KMLWriter.writeJTSPolygons(polys, path, KMLWriter.TRANS_GREEN);

    }

    
    public static void writeVehIdsToFile(List<Trace> traces) {
        
        File dir = new File("results/");
        dir.mkdirs();

        FileWriter writer;
        try {
            writer = new FileWriter("results/vehIds.csv");

            for (Trace trace : traces){
               writer.write(trace.getVehicleId() +"\n");
               writer.flush();           
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
         * sort the traces according their initial heading in noOfIntervals.
         * 
         * TODO make it better use the initialheading of first trace to specify the bounds ... 
         *
         * @param traces to be sorted
         * @param noOfIntervals
         * @return An ArrayList which contains noOfIntervals arrayLists, each containing only traces with similar headings
         */
        public static List<List<Trace>> getByDirectionSeparatedTraces(List<Trace> traces, int noOfIntervals) {
            if (traces.size() < 1 || traces.equals(null)) {
                System.out.println("No traces available");
                return null;
            }
    
            List<List<Trace>> rst = new ArrayList<List<Trace>>();
    
            double delta = 2 * Math.PI / noOfIntervals;
    
            for (int i = 0; i < noOfIntervals; i++) {
                ArrayList<Trace> oneDirTraces = new ArrayList<Trace>();
    
                double bound0 = i * delta;
                double bound1 = (i + 1) * delta;
                System.out.println("between " +bound0 +" and "+bound1);
    
                // loop over all traces and add them only to the resulting ArrayList
                // of traces, if their respective heading doesn't vary too much
                for (Trace currTrace : traces) {
                    double startHeading = 0.0;
                    int no = Math.min(currTrace.tracePts.size(), 3);
                    for (int j = 0; j<no; j++) startHeading += currTrace.tracePts.get(j).getHeading();
                    startHeading = startHeading/(double)no;
                    if (bound0 <= startHeading && startHeading < bound1) {
                        // add current trace
                        oneDirTraces.add(currTrace);
                    }
                }
    //            if (oneDirTraces.size() > 0) 
                //add every list even if empty!
                    rst.add(oneDirTraces);
    
            }
    
            return rst;
        }


        public static List<Trace> getTracesStartingAt(List<Trace> traces, Point lngLat, double radius) {
          
            CoordinateReferenceSystem crsUTM = Transformator.getCrsUTMForLngLat(lngLat.lng(), lngLat.lat());
            double[] utm = Transformator.get2DPointTransformation(Transformator.getWgs84_lat_long(), crsUTM, lngLat.lng(), lngLat.lat());
            Coordinate coor = new Coordinate(utm[0], utm[1]);

            List<Trace> foundTraces = new ArrayList<Trace>();

            for (Trace trace : traces){
                if (coor.distance(trace.tracePts.get(0).getCoor()) < radius){
                    foundTraces.add(trace);
                }  
            }

           return foundTraces; 
        }

        
        public static List<Trace> getTracesEndsAt(List<Trace> traces, Point lngLat, double radius) {
            
            CoordinateReferenceSystem crsUTM = Transformator.getCrsUTMForLngLat(lngLat.lng(), lngLat.lat());
            double[] utm = Transformator.get2DPointTransformation(Transformator.getWgs84_lat_long(), crsUTM, lngLat.lng(), lngLat.lat());
            Coordinate coor = new Coordinate(utm[0], utm[1]);

            List<Trace> foundTraces = new ArrayList<Trace>();

            for (Trace trace : traces){
                if (coor.distance(trace.tracePts.get(trace.tracePts.size()-1).getCoor()) < radius){
                    foundTraces.add(trace);
                }  
            }

           return foundTraces; 
        }
        
        public static List<Trace> getTracesStartingAtAndEndsAt(List<Trace> traces, Point start, Point end, double radius) {
        
            List<Trace> foundTraces = getTracesStartingAt(traces, start, radius);
            
            foundTraces = getTracesEndsAt(foundTraces, end, radius);
            
            return foundTraces;
        }


    /**
      * split the traces according to noOfTraces in mergeTraces and otherTraces (e.g. for smoothing 
      * afterwards)
      * @param traces
      * @param noOfTraces in mergeTraces
      * @return
      */
     public static List<List<Trace>> getTraces4Merger(List<Trace> traces, int noOfTraces) {
         
         List<List<Trace>> allTraces = new ArrayList<List<Trace>>();
         List<Trace> mergeTraces = new ArrayList<Trace>();
         List<Trace> otherTraces = new ArrayList<Trace>();
    
         int nof = Math.min(noOfTraces, traces.size());
         for(int i = 0; i < nof; i++) {
             Trace trace = traces.get(i);
             mergeTraces.add(trace);
         }
         allTraces.add(mergeTraces);
         
         for(int i = nof; i < traces.size(); i++) {
             Trace trace = traces.get(i);
             otherTraces.add(trace);
         }
         allTraces.add(otherTraces);
    
         return allTraces;
     }


    /**
      * only traces starting with cmt and after 2015-02-01
      * @param traces
      * @return
      */
     public static List<Trace> getTracesOfDataDelivery7(List<Trace> originalTraces) {
         
         List<Trace> traces = new ArrayList<Trace>();
         DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
         Date date;
         long timestamp = 0;
         try {
            date = dateFormat.parse("01/02/2015");
            timestamp = date.getTime();
         } catch (ParseException e) {
            e.printStackTrace();
         }
    
         for(Trace trace : originalTraces) {
             if (trace.tracePts.size() < 3) continue;
             if (trace.getVehicleId().startsWith("cmt") 
                     && trace.tracePts.get(0).getTime() > timestamp) {
    
                 traces.add(trace);         
             }
         }
    
         return traces;
     }


    public static List<Trace> getTracesWithMoreThanPoints(List<Trace> traces, int minNo) {
         
         List<Trace> corTraces = new ArrayList<Trace>();
         
         for (Trace trace : traces) {
             if (trace.tracePts.size() > minNo){
                 corTraces.add(trace);
             }
         }
         
         return corTraces;
     }


    /**
      * generates several permutations on the order of traces
      * should be applied before getTraces4Merger
      * @param correctedTraces
      * @param noOfTraces
      * @return
      */
     public static List<Trace> getPermutations(List<Trace> correctedTraces, int type) {
         
         if (type == 0) return correctedTraces;
    
    
         if (type == 1) {
             ArrayList<Trace> permTraces = new ArrayList<Trace>();
             for (Trace trace : correctedTraces) {
                 permTraces.add(0, trace);
             }
             return permTraces;
         }
    
         if (type == 2) {
             Collections.sort(correctedTraces);
             return correctedTraces;
         }
    
         return correctedTraces;
     }


    /**
          * looks for traces which enter and leave the bounding box
          * cut the boundingBox by 5 meters and then look for intersections with the traces ...
          *
          * @param traces
          * @param bb
          * @return
          */
         public static ArrayList<Trace> getOnlyCompleteTraces(ArrayList<Trace> traces, BoundingBox bb) {
             
             ArrayList<Trace> newTraces = new ArrayList<Trace>();
             
             ArrayList<Coordinate> intersections = new ArrayList<Coordinate>();
             
             int[] cuts = new int[4];
             for(int i = 0; i < 4; i++) 
                 cuts[i] = 10; 
             
             BoundingBox bbox = new BoundingBox(bb, cuts);
             LineString[] lines = bbox.toLines();
    //         for(int i = 0; i < 4; i++){
    //             ArrayList<Coordinate> lcs = new ArrayList<Coordinate>();
    //             for(Coordinate c : lines[i].getCoordinates()) lcs.add(c);
    //             KMLWriter.writeLine(lcs, "line_"+i +".kml", KMLWriter.CYAN);
    //         }
             
             for (Trace trace : traces) {
                 LineString traceLine = trace.getLineString();
                 
                 Geometry g;
                 int ct = 0;
                 for (int i = 0; i < 4; i++) {
                         g = lines[i].intersection(traceLine);
                          if (g.getCoordinates().length == 1) ct++;
    //                      else System.out.println("no intersection for line "+i);
                 }
                 if (ct == 2) newTraces.add(trace);
    //             else System.out.println("not complete "+trace.getVehicleId());
    
                 
             }
             
    //         KMLWriter.writePolygon(bbox.toCoordinates(), "box.kml", KMLWriter.TRANS_BLUE);
    //         KMLWriter.writeCoors(intersections, "inter.kml");
             
             return newTraces;
    
             
             
         }
}
