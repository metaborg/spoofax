package org.strategoxt.imp.runtime.parser;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.imp.language.Language;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.jsglr.client.Disambiguator;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.shared.BadTokenException;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.jsglr.shared.TokenExpectedException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.ParseTableProvider;

/**
 * A stand-alone SGLR parsing class that uses the Spoofax/IMP term
 * classes and attachments.
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
			// TODO: don't register tables for StandAloneSGLRI?
			Language lang = new StandAloneLanguage(language);
			ParseTable table = Environment.loadParseTable(parseTable);
			Environment.registerParseTable(lang, new ParseTableProvider(table));
			parser = new JSGLRI(table, startSymbol);
		}
	}
	
	public void setUseRecovery(boolean useRecovery) {
		if (parser instanceof JSGLRI) {
			((JSGLRI) parser).setUseRecovery(useRecovery);
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
	
	@Deprecated
	public void setKeepAmbiguities(boolean value) {
		parser.setKeepAmbiguities(value);
	}
	
	// Parsing
	
	public ISimpleTerm parse(InputStream input, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		return parser.parse(input, filename);
	}
	
	public ISimpleTerm parse(String input, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		return parser.parse(input, filename);
	}
	
	private static class StandAloneLanguage extends Language {
		public StandAloneLanguage(String name) {
			super(name, null, null, null, null, null, null, "", null, null);
		}
	}
}
