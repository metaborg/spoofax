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
import org.spoofax.jsglr.SGLRException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.parser.SimpleSGLRParser;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 * 
 * @see DescriptorFactory#load(InputStream)
 */
public class Descriptor {
	public static final String ROOT_LANGUAGE = "Root";
	
	private static final Language LANGUAGE =
		new Language("EditorService-builtin", "org.strategoxt.imp.builtin.editorservice", "", "Root", "", "", "", null);
	
	private static final SimpleSGLRParser parser;
	
	private final IStrategoAppl document;
	
	private Language language;
	
	private final Map<Class, Object> services = new HashMap<Class, Object>();
	
	// LOADING DESCRIPTOR 
	
	static {
		try {
			InputStream stream = Descriptor.class.getResourceAsStream("/syntax/EditorService.tbl");
			Environment.registerParseTable(LANGUAGE, stream);
			parser = new SimpleSGLRParser(Environment.getParseTable(LANGUAGE), "Module");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Descriptor(IStrategoAppl document) {
		this.document = document;
	}
	
	public static Descriptor load(InputStream file) throws BadDescriptorException {
		try {
			IStrategoAppl input = (IStrategoAppl) parser.parseToTerm(file);
	        return new Descriptor(input);
		} catch (SGLRException e) {
			throw new BadDescriptorException("Could not parse descriptor file", e);
		}
	}
	
	// LOADING SERVICES
	
	public<T extends ILanguageService> T getService(Class<T> type) throws BadDescriptorException {
		Object result = services.get(type);
		if (result != null) return type.cast(result);
		
		if (IParseController.class.isAssignableFrom(type)) {
			ILanguageSyntaxProperties syntaxProperties = getService(ILanguageSyntaxProperties.class);
			result = new SGLRParseController(getLanguage(), syntaxProperties, getStartSymbol());
		} else if (ITokenColorer.class.isAssignableFrom(type)) {
			result = TokenColorerFactory.create(document);
		} else if (IReferenceResolver.class.isAssignableFrom(type)) {
			result = ReferenceResolverFactory.create(document);
		} else if (ILanguageSyntaxProperties.class.isAssignableFrom(type)) {
			result = SyntaxPropertiesFactory.create(document);
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
				getProperty("Name"),
				getProperty("Name"), // natureId
				getProperty("Description", ""),
				ROOT_LANGUAGE,
				getProperty("URL", ""),
				getProperty("Extensions"),
				getProperty("Aliases", ""),
				null);
		return language;
	}
	
	public String getStartSymbol() {
		return getProperty("StartSymbol", null);
	}
	
	public InputStream getTableStream() throws BadDescriptorException {
		String file = getProperty("Table", getProperty("Name"));
		if (!file.endsWith(".tbl")) file += ".tbl";
		try {
			return new FileInputStream(file);
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
		IStrategoAppl result = findTerm(document, name);
		if (result == null) return defaultValue;

		if (termAt(result, 0) instanceof IStrategoAppl &&
				cons((IStrategoAppl) termAt(result, 0)).equals("Values")) {
			return concatTermStrings(termAt(result, 0));
		} else {
			return termContents(result);
		}
	}
}
