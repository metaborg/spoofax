package org.strategoxt.imp.runtime.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.strategoxt.imp.runtime.stratego.SourceAttachment;
import org.strategoxt.lang.WeakValueHashMap;

/**
 * An abstract imploding SGLR parser class.
 *
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */ 
public abstract class AbstractSGLRI {
	
	@SuppressWarnings("unused")
	private static final Map<ParseCacheKey, IStrategoTerm> parseCache =
		Collections.synchronizedMap(new WeakValueHashMap<ParseCacheKey, IStrategoTerm>());
	
	private final SGLRParseController controller;
	
	private boolean implodeEnabled = true;
		
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
	
	public void setImplodeEnabled(boolean implodeEnabled) {
		this.implodeEnabled = implodeEnabled;
	}
	
	public boolean isImplodeEnabled() {
		return implodeEnabled;
	}
	
	public void setCustomDisambiguator(CustomDisambiguator disambiguator) {
		this.disambiguator = disambiguator;
	}
	
	private CustomDisambiguator disambiguator;
	
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
		 *       (should still be disabled for testing language)
		CachingKey cachingKey = new CachingKey(parseTableId, startSymbol, inputChars, filename);
		IStrategoTerm result = parsedCache.get(cachingKey);
		if (result != null) {
			currentTokenizer = getTokenizer(result);
			assert currentTokenizer != null;
			return result;
		}
		*/

		assert getController() == null || getController().getParseLock().isHeldByCurrentThread();
		IStrategoTerm result = doParse(input, filename);
		if (new NullProgressMonitor().isCanceled())
			throw new OperationCanceledException();
		SGLRParseController controller = getController() == null ? null : getController();
		IResource resource = controller == null ? null : controller.getResource();
		if(resource==null && filename !=null) {
			File file = new File(filename);
			if (file.exists() && EditorIOAgent.isResource(file)) {
				resource = EditorIOAgent.getResource(file);
			}
		}
		if (controller != null || resource != null)
			SourceAttachment.putSource(result, resource, controller);
		
		if (disambiguator != null)
			result = disambiguator.disambiguate(result);
		
		// parsedCache.put(cachingKey, result);
		// putTokenizer(result, currentTokenizer);
		
		return result;
	}
	
	/**
	 * Parse an input, returning the AST and initializing the parse stream.
	 * Also initializes a new tokenizer for the given input.
	 * 
	 * @note This redirects to the preferred {@link #parse(String, String)} method.
	 * 
	 * @return  The abstract syntax tree.
	 */
	public final IStrategoTerm parse(InputStream input, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException {
		
		String inputString = FileTools.loadFileAsString(new BufferedReader(new InputStreamReader(input)));
		return parse(inputString, filename);
	}
	
	protected abstract IStrategoTerm doParse(String input, String filename)
			throws TokenExpectedException, BadTokenException, SGLRException, IOException;
}