package graph;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.*;

public class ArrayComponent extends JComponent{
		int[] array;
		int max;
		
		
		public ArrayComponent(){
			
		}
		
		public ArrayComponent(int[] array){
			this.array=array;
			this.max=findMax(array);
			repaint();
			
		}
		
		public void setArray(int[] array){
			this.array=array;
			this.max=findMax(array);
			repaint();
		}
		
		public int findMax(int[] array){
			int max = 0;
			for(int i=0; i<array.length;++i)
				if(max<array[i])
					max=array[i];
			return max;
		}
		
		public void paintComponent (Graphics g){
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D)g;
			g2d.setColor(Color.BLUE);
			
			int widthRect=getWidth()/this.array.length;
			int heightRect=getHeight()/this.max;
			
			for(int i=0; i<array.length; ++i){
				g2d.fillRect(i*widthRect, 0, widthRect, array[i]*heightRect);
			}
			
			g2d.setColor(Color.BLACK);
			for(int i=0; i<array.length; ++i){
				g2d.drawRect(i*widthRect, 0, widthRect, array[i]*heightRect);
			
			
			//g2d.fillRect(0, 0, 500, 500);
			
			}
		}
}
