package org.strategoxt.imp.runtime.dynamicloading;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLR;
import org.spoofax.jsglr.SGLRException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParser;
import org.strategoxt.imp.runtime.services.StrategoFeedback;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 * 
 * @see DescriptorFactory#load(IFile)
 */
public class Descriptor {
	public static final String ROOT_LANGUAGE = "DynamicRoot";
	
	private static final Language LANGUAGE =
		new Language("EditorService-builtin", "org.strategoxt.imp.builtin.editorservice", "", ROOT_LANGUAGE, "", "", "", null);
	
	private static SGLRParser descriptorParser;
	
	private final List<DynamicService> services = new ArrayList<DynamicService>();
	
	private final DynamicServiceFactory serviceFactory;
	
	private final IStrategoAppl document;
	
	private Language language;
	
	private IPath basePath;
	
	private StrategoFeedback feedback;
	
	public IStrategoAppl getDocument() {
		return document;
	}
	
	// LOADING DESCRIPTOR 
	
	private static void init() {
		if (descriptorParser != null) return;
		try {
			SGLR.setWorkAroundMultipleLookahead(true);
			InputStream stream = Descriptor.class.getResourceAsStream("/syntax/EditorService.tbl");
			ParseTable table = Environment.registerParseTable(LANGUAGE, stream);
			descriptorParser = new SGLRParser(table, "Module");
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
			init();
			IStrategoAppl document = descriptorParser.parse(input, null).getTerm();
			return new Descriptor(document);
		} catch (SGLRException e) {
			throw new BadDescriptorException("Could not parse descriptor file", e);
		}
	}
	
	protected void setBasePath(IPath basePath) {
		this.basePath = basePath;
	}
	
	public void initializeService(DynamicService service) {
		services.add(service);
	}
	
	/**
	 * Uninitialize all dynamic services associated with this Descriptor.
	 * 
	 * @see DynamicService#uninitialize()
	 */
	public void uninitialize() {
		for (DynamicService service : services)
			service.uninitialize();
	}
	
	// LOADING SERVICES
	
	public<T extends ILanguageService> T getService(Class<T> type) throws BadDescriptorException {
		return serviceFactory.getService(type);
	}
	
	public StrategoFeedback getStrategoFeedback() throws BadDescriptorException {
		if (feedback == null) {
			feedback = new StrategoFeedbackFactory().create(this);
		}
		return feedback;
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
				ROOT_LANGUAGE,                     // TODO: Use "extends" property?
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
		
		try {
			return openAttachment(file);
		} catch (FileNotFoundException e) {
			throw new BadDescriptorException(e);
		}
	}

	public InputStream openAttachment(String path) throws FileNotFoundException {
		if (basePath != null) { // read from filesystem
			path = basePath.append(path).toString();
			return new BufferedInputStream(new FileInputStream(path));
		} else { // read from jar
			try {
				Class mainClass = getService(IParseController.class).getClass();
				InputStream result = mainClass.getResourceAsStream(path);
				if (result == null)
					throw new FileNotFoundException(path + " not found in editor service plugin");
				return result;
			} catch (BadDescriptorException e) {
				throw new RuntimeException("Unable to instantiate parse controller class", e);
			}
			
		}
	}
	
	// INTERPRETING
	
	protected String getProperty(String name) throws BadDescriptorException {
		String result = getProperty(name, null);
		if (result == null) throw new BadDescriptorException("Property " + name + " not specified");		
		return result;
	}
	
	protected String getProperty(String name, String defaultValue) {
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
