package org.strategoxt.imp.runtime.parser.tokens;

import lpg.runtime.IPrsStream;
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

	public SGLRToken(IPrsStream parseStream, int startOffset, int endOffset, int kind) {
		super(parseStream, startOffset, endOffset, kind);
	}
}
