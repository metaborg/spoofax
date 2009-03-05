package org.strategoxt.imp.runtime.parser;

import java.io.IOException;
import java.util.Iterator;

import lpg.runtime.IToken;
import lpg.runtime.PrsStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.spoofax.jsglr.SGLR;
import org.spoofax.jsglr.SGLRException;
import org.spoofax.jsglr.TokenExpectedException;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
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
	private final TokenKindManager tokenManager = new TokenKindManager();
	
	private final JSGLRI parser;
	
	private final Language language;
	
	private final ILanguageSyntaxProperties syntaxProperties;
	
	private ParseErrorHandler errorHandler;
	
	private AstNode currentAst;
	
	private ISourceProject project;
	
	private IPath path;

	// Simple accessors
	
	public final AstNode getCurrentAst() { 
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
    	return project == null
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
		errorHandler = new ParseErrorHandler(messages, parser.getTokenizer());
    }

	public AstNode parse(String input, boolean scanOnly, IProgressMonitor monitor) {
		if (getPath() == null)
		    throw new IllegalStateException("SGLR parse controller not initialized");

		try {
			errorHandler.clearErrors();
			currentAst = null;
			
			Debug.startTimer();
			
			char[] inputChars = input.toCharArray();
			String filename = getPath().toPortableString();
			
			currentAst = parser.parse(inputChars, filename);
			ATerm asfix = parser.parseNoImplode(inputChars, filename);
			errorHandler.reportNonFatalErrors(asfix);
			
			Debug.stopTimer("File parsed: " + filename);
			
		} catch (TokenExpectedException e) {
			errorHandler.reportError(e);
		} catch (BadTokenException e) {
			errorHandler.reportError(e);
		} catch (SGLRException e) {
			errorHandler.reportError(e);
		} catch (IOException e) {
			errorHandler.reportError(e);
		} catch (RuntimeException e) {
			Environment.logException("Unexpected error during parsing", e);
			errorHandler.reportError(e);
		}
		
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
			errorHandler.reportError(e);
		} catch (RuntimeException e) {
			Environment.logException("Unexpected exception during analysis", e);
			errorHandler.reportError(e);
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
		// TODO: Return a damaged token stream on parse errors
		PrsStream stream = parser.getParseStream();
		if (stream.getTokens().size() == 0 || getCurrentAst() == null) {
			// Parse hasn't succeeded yet, consider the entire stream as one big token
			stream.addToken(new SGLRToken(stream, region.getOffset(), stream.getStreamLength() - 1,
					TokenKind.TK_UNKNOWN.ordinal()));
		}
		return new SGLRTokenIterator(stream, region);
	}
}
