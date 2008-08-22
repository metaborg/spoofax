package org.strategoxt.imp.runtime.stratego;

import java.util.ArrayList;
import java.util.List;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;

/**
 * Represents a list of ATerms, forming a path to the root from a given AST node.
 * 
 * @note This class uses lazy initialization, to avoid the overhead when it's unused.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoTermPath extends StrategoListProxy implements IStrategoList {
	private final IStrategoAstNode node;
	
	public StrategoTermPath(IStrategoAstNode node) {
		this.node = node;
	}

	@Override
	public IStrategoList getWrapped() {
		if (super.getWrapped() == null) initialize();
		return super.getWrapped();
	}

	private void initialize() {
		List<IStrategoTerm> path = new ArrayList<IStrategoTerm>();
		
		for (IStrategoAstNode parent = node; (parent = node.getParent()) != null; ) {
			path.add(parent.getTerm());
		}
		
		setWrapped(Environment.getWrappedAstNodeFactory().makeList(path));
	}	
}
