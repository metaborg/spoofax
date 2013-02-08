package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.services.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * @author Guido Wachsmuth <G.H.Wachsmuth add tudelft.nl>
 */
public class DynamicLabelProvider extends AbstractService<ILabelProvider> implements ILabelProvider {
	
	public DynamicLabelProvider() {
		super(ILabelProvider.class);
	}

	public Image getImage(Object element) {
		if (!isInitialized())
			return null; 
		
		return getWrapped().getImage(element);
	}

	public String getText(Object element) {
		if (!isInitialized())
			return "foo"; 
		
		return getWrapped().getText(element);
	}
	
	public boolean isLabelProperty(Object element, String property) {
		if (!isInitialized())
			return true; 

		return getWrapped().isLabelProperty(element, property);
	}

	public void addListener(ILabelProviderListener listener) {
		if (isInitialized())
			getWrapped().addListener(listener);
	}

	public void removeListener(ILabelProviderListener listener) {
		if (isInitialized())
			getWrapped().removeListener(listener);
	}

	public void dispose() {
		if (isInitialized())
			getWrapped().dispose();
	}

}
