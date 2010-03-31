package org.strategoxt.imp.runtime.parser.ast;

import lpg.runtime.IToken;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
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
	
	//private final SGLRParseController controller;
	
	public AstNodeLocator(SGLRParseController controller) {
		//this.controller = controller;
	}

	public IStrategoAstNode findNode(Object root, int startOffset, int endOffset) {
		IStrategoAstNode ast = impObjectToAstNode(root);
		
		if (ast.getLeftIToken().getStartOffset() <= startOffset && endOffset <= ast.getRightIToken().getEndOffset()) {
		    for (Object child : ast.getChildren()) {
		        IStrategoAstNode candidate = findNode(child, startOffset, endOffset);
		        if (candidate != null)
		            return candidate;
		    }
		    return ast;
		} else {
		    return null;
		}
	}
	
	public IStrategoAstNode findNode(Object root, int offset) {
		return findNode(root, offset, offset);
	}
	
	public int getStartOffset(Object element) {
		SGLRToken token;
		IStrategoAstNode node;
		if (element instanceof IToken) {
			token = (SGLRToken) element;
			node = token.getAstNode();
		} else {
			node = impObjectToAstNode(element);
			token = (SGLRToken) node.getLeftIToken();
		}
		
		try {
			// UNDONE: Should return -1 if not using the same controller, per HyperLinkDetector
			// return node == null || node.getParseController() == controller
			// 	? token.getStartOffset()
			// 	: -1;
			return token.getStartOffset();
		} catch (IllegalStateException e) {
			// HACK: avoid this exception here (Spoofax/49)
			Environment.logException("Could not determine parse controller", e);
			return token.getStartOffset();
		}
	}

	public int getEndOffset(Object element) {
		IToken token;
		if (element instanceof IToken) {
			token = (SGLRToken) element;
		} else {
			AstNode node = impObjectToAstNode(element);
			token = node.getRightIToken();
		}

		return token.getEndOffset();
	}
	
	public int getLength(Object element) {
		return getEndOffset(element) - getStartOffset(element);
	}
	
	public IPath getPath(Object element) {
		IResource resource = impObjectToAstNode(element).getResource();
		return resource.getLocation();
	}

	public static AstNode impObjectToAstNode(Object element) {
		if (element instanceof ModelTreeNode) {
			element = ((ModelTreeNode) element).getASTNode();
			if (element instanceof ModelTreeNode)
				element = ((ModelTreeNode) element).getASTNode();
		}
		if (element instanceof SGLRToken)
			element = ((SGLRToken) element).getAstNode();
		return (AstNode) element;
	}
	
}
