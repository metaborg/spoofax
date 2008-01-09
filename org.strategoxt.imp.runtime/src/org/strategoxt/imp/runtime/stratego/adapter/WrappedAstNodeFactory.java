package org.strategoxt.imp.runtime.stratego.adapter;

import java.util.WeakHashMap;

import org.spoofax.interpreter.terms.BasicTermFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

import lpg.runtime.IAst;

public class WrappedAstNodeFactory extends BasicTermFactory {	
	private static final WeakHashMap<IAst, WrappedAstNode> cache
		= new WeakHashMap<IAst, WrappedAstNode>();

	public IStrategoTerm wrap(IAst node) {
		if (node instanceof IStrategoAstNode) {
			// Get associated term from IStrategoAstNode, may be cached
			return ((IStrategoAstNode) node).getTerm(this);
		} else {
			WrappedAstNode result = cache.get(node);
			if (result != null) return result;
		}
		
		return wrapNew(node);
	}

	public IStrategoTerm wrapNew(IAst node) {
		// TODO2: Foreign IAst wrapping doesn't do terminals
		return new WrappedAstNodeAppl(this, node);
	}

	public IStrategoTerm wrapNew(IStrategoAstNode node) {
		return new WrappedAstNodeAppl(this, node);
	}
}
