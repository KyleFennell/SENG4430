package tower;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Stack;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.UIManager;


/**
 * TowerFrame.java
 * @author Jens Schulz
 */
public class TowersOfBerlin extends JFrame {
	/** Panel for drawing the towers. */
	private TowerPanel hanoiPanel;
	
	/** A timer used for animation. */
	private Timer timer;
	/** A stack storing the steps to be done to solve the problem. */
	Stack<DiskAction> toDo = new Stack<DiskAction>();
	/** A stack storing the steps that have been done already. */
	Stack<DiskAction> done = new Stack<DiskAction>();
	/** The number of disks initially on a tower. */
	private static final int DEFAULT_NUM_DISKS = 3;

	/**
	 * Construct the frame for Towers of Berlin. Creates two buttons for forward
	 * and backward, sets up the towers and available places and adds additional
	 * elements to reset the game and start solving.
	 */
	public TowersOfBerlin() {
		super( "Towers of Berlin" );

		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		hanoiPanel = new TowerPanel( DEFAULT_NUM_DISKS );
		
		
		getContentPane().add( hanoiPanel, BorderLayout.CENTER );

		//create button panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout( new GridLayout( 1, 2 ) );

		JButton redoButton = new JButton( "<<" );
		redoButton.setName( "backward" );
		MyButtonListener buttonHandler = new MyButtonListener();

		redoButton.addActionListener( buttonHandler );
		buttonPanel.add( redoButton );

		JButton undoButton = new JButton( ">>" );
		undoButton.setName( "forward" );
		undoButton.addActionListener( buttonHandler );
		buttonPanel.add( undoButton );

		MySliderListener sliderHandler = new MySliderListener();

		JPanel panelSlider = new JPanel();
		panelSlider.setLayout( new BoxLayout( panelSlider, BoxLayout.Y_AXIS ) );
		panelSlider.add( buttonPanel );

		getContentPane().add( panelSlider, BorderLayout.NORTH );

		// create panel and slider for the number of disks
		JPanel panelDisks = new JPanel();
		JLabel labelDisks = new JLabel( "Number of disks:" );

		JSlider sliderDisk = new JSlider();
		sliderDisk.setName( "disks" );
		sliderDisk.setMinimum( TowerUtil.MIN_DISKS );
		sliderDisk.setMaximum( TowerUtil.MAX_DISKS );
		sliderDisk.setValue( DEFAULT_NUM_DISKS );
		sliderDisk.setMajorTickSpacing( 1 );
		sliderDisk.setPaintLabels( true );
		sliderDisk.setPaintTicks( true );
		sliderDisk.setSnapToTicks( true );
		sliderDisk.addMouseListener( sliderHandler );

		final JButton automatic = new JButton( "Automatic" );
		JButton compute = new JButton( "Calculate solution" );
		compute.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				automatic.setEnabled( false );
				solveProblem();
				automatic.setEnabled( true );
			}
		});
		automatic.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				final int delayFactor = TowerUtil.MAX_DELAY/(TowerUtil.MAX_DISKS - TowerUtil.MIN_DISKS);
				//final int update = -delayFactor*hanoiPanel.getTower( 0 ).getMaxHeight() + TowerUtil.MAX_DELAY + delayFactor * TowerUtil.MIN_DISKS;
				int update=1;
				timer.setDelay( update );
				timer.start();
			}
		});

		panelDisks.add( labelDisks );
		panelDisks.add( sliderDisk );
		panelDisks.add( compute );
		panelDisks.add( automatic );
		getContentPane().add( panelDisks, BorderLayout.SOUTH );
		pack();

		// Set up a timer
		
		int update = (1000/48);
		timer = new Timer( update, new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				forward();
				if( toDo.empty() )
					timer.stop();
				repaint();
			}
		});
	}

	/**
	 * Solves the problem of swapping the two initally filled towers. The
	 * generated solution is stored as list of actions.
	 */
	private void solveProblem() {
		// TODO create solver instance, solve and store the actions in the list toDo
		
		hanoiPanel.resetTowers();
		TowersOfBerlinSolver solv = new TowersOfBerlinSolver(hanoiPanel.getTower(0),hanoiPanel.getTower(1),hanoiPanel.getTower(2));
		Stack<DiskAction> negativ =solv.solve();
		

		
		while(!negativ.empty()){
			toDo.push(negativ.pop());
		}


	}

	/**
	 * Takes one of the stored actions and executes it on the towers in the panel.
	 * The action is than stored to undo it.
	 */
	private void forward() {
		/* TODO handle forward events. */
//		System.out.println(toDo.peek().from+" -> "+toDo.peek().to);
		if(!toDo.empty()){
		hanoiPanel.getTower(toDo.peek().to).addDisk(hanoiPanel.getTower(toDo.peek().from).removeDisk());
		done.push(toDo.pop());
		repaint();
		}
	}

	/**
	 * Takes on of the stored action already performed to undo it and executes it
	 * on the panel. The action is then again stored in the list of actions to
	 * continue the solution.
	 */
	private void backward() {
		/* TODO handle backward events. */
		if(!done.empty()){
		hanoiPanel.getTower(done.peek().from).addDisk(hanoiPanel.getTower(done.peek().to).removeDisk());
		toDo.push(done.pop());
		repaint();
		}
	}

	/**
	 * Starts the program, initializes a window and displays it.
	 * @param args
	 */
	public static void main( String args[] ) {
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		} catch( Exception e ) {
			// Problem changing UI occured. Ignore and show in default mode instead.
		}
		java.awt.EventQueue.invokeLater( new Runnable() {
			@Override
			public void run() {
				new TowersOfBerlin().setVisible( true );
			}
		} );
	}

	/**
	 * Listener for the slider.
	 */
	class MySliderListener extends MouseAdapter {
		/** gets and sets the number of disks */
		@Override
		public void mouseReleased( MouseEvent event ) {
			JSlider source = (JSlider)event.getSource();
			/* gets and sets the number of disks */
			if( source.getName().equals( "disks" ) ) {
				timer.stop();
				toDo.clear();
				done.clear();
				hanoiPanel.setDiskCount( source.getValue() );
				
			}
		}
	}

	/**
	 * Listener catching all action events on buttons.
	 */
	class MyButtonListener implements ActionListener {
		@Override
		public void actionPerformed( ActionEvent e ) {
			JButton source = (JButton)e.getSource();
			String name = source.getName();
			timer.stop();
			if( "forward".equals( name ) )
				forward();
			else if( "backward".equals( name ) )
				backward();
			repaint();
		}
	}
}
