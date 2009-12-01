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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
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
import org.strategoxt.imp.runtime.services.StrategoObserver;

import aterm.ATerm;

/**
 * IMP parse controller for an SGLR parser; instantiated for a particular source file.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 * @author Karl Trygve Kalleberg <karltk add strategoxt.org>
 */
public class SGLRParseController implements IParseController {
	
	private final static int PARSE_TIMEOUT = 4 * 1000;

	private final TokenKindManager tokenManager = new TokenKindManager();
	
	private final ParseErrorHandler errorHandler = new ParseErrorHandler(this);
	
	private final ReentrantLock parseLock = new ReentrantLock(true);
	
	private final JSGLRI parser;
	
	private final Language language;
	
	private final ILanguageSyntaxProperties syntaxProperties;
	
	private volatile RootAstNode currentAst;
	
	private volatile IPrsStream currentParseStream;
	
	private ISourceProject project;
	
	private IPath path;
	
	private EditorState editor;
	
	private volatile boolean isStartupParsed;
	
	private volatile boolean isColorerQueued;

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

    public void initialize(IPath filePath, ISourceProject project,
    		IMessageHandler messages) {
    	
		this.path = filePath;
		this.project = project;
		this.errorHandler.setMessages(messages);
    }
    
    @Deprecated
    public final AstNode parse(String input, boolean scanOnly, IProgressMonitor monitor) {
    	return parse(input, monitor);
    }

	public AstNode parse(String input, IProgressMonitor monitor) {
		if (input.length() == 0) {
			currentParseStream = null;
			return currentAst = null;
		}
		
		if (getPath() == null)
		    throw new IllegalStateException("SGLR parse controller not initialized");

		String filename = getPath().toPortableString();
		
		// XXX: SGLR.asyncAbort() is never called until the old parse actually completes
		//      need to intercept cancellation events in the running thread's monitor instead
		getParser().asyncAbort();
		
		IResource resource = getResource();
		try {
			// while (PlatformUI.getWorkbench().getDisplay().readAndDispatch());
			
			// Can't do resource locking when Eclipse is still starting up; causes deadlock
			currentParseStream = null; // avoid twitchy colorer
			if (isStartupParsed)
				Job.getJobManager().beginRule(resource, monitor); // enter lock
			assert !parseLock.isHeldByCurrentThread() : "Parse lock must be locked after resource lock";
			parseLock.lock();
			// System.out.println("PARSE! " + System.currentTimeMillis()); // DEBUG
			
			Debug.startTimer();
			
			char[] inputChars = input.toCharArray();
				
			if (monitor.isCanceled()) return null;
			ATerm asfix = parser.parseNoImplode(inputChars, filename);
			if (monitor.isCanceled()) return null;
			RootAstNode ast = parser.internalImplode(asfix);

			if (isStartupParsed) {
				// Threading concerns:
				//   - must not be synchronized; uses resource lock
				//   - when ran directly from the main thread, it may block other
				//     UI threads that already have a lock on my resource,
				//     but are waiting to run in the UI thread themselves
				//   - reporting errors at startup may trigger the above condition,
				//     at least for files with an in-workspace editor(?)
				//
				// TODO: Consider using Display.asyncExec for reporting errors;
				//       this could be integrated into the AstMessageHandler class!
				errorHandler.setRecoveryAvailable(true);
				errorHandler.clearErrors();
				errorHandler.reportNonFatalErrors(parser.getTokenizer(), asfix);
			}
			
			// TODO: Delay parse error markers for newly typed text
			//       (the new ast should then also be delayed as to get the old coloring)
			
			currentAst = ast;
			currentParseStream = parser.getParseStream();
				
			Debug.stopTimer("File parsed: " + filename);
		} catch (ParseTimeoutException e) {
			// TODO: Don't show stack trace for this
			if (monitor.isCanceled()) return null;
			reportGenericException(e);
		} catch (TokenExpectedException e) {
			reportGenericException(e);
		} catch (BadTokenException e) {
			reportGenericException(e);
		} catch (SGLRException e) {
			reportGenericException(e);
		} catch (IOException e) {
			reportGenericException(e);
		} catch (OperationCanceledException e) {
			return null;
		} catch (RuntimeException e) {
			Environment.logException("Internal parser error", e);
			errorHandler.reportError(parser.getTokenizer(), e);
		} finally {
			errorHandler.commitChanges();
			
			if (isStartupParsed) Job.getJobManager().endRule(resource);
			else isStartupParsed = true;
			
			IPrsStream parseStream = currentParseStream;
			boolean mustRecolor = isColorerQueued && editor != null && parseStream != null;
			isColorerQueued = false;
			
			parseLock.unlock();
			if (mustRecolor) forceRecolor(parseStream);
		}
		
		if (!monitor.isCanceled())
			updateFeedBack();

		return currentAst;
	}

	private void reportGenericException(Exception e) {
		errorHandler.clearErrors();
		errorHandler.setRecoveryAvailable(false);
		errorHandler.reportError(parser.getTokenizer(), e);
	}

	private void updateFeedBack() {
		// HACK: Need to call IModelListener.update manually; the IMP extension point is not implemented?!
		// TODO: use UniversalEditor.addModelListener() instead?
		//       must attach to an editor and remember to which ones we already attached
		try {
			StrategoObserver feedback = Environment.getDescriptor(getLanguage()).getStrategoObserver();
			if (feedback != null) feedback.asyncUpdate(this);
		} catch (BadDescriptorException e) {
			Environment.logException("Unexpected error during analysis", e);
			errorHandler.reportError(parser.getTokenizer(), e);
		} catch (RuntimeException e) {
			Environment.logException("Unexpected exception during analysis", e);
			errorHandler.reportError(parser.getTokenizer(), e);
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
	 * Gets the token iterator for this parse controller.
	 * The current implementation assumes it is only used for
	 * syntax highlighting.
	 */
	public Iterator<IToken> getTokenIterator(IRegion region) {
		IPrsStream stream;
		isColorerQueued = true;
		parseLock.lock();
		try {
			stream = getIPrsStream();
		} finally {
			isColorerQueued = false;
			// System.out.println("COLOR! " + System.currentTimeMillis()); // DEBUG
			parseLock.unlock();
		}
		
		if (stream == null) {
			return SGLRTokenIterator.EMPTY;
		} else if (stream.getTokens().size() == 0 || getCurrentAst() == null) {
			// Parse hasn't succeeded yet, consider the entire stream as one big token
			stream.addToken(new SGLRToken(stream, region.getOffset(), stream.getStreamLength() - 1,
					TokenKind.TK_UNKNOWN.ordinal()));
		}
		
		return new SGLRTokenIterator(stream, region);
	}

	private void forceRecolor(IPrsStream parseStream) {
		// System.out.println("RECOLOR! " + System.currentTimeMillis()); // DEBUG
		isColorerQueued = true;
		try {
			isColorerQueued = false;
			// System.out.println("RECOLOR GO! " + System.currentTimeMillis()); // DEBUG
			assert !parseLock.isHeldByCurrentThread() : "Parse lock may not be locked: dead lock risk with colorer queue";
			editor.getEditor().updateColoring(new Region(0, parseStream.getStreamLength() - 1));
		} catch (RuntimeException e) {
			Environment.logException("Could reschedule syntax highlighter", e);
		}
	}

	/**
	 * Get a parse stream for the current file, enforcing a new parse
	 * if it hasn't been parsed before.
	 */
	protected IPrsStream getIPrsStream() {
		if (currentParseStream != null)
			return currentParseStream;

		forceInitialParse();
		return currentParseStream;
	}

	private void forceInitialParse() {
		parseLock.lock();
		try {
			if (isStartupParsed)
				return;
	
			parse(getContents(), new NullProgressMonitor());
		} catch (IOException e) {
			Environment.logException("Forced parse failed", e);
		} catch (CoreException e) {
			Environment.logException("Forced parse failed", e);
		} finally {
			parseLock.unlock();
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
