package org.strategoxt.imp.runtime.parser.tokens;

import java.util.Iterator;

import lpg.runtime.IToken;
import lpg.runtime.PrsStream;

import org.eclipse.jface.text.IRegion;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SGLRTokenIterator implements Iterator<IToken> {
	final int lastIndex;
	
	final PrsStream stream;
	
	int index;
	
	public SGLRTokenIterator(PrsStream stream, IRegion region) {
		this.stream = stream;
		index = stream.getTokenIndexAtCharacter(region.getOffset());
		lastIndex = Math.abs(stream.getTokenIndexAtCharacter(region.getOffset() + region.getLength()));
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
