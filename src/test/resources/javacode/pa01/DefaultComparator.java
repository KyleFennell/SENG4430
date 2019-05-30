package pa01;

import java.util.Comparator;

/**
 * A simple comparator that can compare all comparable objects.
 * @param <T> the comparable type
 */
public class DefaultComparator<T extends Comparable<T>> implements Comparator<T> {

  /**
   * Default comparator that compares arbitrary {@link Comparable} objects. {@inheritDoc }
   * @param n1 the first value to be compared.
   * @param n2 the second value to be compared.
   * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
   * than the second.
   */
  @Override
  public int compare( T n1, T n2 ) {
    return n1.compareTo( n2 );
  }
}
