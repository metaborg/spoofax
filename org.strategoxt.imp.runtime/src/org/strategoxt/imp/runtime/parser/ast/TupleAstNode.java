package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import lpg.runtime.IToken;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class TupleAstNode extends AstNode {
	
	public static String CONSTRUCTOR = "";
	
	private final String elementSort;

	public TupleAstNode(String elementSort, IToken leftToken, IToken rightToken,
			ArrayList<AstNode> children) {
		
		super(elementSort + "*", leftToken, rightToken, CONSTRUCTOR, children);
		this.elementSort = elementSort;
	}
	
	/**
	 * @deprecated Use {@link #getElementSort()} to get the element sort instead.
	 */
	@Override @Deprecated
	public String getSort() {
		return super.getSort();
	}
	
	public String getElementSort() {
		return elementSort;
	}
	
	@Override
	public void prettyPrint(ITermPrinter printer) {
		printer.print("(");
		if (getChildren().size() > 0) {
			getChildren().get(0).prettyPrint(printer);
			for (int i = 1; i < getChildren().size(); i++) {
				printer.print(",");
				getChildren().get(i).prettyPrint(printer);
			}
		}
		printer.print(")");
	}

	@Override
	public int getTermType() {
		return IStrategoTerm.TUPLE;
	}

}
