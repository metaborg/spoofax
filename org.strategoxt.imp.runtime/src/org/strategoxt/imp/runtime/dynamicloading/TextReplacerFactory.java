package org.strategoxt.imp.runtime.dynamicloading;

import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.ITextReplacer;
import org.strategoxt.imp.runtime.services.TextReplacer;

/**
 * @author Oskar van Rest
 */
public class TextReplacerFactory extends AbstractServiceFactory<ITextReplacer> {

	public TextReplacerFactory() {
		super(ITextReplacer.class, false); // not cached; depends on derived editor relation
	}

	@Override
	public ITextReplacer create(Descriptor descriptor, SGLRParseController controller) {
		return new TextReplacer(descriptor, controller);
	}
}