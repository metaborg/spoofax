package org.strategoxt.imp.runtime.stratego.adapter;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.InlinePrinter;
import org.strategoxt.lang.terms.StrategoTerm;

public abstract class WrappedAstNode extends StrategoTerm implements IWrappedAstNode, IStrategoTerm, Cloneable {
	
	private final IStrategoAstNode node;
	
	private final WrappedAstNodeFactory factory;
	
	public final IStrategoAstNode getNode() {
		return node;
	}
	
	public WrappedAstNodeFactory getFactory() {
		return factory;
	}
	
	protected WrappedAstNode(WrappedAstNodeFactory factory, IStrategoAstNode node) {
		super(null);
		this.factory = factory;
		this.node = node;
	}
	
	public final int getStorageType() {
		// All WrappedAstNodes wrap around an immutable AstNode,
		// and cannot have non-AstNode children.
		return IMMUTABLE;
	}

	public IStrategoTerm[] getAllSubterms() {
		IStrategoTerm[] result = new IStrategoTerm[getSubtermCount()];
		int size = getSubtermCount();
		
		for (int i = 0; i < size; i++) {
			result[i] = getSubterm(i);
		}
		
		return result;
	}

	public IStrategoTerm getSubterm(int index) {
		return factory.wrap((IStrategoAstNode) node.getChildren().get(index));
	}

	public int getSubtermCount() {
		return node.getChildren().size();
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
