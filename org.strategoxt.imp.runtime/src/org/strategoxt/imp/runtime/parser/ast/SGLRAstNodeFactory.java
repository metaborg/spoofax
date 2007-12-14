package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import lpg.runtime.ILexStream;
import lpg.runtime.IToken;

/**
 * Default ATermAstNode factory.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class SGLRAstNodeFactory {
	/**
	 * Create a new non-terminal AST node (or a terminal with only a constructor).
	 */
	public SGLRAstNode createNonTerminal(String sort, String constructor, IToken leftToken, IToken rightToken,
			ArrayList<SGLRAstNode> children) {
		
		// TODO: Should top node with sort "<START>" get special treatment? Probably not...

		return new SGLRAstNode(sort, constructor, leftToken, rightToken, children);
	}
	
	/**
	 * Create a new terminal AST node.
	 */
	public SGLRAstNode createTerminal(String sort, String value, IToken leftToken,
			IToken rightToken) {
		
		return new StringSGLRAstNode(sort, value, leftToken, rightToken);
	}
	
	/**
	 * Create a new terminal AST node.
	 */
	public SGLRAstNode createTerminal(String sort, int value, IToken leftToken,
			IToken rightToken) {
		
		return new IntSGLRAstNode(sort, value, leftToken, rightToken);
	}
	
	
	/**
	 * Create a new AST node list. 
	 */
	public SGLRAstNode createList(String elementSort, IToken leftToken, IToken rightToken, ArrayList<SGLRAstNode> children) {
		// TODO: Specialized AST Node type for lists
		
		String sort = elementSort + "*";
		return createNonTerminal(sort, SGLRAstNode.LIST_CONSTRUCTOR, leftToken, rightToken, children);
	}
	
	// Bridge method
	
	/**
	 * Create a new terminal AST node.
	 */
	public final SGLRAstNode createTerminal(String sort, IToken token) {
		ILexStream lex = token.getPrsStream().getLexStream();
		
		int length = token.getEndOffset() - token.getStartOffset() + 1;
		StringBuilder tokenContents = new StringBuilder(length);
		
		for (int i = token.getStartOffset(); i <= token.getEndOffset(); i++) {
			tokenContents.append(lex.getCharValue(i));
		}
		
		return createTerminal(sort, tokenContents.toString(), token, token);
	}
}
