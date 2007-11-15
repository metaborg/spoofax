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
import org.strategoxt.imp.runtime.parser.ast.SGLRAstNode;
import org.strategoxt.imp.runtime.parser.ast.SGLRAstNodeFactory;
import org.strategoxt.imp.runtime.parser.ast.AsfixConverter;
import org.strategoxt.imp.runtime.parser.ast.SGLRParsersym;

import aterm.ATerm;

/**
 * IParser implementation for SGLR.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */ 
public class SGLRParser implements IParser {
	private final SGLR parser;
	
	private final String startSymbol;
	
	private final AsfixConverter converter;
	
	private final SGLRTokenizer tokenizer;
	
	// Simple accessors
	
	public SGLRTokenizer getTokenizer() {
		return tokenizer; 
	}

	public int getEOFTokenKind() {
		return SGLRParsersym.TK_EOF;
	}

	public PrsStream getParseStream() {
		return getTokenizer().getParseStream();
	}
	
	// Initialization and parsing
	
	public SGLRParser(SGLRAstNodeFactory factory, ParseTable parseTable, String startSymbol) {	
		this.startSymbol = startSymbol;

		tokenizer = new SGLRTokenizer();		
		converter = new AsfixConverter(factory, tokenizer);
		parser = Environment.createSGLR(parseTable);
	}
	
	/**
	 * Parse an input, returning the AST and initializing the parse stream.
	 * 
	 * @return  A parse tree.
	 * @see     AsfixConverter
	 */
	public SGLRAstNode parse(IPath input) throws SGLRException, IOException {
		String filename = input.toOSString();
		InputStream stream = new FileInputStream(filename);
		ATerm asfix;
		
		try {
			Debug.startTimer();
			
			tokenizer.init(filename);			
			asfix = parser.parse(stream, startSymbol);
		} finally {
			stream.close();
			
			Debug.stopTimer("File parsed");
		}
		
		SGLRAstNode result = converter.implode(asfix);
		
		return result;
	}
	
	// LPG legacy / compatibility

	@Deprecated
	public SGLR parser(Monitor monitor, int error_repair_count) {
		throw new UnsupportedOperationException();
	}
}
