package org.strategoxt.imp.runtime.parser.ast;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.terms.SimpleTermVisitor.tryGetListIterator;
import static org.strategoxt.imp.runtime.stratego.SourceAttachment.getResource;

import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.Tokenizer;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

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

	/**
	 * @param endOffset  The end offset (inclusive).
	 */
	public ISimpleTerm findNode(Object root, int startOffset, int endOffset) {
		ISimpleTerm ast = impObjectToAstNode(root);
		
		if (getLeftToken(ast).getStartOffset() <= startOffset
				&& (endOffset <= getRightToken(ast).getEndOffset()
						|| isPartOfListSuffixAt(ast, endOffset))) {
			Iterator<ISimpleTerm> iterator = tryGetListIterator(ast); 
			for (int i = 0, max = ast.getSubtermCount(); i < max; i++) {
				ISimpleTerm child = iterator == null ? ast.getSubterm(i) : iterator.next();
		        ISimpleTerm candidate = findNode(child, startOffset, endOffset);
		        if (candidate != null) {
		        	assert ImploderAttachment.get(candidate) != null;
		            return candidate;
		        }
		    }
			assert ImploderAttachment.get(ast) != null;
		    return ast;
		} else {
		    return null;
		}
	}

	/**
	 * Tests if an end offset is part of a list suffix
	 * (considers the layout following the list also part of the list).
	 */
	private static boolean isPartOfListSuffixAt(ISimpleTerm node, final int offset) {
		return node.isList() && offset <= Tokenizer.findRightMostLayoutToken(getRightToken(node)).getEndOffset();
	}
	
	public ISimpleTerm findNode(Object root, int offset) {
		return findNode(root, offset, offset - 1);
	}
	
	public int getStartOffset(final Object element) {
		IToken token;
		ISimpleTerm node;
		if (element instanceof IToken) {
			token = (IToken) element;
			node = token.getAstNode();
		} else {
			node = impObjectToAstNode(element);
			token = getLeftToken(node);
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
			token = (IToken) element;
		} else {
			IStrategoTerm node = impObjectToAstNode(element);
			token = getRightToken(node);
		}

		return token.getEndOffset();
	}
	
	public int getLength(Object element) {
		return getEndOffset(element) - getStartOffset(element);
	}
	
	public IPath getPath(Object element) {
		IResource resource = getResource(impObjectToAstNode(element));
		return resource == null ? null : resource.getLocation();
	}

	public static IStrategoTerm impObjectToAstNode(Object element) {
		if (element instanceof ModelTreeNode) {
			element = ((ModelTreeNode) element).getASTNode();
			if (element instanceof ModelTreeNode)
				element = ((ModelTreeNode) element).getASTNode();
		}
		if (element instanceof IToken)
			element = ((IToken) element).getAstNode();
		return (IStrategoTerm) element;
	}
	
}
