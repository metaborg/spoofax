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
class OutlinerFactory extends AbstractServiceFactory<IOutliner> {
	
	@Override
	public Class<IOutliner> getCreatedType() {
		return IOutliner.class;
	}

	/**
	 * @see Descriptor#createService(Class)
	 */
	@Override
	public IOutliner create(Descriptor d) throws BadDescriptorException {
		// TODO: "FoldAll" folding rules
		
		List<NodeMapping> outlined = new ArrayList<NodeMapping>(); 
		Object outlineme = new Object();
		
		for (IStrategoAppl rule : collectTerms(d.getDocument(), "OutlineRule")) {
			outlined.add(NodeMapping.create(rule, outlineme));
		}
		
		return new Outliner(outlined);
	}
}
