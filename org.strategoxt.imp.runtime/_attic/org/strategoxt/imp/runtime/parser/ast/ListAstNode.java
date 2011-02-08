package org.strategoxt.imp.runtime.parser.ast;

import java.util.ArrayList;

import org.spoofax.jsglr.client.imploder.IToken;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;

public class ListAstNode extends IStrategoTerm {
	
	private final String elementSort;
	
	public String getElementSort() {
		return elementSort;
	}
	
	/**
	 * @deprecated Use {@link #getElementSort()} to get the element sort instead.
	 */
	@Override @Deprecated
	public String getSort() {
		return getSort(super);
	}
	
	@Override
	public boolean isList() {
		return true;
	}

	public ListAstNode(String elementSort, IToken leftToken, IToken rightToken,
			ArrayList<IStrategoTerm> children) {
		
		super(elementSort + "*", leftToken, rightToken, "[]", children);
		
		this.elementSort = elementSort;
	}
	
	@Override
	public void prettyPrint(ITermPrinter printer) {
		printer.print("[");
		if (getSubtermCount() > 0) {
			getSubterm(0).prettyPrint(printer);
			for (int i = 1; i < getSubtermCount(); i++) {
				printer.print(",");
				getSubterm(i).prettyPrint(printer);
			}
		}
		printer.print("]");
	}
	
	public IStrategoTerm getFirstChild() {
		return getSubterm(0);
	}
	
	public IStrategoTerm getLastChild() {
		return getSubterm(getSubtermCount() - 1);
	}

	@Override
	public int getTermType() {
		return IStrategoTerm.LIST;
	}
}
