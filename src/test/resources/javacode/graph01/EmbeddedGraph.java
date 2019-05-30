package graph;

import java.awt.geom.Point2D;

public class EmbeddedGraph extends Graph{
	final Point2D.Double[] points;
	private Point2D.Double minCorner;
	private Point2D.Double maxCorner;
	
	
	public EmbeddedGraph(Point2D.Double[] points){
		super(points.length);
		this.points=points.clone();
		getSize();
		
	}
	
	public Point2D.Double getCoordinate( int i ) throws IllegalArgumentException{
		if(i>=this.n)
			throw new IllegalArgumentException("Punkt existiert nicht");
		return points[i];
		
	}

	private void getSize(){
		double maxX=0;
		double maxY=0;
		double minX=Double.MAX_VALUE;
		double minY=Double.MAX_VALUE;
		for(int i=0; i<points.length; ++i){				//besser Methode moeglich
			if(points[i].getY()>maxY)
				maxY=points[i].getY();
			else if(points[i].getY()<minY)
				minY=points[i].getY();
			if(points[i].getX()>maxX)
				maxX=points[i].getX();
			else if(points[i].getX()<minX)
				minX=points[i].getX();
		}	
		this.maxCorner=new Point2D.Double(maxX,maxY);
		this.minCorner=new Point2D.Double(minX,minY);
	}
	
	
	public Point2D.Double getMinCorner(){
		return minCorner;
	}
	
	public double getHeight(){
		return maxCorner.getY()-minCorner.getY();
	}
	
	public double getWidth(){
		return maxCorner.getX()-minCorner.getX();
	}
	
	public void setMaxCorner(Point2D.Double maxCorner){
		this.maxCorner=maxCorner;
	}
	
	public void setMinCorner(Point2D.Double minCorner){
		this.minCorner=minCorner;
	}
	
}
