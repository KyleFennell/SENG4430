package Fields;
import java.util.NoSuchElementException;



/**
 * <p>A simple doubly linked list impelementation. Provides the possibility to
 * iterate through the list. During iteration it is possible to insert elements
 * at the current iterator position. It is always possible to insert and remove
 * new elements at the front and end.</p>
 * <p>This implementation does not take {@code null} as a valid element.</p>
 * @author Jan-Philipp Kappmeier
 */
public class SimpleLinkedList<T> {
  /** A reference to the last element of the list (or tail). */
  private ListNode last;
  /** A pointer to the element at the head of the list (or first). */
  private ListNode head;
  /** A pointer to the current element in an iteration. */
  private ListNode current;
  
  /**
   * Initializes a new instance of {@code SimpleLinkedList}.
   */
  public SimpleLinkedList() {
  }
  
  /**
   * Resets the iterator reference such that it points to the head again to
   * start a new iteration through all elements of the list.
   */
  public void reset() {
      current = head;
  }
  
  /**
   * Decides whether the current reference for an iteration can advance, or not.
   * @return {@code true} if the pointer can advance, {@code false} otherwise.
   */
  public boolean isValid() {
      return !isEmpty() && current != null;
  }
  
  /**
   * Advances the current reference for an iteration to the next element, if
   * possible. This is exactly possible if {@link #isValid() } returns {@code true}.
   * @throws IllegalStateException if the reference points to the last element of the list
   */
  public void advance() throws IllegalStateException {
      if( isValid() )
      current = current.next;
      else
      throw new IllegalStateException( "No next element!" );
  }
  
  /**
   * Returns the element referenced currently in an iteration.
   * @return the element referenced currently in an iteration, or {@code null}, if the list is empty.
   */
  public T getCurrent() {
      if( current != null )
      return current.value;
      else
      return null;
  }
  
  /**
   * Adds a new element after the hitherto last element.  For convenience,
   * returns the value that is added.
   * @param value the element that is added
   * @return the element that was added.
   */
  public T add( T value ) {
      if( isEmpty() ) {
        last = new ListNode( value );
        head = last;
        reset();
      } else {
        ListNode newNode = new ListNode( value, last );
        last = newNode;
      }
      return last.value;
  }
  
  /**
   * Adds a new element before the hitherto first/head element. For convenience,
   * returns the value that is added.
   * @param value the element that is added
   * @return the element that was added.
   */
  public T addFirst( T value ) {
      if( isEmpty() ) {
        last = new ListNode( value );
        head = last;
        reset();
      } else {
        ListNode newNode = new ListNode( value );
        newNode.next = head;
        head.prev = newNode;
        head = newNode;
      }
      return head.value;
  }
  
  /**
   * Inserts a new element before the element referenced currently in the
   * iteration. For convenience, returns the value that is added.
   * @param value the element that is inserted
   * @return the element that was added.
   * @throws IllegalArgumentException if the list is empty
   */
  public T insertBefore( T value ) throws IllegalArgumentException {
      if( isEmpty() )
      throw new IllegalStateException( "Empty list! Cannot insert before current element!" );
      if( current == head ) // current points to the first element
      return addFirst( value );
      else {
        new ListNode( value, current.prev, current );
        return value;
      }
  }
  
  /**
   * Removes the last element of the list. If the current element for iterations
   * is removed, {@link #reset()} is called to reset the iteration reference.
   * @return the element that was removed
   * @throws NoSuchElementException if the list is empty.
   */
  public T remove() throws NoSuchElementException {
      if( last == null )
      throw new NoSuchElementException( "List is empty!" );
      T ret = last.value;
    
      if( head == last ) { // the only element in the list
        head = null;
        last = null;
        current = null;
      } else {
        boolean removeCurrent = last == current;
        last = last.prev;
        last.next = null;
        if( removeCurrent )
          reset();
      }
      return ret;
  }
  
  /**
   * Removes the first/head element of the list. If the current element for
   * iterations is removed, {@link #reset()} is called to reset the iteration
   * reference.
   * @return the element that was removed
   * @throws NoSuchElementException if the list is empty.
   */
  public T removeFirst() throws NoSuchElementException {
      if( head == null )
      throw new NoSuchElementException( "List is empty!" );
      T ret = head.value;
      if( head == last ) { // remove the only element in the list
        head = null;
        last = null;
        current = null;
      } else {
        boolean removeCurrent = head == current;
        head = head.next;
        head.prev = null;
        if( removeCurrent )
          reset();
      }
      return ret;
  }
  
  /**
   * Checks whether the list is empty, or not.
   * @return {@code true} if the list contains no data, {@code false} otherwise
   */
  public boolean isEmpty() {
      return last == null;
  }
  
  /**
   * A string representation of the list consisting of a comma separated list
   * of the string representation of the elements.
   * @return a string representation of the list.
   */
  @Override
  public String toString() {
      String s = "[";
      if( last == null )
      return s + "]";
      ListNode temp = head;
      while( temp != null ) {
        s = s + temp.value.toString() + ", ";
        temp = temp.next;
      }
      return s.substring( 0, s.length() - 2 ) + "]";
  }
  
  /**
   * The nodes in the list containing references to successors and predecessors
   * and the data.
   */
  private class ListNode {
      /** The data stored in the node. */
      private final T value;
      /** Reference to the next node. */
      ListNode next;
      /** Reference to the previous node. */
      ListNode prev;
    
      /**
     * Initializes a new list node without predecessors and successors.
     * @param value the data
     */
      private ListNode( T value ) {
        this.value = requireNonNull( value );
      }
    
      /**
     * Initializes a new node with a predecessor. The pointers of the
     * predecessor are updated.
     * @param value the data
     * @param previous reference to the predecessor
     */
      private ListNode( T value, ListNode previous ) {
        this.value = requireNonNull( value );
        prev = previous;
        previous.next = this;
      }
    
      /**
     * Initializes a new node with a predecessor and successor. The references
     * of the predecessor and successor are updated.
     * @param value the data
     * @param previous reference to the predecessor
     * @param next reference to the successor
     */
      private ListNode( T value, ListNode previous, ListNode next ) {
        this.value = requireNonNull( value );
        this.prev = previous;
        this.next = next;
        next.prev = this;
        prev.next = this;
      }
    
      /**
     * The string representation of the inner node returns the representation of
     * the data.
     * @return string representation of the data.
     */
      @Override
      public String toString() {
        return value.toString();
      }
  }
  
  /**
   * <p>
   * Convenience Method that exists in Java 7.</p>
   * <p>
   * Checks that the specified object reference is not {@code null}. This method
   * is designed primarily for doing parameter validation in methods and
   * constructors, as demonstrated below:</p>
   * <blockquote><pre>
   * public Foo(Bar bar) {
   *     this.bar = Objects.requireNonNull(bar);
   * }
   * </pre></blockquote>
   *
   * @param obj the object reference to check for nullity
   * @param <T> the type of the reference
   * @return {@code obj} if not {@code null}
   * @throws NullPointerException if {@code obj} is {@code null}
   */
  public static <T> T requireNonNull( T obj ) {
      if( obj == null )
      throw new NullPointerException();
      return obj;
  }
  
  public T getMedian(){
    
    ListNode a=this.head;
    ListNode b=this.last;
    
    while(a!=b){
      b=b.prev;
      if (a==b)
      return a.value;
      a=a.next;
      
    }
    return a.value;
  }
  
  public void duplicate(){
    reset();
    while(current.next!=null){
      ListNode dup = new ListNode(current.value,current,current.next);
      current.next.prev=dup;
      current.next=dup;
      advance();
      advance();
    }
    ListNode dup= new ListNode(current.value,current);
    current.next=dup;
    return;
  }
  
  
  
}