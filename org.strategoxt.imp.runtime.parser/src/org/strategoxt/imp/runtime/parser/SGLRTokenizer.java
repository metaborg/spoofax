package org.strategoxt.imp.runtime.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.strategoxt.imp.runtime.Debug;
import static org.strategoxt.imp.runtime.parser.ast.SGLRParsersym.*;

import lpg.runtime.IToken;
import lpg.runtime.LexStream;
import lpg.runtime.PrsStream;

/**
 * Wrapper class to add tokens to an LPG PrsStream.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class SGLRTokenizer {
	private final LexStream lexStream = new LexStream();
	private final PrsStream parseStream = new PrsStream(lexStream);
	
	/** Start of the last token */
	private int beginOffset;
	
	public IToken currentToken() {
		if (parseStream.getSize() == 0) return null;
		
		return parseStream.getTokenAt(parseStream.getSize() - 1);
	}
	
	public final PrsStream getParseStream() {
		return parseStream;
	}
	
	public final LexStream getLexStream() {
		return lexStream;
	}
	
	public final InputStream toByteStream() {
		// TODO: SGLR only does ASCII, but this conversion could be better
		
		char[] chars = lexStream.getInputChars();
		byte[] bytes = new byte[chars.length];
		
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte) chars[i];
		
		return new ByteArrayInputStream(bytes);
	}
	
	public void init(char[] input, String filename) throws IOException {
		lexStream.initialize(input, filename);
		parseStream.resetTokenStream();
		beginOffset = 0;
		
		// Token list must start with a bad token
		parseStream.makeToken(0, -1, 0);
	}
	
	public void endStream() {
		parseStream.makeToken(beginOffset, beginOffset, TK_EOF);
	}
	
	public IToken makeToken(int endOffset, int kind) {
		// TODO: Confirm empty tokens are unsupported
		if (beginOffset == endOffset)
			return null;
		
		parseStream.makeToken(beginOffset, endOffset - 1, kind);
		
		beginOffset = endOffset;

		Debug.log(
				"Token: ",
				SGLRParseController.getDefaultTokenKindName(kind),
				" = \"",
				currentToken(),
				"\"");
		
		// Increment the stream size after adding this token(!)
		parseStream.setStreamLength(parseStream.getSize());
		
		return currentToken();
	}
}
