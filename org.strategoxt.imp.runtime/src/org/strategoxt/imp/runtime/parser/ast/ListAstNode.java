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
}
