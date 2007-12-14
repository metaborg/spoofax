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
		makeToken(0, 0, true);
	}
	
	public void endStream() {
		makeToken(beginOffset, TK_EOF, true);
	}
	
	public SGLRToken makeToken(int endOffset, int kind, boolean allowEmptyToken) {
		if (!allowEmptyToken && beginOffset == endOffset) // empty token
			return null;
		
		SGLRToken token = new SGLRToken(parseStream, beginOffset, endOffset - 1, parseStream.mapKind(kind));
		token.setTokenIndex(parseStream.getSize());
		
		// Add token and increment the stream size(!)
		parseStream.addToken(token);
		parseStream.setStreamLength(parseStream.getSize());
		
		beginOffset = endOffset;
		
		return token;
	}
	
	// Bridge method
	public final SGLRToken makeToken(int endOffset, int kind) {
		return makeToken(endOffset, kind, false);
	}
	
	/**
	 * Creates an error token from existing tokens.
	 */
	public IToken makeErrorToken(IToken left, IToken right) {
		return new Token(parseStream, left.getStartOffset(), right.getEndOffset(), TK_JUNK);
	}
	
	/**
	 * Creates an error token up to the next whitespace character.
	 */
	public IToken makeErrorToken(int offset) {		
		if (offset == lexStream.getStreamLength())
		    return makeErrorTokenBackwards(offset - 1);

		int endOffset = offset;
		boolean onlySeenWhitespace = Character.isWhitespace(lexStream.getCharValue(endOffset));
		
		while (endOffset + 1 < lexStream.getStreamLength()) {
			char c = lexStream.getCharValue(endOffset + 1);
			boolean isWhitespace = Character.isWhitespace(c);
			
			if (onlySeenWhitespace) {
				onlySeenWhitespace = isWhitespace;
			} else if (isWhitespace) {
				break;
			}
			
			endOffset++;
		}
		
		return new Token(parseStream, offset, endOffset, TK_JUNK);
	}
	
	/**
	 * Creates an error token from the last whitespace character.
	 */
	public IToken makeErrorTokenBackwards(int offset) {
		int beginOffset = offset;
		boolean onlySeenWhitespace = true;
		
		while (beginOffset > 0) {
			char c = lexStream.getCharValue(beginOffset - 1);
			boolean isWhitespace = Character.isWhitespace(c);
			
			if (onlySeenWhitespace) {
				onlySeenWhitespace = isWhitespace;
			} else if (isWhitespace) {
				break;
			}
			
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
