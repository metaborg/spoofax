package org.strategoxt.imp.runtime.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lpg.runtime.IToken;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
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
import org.spoofax.jsglr.SGLR;
import org.spoofax.jsglr.TokenExpectedException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.ast.AstNodeLocator;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenIterator;
import org.strategoxt.imp.runtime.parser.tokens.TokenKindManager;

/**
 * IMP parse controller for an SGLR parser.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 * @author Karl Trygve Kalleberg <karltk add strategoxt.org>
 */
public class SGLRParseController implements IParseController {
	private final List<String> problemMarkerTypes = new ArrayList<String>();
	
	private final TokenKindManager tokenManager = new TokenKindManager();
	
	private final SGLRParser parser;
	
	private final Language language;
	
	private final ILanguageSyntaxProperties syntaxProperties;
	
	private AstNode currentAst;
	
	private ISourceProject project;
	
	private IPath path;
	
	private IMessageHandler messages;

	// Simple accessors
	
	public final AstNode getCurrentAst() { 
		return currentAst;
	}

	public final ISourceProject getProject() {
		return project;
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
	
	// Parsing and initialization
    
    static {
    	// UNDONE: UniversalEditor.fAlwaysRepaint = true;
    	
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
    	
    	parser = new SGLRParser(this, tokenManager, Environment.getParseTable(language), startSymbol);
    }

    public void initialize(IPath filePath, ISourceProject project,
    		IMessageHandler handler) {
		this.path = filePath;
		this.project = project;
		this.messages = handler;
    }

	public AstNode parse(String input, boolean scanOnly, IProgressMonitor monitor) {
		try {
			// TODO2: Optimization - don't produce AST if scanOnly is true
			currentAst = parser.parse(input.toCharArray(), getPath().toPortableString());
			messages.clearMessages();
		} catch (TokenExpectedException x) {
			reportParseError(x);
		} catch (BadTokenException x) {
			reportParseError(x);
		} catch (Exception x) {
			// catches SGLRException; NotImplementedException; IOException
			reportParseError(x);
			Activator.log(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.OK, "Fatal error during parsing", x));
		}
		
		return currentAst;
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
		return new SGLRTokenIterator(parser.getParseStream(), region);
	}
	
	// Problem markers
	
	public final List<String> getProblemMarkerTypes() {
		return problemMarkerTypes;
	}
	
	public void addProblemMarkerType(String problemMarkerType) {
		problemMarkerTypes.add(problemMarkerType);
	}
	
	public void removeProblemMarkerType(String problemMarkerType) {
		problemMarkerTypes.remove(problemMarkerType);
	}
	
	// Error reporting
	
	private void reportError(String message, IToken token) {
		messages.handleSimpleMessage(
				message, token.getStartOffset(), token.getEndOffset(),
				token.getColumn(), token.getEndColumn(), token.getLine(), token.getEndLine());
	}
	
	private void reportParseError(TokenExpectedException exception) {
		String message = exception.getShortMessage();
		IToken token = parser.getTokenizer().makeErrorToken(exception.getOffset());
		
		reportError(message, token);
	}
	
	private void reportParseError(BadTokenException exception) {
		IToken token = parser.getTokenizer().makeErrorToken(exception.getOffset());
		String message = exception.isEOFToken()
        	? exception.getShortMessage()
        	: "'" + token.toString() + "' not expected here";

        reportError(message, token);
	}
	
	private void reportParseError(Exception exception) {
		String message = "Internal parsing error: " + exception;
		exception.printStackTrace();
		
		IToken token = parser.getTokenizer().makeErrorToken(0);
		
		reportError(message, token);
	}
}
