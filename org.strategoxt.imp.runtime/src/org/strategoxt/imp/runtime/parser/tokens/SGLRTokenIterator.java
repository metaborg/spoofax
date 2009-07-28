package org.strategoxt.imp.runtime.parser.tokens;

import java.util.Iterator;
import java.util.List;

import lpg.runtime.IToken;
import lpg.runtime.PrsStream;

import org.eclipse.jface.text.IRegion;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SGLRTokenIterator implements Iterator<IToken> {
	private final int lastIndex;
	
	private final PrsStream stream;
	
	private int index;
	
	public SGLRTokenIterator(PrsStream stream, IRegion region) {
		this.stream = stream;
		index = getStartIndex(stream, region);
		lastIndex = getLastIndex(stream, region);
	}

	private static int getStartIndex(PrsStream stream, IRegion region) {
		int result = Math.abs(stream.getTokenIndexAtCharacter(region.getOffset()));
		if (result == 0)
			result = 1; // skip reserved initial token
		return result;
	}

	private static int getLastIndex(PrsStream stream, IRegion region) {
		List tokens = stream.getTokens();
		int end = region.getOffset() + region.getLength() + 1;
		
		int result = stream.getStreamLength();
		while (result-- > 0) {
			IToken token = (IToken) tokens.get(result);
			if (token.getKind() == TokenKind.TK_EOF.ordinal())
				break;
		}

		while (result-- > 0) {
			IToken token = (IToken) tokens.get(result);
			if (token.getEndOffset() <= end)
				break;
		}
		
		return result;
	}

	public boolean hasNext() {
		return index <= lastIndex;
	}

	public IToken next() {
		return stream.getTokenAt(index++);
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
