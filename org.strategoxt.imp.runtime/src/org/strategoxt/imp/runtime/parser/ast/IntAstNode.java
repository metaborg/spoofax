package org.strategoxt.imp.runtime.parser.ast;

import org.spoofax.interpreter.terms.ITermPrinter;

import lpg.runtime.IToken;

/**
 * An integer terminal AST node (representing a single character).
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class IntAstNode extends AstNode {
	private final int value;

	public IntAstNode(String sort, IToken leftToken, IToken rightToken, int value) {
		// Construct an empty list (unfortunately needs to be a concrete ArrayList type)
		super(sort, leftToken, rightToken, null, EMPTY_LIST);
		
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	@Override
	public String yield() {
		return String.valueOf(getValue());
	}
	
	@Override
	public void prettyPrint(ITermPrinter printer) {
		printer.print(String.valueOf(getValue()));
	}
}
