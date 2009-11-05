package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.*;

import java.util.List;

import lpg.runtime.IAst;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IReferenceResolver;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoReferenceResolver implements IReferenceResolver {
	private final StrategoObserver observer;
	
	private final List<NodeMapping<String>> resolverFunctions;
	
	private final List<NodeMapping<String>> helpFunctions;
	
	private final String wildcardResolverFunction;
	
	private final String wildcardHelperFunction;
	
	public StrategoReferenceResolver(StrategoObserver observer, List<NodeMapping<String>> resolverFunctions, List<NodeMapping<String>> helpFunctions) {
		this.observer = observer;
		this.resolverFunctions = resolverFunctions;
		this.helpFunctions = helpFunctions;
		wildcardResolverFunction = NodeMapping.getFirstAttribute(resolverFunctions, "_", null, 0);
		wildcardHelperFunction = NodeMapping.getFirstAttribute(helpFunctions, "_", null, 0);
	}

	public IAst getLinkTarget(Object oNode, IParseController parseController) {
		IStrategoAstNode node = getReferencedNode(oNode);
		
		String function = NodeMapping.getFirstAttribute(resolverFunctions, node.getConstructor(), node.getSort(), 0);
		if (function == null) function = wildcardResolverFunction;
		if (function == null || function.equals("_")) {
			Debug.log("No reference resolver available for node of type ", node.getConstructor());
			return null;
		}
		
		IStrategoTerm resultTerm = observer.invokeSilent(function, node);
		if (resultTerm == null && !observer.isUpdateStarted())
			observer.asyncUpdate(parseController);
		return observer.getAstNode(resultTerm);
	}

	public String getLinkText(Object oNode) {
		IStrategoAstNode node = getReferencedNode(oNode);
		
		String function = NodeMapping.getFirstAttribute(helpFunctions, node.getConstructor(), null, 0);
		if (function == null) function = wildcardHelperFunction;
		if (function == null || function.equals("_"))  {
			Debug.log("No reference info available for node of type ", node.getConstructor());
			return null;
		}
		
		IStrategoTerm result = observer.invokeSilent(function, node);
		if (result == null) {
			return null;
		} else if (isTermString(result)) {
			return ((IStrategoString) result).stringValue();
		} else {
			return result.toString();
		}
	}
	
	private static final IStrategoAstNode getReferencedNode(Object oNode) {
		IStrategoAstNode result = (IStrategoAstNode) oNode;
		while (result != null && result.getConstructor() == null)
			result = result.getParent();
		return result;
	}
}
