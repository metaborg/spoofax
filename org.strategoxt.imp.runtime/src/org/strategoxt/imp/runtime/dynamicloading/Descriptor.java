package org.strategoxt.imp.runtime.dynamicloading;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLR;
import org.spoofax.jsglr.SGLRException;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParser;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 * 
 * @see DescriptorFactory#load(IFile)
 */
public class Descriptor {
	public static final String ROOT_LANGUAGE = "Root";
	
	private static final Language LANGUAGE =
		new Language("EditorService-builtin", "org.strategoxt.imp.builtin.editorservice", "", "Root", "", "", "", null);
	
	private static final SGLRParser parser;
	
	private final DynamicServiceFactory serviceFactory;
	
	private final IStrategoAppl document;
	
	private Language language;
	
	private IPath basePath;
	
	public IStrategoAppl getDocument() {
		return document;
	}
	
	// LOADING DESCRIPTOR 
	
	static {
		try {
			SGLR.setWorkAroundMultipleLookahead(true);
			InputStream stream = Descriptor.class.getResourceAsStream("/syntax/EditorService.tbl");
			ParseTable table = Environment.registerParseTable(LANGUAGE, stream);
			parser = new SGLRParser(table, "Module");
		} catch (Throwable e) {
			Environment.logException("Could not initialize the Descriptor class.", e);
			throw new RuntimeException(e);
		}
	}
	
	private Descriptor(IStrategoAppl document) {
		this.document = document;
		serviceFactory = new DynamicServiceFactory(this);
	}
	
	protected static Descriptor load(InputStream input) throws BadDescriptorException, IOException {
		try {
			IStrategoAppl document = parser.parse(input, null).getTerm();
	        return new Descriptor(document);
		} catch (SGLRException e) {
			throw new BadDescriptorException("Could not parse descriptor file", e);
		}
	}
	
	protected void setBasePath(IPath basePath) {
		this.basePath = basePath;
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
	
	public void addCompilerProviders(Interpreter interpreter) throws BadDescriptorException {
		for (IStrategoAppl term : collectTerms(document, "CompilerProvider")) {
			try {
				Debug.startTimer("Loading interpreter input ", termContents(term));
				interpreter.load(openAttachment(termContents(term)));
				Debug.stopTimer("Successfully loaded " +  termContents(term));
			} catch (InterpreterException e) {
				throw new BadDescriptorException("Error loading reference resolving provider", e);
			} catch (IOException e) {
				throw new BadDescriptorException("Could not load reference resolving provider", e);
			}
		}
	}

	private InputStream openAttachment(String path) throws BadDescriptorException {
		try {
			if (basePath != null)
				path = basePath.append(path).toString();
				
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
