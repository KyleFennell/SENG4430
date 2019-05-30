package graph;

public class Queue<E> { 
    
    /**
  * constructs the element of the SimpleLinkedList Queue
  */
  
   private final SimpleLinkedList <E> queue;
    
    public Queue(){
        queue=new SimpleLinkedList<E>();
    }
    /**
  * return the datum of the new element, which is added on the end of the list
  */
  
    public E enqueue (E element){
        E datum =queue.add (element); 
        return datum;
    }
    
  /**
  * return the datum of the element, which is removed on the start of the list
  */
    
    public E dequeue (){
        E datum=queue.removeFirst();
        return datum;
    }
    
  /**
  * return true if the queue is empty
  */
    
    public boolean isEmpty(){
        return queue.isEmpty();
    }
}
