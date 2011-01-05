package org.strategoxt.imp.runtime.parser.tokens;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.Token;

/**
 * An SGLR token. Maintains a link to its associated AST node.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class SGLRToken extends Token {

	private IStrategoTerm astNode;

	public SGLRToken(ITokenizer tokenizer, int index, int line, int column, int startOffset,
			int endOffset, int kind) {
		super(tokenizer, index, line, column, startOffset, endOffset, kind);
	}

	public IStrategoTerm getAstNode() {
		return astNode;
	}

	public void setAstNode(IStrategoTerm value) {
		astNode = value;
	}
}
