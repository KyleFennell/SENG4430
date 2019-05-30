package used;

import map.Point;

public enum Direction {
	upRight(1,1),
	right(0,1),
	downRight(-1,0),
	downLeft(-1,-1),
	left(0,-1),
	upLeft(1,0);
	
	private Point dir;

	private Direction(int a, int b){
		this.dir= new Point(a,b);
	}
	
	public Point get(){
		return this.dir;
	}
	
	public Point getLeft(){
		if(this.equals(upRight)){
			return upLeft.get();
		}else{
			return Direction.values()[this.ordinal()-1].get();
		}
	}
	
	public Point getRight(){
		if(this.equals(upLeft)){
			return upRight.get();
		}else{
			return Direction.values()[this.ordinal()+1].get();
		}
	}

	public static Point getRight(Point a){
		Direction[] d = Direction.values();
		for(Direction temp : d){
			if(temp.get().equals(a)){
				return temp.getRight();
			}
		}
		return null;
	}
	
	public static Point getLeft(Point a){
		Direction[] d = Direction.values();
		for(Direction temp : d){
			if(temp.get().equals(a)){
				return temp.getLeft();
			}
		}
		return null;
	}
	
}