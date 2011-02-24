/**
 * 
 */
package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicOnSaveService extends AbstractService<IOnSaveService> implements IDocumentListener {

	public DynamicOnSaveService() {
		super(IOnSaveService.class);
	}

	public void documentAboutToBeChanged(DocumentEvent event) {
		getWrapped().documentAboutToBeChanged(event);
	}

	public void documentChanged(DocumentEvent event) {
		getWrapped().documentChanged(event);
	}

}
