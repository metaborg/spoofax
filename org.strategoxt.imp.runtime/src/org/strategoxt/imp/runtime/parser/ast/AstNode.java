package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;

import lpg.runtime.IAst;
import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;

/**
 * A node of an SGLR abstract syntax tree.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class AstNode implements IAst, Iterable<AstNode> {
	// Globally unique objects (circumvent interning)
	
	/** The constructor name for lists. */
	public static final String LIST_CONSTRUCTOR = new String("[]");

	/** The sort name for strings. */
	public static final String STRING_SORT = new String("<string>");
	
	static final ArrayList<AstNode> EMPTY_LIST = new ArrayList<AstNode>(0);
	
	private final ArrayList<AstNode> children;
	
	private final String constructor, sort;
	
	private IToken leftToken, rightToken;
	
	private AstNode parent;
		
	// Accessors
	
	/**
	 * Returns the constructor name of this node, or null if not applicable. 
	 */
	public String getConstructor() {
		return constructor;
	}
	
	public String getSort() {
		return sort;
	}
	
	// must expose impl. type for interface
	// using a bounded type to give it read-only semantics
	public final ArrayList<? extends AstNode> getChildren() {
		assert EMPTY_LIST.size() == 0;
		
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

	public AstNode getParent() {
		return parent;
	}

	private void setParent(AstNode value) {
		parent = value;
	}
	
	// Initialization
	
	/**
	 * Create a new AST node and set it to be the parent node of its children.
	 */
	public AstNode(String sort, String constructor, IToken leftToken, IToken rightToken,
			ArrayList<AstNode> children) {
		
		assert leftToken != null;
		assert rightToken != null;
		assert children != null;
		
		this.constructor = constructor;
		this.sort = sort;
		this.leftToken = leftToken;
		this.rightToken = rightToken;
		this.children = children;
		
		setReferences(leftToken, rightToken, children);
	}

	private void setReferences(IToken leftToken, IToken rightToken, ArrayList<AstNode> children) {
		IPrsStream parseStream = leftToken.getPrsStream();
		int length = rightToken.getTokenIndex();
		
		for (int i = leftToken.getTokenIndex(); i < length; i++) {
			SGLRToken token = (SGLRToken) parseStream.getTokenAt(i);
			if (token.getAstNode() == null) token.setAstNode(this);
		}
		
		for (AstNode node : children) {
			node.setParent(this);
		}
	}
	
	// General access
	
	public Iterator<AstNode> iterator() {
		return children.iterator();
	}

    @Override
	public String toString() {
        return getLeftIToken().getPrsStream().toString(getLeftIToken(), getRightIToken());
    }
    
    // map(getSort)
    public static List<String> getSorts(List<? extends AstNode> children) {
  	  List<String> result = new ArrayList<String>(children.size());
  	  
  	  for (AstNode node : children) {
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
    
    // Visitor support
    
    public void accept(AbstractVisitor visitor) {
    	if (visitor.preVisit(this)) {
    		enter(visitor);
    		visitor.postVisit(this);
    	}
    }
    
    protected void enter(AbstractVisitor visitor) {
    	if (visitor.visit(this)) {
    		int size = children.size();
    		
    		for (int i = 0; i < size; i++)
    			children.get(i).accept(visitor);
    	}
    	
    	visitor.endVisit(this);
    }
	
	// LPG legacy/compatibility
	
	/**
	 * Get all children (including the null ones).
	 * 
	 * @deprecated  Unused; ATermAstNode does not include null children.
	 */
	@Deprecated
	public ArrayList<? extends AstNode> getAllChildren() {
		return getChildren();
	}

    public IToken[] getPrecedingAdjuncts() {
    	return getLeftIToken().getPrecedingAdjuncts();
    }
    
    public IToken[] getFollowingAdjuncts() {
    	return getRightIToken().getFollowingAdjuncts();
    }

	public AstNode getNextAst() {
		return null;
	}

}
