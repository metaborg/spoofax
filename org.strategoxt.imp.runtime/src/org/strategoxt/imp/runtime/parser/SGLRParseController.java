package org.strategoxt.imp.runtime.parser;

import java.util.ArrayList;
import java.util.List;

import lpg.runtime.IToken;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.parser.ParseError;
import org.eclipse.imp.parser.SimpleLPGParseController;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.spoofax.jsglr.BadTokenException;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLR;
import org.spoofax.jsglr.TokenExpectedException;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenKindManager;


/**
 * IMP parse controller for an SGLR parser, reusing some logic from the LPG
 * implementation.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 * @author Karl Trygve Kalleberg <karltk add strategoxt.org>
 */
public abstract class SGLRParseController extends SimpleLPGParseController {
	private final List<String> problemMarkerTypes = new ArrayList<String>();
	
	private final List<ParseError> parseErrors = new ArrayList<ParseError>();
	
	private final SGLRTokenKindManager tokenManager = new SGLRTokenKindManager();
	
	private final String startSymbol;
	
	private SGLRParser parser;
	
	private AstNode currentAst;
	
	private ISourceProject project;
	
	private IPath path;
	
	@SuppressWarnings("unused")
	private org.eclipse.imp.parser.IMessageHandler messages;

	// Simple accessors
	
	@Override
	public final AstNode getCurrentAst() { 
		return currentAst;
	}
	
	@Override
	public final SGLRParser getParser() {
		if (parser == null) throw new IllegalStateException("Parser not determined yet");
		
		return parser;
	}

	@Override
	public final ISourceProject getProject() {
		return project;
	}
	
	public final List<ParseError> getErrors() {
		return parseErrors;
	}

	public final boolean hasErrors() {
		return getErrors().size() != 0;
	}

	/**
	 * @return either a project-relative path, if getProject() is non-null, or
	 *         an absolute path.
	 */
    @Override
	public final IPath getPath() {
    	return project == null
			? path
			: project.getRawProject().getLocation().append(path);
    }
	
	// Parsing and initialization
    
    static {
    	// HACK: set always repainting in IMP using static field
    	UniversalEditor.fAlwaysRepaint = true;
    	
    	SGLR.setWorkAroundMultipleLookahead(true);
    }
    
    /**
     * Create a new SGLRParseController.
     * 
     * @param startSymbol	The start symbol of this grammar, or null.
     */
    public SGLRParseController(String startSymbol) {
    	this.startSymbol = startSymbol;
    }

    @Override
	public void initialize(IPath filePath, ISourceProject project,
    		IMessageHandler handler) {
		this.path = filePath;
		this.project = project;
		this.messages = handler;
		
		ParseTable table = selectParseTable(path);
		parser = new SGLRParser(this, tokenManager, table, startSymbol);
    }

	public AstNode parse(String input, boolean scanOnly, IProgressMonitor monitor) {
		try {
			// TODO2: Optimization - don't produce AST if scanOnly is true
			parseErrors.clear();
			currentAst = parser.parse(input.toCharArray(), getPath().toPortableString());
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
	
	// Token kind management
	
	@Override
	public final boolean isKeyword(int kind) {
		return tokenManager.isKeyword(kind);
	}
	
//	public final String getTokenKindName(int kind) {
//		return tokenManager.getName(kind);
//	}
	
	// Grammar information

//	public String getSingleLineCommentPrefix() {
//		// This is a supposedly short-term solution for getting
//		// a language's single-line comment prefix
//		return "";
//	}

	/**
	 * Returns the name of the currently active grammar definition.
	 */ 
	public abstract String getActiveGrammarName();
	
	public abstract ParseTable selectParseTable(IPath path);
	
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
	
	// FIXME: Parse errors are no longer supported in the newer versions of IMP
	
	private void reportParseError(TokenExpectedException exception) {
		String message = exception.getShortMessage();
		IToken token = parser.getTokenizer().makeErrorToken(exception.getOffset());
		
		parseErrors.add(new ParseError(message, token));
	}
	
	// Error reporting
	
	private void reportParseError(BadTokenException exception) {
		IToken token = parser.getTokenizer().makeErrorToken(exception.getOffset());
		String message = exception.isEOFToken()
        	? exception.getShortMessage()
        	: "'" + token.toString() + "' not expected here";

		parseErrors.add(new ParseError(message, token));
	}
	
	private void reportParseError(Exception exception) {
		String message = "Internal parsing error: " + exception;
		exception.printStackTrace();
		
		IToken token = parser.getTokenizer().makeErrorToken(0);
		
		parseErrors.add(new ParseError(message, token));
	}
	
	// LPG compatibility
	
	/**
	 * @deprecated	Use {@link SGLRParseController#isKeyword(int)} instead.
	 */
	@Deprecated
	public char[][] getKeywords() {
		return new char[0][0];
	}

	@Deprecated
	@Override
	public SGLRLexer getLexer() {
		return new SGLRLexer(getParser().getTokenizer().getLexStream());
	}


	public int getTokenIndexAtCharacter(int offset) {
		int index = getParser().getParseStream().getTokenIndexAtCharacter(offset);
    	return Math.abs(index);
	}

	public IToken getTokenAtCharacter(int offset) {
		return getParser().getParseStream().getTokenAtCharacter(offset);
	}
	
	@Override
	public ISourcePositionLocator getNodeLocator() {
		return new SGLRAstLocator();
	}
	
	@Override
	public ILanguageSyntaxProperties getSyntaxProperties() {
		return null;
	}
	
}
