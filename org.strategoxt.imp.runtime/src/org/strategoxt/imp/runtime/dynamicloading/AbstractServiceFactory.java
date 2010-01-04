package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.language.ILanguageService;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public abstract class AbstractServiceFactory<T extends ILanguageService> {
	
	private final Class<T> serviceType;
	
	private final boolean isCached;

	/**
	 * @param isCached
	 *            Determines whether this service should be cached. In most
	 *            cases, cached services should be thread-safe.
	 */
	public AbstractServiceFactory(Class<T> serviceType, boolean isCached) {
		this.serviceType = serviceType;
		this.isCached = isCached;
	}

	public AbstractServiceFactory(Class<T> serviceType) {
		this(serviceType, false);
	}
	
	/**
	 * Create a new service for a descriptor. 
	 * 
	 * @return The new service, or <code>null</code> if no service could be created.
	 *  
	 * @see Descriptor#createService(Class, SGLRParseController)
	 */
	public abstract T create(Descriptor descriptor, SGLRParseController controller) throws BadDescriptorException;
	
	public boolean isCached() {
		return isCached;
	}
	
	/**
	 * @return <code>true</code> if this factory can create classes of the given type. 
	 */
	public boolean canCreate(Class<T> c) {
		return serviceType.isAssignableFrom(c);
	}
}
