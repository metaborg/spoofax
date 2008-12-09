package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IReferenceResolver;

/**
 * Dynamic proxy class to a reference resolver.
 * 
 * @see AbstractService
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicReferenceResolver extends AbstractService<IReferenceResolver> implements IReferenceResolver {
	
	public DynamicReferenceResolver() {
		super(IReferenceResolver.class);
	}

	public Object getLinkTarget(Object node, IParseController parseController) {
		initialize(parseController.getLanguage());
		return getWrapped().getLinkTarget(node, parseController);
	}

	public String getLinkText(Object node) {
		return getWrapped().getLinkText(node);
	}

}
