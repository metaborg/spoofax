package org.strategoxt.imp.runtime.parser;

import org.eclipse.imp.parser.ILexer;

import lpg.runtime.IPrsStream;
import lpg.runtime.LexStream;
import lpg.runtime.Monitor;

/** Fake SGLR lexer for compatibility with IMP. */ 
public class SGLRLexer implements ILexer {
	public int[] getKeywordKinds() {
		return new int[0];
	}
	
	public void initialize(char[] contents, String filename) {
		// We don't do that.
	}

	public void lexer(Monitor monitor, IPrsStream prsStream) {
		// We don't do that either.
	}

	public LexStream getLexStream() {
		return null;
	}
}
