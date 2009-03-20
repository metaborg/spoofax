package org.strategoxt.imp.runtime.parser;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.imp.language.Language;
import org.spoofax.jsglr.BadTokenException;
import org.spoofax.jsglr.IRecoverAlgorithm;
import org.spoofax.jsglr.InvalidParseTableException;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLRException;
import org.spoofax.jsglr.TokenExpectedException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;

/**
 * A stand-alone SGLR parsing class that uses the Spoofax/IMP imploder and AST classes.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StandAloneSGLRI {
	
	private final AbstractSGLRI parser;
	
	public StandAloneSGLRI(String language, InputStream parseTable, String startSymbol)
			throws IOException, InvalidParseTableException {
		this(language, parseTable, startSymbol, false);
	}
	
	public StandAloneSGLRI(String language, InputStream parseTable, String startSymbol, boolean useCSGLR)
			throws IOException, InvalidParseTableException {
		
		if (useCSGLR) {
			parser = new CSGLRI(parseTable, startSymbol);
		} else {
			Language lang = new StandAloneLanguage(language);
			ParseTable table = Environment.registerParseTable(lang, parseTable);
			parser = new JSGLRI(table, startSymbol);			
		}
	}
	
	@Deprecated
	public void withBacktracking(boolean withBT) {
		if (parser instanceof JSGLRI) {
			((JSGLRI) parser).withBacktracking(withBT);
		}
	}
	
	public void setRecoverHandler(IRecoverAlgorithm recoverHandler) {
		if (parser instanceof JSGLRI) {
			((JSGLRI) parser).setRecoverHandler(recoverHandler);
		}
	}
	
	// Simple accessors
	
	public String getStartSymbol() {
		return parser.getStartSymbol();
	}
	
	public void setStartSymbol(String startSymbol) {
		parser.setStartSymbol(startSymbol);
	}
	
	/**
	 * Sets whether to keep any unresolved ambiguities. Default false.
	 */
	public void setKeepAmbiguities(boolean value) {
		parser.setKeepAmbiguities(value);
	}
	
	// Parsing
	
	public IStrategoAstNode parse(InputStream input, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		return parser.parse(input, filename);
	}
	
	public IStrategoAstNode parse(char[] input, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		return parser.parse(input, filename);
	}
	
	private static class StandAloneLanguage extends Language {
		public StandAloneLanguage(String name) {
			super(name, null, null, null, null, null, null, null);
		}
	}
}
