package org.strategoxt.imp.runtime.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.SimpleAnnotationTypeInfo;
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr.client.NoRecoveryRulesException;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.client.ParseTimeoutException;
import org.spoofax.jsglr.client.StartSymbolException;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.TokenKindManager;
import org.spoofax.jsglr.shared.BadTokenException;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.jsglr.shared.TokenExpectedException;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.SWTSafeLock;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.dynamicloading.DynamicParseController;
import org.strategoxt.imp.runtime.dynamicloading.ParseTableProvider;
import org.strategoxt.imp.runtime.parser.ast.AstNodeLocator;
import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenIterator;
import org.strategoxt.imp.runtime.services.MetaFile;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.services.TokenColorer;

/**
 * IMP parse controller for an SGLR parser; instantiated for a particular source file.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 * @author Karl Trygve Kalleberg <karltk add strategoxt.org>
 */
public class SGLRParseController implements IParseController {
	
	private static final int PARSE_TIMEOUT = 20 * 1000;
	
	private final SWTSafeLock parseLock = new SWTSafeLock(true);
	
	private final JSGLRI parser;
	
	private final Language language;
	
	private final ILanguageSyntaxProperties syntaxProperties;
	
	private final ParseErrorHandler errorHandler = new ParseErrorHandler(this);
	
	private volatile IStrategoTerm currentAst;
	
	private volatile ITokenizer currentParseStream;
	
	private ISourceProject project;
	
	private IPath path;
	
	private EditorState editor;
	
	private MetaFile metaFile;
	
	private Exception unmanagedParseTableMismatch;;
	
	private volatile boolean isStartupParsed;
	
	private volatile boolean disallowColorer;
	
	private volatile boolean isAborted;
	
	private boolean isReplaced;
	
	private volatile boolean performInitialUpdate;

	private volatile long initialReschedule;

	// Simple accessors
	
	/**
	 * Gets the last parsed AST for this editor, or
	 * tries to parse the file if it hasn't been parsed yet.
	 * 
	 * @return The parsed AST, or null if the file could not be parsed (yet).
	 */
	public final IStrategoTerm getCurrentAst() {
		assert !isReplaced();
		if (currentAst == null) forceInitialParse();
		return currentAst;
	}

	public final ISourceProject getProject() {
		return project;
	}
	
	public final JSGLRI getParser() {
		return parser;
	}
	
	public ReentrantLock getParseLock() {
		return parseLock;
	}
	
	public ParseErrorHandler getErrorHandler() {
		return errorHandler;
	}
	
	/**
	 * Returns true if the given parse controller has been replaced by
	 * a newly loaded instance.
	 */
	public boolean isReplaced() {
		return isReplaced;
	}

	/**
	 * @return either a project-relative path, if getProject() is non-null, or
	 *         an absolute path.
	 */
    public final IPath getPath() {
    	return project == null || project.getRawProject().getLocation() == null
			? path
			: project.getRawProject().getLocation().append(path);
    }
    
    public IFile getResource() {
    	IPath path = getPath();
    	if (getProject() == null) {
    		// HACK: out-of-project resource (Spoofax/95)
    		return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
    	} else {
    		IProject project = getProject().getRawProject();
    		path = path.removeFirstSegments(path.matchingFirstSegments(project.getLocation()));
    		return project.getFile(path);
    	}
    }
    
    public void setEditor(EditorState editor) {
		this.editor = editor;
		if (initialReschedule != 0) {
			scheduleParserUpdate(initialReschedule, false);
			initialReschedule = 0;
		}
	}
    
    public EditorState getEditor() {
		return editor;
	}
    
    public boolean getPerformInitialUpdate() {
    	return this.performInitialUpdate;
    }
    
    public void setPerformInitialUpdate(boolean performInitialUpdate) {
    	this.performInitialUpdate = performInitialUpdate;
    }
    
    /**
     * Creates a new SGLRParseController.
     * 
     * @param language      The name of the language, as registered in the {@link LanguageRegistry}.
     * @param startSymbol	The start symbol of this grammar, or null.
     */
    public SGLRParseController(Language language, ParseTableProvider table, ILanguageSyntaxProperties syntaxProperties,
			String startSymbol) {
    	
    	this.language = language;
    	this.syntaxProperties = syntaxProperties;
    	this.performInitialUpdate = true;
    	
    	parser = new JSGLRI(table, startSymbol, this);
		parser.setTimeout(PARSE_TIMEOUT);
		try {
			parser.setUseRecovery(true);
		} catch (NoRecoveryRulesException e) {
			Environment.logException("No recovery rules available for " + language.getName() + " editor", e);
		}
    }
    
    @Deprecated
    public SGLRParseController(Language language, ParseTable table, ILanguageSyntaxProperties syntaxProperties,
			String startSymbol) {
    	this(language, new ParseTableProvider(table), syntaxProperties, startSymbol);
    }
    
    /**
     * Creates a new SGLRParseController, throwing any parse table loading exceptions as runtime exceptions.
     * 
     * @deprecated Use {@link #SGLRParseController(Language, ParseTable, ILanguageSyntaxProperties, String)} instead.
     */
    @Deprecated
    public SGLRParseController(Language language, ILanguageSyntaxProperties syntaxProperties, String startSymbol) {
    	this(language, getTableSwallowExceptions(language), syntaxProperties, startSymbol);
    }

    private static ParseTableProvider getTableSwallowExceptions(Language language) {
		try {
			return Environment.getParseTableProvider(language);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void initialize(IPath filePath, ISourceProject project, IMessageHandler messages) {
		this.path = filePath;
		this.project = project;
    }
    
    @Deprecated
    public final IStrategoTerm parse(String input, boolean scanOnly, IProgressMonitor monitor) {
    	return parse(input, monitor);
    }

	public IStrategoTerm parse(String input, final IProgressMonitor monitor) {
		assert !isReplaced();
		disallowColorer = true;
		boolean wasStartupParsed = isStartupParsed;
		isStartupParsed = true; // Eagerly init this to avoid the main thread acquiring our lock			
		if (input.length() == 0) {
			currentParseStream = null;
			return currentAst = null;
		}
		
		if (getPath() == null)
		    throw new IllegalStateException("SGLR parse controller not initialized");
		
		// XXX: SGLR.asyncAbort() is never called until the old parse actually completes
		//      (or is it?!)
		//      need to intercept cancellation events in the running thread's monitor instead
		// UNDONE: getParser().asyncAbort(); // can't call this anyway atm when completion is running

		String filename = getPath().toPortableString();
		// IResource resource = getResource();
		errorHandler.abortScheduledCommit();
		
		try {
			// Can't do resource locking when Eclipse is still starting up; causes deadlock
			// UNDONE: No longer using resource lock because it sucks
			// if (isStartupParsed)
			// 	Job.getJobManager().beginRule(resource, monitor); // enter lock
			// disallowColorer = isStartupParsed;
			//assert !parseLock.isHeldByCurrentThread() : "Parse lock must be locked after resource lock";
			parseLock.lock();
			//System.out.println("PARSE! " + System.currentTimeMillis()); // DEBUG
			
			processMetaFile();
			// XXX: input chars are changed by the asfix imploder, invalidating caching and requiring a clone here
			char[] inputChars = input.toCharArray();
			char[] originalInputChars = inputChars.clone();
			
			Debug.startTimer();
			
			if (monitor.isCanceled()) return null;
			IStrategoTerm asfix = doParse(inputChars, filename);
			if (monitor.isCanceled()) return null;
			IStrategoTerm ast = parser.internalImplode(asfix);

			errorHandler.clearErrors();
			errorHandler.setRecoveryFailed(false);
			errorHandler.gatherNonFatalErrors(originalInputChars, parser.getCurrentTokenizer(), asfix);
			parser.resetState(); // clean up memory
			
			currentAst = ast;
			currentParseStream = parser.getParseStream();
				
			Debug.stopTimer("File parsed: " + filename);
			
			// TODO: is coloring, then error marking best?
			
		} catch (ParseTimeoutException e) {
			if (monitor.isCanceled() || isAborted) return null;
			reportException(errorHandler, e);
			if (unmanagedParseTableMismatch != null)
				reportException(errorHandler, unmanagedParseTableMismatch);
		} catch (SGLRException e) {
			reportException(errorHandler, e);
			if (unmanagedParseTableMismatch != null)
				reportException(errorHandler, unmanagedParseTableMismatch);
		} catch (IOException e) {
			reportException(errorHandler, e);
		} catch (OperationCanceledException e) {
			return null;
		} catch (RuntimeException e) {
			reportException(errorHandler, e);
		} finally {
			try {
				if (this.performInitialUpdate) {
					onParseCompleted(monitor, wasStartupParsed);
				}
			} catch (RuntimeException e) {
				Environment.logException("Exception in post-parse events", e);
			}
			
			//if (isStartupParsed) Job.getJobManager().endRule(resource);
			isAborted = false;
			parseLock.unlock();
		}

		return monitor.isCanceled() ? null : currentAst;
	}
	
	public void scheduleParserUpdate(long delay, boolean abortFirst) {
		if (abortFirst && parseLock.isLocked()) {
			isAborted = true;
			if (!parseLock.isLocked())
				isAborted = false;
		}
		if (getEditor() != null) {
			getEditor().scheduleParserUpdate(delay);
		} else {
			// Reschedule after fully initialized
			initialReschedule = delay;
		}
	}

	private IStrategoTerm doParse(String input, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		try {
			return parser.parse(input, filename);
		} catch (StartSymbolException e) {
			if (metaFile != null) {
				// Unmanaged parse tables may have different start symbols;
				// try again without the standard start symbol
			} else {
				// Be forgiving: user probably specified an inconsistent start symbol in the ESV
				Environment.logWarning("Incorrect start symbol specified in editor descriptor:" + parser.getStartSymbol(), e);
			}
			parser.setStartSymbol(null);
			return parser.parse(input, filename);
		}
	}

	private void processMetaFile() {
		unmanagedParseTableMismatch = null;
		String metaFileName = getPath().removeFileExtension().addFileExtension("meta").toOSString();
		MetaFile metaFile = MetaFile.read(metaFileName);
		if (metaFile != this.metaFile) {
			Descriptor descriptor = Environment.getDescriptor(getLanguage());
			if (metaFile != null && !descriptor.isUsedForUnmanagedParseTable(metaFile.getLanguage())) {
				String message = "Meta file ignored for " + getResource().getName() + " (unsupported language: " + metaFile.getLanguage() +")";
				unmanagedParseTableMismatch = new InvalidParseTableException(message);
				Environment.logWarning(message);
				metaFile = null;
			}
			if (metaFile == null) {
				parser.setParseTable(getTableSwallowExceptions(getLanguage()));
				parser.getDisambiguator().setHeuristicFilters(false);
			} else {
				ParseTable table = Environment.getUnmanagedParseTable(metaFile.getLanguage() + "-Permissive");
				if (table == null) table = Environment.getUnmanagedParseTable(metaFile.getLanguage());
				parser.getDisambiguator().setHeuristicFilters(metaFile.isHeuristicFiltersEnabled());
				if (table == null) {
					String message = "Could not find descriptor or unmanaged parse table for language " + metaFile.getLanguage();
					unmanagedParseTableMismatch = new InvalidParseTableException(message);
					Environment.logException(unmanagedParseTableMismatch);
				} else {
					parser.setParseTable(table);				
				}
			}
			this.metaFile = metaFile;
		}
	}

	private void onParseCompleted(final IProgressMonitor monitor, boolean wasStartupParsed) {
		assert parseLock.isHeldByCurrentThread();
		
		if (!monitor.isCanceled() && currentParseStream != null)
			forceRecolor(wasStartupParsed);
		
		// Threading concerns:
		// - must never block the main thread with a lock here since
		//   it must be available to draw error markers
		// - must not acquire resource locks when Eclipse is starting
		
		if (!Environment.isMainThread()) {
			if (!monitor.isCanceled() && !Environment.getStrategoLock().isHeldByCurrentThread()) {
				// Note that a resource lock is acquired here
				errorHandler.commitMultiErrorLineAdditions();
				errorHandler.commitDeletions();
			}
			
			// Removed markers seem to require recoloring:
			//if (!monitor.isCanceled() /*&& currentParseStream != null*/ && editor != null)
			//	AstMessageHandler.processEditorRecolorEvents(editor.getEditor());
			
			if (!monitor.isCanceled())
				errorHandler.scheduleCommitAllChanges();
		} else {
			// Report errors again later when not in main thread
			// (this state shouldn't be reachable from normal operation,
			//  or maybe just for newly opened editors)
			scheduleParserUpdate(DynamicParseController.REINIT_PARSE_DELAY, false);
		}
		
		if (!monitor.isCanceled())
			scheduleObserverUpdate(errorHandler);
	}

	private void reportException(ParseErrorHandler errorHandler, Exception e) {
		errorHandler.clearErrors();
		errorHandler.setRecoveryFailed(true);
		errorHandler.reportError(getCurrentTokenizer(), e);
	}

	private void scheduleObserverUpdate(ParseErrorHandler errorHandler) {
		// We bypass the UniversalEditor IModelListener for this,
		// allowing a bit more (timing) control and ease of use (wrt dynamic reloading)
		try {
			StrategoObserver feedback = Environment.getDescriptor(getLanguage()).createService(StrategoObserver.class, this);
			if (feedback != null) feedback.scheduleUpdate(this);
		} catch (BadDescriptorException e) {
			Environment.logException("Unexpected error during analysis", e);
			errorHandler.reportError(getCurrentTokenizer(), e);
			// UNDONE: errorHandler.commitChanges(); (committed by scheduler)
		} catch (RuntimeException e) {
			Environment.logException("Unexpected exception during analysis", e);
			errorHandler.reportError(getCurrentTokenizer(), e);
			// UNDONE: errorHandler.commitChanges(); (committed by scheduler)
		}
	}
	
	// Language information

	public Language getLanguage() {
		return language;
	}
	
	public AstNodeLocator getSourcePositionLocator() {
		return new AstNodeLocator(this);
	}
	
	public ILanguageSyntaxProperties getSyntaxProperties() {
		return syntaxProperties;
	}

	public IAnnotationTypeInfo getAnnotationTypeInfo() {
		return new SimpleAnnotationTypeInfo();
	}

	/**
	 * Gets the token iterator for this parse controller. The current
	 * implementation assumes it is only used for syntax highlighting.
	 * 
	 * @return The token iterator, or an empty token iterator
	 * 
	 * @throws OperationCanceledException
	 *             Thrown if coloring is not allowed or no parse stream is
	 *             available at the time of invocation.
	 */
	public Iterator<IToken> getTokenIterator(IRegion region) {
		return getTokenIterator(region, false);
	}
	
	public Iterator<IToken> getTokenIterator(IRegion region, boolean force) {
		// Threading concerns:
		// - the colorer runs in the main thread and should not be blocked by ANY lock
		// - CANNOT acquire parse lock:
		//   - a parser thread with a parse lock may forceRecolor(), acquiring the colorer queue lock 
		//   - a parser thread with a parse lock may need main thread acess to report errors
		
		ITokenizer stream = currentParseStream;		
		IDocument document = editor == null ? null : editor.getDocument();
		
		if (!force && (stream == null || disallowColorer
				|| (document != null && stream.getInput().getTokenCount() != document.getLength()))) {
			return SGLRTokenIterator.EMPTY;
		} else if (stream.getTokenCount() == 0 || getCurrentAst() == null) {
			// Parse hasn't succeeded yet, consider the entire stream as one big token
			stream.addToken(new SGLRToken(stream, region.getOffset(), stream.getTokenCount() - 1,
					IToken.TK_UNKNOWN));
		}
		
		// UNDONE: Cannot disable colorer afterwards, need it to remove error markers
		// disallowColorer = true;
		
		return new SGLRTokenIterator(stream, region);
	}

	protected void forceRecolor(boolean wasStartupParsed) {
		assert !parseLock.isHeldByCurrentThread() || !Environment.isMainThread() || !wasStartupParsed
			: "Parse lock may not be locked: dead lock risk with colorer queue lock";
		try {
			// System.out.println("FORCECOLOR! " + System.currentTimeMillis()); // DEBUG
			// UNDONE: no longer acquiring parse lock from colorer
			disallowColorer = false;
			TokenColorer.initLazyColors(this);
			if (editor != null)
				editor.getEditor().updateColoring(new Region(0, currentParseStream.getTokenCount() - 1));
		} catch (RuntimeException e) {
			Environment.logException("Could reschedule syntax highlighter", e);
		}
	}

	protected ITokenizer getCurrentTokenizer() {
		IStrategoTerm ast = getCurrentAst();
		return ast == null ? null : ImploderAttachment.getLeftToken(ast).getTokenizer();
	}
	
	private void forceInitialParse() {
		if (isStartupParsed) return;
		
		parseLock.lock();
		try {
			if (isStartupParsed) return;
		} finally {
			parseLock.unlock();
		}
		try {
			parse(getContents(), new NullProgressMonitor());
		} catch (IOException e) {
			Environment.logException("Forced parse failed", e);
		} catch (CoreException e) {
			if (e.getMessage().contains("Resource is out of sync") || e.getMessage().contains("does not exist")) // don't log these
				return;
			Environment.logException("Forced parse failed", e);
		}
	}

	private String getContents() throws CoreException, IOException {
		// This is not a bottleneck right now, but could be optimized to use something
		// like descriptor.getParseController().lastEditor.getDocument().getContents()
		InputStream input = getResource().getContents();
		InputStreamReader reader = new InputStreamReader(input);
		StringBuilder result = new StringBuilder();
		char[] buffer = new char[2048];
		
		for (int read = 0; read != -1; read = reader.read(buffer))
		        result.append(buffer, 0, read);
		
		return result.toString();
	}
}
