package map;

import java.util.ArrayList;
import java.util.Comparator;


import used.*;

public class Point {

	private int indexA;
	private int indexB;
		
	public Point(int a, int b){
		this.indexA=a;
		this.indexB=b;
	}
	
	public int distance(Point x){
		int a = Math.abs(x.indexA-this.indexA);
		int b = Math.abs(x.indexB-this.indexB);
		
		int max = Math.max(a, b);
		Math.abs(a-max);
		
		return max+a;
		
	}
	
	/**
	 * 	
	 * @param distance distance you want to have the the point
	 * @param size the maximum the points are allowed to have as index (often size*2)
	 * @param rotate if you want to rotate the "normal" hexagon
	 * @return an array of 6 points
	 */
	public ArrayList<Point> get6Points(int distance, int size, boolean rotate){
		ArrayList<Point> points = new ArrayList<Point>();
		int a = this.getIdA();
		int b = this.getIdB();
		
		if(rotate&&distance%2==0){
			//System.out.println(distance);
			//if(distance%2!=0){throw new IllegalArgumentException();}
			if(a+distance<=size&&b+distance/2<=size){
				points.add(new Point(a+distance,b+distance/2));
			}
			if(a+distance/2<=size){
				if(b+distance<=size){
					points.add(new Point(a+distance/2,b+distance));
				}
				if(b-distance/2>=0){
					points.add(new Point(a+distance/2,b-distance/2));
				}
			}
			if(a-distance/2>=0){
				if(b+distance/2<=size){
					points.add(new Point(a-distance/2,b+distance/2));
				}
				if(b>=distance){
					points.add(new Point(a-distance/2,b-distance));
				}
			}
			if(a>=distance&&b-distance/2>=0){
				points.add(new Point(a-distance,b-distance/2));
			}
			
		}else{
		if(a+distance<=size){
		points.add(new Point(a+distance,b));
			if(b+distance<=size){
				points.add(new Point(a+distance,b+distance));
				points.add(new Point(a,b+distance));
			}
		}else if(b+distance<=size){
			points.add(new Point(a,b+distance));
		}
			
		if(a>=distance){
		points.add(new Point(a-distance,b));
		
			if(b>=distance){
				points.add(new Point(a,b-distance));
				points.add(new Point(a-distance,b-distance));
			}
		}else if(b>=distance){
			points.add(new Point(a,b-distance));
		}
		}
		return points;
	}
	
	public ArrayList<Point> get6Points(int size){
		ArrayList<Point> points = new ArrayList<Point>();
		int a = this.getIdA();
		int b = this.getIdB();

		if(a+1<=size){
			points.add(new Point(a+1,b));
				if(b+1<=size){
					points.add(new Point(a+1,b+1));
					points.add(new Point(a,b+1));
				}
			}else if(b+1<=size){
				points.add(new Point(a,b+1));
			}
				
			if(a>=1){
			points.add(new Point(a-1,b));
			
				if(b>=1){
					points.add(new Point(a,b-1));
					points.add(new Point(a-1,b-1));
				}
			}else if(b>=1){
				points.add(new Point(a,b-1));
			}

		return points;
	}
	
public ArrayList<Point> get6Points() {
	ArrayList<Point> points = new ArrayList<Point>();
	int a = this.getIdA();
	int b = this.getIdB();

	
	points.add(new Point(a+1,b));
	points.add(new Point(a+1,b+1));
	points.add(new Point(a,b+1));
	points.add(new Point(a-1,b));
	points.add(new Point(a,b-1));
	points.add(new Point(a-1,b-1));
				
	return points;
	}
	
	
	
	public int getIdA(){
		return this.indexA;
	}
	
	public int getIdB(){
		return this.indexB;
	}
	
	public boolean equals(Point p){
		if(this.indexA==p.indexA && this.indexB==p.indexB)
			return true;
		else
			return false;
	}
	
	/**
	 * 
	 * @param other
	 * @return a Point which contains the index. if you add the Index of this Point with the returned Point you
	 * will get the other Point
	 */
	public Point getDifference(Point other){
		return new Point(other.indexA-this.indexA, other.indexB-this.indexB);
	}
	
	public Point add(Point add){
		return new Point(this.indexA+add.indexA,this.indexB+add.indexB);
	}
	
	public Point getLeft(Point mid){
		
		Point dire = mid.getDifference(this);
		return mid.add(Direction.getLeft(dire));
		
	}
	
	public Point getRight(Point mid){
		
		Point dire = mid.getDifference(this);
		return mid.add(Direction.getRight(dire));
		
	}
	
	public String toString(){
		return indexA+","+indexB;
	}

	
}
