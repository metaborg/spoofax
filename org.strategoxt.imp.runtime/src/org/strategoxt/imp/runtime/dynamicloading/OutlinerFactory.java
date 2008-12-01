package org.strategoxt.imp.runtime.dynamicloading;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.imp.services.IOutliner;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.services.NodeMapping;
import org.strategoxt.imp.runtime.services.Outliner;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
class OutlinerFactory {

	/**
	 * @see Descriptor#getService(Class)
	 */
	public static IOutliner create(IStrategoAppl descriptor) throws BadDescriptorException {
		// TODO: "FoldAll" folding rules
		
		List<NodeMapping> outlined = new ArrayList<NodeMapping>(); 
		Object outlineme = new Object();
		
		for (IStrategoAppl rule : collectTerms(descriptor, "OutlineRule")) {
			outlined.add(NodeMapping.create(rule, outlineme));
		}
		
		return new Outliner(outlined);
	}
}
