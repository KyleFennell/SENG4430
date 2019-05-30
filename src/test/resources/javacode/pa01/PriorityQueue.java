package pa01;

import java.util.NoSuchElementException;

/**
 * A priority queue is a datastructure that allows to extract the minimum (or maximum) element. It supports insertion of
 * new elements and the extraction of the minimum element (which includes its removal).
 *
 * @param <T> the type of data that can be stored in the priority queue
 * @author CoMaTeam
 */
public interface PriorityQueue<T> {

  /**
   * Inserts new data into the priority queue.
   *
   * @param data the new data
   */
  void insert( T data );

  /**
   * Returns the minimum element of the queue without removing it.
   *
   * @return the minimum element of the queue
   * @throws NoSuchElementException if the heap is empty.
   */
  T peek() throws NoSuchElementException;

  /**
   * Returns the minimum element of the queue and removes it afterwards.
   *
   * @return the minimum element of the queue
   * @throws NoSuchElementException if the heap is empty.
   */
  T extractMin() throws NoSuchElementException;

  /**
   * Reduces the value of an {@code element} in the queue.
   *
   * @param element the element whose value has decreased
   */
  void decreaseKey( T element );

  /**
   * Decides whether the heap is empty, or not. {@code true} is returned if and only if {@link #peek() } returns an
   * object.
   *
   * @return {@code true} if the heap contains no element, {@code false} otherwise
   */
  boolean isEmpty();
}
