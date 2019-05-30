package graph;

/**
 * Defines basic methods for a sort algorithm that can stop after some steps.
 */
public interface Sorter {

	/**
	 * Sets the upper bound for the sort steps.
	 * @param i the number of sort steps
	 */
	public void setUpTo( int i );

	/**
	 * Sets an array of numbers that is to be sorted
	 * @param numbers numbers to be sorted
	 */
	public void setNumbers( int[] numbers );

	/**
	 * Sorts the array.
	 */
	public void sort();

	/**
	 * Returns the name of the sort algorithm.
	 * @return the name of the sort algorithm
	 */
	public String getName();

	/**
	 * Returns the number of swaps performed by the algorithm.
	 * @return the number of swaps performed by the algorithm
	 */
	public int getSwaps();

}
