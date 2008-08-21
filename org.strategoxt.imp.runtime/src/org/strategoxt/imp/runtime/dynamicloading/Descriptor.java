package org.strategoxt.imp.runtime.dynamicloading;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.SGLRException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SimpleSGLRParser;
import org.strategoxt.imp.runtime.services.TokenColorer;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class Descriptor {
	public static final String ROOT_LANGUAGE = "Root";
	
	private static final Language LANGUAGE =
		new Language("EditorService-builtin", "org.strategoxt.imp.builtin.editorservice", "", "Root", "", "", "", null);
	
	private static final SimpleSGLRParser parser;
	
	private final IStrategoAppl descriptor;
	
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
	
	private Descriptor(IStrategoAppl descriptor) {
		this.descriptor = descriptor;
	}
	
	public static Descriptor load(InputStream file) throws BadDescriptorException {
		try {
			IStrategoAppl input = (IStrategoAppl) parser.parseToTerm(file);
	        return new Descriptor(input);
		} catch (SGLRException e) {
			throw new BadDescriptorException("Could not parse descriptor file", e);
		}
	}
	
	// PUBLIC PROPERTIES
	
	/**
	 * Gets the language for this descriptor, but does not register it.
	 * 
	 * @see LanguageLoader#load(InputStream, boolean)
	 */
	public Language toLanguage() throws BadDescriptorException {
		return new Language(
				getProperty("Name"),
				getProperty("Name"), // natureId
				getProperty("Description", ""),
				ROOT_LANGUAGE,
				getProperty("URL", ""),
				getProperty("Extensions"),
				getProperty("Aliases", ""),
				null);
	}
	
	public void configureColorer(TokenColorer colorer) throws BadDescriptorException {
		new TokenColorerLoader(descriptor).configureColorer(colorer);
	}
	
	public String getStartSymbol() {
		return getProperty("StartSymbol", null);
	}
	
	public String getName() throws BadDescriptorException {
		return getProperty("Name");
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
	
	// PARSING
	
	private String getProperty(String name) throws BadDescriptorException {
		String result = getProperty(name, null);
		if (result == null) throw new BadDescriptorException("Property " + name + " not specified");		
		return result;
	}
	
	private String getProperty(String name, String defaultValue) {
		IStrategoAppl result = findTerm(descriptor, name);
		if (result == null) return defaultValue;

		if (termAt(result, 0) instanceof IStrategoAppl &&
				cons((IStrategoAppl) termAt(result, 0)).equals("Values")) {
			return concatValues(result);
		} else {
			return termContents(result);
		}
	}

	private static String concatValues(IStrategoAppl values) {
		IStrategoTerm list = termAt(termAt(values, 0), 0);
		StringBuilder results = new StringBuilder();
		
		if (list.getSubtermCount() > 0)
			results.append(termContents(termAt(list, 0)));
		
		for (int i = 1; i <  list.getSubtermCount(); i++) {
			results.append(',');
			results.append(termContents(termAt(list, i)));
		}
		return results.toString();
	}
}
