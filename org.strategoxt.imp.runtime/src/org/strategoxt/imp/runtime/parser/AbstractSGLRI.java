package org.strategoxt.imp.runtime.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import lpg.runtime.IPrsStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.spoofax.interpreter.terms.IStrategoTerm;
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
import org.strategoxt.lang.WeakValueHashMap;

import aterm.ATerm;

/**
 * IMP IParser implementation for SGLR, imploding parse trees to AST nodes and tokens.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */ 
public abstract class AbstractSGLRI {
	
	@SuppressWarnings("unused")
	private static final Map<CachingKey, ATerm> parsedCache =
		Collections.synchronizedMap(new WeakValueHashMap<CachingKey, ATerm>());
	
	private final SGLRParseController controller;
	
	private final TokenKindManager tokenManager;
	
	private final char[] buffer = new char[2048];
	
	@SuppressWarnings("unused")
	private final Object parseTableId;
	
	private AsfixImploder imploder;
	
	private String startSymbol;
	
	private SGLRTokenizer currentTokenizer;
	
	// Simple accessors
	
	public SGLRTokenizer getTokenizer() {
		return currentTokenizer; 
	}

	public int getEOFTokenKind() {
		return TokenKind.TK_EOF.ordinal();
	}

	/**
	 * Get the current parsestream.
	 */
	public IPrsStream getParseStream() {
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
	
	// Preferred overload in new-terms branch
	public IStrategoTerm parse(String inputChars, String filename) throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		return parse(inputChars.toCharArray(), filename).getTerm();
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
		SGLRParseController controller = getController() == null ? null : getController();
		IResource resource = controller == null ? null : controller.getResource();
		return RootAstNode.makeRoot(imploded, controller, resource);
	}
	
	/**
	 * Parse an input, returning the AST and initializing the parse stream.
	 * Also initializes a new tokenizer for the given input.
	 */ 
	public ATerm parseNoImplode(char[] inputChars, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		/* UNDONE: disabled the parse cache for now
		 * TODO: revise parse cache?
		CachingKey cachingKey = new CachingKey(parseTableId, startSymbol, inputChars, filename);
		ATerm result = parsedCache.get(cachingKey);
		if (result != null) {
			currentTokenizer = getTokenizer(result);
			assert currentTokenizer != null;
			return result;
		}
		*/
		
		Debug.startTimer();
		try {
			currentTokenizer = new SGLRTokenizer(inputChars, filename);
			ATerm result = doParseNoImplode(inputChars, filename);
			// parsedCache.put(cachingKey, result);
			// putTokenizer(result, currentTokenizer);
		
			return result;
		} finally {
			Debug.stopTimer("File parsed");
		}
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
}

/**
 * A tuple class. Gotta love the Java.
 */
class CachingKey {
	private Object parseTable;
	private String startSymbol;
	private char[] input;
	private String filename; // essential to keep a consistent ast/tokens/resource mapping 
	
	public CachingKey(Object parseTable, String startSymbol, char[] input, String filename) {
		this.parseTable = parseTable;
		this.startSymbol = startSymbol;
		this.input = input;
		this.filename = filename;
	}

	@Override
	public boolean equals(Object obj) {
		CachingKey other = (CachingKey) obj;
		return parseTable.equals(other.parseTable)
			&& Arrays.equals(input, other.input)
			&& (filename == null ? other.filename == null : filename.equals(other.filename))
			&& (startSymbol == null ? other.startSymbol == null : startSymbol.equals(other.startSymbol));
	}
	
	@Override
	public int hashCode() {
		// (Ignores parse table hash code)
		return 12125125
			* (startSymbol == null ? 42 : startSymbol.hashCode())
			* (filename == null ? 42 : filename.hashCode())
			^ Arrays.hashCode(input);
	}
}