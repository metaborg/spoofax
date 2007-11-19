package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import lpg.runtime.ILexStream;
import lpg.runtime.IToken;

/**
 * Default ATermAstNode factory.
 * 
 * @note Should be overridden to supply specialized AstNode classes for a specific grammar.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class SGLRAstNodeFactory {
	/**
	 * Create a new non-terminal AST node
	 */
	public SGLRAstNode createNonTerminal(String constructor, IToken leftToken, IToken rightToken,
			ArrayList<SGLRAstNode> children) {
		
		return new SGLRAstNode(constructor, leftToken, rightToken, children);
	}
	
	/**
	 * Create a new terminal AST node.
	 */
	public SGLRAstNode createTerminal(String contents, IToken leftToken, IToken rightToken) {
		return new SGLRAstNode(contents, leftToken, rightToken);
	}
	
	/**
	 * Create a new AST node list. 
	 */
	public SGLRAstNode createList(IToken leftToken, IToken rightToken,
			ArrayList<SGLRAstNode> children) {
		
		return new SGLRAstNode(SGLRAstNode.LIST_CONSTRUCTOR, leftToken, rightToken, children);
	}
	
	// Bridge method
	
	/**
	 * Create a new terminal AST node.
	 */
	public final SGLRAstNode createTerminal(IToken token) {
		ILexStream lex = token.getPrsStream().getLexStream();
		
		int length = token.getEndOffset() - token.getStartOffset() + 1;
		StringBuilder tokenContents = new StringBuilder(length);
		
		for (int i = token.getStartOffset(); i < token.getEndOffset(); i++) {
			tokenContents.append(lex.getCharValue(i));
		}
		
		return createTerminal(tokenContents.toString(), token, token);
	}
}
