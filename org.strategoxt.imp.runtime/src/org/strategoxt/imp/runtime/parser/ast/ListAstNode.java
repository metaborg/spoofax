package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import org.spoofax.interpreter.terms.ITermPrinter;

import lpg.runtime.IToken;

public class ListAstNode extends AstNode {
	
	private final String elementSort;
	
	public String getElementSort() {
		return elementSort;
	}
	
	/**
	 * @deprecated Use #getElementSort() to get the element sort instead.
	 */
	@Override @Deprecated
	public String getSort() {
		return super.getSort();
	}
	
	@Override
	public boolean isList() {
		return true;
	}

	public ListAstNode(String elementSort, IToken leftToken, IToken rightToken,
			ArrayList<AstNode> children) {
		
		super(elementSort + "*", leftToken, rightToken, "[]", children);
		
		this.elementSort = elementSort;
	}
	
	@Override
	public void prettyPrint(ITermPrinter printer) {
		printer.print("[");
		if (getChildren().size() > 0) {
			getChildren().get(0).prettyPrint(printer);
			for (int i = 1; i < getChildren().size(); i++) {
				printer.print(",");
				getChildren().get(i).prettyPrint(printer);
			}
		}
		printer.print("]");
	}
	
	public AstNode getFirstChild() {
		return getChildren().get(0);
	}
	
	public AstNode getLastChild() {
		return getChildren().get(getChildren().size() - 1);
	}
}
