package pa01;

import pa01.bintree.BinaryTree;
import pa01.bintree.BinaryTreeNode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * A {@code PriorityQueue} represented as a binary heap in a tree. The current implementation is a min heap.
 *
 * @author Jan-Philipp Kappmeier
 * @param <T> the data of the heap
 */
public class MinTreeHeap<T> implements PriorityQueue<T> {

  /** The actual heap. */
  private final BinaryTree<T> heap;
  
  /** The last Node, which was inserted. */
  private BinaryTreeNode<T> last;
  
  /** The Comparator that is used to compare the values of the nodes. */
  private Comparator<T> comparator;

  public MinTreeHeap(T t){
	  this.comparator = new DefaultComparator<t>();
  }
  
  /**j
   * Checks the heap property for all nodes in the heap.
   * @return {@code true} if the heap property is valid
   */
  private boolean checkHeapProperty() {
    return heap.getRoot() == null ? true : checkHeapProperty( heap.getRoot() );
  }

  /**
   * Checks the heap property recursively for a heap with the given root. Notice that this method should not be called
   * in final versions as the runtime is O({@code n}) if {@code n} is the number of nodes.
   * @param node the node where the check is started
   * @return {@code true} if the subtree starting in {@code node} fulfills the heap property, {@code false} otherwise
   */
  private boolean checkHeapProperty( BinaryTreeNode<T> node ) {
    boolean ret = true;
    // check heap condition for the left child and recursively for the subtree at the left child
    if( node.getLeft() != null ) {
      ret = ret && comparator.compare( node.getLeft().getData(), node.getData() ) >= 0;
      ret = ret && checkHeapProperty( node.getLeft() );
    }
    // check heap condition for the right child and recursiveley for the subtree at the right child
    if( node.getRight() != null ) {
      ret = ret && comparator.compare( node.getRight().getData(), node.getData() ) >= 0;
      ret = ret && checkHeapProperty( node.getRight() );
    }

    if( !ret ) {
      System.err.println( "Heap property is violated at node " + node );
    }
    return ret;
  }

  /**
   * Checks if the tree has only logarithmic height.
   * @return {@code true} if it has logarithmic height, {@code false} otherwise
   */
  protected boolean checkLogHeight() {
    final int intBits = 31;
    final int correctHeight = intBits - Integer.numberOfLeadingZeros( heap.size() ); // efficient logarithm rounded down
    final int actualHeight = heap.getHeight();
    return heap.isEmpty() ? heap.getHeight() == -1 : correctHeight == actualHeight;
  }

  /**
   * Returns the string representation of the heap.
   * @return the string representation of the heap
   */
  @Override
  public String toString() {
    return heap.toString();
  }
}
