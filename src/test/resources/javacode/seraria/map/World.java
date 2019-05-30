package map;

import life.*;

import java.util.ArrayList;
import java.util.Random;

public class World {

	private long seed;
	private Random rand;
	private Pattern patter;
	private Height height;
	private Terrain terrain;
	private ArrayList<Charakter> chrakters;
	private ArrayList<Enemy> enemy;
	private String name;
	
	public World(){
		this(15);
	}
	
	public World(int size){
		
		this.seed=(long)(Math.random()*Long.MAX_VALUE);
		this.rand = new Random(seed);
		
		patter = new Pattern(size,0);
		height = new Height(patter,rand);
		patter.setHeight(height.getBiggerMap());
		terrain = new Terrain(patter, rand, patter.getTileCount());
		terrain.buildTerrain();
		chrakters = new ArrayList<Charakter>();
		enemy = new ArrayList<Enemy>();
		name="default";
		
	}
	
	public String toString(){
		return patter.toString();
	}
	
	public Pattern getPattern(){
		return patter;
	}
	
	public String getName(){
		return name;
	}
	
	public void generateHeight(){
		height.reset();
		terrain.reset();
		patter.setHeight(height.getBiggerMap());
		terrain.buildTerrain();
	}
	
	public void generateTerrain(){
		terrain.reset();
		terrain.buildTerrain();
	}
	
	public long getSeed(){
		return seed;
	}
	
}
