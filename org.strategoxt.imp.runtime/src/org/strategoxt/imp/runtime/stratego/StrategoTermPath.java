package org.strategoxt.imp.runtime.stratego;

import java.util.ArrayList;
import java.util.List;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;

/**
 * Maintains aterm paths, lists of nodes on the path to the root from a given AST node.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoTermPath extends StrategoListProxy {

	private StrategoTermPath() {}
	
	public static IStrategoList createPath(IStrategoAstNode node) {
		List<IStrategoTerm> path = new ArrayList<IStrategoTerm>();
		
		while(node.getParent() != null) {
			IStrategoAstNode parent = node.getParent();
			int index = parent.getAllChildren().indexOf(node);
			path.add(Environment.getWrappedTermFactory().makeInt(index));
			node = node.getParent();
		}
		
		return Environment.getWrappedAstNodeFactory().makeList(path);
	}	
}
