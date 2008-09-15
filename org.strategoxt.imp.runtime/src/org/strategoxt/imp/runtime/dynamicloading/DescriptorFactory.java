package org.strategoxt.imp.runtime.dynamicloading;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.spoofax.jsglr.InvalidParseTableException;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DescriptorFactory {
	private DescriptorFactory() {}
	
	public static Descriptor load(IFile descriptor) throws CoreException, BadDescriptorException, IOException {
		IPath basePath = descriptor.getLocation();
		basePath = basePath.removeLastSegments(2); // strip off /bin/filename
		Debug.log("Loading editor services for ", descriptor.getName());
		return load(descriptor.getContents(), null, basePath);
	}
	
	/**
	 * Creates a new {@link Descriptor} instance.
	 * 
	 * @param descriptor  The descriptor stream to load
	 * @param parseTable  An associated parse table stream, or null
	 * @param basePath    A relative path of the descriptor, or null
	 *
	 * @throws BadDescriptorException
	 */
	public static Descriptor load(InputStream descriptor, InputStream parseTable, IPath basePath)
			throws BadDescriptorException, IOException {
		
		Debug.startTimer();
		Descriptor result = Descriptor.load(descriptor);
		result.setBasePath(basePath);
		Language language = result.getLanguage();
		
		Environment.registerDescriptor(language, result);
		LanguageRegistry.registerLanguage(language);
		
		// TODO: Lazily load parse tables
		if (parseTable == null) parseTable = result.openTableStream();
		registerParseTable(language, parseTable);
		
		Debug.stopTimer("Editor services loaded: " + result.getLanguage().getName());
		return result;
	}

	private static void registerParseTable(Language language, InputStream table) throws BadDescriptorException {
		try {
			Environment.registerParseTable(language, table);
		} catch (IOException e) {
			throw new BadDescriptorException("Could not load editor service parse table", e);
		} catch (InvalidParseTableException e) {
			throw new BadDescriptorException("Could not load editor service parse table", e);
		}
	}
}
