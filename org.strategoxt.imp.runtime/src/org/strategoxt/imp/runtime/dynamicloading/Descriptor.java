package org.strategoxt.imp.runtime.dynamicloading;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.imp.language.Language;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.SGLRException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SimpleSGLRParser;
import org.strategoxt.imp.runtime.services.TokenColorer;
import org.spoofax.interpreter.core.Interpreter;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class Descriptor {
	public static final String ROOT_LANGUAGE = "Root";
	
	private static final String LANGUAGE = "EditorService";
	
	private static final SimpleSGLRParser parser;
	
	private static final Interpreter interpreter;
	
	private final IStrategoAppl descriptor;
	
	// LOADING DESCRIPTOR
	
	static {
		try {
			InputStream stream = Descriptor.class.getResourceAsStream("/syntax/EditorService.tbl");
			Environment.registerParseTable(LANGUAGE, stream);
			parser = new SimpleSGLRParser(Environment.getParseTable(LANGUAGE), "Module");
			
			interpreter = Environment.createInterpreter();
			interpreter.load(Descriptor.class.getResourceAsStream("/str/sdf2imp.ctree"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Descriptor(IStrategoAppl descriptor) {
		this.descriptor = descriptor;
	}
	
	public static Descriptor load(InputStream file) throws BadDescriptorException {
		try {
			IStrategoTerm term = parser.parseToTerm(file);
			interpreter.setCurrent(term);
			interpreter.invoke("input_descriptor_file_0_0");
			return new Descriptor((IStrategoAppl) interpreter.current());			
		} catch (InterpreterException e) {
			throw new BadDescriptorException(e); // TODO: Handle description loading exceptions
		} catch (SGLRException e) {
			throw new BadDescriptorException(e);
		}
	}
	
	// PUBLIC PROPERTIES
	
	/**
	 * Gets the language for this descriptor, but does not register it.
	 * 
	 * @see LanguageLoader#register(InputStream, boolean)
	 */
	public Language toLanguage() throws BadDescriptorException {
		return new Language(
				getProperty("Name"),
				getProperty("Name"), // natureId
				getProperty("Description", ""),
				getProperty("Extends", ROOT_LANGUAGE),
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
		return termContents(result);
	}
	

}
