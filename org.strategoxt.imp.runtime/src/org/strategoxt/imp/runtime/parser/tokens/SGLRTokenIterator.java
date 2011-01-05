package org.strategoxt.imp.runtime.parser.tokens;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.jface.text.IRegion;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SGLRTokenIterator implements Iterator<IToken> {
	
	public static Iterator<IToken> EMPTY = new Iterator<IToken>() {
		public boolean hasNext() {
			return false;
		}
		
		public IToken next() {
			throw new NoSuchElementException();
		}
		
		public void remove() {
			throw new UnsupportedOperationException();
		}
	};
	
	private final int lastIndex;
	
	private final ITokenizer stream;
	
	private int index;
	
	public SGLRTokenIterator(ITokenizer stream, IRegion region) {
		this.stream = stream;
		index = getStartIndex(stream, region);
		lastIndex = getLastIndex(stream, region);
	}

	private static int getStartIndex(ITokenizer stream, IRegion region) {
		IToken resultToken = stream.getTokenAtOffset(region.getOffset());
		int result = resultToken == null ? -1 : resultToken.getIndex();
		if (result < 1)
			result = 1; // skip reserved initial token
		return result;
	}

	private static int getLastIndex(ITokenizer tokens, IRegion region) {
		int end = region.getOffset() + region.getLength() + 1;
		
		int result = tokens.getTokenCount();
		while (--result > 0) {
			IToken token = tokens.getTokenAt(result);
			if (token.getKind() == IToken.TK_EOF)
				break;
		}
		if (result == 0 && tokens.getTokenCount() > 20)
			throw new IllegalStateException("No EOF token in parse stream");

		while (--result > 0) {
			IToken token = tokens.getTokenAt(result);
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
