package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.parser.IParseController;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public interface IDynamicLanguageService extends ILanguageService {

	void initialize(IParseController controller);

	void prepareForReinitialize();
	
	/**
	 * Uninitializes the reference to the class that implements this service,
	 * ensuring it is reinitialized on use, using the given new Descriptor.
	 */
	void reinitialize(Descriptor newDescriptor) throws BadDescriptorException;
}
