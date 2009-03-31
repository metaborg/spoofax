package org.strategoxt.imp.runtime.parser;

import java.io.IOException;
import java.util.Iterator;

import lpg.runtime.IToken;
import lpg.runtime.PrsStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.parser.SimpleAnnotationTypeInfo;
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IRegion;
import org.spoofax.jsglr.BadTokenException;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.ParseTimeoutException;
import org.spoofax.jsglr.SGLR;
import org.spoofax.jsglr.SGLRException;
import org.spoofax.jsglr.TokenExpectedException;
import org.spoofax.jsglr.Tools;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.ISourceInfo;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.ast.AstNodeLocator;
import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenIterator;
import org.strategoxt.imp.runtime.parser.tokens.TokenKind;
import org.strategoxt.imp.runtime.parser.tokens.TokenKindManager;
import org.strategoxt.imp.runtime.services.StrategoFeedback;

import aterm.ATerm;

/**
 * IMP parse controller for an SGLR parser; instantiated for a particular source file.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 * @author Karl Trygve Kalleberg <karltk add strategoxt.org>
 */
public class SGLRParseController implements IParseController, ISourceInfo {
	
	private final static int PARSE_TIMEOUT = 4 * 1000;

	private final TokenKindManager tokenManager = new TokenKindManager();
	
	private final ParseErrorHandler errorHandler = new ParseErrorHandler(this);
	
	private final JSGLRI parser;
	
	private final Language language;
	
	private final ILanguageSyntaxProperties syntaxProperties;
	
	private AstNode currentAst;
	
	private ISourceProject project;
	
	private IPath path;

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
    
    public IResource getResource() {
    	IPath path = getPath();
		IProject project = getProject().getRawProject();
		path = path.removeFirstSegments(path.matchingFirstSegments(project.getLocation()));
		return project.getFile(path);
    }
	
	// Parsing and initialization
    
    static {
    	SGLR.setWorkAroundMultipleLookahead(true);
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
    }

    public void initialize(IPath filePath, ISourceProject project,
    		IMessageHandler messages) {
		this.path = filePath;
		this.project = project;
		this.errorHandler.setMessages(messages);
    }

	public AstNode parse(String input, boolean scanOnly, IProgressMonitor monitor) {
		if (getPath() == null)
		    throw new IllegalStateException("SGLR parse controller not initialized");

		String filename = getPath().toPortableString();
		
		// XXX: SGLR.asyncAbort() is never called until the old parse actually completes
		//      need to use the monitor to get this working
		getParser().asyncAbort();
		
		IResource resource = getResource();
		try {
			Job.getJobManager().beginRule(resource, monitor); // enter lock

			// TODO: Wait with parsing until token completes? and do it again for timer?
			
			// TODO: set update field, or use AstMessageHandler if all else fails
			
			ATerm asfix;
			
			Debug.startTimer();
			
			char[] inputChars = input.toCharArray();
				
			currentAst = parser.parse(inputChars, filename);
			
			if (monitor.isCanceled()) return null;
			asfix = parser.parseNoImplode(inputChars, filename);
			if (monitor.isCanceled()) return null;
			
			// (must not be synchronized; uses workspace lock)
			errorHandler.clearErrors();
			errorHandler.reportNonFatalErrors(parser.getTokenizer(), asfix);
				
			Debug.stopTimer("File parsed: " + filename);
		} catch (TokenExpectedException e) {
			errorHandler.clearErrors(); // (may not be synchronized; uses workspace lock)
			errorHandler.reportError(parser.getTokenizer(), e);
		} catch (ParseTimeoutException e) {
			// TODO: Don't show stack trace for this
			if (monitor.isCanceled()) return null;
			errorHandler.clearErrors();
			errorHandler.reportError(parser.getTokenizer(), e);
		} catch (BadTokenException e) {
			errorHandler.clearErrors();
			errorHandler.reportError(parser.getTokenizer(), e);
		} catch (SGLRException e) {
			errorHandler.clearErrors();
			errorHandler.reportError(parser.getTokenizer(), e);
		} catch (IOException e) {
			errorHandler.clearErrors();
			errorHandler.reportError(parser.getTokenizer(), e);
		} catch (OperationCanceledException e) {
			return null;
		} catch (RuntimeException e) {
			Environment.logException("Internal parser error", e);
			errorHandler.reportError(parser.getTokenizer(), e);
		} finally {
			Job.getJobManager().endRule(resource);
		}
		
		if (!monitor.isCanceled())
			updateFeedBack();

		return currentAst;
	}

	private void updateFeedBack() {
		// HACK: Need to call IModelListener.update manually; the IMP extension point is not implemented?!
		try {
			StrategoFeedback feedback = Environment.getDescriptor(getLanguage()).getStrategoFeedback();
			if (feedback != null) feedback.asyncUpdate(this, null);
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
	
	public ISourcePositionLocator getNodeLocator() {
		return new AstNodeLocator();
	}
	
	public ILanguageSyntaxProperties getSyntaxProperties() {
		return syntaxProperties;
	}

	public IAnnotationTypeInfo getAnnotationTypeInfo() {
		return new SimpleAnnotationTypeInfo();
	}

	public Iterator<IToken> getTokenIterator(IRegion region) {
		PrsStream stream = parser.getParseStream();
		if (stream.getTokens().size() == 0 || getCurrentAst() == null) {
			// Parse hasn't succeeded yet, consider the entire stream as one big token
			stream.addToken(new SGLRToken(stream, region.getOffset(), stream.getStreamLength() - 1,
					TokenKind.TK_UNKNOWN.ordinal()));
		}
		
		return new SGLRTokenIterator(stream, region);
	}
}
