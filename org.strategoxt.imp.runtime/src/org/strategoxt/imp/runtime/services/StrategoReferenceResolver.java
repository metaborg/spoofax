package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.isTermString;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getSort;
import static org.spoofax.terms.Term.tryGetName;

import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IReferenceResolver;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Debug;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoReferenceResolver implements IReferenceResolver {
	
	public static boolean ALLOW_MULTI_CHILD_PARENT = false;
	
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

	public ISimpleTerm getLinkTarget(Object oNode, IParseController parseController) {
		IStrategoTerm innerNode = (IStrategoTerm) oNode;
		IStrategoTerm node = InputTermBuilder.getMatchingAncestor(innerNode, ALLOW_MULTI_CHILD_PARENT);
		
		String function = NodeMapping.getFirstAttribute(resolverFunctions, tryGetName(node), getSort(node), 0);
		if (function == null) function = wildcardResolverFunction;
		if (function == null || function.equals("_")) {
			Debug.log("No reference resolver available for node of type ", tryGetName(node));
			return null;
		}
		
		observer.getLock().lock();
		try {
			if (!observer.isUpdateScheduled()) {
				observer.update(parseController, new NullProgressMonitor());
			}
			IStrategoTerm resultTerm = observer.invokeSilent(function, node);
			if (resultTerm == null) resultTerm = observer.invokeSilent(function, innerNode);
			return observer.getAstNode(resultTerm, true);
		} finally {
			observer.getLock().unlock();
		}
	}

	public String getLinkText(Object oNode) {
		IStrategoTerm innerNode = (IStrategoTerm) oNode;
		IStrategoTerm node = InputTermBuilder.getMatchingAncestor(innerNode, ALLOW_MULTI_CHILD_PARENT);
		if (node == null)
			return null;
		
		String function = NodeMapping.getFirstAttribute(helpFunctions, tryGetName(node), null, 0);
		if (function == null) function = wildcardHelperFunction;
		if (function == null || function.equals("_"))  {
			Debug.log("No hover help available for node of type ", tryGetName(node));
			return null;
		}
		
		observer.getLock().lock();
		try {
			IStrategoTerm result = observer.invokeSilent(function, node);
			if (result == null) result = observer.invokeSilent(function, innerNode);
			if (result == null) {
				return null;
			} else if (isTermString(result)) {
				return ((IStrategoString) result).stringValue();
			} else {
				return result.toString();
			}
		} finally {
			observer.getLock().unlock();
		}
	}
}
