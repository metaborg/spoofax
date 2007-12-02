package org.strategoxt.imp.runtime.parser.tokens;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.strategoxt.imp.runtime.parser.tokens.SGLRParsersym.*;

import lpg.runtime.IToken;
import lpg.runtime.LexStream;
import lpg.runtime.PrsStream;
import lpg.runtime.Token;

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
	
	public IToken makeToken(int endOffset, int kind, boolean allowEmptyToken) {
		if (!allowEmptyToken && beginOffset == endOffset) // empty token
			return null;
		
		parseStream.makeToken(beginOffset, endOffset - 1, kind);
		
		beginOffset = endOffset;
		
		// Increment the stream size after adding this token(!)
		parseStream.setStreamLength(parseStream.getSize());
		
		return currentToken();
	}
	
	public final IToken makeToken(int endOffset, int kind) {
		return makeToken(endOffset, kind, false);
	}
	
	/**
	 * Creates an error token up to the next whitespace character.
	 */
	public IToken makeErrorToken(int offset) {
		int endOffset = offset;
		
		if (offset == lexStream.getStreamLength())
		    return makeErrorTokenBackwards(offset - 1);
		
		while (endOffset + 1 < lexStream.getStreamLength()) {
			if (Character.isWhitespace(lexStream.getCharValue(endOffset + 1))) break;
			endOffset++;
		}
		
		return new Token(parseStream, offset, endOffset, TK_JUNK);
	}
	
	/**
	 * Creates an error token from existing tokens.
	 */
	public IToken makeErrorToken(IToken left, IToken right) {
		return new Token(parseStream, left.getStartOffset(), right.getEndOffset(), TK_JUNK);
	}
	
	/**
	 * Creates an error token from the last whitespace character.
	 */
	public IToken makeErrorTokenBackwards(int offset) {
		int beginOffset = offset;
		
		while (beginOffset > 0) {
			if (Character.isWhitespace(lexStream.getCharValue(beginOffset - 1))) break;
			beginOffset--;
		}
		
		return new Token(parseStream, beginOffset, offset, TK_JUNK);
	}
	
	public static String dumpToString(IToken left, IToken right) {
		StringBuilder result = new StringBuilder();
		int last = right.getTokenIndex();
		
		for (int i = left.getTokenIndex(); i <= last; i++) {
			IToken token = left.getPrsStream().getTokenAt(i);
			result.append(SGLRTokenKindManager.getDefaultName(token.getKind()));
			result.append(":");
			result.append(token.toString().replace("\n","\\n").replace("\r","\\r"));
			if (i < last) result.append(", ");
		}
		
		return result.toString();
	}
	
	public static final String dumpToString(IToken token) {
		return dumpToString(token, token);
	}
}
