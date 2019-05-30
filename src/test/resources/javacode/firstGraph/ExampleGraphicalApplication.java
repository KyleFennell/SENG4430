package firstGraph;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class ExampleGraphicalApplication {

	public static void main( String... args ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {

				JFrame window = new JFrame( "Grafisches Beispielprogramm" );

				window.setSize( 800, 600 );
				Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
				window.setLocation(d.width/2 - window.getWidth()/2, d.height/2 - window.getHeight()/2);

				window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

				JButton button1 = new JButton( "Clear" );
				JButton button2 = new JButton( "Random" );
				final JMyComponent myComponent = new JMyComponent();

				window.setLayout( new BorderLayout() );
				window.add( button1, BorderLayout.NORTH );
				window.add( button2, BorderLayout.SOUTH );
				window.add( myComponent, BorderLayout.CENTER );

				button1.addActionListener( new ActionListener() {
					public void actionPerformed( ActionEvent e ) {
						myComponent.clear();
						myComponent.repaint();
					}
				} );

				button2.addActionListener( new ActionListener() {
					public void actionPerformed( ActionEvent e ) {
						Random rnd = new Random();
						Point2D.Double point = new Point2D.Double( rnd.nextInt( myComponent.getWidth() ), rnd.nextInt( myComponent.getWidth() ) );
						myComponent.addPoint( point );
						myComponent.repaint();
					}
				});

				myComponent.addMouseListener( new MyMouseListener( myComponent ) );

				window.setVisible( true );
			}
		} );
	}

	private static class MyMouseListener extends MouseAdapter {
		private final JMyComponent component;

		private MyMouseListener( JMyComponent component ) {
			this.component = component;
		}

		public void mouseClicked( MouseEvent e ) {
			super.mouseClicked( e );
			if( SwingUtilities.isRightMouseButton( e ) )
				component.addPoint( new Point2D.Double( e.getX(), e.getY() ) );
			component.repaint();
		}
	}
}
