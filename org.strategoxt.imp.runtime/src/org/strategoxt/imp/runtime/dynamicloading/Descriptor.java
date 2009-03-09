package org.strategoxt.imp.runtime.dynamicloading;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;
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
	
	private Set<File> attachedFiles;
	
	private Class<?> attachmentProvider;
	
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
		serviceFactories.add(new StrategoFeedbackFactory());
		serviceFactories.add(new SyntaxPropertiesFactory());
		serviceFactories.add(new TokenColorerFactory(this));
	}
	
	/**
	 * Uninitialize all dynamic services associated with this Descriptor.
	 * 
	 * @see AbstractService#uninitialize()
	 */
	public void uninitialize() {
		for (AbstractService service : services)
			service.uninitialize();
		attachedFiles = null;
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
	
	public String getStartSymbols() {
		return getProperty("StartSymbols", null);
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
			throw new BadDescriptorException(e);
		}
	}
    
    public InputStream openPPTableStream() throws BadDescriptorException {
        try {
            return openAttachment(getPPTableName(), false);
        } catch (FileNotFoundException e) {
            throw new BadDescriptorException(e);
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
		    	if (!onlyListedFiles) return openAttachment(path, true);
		    	String specified = onlyListedFiles ? "specified file " : "";
		        throw new FileNotFoundException(specified + path + " not found in editor service plugin");
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
		throw new FileNotFoundException(path + " not specified as an attachment in editor service plugin");
	}
	
	// INTERPRETING
    
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

    private String getParseTableName() throws BadDescriptorException {
        String file = getProperty("Table", getProperty("LanguageName"));
        if (!file.endsWith(".tbl")) file += ".tbl";
        return file;
    }

    private String getPPTableName() throws BadDescriptorException {
        String file = getProperty("PPTable", getProperty("LanguageName"));
        if (!file.endsWith(".pp.af")) file += ".pp.af";
        return file;
    }
    
    /**
     * Get a set of all files attached to this descriptor
     * (e.g., .ctree or .pp.af files). 
     * This method is cached.
     */
    public Set<File> getAttachedFiles() {
    	if (attachedFiles != null) return attachedFiles;
    	attachedFiles = new HashSet<File>();
    	
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
		if (result == null) throw new BadDescriptorException("Property " + name + " not specified");		
		return result;
	}
	
	protected String getProperty(String name, String defaultValue) {
		IStrategoAppl result = findTerm(document, name);
		if (result == null) return defaultValue;
		
		if ( termAt(result, 0).getTermType() == IStrategoTerm.APPL &&
				cons((IStrategoAppl) termAt(result, 0)).equals("Values")) {
			return concatTermStrings(termAt(result, 0));
		} else {
			return termContents(result);
		}
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
