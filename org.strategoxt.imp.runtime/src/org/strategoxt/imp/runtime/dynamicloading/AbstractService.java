package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.parser.IParseController;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * Dynamic service proxy base class: implements an editor service
 * for the "Root" language, from which all dynamically loaded languages
 * inherit. The proxy class then allows it to be specialized for
 * the specific language on demand.  
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AbstractService<T extends ILanguageService> implements IDynamicLanguageService {

	private final Class<T> serviceType;
	
	private T wrapped;
	
	private Throwable notLoadingCause;
	
	private Language language;
	
	private SGLRParseController parseController;
	
	public AbstractService(Class<T> serviceType) {
		this.serviceType = serviceType;
	}
	
	public T getWrapped() {
		// TODO: Perhaps get the dynamic service using an approach similar to DynamicParseController.findLanguage()
		if (wrapped == null) {
			if (notLoadingCause != null) // previous error
				throw new RuntimeException(notLoadingCause);
			if (!isInitialized())
				throw new IllegalStateException("Editor service component not initialized yet - " + getClass().getSimpleName() + "/" + language);
			try {
				Descriptor desc = Environment.getDescriptor(getLanguage());
				if(desc == null)
					throw new IllegalStateException("Language '" + getLanguage().getName() + "' not registered");
				wrapped = desc.createService(serviceType, parseController);
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
	
	SGLRParseController internalGetParseController() {
		return parseController;
	}
	
	public boolean isInitialized() {
		return language != null && Environment.getDescriptor(language) != null;
	}
	
	public final void initialize(IParseController controller) {
		initialize(controller, controller.getLanguage());
	}

	protected void initialize(IParseController controller, Language language) {
		// (Thrown exceptions are shown directly in the editor view.)
		this.language = language;
		if (controller instanceof DynamicParseController)
			this.parseController = (SGLRParseController) ((DynamicParseController) controller).getWrapped();
		else if (controller instanceof SGLRParseController)
			this.parseController = (SGLRParseController) controller;
		if (getWrapped() == null) // (trigger descriptor init)
			throw new RuntimeException("Failed to initialize language " + language.getName());
		if (!this.language.getName().equals(language.getName()))
			throw new RuntimeException("Failed to initialize language " + this.language.getName() + ": language name in plugin.xml (" + language.getName() + ") does not correspond to name in editor service descriptors (" + language.getName() + ")");
		Descriptor descriptor = Environment.getDescriptor(language);
		if (descriptor == null)
			throw new RuntimeException("No definition for language '" + language.getName() + "'; try re-opening the editor");
		descriptor.addActiveService(this);
	}
	
	public void reinitialize(Descriptor newDescriptor) throws BadDescriptorException {
		wrapped = null;
		language = newDescriptor.getLanguage();
	}
	
	public void prepareForReinitialize() {
		// By default, does nothing
	}
}
