package Fields;

public class Queue<E> {
  SimpleLinkedList <E> queue = new SimpleLinkedList<E>();
  
  public E enqueue (E element){
    E datum =queue.add (element);
    return datum;
  }
  
  public E dequeue (){
    E datum=queue.removeFirst();
    return datum;
  }
  
  public boolean isEmpty(){
    return queue.isEmpty();
  }
}