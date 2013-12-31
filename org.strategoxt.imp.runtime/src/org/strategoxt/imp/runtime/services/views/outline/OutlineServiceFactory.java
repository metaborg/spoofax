package org.strategoxt.imp.runtime.services.views.outline;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.findTerm;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.dynamicloading.AbstractServiceFactory;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * @author Oskar van Rest
 */
public class OutlineServiceFactory extends AbstractServiceFactory<IOutlineService> {

	public OutlineServiceFactory() {
		super(IOutlineService.class, false);
	}
	
	@Override
	public IOutlineService create(Descriptor descriptor, SGLRParseController controller) throws BadDescriptorException {
		return new OutlineService(getOutlineRule(descriptor), getExpandToLevel(descriptor), controller);
	}
	
	public static String getOutlineRule(Descriptor descriptor) {
		IStrategoTerm outliner = findTerm(descriptor.getDocument(), "OutlineView");
		if (outliner != null) {
			return termContents(outliner.getAllSubterms()[0]);
		}
		return "outline"; // for backwards compatibility
	}
	
	public static int getExpandToLevel(Descriptor descriptor) {
		String level = termContents(findTerm(descriptor.getDocument(), "ExpandToLevel"));
		if (level != null) {
			return Integer.parseInt(level);
		}
		return 3; // default
	}
}
