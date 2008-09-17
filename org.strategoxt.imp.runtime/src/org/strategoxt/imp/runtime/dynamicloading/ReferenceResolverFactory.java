package org.strategoxt.imp.runtime.dynamicloading;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.services.IReferenceResolver;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.services.StrategoFeedback;
import org.strategoxt.imp.runtime.services.StrategoReferenceResolver;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ReferenceResolverFactory {
	
	// TODO: Allow a Java-based reference resolver?
	//       (possibly using reflection)
	
	/**
	 * @see Descriptor#getService(Class)
	 */
	public static IReferenceResolver create(Descriptor descriptor) throws BadDescriptorException {
		IStrategoAppl descriptorFile = descriptor.getDocument();
		StrategoFeedback feedback = descriptor.getStrategoFeedback();
		
		Map<String, String> resolverFunctions = new HashMap<String, String>();
		Map<String, String> helpFunctions = new HashMap<String, String>();
		
		for (IStrategoAppl rule : collectTerms(descriptorFile, "ReferenceRule")) {
			resolverFunctions.put(termContents(termAt(rule, 0)), termContents(termAt(rule, 1)));
			helpFunctions.put(termContents(termAt(rule, 0)), termContents(termAt(rule, 2)));
		}
		
		return new StrategoReferenceResolver(feedback, resolverFunctions, helpFunctions);
	}
}
