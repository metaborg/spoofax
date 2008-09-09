package org.strategoxt.imp.runtime.dynamicloading;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IFoldingUpdater;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.imp.services.IReferenceResolver;
import org.eclipse.imp.services.ITokenColorer;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicServiceFactory {
	private final Descriptor descriptor;
	
	private final IStrategoAppl descriptorFile;
	
	private final Map<Class, Object> services = new HashMap<Class, Object>();
	
	public DynamicServiceFactory(Descriptor descriptor) {
		this.descriptor = descriptor;
		descriptorFile = descriptor.getDocument();
	}
	
	// LOADING SERVICES
	
	public<T extends ILanguageService> T getService(Class<T> type) throws BadDescriptorException {
		Object result = services.get(type);
		if (result != null) return type.cast(result);
		
		// TODO: Dynamic registration of service factories
		//       (which should not be static)
		
		if (IParseController.class.isAssignableFrom(type)) {
			ILanguageSyntaxProperties syntaxProperties = getService(ILanguageSyntaxProperties.class);
			result = new SGLRParseController(descriptor.getLanguage(), syntaxProperties, descriptor.getStartSymbols());

		} else if (ITokenColorer.class.isAssignableFrom(type)) {
			result = TokenColorerFactory.create(descriptorFile);
		
		} else if (IReferenceResolver.class.isAssignableFrom(type)) {
			result = ReferenceResolverFactory.create(descriptor, descriptorFile);
		
		} else if (ILanguageSyntaxProperties.class.isAssignableFrom(type)) {
			result = SyntaxPropertiesFactory.create(descriptorFile);
		
		} else if (IFoldingUpdater.class.isAssignableFrom(type)) {
			result = FoldingUpdaterFactory.create(descriptorFile);
		
		} else {
			throw new IllegalArgumentException(type.getSimpleName() + " is not a supported editor service type");
		}
		
		services.put(type, result);
		return type.cast(result);
	}
	
	public void clearCache() {
		services.clear();
	}
}
