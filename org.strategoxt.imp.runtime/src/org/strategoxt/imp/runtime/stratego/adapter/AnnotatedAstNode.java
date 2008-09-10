package org.strategoxt.imp.runtime.stratego.adapter;

import lpg.runtime.IAst;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoReal;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermPrinter;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AnnotatedAstNode implements IWrappedAstNode, IStrategoTerm, IStrategoList, IStrategoAppl, IStrategoTuple, IStrategoInt, IStrategoReal {
	
	private final IStrategoTerm wrapped;
	
	private final IStrategoList annotations;
	
	public AnnotatedAstNode(IStrategoTerm node, IStrategoList annotations) {
		this.wrapped = node;
		this.annotations = annotations;
	}

	public IAst getNode() {
		return ((IWrappedAstNode) wrapped).getNode();
	}

	public IStrategoList getAnnotations() {
		return annotations;
	}
	
	// Common accessors

	public IStrategoTerm[] getAllSubterms() {
		return wrapped.getAllSubterms();
	}

	public IStrategoTerm getSubterm(int index) {
		return wrapped.getSubterm(index);
	}

	public int getSubtermCount() {
		return wrapped.getSubtermCount();
	}

	public int getTermType() {
		return wrapped.getTermType();
	}

	public boolean match(IStrategoTerm second) {
		return wrapped.match(second);
	}

	public void prettyPrint(ITermPrinter pp) {
		wrapped.prettyPrint(pp);
	}
	
	// Semi-specialized accessors

	public final IStrategoTerm get(int index) {
		return getSubterm(index);
	}

	public final IStrategoTerm[] getArguments() {
		return wrapped.getAllSubterms();
	}
	
	// Specialized accessors

	public IStrategoTerm head() {
		if (getTermType() != LIST)
			throw new AnnotationWrapperException("Called head() on a term that is not of type LIST");
		return ((IStrategoList) wrapped).head();
	}

	public IStrategoList tail() {
		if (getTermType() != LIST)
			throw new AnnotationWrapperException("Called tail() on a term that is not of type LIST");
		return ((IStrategoList) wrapped).tail();
	}

	public boolean isEmpty() {
		if (getTermType() != LIST)
			throw new AnnotationWrapperException("Called isEmpty() on a term that is not of type LIST");
		return ((IStrategoList) wrapped).isEmpty();
	}

	public IStrategoList prepend(IStrategoTerm prefix) {
		if (getTermType() != LIST)
			throw new AnnotationWrapperException("Called prepend() on a term that is not of type LIST");
		return ((IStrategoList) wrapped).prepend(prefix);
	}

	public int size() {
		switch (getTermType()) {
			case LIST:
				return ((IStrategoList) wrapped).size();
			case TUPLE:
				return ((IStrategoTuple) wrapped).size();
			default:
				throw new AnnotationWrapperException("Called size() on a term that is not a LIST or TUPLE");
		}
	}

	public IStrategoConstructor getConstructor() {
		if (getTermType() != APPL)
			throw new AnnotationWrapperException("Called getConstructor() on a term that is not of type LIST");
		return ((IStrategoAppl) wrapped).getConstructor();
	}

	public int intValue() {
		if (getTermType() != APPL)
			throw new AnnotationWrapperException("Called intValue() on a term that is not of type LIST");
		return ((IStrategoInt) wrapped).intValue();
	}

	public double realValue() {
		if (getTermType() != APPL)
			throw new AnnotationWrapperException("Called realValue() on a term that is not of type LIST");
		return ((IStrategoReal) wrapped).realValue();
	}
}
