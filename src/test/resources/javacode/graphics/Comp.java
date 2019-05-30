package graphics;

import java.awt.Graphics;

import javax.swing.*;

public class Comp extends JComponent {
	
	private byte[][] array;
		
	public void paintComponent (Graphics g){
		super.paintComponent(g);
		
		double maxHeight=getHeight();
		double maxWidth=getWidth();
		double stepHeight=maxHeight/array[0].length;
		double stepWidth=maxWidth/array.length;
		
		
	}
}
