package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.spoofax.interpreter.terms.IStrategoTerm.APPL;

import java.io.File;
import java.io.IOException;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.io.binary.TermReader;
import org.strategoxt.imp.runtime.Environment;

/**
 * A utility class for reading .meta files.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class MetaFile {
	
	private final String language;
	
	private final boolean heuristicFilters;
	
	private MetaFile(String language, boolean heuristicFilters) {
		this.language = language;
		this.heuristicFilters = heuristicFilters;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public boolean isHeuristicFiltersEnabled() {
		return heuristicFilters;
	}
	
	/**
	 * Reads the syntax name specified in a .meta file.
	 * 
	 * If any error occurs while reading, an exception is logged and
	 * null is returned.
	 */
	public static MetaFile read(String file) {
		if (!new File(file).exists()) return null;
		
		try {
			IStrategoTerm meta = readFile(file);
			IStrategoString language = termAt(getEntry(meta, "Syntax"), 0);
			IStrategoAppl filtersTerm = getEntry(meta, "HeuristicFilters");
			boolean heuristicFilters = filtersTerm == null || isOnAppl(termAt(filtersTerm, 0)); 
			return new MetaFile(language.stringValue(), heuristicFilters);
		} catch (IOException e) {
			Environment.logException("Error reading " + file, e);
			return null;
		} catch (RuntimeException e) {
			Environment.logException("Error reading " + file, e);
			return null;
		}
	}

	private static IStrategoAppl getEntry(IStrategoTerm meta, String entryName) {
		IStrategoList entries = termAt(meta, 0);
		for (IStrategoTerm entry : entries.getAllSubterms()) {
			String cons = ((IStrategoAppl) entry).getConstructor().getName();
			if (cons.equals(entryName))
				return (IStrategoAppl) entry;
		}
		return null;
	}
	
	private static boolean isOnAppl(IStrategoTerm term) {
		return term.getTermType() == APPL && "ON".equals(((IStrategoAppl) term).getConstructor().getName());
	}
	
	private static IStrategoTerm readFile(String file) throws IOException {
		return new TermReader(Environment.getTermFactory()).parseFromFile(file);
	}
}
