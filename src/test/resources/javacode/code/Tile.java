package com.dcaiti.traceloader;

import java.util.ArrayList;
import java.util.List;


import com.dcaiti.stargazer.transformation.Transformator;
import com.dcaiti.utilities.Point;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import org.osgeo.proj4j.CoordinateReferenceSystem;


public class Tile {

    public String tileId = "";
    
    public int zoom;
    public int xTileId;
    public int yTileId;

    private List<Trace> traces = null;
    
    //needed for databasketfinder
    public List<Trace> reducedTraces = new ArrayList<Trace>();
    
    private Polygon poly = null;
    
    private CoordinateReferenceSystem crsUTM = Transformator.getCrsUTMForLngLat(9.35,48.671);

    
    public Tile(String tileId){
        this.tileId = tileId;
        String[] split = tileId.split("/");
        this.zoom = Integer.parseInt(split[0]);
        this.xTileId = Integer.parseInt(split[1]);
        this.yTileId = Integer.parseInt(split[2]);
        
//        Point lngLat = getLongLat();
//        crsUTM = Transformator.getCrsUTMForLngLat(lngLat.lng(), lngLat.lat());

    }
    
    public Tile(String tileId, List<Trace> traces){
        this.tileId = tileId;
        String[] split = tileId.split("/");
        this.zoom = Integer.parseInt(split[0]);
        this.xTileId = Integer.parseInt(split[1]);
        this.yTileId = Integer.parseInt(split[2]);
        this.traces = traces;

//        Point lngLat = getLongLat();
//        crsUTM = Transformator.getCrsUTMForLngLat(lngLat.lng(), lngLat.lat());
    }

    public Tile(int zoomFactor, int xTileId, int yTileId){
        this.xTileId = xTileId;
        this.yTileId = yTileId;
        this.zoom = zoomFactor;
        this.tileId = zoom+"/"+xTileId+"/"+yTileId;
        
//        Point lngLat = getLongLat();
//        crsUTM = Transformator.getCrsUTMForLngLat(lngLat.lng(), lngLat.lat());

    }

    private void loadTraces() {

        String[] tileIds = new String[1];
        tileIds[0] = this.tileId;
        TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2(false, false);
        loader.loadDaimlerTracesByTileIds(tileIds, 0);

        this.traces = loader.traces;

    }

    private void loadTraces(String tableName) {

        String[] tileIds = new String[1];
        tileIds[0] = this.tileId;
        TraceLoaderAccumulo2 loader = new TraceLoaderAccumulo2(false, false);
        loader.loadDaimlerTracesByTileIds(tileIds, tableName, 0);

        this.traces = loader.traces;

    }

    /**
     * TODO !!!taken from HotSpotIdentifier !!! merge in one data class in module ???
     * Calculates point of the upper left corner of a tile 
     * 
     * @return point of the upper left corner
     */
    public Point getLongLat() {
        double xtile = (double)xTileId;
        double ytile = (double)yTileId;
        double n = Math.pow(2, zoom);

        double longDeg = xtile / n * 360.0 - 180;
        double latRad = Math.atan(Math.sinh(Math.PI * (1 - 2 * ytile / n)));
        double latDeg = latRad * 180d / Math.PI;

        return new Point(longDeg, latDeg);
    }

    /**
     * Some how this function is broken unter the condition
     * of providing for several tiles the appropiate point
     * coordinates
     * @return
     */
    public ArrayList<Point> getPolygonOLD() {

        ArrayList<Point> points = new ArrayList<Point>(5);

        //upper left corner of tile
        Point ul = getLongLat();
        points.add(ul);
        
        //bottom right corner
        int xtile = this.xTileId + 1;
        int ytile = this.yTileId + 1;
        Tile otherTile = new Tile(zoom + "/" + xtile + "/" + ytile);
        Point br = otherTile.getLongLat();

        points.add(new Point(ul.lng(), br.lat()));
        
        points.add(br);
        
        points.add(new Point(br.lng(), ul.lat()));
        
        points.add(ul);

        return points;

    }
    
    public Polygon getPolygon() {

        if (poly != null) return poly; //already computed
        
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] pts = new Coordinate[5];


        //upper left corner of tile
        Point ul = getLongLat();
//        System.out.println(ul.lng() +", " +ul.lat());
        
        double[] utm = Transformator.get2DPointTransformation(Transformator.getWgs84_lat_long(), crsUTM, ul.lng(), ul.lat());
        pts[0] = new Coordinate(utm[0], utm[1]);

        //bottom right corner
        int xtile = this.xTileId + 1;
        int ytile = this.yTileId + 1;
        Tile otherTile = new Tile(zoom, xtile, ytile);
        Point br = otherTile.getLongLat();

        Point p1 = new Point(ul.lng(), br.lat());
        utm = Transformator.get2DPointTransformation(Transformator.getWgs84_lat_long(), crsUTM, p1.lng(), p1.lat());
        pts[1] = new Coordinate(utm[0], utm[1]);
        
        utm = Transformator.get2DPointTransformation(Transformator.getWgs84_lat_long(), crsUTM, br.lng(), br.lat());
        pts[2] = new Coordinate(utm[0], utm[1]);        

        Point p2 = new Point(br.lng(), ul.lat());
        utm = Transformator.get2DPointTransformation(Transformator.getWgs84_lat_long(), crsUTM, p2.lng(), p2.lat());
        pts[3] = new Coordinate(utm[0], utm[1]);
        
        pts[4] = new Coordinate(pts[0]);
        
        poly = factory.createPolygon(pts);

        return poly;

    }



    public String toString() {
        String res = tileId + " with " + traces.size() + " original traces\n";
        res += " Reduced traces " + reducedTraces.size() +"\n";

        return res;

    }
    
    public String getTileId() {
        return tileId;
    }

    public List<Trace> getTraces() {
        if (this.traces == null) {
            loadTraces();
        }
        return traces;
    }

    /**
     * 
     * @param str
     * @return
     */
    public List<Trace> getTracesWith(String tableName, String str) {
        List<Trace> res = new ArrayList<>();
        if (this.traces == null) {            
            loadTraces(tableName);
        } 
        if(!str.isEmpty()) {
            for (Trace trace : traces){
                if (trace.getVehicleId().startsWith(str)){
                    res.add(trace);
                }
            }
            this.traces = res;
        }
//        System.out.println("-----------------> " +this.traces.size());
        return this.traces;
    }

    public boolean isNeighbor(Tile anotherTile) {
        
        if (this.zoom != anotherTile.zoom) return false;
        if (this.xTileId == anotherTile.xTileId) {
            if ((this.yTileId == anotherTile.yTileId-1) || (this.yTileId == anotherTile.yTileId+1))
                return true;
        }
        if (this.yTileId == anotherTile.yTileId) {
            if ((this.xTileId == anotherTile.xTileId-1) || (this.xTileId == anotherTile.xTileId+1))
                return true;
        }
        return false;
    }
    
    public boolean hasNeighbors(List<Tile> tiles) {
        for (Tile tile : tiles) {
            if (this.isNeighbor(tile)) return true;
        }
        
        return false;
    }


    public static String getTileId(final double lng, final double lat, int zoom) {          
        
        int xtile = (int)Math.floor( (lng + 180) / 360 * (1<<zoom) ) ;
        int ytile = (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom) ) ;
        if (xtile < 0)
                xtile=0;
        if (xtile >= (1<<zoom))
                xtile=((1<<zoom)-1);
        if (ytile < 0)
                ytile=0;
        if (ytile >= (1<<zoom))
                ytile=((1<<zoom)-1);
        return("" + zoom + "/" + xtile + "/" + ytile);
    }
    
    public static int[] getMinMax(String originalTileId, int newZoom) {
        
        //minX, maxX, minY, maxY
        int[] minMax = new int[4];
        
        Tile tile = new Tile(originalTileId);
        Point p = tile.getLongLat();
        int zoom = tile.zoom;

        String initialId = Tile.getTileId(p.lng(), p.lat(), newZoom);
//        System.out.println(initialId);
        Tile initialTile = new Tile(initialId);
        minMax[0] = initialTile.xTileId;
        minMax[2] = initialTile.yTileId;

        int tilesPerDir = (int) Math.pow(2, newZoom - zoom);
        minMax[1] = minMax[0] + tilesPerDir;
        minMax[3] = minMax[2] + tilesPerDir;
        
        return minMax;

    }

    public static List<String> getTileIds(String originalTileId, int newZoom) {
        
        List<String> tileIds = new ArrayList<String>();
        
        Tile tile = new Tile(originalTileId);
        Point p = tile.getLongLat();
        int zoom = tile.zoom;

        String initialId = Tile.getTileId(p.lng(), p.lat(), newZoom);
//        System.out.println(initialId);
        Tile initialTile = new Tile(initialId);
        int tilesPerDir = (int) Math.pow(2, newZoom - zoom);

        for (int i = 0; i < tilesPerDir; i++){
          for(int j = 0; j < tilesPerDir; j++) {
              int x = initialTile.xTileId + i;
              int y = initialTile.yTileId + j;
              String id = newZoom + "/" +x +"/" +y;
//              System.out.println(id);
              tileIds.add(id);
          }
      }
        
        return tileIds;

    }
    


}
