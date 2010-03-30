package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermAppl;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenizer;
import org.strategoxt.lang.terms.StrategoWrapped;

import aterm.ATerm;

/**
 * Maintains mappings between input streams, parse trees, etc. and their origins.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SourceMappings {
	
	private final Map<Integer, File> inputFileMap = new WeakHashMap<Integer, File>();
	
	private final Map<MappableKey, File> stringInputFileMap = new WeakHashMap<MappableKey, File>();
	
	private final Map<IStrategoTerm, File> asfixInputFileMap = new WeakHashMap<IStrategoTerm, File>();
	
	private final Map<IStrategoTerm, char[]> inputCharMap = new WeakHashMap<IStrategoTerm, char[]>();

	private final Map<IStrategoTerm, ATerm> inputTermMap = new WeakHashMap<IStrategoTerm, ATerm>();

	private final Map<IStrategoTerm, SGLRTokenizer> tokenizerMap = new WeakHashMap<IStrategoTerm, SGLRTokenizer>();

	public File putInputFile(int fd, File file) {
		return inputFileMap.put(fd, file);
	}

	public File putInputFile(MappableString string, File file) {
		return stringInputFileMap.put(string.identityKey, file);
	}
	
	public File putInputFile(IStrategoAppl asfix, File file) {
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
	
	public File getInputFile(int fd) {
		return inputFileMap.get(fd);
	}
	
	public File getInputFile(IStrategoString string) {
		if (string instanceof MappableString) {
			MappableKey key = ((MappableString) string).identityKey;
			return stringInputFileMap.get(key);
		} else {
			return null;
		}
	}
	
	public File getInputFile(IStrategoAppl asfix) {
		assert isTermAppl(asfix);
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
	
	/**
	 * @author Lennart Kats <lennart add lclnet.nl>
	 */
	public static class MappableString extends StrategoWrapped {

		final MappableKey identityKey = new MappableKey();
		
		public MappableString(IStrategoString wrapped) {
			super(wrapped);
		}

		@Override
		public int getStorageType() {
			return IMMUTABLE;
		}
	}
	
	private static class MappableKey {
		// Just used for identity hashcode and equals implementation
	}
}
