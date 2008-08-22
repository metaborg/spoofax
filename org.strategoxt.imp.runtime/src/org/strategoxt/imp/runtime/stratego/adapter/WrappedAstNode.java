package org.strategoxt.imp.runtime.stratego.adapter;

import lpg.runtime.IAst;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.InlinePrinter;

public abstract class WrappedAstNode implements IStrategoTerm {
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
	
	public final boolean match(IStrategoTerm second) {
		return equals(second);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof WrappedAstNode) {
			return node.equals(((WrappedAstNode) other).node);
		} else if (other instanceof IStrategoTerm) {
			return slowCompare((IStrategoTerm) other);
		} else {
			return false;
		}
	}
	
	protected abstract boolean slowCompare(IStrategoTerm second);
	
	@Override
    public String toString() {
    	InlinePrinter ip = new InlinePrinter();
    	prettyPrint(ip);
    	return ip.getString();
    }

}
