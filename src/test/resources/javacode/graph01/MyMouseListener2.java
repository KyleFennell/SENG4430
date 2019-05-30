package graph;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

class MyMouseListener2 extends MouseAdapter{
	JGraphBoard board;
	
	public MyMouseListener2(JGraphBoard board){
		super();
		this.board=board;
	}
	
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
		if(SwingUtilities.isLeftMouseButton( e ))
			board.increaseThick();
		if(SwingUtilities.isRightMouseButton(e))
			board.decreaseThick();
	board.repaint();	
	}
 }