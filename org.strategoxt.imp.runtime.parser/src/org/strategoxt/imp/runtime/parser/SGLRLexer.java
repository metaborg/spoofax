package org.strategoxt.imp.runtime.parser;

import org.eclipse.imp.parser.ILexer;

import lpg.runtime.IPrsStream;
import lpg.runtime.LexStream;
import lpg.runtime.Monitor;

/**
 * Fake SGLR ILexer implementation for compatibility with IMP.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 * */ 
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

	@Deprecated
	/**
	 * Return an empty lex stream.
	 * 
	 * @deprecated  SGLR does not employ a lexer or lex stream.
	 */
	public LexStream getLexStream() {
		// Used by OutlinerBase.significantChange()		
		return new LexStream();
	}
}
