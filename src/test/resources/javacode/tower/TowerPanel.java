package tower;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * TowerPanel.java
 * @author Jens Schulz
 */
public class TowerPanel extends JPanel {

	/** Array of the (possibly empty) towers for each place. */
	private Tower[] towers;

	/** Total number of disks. */
	private int diskCount;

	/** Index of first tower, only valid if {@link #firstTowerSelected} is {@code true}. */
	private int firstTower;

	/** Stores if the first tower is selected. */
	private boolean firstTowerSelected;

	/**
	 * Initializes the panel and fills the first two spaces with disks.
	 * @param diskCount
	 */
	public TowerPanel( int diskCount ) {
		this.diskCount = diskCount;
		firstTowerSelected = false;

		towers = new Tower[TowerUtil.NUM_SPACES];
		for( int i = 0; i < TowerUtil.NUM_SPACES; ++i )
			towers[i] = new Tower( diskCount );

		//add all disks of first color to the first tower
		for( int i = diskCount - 1; i >= 0; --i )
			towers[0].addDisk( new Disk( i + 1, TowerUtil.DISK_COLORS_TOWER_1[i] ) );

		//add all disks of second color to the first tower
		for( int i = diskCount - 1; i >= 0; --i )
			towers[1].addDisk( new Disk( i + 1, TowerUtil.DISK_COLORS_TOWER_2[i] ) );

		setBackground( Color.white );

		addMouseListener( new MyMouseListener() );
	}

	/**
	 * Overrides the panel size such that the towers fit appropriately.
	 * @return the correct panel size for the number of tower spaces
	 */
	@Override
	public Dimension getPreferredSize() {
		// panel width = largest disk times the number of towers + 2 disk step width
		int panelWidth = TowerUtil.MAX_DISKS * TowerUtil.DISK_WIDTH_STEP * TowerUtil.NUM_SPACES + 2 * TowerUtil.DISK_WIDTH_STEP + TowerUtil.NUM_SPACES;

		int panelHeight = TowerUtil.DISK_HEIGHT * TowerUtil.MAX_DISKS * TowerUtil.MAX_TOWERS;
		panelHeight = Math.round( 1.2f * (float)panelHeight );

		return new Dimension( panelWidth, panelHeight );
	}

	/**
	 * Paints the towers places but not the disks.
	 * @param g graphics object for painting
	 */
	@Override
	public void paintComponent( Graphics g ) {
		super.paintComponent( g );
		int towerWidth = getSize().width / TowerUtil.NUM_SPACES;

		int towerX = towerWidth / 2 - TowerUtil.TOWER_WIDTH / 2;
		int towerY = (int)(0.9 * getSize().height);

		for( int i = 0; i < TowerUtil.NUM_SPACES; ++i ) {
			towers[i].draw( g, towerX, towerY );
			towerX += towerWidth;
		}
	}

	/**
	 * Set the number of towers and redraw
	 * @param nDisks number of disks
	 */
	public void setDiskCount( int nDisks ) {
		this.diskCount = nDisks;
		resetTowers();
	}

	/**
	 * Moves a disk from the first to the second tower. Assumes that a disk on the
	 * first tower exists and can be moved to the second.
	 * @param firstTower tower from where to move the disk
	 * @param secondTower tower where to move disk to
	 */
	public void moveDisk( int firstTower, int secondTower ) {
		assert towers[firstTower].peek() != null && (towers[secondTower].peek() == null || towers[firstTower].peek().getDiskSize() <= towers[secondTower].peek().getDiskSize());
		Disk disk = towers[firstTower].removeDisk();
		towers[secondTower].addDisk( disk );
	}

	/**
	 * reinitializes the towers and repaints the panel
	 */
	public void resetTowers() {
		towers = new Tower[TowerUtil.NUM_SPACES];
		for( int i = 0; i < TowerUtil.NUM_SPACES; ++i )
			towers[i] = new Tower( diskCount );

		for( int i = diskCount - 1; i >= 0; --i )
			towers[0].addDisk( new Disk( i + 1 , TowerUtil.DISK_COLORS_TOWER_1[i]) );

		for( int i = diskCount - 1; i >= 0; --i )
			towers[1].addDisk( new Disk( i + 1, TowerUtil.DISK_COLORS_TOWER_2[i] ) );

		/* Paint the panel again */
		repaint();
	}

	/**
	 * Returns one of the towers on the panel.
	 * @param i the index of the tower
	 * @return the tower at index {@code i}
	 */
	Tower getTower( int i ) {
		return towers[i];
	}

	/**
	 * if the mouse is pressed on the panel, a tower is selected; this listener
	 * handles the moving of disks TODO store moves on undo stack
	 */
	class MyMouseListener extends MouseAdapter {
		@Override
		public void mousePressed( MouseEvent e ) {
			int towerWidth = getSize().width / TowerUtil.NUM_SPACES;

			// determine chosen tower
			int selectedTower;
			int x = e.getX();

			if( x < towerWidth )
				selectedTower = 0;
			else if( x < 2 * towerWidth )
				selectedTower = 1;
			else
				selectedTower = 2;

			/* first tower to be selected */
			if( !firstTowerSelected )
				/* check whether this tower is not isEmpty */
				if( towers[selectedTower].isEmpty() )
					JOptionPane.showConfirmDialog( null, "Selected tower is empty!",
									"", JOptionPane.CLOSED_OPTION, JOptionPane.WARNING_MESSAGE );
				else {
					firstTower = selectedTower;
					firstTowerSelected = true;
					Disk disk = towers[firstTower].peek();
					disk.setSelected( true );
					repaint();
				}
			else {
				firstTowerSelected = false;

				/* check whether this is a feasible move */
				Disk disk1 = towers[firstTower].peek();
				Disk disk2 = towers[selectedTower].peek();
				disk1.setSelected( false );

				if( disk2 == null || disk1.getDiskSize() <= disk2.getDiskSize() )
					moveDisk( firstTower, selectedTower );
				else // incorrect move

					/* cannot put a larger disk on a smaller one! */
					JOptionPane.showConfirmDialog( null, "This move is not allowed!",
									"", JOptionPane.CLOSED_OPTION, JOptionPane.WARNING_MESSAGE );

				repaint();
			}
		}
	}
}
