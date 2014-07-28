package org.strategoxt.imp.runtime.editor;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.views.properties.IPropertySource;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.services.views.properties.StrategoTermPropertySource;

/**
 * @author Oskar van Rest
 */
public class StrategoTermAdapterFactory implements IAdapterFactory {

	@Override
	public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
		if (adapterType == IPropertySource.class) {
			IStrategoTerm properties = (IStrategoTerm) adaptableObject;
			return new StrategoTermPropertySource(properties);
		}
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IPropertySource.class };
	}

}
