package org.strategoxt.imp.runtime.parser;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.imp.language.Language;
import org.spoofax.jsglr.BadTokenException;
import org.spoofax.jsglr.InvalidParseTableException;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLRException;
import org.spoofax.jsglr.TokenExpectedException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.AstNode;

/**
 * A stand-alone SGLR parsing class that uses the Spoofax/IMP imploder and AST classes.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StandAloneSGLRParser {
	
	private final SGLRParser parser;
	
	public StandAloneSGLRParser(String language, InputStream parseTable, String startSymbol)
			throws IOException, InvalidParseTableException {
		
		Language lang = new StandAloneLanguage(language);
		ParseTable table = Environment.registerParseTable(lang, parseTable);
		parser = new SGLRParser(table, startSymbol);
	}
	
	public AstNode parse(InputStream input, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		return parser.parse(input, filename);
	}
	
	/**
	 * Sets whether to keep any unresolved ambiguities. Default false.
	 */
	public void setKeepAmbiguities(boolean value) {
		parser.setKeepAmbiguities(value);
	}
	
	private static class StandAloneLanguage extends Language {
		public StandAloneLanguage(String name) {
			super(name, null, null, null, null, null, null, null);
		}
	}
}
