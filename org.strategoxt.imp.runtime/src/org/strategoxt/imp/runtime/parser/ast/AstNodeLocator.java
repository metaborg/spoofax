package org.strategoxt.imp.runtime.parser.ast;

import lpg.runtime.IToken;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;

/**
 * Ast locator: mapping source positions to AST nodes,
 * and providing node position information.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Karl Trygve Kalleberg <karltk add strategoxt.org>
 */
public class AstNodeLocator implements ISourcePositionLocator {

	public AstNode findNode(Object root, int startOffset, int endOffset) {
		AstNode ast = root instanceof SGLRToken
				? ((SGLRToken) root).getAstNode()
				: (AstNode) root;
		
		if (ast.getLeftIToken().getStartOffset() <= startOffset && endOffset <= ast.getRightIToken().getEndOffset()) {
		    for (AstNode child : ast.getChildren()) {
		        AstNode candidate = findNode(child, startOffset, endOffset);
		        if (candidate != null)
		            return candidate;
		    }
		    return ast;
		} else {
		    return null;
		}
	}
	
	public AstNode findNode(Object root, int offset) {
		return findNode(root, offset, offset);
	}
	
	public int getStartOffset(Object token) {
	    	if (token instanceof AstNode)
	    	    token = ((AstNode) token).getLeftIToken();
	    	    
		return ((IToken) token).getStartOffset();
	}
	
	public int getEndOffset(Object token) {
            if (token instanceof AstNode)
                token = ((AstNode) token).getRightIToken();
            
            return ((IToken) token).getEndOffset();
	}
	
	public int getLength(Object token) {
		return getEndOffset(token) - getStartOffset(token);
	}
	
	public IPath getPath(Object node) {
	   if (node instanceof SGLRToken)
            	node = ((SGLRToken) node).getAstNode();
            
            return ((IStrategoAstNode) node).getSourceInfo().getPath(); 
	}
	
}
