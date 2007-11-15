package org.strategoxt.imp.runtime.parser;

import org.eclipse.imp.parser.ILexer;

import lpg.runtime.IPrsStream;
import lpg.runtime.LexStream;
import lpg.runtime.Monitor;

/**
 * SGLR ILexer implementation for compatibility with IMP.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 * */ 
public class SGLRLexer implements ILexer {
	private final LexStream lexStream;

	public LexStream getLexStream() {
		return lexStream;
	}
	
	public SGLRLexer(LexStream lexStream) {
		this.lexStream = lexStream;
	}
	
	public void initialize(char[] contents, String filename) {
		// We don't do that.
	}
	
	public int[] getKeywordKinds() {
		return new int[0];
	}

	public void lexer(Monitor monitor, IPrsStream prsStream) {
		// We don't do that.
	}
}
