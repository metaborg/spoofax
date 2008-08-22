package org.strategoxt.imp.runtime.dynamicloading;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.services.IReferenceResolver;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.services.StrategoReferenceResolver;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
class ReferenceResolverFactory {
	/**
	 * @see Descriptor#getService(Class)
	 */
	public static IReferenceResolver create(IStrategoAppl descriptor) throws BadDescriptorException {
		Map<String, String> resolverFunctions = new HashMap<String, String>();
		
		for (IStrategoAppl rule : collectTerms(descriptor, "ResolverRule")) {
			resolverFunctions.put(termContents(termAt(rule, 0)), termContents(termAt(rule, 1)));
		}
		
		return new StrategoReferenceResolver(resolver, descriptor);
	}
}
