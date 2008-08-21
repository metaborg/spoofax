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
public class LanguageLoader {
	private LanguageLoader() {}
	
	public static Language load(InputStream file, boolean readTableFromFile) throws BadDescriptorException {
		Debug.startTimer();
		Descriptor descriptor = Descriptor.load(file);
		Language language = descriptor.toLanguage();
		
		Environment.registerDescriptor(language, descriptor);
		LanguageRegistry.registerLanguage(language);
		
		if (readTableFromFile) registerParseTable(descriptor, language);
		
		Debug.stopTimer("Editor service loaded: " + descriptor.getName());
		return language;
	}

	private static void registerParseTable(Descriptor descriptor, Language language) throws BadDescriptorException {
		try {
			Environment.registerParseTable(language, descriptor.getTableStream());
		} catch (IOException e) {
			throw new BadDescriptorException(e);
		} catch (InvalidParseTableException e) {
			throw new BadDescriptorException(e);
		}
	}
}
