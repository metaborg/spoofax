package org.strategoxt.imp.runtime.stratego.adapter;

import lpg.runtime.IAst;

import org.spoofax.interpreter.terms.BasicTermFactory;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.InlinePrinter;

public abstract class WrappedAstNode implements IWrappedAstNode, IStrategoTerm {
	private final IAst node;
	
	private final WrappedAstNodeFactory factory;
	
	public final IAst getNode() {
		return node;
	}
	
	protected WrappedAstNode(WrappedAstNodeFactory factory, IAst node) {
		this.factory = factory;
		this.node = node;
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
		return factory.wrap((IAst) node.getChildren().get(index));
	}

	public int getSubtermCount() {
		return node.getChildren().size();
	}
	
	public IStrategoList getAnnotations() {
		/** Only {@link AnnotatedAstNode } has annotations */
		return BasicTermFactory.EMPTY_LIST;
	}
	
	public final boolean match(IStrategoTerm second) {
		return equals(second);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof WrappedAstNode) {
			return node == ((WrappedAstNode) other).node
				|| slowCompare((IStrategoTerm) other);
		} else if (other instanceof IStrategoTerm) {
			return slowCompare((IStrategoTerm) other);
		} else {
			return false;
		}
	}
	
	@Override
	public abstract int hashCode();
	
	protected abstract boolean slowCompare(IStrategoTerm second);
	
	@Override
    public String toString() {
    	InlinePrinter ip = new InlinePrinter();
    	prettyPrint(ip);
    	return ip.getString();
    }

}
