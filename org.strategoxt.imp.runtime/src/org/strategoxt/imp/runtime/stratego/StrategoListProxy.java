package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;

/**
 * A proxy class for IStrategoList.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoListProxy implements IStrategoList {
	private IStrategoList wrapped;
	
	public IStrategoList getWrapped() {
		return wrapped;
	}
	
	public void setWrapped(IStrategoList wrapped) {
		this.wrapped = wrapped;
	}

	public IStrategoTerm get(int index) {
		return getWrapped().get(index);
	}

	public IStrategoTerm head() {
		return getWrapped().head();
	}

	public boolean isEmpty() {
		return getWrapped().isEmpty();
	}

	public IStrategoList prepend(IStrategoTerm prefix) {
		return getWrapped().prepend(prefix);
	}

	public int size() {
		return getWrapped().size();
	}

	public IStrategoList tail() {
		return getWrapped().tail();
	}

	public IStrategoTerm[] getAllSubterms() {
		return getWrapped().getAllSubterms();
	}
	
	public IStrategoList getAnnotations() {
		return getWrapped().getAnnotations();
	}

	public IStrategoTerm getSubterm(int index) {
		return getWrapped().getSubterm(index);
	}

	public int getSubtermCount() {
		return getWrapped().getSubtermCount();
	}

	public int getTermType() {
		return getWrapped().getTermType();
	}

	public boolean match(IStrategoTerm second) {
		return getWrapped().match(second);
	}

	public void prettyPrint(ITermPrinter pp) {
		getWrapped().prettyPrint(pp);
	}
}
