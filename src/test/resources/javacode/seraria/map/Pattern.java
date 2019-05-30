package map;

import java.io.ObjectInputStream.GetField;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Comparator;

import used.PrioritySetQueue;
import map.Field.Typ;

public class Pattern implements Iterable<Field>{
	private Field[][] map;
	private HashSet<Field> edges;
	private int size;
	private int tileCount;
	 	
	//max{i,j}<=size sein!
	
	
	/**
	 * 
	 * @param getsize
	 * @param seed set 0 if you dont have any. 
	 * The Pattern will be the same, if the seeds are equal and the size not too different 
	 * (not more than 3^n to 3^n*2 or to 3^(n-1)*2)
	 */
	public Pattern(int getsize,long seed){
		
		this.size=getsize-1;
		//"indexverschiebung"
		
		//count the needed tiles
		int sum = 0;
		for(int i=getsize;i<2*getsize-1;++i){
			sum += 2*i;
		}
		this.tileCount = sum + 2*getsize-1;
						
		this.edges= new HashSet<Field>();
		
		map= new Field[size*2+1][size*2+1];
			
	}
	
	void setHeight(Field[][] biggerMap){
		//the down-side (could done with the iterator)
				for(int i=0; i<=size; ++i){
					for(int j=0; j<=size+i; ++j){
						map[i][j]= biggerMap[i][j];
					}
					edges.add(map[i][0]);
					edges.add(map[i][size+i]);
				}
				
				for(int j=0; j<=size; ++j){
					edges.add(map[0][j]);
				}
				
				//the upper-side
				for(int i=size+1; i<=size*2; ++i){
					for(int j=size*2; j>=i-size; --j){
						map[i][j]= biggerMap[i][j];
					}
					edges.add(map[i][size*2]);
					edges.add(map[i][i-size]);
				}
				
				for(int j=size*2; j>=size; --j){
					edges.add(map[size*2][j]);
				}
	}
	
	void setField(Field field, int a, int b){
		map[a][b]= field;
	}
	
	
//	private Field translate(int a, int b, int c){
//		return map[a+b][c+b];
//	}
	
	Field translate(int a, int b, int c){
		return map[a+b+size][c+b+size];
	}
	
public Field getField(Point p){
		return getField(p.getIdA(),p.getIdB());
	}
	
	public Field getField(int a, int b){
		if(a>size*2 || b>size*2){
			return null;
		}else{
		return map[a][b];
		}
	}
	
	/**
	 * not more than a Pattern of the size 70 is able to show.
	 */
	public String toStringReverse(){
		StringBuffer buff = new StringBuffer();
		for(Field[] arr : map){
			for(Field fie : arr){
				if(fie==null){
					buff.append("[   ] ");
					//buff.append("    ");
					
				}else{
				buff.append(fie.toString());
				//buff.append(':');
				//buff.append(fie.getPoint().toString());
				buff.append(" ");
				}
			}
			buff.append("\n");
		}
		
		return buff.toString();
	}
	
	public String toString(){
		StringBuffer buff = new StringBuffer();
		for(int i=map.length-1; i>=0; --i){
			for(int j =0; j<map[i].length; ++j){
				if(map[i][j]==null){
					buff.append("[   ] ");
//					buff.append("    ");
					
				}else{
				buff.append(map[i][j].toString());
//				buff.append(':');
//				buff.append(map[i][j].getPoint().toString());
				buff.append(" ");
				}
			}
			buff.append("\n");
		}
		
		return buff.toString();
	}
	
	public String toStringIterator(){
		StringBuffer buff = new StringBuffer();
		
		for(Field temp : this){
			buff.append("[");
			buff.append(temp.getHeight());
			buff.append("]");
		}
		
		return buff.toString();
	}
	
	public int getSize(){
		return size;
	}
	
	public int getWidth(){
		return size*2;
	}
	
	int getTileCount(){
		return this.tileCount;
	}
	
	ArrayList<Field> getNeighboursRisk(Field f){
		return getNeighboursRisk(f.getPoint());
	}
	
	ArrayList<Field> getNeighboursRisk(Point p){
		ArrayList<Field> fields = new ArrayList<Field>();
		ArrayList<Point> points = p.get6Points();
		
		for(Point q : points){
				fields.add(getField(q));
		}
		
		return fields;
	}
	

	public ArrayList<Field> getNeighbours(Field f){
		return getNeighbours(f.getPoint());
	}
	
	public ArrayList<Field> getNeighbours(Point p){
		ArrayList<Field> fields = new ArrayList<Field>();
		ArrayList<Point> points = p.get6Points(size*2);
		
		for(Point q : points){
				fields.add(getField(q));
		}
		
		return fields;
		
	}
	
	/**
	 * 
	 * @param now
	 * @param back
	 * @return Field[0] = the direction you look, [1] your right side, [2] the left
	 */
	public ArrayList<Field> getFore(Field now, Field back){
		ArrayList<Field> fields = new ArrayList<Field>();
		
		ArrayList<Field> nNow = getNeighbours(now);
		ArrayList<Field> nBack = getNeighbours(back);
		
		for(Field temp : nNow){
			if(!nBack.contains(temp) && temp!=back){
				fields.add(temp);
			}
		}
		
		return fields;
	}
	
	public Field getMinFore(Field now, Field back){
		
		Field lowest= null;
		
		for(Field f : getFore(now, back)){
			if(lowest == null|| f.getHeight()<lowest.getHeight()){
				lowest=f;
			}
		}
		
		return lowest;
	}
	
	//TODO: can be written without translate
	public Field[] getPyramide(Field now, Field back, int length){
		return new Field[0];
	}

	public HashSet<Field> getEdges(){
		return edges;
	}
	
		
	public boolean isEdge(Field field){
		return edges.contains(field);
	}
	
//	public static void main(String[] args){
//		Pattern world = new Pattern(20,0); //nicht mehr als 1000! 
//		System.out.println(world);
//		//System.out.println(world.isEdge(world.getField(new Point(10,1))));
//		//System.out.println(world.isEdge(world.getField(new Point(2,11))));
////		HashSet<Field> set = world.getEdges();
////		for(Field f : set){
////			System.out.println(f.getPoint());
////		}
//		//System.out.println(world.getEdges().toArray());
//		//System.out.println(world.toString());
////		System.out.println(world.toStringIterator());
//	
//	}

	public Iterator<Field> iterator(){
		return new PatternIterator();
	}
	
	private class PatternIterator implements Iterator<Field>{
	
		private Field iter;
	
	public PatternIterator(){
		iter=null;
	}
	
	public void reset(){
		iter=null;
	}
	
	public Field current(){
		return this.iter;
	}
	
	@Override
	public boolean hasNext() {
		if(iter==null){
			return true;
		}else{
		return !iter.getPoint().equals(new Point(0,size));
		}
	}

	@Override
	public Field next() {
				
		if(iter==null){
			return iter = getField(size*2,size);
		}
		
		int a = iter.getPoint().getIdA();
		int b = iter.getPoint().getIdB();
		
		//untere Häfte
		if(a<=size){
			if(b<size*2-Math.abs(a-size)){
				return iter = getField(a,b+1);
			}else{
				return iter = getField(a-1,0);
			}
			//obere Häfte
		}else{
			if(b<size*2){
				return iter = getField(a,b+1);
			}else{
				return iter = getField(a-1,a-size-1);
			}
		}
		 
	}
	
	@Override
	public void remove() {
		// TODO Auto-generated method stub
		}
	}
	
}
