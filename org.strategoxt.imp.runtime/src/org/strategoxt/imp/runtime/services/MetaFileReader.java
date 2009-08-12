package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.*;

import java.io.File;
import java.io.IOException;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.Environment;

/**
 * A utility class for reading .meta files.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class MetaFileReader {
	
	private MetaFileReader() {
		// No public constructor
	}
	
	/**
	 * Reads the syntax name specified in a .meta file.
	 * 
	 * If any error occurs while reading, an exception is logged and
	 * null is returned.
	 */
	public static String readSyntax(String file) {
		if (!new File(file).exists()) return null;
		
		try {
			IStrategoTerm meta = readFile(file);
			IStrategoString language = termAt(getEntry(meta, "Syntax"), 0);
			return language.stringValue();
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
	
	private static IStrategoTerm readFile(String file) throws IOException {
		return Environment.getTermFactory().parseFromFile(file);
	}
}
