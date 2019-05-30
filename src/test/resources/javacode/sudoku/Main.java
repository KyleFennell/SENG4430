package sudoku;

/*
 * Main.java
 *
 */
import java.awt.Cursor;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main extends javax.swing.JFrame {

  private List<Sudoku> sudokus = new LinkedList<Sudoku>();
  private int threshold = 100;

  private JButton buttonLoadSudokus;
  private JButton buttonStartComputation;
  private JCheckBox checkMultiCore;
  private JLabel labelLoadedFile;
  private JLabel labelLoadedSudokus;
  private JLabel labelSolvingProgress;
  private JLabel labelTimeProgress;
  private JProgressBar progressBarSudokus;
  private JProgressBar progressBarTime;
  private JScrollPane scrollPane;
  private JTextArea jTextArea1;
  private JTextField textFieldTimeLimit;

  /** Creates new form Main */
  public Main() {
    initComponents();
    setTitle( "Sudoku-Löser" );
    scrollPane.setBorder( null );
    setLocationRelativeTo( null );
  }

  /** This method is called from within the constructor to initialize the form.
   */
  @SuppressWarnings( "unchecked" )
  private void initComponents() {

    buttonLoadSudokus = new JButton();
    labelLoadedFile = new JLabel();
    labelLoadedSudokus = new JLabel();
    buttonStartComputation = new JButton();
    textFieldTimeLimit = new JTextField();
    labelSolvingProgress = new JLabel();
    progressBarSudokus = new JProgressBar();
    labelTimeProgress = new JLabel();
    progressBarTime = new JProgressBar();
    checkMultiCore = new JCheckBox();
    scrollPane = new JScrollPane();
    jTextArea1 = new JTextArea();

    setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );


    buttonLoadSudokus.setText( "Sudokus laden!" );
    buttonLoadSudokus.addMouseListener( new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked( java.awt.event.MouseEvent evt ) {
        loadInputClicked( evt );
      }
    } );

    JLabel labelLoadedFileText = new JLabel( "Aktuelle Sudoku-Sammlung:" );

    labelLoadedFile.setText( "Keine" );

    JLabel labelLoadedSudokusText = new JLabel( "Enthält:" );

    labelLoadedSudokus.setText( "0 Sudokus" );

    JPanel inputPanel = new JPanel();
    GroupLayout inputPanelLayout = new GroupLayout( inputPanel );
    inputPanel.setLayout( inputPanelLayout );
    inputPanel.setBorder( BorderFactory.createTitledBorder( "Input:" ) );

    inputPanelLayout.setHorizontalGroup(
            inputPanelLayout.createParallelGroup( GroupLayout.Alignment.LEADING )
            .addComponent( buttonLoadSudokus, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE )
            .addGroup( inputPanelLayout.createSequentialGroup()
                    .addGroup( inputPanelLayout.createParallelGroup( GroupLayout.Alignment.LEADING )
                            .addComponent( labelLoadedFileText )
                            .addComponent( labelLoadedSudokusText ) )
                    .addPreferredGap( LayoutStyle.ComponentPlacement.RELATED )
                    .addGroup( inputPanelLayout.createParallelGroup( GroupLayout.Alignment.LEADING )
                            .addComponent( labelLoadedSudokus, GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE )
                            .addComponent( labelLoadedFile, GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE ) )
                    .addContainerGap() )
    );
    inputPanelLayout.setVerticalGroup(
            inputPanelLayout.createParallelGroup( GroupLayout.Alignment.LEADING )
            .addGroup( inputPanelLayout.createSequentialGroup()
                    .addComponent( buttonLoadSudokus )
                    .addPreferredGap( LayoutStyle.ComponentPlacement.RELATED )
                    .addGroup( inputPanelLayout.createParallelGroup( GroupLayout.Alignment.BASELINE )
                            .addComponent( labelLoadedFileText )
                            .addComponent( labelLoadedFile ) )
                    .addPreferredGap( LayoutStyle.ComponentPlacement.RELATED )
                    .addGroup( inputPanelLayout.createParallelGroup( GroupLayout.Alignment.BASELINE )
                            .addComponent( labelLoadedSudokusText )
                            .addComponent( labelLoadedSudokus ) ) )
    );

    JPanel outputPanel = new JPanel();
    outputPanel.setBorder( BorderFactory.createTitledBorder( "Output:" ) );

    JLabel labelTaskText = new JLabel( "Löse soviele Sudokus wie möglich - Zeitlimit (in Sekunden):" );

    buttonStartComputation.setText( "Los!" );
    buttonStartComputation.addMouseListener( new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked( java.awt.event.MouseEvent evt ) {
        solveSudokusClicked( evt );
      }
    } );

    textFieldTimeLimit.setHorizontalAlignment( JTextField.RIGHT );
    textFieldTimeLimit.setText( "100" );

    labelSolvingProgress.setText( "0 von 0 Sudokus geloest" );

    labelTimeProgress.setText( "0 von 100 Sekunden gerechnet" );

    jTextArea1.setBackground( new java.awt.Color( 235, 233, 237 ) );
    jTextArea1.setColumns( 5 );
    jTextArea1.setEditable( false );
    jTextArea1.setFont( new java.awt.Font( "Tahoma", 0, 11 ) );
    jTextArea1.setLineWrap( true );
    jTextArea1.setRows( 2 );
    jTextArea1.setText( "Eigenen SudokuSolver für jeden CPU-Kern starten (schneller auf Multicore-Systemen, aber auch schwerer zu debuggen)" );
    jTextArea1.setAutoscrolls( false );
    jTextArea1.setBorder( null );
    jTextArea1.setDisabledTextColor( new java.awt.Color( 0, 0, 0 ) );
    jTextArea1.setEnabled( false );
    jTextArea1.setRequestFocusEnabled( false );
    scrollPane.setViewportView( jTextArea1 );

    GroupLayout outputPanelLayout = new GroupLayout( outputPanel );
    outputPanel.setLayout( outputPanelLayout );
    outputPanelLayout.setHorizontalGroup(
            outputPanelLayout.createParallelGroup( GroupLayout.Alignment.LEADING )
            .addGroup( outputPanelLayout.createSequentialGroup()
                    .addComponent( labelTaskText )
                    .addPreferredGap( LayoutStyle.ComponentPlacement.RELATED )
                    .addComponent( textFieldTimeLimit, GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE ) )
            .addComponent( buttonStartComputation, GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE )
            .addGroup( outputPanelLayout.createSequentialGroup()
                    .addComponent( labelSolvingProgress )
                    .addContainerGap() )
            .addComponent( progressBarSudokus, GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE )
            .addGroup( outputPanelLayout.createSequentialGroup()
                    .addComponent( labelTimeProgress )
                    .addContainerGap() )
            .addComponent( progressBarTime, GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE )
            .addGroup( outputPanelLayout.createSequentialGroup()
                    .addComponent( checkMultiCore )
                    .addPreferredGap( LayoutStyle.ComponentPlacement.RELATED )
                    .addComponent( scrollPane, GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE ) )
    );
    outputPanelLayout.setVerticalGroup(
            outputPanelLayout.createParallelGroup( GroupLayout.Alignment.LEADING )
            .addGroup( outputPanelLayout.createSequentialGroup()
                    .addGroup( outputPanelLayout.createParallelGroup( GroupLayout.Alignment.BASELINE )
                            .addComponent( labelTaskText )
                            .addComponent( textFieldTimeLimit, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) )
                    .addPreferredGap( LayoutStyle.ComponentPlacement.RELATED )
                    .addComponent( buttonStartComputation )
                    .addPreferredGap( LayoutStyle.ComponentPlacement.RELATED )
                    .addComponent( labelSolvingProgress )
                    .addPreferredGap( LayoutStyle.ComponentPlacement.RELATED )
                    .addComponent( progressBarSudokus, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
                    .addPreferredGap( LayoutStyle.ComponentPlacement.UNRELATED )
                    .addComponent( labelTimeProgress )
                    .addPreferredGap( LayoutStyle.ComponentPlacement.RELATED )
                    .addComponent( progressBarTime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
                    .addGap( 6, 6, 6 )
                    .addGroup( outputPanelLayout.createParallelGroup( GroupLayout.Alignment.LEADING )
                            .addComponent( checkMultiCore )
                            .addComponent( scrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) ) )
    );

    GroupLayout layout = new GroupLayout( getContentPane() );
    getContentPane().setLayout( layout );
    layout.setHorizontalGroup(
            layout.createParallelGroup( GroupLayout.Alignment.LEADING )
            .addComponent( inputPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE )
            .addComponent( outputPanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE )
    );
    layout.setVerticalGroup(
            layout.createParallelGroup( GroupLayout.Alignment.LEADING )
            .addGroup( layout.createSequentialGroup()
                    .addComponent( inputPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE )
                    .addPreferredGap( LayoutStyle.ComponentPlacement.RELATED )
                    .addComponent( outputPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE ) )
    );

    pack();
  }

  private void loadInputClicked( java.awt.event.MouseEvent evt ) {//GEN-FIRST:event_loadInputClicked
    JFileChooser dialog = new JFileChooser();
    dialog.setFileFilter( new FileNameExtensionFilter( "Sudoku-Sammlung", "sudoku" ) );
    int result = dialog.showOpenDialog( this );
    if( result == JFileChooser.APPROVE_OPTION ) {
      File selectedFile = dialog.getSelectedFile();
      if( selectedFile.exists() )
        try {
          setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
          sudokus = SudokuReader.readSudokusFromFile( selectedFile.getAbsolutePath() );
          labelLoadedFile.setText( selectedFile.getName() );
          labelLoadedSudokus.setText( sudokus.size() + ((sudokus.size() != 1) ? " Sudokus" : " Sudoku") );
        } catch( Exception ex ) {
          JOptionPane.showMessageDialog( rootPane, ex.getMessage(), "Fehler!", JOptionPane.ERROR_MESSAGE );
        } finally {
          setCursor( Cursor.getDefaultCursor() );
        }
      else
        JOptionPane.showMessageDialog( rootPane, "Datei existiert nicht.", "Fehler!", JOptionPane.ERROR_MESSAGE );
    }
  }
  private AtomicInteger count;
  private AtomicInteger running;

  private void solveSudokusClicked( java.awt.event.MouseEvent evt ) {//GEN-FIRST:event_solveSudokusClicked
    String thres = textFieldTimeLimit.getText();
    try {
      threshold = Integer.parseInt( thres );
    } catch( Exception ex ) {
      JOptionPane.showMessageDialog( rootPane, "Zeitlimit ist keine ganze Zahl.", "Fehler!", JOptionPane.ERROR_MESSAGE );
      return;
    }
    if( sudokus.size() > 0 ) {
      buttonStartComputation.setEnabled( false );
      progressBarSudokus.setMaximum( sudokus.size() );
      progressBarSudokus.setValue( 0 );
      labelSolvingProgress.setText( "0 von " + sudokus.size() + " " + (sudokus.size() > 1 ? "Sudokus" : "Sudoku") + " gelöst" );
      progressBarTime.setMaximum( threshold );
      progressBarTime.setValue( 0 );
      labelTimeProgress.setText( "0 von " + textFieldTimeLimit.getText() + " Sekunden gerechnet" );
      final int cpus = (checkMultiCore.getModel().isSelected()) ? Runtime.getRuntime().availableProcessors() : 1;
      count = new AtomicInteger( 0 );
      running = new AtomicInteger( cpus );
      List<Thread> threads = new LinkedList<Thread>();

      final ConcurrentLinkedQueue<Sudoku> queue = new ConcurrentLinkedQueue<Sudoku>( sudokus );

      final long start = System.currentTimeMillis();
      for( int i = 0; i < cpus; i++ )
        threads.add( new Thread( new Runnable() {

          @Override
          public void run() {
            Sudoku sudoku;
            while( (sudoku = queue.poll()) != null ) {
              SudokuSolver s = new SudokuSolver( sudoku );
              int[][] solution = s.solve();
              assert Sudoku.isValid( solution ) : Arrays.deepToString( solution );

              count.incrementAndGet();
              if( System.currentTimeMillis() - start > threshold * 1000 )
                break;
            }
            boolean terminated = (running.decrementAndGet() == 0);
            if( terminated ) {
              long now = System.currentTimeMillis();
              labelSolvingProgress.setText( count.intValue() + " von " + sudokus.size() + " " + (sudokus.size() > 1 ? "Sudokus" : "Sudoku") + " gelöst" );
              progressBarSudokus.setValue( count.intValue() );
              labelTimeProgress.setText( ((now - start) / 1000.0) + " von " + threshold + " Sekunden gerechnet" );
              progressBarTime.setValue( (int)((now - start) / 1000) );
            }
          }
        } ) );
      threads.add( new Thread( new Runnable() {

        @Override
        public void run() {
          while( (running.intValue()) > 0 ) {
            int c = count.intValue();
            try {
              int time = (int)((System.currentTimeMillis() - start) / 1000);
              labelSolvingProgress.setText( c + " von " + sudokus.size() + " " + (sudokus.size() > 1 ? "Sudokus" : "Sudoku") + " gelöst" );
              progressBarSudokus.setValue( c );
              labelTimeProgress.setText( time + " von " + threshold + " Sekunden gerechnet" );
              progressBarTime.setValue( time );
              Thread.sleep( 500 );
            } catch( InterruptedException ex ) {
              Logger.getLogger( Main.class.getName() ).log( Level.SEVERE, null, ex );
            }
          }
          buttonStartComputation.setEnabled( true );
        }
      } ) );
      for( Thread thread : threads )
        thread.start();
    } else
      JOptionPane.showMessageDialog( rootPane, "Es sind keine Sudokus geladen!", "Fehler", JOptionPane.ERROR_MESSAGE );
  }

  /**
   * @param args the command line arguments
   */
  public static void main( String args[] ) {
    try {
      UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
    } catch( Exception e ) {
    }
    java.awt.EventQueue.invokeLater( new Runnable() {

      @Override
      public void run() {
        new Main().setVisible( true );
      }
    } );
  }
}
