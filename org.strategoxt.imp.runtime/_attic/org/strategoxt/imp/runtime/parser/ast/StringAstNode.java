package org.strategoxt.imp.runtime.parser.ast;

import org.spoofax.jsglr.client.imploder.IToken;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;
import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;

/**
 * A String terminal AST node.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class StringAstNode extends IStrategoTerm {
	private String value;

	/**
	 * @param value  The string value for this node or null if the token value should be used.
	 */
	public StringAstNode(String value, String sort, IToken leftToken, IToken rightToken) {
		// Construct an empty list (unfortunately needs to be a concrete ArrayList type)
		super(sort, leftToken, rightToken, null, EMPTY_LIST);
		this.value = value;
	}

	public String getValue() {
		if (value != null) return value;
		return value = SGLRToken.toString(getLeftIToken(), getRightIToken());
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String yield() {
		return getValue();
	}
	
	
	@Override
	public void prettyPrint(ITermPrinter printer) {
		printer.print("\"");
		printer.print(getValue().replace("\"", "\\\""));
		printer.print("\"");
	}

	@Override
	public int getTermType() {
		return IStrategoTerm.STRING;
	}
}
