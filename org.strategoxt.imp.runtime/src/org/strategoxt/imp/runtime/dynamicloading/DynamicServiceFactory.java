package org.strategoxt.imp.runtime.dynamicloading;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.parser.IParseController;
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
	
	private Language language;
	
	private final Map<Class, Object> services = new HashMap<Class, Object>();
	
	public DynamicServiceFactory(Descriptor descriptor) {
		this.descriptor = descriptor;
		descriptorFile = descriptor.getDocument();
	}
	
	// LOADING SERVICES
	
	public<T extends ILanguageService> T getService(Class<T> type) throws BadDescriptorException {
		Object result = services.get(type);
		if (result != null) return type.cast(result);
		
		if (IParseController.class.isAssignableFrom(type)) {
			ILanguageSyntaxProperties syntaxProperties = getService(ILanguageSyntaxProperties.class);
			result = new SGLRParseController(getLanguage(), syntaxProperties, getStartSymbol());

		} else if (ITokenColorer.class.isAssignableFrom(type)) {
			result = TokenColorerFactory.create(descriptorFile);
		
		} else if (IReferenceResolver.class.isAssignableFrom(type)) {
			result = ReferenceResolverFactory.create(descriptor, descriptorFile);
		
		} else if (ILanguageSyntaxProperties.class.isAssignableFrom(type)) {
			result = SyntaxPropertiesFactory.create(descriptorFile);
		
		} else {
			throw new IllegalArgumentException(type.getSimpleName() + " is not a supported editor service type");
		}
		
		services.put(type, result);
		return type.cast(result);
	}
	
	// PUBLIC PROPERTIES
	
	/**
	 * Gets the language for this descriptor, but does not register it.
	 */
	public Language getLanguage() throws BadDescriptorException {
		if (language == null)
			language = new Language(
				getProperty("LanguageName"),
				getProperty("LanguageId", getProperty("LanguageName")), // natureId
				getProperty("Description", ""),
				Descriptor.ROOT_LANGUAGE,
				getProperty("URL", ""),
				getProperty("Extensions"),
				getProperty("Aliases", ""),
				null);
		return language;
	}
	
	public String getStartSymbol() {
		return getProperty("StartSymbol", null);
	}
	
	public InputStream openTableStream() throws BadDescriptorException {
		String file = getProperty("Table", getProperty("Name"));
		if (!file.endsWith(".tbl")) file += ".tbl";
		return openAttachment(file);
	}
	
	public InputStream openProviderStream() throws BadDescriptorException {
		String file = getProperty("ReferenceProvider");
		if (!file.endsWith(".ctree")) file += ".ctree";
		return openAttachment(file);
	}

	private InputStream openAttachment(String path) throws BadDescriptorException {
		try {
			return new FileInputStream(path);
		} catch (FileNotFoundException e) {
			throw new BadDescriptorException(e);
		}
	}
	
	// INTERPRETING
	
	private String getProperty(String name) throws BadDescriptorException {
		String result = getProperty(name, null);
		if (result == null) throw new BadDescriptorException("Property " + name + " not specified");		
		return result;
	}
	
	private String getProperty(String name, String defaultValue) {
		IStrategoAppl result = findTerm(descriptorFile, name);
		if (result == null) return defaultValue;

		if (termAt(result, 0) instanceof IStrategoAppl &&
				cons((IStrategoAppl) termAt(result, 0)).equals("Values")) {
			return concatTermStrings(termAt(result, 0));
		} else {
			return termContents(result);
		}
	}
}
