package org.strategoxt.imp.runtime.services;

import java.io.File;
import java.io.IOException;

import org.spoofax.NotImplementedException;
import org.strategoxt.imp.runtime.Environment;

public class MetaFileReader {
	public static String tryReadSyntax(String file) {
		if (!new File(file).exists()) return null;
		
		try {
			return readSyntax(file);
		} catch (IOException e) {
			return null;
		} catch (BadMetaFileException e) {
			return null;
		}
	}
	
	public static String readSyntax(String file) throws IOException, BadMetaFileException {
		// IStrategoTerm term =
			Environment.getTermFactory().parseFromFile(file);
		
		try {
			// TODO: .meta file reading
			// ATermAppl appl =
			throw new NotImplementedException();
			
		} catch (ClassCastException e) {
			throw new BadMetaFileException(file);
		}
	}
}
