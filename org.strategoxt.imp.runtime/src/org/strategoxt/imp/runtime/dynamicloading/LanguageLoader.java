package org.strategoxt.imp.runtime.dynamicloading;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.language.ServiceFactory;
import org.spoofax.jsglr.InvalidParseTableException;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.TokenColorer;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class LanguageLoader {
	private LanguageLoader() {}
	
	public static Language register(InputStream file, boolean readTableFromFile) throws BadDescriptorException {
		Debug.startTimer();
		Descriptor descriptor = Descriptor.load(file);
		Language language = descriptor.toLanguage();
		
		if (readTableFromFile) registerParseTable(descriptor, language);
		
		registerServices(descriptor, language);
		
		Debug.stopTimer("Editor service loaded");
		return language;
	}

	private static void registerServices(Descriptor descriptor, Language language)
			throws BadDescriptorException {
		
		DynamicParseController proxy = (DynamicParseController) ServiceFactory.getInstance().getParseController(language);
		proxy.setWrapped(new SGLRParseController(language, descriptor.getStartSymbol()));
		
		TokenColorer colorer = (TokenColorer) ServiceFactory.getInstance().getTokenColorer(language);
		descriptor.configureColorer(colorer);
	}

	private static Language registerParseTable(Descriptor descriptor, Language language) throws BadDescriptorException {
		
		try {
			Environment.registerParseTable(language.getName(), descriptor.getTableStream());
		} catch (IOException e) {
			throw new BadDescriptorException(e);
		} catch (InvalidParseTableException e) {
			throw new BadDescriptorException(e);
		}
		
		LanguageRegistry.registerLanguage(language);
		return language;
	}
}
