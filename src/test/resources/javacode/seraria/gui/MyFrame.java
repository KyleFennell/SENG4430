package gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import map.World;

public class MyFrame extends JFrame{

	private final World world;
	
	public MyFrame(String s, final World world){
	super(s);	
	this.world=world;
	
	setForeground(Color.ORANGE);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(50, 50, 1200, 750);
	
	JMenuBar menuBar = new JMenuBar();
	setJMenuBar(menuBar);
	
	JMenu Reset = new JMenu("Reset");
	menuBar.add(Reset);
	JMenuItem Height = new JMenuItem("reset Height");
	Height.addActionListener(new ActionListener() {
		public void actionPerformed( ActionEvent e ) {
			world.generateHeight();
			repaint();		
		}
	} );
	Reset.add(Height);
	JMenuItem Terrain = new JMenuItem("reset Terrain");
	Terrain.addActionListener(new ActionListener() {
		public void actionPerformed( ActionEvent e ) {
			world.generateTerrain();
			repaint();		
		}
	} );
	Reset.add(Terrain);
	
	}
	
	
	void generateTerrain(){
		world.generateTerrain();
		repaint();
	}
	
}
