package org.strategoxt.imp.runtime.parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import lpg.runtime.Monitor;
import lpg.runtime.PrsStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.parser.IParser;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLR;
import org.spoofax.jsglr.SGLRException;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.ATermAstNode;
import org.strategoxt.imp.runtime.parser.ast.ATermAstNodeFactory;
import org.strategoxt.imp.runtime.parser.ast.AsfixConverter;

import aterm.ATerm;

/**
 * IParser implementation for SGLR.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */ 
public class SGLRParser implements IParser {	
	private static final int EOFT_SYMBOL = -1;
	
	private final SGLR parser;
	
	private final String startSymbol;
	
	private PrsStream parseStream;
	
	// Simple accessors

	public int getEOFTokenKind() {
		return EOFT_SYMBOL;
	}

	public PrsStream getParseStream() {
		if (parseStream == null) throw new IllegalStateException();
		
		return parseStream;
	}
	
	// Initialization and parsing
	
	public SGLRParser(ATermAstNodeFactory factory, ParseTable parseTable, String startSymbol) {	
		parser = Environment.createSGLR(parseTable);
		this.startSymbol = startSymbol;
	}
	
	/**
	 * Parse an input, returning the AST and initializing the parse stream.
	 * 
	 * @return  A parse tree.
	 * @see     AsfixConverter
	 */
	public ATermAstNode parse(IPath input) throws SGLRException, IOException {
		InputStream stream = new FileInputStream(input.toOSString());
		ATerm asfix;
		
		try {
			Debug.startTimer();
			
			asfix = parser.parse(stream, startSymbol);
		} finally {
			Debug.stopTimer("File parsed");
			stream.close();
		}
		
		parseStream = new PrsStream();
		ATermAstNode result = Environment.getConverter().implode(asfix, parseStream);
		
		return result;
	}
	
	// LPG legacy / compatibility

	@Deprecated
	public SGLR parser(Monitor monitor, int error_repair_count) {
		throw new UnsupportedOperationException();
	}
}
