package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import lpg.runtime.IToken;

/**
 * Default ATermAstNode factory.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class AstNodeFactory {
	/**
	 * Create a new non-terminal AST node (or a terminal with only a constructor).
	 */
	public AstNode createNonTerminal(String sort, String constructor, IToken leftToken, IToken rightToken,
			ArrayList<AstNode> children) {
		
		return new AstNode(sort, leftToken, rightToken, constructor, children);
	}
	
	/**
	 * Create a new terminal AST node for an int value.
	 */
	public IntAstNode createIntTerminal(String sort, IToken token, int value) {
		return new IntAstNode(sort, token, token, value);
	}
	
	/**
	 * Create a new terminal AST node for a string token.
	 */
	public final StringAstNode createStringTerminal(String value, String sort, IToken token) {
		return new StringAstNode(value, sort, token, token);
	}
	
	/**
	 * Create a new AST node list. 
	 */
	public AstNode createList(String elementSort, IToken leftToken, IToken rightToken, ArrayList<AstNode> children) {
		// Flatten lists
		// FIXME: Don't flatten all lists in the imploder?
		//
		// this implementation is not exactly good for performance,
		// but the current IMP interfaces call for ArrayLists rather than just Lists...
		
		// FIXME: createList() has horrible performance right now
		//        what with the flattening and the arraylists and the overrideReferences...
		
		ListAstNode result = new ListAstNode(elementSort, leftToken, rightToken, children);
		
		for (int i = 0; i < children.size(); i++) {
			AstNode child = children.get(i);
			if (child.isList()) {
				children.remove(i--);
				ArrayList<AstNode> subChildren = child.getChildren();
				for (int j = 0; j < subChildren.size(); j++) {
					AstNode subChild = subChildren.get(j);
					children.add(++i, subChild);
				}
				result.overrideReferences(child.getLeftIToken(), child.getRightIToken(), subChildren, child);
			}
		}
		
		return result;
	}
}
