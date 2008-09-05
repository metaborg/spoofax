package org.strategoxt.imp.runtime.dynamicloading;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.imp.services.IFoldingUpdater;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.services.FoldingUpdater;
import org.strategoxt.imp.runtime.services.NodeMapping;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
class FoldingUpdaterFactory {

	/**
	 * @see Descriptor#getService(Class)
	 */
	public static IFoldingUpdater create(IStrategoAppl descriptor) throws BadDescriptorException {
		// TODO: "FoldAll" folding rules
		
		List<NodeMapping> folded = new ArrayList<NodeMapping>(); 
		
		for (IStrategoAppl folding : collectTerms(descriptor, "Folding")) {
			folded.add(NodeMapping.create(folding, null));
		}
		
		FoldingUpdater result = new FoldingUpdater(folded);
		return result;
	}
}
