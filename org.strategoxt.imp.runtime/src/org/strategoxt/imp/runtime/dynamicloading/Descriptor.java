package org.strategoxt.imp.runtime.dynamicloading;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.services.StrategoFeedback;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 * 
 * @see DescriptorFactory#load(IFile)
 */
public class Descriptor {
	public static final String ROOT_LANGUAGE = "DynamicRoot";

	protected static final Language DESCRIPTOR_LANGUAGE =
		new Language("EditorService-builtin", "org.strategoxt.imp.builtin.editorservice", "", ROOT_LANGUAGE, "", "", "", null);
	
	private final List<AbstractService> services = new ArrayList<AbstractService>();
	
	private final List<AbstractServiceFactory> serviceFactories = new ArrayList<AbstractServiceFactory>();
	
	private final IStrategoAppl document;
	
	private Language language;
	
	private IPath basePath;
	
	private StrategoFeedback feedback;
	
	protected Descriptor(IStrategoAppl document) {
		this.document = document;
		
		initializeFactories();
	}
	
	private void initializeFactories() {
		serviceFactories.add(new ParseControllerFactory());
		serviceFactories.add(new FoldingUpdaterFactory());
		serviceFactories.add(new OutlinerFactory());
		serviceFactories.add(new ReferenceResolverFactory());
		serviceFactories.add(new StrategoFeedbackFactory());
		serviceFactories.add(new SyntaxPropertiesFactory());
		serviceFactories.add(new TokenColorerFactory());
	}
	
	/**
	 * Uninitialize all dynamic services associated with this Descriptor.
	 * 
	 * @see AbstractService#uninitialize()
	 */
	public void uninitialize() {
		for (AbstractService service : services)
			service.uninitialize();
	}
	
	// LOADING SERVICES
	
	public synchronized<T extends ILanguageService> T createService(Class<T> type)
			throws BadDescriptorException {
		
		boolean foundFactory = false;
		
		try {
			for (AbstractServiceFactory<T> factory : serviceFactories) {
				if (factory.canCreate(type)) {
					T result = (T) factory.create(this);
					foundFactory = true;
					if (result != null) return result;
				}
			}
		} catch (RuntimeException e) {
			throw new BadDescriptorException("Exception occurred when initializing "
					+ type.getSimpleName() + " editor service for " + getLanguage().getName(), e);
		}
		
		if (!foundFactory)
			throw new IllegalArgumentException(type.getSimpleName() + " is not a supported editor service type");
		else
			throw new IllegalStateException("Could not create an editor service for " + type.getSimpleName());
	}
	
	public StrategoFeedback getStrategoFeedback() throws BadDescriptorException {
		if (feedback == null) {
			feedback = new StrategoFeedbackFactory().create(this);
		}
		return feedback;
	}
	
	public void addInitializedService(AbstractService service) {
		services.add(service);
	}
	
	public IStrategoAppl getDocument() {
		return document;
	}
	
	protected void setBasePath(IPath basePath) {
		this.basePath = basePath;
	}
	
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
				Class mainClass = createService(IParseController.class).getClass();
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
