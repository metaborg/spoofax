package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.terms.IStrategoTerm.*;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenizer;

import aterm.ATerm;

/**
 * Maintains mappings between input streams, parse trees, etc. and their origins.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SourceMappings {
	
	private final Map<InputStream, File> inputFileMap = new WeakHashMap<InputStream, File>();
	
	private final Map<IStrategoString, File> stringInputFileMap = new WeakHashMap<IStrategoString, File>();
	
	private final Map<IStrategoTerm, File> asfixInputFileMap = new WeakHashMap<IStrategoTerm, File>();
	
	private final Map<IStrategoTerm, char[]> inputCharMap = new WeakHashMap<IStrategoTerm, char[]>();

	private final Map<IStrategoTerm, ATerm> inputTermMap = new WeakHashMap<IStrategoTerm, ATerm>();

	private final Map<IStrategoTerm, SGLRTokenizer> tokenizerMap = new WeakHashMap<IStrategoTerm, SGLRTokenizer>();

	public File putInputFile(InputStream stream, File file) {
		return inputFileMap.put(stream, file);
	}

	public File putInputFile(IStrategoString string, File file) {
		return stringInputFileMap.put(string, file);
	}
	
	public File putInputFile(IStrategoTerm asfix, File file) {
		return asfixInputFileMap.put(asfix, file);
	}

	public char[] putInputChars(IStrategoTerm asfix, char[] inputChars) {
		return inputCharMap.put(asfix, inputChars);
	}

	public ATerm putInputTerm(IStrategoTerm asfix, ATerm asfixATerm) {
		return inputTermMap.put(asfix, asfixATerm);
	}
	
	public SGLRTokenizer putTokenizer(IStrategoTerm asfix, SGLRTokenizer tokenizer) {
		return tokenizerMap.put(asfix, tokenizer);
	}
	
	public File getInputFile(InputStream stream) {
		return inputFileMap.get(stream);
	}
	
	public File getInputFile(IStrategoString string) {
		return stringInputFileMap.get(string);
	}
	
	public File getInputFile(IStrategoTerm asfix) {
		assert asfix.getTermType() == APPL;
		return asfixInputFileMap.get(asfix);
	}
	
	public char[] getInputChars(IStrategoTerm asfix) {
		return inputCharMap.get(asfix);
	}
	
	public ATerm getInputTerm(IStrategoTerm asfix) {
		return inputTermMap.get(asfix);
	}
	
	public SGLRTokenizer getTokenizer(IStrategoTerm asfix) {
		return tokenizerMap.get(asfix);
	}
}
