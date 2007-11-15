package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;
import java.util.Iterator;

import aterm.ATerm;

import lpg.runtime.IAst;
import lpg.runtime.IToken;

/**
 * Wrapper class for an ATerm to implement IAst.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class ATermAstNode implements IAst, Iterable<ATermAstNode> {
	private final ATerm aterm;
	
	private final ArrayList<ATermAstNode> children;
	
	private final IToken leftToken, rightToken;
	
	private ATermAstNode parent;
		
	// Accessors
	
	public ATerm getATerm() {
		return aterm;
	}

	public ATermAstNode getParent() {
		return parent;
	}

	public void setParent(ATermAstNode value) {
		parent = value;
	}

	public ArrayList<ATermAstNode> getChildren() {
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
	
	// Initialization
	
	protected ATermAstNode(ATerm aterm, ArrayList<ATermAstNode> children,
			IToken leftToken, IToken rightToken) {
		this.aterm = aterm;
		this.children = children;
		this.leftToken = leftToken;
		this.rightToken = rightToken;
	}
	
	// General access
	
	public Iterator<ATermAstNode> iterator() {
		return getChildren().iterator();
	}

    @Override
	public String toString() {
        return getLeftIToken().getPrsStream().toString(getLeftIToken(), getRightIToken());
    }

    @Override
	public boolean equals(Object o) {
    	if (o instanceof ATermAstNode)
    		return getATerm().equals(((ATermAstNode) o).getATerm());
    	else
    		return false;
    }
    
    @Override
    public int hashCode() {
    	return getATerm().hashCode();
    }
	
	// LPG legacy/compatibility
	
	/**
	 * Get all children (including the null ones).
	 * 
	 * @deprecated  Unused; ATermAstNode does not include null children.
	 */
	@Deprecated
	public ArrayList<ATermAstNode> getAllChildren() {
		return getChildren();
	}

    public IToken[] getPrecedingAdjuncts() {
    	return getLeftIToken().getPrecedingAdjuncts();
    }
    
    public IToken[] getFollowingAdjuncts() {
    	return getRightIToken().getFollowingAdjuncts();
    }

	public ATermAstNode getNextAst() {
		return null;
	}

}
