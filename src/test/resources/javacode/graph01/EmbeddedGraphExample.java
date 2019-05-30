package graph;

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
public class EmbeddedGraphExample {

	/**
	 * Initializes a window and sets it to be visible.
	 * @param args 
	 */
	public static void main( String... args ) {
		SwingUtilities.invokeLater( new Runnable() {

			public void run() {
				// Generate a new window and center on the screen
				JFrame window = new JFrame( "CoMa-Graph-Embedding" );
				window.setSize( 800, 800 );
				window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
				Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
				window.setLocation(d.width/2 - window.getWidth()/2, d.height/2 - window.getHeight()/2);

				// Create an instance of the graph board
				final JGraphBoard board = new JGraphBoard( EmbeddingTestInstances.SIMPLE.getGraph());
				
				// Set the layout manager and add the graph board
				window.setLayout( new BorderLayout() );
				window.add( board, BorderLayout.CENTER );
				
				//TODO: (e)
				
				board.addMouseListener(new MyMouseListener2(board));
				
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
				 
				
				//TODO: (f)
				
				// Add some menus for test instances
				window.setJMenuBar( getMenuBar( board ) );
				
				// show the window
				window.setVisible( true );
			}
			
		} );
	}
	
	/**
	 * <p>Draws the vee of an arc. The vee will be drawn at the end of a line that
	 * is supposed to go from <code>(x_1, y_1)</code> to <code>(x_2, y_2)</code>.
	 * </p>
	 * @param g the graphics context to draw on
	 * @param x1 <code>x</code>-coordinate of the start of the arc
	 * @param y1 <code>y</code>-coordinate of the start of the arc
	 * @param x2 <code>x</code>-coordinate of the end of the arc
	 * @param y2 <code>y</code>-coordinate of the end of the arc
	 * @param size the size of the vee. good value is the node size.
	 */
	public static void drawArrowTip( Graphics2D g, double x1, double y1, double x2, double y2, double size ) {
		// Store the old transformation to restore after drawing
		final AffineTransform oldTransform = g.getTransform();
		
		// Set up an affince transformation such that the arc will be vertically
		// aligned in the transformed space
    final double angle = Math.atan2( y2-y1, x2-x1 );
    g.translate( x2, y2 );
    g.rotate( (angle-Math.PI/2d) );

		// draw the vee to the transformed arc
		g.drawLine( 0, 0, (int)(-size*0.25), (int)(-size*0.5) );
		g.drawLine( 0, 0, (int)(+size*0.25), (int)(-size*0.5) );

		// restore transformation
		g.setTransform( oldTransform );
	}
	
	/**
	 * Generates the menu entries to select test instances.
	 */
	private static JMenuBar getMenuBar( final JGraphBoard board ) {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu( "Beispiele" );
		menuBar.add( menu );
		for( final EmbeddingTestInstances instance : EmbeddingTestInstances.getAllInstances() ) {
			JMenuItem menuItem = new JMenuItem( instance.getName() );
			menu.add( menuItem );

			menuItem.addActionListener( new ActionListener() {

				public void actionPerformed( ActionEvent e ) {
					board.setEmbeddedGraph( instance.getGraph() );
				}
			});
		}
		return menuBar;
	}
}