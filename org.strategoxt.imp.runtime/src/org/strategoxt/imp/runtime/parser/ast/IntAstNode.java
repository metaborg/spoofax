package org.strategoxt.imp.runtime.parser.ast;

import lpg.runtime.IToken;

/**
 * An integer terminal AST node (representing a single character).
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class IntAstNode extends AstNode {
	private final int value;

	protected IntAstNode(String sort, int value, IToken leftToken, IToken rightToken) {
		// Construct an empty list (unfortunately needs to be a concrete ArrayList type)
		super(sort, null, leftToken, rightToken, EMPTY_LIST);
		
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
