package org.strategoxt.imp.runtime.stratego;

import java.io.File;
import java.io.IOException;

import org.strategoxt.imp.runtime.Environment;
import static org.spoofax.jsglr.Term.*;

import aterm.ATerm;
import aterm.ATermAppl;

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
		ATerm term = Environment.getATermFactory().readFromFile(file);
		
		try {
			ATermAppl appl = asAppl(term);
			
		} catch (ClassCastException e) {
			throw new BadMetaFileException(file);
		}
		
		throw new BadMetaFileException(file);
	}
}
