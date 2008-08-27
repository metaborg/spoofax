package org.strategoxt.imp.runtime.dynamicloading;

import java.io.IOException;
import java.io.InputStream;

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
	
	public static Descriptor load(InputStream descriptor) throws BadDescriptorException {
		return load(descriptor, null);
	}
	
	public static Descriptor load(InputStream descriptor, InputStream parseTable) throws BadDescriptorException {
		Debug.startTimer();
		Descriptor result = Descriptor.load(descriptor);
		Language language = result.getLanguage();
		
		Environment.registerDescriptor(language, result);
		LanguageRegistry.registerLanguage(language);
		
		if (parseTable == null) parseTable = result.openTableStream();
		registerParseTable(language, parseTable);
		
		Debug.stopTimer("Editor service loaded: " + result.getLanguage().getName());
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
