package org.strategoxt.imp.runtime.services;

import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.ServiceFactory;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IDocumentationProvider;
import org.eclipse.imp.services.IReferenceResolver;
import org.strategoxt.imp.runtime.dynamicloading.DynamicService;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DocumentationProvider implements IDocumentationProvider {
	
	private IReferenceResolver references;
	
	private void initialize(IParseController parseController) {
		if (references != null) return;
		
		Language language = parseController.getLanguage();
		references = ServiceFactory.getInstance().getReferenceResolver(language);
		((DynamicService) references).initialize(language);
	}

	public String getDocumentation(Object target, IParseController parseController) {
		initialize(parseController);
		
		return references.getLinkText(target);
	}

}
