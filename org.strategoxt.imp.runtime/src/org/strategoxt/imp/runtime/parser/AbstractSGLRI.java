package org.strategoxt.imp.runtime.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import lpg.runtime.Monitor;
import lpg.runtime.PrsStream;

import org.eclipse.imp.parser.IParser;
import org.spoofax.jsglr.BadTokenException;
import org.spoofax.jsglr.SGLRException;
import org.spoofax.jsglr.TokenExpectedException;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.parser.ast.AmbAsfixImploder;
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
public abstract class AbstractSGLRI implements IParser {
	
	private static final Map<CachingKey, WeakReference<RootAstNode>> implodedCache =
		Collections.synchronizedMap(new WeakHashMap<CachingKey, WeakReference<RootAstNode>>());
	
	private final SGLRParseController controller;
	
	private final SGLRTokenizer tokenizer;
	
	private final TokenKindManager tokenManager;
	
	private final char[] buffer = new char[2048];
	
	private final Object parseTableId;
	
	private AsfixImploder imploder;
	
	private String startSymbol;
	
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
	
	public SGLRParseController getController() {
		return controller;
	}
	
	public String getStartSymbol() {
		return startSymbol;
	}
	
	public void setStartSymbol(String startSymbol) {
		this.startSymbol = startSymbol;
	}
	
	public void setKeepAmbiguities(boolean value) {
		imploder = value
			? new AmbAsfixImploder(tokenManager, tokenizer)
			: new AsfixImploder(tokenManager, tokenizer);
	}
	
	// Initialization and parsing
	
	public AbstractSGLRI(SGLRParseController controller, TokenKindManager tokenManager, String startSymbol, Object parseTableId) {
		this.controller = controller;
		this.startSymbol = startSymbol;
		this.tokenManager = tokenManager;
		this.parseTableId = parseTableId;

		tokenizer = new SGLRTokenizer();		
		imploder = new AsfixImploder(tokenManager, tokenizer);
	}
	
	// Parsing
	
	/**
	 * Parse an input, returning the AST and initializing the parse stream.
	 * 
	 * @return  The abstract syntax tree.
	 */
	public RootAstNode parse(char[] inputChars, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		// TODO: Support caching in parseNoImplode
		
		CachingKey cachingKey = new CachingKey(parseTableId, startSymbol, inputChars);
		Reference<RootAstNode> cached = implodedCache.get(cachingKey);
		if (cached != null) return cached.get();
		
		tokenizer.init(inputChars, filename);
		
		Debug.startTimer();
		ATerm asfix = parseNoImplode(inputChars, filename);
		Debug.stopTimer("File parsed: " + filename);
			
		AstNode imploded = imploder.implode(asfix);
		RootAstNode result = RootAstNode.makeRoot(imploded, getController());
		
		result.setCachingKey(cachingKey);
		implodedCache.put(cachingKey, new WeakReference<RootAstNode>(result));
		return result;
	}
	
	/**
	 * Parse an input, returning the AST and initializing the parse stream.
	 * 
	 * @note This redirects to the preferred {@link #parse(char[], String)} method.
	 * 
	 * @return  The abstract syntax tree.
	 */
	public RootAstNode parse(InputStream input, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		return parse(toCharArray(input), filename);
	}
	
	public abstract ATerm parseNoImplode(char[] inputChars, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException;

	private char[] toCharArray(InputStream input) throws IOException {
		StringBuilder copy = new StringBuilder();
		InputStreamReader reader = new InputStreamReader(input);
		
		for (int read = 0; read != -1; read = reader.read(buffer))
			copy.append(buffer, 0, read);
		
		char[] chars = new char[copy.length()];
		copy.getChars(0, copy.length(), chars, 0);
		
		return chars;
	}
	
	protected static ByteArrayInputStream toByteStream(char[] chars) {
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

/**
 * A tuple class. Gotta love the Java.
 */
class CachingKey {
	private Object parseTable;
	private String startSymbol;
	private char[] input;
	
	public CachingKey(Object parseTable, String startSymbol, char[] input) {
		this.parseTable = parseTable;
		this.startSymbol = startSymbol;
		this.input = input;
	}

	@Override
	public boolean equals(Object obj) {
		CachingKey other = (CachingKey) obj;
		return parseTable.equals(other.parseTable) && Arrays.equals(input, other.input) && 
			(startSymbol == null ? other.startSymbol == null : startSymbol.equals(other.startSymbol));
	}
	
	@Override
	public int hashCode() {
		// (Ignores parse table hash code)
		return 12125125 * (startSymbol == null ? 42 : startSymbol.hashCode())
			^ Arrays.hashCode(input);
	}
}