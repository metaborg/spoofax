package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import org.spoofax.interpreter.terms.ITermPrinter;

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
}
