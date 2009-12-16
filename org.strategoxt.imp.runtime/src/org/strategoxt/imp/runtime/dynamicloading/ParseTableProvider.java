package org.strategoxt.imp.runtime.dynamicloading;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.spoofax.jsglr.InvalidParseTableException;
import org.spoofax.jsglr.ParseTable;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;

/**
 * Lazily loads a parse table from a resource.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ParseTableProvider {
	
	private final IFile file;
	
	private ParseTable table;
	
	public ParseTableProvider(IFile file) {
		this.file = file;
	}
	
	public ParseTable get() throws InvalidParseTableException, IOException, CoreException {
		if (table != null) return table;
		Debug.startTimer();
		ParseTable table = Environment.registerParseTable(null, file.getContents(true));
		Debug.stopTimer("Parse table loaded:" + file.getName());
		return this.table = table;
	}
}
