package org.strategoxt.imp.runtime.parser.tokens;

import java.util.Iterator;

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
		// TODO: Just fetch the last token by hand; the IMP interface and this workaround is terrible
		int result = stream.getTokenIndexAtCharacter(region.getOffset() + region.getLength());
		if (result < 0)
			result = -result + 1;
		if (result >= stream.getTokens().size())
			result = stream.getTokens().size() - 1;
		if (result > 0 && stream.getTokenAt(result).getStartOffset() == 0)
			result--;
		// while (stream.getTokenAt(result).getKind() == TokenKind.TK_ERROR.ordinal())
		// 	result--;
		return result;
	}

	public boolean hasNext() {
		return index <= lastIndex
			&& stream.getTokenAt(index).getKind() != TokenKind.TK_ERROR.ordinal()
			&& stream.getTokenAt(index).getKind() != TokenKind.TK_UNKNOWN.ordinal();
	}

	public IToken next() {
		return stream.getTokenAt(index++);
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
