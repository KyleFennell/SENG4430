package pa01.bintree;

/**
 * A binary tree with a pointer to a root node and data of type T inside each node.
 *
 * @param <T> The data in the binary tree.
 * @author CoMaTeam
 */
public class BinaryTree<T> {

  /** The root node of the tree. */
  private BinaryTreeNode<T> root;

  /**
   * Default constructor, initializes an empty tree.
   */
  public BinaryTree() {
    this.root = null;
  }

  /**
   * Constructs a tree of only one node and stores the given data in the root.
   *
   * @param rootData the given data
   */
  public BinaryTree( T rootData ) {
    root = new BinaryTreeNode<>( rootData );
  }

  /**
   * Constructs a tree with a new root node and the given trees as left and right subtrees. And the given data in the
   * root node.
   *
   * @param leftTree the left subtree
   * @param rightTree the right subtree
   * @param data the data for the new root node
   */
  public BinaryTree( BinaryTree<T> leftTree, BinaryTree<T> rightTree, T data ) {
    root = new BinaryTreeNode<>( data );
    root.setLeftChild( leftTree.getRoot() );
    root.setRightChild( rightTree.getRoot() );
  }

  /**
   * Adds a new child node as left child to the given node. The new node will contain the given data.
   *
   * @param node parent node for the new left child
   * @param data data the will be stored in the new child node
   * @return returns the newly created node
   */
  public BinaryTreeNode<T> addLeftChild( BinaryTreeNode<T> node, T data ) {
    if( node == null ) {
      throw new IllegalArgumentException( "No valid node as parent of a left child given" );
    }
    node.setLeftChild( new BinaryTreeNode<>( data ) );
    return node.getLeft();
  }

  /**
   * Adds a new child node as right child to the given node. The new node will contain the given data.
   *
   * @param node parent node for the new right child
   * @param data data the will be stored in the new child node
   * @return returns the newly created node
   */
  public BinaryTreeNode<T> addRightChild( BinaryTreeNode<T> node, T data ) {
    if( node == null ) {
      throw new IllegalArgumentException( "No valid node as parent of a left child given" );
    }
    node.setRightChild( new BinaryTreeNode<>( data ) );
    return node.getRight();
  }

  /**
   * Sets the data in the root. Only possible if the root doesn't have any data so far.
   *
   * @param data new data
   * @return returns the root itself
   */
  public BinaryTreeNode<T> setRoot( T data ) {
    if( root != null ) {
      throw new IllegalStateException( "Root exists already" );
    }

    this.root = new BinaryTreeNode<>( data );
    return root;
  }

  /**
   * Decides whether the tree is empty, or not.
   *
   * @return {@code true} if the tree is empty, {@code false} otherwise
   */
  public boolean isEmpty() {
    return root == null;
  }

  /**
   * Returns the root node of the tree.
   *
   * @return the root node of the tree
   */
  public BinaryTreeNode<T> getRoot() {
    return root;
  }

  /**
   * Removes a node from the tree. Only leaves or nodes with a single child can be removed.
   *
   * @param node node to remove
   */
  public void remove( BinaryTreeNode<T> node ) {
    if( node == null ) {
      throw new IllegalArgumentException( "Cannot remove null-node." );
    }
    //treat root differntly - only deletable iff no children
    if( node == root && null == node.getLeft() && null == node.getRight() ) {
      root.setData( null );
      root = null;
      return;
    }

    node.decouple();
  }

  /**
   * Recursive method that returns the size of the subtree at a given node.
   *
   * @param startNode the root of the subtree whose size is computed
   * @return the size of the subtree
   */
  private int sizeOfSubTree( BinaryTreeNode<T> startNode ) {
    final int l = startNode.getLeft() == null ? 0 : sizeOfSubTree( startNode.getLeft() );
    final int r = startNode.getRight() == null ? 0 : sizeOfSubTree( startNode.getRight() );
    return l + r + 1;
  }

  /**
   * Returns the height of the tree. The runtime is in {@code O(#nodes)}.
   *
   * @return the height of the tree
   */
  public int getHeight() {
    return root == null ? -1 : height( root, 0 );
  }

  /**
   * Recursive method that computes the size of a subtree at a given node. The information at which level the node is is
   * needed. The runtime is in {@code O(#nodes in subtree)}.
   *
   * @param startNode the node from where the height is started
   * @param level the level on which the node resides
   * @return the maximal level of a node in the subtree starting in {@code root} with respect to the level
   */
  private int height( BinaryTreeNode<T> startNode, int level ) {
    final int l = startNode.getLeft() == null ? 0 : height( startNode.getLeft(), level + 1 );
    final int r = startNode.getRight() == null ? 0 : height( startNode.getRight(), level + 1 );
    return l == 0 && r == 0 ? level : Math.max( l, r );
  }

  /**
   * Calculates the number of nodes in this tree. This is done recursively, starting from the root. Therefore, the
   * complexity is linear in the number of nodes.
   *
   * @return number of nodes
   */
  public int size() {
    return size( root );
  }

  /**
   * Calculates the number of nodes in the subtree of a given node in a recursive manner.
   *
   * @param node root node of the subtree
   * @return number of nodes in the subtree
   */
  private int size( BinaryTreeNode<T> node ) {
    if( node == null ) {
      return 0;
    }
    int n = 1;
    if( node.hasLeftChild() ) {
      n += size( node.getLeft() );
    }
    if( node.hasRightChild() ) {
      n += size( node.getRight() );
    }

    return n;
  }

  /**
   * Recursively traverses the tree, appends output of level to given StringBuffer.
   *
   * @param strbuf StringBuffer to append to
   * @param level current level in tree (for indentation)
   * @param node current tree node
   */
  private void tree2string( StringBuffer strbuf, int level, BinaryTreeNode<T> node ) {
    if( node.getRight() != null ) {
      tree2string( strbuf, level + 1, node.getRight() );
    }

    for( int i = 0; i < level; ++i ) {
      strbuf.append( "    " );
    }
    if( !node.isRoot() ) {
      strbuf.append( node.isLeftChild() ? "/" : "\\" );
    }
    strbuf.append( node ).append( '\n' );

    if( node.getLeft() != null ) {
      tree2string( strbuf, level + 1, node.getLeft() );
    }
  }

  /**
   * Checks weather the tree has valid node structure, i.e. each child knows its real parent node.
   *
   * @return {@code true} iff the linking is correct
   */
  public boolean checkTree() {
    return checkTree( root );
  }

  /**
   * Recursiveley checks the tree structure in a sub tree. It is checked that parent nodes point to the child nodes and
   * vice versa.
   * @param node the start node
   * @return {@code true} if the subtree of {@code node} forms a correct tree
   */
  private boolean checkTree( BinaryTreeNode<T> node ) {
    if( null == node ) {
      return true;
    }
    if( node.hasLeftChild() && node.getLeft().getParent() != node ) {
      return false;
    }
    if( node.hasRightChild() && node.getRight().getParent() != node ) {
      return false;
    }

    return checkTree( node.getRight() ) && checkTree( node.getLeft() );
  }

  /**
   * Returns a string representation of the tree.
   *
   * @return string representation of tree
   */
  @Override
  public String toString() {
    StringBuffer strbuf = new StringBuffer( this.getClass() + ": " );

    if( isEmpty() ) {
      strbuf.append( "EMPTY\n" );
    } else {
      strbuf.append( '\n' );
      tree2string( strbuf, 0, getRoot() );
    }

    return strbuf.toString();
  }
}
