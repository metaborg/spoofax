package org.strategoxt.imp.runtime.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.spoofax.jsglr.BadTokenException;
import org.spoofax.jsglr.NoRecoveryRulesException;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.ParseTimeoutException;
import org.spoofax.jsglr.SGLRException;
import org.spoofax.jsglr.StartSymbolException;
import org.spoofax.jsglr.StructureRecoveryAlgorithm;
import org.spoofax.jsglr.TokenExpectedException;
import org.spoofax.jsglr.Tools;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.ast.AstNodeLocator;
import org.strategoxt.imp.runtime.parser.ast.RootAstNode;
import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenIterator;
import org.strategoxt.imp.runtime.parser.tokens.TokenKind;
import org.strategoxt.imp.runtime.parser.tokens.TokenKindManager;
import org.strategoxt.imp.runtime.services.MetaFileReader;
import org.strategoxt.imp.runtime.services.StrategoObserver;

import aterm.ATerm;

/**
 * IMP parse controller for an SGLR parser; instantiated for a particular source file.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 * @author Karl Trygve Kalleberg <karltk add strategoxt.org>
 */
public class SGLRParseController implements IParseController {
	
	private static final int PARSE_TIMEOUT = 4 * 1000;
	
	private final TokenKindManager tokenManager = new TokenKindManager();
	
	private final ReentrantLock parseLock = new ReentrantLock(true);
	
	private final JSGLRI parser;
	
	private final Language language;
	
	private final ILanguageSyntaxProperties syntaxProperties;
	
	private final ParseErrorHandler errorHandler = new ParseErrorHandler(this);
	
	private volatile RootAstNode currentAst;
	
	private volatile IPrsStream currentParseStream;
	
	private ISourceProject project;
	
	private IPath path;
	
	private EditorState editor;
	
	private String metaSyntax;
	
	private volatile boolean isStartupParsed;
	
	private volatile boolean disallowColorer;

	// Simple accessors
	
	/**
	 * Gets the last parsed AST for this editor, or
	 * tries to parse the file if it hasn't been parsed yet.
	 * 
	 * @return The parsed AST, or null if the file could not be parsed (yet).
	 */
	public final AstNode getCurrentAst() {
		if (currentAst == null) forceInitialParse();
		return currentAst;
	}

	public final ISourceProject getProject() {
		return project;
	}
	
	public final JSGLRI getParser() {
		return parser;
	}
	
	protected ReentrantLock getParseLock() {
		return parseLock;
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
		IProject project = getProject().getRawProject();
		path = path.removeFirstSegments(path.matchingFirstSegments(project.getLocation()));
		return project.getFile(path);
    }
    
    public void setEditor(EditorState editor) {
		this.editor = editor;
	}
    
    public EditorState getEditor() {
		return editor;
	}
	
	// Parsing and initialization
    
    static {
    	if (!Debug.ENABLED)
    		Tools.setTimeout(PARSE_TIMEOUT);
    }
    
    /**
     * Create a new SGLRParseController.
     * 
     * @param language      The name of the language, as registered in the {@link LanguageRegistry}.
     * @param startSymbol	The start symbol of this grammar, or null.
     */
    public SGLRParseController(Language language, ILanguageSyntaxProperties syntaxProperties,
			String startSymbol) {
    	
    	this.language = language;
    	this.syntaxProperties = syntaxProperties;
    	
    	ParseTable table = Environment.getParseTable(language);
		parser = new JSGLRI(table, startSymbol, this, tokenManager);
		parser.setKeepAmbiguities(true);
		try {
			parser.setRecoverHandler(new StructureRecoveryAlgorithm());
		} catch (NoRecoveryRulesException e) {
			Environment.logException("Warning: no recovery rules available for " + language.getName());
		}
    }

    public void initialize(IPath filePath, ISourceProject project, IMessageHandler messages) {
		this.path = filePath;
		this.project = project;
    }
    
    @Deprecated
    public final AstNode parse(String input, boolean scanOnly, IProgressMonitor monitor) {
    	return parse(input, monitor);
    }

	public AstNode parse(String input, final IProgressMonitor monitor) {
		disallowColorer = true; // isStartupParsed;
		if (input.length() == 0) {
			currentParseStream = null;
			return currentAst = null;
		}
		
		// TODO: Eclipse 3.5 issues: static icons, dynamic icons
		if (getPath() == null)
		    throw new IllegalStateException("SGLR parse controller not initialized");
		
		// XXX: SGLR.asyncAbort() is never called until the old parse actually completes
		//      (or is it?!)
		//      need to intercept cancellation events in the running thread's monitor instead
		getParser().asyncAbort();

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
			char[] inputChars = input.toCharArray();
			
			Debug.startTimer();
				
			if (monitor.isCanceled()) return null;
			ATerm asfix = parser.parseNoImplode(inputChars, filename);
			if (monitor.isCanceled()) return null;
			RootAstNode ast = parser.internalImplode(asfix);

			errorHandler.clearErrors();
			errorHandler.setRecoveryAvailable(true);
			errorHandler.gatherNonFatalErrors(parser.getTokenizer(), asfix);
			
			currentAst = ast;
			currentParseStream = parser.getParseStream();
				
			Debug.stopTimer("File parsed: " + filename);
			
			// TODO: is coloring, then error marking best?
		
		} catch (StartSymbolException e) {
			if (metaSyntax != null) {
				// Unmanaged parse tables may have different start symbols;
				// try again without the standard start symbol
				parser.setStartSymbol(null);
				return parse(input, monitor);
			}
		} catch (ParseTimeoutException e) {
			// TODO: Don't show stack trace for this
			if (monitor.isCanceled()) return null;
			reportGenericException(errorHandler, e);
		} catch (TokenExpectedException e) {
			reportGenericException(errorHandler, e);
		} catch (BadTokenException e) {
			reportGenericException(errorHandler, e);
		} catch (SGLRException e) {
			reportGenericException(errorHandler, e);
		} catch (IOException e) {
			reportGenericException(errorHandler, e);
		} catch (OperationCanceledException e) {
			return null;
		} catch (RuntimeException e) {
			Environment.logException("Internal parser error", e);
			errorHandler.reportError(parser.getTokenizer(), e);
		} finally {
			try {
				onParseCompleted(monitor);
			} catch (RuntimeException e) {
				Environment.logException("Exception in post-parse events", e);
			}
			
			//if (isStartupParsed) Job.getJobManager().endRule(resource);
			isStartupParsed = true;			
			parseLock.unlock();
		}

		return monitor.isCanceled() ? null : currentAst;
	}

	private void processMetaFile() {
		String metaFile = getPath().removeFileExtension().addFileExtension("meta").toOSString();
		String metaSyntax = MetaFileReader.readSyntax(metaFile);
		if (metaSyntax != this.metaSyntax) {
			ParseTable table = Environment.getUnmanagedParseTable(metaSyntax);
			if (table == null) {
				Environment.logException("Could not find descriptor or unmanaged parse table for language " + metaSyntax);
			} else {
				parser.setParseTable(table);				
			}
			this.metaSyntax = metaSyntax;
		}
	}

	private void onParseCompleted(final IProgressMonitor monitor) {
		assert parseLock.isHeldByCurrentThread();
		
		if (!monitor.isCanceled() && currentParseStream != null)
			forceRecolor();
		
		// TODO: Delay parse error markers for newly typed text
		
		// Threading concerns:
		// must never block the main thread with a lock here since
		// it must be available to draw error markers
		
		if (!Environment.isMainThread() || !isStartupParsed) {
			if (!monitor.isCanceled()) {
				// Note that a resource lock is acquired here
				errorHandler.commitMultiErrorLineAdditions();
				errorHandler.commitDeletions();
			}
			
			// Removed markers seem to require recoloring:
			//if (!monitor.isCanceled() /*&& currentParseStream != null*/ && editor != null)
			//	AstMessageHandler.processEditorRecolorEvents(editor.getEditor());
			
			if (!monitor.isCanceled())
				errorHandler.scheduleCommitAdditions();
		} else {
			// Report errors again later when not in main thread
			// (this state shouldn't be reachable from normal operation)
			if (editor != null)
				editor.scheduleParserUpdate(ParseErrorHandler.PARSE_ERROR_DELAY);
		}
		
		if (!monitor.isCanceled())
			scheduleObserverUpdate(errorHandler);
	}

	private void reportGenericException(ParseErrorHandler errorHandler, Exception e) {
		errorHandler.clearErrors();
		errorHandler.setRecoveryAvailable(false);
		errorHandler.reportError(parser.getTokenizer(), e);
	}

	private void scheduleObserverUpdate(ParseErrorHandler errorHandler) {
		// We bypass the UniversalEditor IModelListener for this,
		// allowing a bit more (timing) control and ease of use (wrt dynamic reloading)
		try {
			StrategoObserver feedback = Environment.getDescriptor(getLanguage()).getStrategoObserver();
			if (feedback != null) feedback.scheduleUpdate(this);
		} catch (BadDescriptorException e) {
			Environment.logException("Unexpected error during analysis", e);
			errorHandler.reportError(parser.getTokenizer(), e);
			// UNDONE: errorHandler.commitChanges(); (committed by scheduler)
		} catch (RuntimeException e) {
			Environment.logException("Unexpected exception during analysis", e);
			errorHandler.reportError(parser.getTokenizer(), e);
			// UNDONE: errorHandler.commitChanges(); (committed by scheduler)
		}
	}
	
	// Language information

	public Language getLanguage() {
		return language;
	}
	
	public AstNodeLocator getSourcePositionLocator() {
		return new AstNodeLocator();
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
	 * @return The token iterator, or an empty token iterator if coloring is not
	 *         allowed or no parse stream is available the time of invocation
	 */
	public Iterator<IToken> getTokenIterator(IRegion region) {
		return getTokenIterator(region, false);
	}
	
	
	public Iterator<IToken> getTokenIterator(IRegion region, boolean notForColorer) {
		// Threading concerns:
		// - the colorer runs in the main thread and should not be blocked by ANY lock
		// - CANNOT acquire parse lock:
		//   - a parser thread with a parse lock may forceRecolor(), acquiring the colorer queue lock 
		//   - a parser thread with a parse lock may need main thread acess to report locks
		
		
		IPrsStream stream = currentParseStream;
		
		if (!notForColorer && (stream == null || disallowColorer || (editor != null && stream.getILexStream().getStreamLength() != editor.getDocument().getLength()))) {
			return SGLRTokenIterator.EMPTY;
		} else if (stream.getTokens().size() == 0 || getCurrentAst() == null) {
			// Parse hasn't succeeded yet, consider the entire stream as one big token
			stream.addToken(new SGLRToken(stream, region.getOffset(), stream.getStreamLength() - 1,
					TokenKind.TK_UNKNOWN.ordinal()));
		}
		
		// UNDONE: Cannot disable colorer afterwards, need it to remove error markers
		// disallowColorer = true;
		
		return new SGLRTokenIterator(stream, region);
	}

	protected void forceRecolor() {
		assert !parseLock.isHeldByCurrentThread() || !Environment.isMainThread() || !isStartupParsed
			: "Parse lock may not be locked: dead lock risk with colorer queue lock";
		try {
			// System.out.println("FORCECOLOR! " + System.currentTimeMillis()); // DEBUG
			// UNDONE: no longer acquiring parse lock from colorer
			disallowColorer = false;
			if (editor != null)
				editor.getEditor().updateColoring(new Region(0, currentParseStream.getStreamLength() - 1));
		} catch (RuntimeException e) {
			Environment.logException("Could reschedule syntax highlighter", e);
		}
	}

	protected IPrsStream getIPrsStream() {
		return currentParseStream;
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
			if (e.getMessage().contains("Resource is out of sync")) // don't log these
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
