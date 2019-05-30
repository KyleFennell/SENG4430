package map;

public class Field {

	private Point index;
	private int height;
	private Typ typ;
	
	
	Field(int a, int b){
		
		index= new Point(a,b);
		height=-1;
		typ= Typ.NotDeclared;
		//everything shall be null
	}
	
	Field(Point a){
		
		index= a;
		height=-1;
		typ= Typ.NotDeclared;
		//everything shall be null
	}
	
	void reset(){
		height=-1;
		typ=Typ.NotDeclared;
	}
	
	void resetTyp(){
		typ=Typ.NotDeclared;
	}
	
	void setHeight(int height) throws IllegalArgumentException{
		if(height<0 || height>100)
			throw new IllegalArgumentException();
		this.height=height;
	}
	
	void decreaseHeight(int decrease){
		if(height>decrease){
			this.height-=decrease;
		}else{
			this.height=0;
		}
	}
	
	public boolean hasHeight(){
		return height>=0;
	}
	
	public int getHeight(){
		return this.height;
	}
	
	public Point getPoint(){
		return index;
	}
	
	public String toString(){
		if(height<10&&height>=0){
			return "[  "+height+"]";
		}else if(height==100){
			return "["+height+"]";
		}else{
		return "[ "+height+"]";
		}
	}
	
	void setTyp(Typ typ){
		this.typ=typ;
	}
	
	public Typ getTyp(){
		return typ;
	}
	
	boolean equals(Typ typ){
		return this.typ==typ;
	}


	public enum Typ{
		Building,
		Spring,
		Mountain,
		River,
		Forest,
		Sea,
		Coast,
		Desert,
		Flatland,
		NotDeclared;
		
		
	}
	
}
