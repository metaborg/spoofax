package org.strategoxt.imp.runtime.stratego.adapter;

import java.util.ArrayList;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * A WrappedAstNode with children (more specifically, a tuple, appl, or link term). 
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public abstract class WrappedAstNodeParent extends WrappedAstNode {

	IStrategoTerm[] subterms;
	
	protected WrappedAstNodeParent(IStrategoAstNode node, IStrategoList annotations) {
		super(node, annotations);
	}
	
	protected WrappedAstNodeParent(IStrategoAstNode node) {
		super(node);
	}

	@Override
	public IStrategoTerm[] getAllSubterms() {
		if (subterms != null) return subterms;
		
		IStrategoTerm[] result = new IStrategoTerm[getSubtermCount()];
		int size = getSubtermCount();
		ArrayList children = getNode().getChildren();
		
		
		for (int i = 0; i < size; i++) {
			result[i] = ((IStrategoAstNode) children.get(i)).getTerm();
		}
		
		return subterms = result;
	}

	@Override
	public IStrategoTerm getSubterm(int index) {
		if (subterms == null) {
			return ((IStrategoAstNode) getNode().getChildren().get(index)).getTerm();
		} else {
			if (-1 < index && index < subterms.length)
				return subterms[index];
			else
				throw new IndexOutOfBoundsException("Index " + index + " of " + this);
		}
	}

	@Override
	public int getSubtermCount() {
		return subterms != null ? subterms.length : getNode().getChildren().size();
	}
}
