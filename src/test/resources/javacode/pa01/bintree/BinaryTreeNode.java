package pa01.bintree;

/**
 * This class models nodes of a binary tree node which knows of their parent and up to two children. A node contains
 * data of the type T
 * @param <T> type for the data in a node
 * @author CoMaTeam
 */
public class BinaryTreeNode<T> {

  /** The data held by this node. */
  private T data;
  /** The left child of this node. */
  private BinaryTreeNode<T> leftChild;
  /** The right child of this node. */
  private BinaryTreeNode<T> rightChild;
  /** The parent of this node. */
  private BinaryTreeNode<T> parent;

  /**
   * Creates a node with the given data in it.
   * @param data the data for the node
   */
  BinaryTreeNode( T data ) {
    this.data = data;
  }

  /**
   * Returns the left child of the node.
   * @return left child
   */
  public BinaryTreeNode<T> getLeft() {
    return leftChild;
  }

  /**
   * Returns the right child of the node.
   * @return right child
   */
  public BinaryTreeNode<T> getRight() {
    return rightChild;
  }

  /**
   * Returns the parent of the node.
   * @return parent node
   */
  public BinaryTreeNode<T> getParent() {
    return parent;
  }

  /**
   * Returns the data inside the node.
   * @return stored data
   */
  public T getData() {
    return data;
  }

  /**
   * Changes the stored data of the node.
   * @param data new data
   */
  public void setData( T data ) {
    this.data = data;
  }

  /**
   * Decides if the node is a leaf, that means, it has no children.
   * @return {@code true} if the node is a leaf, {@code false} otherwise
   */
  public boolean isLeaf() {
    return leftChild == null && rightChild == null;
  }

  /**
   * Decides if the node is the tree node of a tree, or not.
   * @return {@code true} if the node is root, {@code false} otherwise
   */
  public boolean isRoot() {
    return getParent() == null;
  }

  /**
   * Returns the number of children of this node. 0 if it is a leaf.
   * @return the number of children of this node
   */
  public int numChildren() {
    if( leftChild == null && rightChild == null ) {
      return 0;
    }
    if( leftChild != null && rightChild != null ) {
      return 2;
    }
    return 1;
  }

  /**
   * Decides whether the node is the left child of the parent, or not.
   * @return {@code true} if the node is the left node of the parent, {@code false} otherwise
   */
  public boolean isLeftChild() {
    return (getParent() != null && getParent().getLeft() == this);
  }

  /**
   * Decides whether the node is the right child of the parent, or not.
   * @return {@code true} if the node is the right node of the parent, {@code false} otherwise
   */
  public boolean isRightChild() {
    return (getParent() != null && getParent().getRight() == this);
  }

  /**
   * Converts the node to a string representation.
   * @return a string representation of the node
   */
  @Override
  public String toString() {
    return (getData() != null ? getData().toString() : "*");
  }

  /**
   * Sets the left child of the node to the given node. This method will throw an exception if the given child is
   * {@code null} or there is already a left child or the new child node has already a parent.
   * @param child the left child
   */
  void setLeftChild( BinaryTreeNode<T> child ) {
    if( child == null || getLeft() != null || child.parent != null ) {
      throw new IllegalStateException( "Node can not be added as child!" );
    }
    leftChild = child;
    child.parent = this;
  }

  /**
   * Sets the right child of the node to the given node. This method will throw an exception if the given child is
   * {@code null} or there is already a right child or the new child node has already a parent.
   * @param child the right child
   */
  void setRightChild( BinaryTreeNode<T> child ) {
    if( child == null || getRight() != null || child.parent != null ) {
      throw new IllegalStateException( "Node can not be added as child!" );
    }
    rightChild = child;
    child.parent = this;
  }

  /**
   * Decides whether the node has a left child or not.
   * @return {@code true} if the node has a left child, {@code false} otherwise
   */
  public boolean hasLeftChild() {
    return leftChild != null;
  }

  /**
   * Decides whether the node has a right child or not.
   * @return {@code true} if the node has a right child, {@code false} otherwise
   */
  public boolean hasRightChild() {
    return rightChild != null;
  }

  /**
   * Deletes all references to other nodes, i.e. leftChild, rightChild and parent. A node without a parent is not
   * allowed to be decoupled because this would break a trees structure. Only leaves or nodes with a single child are
   * allowed to be decoupled. In these cases the single child will become the new child of his former gradparent.
   * @throws IllegalArgumentException if a root node or a node with two child nodes should be decoupled
   */
  void decouple() throws IllegalArgumentException {
    if( parent == null ) {
      throw new IllegalArgumentException( "Can't decouple a root-node!" );
    }
    if( leftChild != null || rightChild != null ) {
      throw new IllegalArgumentException( "Can't decouple a node with two child-nodes!" );
    }

    //parent points to his former grand child
    if( isLeftChild() ) {//node is left child
      if( hasLeftChild() ) {
        parent.leftChild = leftChild;
        leftChild.parent = parent;
      } else if( hasRightChild() ) {
        parent.leftChild = rightChild;
        rightChild.parent = parent;
      } else {
        parent.leftChild = null;
      }
    } else {//node is right child
      if( hasLeftChild() ) {
        parent.rightChild = leftChild;
        leftChild.parent = parent;
      } else if( hasRightChild() ) {
        parent.rightChild = rightChild;
        rightChild.parent = parent;
      } else {
        parent.rightChild = null;
      }
    }

    //delte every ref. from this
    leftChild = null;
    rightChild = null;
    parent = null;
    data = null; //could help the GC
  }
}
