package org.strategoxt.imp.runtime.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import lpg.runtime.IPrsStream;

import org.spoofax.jsglr.BadTokenException;
import org.spoofax.jsglr.Disambiguator;
import org.spoofax.jsglr.FilterException;
import org.spoofax.jsglr.NoRecoveryRulesException;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLR;
import org.spoofax.jsglr.SGLRException;
import org.spoofax.jsglr.TokenExpectedException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.ParseTableProvider;
import org.strategoxt.imp.runtime.parser.tokens.TokenKindManager;

import aterm.ATerm;

/**
 * IMP IParser implementation using JSGLR, imploding parse trees to AST nodes and tokens.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */ 
public class JSGLRI extends AbstractSGLRI {
	
	private ParseTableProvider parseTable;
	
	private boolean useRecovery = false;
	
	private SGLR parser;
	
	private Disambiguator disambiguator;
	
	private int timeout;
	
	// Initialization and parsing
	
	public JSGLRI(ParseTableProvider parseTable, String startSymbol,
			SGLRParseController controller, TokenKindManager tokenManager) {
		super(controller, tokenManager, startSymbol, parseTable);
		
		this.parseTable = parseTable;
		resetState();
	}
	
	public JSGLRI(ParseTable parseTable, String startSymbol,
			SGLRParseController controller, TokenKindManager tokenManager) {
		this(new ParseTableProvider(parseTable), startSymbol, controller, tokenManager);
	}
	
	public JSGLRI(ParseTableProvider parseTable, String startSymbol) {
		this(parseTable, startSymbol, null, new TokenKindManager());
	}
	
	public JSGLRI(ParseTable parseTable, String startSymbol) {
		this(new ParseTableProvider(parseTable), startSymbol);
	}
	
	protected SGLR getParser() {
		return parser;
	}
	
	/**
	 * @see SGLR#setUseStructureRecovery(boolean)
	 */
	public void setUseRecovery(boolean useRecovery) throws NoRecoveryRulesException {
		this.useRecovery = useRecovery;
		parser.setUseStructureRecovery(useRecovery);
	}
	
	public ParseTable getParseTable() {
		try {
			return parseTable.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Disambiguator getDisambiguator() {
		return disambiguator;
	}
	
	public IPrsStream getIPrsStream() {
		return super.getController().getIPrsStream();
	}
	
	public void setParseTable(ParseTable parseTable) {
		this.parseTable = new ParseTableProvider(parseTable);
		resetState();
	}
	
	public void setParseTable(ParseTableProvider parseTable) {
		this.parseTable = parseTable;
		resetState();
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
		resetState();
	}
	
	@Override
	protected ATerm doParseNoImplode(char[] inputChars, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		return doParseNoImplode(toByteStream(inputChars), inputChars, filename);
	}
	
	/**
	 * Resets the state of this parser, reinitializing the SGLR instance
	 */
	void resetState() {
		parser = Environment.createSGLR(getParseTable());
		parser.setTimeout(timeout);
		if (disambiguator != null) parser.setDisambiguator(disambiguator);
		else disambiguator = parser.getDisambiguator();
		try {
			setUseRecovery(useRecovery);
		} catch (NoRecoveryRulesException e) {
			// Already handled/logged this error in setRecoverHandler()
		}
	}
	
	private ATerm doParseNoImplode(InputStream inputStream, char[] inputChars, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		// Read stream using tokenizer/lexstream
		
		// FIXME: Some bug in JSGLR is causing its state to get corrupted; must reset it every parse
		// (must do this beforehand to keep getCollectedErrors() intact afterwards)
		if (parseTable.isDynamic()) {
			parseTable.initialize(new File(filename));
		}
		resetState();
		try {
			return parser.parse(inputStream, getStartSymbol());
		} catch (FilterException e) {
			if (e.getCause() == null && parser.getDisambiguator().getFilterPriorities()) {
				Environment.logException("Parse filter failure - disabling priority filters and trying again", e);
				getDisambiguator().setFilterPriorities(false);
				try {
					return parser.parse(inputStream, getStartSymbol());
				} finally {
					getDisambiguator().setFilterPriorities(true);
				}
			} else {
				throw new FilterException(e.getParser(), e.getMessage(), e);
			}
		}
	}
}
