package org.strategoxt.imp.runtime.services;

import java.util.Map;
import java.util.WeakHashMap;

import lpg.runtime.IAst;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IReferenceResolver;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.IntAstNode;
import org.strategoxt.imp.runtime.stratego.StrategoTermPath;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;
import org.strategoxt.imp.runtime.stratego.adapter.WrappedAstNode;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoReferenceResolver implements IReferenceResolver {
	private final Interpreter resolver;
	
	private final Map<String, String> resolverFunctions;
	
	private final WeakHashMap<IStrategoAstNode, IAst> cache =
		new WeakHashMap<IStrategoAstNode, IAst>();
	
	private final IStrategoAstNode NOT_FOUND = new IntAstNode(null, 0, null, null) {};
	
	public StrategoReferenceResolver(Interpreter resolver, Map<String, String> resolverFunctions) {
		this.resolver = resolver;
		this.resolverFunctions = resolverFunctions;
	}

	public IAst getLinkTarget(Object oNode, IParseController parseController) {
		IStrategoAstNode node = (IStrategoAstNode) oNode;
		IAst result = cache.get(oNode);
		if (result != null) return result == NOT_FOUND ? null : result;
		
		String resolverFunction = resolverFunctions.get(node.getConstructor());
		if (resolverFunction == null) return null;
		
		result = resolveReference(node, resolverFunction);	
		cache.put(node, result);
		return result;
	}

	private IAst resolveReference(IStrategoAstNode node, String resolverFunction) {
		IStrategoTerm input = resolver.getFactory().makeTuple(getRoot(node).getTerm(), node.getTerm(), new StrategoTermPath(node));
		try {
			boolean success = resolver.invoke(resolverFunction);
			
			if (!success) {
				Environment.logException("Unable to resolve reference " + input.toString());
				return null;
			}
		} catch (InterpreterException e) {
			Environment.logException("Unable to resolve reference " + input.toString(), e);
			return null;
		}
		
		if (resolver.current() instanceof WrappedAstNode) {
			return ((WrappedAstNode) resolver.current()).getNode();
		} else {
			Environment.logException("Resolved reference is not associated with an AST node " + resolver.current());
			return null;
		}
	}
	
	private static IStrategoAstNode getRoot(IStrategoAstNode node) {
		while (node.getParent() != null)
			node = node.getParent();
		return node;
	}

	public String getLinkText(Object node) {
		// TODO Auto-generated method stub
		return null;
	}

}
