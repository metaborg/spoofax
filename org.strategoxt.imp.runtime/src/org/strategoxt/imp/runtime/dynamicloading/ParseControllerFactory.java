package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ParseControllerFactory extends AbstractServiceFactory<IParseController> {

	@Override
	public Class<IParseController> getCreatedType() {
		return IParseController.class;
	}

	@Override
	public IParseController create(Descriptor descriptor) throws BadDescriptorException {
		ILanguageSyntaxProperties syntaxProperties = descriptor.createService(ILanguageSyntaxProperties.class);
		return new SGLRParseController(descriptor.getLanguage(), syntaxProperties, descriptor.getStartSymbols());
	}

}
