package org.strategoxt.imp.runtime.stratego.adapter;

import lpg.runtime.IAst;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoReal;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermPrinter;
import org.strategoxt.lang.terms.TermFactory;

/**
 * A wrapper class linking any {@link IStrategoTerm} to an {@link IAst} node.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class WrappedAstNodeLink extends WrappedAstNode implements IWrappedAstNode, IStrategoTerm, IStrategoList, IStrategoAppl, IStrategoTuple, IStrategoInt, IStrategoReal, IStrategoString {
	
	private final IStrategoTerm wrapped;
	
	public WrappedAstNodeLink(WrappedAstNodeFactory factory, IStrategoTerm term, IStrategoAstNode node) {
		super(factory, node);
		this.wrapped = term;
		assert !(wrapped instanceof IWrappedAstNode);
	}
	
	public final IStrategoTerm getWrapped() {
		return wrapped;
	}
	
	// Annotation handling
	
	@Override
	protected WrappedAstNodeLink getAnnotatedWith(IStrategoList annotations) {
		// To get a working equals() and hashCode() impl, we need to annotate the wrapped term
		IStrategoTerm wrapped = getFactory().annotateTerm(getWrapped(), annotations);
		return new WrappedAstNodeLink(getFactory(), wrapped, getNode());
	}
	
	// Common accessors

	@Override
	protected boolean doSlowMatch(IStrategoTerm second, int commonStorageType) {
		IStrategoTerm wrapped = getWrapped();
		IStrategoList annotations = getAnnotations();
		IStrategoList secondAnnotations = second.getAnnotations();
		
		if (annotations != secondAnnotations && !annotations.match(secondAnnotations))
        	return false;
		
		if (annotations.isEmpty()) {
			return wrapped.match(second);
		} else {
			second = getFactory().annotateTerm(second, TermFactory.EMPTY_LIST);
			return wrapped.match(second);
		}
	}

	@Override
	public int hashFunction() {
		return wrapped.hashCode();
	}

	@Override
	public IStrategoTerm[] getAllSubterms() {
		return wrapped.getAllSubterms();
	}

	@Override
	public IStrategoTerm getSubterm(int index) {
		return wrapped.getSubterm(index);
	}

	@Override
	public int getSubtermCount() {
		return wrapped.getSubtermCount();
	}

	public int getTermType() {
		return wrapped.getTermType();
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

	@Deprecated
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
			throw new AnnotationWrapperException("Called getConstructor() on a term that is not of type APPL");
		return ((IStrategoAppl) wrapped).getConstructor();
	}

	public int intValue() {
		if (getTermType() != INT)
			throw new AnnotationWrapperException("Called intValue() on a term that is not of type INT");
		return ((IStrategoInt) wrapped).intValue();
	}

	public double realValue() {
		if (getTermType() != REAL)
			throw new AnnotationWrapperException("Called realValue() on a term that is not of type APPL");
		return ((IStrategoReal) wrapped).realValue();
	}

	public String stringValue() {
		if (getTermType() != STRING)
			throw new AnnotationWrapperException("Called stringValue() on a term that is not of type STRING");
		return ((IStrategoString) wrapped).stringValue();
	}
}
