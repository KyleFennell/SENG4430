package graph;

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class JGraphBoard extends JComponent {
	EmbeddedGraph graph;
	int thick=30;
	
	public JGraphBoard(EmbeddedGraph graph){
	this.graph=graph;	
	}
	
	public void paintComponent (Graphics g){
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke( new BasicStroke( thick/15 ) );
		
		
		for(int i=0;i<graph.n;++i){
			//g2d.setColor( Color.red );
			//g2d.drawOval((int) transform(graph.points[i]).getX()-thick/2,(int) transform(graph.points[i]).getY()-thick/2, this.thick, this.thick);
			
			SimpleLinkedList<GraphEdge> edges = graph.getNode(i).getEdges();
			
			g2d.setColor( Color.black );
			for(edges.reset();edges.isValid(); edges.advance() ){
			g2d.drawLine((int) transform(graph.points[i]).getX(),(int) transform(graph.points[i]).getY() ,
				(int) transform(graph.points[edges.getCurrent().getEnd().id()]).getX(),
				(int) transform(graph.points[edges.getCurrent().getEnd().id()]).getY() );
			EmbeddedGraphExample.drawArrowTip(g2d,(int) transform(graph.points[i]).getX(),(int) transform(graph.points[i]).getY() ,
					(int) transform(graph.points[edges.getCurrent().getEnd().id()]).getX(),
					(int) transform(graph.points[edges.getCurrent().getEnd().id()]).getY()  , this.thick*2);
			}
		}
		for(int i=0;i<graph.n;++i){
			
		g2d.setColor( Color.red );
		g2d.drawOval((int) transform(graph.points[i]).getX()-thick/2,(int) transform(graph.points[i]).getY()-thick/2, this.thick, this.thick);
		}
	}
	
	public void setEmbeddedGraph(EmbeddedGraph g){
		this.graph = g;
		repaint();
		
	}
	
	public Point2D.Double transform(Point2D.Double point){
		//double distanceX =this.getX()-graph.getMinCorner().getX(); 
		//double distanceY =this.getY()-graph.getMinCorner().getY();
		
		double skalar = Math.min((getHeight()-thick*2)/graph.getHeight(), (getWidth()-thick*2)/graph.getWidth());
		double OffsetX = getWidth() - graph.getWidth()*skalar;
		OffsetX/=2;
		double OffsetY = getHeight() - graph.getHeight()*skalar;
		OffsetY/=2;
		
		return new Point2D.Double((point.getX()-graph.getMinCorner().getX())*skalar+OffsetX, (point.getY()-graph.getMinCorner().getY())*skalar+OffsetY);
		
	}
	
	public void setThick(int thick){
		if (thick>0)
		this.thick=thick;
	}
	
	public void increaseThick(){
		this.thick+=1;
	}
	
	public void decreaseThick(){
		if (this.thick>2)
			this.thick-=1;
	}
	
	
	
}
