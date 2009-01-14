package org.strategoxt.imp.runtime.services;

import org.eclipse.imp.editor.LanguageServiceManager;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IDocumentationProvider;
import org.eclipse.imp.services.IReferenceResolver;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DocumentationProvider implements IDocumentationProvider {
	
	private IReferenceResolver references;
	
	public void initialize(LanguageServiceManager manager) {
		references = manager.getResolver();
	}

	public String getDocumentation(Object target, IParseController parseController) {
		return references.getLinkText(target);
	}

}
