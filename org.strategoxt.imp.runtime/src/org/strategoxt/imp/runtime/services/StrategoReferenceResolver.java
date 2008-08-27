package org.strategoxt.imp.runtime.services;

import java.io.FileNotFoundException;
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
	
	private final Map<String, String> helpFunctions;
	
	private final WeakHashMap<IStrategoAstNode, IAst> resolverCache =
		new WeakHashMap<IStrategoAstNode, IAst>();
	
	private final WeakHashMap<IStrategoAstNode, String> helpCache =
		new WeakHashMap<IStrategoAstNode, String>();
	
	private final IStrategoAstNode NOT_FOUND = new IntAstNode(null, 0, null, null) {};
	
	public StrategoReferenceResolver(Interpreter resolver, Map<String, String> resolverFunctions, Map<String, String> helpFunctions) {
		this.resolver = resolver;
		this.resolverFunctions = resolverFunctions;
		this.helpFunctions = helpFunctions;
	}

	public IAst getLinkTarget(Object oNode, IParseController parseController) {
		IStrategoAstNode node = (IStrategoAstNode) oNode;
		IAst result = resolverCache.get(oNode);
		if (result != null) return result == NOT_FOUND ? null : result;
		
		String function = resolverFunctions.get(node.getConstructor());
		if (function == null) return null;
		
		IStrategoTerm resultTerm = strategoCall(node, function);
		result = resultTerm == null ? null : ((WrappedAstNode) resultTerm).getNode();
		
		resolverCache.put(node, result == null ? NOT_FOUND : result);
		return result;
	}

	public String getLinkText(Object oNode) {
		IStrategoAstNode node = (IStrategoAstNode) oNode;
		String result = helpCache.get(oNode);
		if (result != null) return result == "" ? null : result;
		
		String function = helpFunctions.get(node.getConstructor());
		if (function == null) return null;
		
		result = strategoCall(node, function).toString();	
		helpCache.put(node, result == null ? "" : result);
		return result;
	}

	private IStrategoTerm strategoCall(IStrategoAstNode node, String function) {
		IStrategoTerm path = resolver.getFactory().makeString(node.getResourcePath().toOSString());
		IStrategoTerm[] inputParts = { getRoot(node).getTerm(), path, node.getTerm(), new StrategoTermPath(node) };
		IStrategoTerm input = resolver.getFactory().makeTuple(inputParts);

		try {
			initResolver(node);

			boolean success = resolver.invoke(function);
			
			if (!success) {
				Environment.logException("Unable to resolve reference " + input.toString());
				return null;
			}
		} catch (InterpreterException e) {
			Environment.logException("Unable to resolve reference " + input.toString(), e);
			return null;
		}
		
		if (resolver.current() instanceof WrappedAstNode) {
			return resolver.current();
		} else {
			Environment.logException("Resolved reference is not associated with an AST node " + resolver.current());
			return null;
		}
	}
	
	private void initResolver(IStrategoAstNode node) {
		resolver.reset();
		try {
			String workingDir = node.getRootPath().toOSString();
			resolver.setWorkingDir(workingDir);
		} catch (FileNotFoundException e) {
			Environment.logException("Could not set Stratego working directory", e);
			throw new RuntimeException(e);
		}
	}
	
	private static IStrategoAstNode getRoot(IStrategoAstNode node) {
		while (node.getParent() != null)
			node = node.getParent();
		return node;
	}

}
