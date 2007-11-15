package org.strategoxt.imp.runtime.parser;

import java.io.IOException;

import lpg.runtime.IToken;
import lpg.runtime.LexStream;
import lpg.runtime.PrsStream;

public class SGLRTokenizer {
	private final LexStream lexStream = new LexStream();
	private final PrsStream parseStream = new PrsStream(lexStream);
	
	private int position, count;
	
	public PrsStream getParseStream() {
		return parseStream;
	}
	
	public LexStream getLexStream() {
		return lexStream;
	}
	
	// TODO: Don't initialize SGLRTokenizer using filename
	public void init(String filename) throws IOException {
		lexStream.initialize(filename);
		parseStream.resetTokenStream();
		position = count = 0;
	}
	
	public IToken add(int length, int kind) {
		parseStream.makeToken(position, position + length, kind);
		IToken result = parseStream.getTokenAt(count);
		
		count++;
		position += length;
		
		return result;
	}
}
