package org.strategoxt.imp.runtime.dynamicloading;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.CustomDisambiguator;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ParseControllerFactory extends AbstractServiceFactory<IParseController> {

	public ParseControllerFactory() {
		super(IParseController.class);
	}

	@Override
	public IParseController create(Descriptor descriptor, SGLRParseController controller) throws BadDescriptorException {
		ILanguageSyntaxProperties syntaxProperties = descriptor.createService(ILanguageSyntaxProperties.class, controller);
		Language language = descriptor.getLanguage();
		ParseTableProvider table;
		try {
			table = Environment.getParseTableProvider(language);
		} catch (InvalidParseTableException e) {
			throw new BadDescriptorException("Could not load parse table for " + language.getName(), e);
		} catch (IOException e) {
			throw new BadDescriptorException("Could not load parse table for " + language.getName(), e);
		} catch (CoreException e) {
			throw new BadDescriptorException("Could not load parse table for " + language.getName(), e);
		} catch (RuntimeException e) {
			throw new BadDescriptorException("Could not load parse table for " + language.getName(), e);
		}
		SGLRParseController result = new SGLRParseController(language, table, syntaxProperties, descriptor.getStartSymbol());
		result.getParser().setCustomDisambiguator(new CustomDisambiguator(result, descriptor.getProperties("Disambiguator")));
		if (table.isDynamic())
			table.setController(result);
		return result;
	}

}
