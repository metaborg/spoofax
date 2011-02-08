package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import org.spoofax.jsglr.client.imploder.IToken;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class TupleAstNode extends IStrategoTerm {
	
	public static String CONSTRUCTOR = "";
	
	private final String elementSort;

	public TupleAstNode(String elementSort, IToken leftToken, IToken rightToken,
			ArrayList<IStrategoTerm> children) {
		
		super(elementSort + "*", leftToken, rightToken, CONSTRUCTOR, children);
		this.elementSort = elementSort;
	}
	
	/**
	 * @deprecated Use {@link #getElementSort()} to get the element sort instead.
	 */
	@Override @Deprecated
	public String getSort() {
		return getSort(super);
	}
	
	public String getElementSort() {
		return elementSort;
	}
	
	@Override
	public void prettyPrint(ITermPrinter printer) {
		printer.print("(");
		if (getSubtermCount() > 0) {
			getSubterm(0).prettyPrint(printer);
			for (int i = 1; i < getSubtermCount(); i++) {
				printer.print(",");
				getSubterm(i).prettyPrint(printer);
			}
		}
		printer.print(")");
	}

	@Override
	public int getTermType() {
		return IStrategoTerm.TUPLE;
	}

}
