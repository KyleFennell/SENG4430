package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;

import javax.swing.JComponent;




import map.*;

public class MapComponent extends JComponent{

	private World world;
	private int sizeHex;
	private int size;
	
private static View view;
	
	private enum View{
		Typ,
		Height;
		
		private View change(){
			if(this.ordinal()==View.values().length-1)
				return View.values()[0];
			return View.values()[this.ordinal()+1];  
		}
	}
	
	
	public MapComponent(World world){
		this.world=world;
		this.size=world.getPattern().getSize();
		view= View.Typ;
	}
	
	void change(){
		view = view.change();
		repaint();
	}
	
	public void paintComponent( Graphics g ) {
		super.paintComponent( g );
		
		setSizeHex();

		// Cast graphics component to correct type
		Graphics2D g2d = (Graphics2D)g;

		// Clear background
		g2d.setColor( Color.white );
		
//		Iterator<Field> itr = world.getPattern().iterator();
//		Field temp;
//		while(itr.hasNext()){	
//			temp=itr.next();
		
		
		switch(view){
		case Typ:
			for(Field temp : world.getPattern()){
				switch(temp.getTyp()){
				case River:
					g2d.setColor(Color.cyan);
					break;
				case Mountain:
					g2d.setColor(Color.darkGray);
					break;
				case Spring:
					g2d.setColor(Color.blue);
					break;
				default:
					g2d.setColor(Color.green);
					break;
				}
				Point p = this.transform(temp.getPoint());
				g2d.fillOval(p.getIdA(),p.getIdB(), sizeHex, sizeHex);
			}
			break;
		case Height:
			for(Field temp : world.getPattern()){
				if(temp.getHeight()<=20)
					g2d.setColor(Color.green);
				else if(temp.getHeight()<=40){
					g2d.setColor(Color.yellow);
				}else if(temp.getHeight()<=60){
					g2d.setColor(Color.ORANGE);
				}else if(temp.getHeight()<=80){	
					g2d.setColor(Color.red);
				}else{
					g2d.setColor(Color.black);
				}
				Point p = this.transform(temp.getPoint());
				g2d.fillOval(p.getIdA(),p.getIdB(), sizeHex, sizeHex);
			}
			break;
		default:
			break;			
			
		}
		
			
	}
	
	public World getWorld(){
		return world;
	}
	
	
	private void setSizeHex(){
		sizeHex=(int) Math.min((getHeight())/(double)(world.getPattern().getWidth()+1),(getWidth())/(double)(world.getPattern().getWidth()+1));
	}
	
	
	private Point transform(Point p){
		if(p.getIdB()>size){
			return new Point(p.getIdA()*sizeHex-(p.getIdB()-size)*sizeHex/2,getHeight()-(p.getIdB()+1)*sizeHex); 
		}else{
			return new Point(p.getIdA()*sizeHex+(size-p.getIdB())*sizeHex/2,getHeight()-(p.getIdB()+1)*sizeHex);
		}
	}
	
	
	
}
