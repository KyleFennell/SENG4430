package com.dcaiti.traceloader;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class BoundingBox {
    
    public Coordinate bottomLeft;
    public Coordinate upperRight;
    
    public BoundingBox() {
        double x = Double.MAX_VALUE;
        double y = Double.MAX_VALUE;
        bottomLeft = new Coordinate(x,y);
        x = Double.MIN_VALUE;
        y = Double.MIN_VALUE;
        upperRight = new Coordinate(x,y);
    }
    
    
    public BoundingBox(Coordinate bottomLeft, Coordinate upperRight) {
        this.bottomLeft = bottomLeft;
        this.upperRight = upperRight;
    }
    
    

    
    /**
     * Determine a better bounding box for the approach.
     * Because at the beginning and end of the original bounding box too few tracePoints are available.
     * @param bb original bounding box
     * @param traces all traces
     * @return new bounding box
     */
    public BoundingBox(BoundingBox bb, List<Trace> traces) {
        
        LineString[] line = bb.toLines();

        int[] ct = new int[4];
        int[] cuts = new int[4];
        for (int i = 0; i < 4; i++) {
                ct[i] = 0;
                cuts[i] = 5;
        }
        
        int cut = 100;

        for (Trace trace : traces) {
                if (trace.tracePts.size() < 2) continue;

//              System.out.println(trace.tracePts.size());
                Coordinate[] pts = new Coordinate[4];
                pts[1] = new Coordinate(trace.tracePts.get(0).getCoor()); 
                double angle = trace.tracePts.get(0).getHeading();
                pts[0] = new Coordinate(pts[1].x + (-100.0 * Math.cos(angle)), pts[1].y + (-100.0 * Math.sin(angle)));
                pts[2] = new Coordinate(trace.tracePts.get(trace.tracePts.size()-1).getCoor()); 
                angle = trace.tracePts.get(trace.tracePts.size()-1).getHeading();
                pts[3] = new Coordinate(pts[2].x + (100.0 * Math.cos(angle)), pts[2].y + (100.0 * Math.sin(angle)));

                LineString traceLine = new GeometryFactory().createLineString(pts);                     
                Geometry g;
                for (int i = 0; i < 4; i++) {
                        g = line[i].intersection(traceLine);
                        for (Coordinate coor: g.getCoordinates()) {
                                ct[i]++;
//                              System.out.println("trace " +ct +" intersection in "+i);
                        }
                }
        }
        
        
        for (int i = 0; i < 2; i++) {
                int max = 0;
                int jj= 0;
                for (int j = 0; j < 4; j++) {
                        if (ct[j] > max) {
                                max = ct[j];
                                jj = j;
                        }
                }
                cuts[jj] = cut;
                ct[jj] = 0;
        }

        System.out.print("cuts: ");
        for (int i = 0; i < 4; i++) {
                System.out.print(cuts[i] +", ");
        }
        System.out.println();

        int cutBottom = cuts[0];
        int cutRight = cuts[1];
        int cutTop = cuts[2];
        int cutLeft = cuts[3];
        //            
        bottomLeft = new Coordinate(bb.bottomLeft.x + cutLeft, bb.bottomLeft.y + cutBottom);
        upperRight = new Coordinate(bb.upperRight.x - cutRight, bb.upperRight.y - cutTop);
        
}

    public BoundingBox(BoundingBox bb, int[] cuts) {
        
        int cutLeft = cuts[0];
        int cutTop = cuts[1];
        int cutRight = cuts[2];
        int cutBottom= cuts[3];
        bottomLeft = new Coordinate(bb.bottomLeft.x + cutLeft, bb.bottomLeft.y + cutBottom);
        upperRight = new Coordinate(bb.upperRight.x - cutRight, bb.upperRight.y - cutTop);
        
        
    }

    
    public void mergeBoundingBox(BoundingBox bb) {
        
        if (bb.bottomLeft.x < this.bottomLeft.x)
            this.bottomLeft.x = bb.bottomLeft.x;
        if (bb.bottomLeft.y < this.bottomLeft.y)
            this.bottomLeft.y = bb.bottomLeft.y;
        if (bb.upperRight.x > this.upperRight.x)
            this.upperRight.x = bb.upperRight.x;
        if (bb.upperRight.y > this.upperRight.y)
            this.upperRight.y = bb.upperRight.y;
    
    }
    
    public void expandBoundingBox(Coordinate co) {
        
        if (co.x < this.bottomLeft.x)
            this.bottomLeft.x = co.x;
        if (co.y < this.bottomLeft.y)
            this.bottomLeft.y = co.y;
        if (co.x > this.upperRight.x)
            this.upperRight.x = co.x;
        if (co.y > this.upperRight.y)
            this.upperRight.y = co.y;
    
    }

    public LineString[] toLines() {
        
        LineString[] lines = new LineString[4];
        
        ArrayList<Coordinate> bbCoors = this.toCoordinates();
        for (int i = 0; i < 4; i++) {
            Coordinate[] coors = new Coordinate[2];
            coors[0] = new Coordinate(bbCoors.get(i));
            coors[1] = new Coordinate(bbCoors.get(i+1));
            lines[i] = new GeometryFactory().createLineString(coors);
        }
            
       return lines; 
        
    }
    
    /**
     * closed polygon
     * @return
     */
    public ArrayList<Coordinate> toCoordinates() {
      
        ArrayList<Coordinate> bb = new ArrayList<Coordinate>();
        bb.add(new Coordinate(bottomLeft));
        bb.add(new Coordinate(upperRight.x, bottomLeft.y));
        bb.add(new Coordinate(upperRight));
        bb.add(new Coordinate(bottomLeft.x, upperRight.y));
        bb.add(new Coordinate(bottomLeft));
        
        return bb;

    }
    
    public String toString() {
        
        return bottomLeft + " ... " + upperRight;
    }


}
