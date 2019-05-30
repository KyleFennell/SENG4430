package tower;

/**
 * HanoiAction.java
 * Created: 17.01.2014, 15:41:57
 */

/**
 * Stores information of a disk moved from a tower to another tower.
 * @author Jan-Philipp Kappmeier
 */
	public class DiskAction {
		final int from;
		final int to;

		/**
		 * Initializes the action with the information about the source and destination.
		 * @param from the source tower of the move
		 * @param to the destination tower of the move
		 */
		public DiskAction( int from, int to ) {
			this.from = from;
			this.to = to;
		}
	}

