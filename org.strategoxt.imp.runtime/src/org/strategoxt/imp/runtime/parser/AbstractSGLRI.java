package org.strategoxt.imp.runtime.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import lpg.runtime.ILexStream;
import lpg.runtime.Monitor;
import lpg.runtime.PrsStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.imp.parser.IParser;
import org.jboss.util.collection.WeakValueHashMap;
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
	
	private static final Map<CachingKey, ATerm> parsedCache =
		Collections.synchronizedMap(new WeakValueHashMap<CachingKey, ATerm>());
	
	private static final Map<ATerm, SGLRTokenizer> tokenizerCache =
		Collections.synchronizedMap(new WeakHashMap<ATerm, SGLRTokenizer>());
	
	private final SGLRParseController controller;
	
	private final TokenKindManager tokenManager;
	
	private final char[] buffer = new char[2048];
	
	private final Object parseTableId;
	
	private AsfixImploder imploder;
	
	private String startSymbol;
	
	private SGLRTokenizer currentTokenizer;
	
	// Simple accessors
	
	protected SGLRTokenizer getTokenizer() {
		return currentTokenizer; 
	}

	public int getEOFTokenKind() {
		return TokenKind.TK_EOF.ordinal();
	}

	/**
	 * Get the current parsestream.
	 */
	public PrsStream getParseStream() {
		SGLRTokenizer tokenizer = getTokenizer();
		if (tokenizer == null) return null;
		return tokenizer.getParseStream();
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
	
	/**
	 * Sets whether to keep any unresolved ambiguities. Default true.
	 */
	public void setKeepAmbiguities(boolean value) {
		imploder = value
			? new AmbAsfixImploder(tokenManager)
			: new AsfixImploder(tokenManager);
	}
	
	// Initialization and parsing
	
	public AbstractSGLRI(SGLRParseController controller, TokenKindManager tokenManager, String startSymbol, Object parseTableId) {
		this.controller = controller;
		this.startSymbol = startSymbol;
		this.tokenManager = tokenManager;
		this.parseTableId = parseTableId;

		imploder = new AmbAsfixImploder(tokenManager);
	}
	
	// Parsing
	
	/**
	 * Parse an input, returning the AST and initializing the parse stream.
	 * 
	 * @return  The abstract syntax tree.
	 */
	protected RootAstNode parse(char[] inputChars, String filename, IProgressMonitor monitor)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {

		ATerm asfix = parseNoImplode(inputChars, filename);
		if (monitor.isCanceled())
			throw new OperationCanceledException();
		return internalImplode(asfix);
	}
	
	/**
	 * Parse an input, returning the AST and initializing the parse stream.
	 * 
	 * @return  The abstract syntax tree.
	 */
	public RootAstNode parse(char[] inputChars, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {

		return parse(inputChars, filename, new NullProgressMonitor());
	}
	
	/**
	 * Parse an input, returning the AST and initializing the parse stream.
	 * Also initializes a new tokenizer for the given input.
	 * 
	 * @note This redirects to the preferred {@link #parse(char[], String)} method.
	 * 
	 * @return  The abstract syntax tree.
	 */
	public final RootAstNode parse(InputStream input, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		return parse(toCharArray(input), filename);
	}
	
	/**
	 * Implodes a parse tree that was just produced.
	 * 
	 * @note May only work with the latest parse tree produced.
	 */
	protected RootAstNode internalImplode(ATerm asfix) {
		AstNode imploded = imploder.implode(asfix, currentTokenizer);
		return RootAstNode.makeRoot(imploded, getController() == null ? null : getController().getResource());
	}
	
	/**
	 * Parse an input, returning the AST and initializing the parse stream.
	 * Also initializes a new tokenizer for the given input.
	 */ 
	public ATerm parseNoImplode(char[] inputChars, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		CachingKey cachingKey = new CachingKey(parseTableId, startSymbol, inputChars);
		ATerm result = parsedCache.get(cachingKey);
		if (result != null) {
			currentTokenizer = getTokenizer(result);
			assert currentTokenizer != null;
			return result;
		}
		
		Debug.startTimer();
		try {
			currentTokenizer = new SGLRTokenizer(inputChars, filename);
			result = doParseNoImplode(inputChars, filename);
			parsedCache.put(cachingKey, result);
			putTokenizer(result, currentTokenizer);
		
			return result;
		} finally {
			Debug.stopTimer("File parsed");
		}
	}
	
	@Deprecated
	public void reset(ILexStream lexStream) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Retrieve the original tokenizer for an asfix tree,
	 * if available.
	 */
	public static SGLRTokenizer getTokenizer(ATerm asfix) {
		return tokenizerCache.get(asfix);
	}

	/**
	 * Store the original tokenizer for an asfix tree.
	 */
	public static SGLRTokenizer putTokenizer(ATerm asfix, SGLRTokenizer tokenizer) {
		return tokenizerCache.put(asfix, tokenizer);
	}
	
	protected abstract ATerm doParseNoImplode(char[] inputChars, String filename)
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
		// FIXME: AbstractSGLRI.toByteStream() breaks extended ASCII support
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
		return parseTable.equals(other.parseTable)
			&& Arrays.equals(input, other.input)
			&& (startSymbol == null ? other.startSymbol == null : startSymbol.equals(other.startSymbol));
	}
	
	@Override
	public int hashCode() {
		// (Ignores parse table hash code)
		return 12125125 * (startSymbol == null ? 42 : startSymbol.hashCode())
			^ Arrays.hashCode(input);
	}
}