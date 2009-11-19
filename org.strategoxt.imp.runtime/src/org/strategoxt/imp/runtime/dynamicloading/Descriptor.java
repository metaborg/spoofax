package org.strategoxt.imp.runtime.dynamicloading;

import static org.spoofax.interpreter.core.Tools.*;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.services.MetaFileLanguageValidator;
import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 * 
 * @see DescriptorFactory#load(IFile, IResource)
 */
public class Descriptor {
	public static final String ROOT_LANGUAGE = "DynamicRoot";

	protected static final Language DESCRIPTOR_LANGUAGE =
		new Language("EditorService-builtin", "org.strategoxt.imp.builtin.editorservice", "", ROOT_LANGUAGE, "", "", "", null);
	
	private final Map<AbstractService, Object> services = new WeakHashMap<AbstractService, Object>();
	
	private final List<AbstractServiceFactory> serviceFactories = new ArrayList<AbstractServiceFactory>();
	
	private final IStrategoAppl document;
	
	private Language language;
	
	private IPath basePath;
	
	private StrategoObserver feedback;
	
	private Set<File> attachedFiles;
	
	private Class<?> attachmentProvider;
	
	private boolean dynamicallyLoaded;
	
	// LOADING DESCRIPTOR 

	protected Descriptor(IStrategoAppl document) throws BadDescriptorException {
		this.document = document;
		
		initializeFactories();
	}
	
	private void initializeFactories() throws BadDescriptorException {
		serviceFactories.add(new ParseControllerFactory());
		serviceFactories.add(new FoldingUpdaterFactory());
		serviceFactories.add(new OutlinerFactory());
		serviceFactories.add(new ReferenceResolverFactory());
		serviceFactories.add(new StrategoObserverFactory());
		serviceFactories.add(new SyntaxPropertiesFactory());
		serviceFactories.add(new TokenColorerFactory());
		serviceFactories.add(new BuilderFactory());
	}
	
	/**
	 * Uninitialize all dynamic services associated with this Descriptor,
	 * and lazily initializes them to use the given new Descriptor.
	 * 
	 * @see AbstractService#reinitialize(Descriptor)
	 */
	public void reinitialize(Descriptor newDescriptor) throws BadDescriptorException {
		for (AbstractService service : services.keySet())
			service.reinitialize(newDescriptor);
		attachedFiles = null;
		// UNDONE: StrategoBuilderListener.rescheduleAllListeners(); // TODO: cleanup
	}
	
	/**
	 * Prepares editor services for reinitialization with a new descriptor.
	 */
	public void prepareForReinitialize() {
		for (AbstractService service : services.keySet())
			service.prepareForReinitialize();
	}
	
	// LOADING SERVICES
	
	/**
	 * Creates a new service of a particular type.
	 * 
	 * @see #addInitializedService(AbstractService)
	 *      Must be called for dynamic _wrapper_ services as soon as they are fully initialized.
	 */
	public synchronized<T extends ILanguageService> T createService(Class<T> type)
			throws BadDescriptorException {
		
		boolean foundFactory = false;
		
		// TODO: caching of builders and reference resolvers?
		
		try {
			for (AbstractServiceFactory<T> factory : serviceFactories) {
				if (factory.canCreate(type)) {
					T result = factory.create(this);
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
	
	public StrategoObserver getStrategoObserver() throws BadDescriptorException {
		if (feedback == null)
			feedback = new StrategoObserverFactory().create(this);
		return feedback;
	}
	
	public void addInitializedService(AbstractService service) {
		services.put(service, null);
	}
	
	public void addInitializedServices(Descriptor descriptor) {
		for (AbstractService service : descriptor.services.keySet()) {
			services.put(service, null);
		} 
	}
	
	public IStrategoAppl getDocument() {
		return document;
	}
	
	protected void setBasePath(IPath basePath) {
		this.basePath = basePath;
	}
	
	public IPath getBasePath() {
		return basePath;
	}
	
	public String getStartSymbols() {
		return getProperty("StartSymbols", null);
	}
	
	public boolean isDynamicallyLoaded() {
		return dynamicallyLoaded;
	}
	
	public void setDynamicallyLoaded(boolean dynamicallyLoaded) {
		this.dynamicallyLoaded = dynamicallyLoaded;
	}
	
	/**
	 * Sets the class that provides access to any attached files in its enclosing JAR file.
	 */
	public void setAttachmentProvider(Class<?> attachmentProvider) {
		this.attachmentProvider = attachmentProvider;
	}
	
	/**
	 * Gets the class that provides access to any attached files in its enclosing JAR file.
	 */
	protected Class<?> getAttachmentProvider() {
		try {
			if (attachmentProvider == null)
				attachmentProvider = createService(IParseController.class).getClass();
			return attachmentProvider;
		} catch (BadDescriptorException e) { // Unexpected exception
			Environment.logException("Unable to instantiate parse controller class", e);
			return Descriptor.class;
		}
	}

	public InputStream openParseTableStream() throws BadDescriptorException {
		try {
			return openAttachment(getParseTableName(), false);
		} catch (FileNotFoundException e) {
			throw new BadDescriptorException("Could not open parse table", e);
		}
	}

	public InputStream openPPTableStream() throws BadDescriptorException {
		try {
			return openAttachment(getPPTableName(), false);
		} catch (FileNotFoundException e) {
			throw new BadDescriptorException("Could not pretty printing table", e);
		}
	}

	/**
	 * Open an attached file associated with this descriptor.
	 */ 
	public InputStream openAttachment(String path) throws FileNotFoundException {
		return openAttachment(path, false);
	}
    
    /**
     * Open an attached file associated with this descriptor.
     * 
     * @param onlyListedFiles  Only consider attached files listed in the descriptor.
     */
	private InputStream openAttachment(String path, boolean onlyListedFiles) throws FileNotFoundException {
		if (onlyListedFiles)
			path = getAttachmentPath(path);

		if (basePath != null) { // read from filesystem
			path = basePath.append(path).toString();
			if (!onlyListedFiles && !new File(path).exists())
				return openAttachment(path, true);
			return new BufferedInputStream(new FileInputStream(path));
		} else { // read from jar
			InputStream result = getAttachmentProvider().getResourceAsStream("/" + path);
			if (result == null) { // read resource listed in descriptor
				if (!onlyListedFiles)
					return openAttachment(path, true);
				String specified = onlyListedFiles ? "specified file " : "";
				throw new FileNotFoundException(specified + path
						+ " not found in editor service plugin");
			}
			return result;
		}
	}

	private String getAttachmentPath(String path) throws FileNotFoundException {
		File file = new File(path);
		String name = file.getName();
		for (File attached : getAttachedFiles()) {
			if (attached.getName().equals(name))
				return attached.toString();
		}
		throw new FileNotFoundException(path
				+ " not specified as an attachment in editor service plugin");
	}
	
	// INTERPRETING
    
    /**
     * Gets the language for this descriptor, but does not register it.
     */
    public Language getLanguage() throws BadDescriptorException {
        if (language == null) {
            language = new Language(
                getProperty("LanguageName"),
                getProperty("LanguageId", getProperty("LanguageName")),
                getProperty("Description", ""),
                ROOT_LANGUAGE,          // ("Extends" is not used for IMP)
                getProperty("URL", ""),
                getProperty("Extensions"),
                getProperty("Aliases", ""),
                new MetaFileLanguageValidator(this));
        }
        return language;
    }

	private String getParseTableName() throws BadDescriptorException {
		String file = getProperty("Table", getProperty("LanguageName"));
		if (!file.endsWith(".tbl"))
			file += ".tbl";
		return file;
	}

	private String getPPTableName() throws BadDescriptorException {
		String file = getProperty("PPTable", getProperty("LanguageName"));
		if (!file.endsWith(".pp.af"))
			file += ".pp.af";
		return file;
	}

	public String[] getExtendedLanguages() {
    	return getPropertyArray("Extends");
    }

	/**
	 * Get a set of all files attached to this descriptor (e.g., .ctree or
	 * .pp.af files). This method is cached.
	 */
	public Set<File> getAttachedFiles() {
		if (attachedFiles != null)
			return attachedFiles;
		attachedFiles = new LinkedHashSet<File>(); // (linked: must maintain jar order)

		try {
			attachedFiles.add(new File(getParseTableName()));
			attachedFiles.add(new File(getPPTableName()));
		} catch (Exception e) {
			// Ignore missing language name here
		}

		for (IStrategoAppl s : collectTerms(getDocument(), "SemanticProvider")) {
			attachedFiles.add(new File(termContents(s)));
		}

		return attachedFiles;
	}

	protected String getProperty(String name) throws BadDescriptorException {
		String result = getProperty(name, null);
		if (result == null)
			throw new BadDescriptorException("Property " + name + " not specified");
		return result;
	}

	protected String getProperty(String name, String defaultValue) {
		IStrategoAppl result = findTerm(document, name);
		if (result == null)
			return defaultValue;

		if (termAt(result, 0).getTermType() == IStrategoTerm.APPL
				&& cons(termAt(result, 0)).equals("Values")) {
			return concatTermStrings(termAt(result, 0));
		} else {
			return termContents(result);
		}
	}

	protected String[] getPropertyArray(String name) {
		IStrategoAppl result = findTerm(document, name);
		if (result == null)
			return new String[0];
		
		IStrategoList list = termAt(termAt(result, 0), 0);

		String[] results = new String[list.size()];
		for (int i = 0; i < results.length; i++)
			results[i] = termContents(list.get(i));
		
		return results;
	}


	@Override
	public String toString() {
		try {
			return "D:" + getLanguage();
		} catch (BadDescriptorException e) {
			return "D:" + e;
		}
	}
}
