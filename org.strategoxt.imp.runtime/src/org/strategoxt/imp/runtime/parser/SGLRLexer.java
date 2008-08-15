package org.strategoxt.imp.runtime.parser;

import org.eclipse.imp.parser.ILexer;
import org.strategoxt.imp.runtime.parser.tokens.TokenKind;

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
	
	// IMP legacy / compatibility

	/**
	 * @deprecated	Use {@link SGLRParseController#isKeyword(int)} instead.
	 */
	@Deprecated
	public int[] getKeywordKinds() {
		return new int[] { TokenKind.TK_KEYWORD.ordinal() };
	}
	
	@Deprecated
	public void initialize(char[] contents, String filename) {
		// We don't do that.
	}

	@Deprecated
	public void lexer(Monitor monitor, IPrsStream prsStream) {
		// We don't do that.
	}
}
