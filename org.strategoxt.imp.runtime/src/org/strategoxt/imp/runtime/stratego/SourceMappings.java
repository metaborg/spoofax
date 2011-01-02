package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermString;
import static org.strategoxt.imp.runtime.stratego.SourceMappings.MappableTerm.getValue;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.LazyTerm;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenizer;

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

	private final Map<MappableKey, IStrategoTerm> inputTermMap = new WeakHashMap<MappableKey, IStrategoTerm>();

	private final Map<MappableKey, SGLRTokenizer> tokenizerMap = new WeakHashMap<MappableKey, SGLRTokenizer>();

	public File putInputFile(int fd, File file) {
		return inputFileMap.put(fd, file);
	}

	public File putInputFileForString(MappableTerm string, File file) {
		assert isTermString(string);
		return stringInputFileMap.put(string.key, file);
	}
	
	public File putInputFileForTree(MappableTerm asfix, File file) {
		// assert isTermAppl(asfix);
		return asfixInputFileMap.put(asfix.key, file);
	}

	public char[] putInputChars(MappableTerm asfix, char[] inputChars) {
		return inputCharMap.put(asfix.key, inputChars);
	}

	public IStrategoTerm putInputTerm(MappableTerm asfix, IStrategoTerm asfixIStrategoTerm) {
		return inputTermMap.put(asfix.key, asfixIStrategoTerm);
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
		return getValue(asfixInputFileMap, asfix);
	}
	
	public char[] getInputChars(IStrategoTerm asfix) {
		return getValue(inputCharMap, asfix);
	}
	
	public IStrategoTerm getInputTerm(IStrategoTerm asfix) {
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
	public static class MappableTerm extends LazyTerm {

		private final MappableKey key = new MappableKey();
		
		private final IStrategoTerm wrapped;
		
		public MappableTerm(IStrategoTerm wrapped) {
			this.wrapped = wrapped;
		}
		
		@Override
		protected IStrategoTerm init() {
			return wrapped;
		}

		@Override
		public int getStorageType() {
			return IMMUTABLE;
		}
		
		@Override
		public int getTermType() {
			return wrapped.getTermType();
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
