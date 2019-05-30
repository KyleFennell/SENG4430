package tower;

import java.util.Stack;
import java.awt.Color;
import java.awt.Graphics;

/**
 * Tower.java class represents one tower.
 * Stack
 * @author Jens Schulz, Jan-Philipp Kappmeier
 */
public class Tower {
	/** A stack containing the disks. */
	private final Stack<Disk> disks;

	/** The eight of the tower. */
	private final int maxHeight;

	/**
	 * Creates a new tower as a private stack, set height to the max number of
	 * disks.
	 * @param maxHeight maximum possible height of one tower
	 */
	public Tower( int maxHeight ) {
		disks = new Stack<Disk>();
		this.maxHeight = maxHeight;
	}

	/**
	 * Creates a tower that is a copy of a given tower.
	 * @param t the original tower
	 */
	public Tower( Tower t ) {
		this.disks = new Stack<Disk>();
		for( Disk disk : t.disks )
			this.disks.add( disk );
		this.maxHeight = t.maxHeight;
	}

	/**
	 * Returns the available height of this stack i.e. the maximum number of disks
	 * that can be stored.
	 * @return the maximum number of disks on the tower.
	 */
	public int getMaxHeight() {
		return this.maxHeight;
	}

	/**
	 * Returns whether this tower is isEmpty, or not.
	 * @return {@code true} if the tower is isEmpty {@code false} otherwise
	 */
	public boolean isEmpty() {
		return disks.empty();
	}

	/**
	 * Adds disk to the top of the tower.
	 * @param disk Disk to be added
	 */
	void addDisk( Disk disk ) {
		if( !disks.isEmpty() && disk.getDiskSize() > disks.peek().getDiskSize() )
			throw new IllegalArgumentException( "Disk size do not fit!" );
		disks.push( disk );
	}

	/**
	 * Returns the next disk without removing it from the tower, or {@cod null}
 if tower is isEmpty.
	 * @return the top disk of the tower
	 */
	public Disk peek() {
		if( disks.empty() )
			return null;
		else
			return disks.peek();
	}

	/**
	 * Removes the disk on top of the tower, returns {@code null} if tower is isEmpty
	 * @return the top disk of the tower
	 */
	public Disk removeDisk() {
		if( disks.empty() )
			return null;
		else
			return disks.pop();
	}

	/**
	 * Draws the tower. (x,y) is the lower left corner.
	 * @param g graphic to draw on
	 * @param x x-coordinate of the lower left corner
	 * @param y y-coordinate of the lower left corner
	 */
	public void draw( Graphics g, int x, int y ) {
		int height = TowerUtil.DISK_HEIGHT * this.maxHeight * TowerUtil.MAX_TOWERS;
		g.setColor( Color.gray );
		g.fillRect( x, y - height - 2, TowerUtil.TOWER_WIDTH, height + 2 );

		// draw disks
		if( !disks.empty() ) {
			// create a local stack of disks in reverse order
			Stack<Disk> localstack = new Stack<Disk>();
			while( !disks.empty() )
				localstack.push( disks.pop() );

			// draw the disks
			int curheight = 1;
			int diskX = x + TowerUtil.TOWER_WIDTH / 2;
			while( !localstack.empty() ) {
				Disk disk = localstack.pop();

				int diskY = y - TowerUtil.DISK_HEIGHT * curheight;
				disk.draw( g, diskX, diskY );

				// put disk back on global stack
				disks.push( disk );
				curheight++;
			}
		}
	}
}
