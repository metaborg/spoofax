package org.strategoxt.imp.runtime.services;

import java.util.Map;
import java.util.WeakHashMap;

import lpg.runtime.IAst;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IReferenceResolver;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.parser.ast.IntAstNode;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoReferenceResolver implements IReferenceResolver {
	private final StrategoFeedback feedback;
	
	private final Map<String, String> resolverFunctions;
	
	private final Map<String, String> helpFunctions;
	
	private final String wildcardResolverFunction;
	
	private final String wildcardHelperFunction;
	
	private final WeakHashMap<IStrategoAstNode, IAst> resolverCache =
		new WeakHashMap<IStrategoAstNode, IAst>();
	
	private final WeakHashMap<IStrategoAstNode, String> helpCache =
		new WeakHashMap<IStrategoAstNode, String>();
	
	private final IStrategoAstNode NOT_FOUND = new IntAstNode(null, 0, null, null) {};
	
	public StrategoReferenceResolver(StrategoFeedback feedback, Map<String, String> resolverFunctions, Map<String, String> helpFunctions) {
		this.feedback = feedback;
		this.resolverFunctions = resolverFunctions;
		this.helpFunctions = helpFunctions;
		wildcardResolverFunction = resolverFunctions.get("_");
		wildcardHelperFunction = resolverFunctions.get("_");
	}

	public IAst getLinkTarget(Object oNode, IParseController parseController) {
		IStrategoAstNode node = getReferencedNode(oNode);
		IAst result = resolverCache.get(node);
		if (result != null) return result == NOT_FOUND ? null : result;
		
		String function = resolverFunctions.get(node.getConstructor());
		if (function == null) function = wildcardResolverFunction;
		if (function == null || function.equals("_")) {
			Debug.log("No reference resolver available for node of type ", node.getConstructor());
			return null;
		}
		
		IStrategoTerm resultTerm = feedback.invoke(function, node);
		result = feedback.getAstNode(resultTerm);
		
		resolverCache.put(node, result == null ? NOT_FOUND : result);
		return result;
	}

	public String getLinkText(Object oNode) {
		IStrategoAstNode node = getReferencedNode(oNode);
		String cached = helpCache.get(node);
		if (cached != null) return cached == "" ? null : cached;
		
		String function = helpFunctions.get(node.getConstructor());
		if (function == null) function = wildcardHelperFunction;
		if (function == null || function.equals("_"))  {
			Debug.log("No reference info available for node of type ", node.getConstructor());
			return null;
		}
		
		IStrategoTerm resultTerm = feedback.invoke(function, node);
		String result = resultTerm == null ? null : resultTerm.toString();
		
		helpCache.put(node, result == null ? "" : result.toString());
		return result;
	}
	
	private static final IStrategoAstNode getReferencedNode(Object oNode) {
		IStrategoAstNode result = (IStrategoAstNode) oNode;
		while (result != null && result.getConstructor() == null)
			result = result.getParent();
		return result;
	}
}
