package org.strategoxt.imp.runtime.dynamicloading;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.services.IReferenceResolver;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.services.StrategoReferenceResolver;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
class ReferenceResolverFactory {
	
	// TODO: Allow a Java-based reference resolver?
	//       (possibly using reflection)
	
	/**
	 * @see Descriptor#getService(Class)
	 */
	public static IReferenceResolver create(Descriptor descriptor, IStrategoAppl descriptorFile) throws BadDescriptorException {
		Map<String, String> resolverFunctions = new HashMap<String, String>();
		Map<String, String> helpFunctions = new HashMap<String, String>();
		
		for (IStrategoAppl rule : collectTerms(descriptorFile, "ReferenceRule")) {
			resolverFunctions.put(termContents(termAt(rule, 0)), termContents(termAt(rule, 1)));
			helpFunctions.put(termContents(termAt(rule, 0)), termContents(termAt(rule, 2)));
		}
		
		Interpreter resolver;
		try {
			resolver = Environment.createInterpreter();
		} catch (Exception e) {
			Environment.logException("Could not create interpreter", e);
			return null;
		}

		descriptor.addCompilerProviders(resolver);
		
		return new StrategoReferenceResolver(descriptor, resolver, resolverFunctions, helpFunctions);
	}
}
