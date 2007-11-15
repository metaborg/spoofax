package org.strategoxt.imp.runtime.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lpg.runtime.IMessageHandler;
import lpg.runtime.IToken;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.AstLocator;
import org.eclipse.imp.parser.IASTNodeLocator;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ParseError;
import org.spoofax.jsglr.InvalidParseTableException;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLRException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.SGLRAstNode;
import org.strategoxt.imp.runtime.parser.ast.SGLRAstNodeFactory;
import org.strategoxt.imp.runtime.parser.ast.SGLRParsersym;

/**
 * Base class of an IMP parse controller for an SGLR parser.
 * 
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public abstract class SGLRParseController implements IParseController {	
	private final List<String> problemMarkerTypes = new ArrayList<String>();
	
	private final SGLRParser parser;
	
	private final SGLRLexer lexer;
	
	private SGLRAstNode currentAst;
	
	private ISourceProject project;
	
	private IPath path;
	
	@SuppressWarnings("unused")
	private IMessageHandler messages;

	// Simple accessors
	
	public SGLRAstNode getCurrentAst() { 
		return currentAst;
	}
	
	public SGLRParser getParser() {
		return parser;
	}

	public ISourceProject getProject() {
		return project;
	}

	/**
	 * @return either a project-relative path, if getProject() is non-null, or
	 *         an absolute path.
	 */
    public IPath getPath() {
		return path;
	}
	
	// Parsing and initialization
    
    public SGLRParseController(SGLRAstNodeFactory factory, ParseTable parseTable, String startSymbol) {
    	parser = new SGLRParser(factory, parseTable, startSymbol);
    	lexer = new SGLRLexer(parser.getTokenizer().getLexStream());
    }
    
    public SGLRParseController(SGLRAstNodeFactory factory, ParseTable parseTable) {
    	this(factory, parseTable, null);
    }
    
    /**
     * Constructs a new iSGLRParseController instance.
     * Reads the parse table from a stream and throws runtime exceptions
     * if anything goes wrong. 
     */
    protected SGLRParseController(SGLRAstNodeFactory factory, InputStream parseTable, String startSymbol)
    		throws IOException, InvalidParseTableException {
    	
    	this(factory, Environment.loadParseTable(parseTable), startSymbol);
	}
    
    protected SGLRParseController(SGLRAstNodeFactory factory, InputStream parseTable)
			throws IOException, InvalidParseTableException {
    	
    	this(factory, parseTable, null);
    }

	public void initialize(IPath path, ISourceProject project, IMessageHandler messages) {
		this.path = path;
		this.project = project;
		this.messages = messages;
	}

	public SGLRAstNode parse(String input, boolean scanOnly, IProgressMonitor monitor) {
		// Make some assumptions to get the input file path
		IPath completePath = project == null
				? path
				: project.getRawProject().getLocation().append(path);
	
		try {
			currentAst = parser.parse(completePath);
		} catch (SGLRException x) {
			// FIXME: Report SGLR parsing errors
			throw new RuntimeException(x);
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
		
		return currentAst;
	}
	
	// Generic parse table information (should be overridden in subclasses)
	
	public String getTokenKindName(int kind) {
		return getDefaultTokenKindName(kind);
	}
	
	static String getDefaultTokenKindName(int kind) {
		switch (kind) {
			case SGLRParsersym.TK_IDENTIFIER:
				return "TK_IDENTIFIER";
			case SGLRParsersym.TK_KEYWORD:
				return "TK_KEYWORD";
			case SGLRParsersym.TK_LAYOUT:
				return "TK_LAYOUT";
			case SGLRParsersym.TK_EOF:
				return "TK_EOF";
			default:
				return "TK_UNKNOWN";
		}
	}
	
	public boolean isKeyword(int kind) {
		return kind == SGLRParsersym.TK_KEYWORD;
	}

	// Problem markers

	public List<String> getProblemMarkerTypes() {
		return problemMarkerTypes;
	}
	
	public void addProblemMarkerType(String problemMarkerType) {
		problemMarkerTypes.add(problemMarkerType);
	}
	
	public void removeProblemMarkerType(String problemMarkerType) {
		problemMarkerTypes.remove(problemMarkerType);
	}
	
	// Errors
	
	public List<ParseError> getErrors() {
		return Collections.emptyList(); // TODO: Return SGLR errors?
	}

	public boolean hasErrors() {
		return getErrors().size() != 0;
	}
	
	// LPG Compatibility
	
	@Deprecated
	public char[][] getKeywords() {
		return new char[0][0];
	}

	@Deprecated
	public SGLRLexer getLexer() {
		return lexer;
	}

	@Deprecated
	public String getSingleLineCommentPrefix() {
		// This is a supposedly short-term solution for getting
		// a language's single-line comment prefix
		return "";
	}

	public IASTNodeLocator getNodeLocator() {
		// Use the default AST Locator defined in IMP for IAst trees 
		return new AstLocator();
	}

	public int getTokenIndexAtCharacter(int offset) {
		int index= getParser().getParseStream().getTokenIndexAtCharacter(offset);
    	return Math.abs(index);
	}

	public IToken getTokenAtCharacter(int offset) {
		return getParser().getParseStream().getTokenAtCharacter(offset);
	}
}
