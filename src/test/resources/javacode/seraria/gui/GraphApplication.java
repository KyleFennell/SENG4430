package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

import map.World;

public class GraphApplication {
	
	public static void main( String... args ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {

				
				final World world = new World(50);
				JFrame window = new MyFrame( world.getName()+" - "+world.getSeed(), world );

				window.setSize( 600, 600 );
				Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
				window.setLocation(d.width/2 - window.getWidth()/2, d.height/2 - window.getHeight()/2);

				window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

				
				JButton button1 = new JButton( "show Height" );
				//JButton button2 = new JButton( "generate again" );
				final MapComponent component = new MapComponent(world);

				window.setLayout( new BorderLayout() );
				//window.add( button2, BorderLayout.NORTH );
				window.add( button1, BorderLayout.SOUTH );
				window.add( component, BorderLayout.CENTER );

				button1.addActionListener( new ActionListener() {
					public void actionPerformed( ActionEvent e ) {
						component.change();		
					}
				} );

//				button2.addActionListener( new ActionListener() {
//					public void actionPerformed( ActionEvent e ) {
//						component.generateTerrain();
//					}
//				});

//				component.addMouseListener( new MyMouseListener( component ) );
				window.setVisible( true );
			}
		} );
	}

//	private static class MyMouseListener extends MouseAdapter {
//		private final JMyComponent component;
//
//		private MyMouseListener( JMyComponent component ) {
//			this.component = component;
//		}
//
//		public void mouseClicked( MouseEvent e ) {
//			super.mouseClicked( e );
//			if( SwingUtilities.isRightMouseButton( e ) )
//				component.addPoint( new Point2D.Double( e.getX(), e.getY() ) );
//			component.repaint();
//		}
//	}
//	
}
