package org.strategoxt.imp.runtime.stratego.adapter;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoTerm;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.io.InlinePrinter;

public abstract class WrappedAstNode extends StrategoTerm implements IStrategoTerm, IStrategoTerm, Cloneable {
	
	private final ISimpleTerm node;
	
	/**
	 * Gets the node or origin node associated with this term.
	 */
	public final ISimpleTerm getNode() {
		return node;
	}
	
	public WrappedAstNode(ISimpleTerm node) {
		this(node, null);
	}
	
	public WrappedAstNode(ISimpleTerm node, IStrategoList annotations) {
		super(annotations);
		this.node = node;
		assert node != null;
	}
	
	public final int getStorageType() {
		// All WrappedAstNodes wrap around an immutable IStrategoTerm,
		// and cannot have non-IStrategoTerm children.
		return IMMUTABLE;
	}

	public IStrategoTerm[] getAllSubterms() {
		assert node.getSubtermCount() == 0;
		return TermFactory.EMPTY;
	}

	public IStrategoTerm getSubterm(int index) {
		assert node.getSubtermCount() == 0;
        throw new IndexOutOfBoundsException();
	}

	public int getSubtermCount() {
		assert node.getSubtermCount() == 0;
		return 0;
	}
	
	/**
	 * Creates a copy of this term, and applies the given annotations to it.
	 * 
	 * @see WrappedAstNodeFactory#annotateTerm(IStrategoTerm, IStrategoList)
	 */
	protected WrappedAstNode getAnnotatedWith(IStrategoList annotations) {
		WrappedAstNode result = clone();
		result.internalSetAnnotations(annotations);
		return result;
	}
	
	@Override
	protected WrappedAstNode clone() {
		return (WrappedAstNode) super.clone();
	}
	
	@Override
    public final String toString() {
    	InlinePrinter ip = new InlinePrinter();
    	prettyPrint(ip);
    	return ip.getString();
    }
}
