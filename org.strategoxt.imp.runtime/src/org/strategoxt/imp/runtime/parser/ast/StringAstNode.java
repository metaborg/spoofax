package org.strategoxt.imp.runtime.parser.ast;

import org.spoofax.interpreter.terms.ITermPrinter;

import lpg.runtime.ILexStream;
import lpg.runtime.IToken;

/**
 * A String terminal AST node.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class StringAstNode extends AstNode {
	private String value;

	protected StringAstNode(String sort, IToken leftToken, IToken rightToken) {
		// Construct an empty list (unfortunately needs to be a concrete ArrayList type)
		super(sort, leftToken, rightToken, null, EMPTY_LIST);
	}

	public String getValue() {
		if (value != null) return value;
		
		IToken left = getLeftIToken();
		IToken right = getRightIToken();
		ILexStream lex = left.getIPrsStream().getILexStream();
		
		int length = right.getEndOffset() - left.getStartOffset() + 1;
		StringBuilder tokenContents = new StringBuilder(length);
		
		for (int i = left.getStartOffset(), end = right.getEndOffset(); i <= end; i++) {
			tokenContents.append(lex.getCharValue(i));
		}
		
		value = tokenContents.toString();

		return value;
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
}
