package org.strategoxt.imp.runtime.dynamicloading;

import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoObserverFactory extends AbstractServiceFactory<StrategoObserver> {
	
	@Override
	public Class<StrategoObserver> getCreatedType() {
		return StrategoObserver.class;
	}
	
	@Override
	public StrategoObserver create(Descriptor descriptor) throws BadDescriptorException {
		// TODO: Sharing of FeedBack instances??
		String observerFunction = descriptor.getProperty("SemanticObserver", null);
		
		return new StrategoObserver(descriptor, observerFunction);
	}

}
