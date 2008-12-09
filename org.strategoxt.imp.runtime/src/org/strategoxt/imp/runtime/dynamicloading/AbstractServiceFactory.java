package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.language.ILanguageService;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public abstract class AbstractServiceFactory<T extends ILanguageService> {
	
	/**
	 * Create a new service for a descriptor. 
	 * 
	 * @return The new service, or <code>null</code> if no service could be created. 
	 */
	public abstract T create(Descriptor descriptor) throws BadDescriptorException;
	
	public abstract Class<T> getCreatedType();
	
	/**
	 * @return <code>true</code> if this factory can create classes of the given type. 
	 */
	public boolean canCreate(Class<T> c) {
		return getCreatedType().isAssignableFrom(c);
	}
}
