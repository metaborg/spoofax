package org.strategoxt.imp.runtime.services;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IHoverHelper;
import org.eclipse.imp.services.IReferenceResolver;
import org.eclipse.imp.services.base.HoverHelperBase;
import org.eclipse.jface.text.source.ISourceViewer;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.dynamicloading.DynamicParseController;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class HoverHelper extends HoverHelperBase implements IHoverHelper {

	IReferenceResolver references;
	
	public String getHoverHelpAt(IParseController parseController, ISourceViewer viewer, int offset) {
		if (references == null) {
			DynamicParseController controller = (DynamicParseController) parseController;
			Descriptor descriptor = Environment.getDescriptor(controller.getLanguage());
			try {
				references = descriptor.getService(IReferenceResolver.class);
			} catch (BadDescriptorException e) {
				Environment.logException("Could not load reference resolver", e);
				return null;
			}
		}
		
		Object ast = parseController.getCurrentAst();
		Object node = parseController.getNodeLocator().findNode(ast, offset);
		
		return references.getLinkText(node);
	}

}
