package org.strategoxt.imp.runtime.dynamicloading;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.jsglr.SGLRException;
import org.strategoxt.imp.runtime.Environment;
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
	
	private final DynamicServiceFactory serviceFactory;
	
	private final IStrategoAppl document;
	
	private Language language;
	
	public IStrategoAppl getDocument() {
		return document;
	}
	
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
		serviceFactory = new DynamicServiceFactory(this);
	}
	
	protected static Descriptor load(InputStream file) throws BadDescriptorException {
		try {
			IStrategoAppl input = (IStrategoAppl) parser.parseImplode(file);
	        return new Descriptor(input);
		} catch (SGLRException e) {
			throw new BadDescriptorException("Could not parse descriptor file", e);
		}
	}
	
	// LOADING SERVICES
	
	public<T extends ILanguageService> T getService(Class<T> type) throws BadDescriptorException {
		return serviceFactory.getService(type);
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
				ROOT_LANGUAGE,
				getProperty("URL", ""),
				getProperty("Extensions"),
				getProperty("Aliases", ""),
				null);
		return language;
	}
	
	public String getStartSymbols() {
		return getProperty("StartSymbols", null);
	}
	
	public InputStream openTableStream() throws BadDescriptorException {
		String file = getProperty("Table", getProperty("LanguageName"));
		if (!file.endsWith(".tbl")) file += ".tbl";
		return openAttachment(file);
	}
	
	public InputStream openProviderStream() throws BadDescriptorException {
		String file = getProperty("CompilerProvider");
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
