package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import lpg.runtime.IAst;
import lpg.runtime.IToken;

/**
 * A node of an SGLR abstract syntax tree.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public abstract class SGLRAstNode<TAstNode extends SGLRAstNode>
		implements IAst, Iterable<TAstNode> {

	// Unique objects (circumvent interning)
	
	/** The constructor name for lists. */
	public static final String LIST_CONSTRUCTOR = new String("[]");

	/** The sort name for strings. */
	public static final String STRING_SORT = new String("<string>");
	
	private final ArrayList<TAstNode> children;
	
	private IToken leftToken, rightToken;
	
	private SGLRAstNode parent;
		
	// Accessors
	
	/**
	 * Returns the constructor name of this node, or null if not applicable. 
	 */
	public abstract String getConstructor();
	
	public abstract String getSort();
	
	public ArrayList<TAstNode> getChildren() { // must expose impl. type for interface 
		return children;
	}

	/** Get the leftmost token associated with this node. */
	public IToken getLeftIToken() {
		return leftToken;
	}

	/** Get the leftmost token associated with this node. */
	public IToken getRightIToken() {
		return rightToken;
	}

	/** Set the leftmost token associated with this node. */
	protected void setLeftIToken(IToken value) {
		leftToken = value;
	}

	/** Set the leftmost token associated with this node. */
	protected void setRightIToken(IToken value) {
		rightToken = value;
	}

	public SGLRAstNode getParent() {
		return parent;
	}

	void setParent(SGLRAstNode value) {
		parent = value;
	}
	
	// Initialization
	
	/**
	 * Create a new AST node and set it to be the parent node of its children.
	 */
	protected SGLRAstNode(IToken leftToken, IToken rightToken, ArrayList<TAstNode> children) {
		assert leftToken != null;
		assert rightToken != null;
		assert children != null;
		
		this.leftToken = leftToken;
		this.rightToken = rightToken;
		this.children = children;
		
		for (TAstNode node : children)
			node.setParent(this);
	}
	
	protected SGLRAstNode(IToken leftToken, IToken rightToken) {
		// Construct an empty list (unfortunately needs to be a concrete ArrayList type)
		this(leftToken, rightToken, new ArrayList<TAstNode>(0));
	}
	
	// General access
	
	public Iterator<TAstNode> iterator() {
		return getChildren().iterator();
	}

    @Override
	public String toString() {
        return getLeftIToken().getPrsStream().toString(getLeftIToken(), getRightIToken());
    }
    
    // map(getSort)
    public static List<String> getSorts(List<? extends SGLRAstNode> children) {
  	  List<String> result = new ArrayList<String>(children.size());
  	  
  	  for (SGLRAstNode node : children) {
  		  result.add(node.getSort());
  	  }
  	  
  	  return result;
    }

    /* UNDONE: Removed aterm<->ast node coupling
    @Override
	public boolean equals(Object o) {
    	if (o instanceof SGLRAstNode)
    		return getATerm().equals(((SGLRAstNode) o).getATerm());
    	else
    		return false;
    }
    
    @Override
    public int hashCode() {
    	return getATerm().hashCode();
    }
    */
	
	// LPG legacy/compatibility
	
	/**
	 * Get all children (including the null ones).
	 * 
	 * @deprecated  Unused; ATermAstNode does not include null children.
	 */
	@Deprecated
	public ArrayList<TAstNode> getAllChildren() {
		return getChildren();
	}

    public IToken[] getPrecedingAdjuncts() {
    	return getLeftIToken().getPrecedingAdjuncts();
    }
    
    public IToken[] getFollowingAdjuncts() {
    	return getRightIToken().getFollowingAdjuncts();
    }

	public SGLRAstNode getNextAst() {
		return null;
	}

}
