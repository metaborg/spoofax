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
public abstract class SGLRAstNodeFactory<TNode extends SGLRAstNode> {
	// TODO: Cleanup
	
	/**
	 * Create a new non-terminal AST node
	 */
	public abstract TNode createNonTerminal(String sort, String constructor, IToken leftToken, IToken rightToken,
			ArrayList<TNode> children);
	
	/**
	 * Create a new terminal AST node.
	 */
	public abstract TNode createTerminal(String sort, String value, IToken leftToken,
			IToken rightToken);
	
	
	/**
	 * Create a new AST node list. 
	 */
	public TNode createList(String elementSort, IToken leftToken, IToken rightToken, ArrayList<TNode> children) {
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
