package org.strategoxt.imp.runtime.parser.ast;

import lpg.runtime.IToken;

/**
 * A String terminal AST node.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class StringSGLRAstNode extends SGLRAstNode {
	private final String value;

	protected StringSGLRAstNode(String sort, String value, IToken leftToken, IToken rightToken) {
		// Construct an empty list (unfortunately needs to be a concrete ArrayList type)
		super(sort, null, leftToken, rightToken, EMPTY_LIST);
		
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return getValue();
	}
}
