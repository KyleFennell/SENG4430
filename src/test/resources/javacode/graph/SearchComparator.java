package graph;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.SecureRandom;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class SearchComparator {
	int currentSteps = 0;
	Sorter sorter = null;

	private void showWindow() {

		final JFrame window = new JFrame( "Vergleich von Suchalgorithmen" );

		window.setSize( 800, 600 );
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		window.setLocation( d.width / 2 - window.getWidth() / 2, d.height / 2 - window.getHeight() / 2 );

		window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		//TODO: Komponente für Darstellung von Arrays erstellen und einbinden.
		//final ArrayComponent comp = new ArrayComponent();
		//window.add(comp, BorderLayout.CENTER);
		//comp.repaint();

		final JPanel componentsNorth = new JPanel();

		componentsNorth.add( new JLabel( "Eingabezahlen: " ) );
		final JTextField inputNumbers = new JTextField( "9;8;7;6;5;4;3;2;1", 16 );
		componentsNorth.add( inputNumbers );
		JButton buttonRandom = new JButton( "Random" );
		componentsNorth.add( buttonRandom );
		JButton buttonReset = new JButton( "Reset" );
		componentsNorth.add( buttonReset );
		JButton buttonNext = new JButton( "Nächster Sortierschritt" );
		componentsNorth.add( buttonNext );
		final JLabel steps = new JLabel( "(BubbleSort)-Schritte: 0" );
		componentsNorth.add( steps );

		window.setLayout( new BorderLayout() );
		
		
		
		window.add( componentsNorth, BorderLayout.NORTH );
		//window.add( buttonNext, BorderLayout.SOUTH );
		final ArrayComponent comp = new ArrayComponent();
		window.add(comp, BorderLayout.CENTER);
		comp.repaint();
		

		buttonReset.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				//TODO: Einlesen der Zahlen und Übergabe an Sortierer
				currentSteps=0;
				String s=inputNumbers.getText();
				String[] strings= s.split(";");
				int[] numbers= new int[strings.length];
				for(int i=0; i<numbers.length; ++i)
					numbers[i]=Integer.parseInt(strings[i]);
				
				sorter.setNumbers(numbers);
				comp.setArray(numbers);
				steps.setText("(" + sorter.getName() + ")-Schritte: " + sorter.getSwaps());
				comp.repaint();
				
				
				
				
			}
		} );

		buttonNext.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				//TODO: Einen Sortierschritt ausführen.
				sorter.setUpTo(++currentSteps);
				sorter.sort();
				
				steps.setText("(" + sorter.getName() + ")-Schritte: " + sorter.getSwaps());
				comp.repaint();
				
			}
		} );

		buttonRandom.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent e ) {
				Random rnd = new SecureRandom();
				String a = "";
				for( int i = 0; i < 20; ++i ) {
					a = a + rnd.nextInt( 40 ) + ";";
				}
				a += rnd.nextInt( 40 );
				inputNumbers.setText( a );
			}
		});

		JPanel sortAlgorithms = new JPanel();
		JButton insertionSort = new JButton( "InsertionSort" );
		sortAlgorithms.add( insertionSort );
		JButton bubbleSort = new JButton( "BubbleSort" );
		sortAlgorithms.add( bubbleSort );
		JButton selectionSort = new JButton( "SelectionSort" );
		sortAlgorithms.add( selectionSort );
		JButton radixSort = new JButton( "RadixSort" );
		sortAlgorithms.add( radixSort );
		JButton mergeSort = new JButton( "MergeSort" );
		sortAlgorithms.add( mergeSort );

		window.add( sortAlgorithms, BorderLayout.SOUTH );

		insertionSort.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				// TODO create insertion sort instance and store in sorter
			}
		});
		bubbleSort.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				// TODO create bubble sort instance and store in sorter
				sorter = new CountingBubbleSort(comp);
			}
		});
		selectionSort.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				// TODO create selection sort instance and store in sorter
			}
		});
		mergeSort.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				// TODO create merge sort instance and store in sorter
			}
		});
		radixSort.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				// TODO create radix sort instance and store in sorter
			}
		});

		window.setVisible( true );
	}


	public static void main( String... args ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				SearchComparator sc = new SearchComparator();
				sc.showWindow();
			}
		} );
	}
	
	
	
}
