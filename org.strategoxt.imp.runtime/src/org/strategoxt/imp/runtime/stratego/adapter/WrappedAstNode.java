package org.strategoxt.imp.runtime.stratego.adapter;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.io.InlinePrinter;
import org.strategoxt.lang.terms.StrategoTerm;
import org.strategoxt.lang.terms.TermFactory;

public abstract class WrappedAstNode extends StrategoTerm implements IWrappedAstNode, IStrategoTerm, Cloneable {
	
	private final IStrategoAstNode node;
	
	/**
	 * Gets the node or origin node associated with this term.
	 */
	public final IStrategoAstNode getNode() {
		return node;
	}
	
	public WrappedAstNode(IStrategoAstNode node) {
		this(node, null);
	}
	
	public WrappedAstNode(IStrategoAstNode node, IStrategoList annotations) {
		super(annotations);
		this.node = node;
		assert node != null;
	}
	
	public final int getStorageType() {
		// All WrappedAstNodes wrap around an immutable AstNode,
		// and cannot have non-AstNode children.
		return IMMUTABLE;
	}

	public IStrategoTerm[] getAllSubterms() {
		assert node.getChildren().size() == 0;
		return TermFactory.EMPTY;
	}

	public IStrategoTerm getSubterm(int index) {
		assert node.getChildren().size() == 0;
        throw new IndexOutOfBoundsException();
	}

	public int getSubtermCount() {
		assert node.getChildren().size() == 0;
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
