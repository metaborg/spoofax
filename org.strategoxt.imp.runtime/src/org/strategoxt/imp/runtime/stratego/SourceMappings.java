package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.interpreter.core.Tools.isTermString;
import static org.strategoxt.imp.runtime.stratego.SourceMappings.MappableTerm.getValue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.LazyTerm;

/**
 * Maintains mappings between input streams, parse trees, etc. and their origins.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SourceMappings {
	
	private static final int FILE_DESCRIPTOR_EXPIRATION = 40;
	
	// TODO: use term attachments instead of weak hash maps..?
	
	private final Map<Integer, File> inputFileMap = new HashMap<Integer, File>();
	
	private final Map<MappableKey, File> stringInputFileMap = new WeakHashMap<MappableKey, File>();
	
	private final Map<MappableKey, File> asfixInputFileMap = new WeakHashMap<MappableKey, File>();
	
	private final Map<MappableKey, String> inputCharMap = new WeakHashMap<MappableKey, String>();

	private final Map<MappableKey, IStrategoTerm> inputTermMap = new WeakHashMap<MappableKey, IStrategoTerm>();

	public File putInputFile(int fd, File file) {
		// HACK: crappy manual garbage collection		
		if (fd - FILE_DESCRIPTOR_EXPIRATION >= 0)
			inputFileMap.remove(fd - FILE_DESCRIPTOR_EXPIRATION);
		
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

	public String putInputString(MappableTerm asfix, String inputChars) {
		return inputCharMap.put(asfix.key, inputChars);
	}

	public IStrategoTerm putInputTerm(MappableTerm asfix, IStrategoTerm asfixIStrategoTerm) {
		return inputTermMap.put(asfix.key, asfixIStrategoTerm);
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
	
	public String getInputString(IStrategoTerm asfix) {
		return getValue(inputCharMap, asfix);
	}
	
	public IStrategoTerm getInputTerm(IStrategoTerm asfix) {
		return getValue(inputTermMap, asfix);
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
