/**
 * 
 */
package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.strategoxt.imp.runtime.services.OnSaveService;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicOnSaveService extends AbstractService<OnSaveService> implements IDocumentListener {

	public DynamicOnSaveService() {
		super(OnSaveService.class);
	}

	public void documentAboutToBeChanged(DocumentEvent event) {
		getWrapped().documentAboutToBeChanged(event);
	}

	public void documentChanged(DocumentEvent event) {
		getWrapped().documentChanged(event);
	}

}
