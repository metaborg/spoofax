package org.strategoxt.imp.runtime.dynamicloading;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getFilename;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.collectTerms;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.findTerm;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.ServiceFactory;
import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.terms.Term;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.RuntimeActivator;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.MetaFileLanguageValidator;
import org.strategoxt.lang.WeakValueHashMap;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 * 
 * @see DescriptorFactory#load(IFile, IResource)
 */
public class Descriptor {
	
	public static final String ROOT_LANGUAGE = "DynamicRoot";

	protected static final Language DESCRIPTOR_LANGUAGE =
		new Language("EditorService-builtin", "org.strategoxt.imp.builtin.editorservice", "", ROOT_LANGUAGE, null, "", null, "", "", null);
	
	protected static final String DEFAULT_ICON = "icons/IMP-editor.gif";
	
	protected static final String DEFAULT_ICON_BUNDLE;
	
	static {
		if (RuntimeActivator.getInstance() != null) {
			DEFAULT_ICON_BUNDLE = RuntimeActivator.getInstance().getBundle().getSymbolicName();
		} else {
			// Happens when ran outside of Eclipse
			Environment.logException("Bundle not yet initialized when creating Descriptor");
			DEFAULT_ICON_BUNDLE = null;
		}
	}
	
	/**
	 * A set of all active services for a given descriptor.
	 * (Implemented as a map from services to null.)
	 */
	private final Map<IDynamicLanguageService, Object> activeServices =
		Collections.synchronizedMap(new WeakHashMap<IDynamicLanguageService, Object>());
	
	/**
	 * A per-editor map for all services marked as cacheable.
	 * 
	 * Only using weak keys and values can ensure that these services
	 * are garbage collected, as many language services refer to
	 * their parse controller (or AST), which is used as the key in this map. 
	 */
	private final Map<SGLRParseController, Map<Class, ILanguageService>> cachedServices =
		Collections.synchronizedMap(new WeakHashMap<SGLRParseController, Map<Class, ILanguageService>>());
	
	private final List<AbstractServiceFactory> serviceFactories = new ArrayList<AbstractServiceFactory>();
	
	private IStrategoAppl document;
	
	private Language language;
	
	private IPath basePath;
	
	private String builderCaption;
	
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
		serviceFactories.add(new TreeModelBuilderFactory());
		serviceFactories.add(new ReferenceResolverFactory());
		serviceFactories.add(new StrategoObserverFactory());
		serviceFactories.add(new SyntaxPropertiesFactory());
		serviceFactories.add(new TokenColorerFactory());
		serviceFactories.add(new BuilderFactory());
		serviceFactories.add(new RefactoringFactory());
		serviceFactories.add(new ContentProposerFactory());
		serviceFactories.add(new LabelProviderFactory());
		serviceFactories.add(new AutoEditStrategyFactory());
		serviceFactories.add(new OnSaveServiceFactory());
	}
	
	/**
	 * Uninitialize all dynamic activeServices associated with this Descriptor,
	 * and lazily initializes them to use the given new Descriptor.
	 * 
	 * Must be preceded by a call to {@link #prepareForReinitialize()}. 
	 * 
	 * @see AbstractService#reinitialize(Descriptor)
	 */
	public void reinitialize(Descriptor newDescriptor) throws BadDescriptorException {
		// Note: may also be reinitialized with the same descriptor
		IDynamicLanguageService[] currentServices;
		synchronized (activeServices) { // defensively copy
			currentServices = activeServices.keySet().toArray(new IDynamicLanguageService[0]);
		}
		for (IDynamicLanguageService service : currentServices) {
			service.reinitialize(newDescriptor);
		}
		attachedFiles = null;
		cachedServices.clear();
	}
	
	/**
	 * Prepares editor activeServices for reinitialization with a new descriptor.
	 */
	public void prepareForReinitialize() {
		IDynamicLanguageService[] currentServices;
		synchronized (activeServices) { // defensively copy
			currentServices = activeServices.keySet().toArray(new IDynamicLanguageService[0]);
		}
		for (IDynamicLanguageService service : currentServices)
			service.prepareForReinitialize();
	}
	
	// LOADING SERVICES
	
	/**
	 * Creates a new parse controller for this descriptor's language.
	 * May be a {@link DynamicParseController} or possibly a custom
	 * implementation of {@link IParseController}.
	 */
	public IParseController createParseController() throws BadDescriptorException {
		IParseController result = ServiceFactory.getInstance().getParseController(getLanguage());
		if (result instanceof DynamicParseController) {
			((DynamicParseController) result).initialize(null, getLanguage());
		}
		return result;
	}
	
	/**
	 * Creates a new service of a particular type.
	 * 
	 * For parse controllers, {@link #createParseController()}
	 * should be used instead, as these may have custom Java implementations.
	 */
	public synchronized<T extends ILanguageService> T createService(Class<T> type, SGLRParseController controller)
			throws BadDescriptorException {

		boolean foundFactory = false;
		assert controller == null || !controller.isReplaced()
			: "Stale SGLRParseController given to Descriptor.createService() - service didn't call initialize()?";
		
		try {
			for (AbstractServiceFactory<T> factory : serviceFactories) {
				if (factory.canCreate(type)) {
					synchronized (factory) {
						T result = getCachedService(type, controller);
						if (result != null) return result;
						result = factory.create(this, controller);
						if (result != null) {
							addKnownService(type, controller, result, factory.isCachable() && controller != null);
							return result;
						}
						foundFactory = true;
					}
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
	
	@SuppressWarnings("unchecked")
	private<T> T getCachedService(Class<T> type, SGLRParseController controller) {
		Map<Class, ILanguageService> cache = cachedServices.get(controller);
		T result = cache == null ? null : (T) cache.get(type);
		return result;
	}
	
	protected void simpleClearCache(SGLRParseController controller) {
		cachedServices.remove(controller);
	}
	
	protected Iterable<IDynamicLanguageService> getActiveServices(SGLRParseController controller) {
		List<IDynamicLanguageService> results = new ArrayList<IDynamicLanguageService>();
		synchronized (activeServices) {
			for (IDynamicLanguageService service : activeServices.keySet()) {
				if (service instanceof AbstractService &&
					controller == ((AbstractService) service).internalGetParseController()) {
					results.add(service);
				}
			}
			return results;
		}
	}
	
	protected final Iterable<IDynamicLanguageService> getActiveServices() {
		synchronized (activeServices) {
			return new HashSet<IDynamicLanguageService>(activeServices.keySet());
		}
	}

	private void addKnownService(Class type, SGLRParseController controller, ILanguageService service, boolean isCachable) {
		if (service instanceof IDynamicLanguageService)
			activeServices.put((IDynamicLanguageService) service, null);
		if (isCachable) {
			Map<Class, ILanguageService> cache = cachedServices.get(controller);
			if (cache == null) {
				cache = Collections.synchronizedMap(new WeakValueHashMap<Class, ILanguageService>());
				cachedServices.put(controller, cache);
			}
			cache.put(type, service);
		}
	}
	
	public void addActiveService(IDynamicLanguageService service) {
		activeServices.put(service, null);
	}
	
	public void addActiveServices(Descriptor descriptor) {
		// We only copy the active services here; the service cache may be invalid
		synchronized (descriptor.activeServices) {
			for (IDynamicLanguageService service : descriptor.activeServices.keySet()) {
				activeServices.put(service, null);
			}
		}
	}
	
	public final IStrategoAppl getDocument() {
		return document;
	}
	
	protected void setDocument(IStrategoAppl document) {
		this.document = document;
		attachedFiles = null;
	}
	
	protected void setBasePath(IPath basePath) {
		this.basePath = basePath;
	}
	
	/**
	 * Gets the directory location of the editor plugin.
	 */
	public IPath getBasePath() {
		if (basePath == null) {
			Class attachmentProvider = getAttachmentProvider();
			if (attachmentProvider != null)
				basePath = new Path(attachmentProvider.getProtectionDomain().getCodeSource().getLocation().getFile());
		}
		return basePath;
	}
	
	public String getStartSymbol() {
		IStrategoAppl result = findTerm(document, "StartSymbols");
		if (result == null)
			return null;

		// FIXME: support more than one start symbol
		return termContents(termAt(termAt(result, 0), 0));
	}
	
	public boolean isUsedForUnmanagedParseTable(String languageName) {
		String prefix = getProperty("UnmanagedTablePrefix", null);
		return prefix != null && languageName.startsWith(prefix);
	}
	
	public boolean isDynamicallyLoaded() {
		return dynamicallyLoaded;
	}
	
	public boolean isATermEditor() {
		try {
			return "IStrategoTerm".equals(getLanguage().getName());
		} catch (BadDescriptorException e) {
			return false;
		}
	}
	
	public boolean isUnicodeFlattened() {
		ArrayList<IStrategoAppl> flattened = TermReader.collectTerms(document, "FlattenUnicode");
		return !flattened.isEmpty() && !"False".equals(Term.tryGetName(flattened.get(0).getSubterm(0)));
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
	public Class<?> getAttachmentProvider() {
		try {
			if (attachmentProvider == null) {
				// might be better, but unnecessary as long as setAttachmentProvider is called
				// IParseController parser = createParseController();
				// if (parser instanceof DynamicParseController)
				// 	parser = ((DynamicParseController) parser).getWrapped();
				IParseController parser = createService(IParseController.class, null);
				attachmentProvider = parser.getClass();
			}
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
			Class provider = getAttachmentProvider();
			InputStream result = provider.getResourceAsStream("/" + path);
			if (result == null) // Try non-windows path
				result = provider.getResourceAsStream("/" + path.replace('\\', '/'));
			if (result == null) { // read resource listed in descriptor
				if (!onlyListedFiles)
					return openAttachment(path, true);
				String specified = onlyListedFiles ? "specified file " : "";
				Environment.logException("Attachment " + path
						+ " not found in plugin with attachment provider "
						+ provider.getName());
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
				ROOT_LANGUAGE,		  // ("Extends" is not used for IMP)
				DEFAULT_ICON, // TODO: use a default icon path /icons/editor.gif, if it exists
				getProperty("URL", ""),
				// FIXME: ID of the bundle containing the language descriptor and icon for this language
				//        (does getAttachmentProvider() already provide enough functionality for this?)
				DEFAULT_ICON_BUNDLE, 
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
	
	public String getParseTableProviderFunction() throws BadDescriptorException {
		return getProperty("TableProvider", null);
	}
	
	public String getBuilderCaption() {
		if (builderCaption == null)
			builderCaption = getProperty("BuilderCaption", "");
		return builderCaption.length() == 0 ? null : builderCaption;
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
		if (result == null) {
			String filename = getFilename(document);
			String context = filename == null ? "" : " in " + filename;
			throw new BadDescriptorException("Property " + name + " not specified" + context);
		}
		return result;
	}

	protected String getProperty(String name, String defaultValue) {
		IStrategoAppl result = findTerm(document, name);
		if (result == null)
			return defaultValue;

		return termContents(result);
	}

	protected String[] getProperties(String name) {
		List<IStrategoAppl> resultTerms = collectTerms(document, name);
		String[] results = new String[resultTerms.size()];
		for (int i = 0, max = results.length; i < max; i++) {
			results[i] = termContents(resultTerms.get(i));
		}
		return results;
	}

	protected String[] getPropertyArray(String name) {
		IStrategoAppl result = findTerm(document, name);
		if (result == null)
			return new String[0];
		
		IStrategoList list = termAt(termAt(result, 0), 0);

		String[] results = new String[list.size()];
		for (int i = 0; i < results.length; i++)
			results[i] = termContents(list.getSubterm(i));
		
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
