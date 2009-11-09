package org.strategoxt.imp.runtime.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

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
import org.spoofax.jsglr.BadTokenException;
import org.spoofax.jsglr.NoRecoveryRulesException;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.ParseTimeoutException;
import org.spoofax.jsglr.SGLR;
import org.spoofax.jsglr.SGLRException;
import org.spoofax.jsglr.StructureRecoveryAlgorithm;
import org.spoofax.jsglr.TokenExpectedException;
import org.spoofax.jsglr.Tools;
import org.strategoxt.imp.runtime.Debug;
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
	
	private final JSGLRI parser;
	
	private final Language language;
	
	private final ILanguageSyntaxProperties syntaxProperties;
	
	private RootAstNode currentAst;
	
	private ISourceProject project;
	
	private IPath path;
	
	private boolean isStartupParsed;

	// Simple accessors
	
	public final synchronized AstNode getCurrentAst() { 
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
	
	// Parsing and initialization
    
    static {
    	SGLR.setWorkAroundMultipleLookahead(true);
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

	public AstNode parse(String input, boolean scanOnly, IProgressMonitor monitor) {
		if (input.length() == 0)
			return currentAst = null;
		
		if (getPath() == null)
		    throw new IllegalStateException("SGLR parse controller not initialized");

		String filename = getPath().toPortableString();
		
		// XXX: SGLR.asyncAbort() is never called until the old parse actually completes
		//      need to intercept cancellation events in the running thread's monitor instead
		getParser().asyncAbort();
		
		IResource resource = getResource();
		try {
			// while (PlatformUI.getWorkbench().getDisplay().readAndDispatch());
			
			if (isStartupParsed)
				Job.getJobManager().beginRule(resource, monitor); // enter lock
			
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
				errorHandler.gatherNonFatalErrors(parser.getTokenizer(), asfix);
				errorHandler.clearErrors();
				errorHandler.applyMarkers();
			}
			
			currentAst = ast;
				
			Debug.stopTimer("File parsed: " + filename);
		} catch (ParseTimeoutException e) {
			// TODO: Don't show stack trace for this
			if (monitor.isCanceled()) return null;
			errorHandler.clearErrors();
			errorHandler.setRecoveryAvailable(false);
			errorHandler.reportError(parser.getTokenizer(), e);
			errorHandler.applyMarkers();
		} catch (TokenExpectedException e) {
			errorHandler.clearErrors(); // (must not be synchronized; uses workspace lock)
			errorHandler.reportError(parser.getTokenizer(), e);
			errorHandler.applyMarkers();
		} catch (BadTokenException e) {
			errorHandler.clearErrors();
			errorHandler.setRecoveryAvailable(false);
			errorHandler.reportError(parser.getTokenizer(), e);
			errorHandler.applyMarkers();
		} catch (SGLRException e) {
			errorHandler.clearErrors();
			errorHandler.setRecoveryAvailable(false);
			errorHandler.reportError(parser.getTokenizer(), e);
			errorHandler.applyMarkers();
		} catch (IOException e) {
			errorHandler.clearErrors();
			errorHandler.setRecoveryAvailable(false);
			errorHandler.reportError(parser.getTokenizer(), e);
			errorHandler.applyMarkers();
		} catch (OperationCanceledException e) {
			return null;
		} catch (RuntimeException e) {
			Environment.logException("Internal parser error", e);
			errorHandler.reportError(parser.getTokenizer(), e);
			errorHandler.applyMarkers();
		} finally {
			if (isStartupParsed)
				Job.getJobManager().endRule(resource);
			else 
				isStartupParsed = true;
		}
		
		if (!monitor.isCanceled())
			updateFeedBack();

		return currentAst;
	}

	private void updateFeedBack() {
		// HACK: Need to call IModelListener.update manually; the IMP extension point is not implemented?!
		// TODO: use UniversalEditor.addModelListener() instead?
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
	
	public AstNodeLocator getNodeLocator() {
		return new AstNodeLocator();
	}
	
	public ILanguageSyntaxProperties getSyntaxProperties() {
		return syntaxProperties;
	}

	public IAnnotationTypeInfo getAnnotationTypeInfo() {
		return new SimpleAnnotationTypeInfo();
	}

	public Iterator<IToken> getTokenIterator(IRegion region) {
		IPrsStream stream = getParseStream();
		
		if (stream == null) {
			return SGLRTokenIterator.EMPTY;
		} else if (stream.getTokens().size() == 0 || getCurrentAst() == null) {
			// Parse hasn't succeeded yet, consider the entire stream as one big token
			stream.addToken(new SGLRToken(stream, region.getOffset(), stream.getStreamLength() - 1,
					TokenKind.TK_UNKNOWN.ordinal()));
		}
		
		return new SGLRTokenIterator(stream, region);
	}

	/**
	 * Get a parse stream for the current file, enforcing a new parse
	 * if it hasn't been parsed before.
	 */
	public IPrsStream getParseStream() {
		try {
			IPrsStream stream = parser.getParseStream();
			
			if (stream == null) {
				InputStream input = getResource().getContents();
	            InputStreamReader reader = new InputStreamReader(input);
	            StringBuilder contents = new StringBuilder();
	            char[] buffer = new char[2048];
	            
	            for (int read = 0; read != -1; read = reader.read(buffer))
	                    contents.append(buffer, 0, read);
	
				parse(contents.toString(), true, new NullProgressMonitor());
				stream = parser.getParseStream();
			}
			return stream;
		} catch (IOException e) {
			return null;
		} catch (CoreException e) {
			return null;
		}
	}
}
