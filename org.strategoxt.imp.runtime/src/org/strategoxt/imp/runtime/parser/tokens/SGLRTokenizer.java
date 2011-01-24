package org.strategoxt.imp.runtime.parser.tokens;

/*
import static org.spoofax.jsglr.client.imploder.IToken.TK_EOF;
import static org.spoofax.jsglr.client.imploder.IToken.TK_ERROR;
import static org.spoofax.jsglr.client.imploder.IToken.TK_RESERVED;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.Token;
import org.spoofax.jsglr.client.imploder.Tokenizer;

/*
 * Wrapper class to add tokens to an LPG PrsStream.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 *
public class SGLRTokenizer extends Tokenizer {
	
	/*
	private final LexStream lexStream = new LexStream();
	
	private final ITokenizer parseStream = new PrsStream(lexStream);
	
	private IStrategoTerm cachedAst;
	
	/** Start of the last token *
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
	
	public ITokenizer getParseStream() {
		return parseStream;
	}
	
	public IStrategoTerm getCachedAst() {
		return cachedAst;
	}
	
	public void setCachedAst(IStrategoTerm cachedAst) {
		this.cachedAst = cachedAst;
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
	
	public SGLRToken makeToken(int endOffset, int kind, boolean allowEmptyToken) {
		if (!allowEmptyToken && startOffset >= endOffset) // empty token
			return null;
		
		//if (Debug.ENABLED) {
			assert endOffset >= startOffset || (kind == TK_RESERVED && startOffset == 0);
		//	if (parseStream.getTokenCount() > 0) {
		//		IToken lastToken = (IToken) parseStream.getTokens().get(parseStream.getTokenCount() - 1); 
		//		assert lastToken.getKind() == TK_RESERVED
		//			|| (lastToken.getStartOffset() + lastToken.getEndOffset()) == startOffset;
		//	}
		//}
		
		SGLRToken token = new SGLRToken(parseStream, startOffset, endOffset - 1, kind);
		token.setTokenIndex(parseStream.getSize());
		
		// Add token and increment the stream size(!)
		parseStream.addToken(token);
		parseStream.setStreamLength(parseStream.getSize());
		
		startOffset = endOffset;
		
		return token;
	}
	
	// Bridge method
	public final SGLRToken makeToken(int endOffset, int kind) {
		return makeToken(endOffset, kind, false);
	}
	
	/**
	 * Creates an error token from existing tokens.
	 *
	public IToken makeErrorToken(IToken left, IToken right) {
		return new Token(parseStream, left.getStartOffset(), right.getEndOffset(), TK_ERROR);
	}
	
	/**
	 * Creates an error token up to the next whitespace character.
	 *
	public IToken makeErrorToken(int offset) {		
		if (offset == lexStream.getTokenCount())
		    return makeErrorTokenBackwards(offset - 1);
		if (offset > lexStream.getTokenCount())
			return makeErrorTokenBackwards(lexStream.getTokenCount() - 1);

		int endOffset = offset;
		boolean onlySeenWhitespace = Character.isWhitespace(lexStream.getCharValue(endOffset));
		
		while (endOffset + 1 < lexStream.getTokenCount()) {
			char next = lexStream.getCharValue(endOffset+1);
			
			if (onlySeenWhitespace) {
				onlySeenWhitespace = Character.isWhitespace(next);
				offset++;
			} else if (!Character.isLetterOrDigit(next)) {
				break;
			}
			
			endOffset++;
		}
		
		return new Token(parseStream, offset, endOffset, TK_ERROR);
	}
	
	/**
	 * Creates an error token, trying to avoid including whitespace.
	 *
	public IToken makeErrorToken(int beginOffset, int endOffset) {		
		while (beginOffset < endOffset && Character.isWhitespace(lexStream.getCharValue(beginOffset)))
			beginOffset++;
		
		// FIXME: error markers at last character of file don't show up?
		if (endOffset > lexStream.getTokenCount()) {
			endOffset = lexStream.getTokenCount();
			beginOffset = Math.min(beginOffset, endOffset);
		}
		
		if (beginOffset > endOffset && beginOffset > 0)
			beginOffset = endOffset - 1;

		return new Token(parseStream, beginOffset, endOffset, TK_ERROR);
	}
	
	/**
	 * Changes the token kinds of existing tokens.
	 *
	public void changeTokenKinds(int beginOffset, int endOffset, int fromKind, int toKind) {
		// FIXME: changeTokenKinds sometimes changes the token kinds of comments just adjacent to erroneous regions
		//        (not sure if it still does that with the 0.5.1 tokenization changes)
		int fromOrdinal = fromKind;
		ITokenizer tokens = lexStream.getCurrentTokenizer();
		for (int i = 0, end = tokens.getTokenCount(); i < end; i++) {
			IToken token = tokens.getTokenAt(i);
			if (token.getEndOffset() >= beginOffset && token.getKind() == fromOrdinal) {
				token.setKind(toKind);
			}
			if (token.getEndOffset() >= endOffset)
				return;
		}
	}

	/**
	 * Creates an error token on stream part, backwards skipping whitespace
	 * 
	 * @param beginOffset       The begin offset of the erroneous location.
     * @param endOffset         The end offset of the erroneous location.
     * @param outerBeginOffset  The begin offset of the enclosing construct.
	 *
	public IToken makeErrorTokenSkipLayout(int beginOffset, int endOffset, int outerBeginOffset) {	    
		if (endOffset >= lexStream.getTokenCount()) {
			endOffset = lexStream.getTokenCount() - 1;
			beginOffset = Math.min(beginOffset, endOffset);
		}

		int skipLength;
		int newlineSkipLength = -1;
		
		for (skipLength = 0; beginOffset - skipLength > 0; skipLength++) {
			int offset = beginOffset - skipLength - 1;
			char c = lexStream.getCharValue(offset);
			if (!Character.isWhitespace(c)) {
			    if (newlineSkipLength == -1) {
			    	if (skipLength >= 1 && endOffset == beginOffset) {
				    	// Report in whitespace just after the current token
			    		return new Token(parseStream, beginOffset - skipLength, endOffset - skipLength, TK_ERROR);
			    	}
			    	break;
			    } else {
			        if (lexStream.getLine(offset) != lexStream.getLine(outerBeginOffset)) {
	                    // Report the error at the next newline
			        	// if the outer construct started on a different line
			            skipLength = newlineSkipLength;
			            break;
			        } else {
			        	// Skip to the previous token at the end of this line
			        	// if the outer construct started on the same line
			        	return makeErrorTokenBackwards(beginOffset - skipLength);
			        }
			    }
			}
			if (c == '\n')
			    newlineSkipLength = skipLength;
			    
		}
		
		return makeErrorToken(beginOffset - skipLength, endOffset - skipLength);
	}
	
	/**
	 * Creates an error token from the last whitespace character.
	 *
	public IToken makeErrorTokenBackwards(int offset) {
		int beginOffset = offset;
		boolean onlySeenWhitespace = true;
		
		while (offset >= lexStream.getTokenCount())
			offset--;
		
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
		
		return new Token(parseStream, beginOffset, offset, TK_ERROR);
	}
	
	public IToken getLastTokenOnSameLine(IToken token) {
		ITokenizer stream = token.getTokenizer();
		int i = token.getIndex();
		int line = stream.getEndLine(i);
		IToken result = token;
		IToken current = token;
		while (current.getKind() != IToken.TK_EOF && current.getEndLine() == line) {
			result = current;
			current = stream.getTokenAt(++i); 
		}
		return result;
	}
	
	public static String dumpToString(IToken left, IToken right) {
		StringBuilder result = new StringBuilder();
		int last = right.getIndex();
		
		for (int i = left.getIndex(); i <= last; i++) {
			IToken token = left.getTokenizer().getTokenAt(i);
			result.append(Token.tokenKindToString(token.getKind()));
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
*/