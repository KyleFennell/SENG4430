package tower;

import java.awt.Color;
import java.util.Random;

/**
 * TowerUtil.java
 * @author Jens Schulz, Jan-Philipp Kappmeier
 */
public final class TowerUtil {


	/** The height in pixel for a disk. */
	public static final int DISK_HEIGHT = 20;
	/** The indentation step for disks of smaller size. */
	public static final int DISK_WIDTH_STEP = 20;
	/** Width in pixels of the arc for the rounded border. */
	public static final int ARC_WIDTH = 5;
	/** Height in pixels of the arc for the rounded border. */
	public static final int ARC_HEIGHT = 5;

	/** Colors used for tower 1. */
	public static final Color[] DISK_COLORS_TOWER_1;
	/** Colors used for tower 2. */
	public static final Color[] DISK_COLORS_TOWER_2;

	/** The number of possible tower spaces on the panel. */
	public static final int NUM_SPACES = 3;

	/** The minimum number of disks on a tower (when starting). */
	public static final int MIN_DISKS = 2;
	/** The maximum number of disks on a tower (when starting). */
	public static final int MAX_DISKS = 10;
	/** The maximum number of towers on the available spaces. */
	public static final int MAX_TOWERS = 2;

	/** The width of a tower in pixels. */
	public static final int TOWER_WIDTH = 12;

	/** Animation delay time. */
	public static final int MAX_DELAY = 300;

	/**
	 * Initialize the color tables for the two towers.
	 */
	static {
		DISK_COLORS_TOWER_1 = new Color[MAX_DISKS];
		for( int i = 0; i < MAX_DISKS; ++i )
			DISK_COLORS_TOWER_1[i] = randomVibrantColor();
		DISK_COLORS_TOWER_2 = new Color[MAX_DISKS];
		for( int i = 0; i < MAX_DISKS; ++i )
			DISK_COLORS_TOWER_2[i] = randomPastelColor();
	}

	/**
	 * Generates a vibrant color randomly. Exactly one of RGB values is set to
	 * 255 and 0. The third value is set to a random value up to 128.
	 * @return a vibrant color.
	 */
	public static Color randomVibrantColor() {
		Random r = new Random();
		Color c;
		int d = r.nextInt( 128 );
		switch( r.nextInt( 6 ) ) {
			case 0:
				return new Color( 255, 0, d );
			case 1:
				return new Color( 0, 255, d );
			case 2:
				return new Color( 255, d, 0 );
			case 3:
				return new Color( 0, d, 255 );
			case 4:
				return new Color( d, 0, 255 );
			case 5:
				return new Color( d, 255, 0 );
			default:
				throw new AssertionError();
		}
	}

	/**
	 * Generates a genetic pastel color randomly. First a random color is created
	 * and then mixed with plain white to generate the pastel tone. All RGB-values
	 * are larget than 128.
	 * @return a pastel color
	 */
	public static Color randomPastelColor() {
		final Color mix = Color.white;
		Random random = new Random();

		// mix the color
		int red = (random.nextInt( 256 ) + mix.getRed()) / 2;
		int green = (random.nextInt( 256 ) + mix.getGreen()) / 2;
		int blue = (random.nextInt( 256 ) + mix.getBlue()) / 2;

		return new Color( red, green, blue );
	}

	/**
	 * Private constructor for utility class.
	 */
	private TowerUtil() {
	}
}

