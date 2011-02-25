package org.strategoxt.imp.runtime;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.jface.text.IDocument;
import org.spoofax.NotImplementedException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.parser.SGLRParseController;


/**
 * Helper class for accessing a file in some language,
 * that may not necessarily be opened in an editor.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class FileState {
	
	private final Descriptor descriptor;
	
	private final IResource resource;
	
	protected FileState(Descriptor descriptor, IResource resource) {
		this.descriptor = descriptor;
		this.resource = resource;
	}
	
	protected FileState() {
		this(null, null); // TODO: remove me
	}
	
	
	public static FileState getFile(IPath path, IDocument document) {
		Language language = LanguageRegistry.findLanguage(path, document);
		Descriptor descriptor = Environment.getDescriptor(language);
		if (descriptor == null) return null;
		throw new NotImplementedException();
	}
	
	public SGLRParseController getParseController() {
		throw new NotImplementedException();
	}
}
