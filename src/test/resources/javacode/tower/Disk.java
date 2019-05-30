package tower;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Disk.java
 * @author Jens Schulz
 */
public class Disk implements Cloneable {
	/** Size of this disk. */
	private final int diskSize;

	/** The color of the disk. */
	private final Color diskColor;

	/** Is the disk selected? */
	private boolean isSelected;

	/**
	 * Create a disk for the given size and color.
	 * @param size size of disk
	 * @param diskColor color of the disk
	 */
	public Disk( int size, Color diskColor ) {
		diskSize = size;
		this.diskColor = diskColor;
		isSelected = false;
	}

	/**
	 * Returns whether this disk is selected, or not.
	 * @return {@code true} if the disk is selected, {@code false} otherwise
	 */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * Sets selection status of this disk.
	 * @param isSelected decides whether the disk is selected, or not
	 */
	public void setSelected( boolean isSelected ) {
		this.isSelected = isSelected;
	}

	/**
	 * Returns the size of this disk.
	 * @return the size of this disk
	 */
	public int getDiskSize() {
		return this.diskSize;
	}

	/**
	 * Returns the width of the disk to be drawn on the screen.
	 * @return the width of the disk in pixels
	 */
	int getDiskWidth() {
		return diskSize * TowerUtil.DISK_WIDTH_STEP;
	}

	/**
	 * Draws the disk.
	 * @param g1 graphics context g1
	 * @param x x-coordinate of the center of the tower
	 * @param y y-coordinate top position of the disk on the tower
	 */
	void draw( Graphics g1, int x, int y ) {
		Graphics2D g = (Graphics2D)g1;
		int width = getDiskWidth();
		int xpos = x - width / 2;
		g.setColor( diskColor );
		g.fillRoundRect( xpos, y, width, TowerUtil.DISK_HEIGHT, TowerUtil.ARC_WIDTH, TowerUtil.ARC_HEIGHT );
		g.setStroke( new BasicStroke( 2 ) );
		if( isSelected ) {
			g.setColor( Color.red );
			g.setStroke( new BasicStroke( 4 ) );
		} else
			g.setColor( Color.black );
		g.drawRoundRect( xpos, y, width, TowerUtil.DISK_HEIGHT, TowerUtil.ARC_WIDTH, TowerUtil.ARC_HEIGHT );
	}
}
