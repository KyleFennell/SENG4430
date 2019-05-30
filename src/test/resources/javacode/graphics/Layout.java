package graphics;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;


/**
 * Creates a window and adds a menu to load several test graphs.
 * @author Jan-Philipp Kappmeier
 */
public class Layout {

	/**
	 * Initializes a window and sets it to be visible.
	 * @param args 
	 */
	public static void main( String... args ) {
		SwingUtilities.invokeLater( new Runnable() {

			public void run() {
				// Generate a new window and center on the screen
				JFrame window = new JFrame( "4gewinnt" );
				window.setSize( 800, 800 );
				window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
				Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
				window.setLocation(d.width/2 - window.getWidth()/2, d.height/2 - window.getHeight()/2);

				// Create an instance of the graph board
				final Comp comp = new Comp();
				
				// Set the layout manager and add the graph board
				window.setLayout( new BorderLayout() );
				window.add( comp, BorderLayout.CENTER );
				
			
				
				comp.addMouseListener(new MyMouseListener());
				
				   class MyMouseListener extends MouseAdapter{
			  
			  		public MyMouseListener(){
			  		super();
			  		}		
			  
					public void mouseClicked(MouseEvent e){
						super.mouseClicked(e);
						if(SwingUtilities.isRightMouseButton( e ))
//							comp.increaseThick();
					
					comp.repaint();	
					}
				 }
				
				/*board.addMouseListener(new EmbeddedGraphExample().new MyMouseListener());
				
				  class MyMouseListener extends MouseAdapter{
			  
			  		public MyMouseListener(){
			  		super();
			  		}		
			  
					public void mouseClicked(MouseEvent e){
						super.mouseClicked(e);
						if(SwingUtilities.isRightMouseButton( e ))
							board.increaseThick();
						if(SwingUtilities.isLeftMouseButton(e))
							board.decreaseThick();
					board.repaint();	
					}
				 }*/
				 
				
				
				// Add some menus for test instances
//				window.setJMenuBar( getMenuBar( board ) );
				
				// show the window
				window.setVisible( true );
			}
			
		} );
	
	}
}