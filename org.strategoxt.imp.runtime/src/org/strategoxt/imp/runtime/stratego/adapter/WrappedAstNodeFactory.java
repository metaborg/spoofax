package org.strategoxt.imp.runtime.stratego.adapter;

import java.io.IOException;
import java.util.WeakHashMap;

import org.spoofax.interpreter.terms.BasicTermFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.IntAstNode;
import org.strategoxt.imp.runtime.parser.ast.StringAstNode;

import lpg.runtime.IAst;

/**
 * A factory creating ATerms from AST nodes.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Karl Trygve Kalleberg <karltk add strategoxt.org>
 */
public class WrappedAstNodeFactory extends BasicTermFactory {	
	private static final WeakHashMap<IAst, WrappedAstNode> cache
		= new WeakHashMap<IAst, WrappedAstNode>();

	public IStrategoTerm wrap(IAst node) {
		if (node instanceof IStrategoAstNode) {
			// Get associated term from IStrategoAstNode, may be cached
			return ((IStrategoAstNode) node).getTerm();
		} else {
			WrappedAstNode result = cache.get(node);
			if (result != null) return result;
		}
		
		return wrapNew(node);
	}

	public IStrategoTerm wrapNew(IAst node) {
		// TODO: Foreign IAst wrapping doesn't do terminals
		if(node instanceof IntAstNode) {
			return new WrappedAstNodeInt(this, (IntAstNode)node);
		} else if(node instanceof WrappedAstNodeString) {
			return new WrappedAstNodeString(this, (StringAstNode)node);
		} else {
			return new WrappedAstNodeAppl(this, node);
		}
	}
	
	@Override
	public IStrategoTerm parseFromFile(String path) throws IOException {
		// BasicTermFactory does not support binary aterms atm
		return Environment.getWrappedTermFactory().parseFromFile(path);
	}
	
	@Override
	public IStrategoTerm parseFromString(String path) {
		// BasicTermFactory does not support binary aterms atm
		return Environment.getWrappedTermFactory().parseFromString(path);
	}

	public IStrategoTerm wrapNew(IStrategoAstNode node) {
		if(node instanceof IntAstNode) {
			return new WrappedAstNodeInt(this, (IntAstNode)node);
		} else if(node instanceof StringAstNode) {
			return new WrappedAstNodeString(this, (StringAstNode)node);
		} else {
			return new WrappedAstNodeAppl(this, node);
		}
	}
}
