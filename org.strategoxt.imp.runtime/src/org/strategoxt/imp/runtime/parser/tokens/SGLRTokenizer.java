package org.strategoxt.imp.runtime.parser.tokens;

import static org.strategoxt.imp.runtime.parser.tokens.TokenKind.*;
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
	private int startOffset;
	
	public SGLRTokenizer(char[] input, String filename) {
		lexStream.initialize(input, filename);
		parseStream.resetTokenStream();
		startOffset = 0;
		
		// Token list must start with a bad token
		makeToken(0, TK_RESERVED, true);
	}
	
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
	
	public int getStartOffset() {
		return startOffset;
	}
	
	public void setStartOffset(int beginOffset) {
		this.startOffset = beginOffset;
	}
	
	public void endStream() {
		makeToken(startOffset + 1, TK_EOF, true);
	}
	
	public SGLRToken makeToken(int endOffset, TokenKind kind, boolean allowEmptyToken) {
		if (!allowEmptyToken && startOffset == endOffset) // empty token
			return null;
		
		//if (Debug.ENABLED) {
			assert endOffset >= startOffset || (kind == TK_RESERVED && startOffset == 0);
		//	if (parseStream.getTokens().size() > 0) {
		//		IToken lastToken = (IToken) parseStream.getTokens().get(parseStream.getTokens().size() - 1); 
		//		assert lastToken.getKind() == TK_RESERVED.ordinal()
		//			|| (lastToken.getStartOffset() + lastToken.getEndOffset()) == startOffset;
		//	}
		//}
		
		SGLRToken token = new SGLRToken(parseStream, startOffset, endOffset - 1, kind.ordinal());
		token.setTokenIndex(parseStream.getSize());
		
		// Add token and increment the stream size(!)
		parseStream.addToken(token);
		parseStream.setStreamLength(parseStream.getSize());
		
		startOffset = endOffset;
		
		return token;
	}
	
	// Bridge method
	public final SGLRToken makeToken(int endOffset, TokenKind kind) {
		return makeToken(endOffset, kind, false);
	}
	
	/**
	 * Creates an error token from existing tokens.
	 */
	public IToken makeErrorToken(IToken left, IToken right) {
		return new Token(parseStream, left.getStartOffset(), right.getEndOffset(), TK_ERROR.ordinal());
	}
	
	/**
	 * Creates an error token up to the next whitespace character.
	 */
	public IToken makeErrorToken(int offset) {		
		if (offset == lexStream.getStreamLength())
		    return makeErrorTokenBackwards(offset - 1);
		if (offset > lexStream.getStreamLength())
			return makeErrorTokenBackwards(lexStream.getStreamLength() - 1);

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
		
		return new Token(parseStream, offset, endOffset, TK_ERROR.ordinal());
	}
	
	/**
	 * Creates an error token on stream part
	 */
	public IToken makeErrorToken(int beginOffset, int endOffset) {		
		return new Token(parseStream, beginOffset, endOffset, TK_ERROR.ordinal());
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
		
		return new Token(parseStream, beginOffset, offset, TK_ERROR.ordinal());
	}
	
	public static String dumpToString(IToken left, IToken right) {
		StringBuilder result = new StringBuilder();
		int last = right.getTokenIndex();
		
		for (int i = left.getTokenIndex(); i <= last; i++) {
			IToken token = left.getIPrsStream().getTokenAt(i);
			result.append(valueOf(token.getKind()));
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
