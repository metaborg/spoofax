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
		index = Math.abs(stream.getTokenIndexAtCharacter(region.getOffset()));
		
		int lastIndex = stream.getTokenIndexAtCharacter(region.getOffset() + region.getLength());
		if (lastIndex < 0)
			lastIndex = -lastIndex + 1;
		if (lastIndex >= stream.getTokens().size())
			lastIndex = stream.getTokens().size() - 1;
		//if (lastIndex > 0 && stream.getTokenAt(lastIndex).getKind() == TokenKind.TK_EOF.ordinal())
		//	lastIndex--;
		if (lastIndex > 0 && stream.getTokenAt(lastIndex).getStartOffset() == 0)
			lastIndex--;
		
		this.lastIndex = lastIndex;
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
