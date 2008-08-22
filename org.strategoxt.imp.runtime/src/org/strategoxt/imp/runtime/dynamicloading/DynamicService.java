package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.strategoxt.imp.runtime.Environment;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicService<T extends ILanguageService> {
	private final Class<T> serviceType;
	
	private T wrapped;
	
	private Throwable notLoadingCause;
	
	private Language language;
	
	protected DynamicService(Class<T> serviceType) {
		this.serviceType = serviceType;
	}
	
	protected T getWrapped() {
		if (wrapped == null) {
			if (getNotLoadingCause() != null)
				throw new RuntimeException(getNotLoadingCause());
			if (!isInitialized())
				throw new IllegalStateException("Editor service component not initialized yet - " + getClass().getSimpleName());
			try {
				wrapped = Environment.getDescriptor(getLanguage()).getService(serviceType);
			} catch (Exception e) {
				setNotLoadingCause(e);
				throw new RuntimeException(e);
			}
		}
		return wrapped;
	}
	
	private Throwable getNotLoadingCause() {
		return notLoadingCause;
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
	
	protected void initialize(Language language) {
		this.language = language;
	}
}
