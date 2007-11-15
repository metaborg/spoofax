package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;
import java.util.Iterator;

import lpg.runtime.IAst;
import lpg.runtime.IToken;

/**
 * A node of an SGLR abstract syntax tree.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class SGLRAstNode implements IAst, Iterable<SGLRAstNode> {
	/** The constructor name for lists. */
	public static final String LIST_CONSTRUCTOR = "[]";
	
	private final String constructor;
	
	private final ArrayList<SGLRAstNode> children;
	
	private final IToken leftToken, rightToken;
	
	private SGLRAstNode parent;
		
	// Accessors	
	public String getConstructor() {
		return constructor;
	}
	
	public ArrayList<SGLRAstNode> getChildren() {
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

	public SGLRAstNode getParent() {
		return parent;
	}

	void setParent(SGLRAstNode value) {
		parent = value;
	}
	
	// Initialization
	
	protected SGLRAstNode(String constructor, IToken leftToken, IToken rightToken,
			ArrayList<SGLRAstNode> children) {
		this.constructor = constructor;
		this.leftToken = leftToken;
		this.rightToken = rightToken;
		this.children = children;
	}
	
	protected SGLRAstNode(String constructor, IToken leftToken, IToken rightToken) {
		// Construct an empty list (unfortunately needs to be a concrete ArrayList type)
		this(constructor, leftToken, rightToken, new ArrayList<SGLRAstNode>(0));
	}
	
	// General access
	
	public Iterator<SGLRAstNode> iterator() {
		return getChildren().iterator();
	}

    @Override
	public String toString() {
        return getLeftIToken().getPrsStream().toString(getLeftIToken(), getRightIToken());
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
	public ArrayList<SGLRAstNode> getAllChildren() {
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
