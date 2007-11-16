package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import aterm.ATermAppl;

import static org.strategoxt.imp.runtime.parser.ast.SGLRParsersym.*;

import lpg.runtime.ILexStream;
import lpg.runtime.IToken;

/**
 * Default ATermAstNode factory.
 * 
 * Should be overridden to supply specialized AstNode classes for a specific grammar.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class SGLRAstNodeFactory {
	/**
	 * Create a non-terminal AST node
	 */
	public SGLRAstNode createNonTerminal(String constructor, IToken leftToken, IToken rightToken,
			ArrayList<SGLRAstNode> children) {
		
		return new SGLRAstNode(constructor, leftToken, rightToken, children);
	}
	
	/**
	 * Create a terminal AST node.
	 */
	public SGLRAstNode createTerminal(String contents, IToken leftToken, IToken rightToken) {
		return new SGLRAstNode(contents, leftToken, rightToken);
	}
	
	/**
	 * Create a AST node list. 
	 */
	public SGLRAstNode createList(IToken leftToken, IToken rightToken,
			ArrayList<SGLRAstNode> children) {
		
		return new SGLRAstNode(SGLRAstNode.LIST_CONSTRUCTOR, leftToken, rightToken, children);
	}
	
	/**
	 * Get the token kind for a given sort.
	 */
	public int getTokenKind(ATermAppl sort) {
		// TODO: More default token kinds
		//       e.g., for numbers, comments 

		if (isLayoutSort(sort)) {
			return TK_LAYOUT;
		} else if (sort.getName().equals("lex")) {
			return TK_IDENTIFIER;
		} else if (isOperator(sort)) {
			return TK_OPERATOR;
		} else {
			return TK_KEYWORD;
		}
	}
	
	// Utility methods
	
	public final SGLRAstNode createTerminal(IToken token) {
		ILexStream lex = token.getPrsStream().getLexStream();
		
		int length = token.getEndOffset() - token.getStartOffset() + 1;
		StringBuilder tokenContents = new StringBuilder(length);
		
		for (int i = token.getStartOffset(); i < token.getEndOffset(); i++) {
			tokenContents.append(lex.getCharValue(i));
		}
		
		return createTerminal(tokenContents.toString(), token, token);
	}

	private static boolean isLayoutSort(ATermAppl sort) {
		ATermAppl details = (ATermAppl) sort.getChildAt(0);
	
		if (details.getName().equals("opt"))
			details = (ATermAppl) details.getChildAt(0);
			
		return details.getName().equals("layout");
	}
	
	private static boolean isOperator(ATermAppl sort) {
		if (sort.getName() != "lit") return false;
		
		ATermAppl lit = (ATermAppl) sort.getChildAt(0);
		String contents = lit.getName();
		
		for (int i = 0; i < contents.length(); i++) {
			char c = contents.charAt(i);
			if (Character.isLetterOrDigit(c)) return false;
		}
		
		return true;
	}
}
