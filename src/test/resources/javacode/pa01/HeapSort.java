package pa01;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A simple heap sort implementation.
 * @author CoMaTeam
 */
public class HeapSort {

  private final static int SEED = 21007;
  private final static int SAMPLE_SIZE = 5000;
  private final static double NANO_TO_SECS = 1000000000.;
  private PriorityQueue<Integer> heap;

  /**
   * Initializes a new heap sort with an empty priority queue.
   * @throws IllegalArgumentException if the priority queue is not empty.
   * @param heap the priority queue.
   */
  public HeapSort( PriorityQueue<Integer> heap ) throws IllegalArgumentException {
    if( !heap.isEmpty() ) {
      throw new IllegalArgumentException( "Heap is not empty!" );
    }
    this.heap = heap;
  }

  /**
   * Generates {@code n} random numbers and inserts to the priority queue.
   *
   * @param n the number of generated values
   * @return a list of {@code n} randomly generated numbers
   */
  public List<Integer> build( int n ) {
    Random r = new Random( SEED );
    ArrayList<Integer> inputList = new ArrayList<>( n );
    for( int i = 1; i <= n; ++i ) {
      int rnd = r.nextInt( n );
      heap.insert( Integer.valueOf( rnd ) );
      inputList.add( rnd );
    }
    return inputList;
  }

  /**
   * Sorts the data generated by {@link #build(int) } and returns the sorted list.
   *
   * @return the sorted list of numbers
   */
  public List<Integer> sort() {
    ArrayList<Integer> res = new ArrayList<>();
    while( !heap.isEmpty() ) {
      res.add( heap.extractMin() );
    }
    return res;
  }

  /**
   * Testing method for {@code HeapSort}. Creates a heap, generates some random numbers and sorts them using heap sort.
   * The runtime is measured.
   * @param arguments the arguments are ignored
   */
  public static void main( String[] arguments ) {
    DefaultComparator<Integer> comparator = new DefaultComparator<>();

    // TODO: create instance of your own heap.
    PriorityQueue<Integer> heap = null;

    HeapSort heapSortExample = new HeapSort( heap );

    System.out.println( "In HeapSort.java: change value of variable 'n' for larger runs" );
    int n = SAMPLE_SIZE;

    List<Integer> inputList = heapSortExample.build( n );

    System.out.println( "Sorting..." );
    long start = System.nanoTime();
    List<Integer> res = heapSortExample.sort();
    long end = System.nanoTime();

    System.out.println( (end - start) / NANO_TO_SECS + "s" );

    // Compare the results with the internal sorts result. They should be equal.
    System.out.println();
    Collections.sort( inputList );
    System.out.println( res.equals( inputList ) );
  }
}