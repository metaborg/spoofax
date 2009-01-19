package org.strategoxt.imp.runtime.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import lpg.runtime.Monitor;
import lpg.runtime.PrsStream;

import org.eclipse.imp.language.Language;
import org.eclipse.imp.parser.IParser;
import org.spoofax.jsglr.BadTokenException;
import org.spoofax.jsglr.InvalidParseTableException;
import org.spoofax.jsglr.ParseTable;
import org.spoofax.jsglr.SGLR;
import org.spoofax.jsglr.SGLRException;
import org.spoofax.jsglr.TokenExpectedException;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.AsfixImploder;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.ast.RootAstNode;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenizer;
import org.strategoxt.imp.runtime.parser.tokens.TokenKind;
import org.strategoxt.imp.runtime.parser.tokens.TokenKindManager;

import aterm.ATerm;

/**
 * IMP IParser implementation for SGLR, imploding parse trees to AST nodes and tokens.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */ 
public class SGLRParser implements IParser {
	private final SGLRParseController controller;
	
	private final SGLR parser;
	
	private final String startSymbol;
	
	private final AsfixImploder converter;
	
	private final SGLRTokenizer tokenizer;
	
	private final char[] buffer = new char[4096];
	
	// Simple accessors
	
	public SGLRTokenizer getTokenizer() {
		return tokenizer; 
	}

	public int getEOFTokenKind() {
		return TokenKind.TK_EOF.ordinal();
	}

	public PrsStream getParseStream() {
		return getTokenizer().getParseStream();
	}
	
	// Initialization and parsing
	
	public SGLRParser(SGLRParseController controller, TokenKindManager tokenManager,
			ParseTable parseTable, String startSymbol) {
		
		// TODO: Once spoofax supports it, use a start symbol
		this.controller = controller;
		this.startSymbol = null; // startSymbol;

		tokenizer = new SGLRTokenizer();		
		converter = new AsfixImploder(tokenManager, tokenizer);
		parser = Environment.createSGLR(parseTable);
		parser.setCycleDetect(false);
		parser.setFilter(false);
	}
	
	public SGLRParser(ParseTable parseTable, String startSymbol) {
		this(null, new TokenKindManager(), parseTable, startSymbol);
	}
	
	/**
	 * Parse an input, returning the AST and initializing the parse stream.
	 * 
	 * @return  The abstract syntax tree.
	 */
	public RootAstNode parse(InputStream inputStream, char[] inputChars, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		Debug.startTimer();
		
		// TODO: Parse caching
		//       using a WeakHashMap<char[], WeakReference<AstNode>>

		// Read stream using tokenizer/lexstream
		tokenizer.init(inputChars, filename);
		
		ATerm asfix = parser.parse(inputStream, startSymbol);
			
		Debug.stopTimer("File parsed");
		Debug.startTimer();
		
		AstNode imploded = converter.implode(asfix);
		RootAstNode result = RootAstNode.makeRoot(imploded, controller, inputChars);
		
		if (Debug.ENABLED) {
			Debug.log("Parsed " + result.toString());
		}
			
		Debug.stopTimer("Parse tree imploded");
		
		return result;
	}
	
	public RootAstNode parse(char[] inputChars, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		return parse(toByteStream(inputChars), inputChars, filename);
	}
	
	public RootAstNode parse(InputStream input, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		return parse(toCharArray(input), filename); // don't ask
	}
	
	// Utility methods

	private char[] toCharArray(InputStream input) throws IOException {
		StringBuilder copy = new StringBuilder();
		InputStreamReader reader = new InputStreamReader(input);
		
		for (int read = 0; read != -1; read = reader.read(buffer))
			copy.append(buffer, 0, read);
		
		char[] chars = new char[copy.length()];
		copy.getChars(0, copy.length(), chars, 0);
		
		return chars;
	}
	
	private static ByteArrayInputStream toByteStream(char[] chars) {
		byte[] bytes = new byte[chars.length];
		
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte) chars[i];
		
		return new ByteArrayInputStream(bytes);
	}
	
	// LPG legacy / compatibility

	@Deprecated
	public Object parser(Monitor monitor, int error_repair_count) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public String[] orderedTerminalSymbols() {
		return null; // should map token kinds to names
	}

	@Deprecated
	public int numTokenKinds() {
		return 10;
	}
}
