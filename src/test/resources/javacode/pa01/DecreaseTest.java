package pa01;

/**
 * Simple test case for decrease key operation.
 * @author Jan-Philipp Kappmeier
 */
public class DecreaseTest {

  /** Index for first decrease test. */
  final static int TEST_CASE_1 = 6;
  /** Index for second decrease test. */
  final static int TEST_CASE_2 = 3;
  /** Index for third decrease test. */
  final static int TEST_CASE_3 = 4;

  /** Utility class constructor. */
  private DecreaseTest() { }

  /**
   * Generates a heap instance and tests several decrease key operations.
   * @param args ignored arguments.
   */
  public static void main( String... args ) {
    MinTreeHeap<Value> heap = new MinTreeHeap<>( new DefaultComparator<Value>() );

    final int size = 10;

    Value[] elements = new Value[size];

    System.out.println( "Loading elements from 0 to " + size + " to the heap." );
    for( int i = 0; i < size; ++i ) {
      elements[i] = new Value( i );
      heap.insert( elements[i] );
    }
    if( heap.extractMin().value != 0 ) {
      System.err.println( "Error! Smallest element at beginning thould be 0." );
    }
    if( heap.peek().value != 1 ) {
      System.err.println( "Error! After removing 0 smallest should be 1." );
    }

    elements[TEST_CASE_1].setValue( -1 );
    heap.decreaseKey( elements[TEST_CASE_1] );
    if( heap.extractMin().value != -1 ) {
      System.err.println( "Error! Element decreased to -1 should be at the root." );
    }
    if( heap.peek().value != 1 ) {
      System.err.println( "Error! After removal, again 1 should be at the root." );
    }

    elements[TEST_CASE_2].setValue( 2 );
    heap.decreaseKey( elements[TEST_CASE_2] );
    if( heap.extractMin().value != 1 ) {
      System.err.println( "Error! Decrease to 2 should not change root value." );
    }
    if( heap.peek().value != 2 ) {
      System.err.println( "Error! Removal of 1 should leave 2 as smallest element." );
    }

    if( heap.extractMin().value != 2 ) {
      System.err.println( "Error! Peek and extractMin should return the same elements." );
    }
    if( heap.peek().value != 2 ) {
      System.err.println( "Error! Double elements with value 2. Value at root should remain the same." );
    }
    elements[TEST_CASE_3].setValue( elements[TEST_CASE_3].getValue() - 1 );
    heap.decreaseKey( elements[TEST_CASE_3] );
    if( heap.peek().value != 2 ) {
      System.err.println( "Error! Decreasing should not change root element." );
    }
    elements[TEST_CASE_3].setValue( 2 );
    heap.decreaseKey( elements[TEST_CASE_3] );
    if( heap.peek().value != 2 ) {
      System.err.println( "Error! Decrease to same value should not change root alue." );
    }
    elements[TEST_CASE_3].setValue( 1 );
    heap.decreaseKey( elements[TEST_CASE_3] );
    if( heap.extractMin().value != 1 ) {
      System.err.println( "Error! Decreased to 1 should be smallest." );
    }

    if( heap.extractMin().value != 2 ) {
      System.err.println( "Error! Again, value 2 element should be in root." );
    }

    System.out.println( "Done." );

    System.out.println( "Remaining heap should contain elements 5, 7, 8, 9." );

    System.out.println( heap.toString() );
  }

  /**
   * Private class for the test case containing a value used for comparison.
   */
  private static class Value implements Comparable<Value> {
    /** The value used in the heap test. */
    private int value;

    /**
     * Initializes the class with a given value.
     * @param value the initial value
     */
    Value( int value ) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    public void setValue( int value ) {
      this.value = value;
    }

    @Override
    public int compareTo( Value o ) {
      return value - o.value;
    }

    @Override
    public String toString() {
      return Integer.toString( value );
    }
  }
}
