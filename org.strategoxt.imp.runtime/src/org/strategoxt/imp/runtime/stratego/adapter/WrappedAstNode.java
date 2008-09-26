package org.strategoxt.imp.runtime.stratego.adapter;

import lpg.runtime.IAst;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;
import org.spoofax.interpreter.terms.InlinePrinter;

public abstract class WrappedAstNode implements IWrappedAstNode, IStrategoTerm, Cloneable {
	private final IAst node;
	
	private final WrappedAstNodeFactory factory;
	
	private IStrategoList annotations;
	
	public final IAst getNode() {
		return node;
	}
	
	public WrappedAstNodeFactory getFactory() {
		return factory;
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
		return factory.wrap((IStrategoAstNode) node.getChildren().get(index));
	}

	public int getSubtermCount() {
		return node.getChildren().size();
	}
	
	public IStrategoList getAnnotations() {
		return annotations == null ? WrappedAstNodeFactory.EMPTY_LIST : annotations;
	}
	
	protected void internalSetAnnotations(IStrategoList annotations) {
		this.annotations = annotations;
	}
	
	public final boolean match(IStrategoTerm second) {
		return equals(second);
	}

	@Override
	public final boolean equals(Object other) {
		if (other instanceof WrappedAstNode) {
			WrappedAstNode otherTerm = (WrappedAstNode) other;
			return (node == otherTerm.node && getAnnotations().equals(otherTerm.getAnnotations()))
				|| slowCompare(otherTerm);
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
	protected WrappedAstNode clone() {
		try {
			return (WrappedAstNode) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
    public final String toString() {
    	InlinePrinter ip = new InlinePrinter();
    	prettyPrint(ip);
    	return ip.getString();
    }
    
    protected void printAnnotations(ITermPrinter pp) {
        IStrategoList annos = getAnnotations();
        if (annos.size() == 0) return;
        
        pp.print("{");
        annos.get(0).prettyPrint(pp);
        for (int i = 1; i < annos.size(); i++) {
            pp.print(",");
            pp.print(annos.toString());
        }
        pp.print("}");
    }
}
