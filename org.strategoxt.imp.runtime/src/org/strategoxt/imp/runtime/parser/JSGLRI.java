package org.strategoxt.imp.runtime.parser;

import java.io.IOException;
import java.io.InputStream;

import org.spoofax.jsglr.BadTokenException;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLR;
import org.spoofax.jsglr.SGLRException;
import org.spoofax.jsglr.TokenExpectedException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.tokens.TokenKindManager;

import aterm.ATerm;

/**
 * IMP IParser implementation using JSGLR, imploding parse trees to AST nodes and tokens.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */ 
public class JSGLRI extends AbstractSGLRI {
	private final SGLR parser;
	
	// Initialization and parsing
	
	public JSGLRI(ParseTable parseTable, String startSymbol,
			SGLRParseController controller, TokenKindManager tokenManager) {
		super(controller, tokenManager, startSymbol);
		
		parser = Environment.createSGLR(parseTable);
		parser.setCycleDetect(false);
		parser.setFilter(false); // FIXME: Filters not supported ATM
	}
	
	public JSGLRI(ParseTable parseTable, String startSymbol) {
		this(parseTable, startSymbol, null, new TokenKindManager());
	}
	
	@Override
	public ATerm parseNoImplode(char[] inputChars, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		return parseNoImplode(toByteStream(inputChars), inputChars);
	}
	
	public ATerm parseNoImplode(InputStream inputStream, char[] inputChars)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		// Read stream using tokenizer/lexstream
		
		// TODO: Once spoofax supports it, use the start symbol
		ATerm asfix = parser.parse(inputStream, null /*getStartSymbol()*/); 
		
		return asfix;
	}
}
