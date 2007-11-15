package org.strategoxt.imp.runtime.parser;

import java.io.IOException;

import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.parser.ast.SGLRParsersym;

import lpg.runtime.IToken;
import lpg.runtime.LexStream;
import lpg.runtime.PrsStream;

public class SGLRTokenizer {
	private final LexStream lexStream = new LexStream();
	private final PrsStream parseStream = new PrsStream(lexStream);
	
	/** Start of the last token */
	private int beginOffset;
	
	public IToken currentToken() {
		if (parseStream.getSize() == 0) return null;
		
		return parseStream.getTokenAt(parseStream.getSize() - 1);
	}
	
	public PrsStream getParseStream() {
		return parseStream;
	}
	
	public LexStream getLexStream() {
		return lexStream;
	}
	
	// TODO: Don't initialize SGLRTokenizer using filename
	public void init(String filename) throws IOException {
		lexStream.initialize(filename);
		parseStream.resetTokenStream();
		beginOffset = 0;
	}
	
	public IToken makeToken(int endOffset, int kind) {
		// if (beginOffset == endOffset)
		//	return null;
		
		parseStream.makeToken(beginOffset, endOffset - 1, kind);
		
		beginOffset = endOffset;

		Debug.log(
				"Token: ",
				SGLRParseController.getDefaultTokenKindName(kind),
				" = \"",
				currentToken(),
				"\"");
		
		return currentToken();
	}
}
