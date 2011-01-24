package org.strategoxt.imp.runtime.stratego.adapter;

import lpg.runtime.ISimpleTerm;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoReal;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermPrinter;

/**
 * A wrapper class linking any {@link IStrategoTerm} to an {@link ISimpleTerm} node.
 * 
 * @see WrappedAstNodeFactory#makeLink(IStrategoTerm, IStrategoTerm)
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class WrappedAstNodeLink extends WrappedAstNodeParent implements IStrategoTerm, IStrategoTerm, IStrategoList, IStrategoAppl, IStrategoTuple, IStrategoInt, IStrategoReal, IStrategoString {
	
	private final WrappedAstNodeFactory factory;
	
	private final IStrategoTerm origin; // = node + offset for lists
	
	private final IStrategoTerm wrapped;
	
	protected WrappedAstNodeLink(WrappedAstNodeFactory factory, IStrategoTerm term, IStrategoTerm origin) {
		super(origin, term.getAnnotations());
		this.factory = factory;
		this.wrapped = term;
		this.origin = origin;
		
		assert !hasImploderOrigin(wrapped) : "Already wrapped";
		assert wrapped.getTermType() != LIST || origin.getTermType() != LIST
				|| wrapped.getSubtermCount() == 0
				|| wrapped.getSubtermCount() != origin.getSubtermCount()
				: "Track lists using WrappedAstNodeList / WrappedAstNodeFactory.makeLink()";
	}
	
	public final IStrategoTerm getWrapped() {
		return wrapped;
	}
	
	// Annotation handling
	
	@Override
	protected WrappedAstNodeLink getAnnotatedWith(IStrategoList annotations) {
		// To get a working equals() and hashCode() impl, we need to annotate the wrapped term
		IStrategoTerm wrapped = factory.annotateTerm(getWrapped(), annotations);
		WrappedAstNodeLink result = new WrappedAstNodeLink(factory, wrapped, origin);
		result.subterms = subterms;
		return result;
	}
	
	// Common accessors

	@Override
	protected boolean doSlowMatch(IStrategoTerm second, int commonStorageType) {
		return wrapped == second || wrapped.match(second);
	}

	@Override
	protected final int hashFunction() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int hashCode() {
		return wrapped.hashCode();
	}

	@Override
	public IStrategoTerm[] getAllSubterms() {
		if (subterms == null)
			subterms = ensureChildLinks(wrapped.getAllSubterms());
		return getTermType() == LIST ? subterms.clone() : subterms; // IStrategoList contract is to copy the array
	}

	/**
	 * Ensures (lazy) origin tracking links for subterms.
	 */
	private IStrategoTerm[] ensureChildLinks(IStrategoTerm[] kids) {
		if (!isCorrespondingTerm(kids))
			return kids;
		
		for (int i = 0; i < kids.length; i++) {
			if (!(kids[i] instanceof OneOfThoseTermsWithOriginInformation)) {
				IStrategoTerm[] newKids = new IStrategoTerm[kids.length];
				System.arraycopy(kids, 0, newKids, 0, i);
				newKids[i] = ensureChildLink(kids[i], i);
				while (++i < kids.length) {
					newKids[i] = ensureChildLink(kids[i], i);
				}
				return newKids;
			}
		}
		return kids;
	}
	
	private IStrategoTerm ensureChildLink(IStrategoTerm kid, int index) {
		if (hasImploderOrigin(kid)
				|| index >= origin.getSubtermCount()) {
			return kid;
		} else {
			return factory.ensureLink(kid, origin.getSubterm(index));
		}
	}
	
	/**
	 * Tests if this node corresponds in shape (constructor/nr of subterms) to its origin node.
	 */
	private boolean isCorrespondingTerm(IStrategoTerm[] kids) {
		int termType = wrapped.getTermType();
		if (termType == LIST) {
			// List with inequal amount of kids; other lists are WrappedAstNodeLists not WrappedAstNodeLinks
			return false;
		}
		
		if (origin.getTermType() != termType || origin.getSubtermCount() != kids.length)
			return false;
		
		if (termType == APPL) {
			if (((IStrategoAppl) wrapped).getConstructor() != ((IStrategoAppl) origin).getConstructor()) {
				assert !((IStrategoAppl) wrapped).getConstructor().equals(((IStrategoAppl) origin).getConstructor())
						: "Maximally shared constructors are assumed";
				return false;				
			}
		} else if (termType != TUPLE) {
			// UNDONE: throw new IllegalStateException("Unexpected type of term with kids: " + this);
			return false;
		}

		return true;
	}

	@Override
	public IStrategoTerm getSubterm(int index) {
		return getAllSubterms()[index];
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
		return getAllSubterms();
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
			throw new AnnotationWrapperException("Called realValue() on a term that is not of type REAL");
		return ((IStrategoReal) wrapped).realValue();
	}

	public String stringValue() {
		if (getTermType() != STRING)
			throw new AnnotationWrapperException("Called stringValue() on a term that is not of type STRING");
		return ((IStrategoString) wrapped).stringValue();
	}
}
