package org.strategoxt.imp.runtime.parser;

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
	
	private final ATermAstNode parent;
	
	private final ArrayList<ATermAstNode> children;
	
	private final IToken leftToken, rightToken;
		
	// Accessors
	
	public ATerm getATerm() {
		return aterm;
	}

	public ATermAstNode getParent() {
		return parent;
	}

	public ArrayList<ATermAstNode> getChildren() {
		return children;
	}

	public IToken getLeftIToken() {
		return leftToken;
	}

	public IToken getRightIToken() {
		return rightToken;
	}
	
	// Initialization
	
	protected ATermAstNode(ATerm aterm, ATermAstNode parent, ArrayList<ATermAstNode> children, IToken leftToken, IToken rightToken) {
		this.aterm = aterm;
		this.parent = parent;
		this.children = children;
		this.leftToken = leftToken;
		this.rightToken = rightToken;
	}
	
	/*  Naive factory method
	public static ATermAstNode wrap(ATerm term) {
		ArrayList<ATermAstNode> children = new ArrayList<ATermAstNode>();
		children.ensureCapacity(term.getChildCount());
		
		ATermAstNode result = new ATermAstNode(term, children);
		
		for (int i = 0; i < term.getChildCount(); i++) {
			ATermAstNode child = wrap((ATerm) term.getChildAt(i));
			child.parent = result;
			children.add(child);
		}
		
		return result;
	}
	*/
	
	// General access
	
	public Iterator<ATermAstNode> iterator() {
		return getChildren().iterator();
	}

    @Override
	public String toString() {
        return getLeftIToken().getPrsStream().toString(getLeftIToken(), getRightIToken());
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
