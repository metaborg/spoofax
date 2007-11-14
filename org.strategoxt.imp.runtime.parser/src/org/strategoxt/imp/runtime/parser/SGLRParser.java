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
	
	private final PrsStream parseStream = new PrsStream();
	
	// Simple accessors

	public int getEOFTokenKind() {
		return EOFT_SYMBOL;
	}

	public PrsStream getParseStream() {
		return parseStream;
	}
	
	public SGLRParser(ParseTable parseTable, String startSymbol) {	
		parser = Environment.createSGLR(parseTable);
		this.startSymbol = startSymbol;
	}
	
	/**
	 * Parse an input.
	 * 
	 * @return  A parse tree (asfix representation)
	 * @see     AsfixConverter
	 */
	public ATerm parse(IPath input) throws SGLRException, IOException {
		InputStream stream = new FileInputStream(input.toOSString());
		ATerm asfix;
		
		try {
			Debug.startTimer();
			
			asfix = parser.parse(stream, startSymbol);
		} finally {
			Debug.stopTimer("File parsed");
			stream.close();
		}
		
		return asfix;
	}
	
	// LPG compatibility

	@Deprecated
	public SGLR parser(Monitor monitor, int error_repair_count) {
		// TODO: Return SGLR Parser implementation? 
		throw new UnsupportedOperationException();
	}
}
