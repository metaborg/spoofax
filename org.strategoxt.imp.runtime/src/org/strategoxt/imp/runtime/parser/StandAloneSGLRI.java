package org.strategoxt.imp.runtime.parser;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.imp.language.Language;
import org.spoofax.jsglr.BadTokenException;
import org.spoofax.jsglr.Disambiguator;
import org.spoofax.jsglr.InvalidParseTableException;
import org.spoofax.jsglr.NoRecoveryRulesException;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.RecoverAlgorithm;
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
	
	public void setRecoverHandler(RecoverAlgorithm recoverHandler) throws NoRecoveryRulesException {
		if (parser instanceof JSGLRI) {
			((JSGLRI) parser).setRecoverHandler(recoverHandler);
		} else {
			throw new UnsupportedOperationException("C-SGLR does not support error recovery");
		}
	}
	
	// Simple accessors
	
	public String getStartSymbol() {
		return parser.getStartSymbol();
	}
	
	public Disambiguator getDisambiguator() {
        if (parser instanceof JSGLRI) {
            return ((JSGLRI) parser).getDisambiguator();
        } else {
            throw new UnsupportedOperationException("C-SGLR does not provide a disambiguator configuration object");
        }
	}
	
	public void setStartSymbol(String startSymbol) {
		parser.setStartSymbol(startSymbol);
	}
	
	/**
	 * Sets whether to keep any unresolved ambiguities. Default true.
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
		
		synchronized (Environment.getSyncRoot()) {
			return parser.parse(input, filename);
		}
	}
	
	private static class StandAloneLanguage extends Language {
		public StandAloneLanguage(String name) {
			super(name, null, null, null, null, null, null, "", null, null);
		}
	}
}
