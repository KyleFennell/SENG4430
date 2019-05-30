package map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

import used.PrioritySetQueue;
import map.Field.Typ;


public class Terrain {

	private static final int Mountain_HEIGHT = 80;
	private static final int Min_MOUNTAINS = 5;
	private static final int averageWater= 15; //The count of Watertiles a River has in average
	private static final int coverWater = 2; //How much of the World shall be under Water (in 1/x)
	private static final int River_Decrease = 5;
	
	private final int maxWater;
	private int riverReader;
	
	
	private Pattern p;
	private Random rand;
	private int tileCount;
	private PrioritySetQueue<Field> queue; 
	
	private ArrayList<ArrayList<Field>> mountains;
	private ArrayList<ArrayList<Field>> edgesMountains;
	private Stack<Field> springs;
	private LinkedList<Field> possibleSprings;
	private ArrayList<ArrayList<Field>> rivers;
	private ArrayList<ArrayList<Field>> edgesRivers;
	private ArrayList<Field> ocean;
	
	public Terrain(Pattern p, Random rand, int tileCount){
		
		queue = new PrioritySetQueue<Field>(10,new Comparator<Field>(){
			
			public int compare(Field a, Field b){
				return a.getHeight()-b.getHeight();
			}
			
		});
		
		this.p=p;
		this.rand=rand;
		this.tileCount=tileCount;
		
		this.maxWater = rand.nextInt(this.tileCount/coverWater);
		
		
		
		springs = new Stack<Field>();
		possibleSprings = new LinkedList<Field>();
		edgesRivers =  new ArrayList<ArrayList<Field>>();
		edgesMountains =  new ArrayList<ArrayList<Field>>();
		rivers =  new ArrayList<ArrayList<Field>>();
		mountains = new ArrayList<ArrayList<Field>>();
		ocean = new ArrayList<Field>();
		
	}
	
	
	
	
	public void buildTerrain(){
		//find Mountains and set them
			
		
				buildMountains();
				
			
				
				buildSprings();
					
						
				//TODO: Der Ort von Spring wird zufällig ausgewählt. Sorge dafür, dass es am Rand ist!
				//ebenso: überlege dir den Unterschied zwischen buildRiver und buildOcean. Zusammenpacken? Man könnte Ocean von einem River bilden!
				
				
				//create rivers (use the springs)
				 
				int maxWaterRiver=rand.nextInt(maxWater);
				int maxWaterOcean=maxWaterRiver-maxWater;
				
				int water =0;
				
				
				if(springs.size()>0){
//					System.out.println("Flüsse werden geleitet");
					
				while(water+averageWater<maxWaterRiver && rivers.size()<springs.size()){
					 	 rivers.add(buildRiver(springs.get(rand.nextInt(springs.size()))));
					 	 water+=rivers.get(rivers.size()-1).size();
				 }			
				}
				
				
		
				
				//if there is a River:
//				if(!rivers.isEmpty()){
//				ArrayList<Field> biggestRiver = rivers.get(0);
//				
//				for(ArrayList<Field> a : rivers){
//					if(a.size()>biggestRiver.size()){
//						biggestRiver=a;
//					}
//				}
				
						
				//TODO: find out an algo for height and maxWater! (maxWater have to be split up in Rivers and Ocean(s))
				//buildOcean(biggestRiver.get(biggestRiver.size()-1),50,maxWaterOcean);
				//}
				
				//TODO: Search all rivers which have more than 2 rivers next by them an set them to seas or coast.
	}
	
	
	private void buildMountains(){
		//find Mountains and set them
				HashSet<Field> hash = new HashSet<Field>(); //to save if the points was already visited
				Queue<Field> queue = new LinkedList<Field>();
								
				
				Field queueTemp;
				int index=0;
				ArrayList<Field> pointTemp;
					
				for(Field temp : p){
										
					if(temp.getHeight()>=Mountain_HEIGHT && !hash.contains(temp)){
						//the algo to create the mountains
						
						
						mountains.add(new ArrayList<Field>());
						queue.add(temp);
						
						while(!queue.isEmpty()){
							queueTemp=queue.poll();
							
							if(queueTemp.getHeight()>=Mountain_HEIGHT && !hash.contains(queueTemp)){
								mountains.get(index).add(queueTemp);
								pointTemp=p.getNeighbours(queueTemp);
								for(Field f : pointTemp){
									if(f!=null){
									queue.offer(f);
									}
								}
							}
							hash.add(queueTemp);
						}
							
							++index;
					}
				}
	}
	
	
	private void buildSprings(){
		
		
		//Mächtigkeit auswerten von Mountains!	
		for(ArrayList<Field> list : mountains){
			if(list.size()>Min_MOUNTAINS){
				for(Field field : list){
					field.setTyp(Field.Typ.Mountain);
				}
				
				//set one spring-Location 
				//TODO: found the edge of the mountain and insert a constant to say, how many springs are allowed of an huge mountain
				int index = rand.nextInt(list.size());
				springs.add(list.get(index));
			}
		}
		
	}
	
	/**
	 * 
	 * @return list of the field which where convert to rivers-fields
	 */
	private LinkedList<Field> fillWater(Field spring){
		
		queue.clear();
		
		//System.out.println(spring);
		spring.setTyp(Typ.Spring);
				
		LinkedList<Field> waterTile = new LinkedList<Field>();
		ArrayList<Field> neigbours;
		Field temp;

		if(!p.isEdge(spring)){
			neigbours=p.getNeighboursRisk(spring);
			for(Field field : neigbours){
				queue.offer(field);
			}
			
		temp=spring; //just to ensure that temp!=null	
		//queue.set(temp);
//		queue.offer(temp);
		do{
			
			
			if(temp.getTyp() == Field.Typ.NotDeclared){
				temp.setTyp(Typ.River);
				waterTile.add(temp);
				
				neigbours=p.getNeighboursRisk(temp);
				for(Field field : neigbours){
					// nun zur Behebung PrioritySetQueue Erstellt!
						queue.offer(field);
					
				}

			}
			
			//seitliche Wegstücke werden mehrmals betrachtet! kann ausgenutzt werden gerade Flüsse zu erzeugen!
			
			temp=queue.remove();
				
			}while(!p.isEdge(temp) && !queue.isEmpty() 
					&& temp.getTyp()!=Typ.River
					);
		
			if(temp.getTyp()==Typ.NotDeclared){
				temp.setTyp(Typ.River);
				waterTile.add(temp);
			}
		
		
		}
		return waterTile;

	}
	
private ArrayList<Field> buildRiver(Field spring){
		
		queue.clear();
		spring.setTyp(Typ.Spring);
		riverReader=0;
				
		ArrayList<Field> waterTile = new ArrayList<Field>();
		Field temp = spring;
		Field before;
		Field temp2;

		if(!p.isEdge(spring)){
			
			for(Field field : p.getNeighboursRisk(spring)){
				queue.offer(field);
			}
			
		before=spring;	
		temp=queue.remove();
		
		queue.clear();
			
			
		while(!p.isEdge(temp)	&& temp.getTyp()!=Typ.River){
			
			
			
				temp.setTyp(Typ.River);
				waterTile.add(temp);
				
				temp2=temp;
				
				temp=getCalculatedMin(temp,before);
				
				
				before=temp2;
				
			
				
			}
			}
		
			if(temp.getTyp()==Typ.NotDeclared){
				temp.setTyp(Typ.River);
				waterTile.add(temp);
			}
			
		
		return waterTile;

	}
	
/**
 * create a linear River. Arraylength is the indicator for the size of the waterstream.
 * 
 * @param spring[0] have to be the main-spring
 * @return All Fields that are now a River
 */
private LinkedList<Field> buildRiver(Field[] springs){
		
		queue.clear();
		
		for(Field temp: springs)
		temp.setTyp(Typ.Spring);
				
		LinkedList<Field> waterTile = new LinkedList<Field>();
		ArrayList<Field> neigbours;
		Field temp;
		Field before;

		for(Field spring : springs){
		if(!p.isEdge(spring)){
			neigbours=p.getNeighboursRisk(spring);
			for(Field field : neigbours){
				queue.offer(field);
			}
			
		temp=spring;
		before=spring;
		//just to ensure that temp!=null	
		//queue.set(temp);
//		queue.offer(temp);
		do{
			
			
			if(temp.getTyp() == Field.Typ.NotDeclared){
				temp.setTyp(Typ.River);
				waterTile.add(temp);
				
				neigbours=p.getFore(temp,before);
				for(Field field : neigbours){
					// nun zur Behebung PrioritySetQueue Erstellt!
						queue.offer(field);
					
				}

			}
			
			before=temp;
			temp=queue.remove();
				
			}while(!p.isEdge(temp) && !queue.isEmpty() 
					&& temp.getTyp()!=Typ.River
					);
		
			if(temp.getTyp()==Typ.NotDeclared){
				temp.setTyp(Typ.River);
				waterTile.add(temp);
			}
		}
		
		queue.clear(waterTile);
		
		}
		return waterTile;

	}
	
	private LinkedList<Field> buildOcean(Field field, int height, int maxWater){
		
//		System.out.println(queue.size());
//		System.out.println(queue.sizeHash());
		
		queue.clear();
		
//		System.out.println(queue.size());
//		System.out.println(queue.sizeHash());
//	
//		System.out.println(queue.add(field));
		queue.add(field);
//		
//		System.out.println(queue.size());
//		System.out.println(queue.sizeHash());
		
		int currentHeight=0;
		int currentWater=0;
		Field temp = null;
		Collection<Field> coll;
		LinkedList<Field> ocean = new LinkedList<Field>();
		
		while(!queue.isEmpty() && currentHeight<= height && currentWater <= maxWater){
			temp = queue.poll();

			coll=p.getNeighbours(temp);
			//System.out.println(coll.size());
			for(Field f : coll){
				queue.add(f);
			}
			currentHeight = temp.getHeight();
			++currentWater;
			temp.setTyp(Typ.River);
			ocean.add(temp);
		}
//		System.out.println(currentWater);
		return ocean;
	}
		
	
	Field getMinForRiver(Field now, Field before){
		ArrayList<Field> lBefore = p.getNeighboursRisk(before);
		ArrayList<Field> lNow = p.getNeighboursRisk(now);
		Field best=null;
		
		for(Field temp: lNow){
			if(!lBefore.contains(temp) && (best==null || temp.getHeight()<best.getHeight())){
				best=temp;
			}
		}
		
		return best;
	}
	
	//use the riverReader
	
	
Field getCalculatedMin(Field now, Field before){
		
		Field min;
		
		Point direction = before.getPoint().getDifference(now.getPoint());
		Field[] poss =  new Field[3];
		poss[1]=p.getField(now.getPoint().add(direction));
		poss[0]=p.getField(poss[1].getPoint().getLeft(now.getPoint()));
		poss[2]=p.getField(poss[1].getPoint().getRight(now.getPoint()));
		
		min=poss[1];
		
		if(Math.abs(riverReader)<1){
			for(Field temp : poss){
				if(temp.getHeight()<min.getHeight()){
					min=temp;
				}
			}
		}else if(riverReader==2){
			if(poss[0].getHeight()<min.getHeight()+20){
				min=poss[0];
			}
		}else if(riverReader==-2){
			if(poss[2].getHeight()<min.getHeight()+20){
				min=poss[2];
			}
		}else if(riverReader==1){
			if(poss[0].getHeight()<poss[1].getHeight()+5){
				if(poss[0].getHeight()<poss[2].getHeight()+10){
					min = poss[0];
				}else{
					min=poss[2];
				}
			}else if(poss[2].getHeight()+5<poss[1].getHeight()){
				min=poss[2];
			}else{
				min=poss[1];
			}
		}else if(riverReader==-1){
			if(poss[2].getHeight()<poss[1].getHeight()+5){
				if(poss[2].getHeight()<poss[0].getHeight()+10){
					min=poss[2];
				}else{
					min=poss[0];
				}
			}else if(poss[0].getHeight()+5<poss[1].getHeight()){
				min=poss[0];
			}else{
				min=poss[1];
			}
			
		}
		
				
		if(poss[0]==min){
			--riverReader;
		}else if(poss[2]==min){
			++riverReader;
		}
		
		return min;
		
	}
	

	void reset(){
		
		queue.clear();
		mountains.clear();
		edgesMountains.clear();
		springs.clear();
		possibleSprings.clear();
		rivers.clear();
		edgesRivers.clear();
		ocean.clear();
		
		for(Field temp : p){
			if(temp.getTyp()==Typ.Mountain){
				
			}else{
			temp.resetTyp();
			}
		}
		
	}
	
}
