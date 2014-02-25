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
		System.out.println("hello");
		System.out.println(adaptableObject.getClass().getName());
//		if (adapterType == IPropertySource.class) {
//			// TODO: obtain properties model
//			return new StrategoTermPropertySource((IStrategoTerm) adaptableObject);
//		}
		return null;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IPropertySource.class };
	}

}
