package org.strategoxt.imp.runtime.dynamicloading;

import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoObserverFactory extends AbstractServiceFactory<StrategoObserver> {
	
	public StrategoObserverFactory() {
		super(StrategoObserver.class, true);
	}
	
	@Override
	public StrategoObserver create(Descriptor descriptor, SGLRParseController controller) throws BadDescriptorException {
		// TODO: Sharing of FeedBack instances??
		//       Each file should have its own Context, I guess, but not its own HybridInterpreter
		String observerFunction = descriptor.getProperty("SemanticObserver", null);
		
		return new StrategoObserver(descriptor, observerFunction);
	}

}
