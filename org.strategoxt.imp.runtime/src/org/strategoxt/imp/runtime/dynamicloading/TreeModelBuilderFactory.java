package org.strategoxt.imp.runtime.dynamicloading;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.collectTerms;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.imp.services.ILabelProvider;
import org.eclipse.imp.services.base.TreeModelBuilderBase;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.NodeMapping;
import org.strategoxt.imp.runtime.services.TreeModelBuilder;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
class TreeModelBuilderFactory extends AbstractServiceFactory<TreeModelBuilderBase> {
	
	public TreeModelBuilderFactory() {
		super(TreeModelBuilderBase.class);
	}

	@Override
	public TreeModelBuilderBase create(Descriptor d, SGLRParseController controller) throws BadDescriptorException {
		List<NodeMapping> outlined = new ArrayList<NodeMapping>(); 
		Object outlineme = new Object();
		
		for (IStrategoAppl rule : collectTerms(d.getDocument(), "OutlineRule")) {
			outlined.add(NodeMapping.create(rule, outlineme));
		}
		
		return new TreeModelBuilder(outlined, d.createService(ILabelProvider.class, controller));
	}
}
