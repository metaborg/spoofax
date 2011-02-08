package org.strategoxt.imp.runtime.dynamicloading;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr.client.ParseTable;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * Lazily loads a parse table from a resource.
 * 
 * ParseTableProvider instances are shared among multiple editors
 * to ensure caching and reuse of their table. This means
 * they do not support the {@link DynamicParseTableProvider#initialize}
 * and {@link DynamicParseTableProvider#setTable} methods.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ParseTableProvider {
	
	private final IFile file;
	
	private final Descriptor descriptor;
	
	private ParseTable table;
	
	public ParseTableProvider(IFile file) {
		this.file = file;
		this.descriptor = null;
	}
	
	public ParseTableProvider(Descriptor descriptor) {
		this.descriptor = descriptor;
		this.file = null;
	}
	
	public ParseTableProvider(ParseTable table) {
		this.table = table;
		this.descriptor = null;
		this.file = null;
	}
	
	public ParseTable get() throws BadDescriptorException, InvalidParseTableException, IOException, CoreException {
		if (table != null) return table;
		Debug.startTimer();
		InputStream stream = file == null ? descriptor.openParseTableStream() : file.getContents(true);
		ParseTable table = Environment.loadParseTable(stream);
		Debug.stopTimer("Loaded parse table for " + (file == null ? descriptor.getLanguage().getName() : file.getName()));
		return this.table = table;
	}
	
	protected Descriptor getDescriptor() {
		return descriptor;
	}
	
	protected void setTable(ParseTable table) {
		this.table = table;
	}
	
	/**
	 * @see DynamicParseTableProvider
	 */
	public boolean isDynamic() {
		return false;
	}
	
	public void setController(SGLRParseController controller) {
		throw new UnsupportedOperationException();
	}
	
	public void initialize(File input) {
		throw new UnsupportedOperationException();
	}
}
