package org.strategoxt.imp.runtime.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lpg.runtime.IAst;
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
import org.spoofax.jsglr.ParseTableManager;
import org.spoofax.jsglr.SGLRException;
import org.strategoxt.imp.runtime.Debug;

import aterm.ATerm;

/**
 * Base class of an IMP parse controller for an SGLR parser.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public abstract class SGLRParseController implements IParseController {	
	private final List<String> problemMarkerTypes = new ArrayList<String>();
	
	private final SGLRParser parser;
	
	private final static ParseTableManager parseTables
		= new ParseTableManager(SGLRParser.getFactory());
	
	private IAst currentAst;
	
	private ISourceProject project;
	
	private IPath path;
	
	private IMessageHandler messages;

	// Simple accessors
	
	public IAst getCurrentAst() { 
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

    public SGLRParseController(ParseTable parseTable, String startSymbol) {
    	this.parser = new SGLRParser(parseTable);
    }
    
    public SGLRParseController(ParseTable parseTable) {
    	this(parseTable, null);
    }
    
    public SGLRParseController(String parseTable, String startSymbol) {
		this(loadParseTable(parseTable), startSymbol);
	}
    
    public SGLRParseController(String parseTable) {
    	this(parseTable, null);
    }

	public void initialize(IPath path, ISourceProject project, IMessageHandler messages) {
		this.path = path;
		this.project = project;
		this.messages = messages;
	}

	public IAst parse(String input, boolean scanOnly, IProgressMonitor monitor) {
		IPath completePath = project == null ? path : project.resolvePath(path);
	
		try {
			ATerm parsed = parser.parse(completePath);
			currentAst = getAst(parsed);
			
		} catch (SGLRException x) {
			// FIXME: Report SGLR parsing errors
		} catch (IOException x) {
			throw new RuntimeException(x); // TODO: Better handling of 
		}
		
		return currentAst;
	}
    
    private static ParseTable loadParseTable(String parseTable) {
    	try {
    		Debug.startTimer("Loading parse table ", parseTable);
    		
	    	return parseTables.loadFromFile(parseTable);
			
			// TODO: Proper Exception handling for bad parse table
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InvalidParseTableException e) {
			throw new RuntimeException(e);
		} finally {
			Debug.stopTimer("Parse table loaded");
		}
    }
	
	protected abstract IAst getAst(ATerm term);

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
	public boolean isKeyword(int kind) {
		return false;
	}

	@Deprecated
	public SGLRLexer getLexer() {
		return new SGLRLexer();
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
