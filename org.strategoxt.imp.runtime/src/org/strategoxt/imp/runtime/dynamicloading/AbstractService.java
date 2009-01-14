package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.editor.LanguageServiceManager;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.strategoxt.imp.runtime.Environment;

/**
 * Dynamic service proxy base class: implements an editor service
 * for the "Root" language, from which all dynamically loaded languages
 * inherit. The proxy class then allows it to be specialized for
 * the specific language on demand.  
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AbstractService<T extends ILanguageService> {
	private final Class<T> serviceType;
	
	private T wrapped;
	
	private Throwable notLoadingCause;
	
	private Language language;
	
	protected AbstractService(Class<T> serviceType) {
		this.serviceType = serviceType;
	}
	
	protected synchronized T getWrapped() {
		// TODO: Perhaps get the dynamic service using an approach similar to DynamicParseController.findLanguage()
		if (wrapped == null) {
			if (notLoadingCause != null) // previous error
				throw new RuntimeException(notLoadingCause);
			if (!isInitialized())
				throw new IllegalStateException("Editor service component not initialized yet - " + getClass().getSimpleName());
			try {
				wrapped = Environment.getDescriptor(getLanguage()).createService(serviceType);
			} catch (Exception e) {
				setNotLoadingCause(e);
				Environment.logException("Unable to dynamically initialize service of type " + serviceType.getSimpleName(), e);
				throw new RuntimeException(e);
			}
		}
		return wrapped;
	}
	
	protected void setNotLoadingCause(Throwable notLoadingCause) {
		this.notLoadingCause = notLoadingCause;
	}
	
	protected void setWrapped(T wrapped) {
		this.wrapped = wrapped;
	}
	
	public Language getLanguage() {
		return language;
	}
	
	public boolean isInitialized() {
		return language != null;
	}
	
	public void initialize(LanguageServiceManager manager) {
		this.language = manager.getLanguage();
		getWrapped().initialize(manager);
		Environment.getDescriptor(language).addInitializedService(this);
	}
	
	/**
	 * Uninitializes the reference to the class that implements this service,
	 * ensuring it is reinitialized on use.
	 */
	public void uninitialize() {
		wrapped = null;
		// UNDONE: language = null;
	}
}
