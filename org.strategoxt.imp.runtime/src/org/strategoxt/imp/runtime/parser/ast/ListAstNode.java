package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import lpg.runtime.IToken;

public class ListAstNode extends AstNode {
	private final String elementSort;
	
	public String getElementSort() {
		return elementSort;
	}
	
	@Override
	public boolean isList() {
		return true;
	}

	public ListAstNode(String elementSort, IToken leftToken, IToken rightToken,
			ArrayList<AstNode> children) {
		
		super(elementSort + "*", "[]", leftToken, rightToken, children);
		
		this.elementSort = elementSort;
	}
	
	@Override
	public String repr() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for(AstNode kid : children) {
			sb.append(kid.repr());
			sb.append(',');
		}
		sb.append(']');
		return sb.toString();
	}
}
