package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermAppl;
import static org.spoofax.interpreter.core.Tools.isTermString;
import static org.strategoxt.imp.runtime.stratego.SourceMappings.MappableTerm.*;

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
	
	private final Map<MappableKey, File> asfixInputFileMap = new WeakHashMap<MappableKey, File>();
	
	private final Map<MappableKey, char[]> inputCharMap = new WeakHashMap<MappableKey, char[]>();

	private final Map<MappableKey, ATerm> inputTermMap = new WeakHashMap<MappableKey, ATerm>();

	private final Map<MappableKey, SGLRTokenizer> tokenizerMap = new WeakHashMap<MappableKey, SGLRTokenizer>();

	public File putInputFile(int fd, File file) {
		return inputFileMap.put(fd, file);
	}

	public File putInputFileForString(MappableTerm string, File file) {
		assert isTermString(string);
		return stringInputFileMap.put(string.key, file);
	}
	
	public File putInputFileForTree(MappableTerm asfix, File file) {
		assert isTermAppl(asfix);
		return asfixInputFileMap.put(asfix.key, file);
	}

	public char[] putInputChars(MappableTerm asfix, char[] inputChars) {
		return inputCharMap.put(asfix.key, inputChars);
	}

	public ATerm putInputTerm(MappableTerm asfix, ATerm asfixATerm) {
		return inputTermMap.put(asfix.key, asfixATerm);
	}
	
	public SGLRTokenizer putTokenizer(MappableTerm asfix, SGLRTokenizer tokenizer) {
		return tokenizerMap.put(asfix.key, tokenizer);
	}
	
	public File getInputFile(int fd) {
		return inputFileMap.get(fd);
	}
	
	public File getInputFile(IStrategoString string) {
		return getValue(stringInputFileMap, string);
	}
	
	public File getInputFile(IStrategoAppl asfix) {
		assert isTermAppl(asfix);
		return getValue(asfixInputFileMap, asfix);
	}
	
	public char[] getInputChars(IStrategoTerm asfix) {
		return getValue(inputCharMap, asfix);
	}
	
	public ATerm getInputTerm(IStrategoTerm asfix) {
		return getValue(inputTermMap, asfix);
	}
	
	public SGLRTokenizer getTokenizer(IStrategoTerm asfix) {
		return getValue(tokenizerMap, asfix);
	}
	
	/**
	 * A wrapped term that provides a key with identity equality semantics
	 * for use in Map implementations where identity equality is preferred.
	 * 
	 * @author Lennart Kats <lennart add lclnet.nl>
	 */
	public static class MappableTerm extends StrategoWrapped {

		private final MappableKey key = new MappableKey();
		
		public MappableTerm(IStrategoTerm wrapped) {
			super(wrapped);
		}

		@Override
		public int getStorageType() {
			return IMMUTABLE;
		}
		
		public static<T> T getValue(Map<? extends MappableKey, T> map, IStrategoTerm term) {
			if (term instanceof MappableTerm) {
				return map.get(((MappableTerm) term).key);
			} else {
				return null;
			}
		}
	}
	
	public static class MappableKey {
		// Just used for identity hashcode and equals implementation
	}
}
