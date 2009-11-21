package org.strategoxt.imp.runtime.parser.tokens;

import lpg.runtime.ILexStream;
import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;
import lpg.runtime.Token;

import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;

/**
 * An SGLR token. Maintains a link to its associated AST node.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class SGLRToken extends Token {
	private AstNode astNode;

	public IStrategoAstNode getAstNode() {
		return astNode;
	}

	public void setAstNode(AstNode value) {
		astNode = value;
	}

	public static String toString(IToken left, IToken right) {
		ILexStream lex = left.getIPrsStream().getILexStream();
		
		int length = right.getEndOffset() - left.getStartOffset() + 1;
		StringBuilder result = new StringBuilder(length);
		
		for (int i = left.getStartOffset(), end = right.getEndOffset(); i <= end; i++) {
			result.append(lex.getCharValue(i));
		}
		return result.toString();
	}
	
	public static int indexOf(IToken token, char c) {
		ILexStream stream = token.getILexStream();
		for (int i = token.getStartOffset(), last = token.getEndOffset(); i <= last; i++) { 
			if (stream.getCharValue(i) == c)
				return i;
		}
		return -1;
	}
	
	public static boolean isWhiteSpace(IToken token) {
		ILexStream stream = token.getILexStream();
		for (int i = token.getStartOffset(), last = token.getEndOffset(); i <= last; i++) { 
			if (!Character.isWhitespace(stream.getCharValue(i)))
				return false;
		}
		return true;
	}

	public SGLRToken(IPrsStream parseStream, int startOffset, int endOffset, int kind) {
		super(parseStream, startOffset, endOffset, kind);
	}
}
