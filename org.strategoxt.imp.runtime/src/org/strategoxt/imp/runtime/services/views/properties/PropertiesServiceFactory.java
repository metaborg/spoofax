package org.strategoxt.imp.runtime.services.views.properties;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.cons;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.findTerm;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.dynamicloading.AbstractServiceFactory;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * @author Oskar van Rest
 */
public class PropertiesServiceFactory extends AbstractServiceFactory<IPropertiesService> {

	public PropertiesServiceFactory() {
		super(IPropertiesService.class, true);
	}
	
	@Override
	public IPropertiesService create(Descriptor descriptor, SGLRParseController controller) throws BadDescriptorException {
		IStrategoTerm propertiesViewDescription = findTerm(descriptor.getDocument(), "PropertiesView");
		
		String propertiesRule = null;
		boolean source = false;
		
		if (propertiesViewDescription != null) {
			propertiesRule = termContents(termAt(propertiesViewDescription, 0));
			IStrategoList options = termAt(propertiesViewDescription, 1);
			
			for (IStrategoTerm option : options.getAllSubterms()) {
				String type = cons(option);
				if (type.equals("Source")) {
					source = true;
				}
			}
		}
		
		return new PropertiesService(propertiesRule, source, controller);
	}
}
