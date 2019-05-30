package map;

import java.util.ArrayList;
import java.util.Random;

public class Height {
	
	private static final double FAC1 = 3; //Multiplikation vor der Rechnung
	private static final double FAC2 = 3; //Potenz (muss ungerade sein!)
	private static final double FAC3 = 5; //Multiplikation nach der Rechnung

	private Pattern p;
	private int size;
	
	private Field[][] biggerMap; //only != 0 as long as the constructor works
	private int biggerSize;
	private Random rand;
	
	public Height(Pattern p, Random rand){
		this.p=p;
		this.size=p.getSize();
		this.rand=rand;
	}

		void buildHeight(){
		
		//method to measure which biggerField should be used
		int n =0;
		biggerSize=1;
		boolean multiplier = false;
		while(size>=biggerSize){
			if(!multiplier){
				multiplier = true;
				biggerSize *= 2;
			}else{
				multiplier=false;
				biggerSize *= 3;
				biggerSize /= 2;
				++n;
			}
		}
				
		//creating the biggerField
		biggerMap = new Field[biggerSize*2+1][biggerSize*2+1];
		
		//the down-side (could not done with the iterator(cause the iterator use only field, not biggerField))
		for(int i=0; i<=biggerSize; ++i)
			for(int j=0; j<=biggerSize+i; ++j)
				biggerMap[i][j]= new Field(i,j);
		
		//the upper-side
		for(int i=biggerSize+1; i<=biggerSize*2; ++i)
			for(int j=biggerSize*2; j>=i-biggerSize; --j)
				biggerMap[i][j]= new Field(i,j);
		
		// starting with the 6 edges and creating a Random-object
		//Seed will be saved!
		
		Field field1;
		Field field2;
		Field field3;
		Field field4;
		Field field5;
		Field field6;
		
		
		field1 = trans(biggerSize,0,0);											
		field2 = trans(0,biggerSize,0);			
		field3 = trans(0,0,biggerSize);			
		field4 = trans(-biggerSize,0,0);						
		field5 = trans(0,-biggerSize,0);								
		field6 = trans(0,0,-biggerSize);						
		
		
		
		//creating the corners			
		field1.setHeight(rand.nextInt(101));											
		field2.setHeight(rand.nextInt(101));			
		field3.setHeight(rand.nextInt(101));			
		field4.setHeight(rand.nextInt(101));						
		field5.setHeight(rand.nextInt(101));								
		field6.setHeight(rand.nextInt(101));						
		
		setMid(trans(0,0,0),biggerSize, false);
		
		
		//initialisise all variables
		boolean rotate = false;
		ArrayList<Point> storage1 = new ArrayList<Point>();
		storage1.add(trans(0,0,0).getPoint());
		ArrayList<Point> storage2 = new ArrayList<Point>();
		ArrayList<Point> temp;
		int distance = biggerSize;
		//boolean multiplier und int n vorhanden!
		
		//the main-algo
		while(distance>0){
			if(rotate){
				rotate=false;
			}else{
				rotate=true;
				--n;
				if(n==-1 && multiplier){
					multiplier=false;
					n=0;
				}
			}
			if(multiplier){
			distance-=Math.pow(3, n)*2;	
			}else{
			distance-=Math.pow(3, n);
			}
			
			if(rotate){
				//nutze storage1 und packe alle neuen Felder in storage2(und sette ihre Höhen)
				for(Point point : storage1){
					temp = point.get6Points(distance, biggerSize*2, rotate);
					for(Point p : temp){
						if(getBiggerField(p)==null){
							
						}else if(!getBiggerField(p).hasHeight()){
						storage2.add(p);
						setMid(getBiggerField(p), distance, rotate);
						}
					}
				}
				storage1.clear();
			}else{
				for(Point point : storage2){
					temp = point.get6Points(distance, biggerSize*2, rotate);
					for(Point p : temp){
						if(getBiggerField(p)==null){
							
						}else
						if(!getBiggerField(p).hasHeight()){
						storage1.add(p);
						setMid(getBiggerField(p), distance, rotate);
						}
					}
				}
				storage2.clear();
			}			
		}
	}
	
	private void setMid(Field field, int distance, boolean rotate){
		
		ArrayList<Integer> array = new ArrayList<Integer>();
		array.add(distance);
		ArrayList<Point> points = field.getPoint().get6Points( distance, biggerSize*2, rotate );
		for(Point point : points){
			if(getBiggerField(point)==null){
				
			}else if(getBiggerField(point).hasHeight()){
			array.add(getBiggerField(point).getHeight());
			}
		}
		
		field.setHeight(random(array));	
		
	}
	
	
	/**
	 * 
	 * @param numbers number[0] have to be the distance between the nodes, the others are their heights
	 * @return a random number between 0 and 100
	 */
	private int random(ArrayList<Integer> numbers){
		double heightsum=0;
		
		for(int i=1; i<numbers.size(); ++i){
			heightsum+=numbers.get(i);
		}
		
		heightsum/=numbers.size()-1;
		
		int diff = (int)(Math.pow((FAC1*rand.nextDouble())-(FAC2/2),FAC2)*FAC3*numbers.get(0)+heightsum);
		
		if(diff>100){
			return 100;
		}else if(diff<0){
			return 0;
		}else{
			return diff;
		}
		
		
	}
	
	
//	used for the build-method
	private Field getBiggerField(Point point){
		if(point.getIdA()>biggerSize*2 || point.getIdB()>biggerSize*2){
			return null;
		}else{
		return biggerMap[point.getIdA()][point.getIdB()];
		}
	}
	
	//is only used in the build-algo. not necessary!
		Field trans(int a, int b, int c){
			return biggerMap[a+b+biggerSize][c+b+biggerSize];
		}
	
		Field[][] getBiggerMap(){
			
			if(biggerMap==null){
				buildHeight();
			}
			
			return this.biggerMap;
		}
		
	
		public void reset(){
			for(Field temp : p){
				temp.reset();
			}
			
			biggerMap=null;
			
		}
		
}
