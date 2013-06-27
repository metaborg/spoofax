package org.strategoxt.imp.runtime.services;

import org.eclipse.imp.services.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * @author Oskar van Rest
 */
public class LabelProviderSemantic implements ILabelProvider {

	private final LabelProvider labelProvider = new LabelProvider();
	
	public Image getImage(Object element) {
		return null;
	}

	public String getText(Object element) {
		return labelProvider.getText(element);
	}

	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	public void addListener(ILabelProviderListener listener) {
		// Do nothing
	}

	public void removeListener(ILabelProviderListener listener) {
		// Do nothing
	}

	public void dispose() {
		// Do nothing
	}

}
