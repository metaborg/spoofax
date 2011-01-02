package org.strategoxt.imp.runtime.parser.tokens;

import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;

/**
 * An SGLR token. Maintains a link to its associated AST node.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class SGLRToken extends Token {
	private IStrategoTerm astNode;

	public ISimpleTerm getAstNode() {
		return astNode;
	}

	public void setAstNode(IStrategoTerm value) {
		astNode = value;
	}
	
	@Override
	public String toString() {
		return toString(this, this);
	}

	public static String toString(IToken left, IToken right) {
		ILexStream lex = left.getILexStream();
		
		int length = right.getEndOffset() - left.getStartOffset() + 1;
		if (length < 1) return "";
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

	public SGLRToken(ITokenizer parseStream, int startOffset, int endOffset, int kind) {
		super(parseStream, startOffset, endOffset, kind);
	}
}
