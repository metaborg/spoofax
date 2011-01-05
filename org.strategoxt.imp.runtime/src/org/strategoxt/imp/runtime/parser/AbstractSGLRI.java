package org.strategoxt.imp.runtime.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.io.FileTools;
import org.spoofax.jsglr.shared.BadTokenException;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.jsglr.shared.TokenExpectedException;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;
import org.strategoxt.lang.WeakValueHashMap;

/**
 * An abstract imploding SGLR parser class.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */ 
public abstract class AbstractSGLRI {
	
	@SuppressWarnings("unused")
	private static final Map<CachingKey, IStrategoTerm> parsedCache =
		Collections.synchronizedMap(new WeakValueHashMap<CachingKey, IStrategoTerm>());
	
	private final SGLRParseController controller;
		
	@SuppressWarnings("unused")
	private final Object parseTableId;
	
	private String startSymbol;
	
	// Simple accessors

	public int getEOFTokenKind() {
		return IToken.TK_EOF;
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
	
	@Deprecated
	public void setKeepAmbiguities(boolean value) {
		if (!value)
			throw new UnsupportedOperationException();
	}
	
	// Initialization and parsing
	
	public AbstractSGLRI(Object parseTableId, String startSymbol, SGLRParseController controller) {
		this.controller = controller;
		this.startSymbol = startSymbol;
		this.parseTableId = parseTableId;
	}
	
	// Parsing
	
	/**
	 * Parse an input, returning the AST and initializing the parse stream.
	 * 
	 * @return  The abstract syntax tree.
	 */
	public IStrategoTerm parse(String input, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		
		/* UNDONE: disabled the parse cache for now
		 * TODO: revise parse cache?
		CachingKey cachingKey = new CachingKey(parseTableId, startSymbol, inputChars, filename);
		IStrategoTerm result = parsedCache.get(cachingKey);
		if (result != null) {
			currentTokenizer = getTokenizer(result);
			assert currentTokenizer != null;
			return result;
		}
		*/

		IStrategoTerm result = doParseAndImplode(input, filename);
		if (new NullProgressMonitor().isCanceled())
			throw new OperationCanceledException();
		SGLRParseController controller = getController() == null ? null : getController();
		IResource resource = controller == null ? null : controller.getResource();
		SourceAttachment.setSource(result, resource, controller);
		
		// parsedCache.put(cachingKey, result);
		// putTokenizer(result, currentTokenizer);
		
		return result;
	}
	
	/**
	 * Parse an input, returning the AST and initializing the parse stream.
	 * Also initializes a new tokenizer for the given input.
	 * 
	 * @note This redirects to the preferred {@link #parse(char[], String)} method.
	 * 
	 * @return  The abstract syntax tree.
	 */
	public final IStrategoTerm parse(InputStream input, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		String inputString = FileTools.loadFileAsString(new BufferedReader(new InputStreamReader(input)));
		return parse(inputString, filename);
	}
	
	protected abstract IStrategoTerm doParseAndImplode(String inputChars, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException;
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