package org.strategoxt.imp.runtime.dynamicloading;

import org.strategoxt.imp.runtime.services.StrategoFeedback;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoFeedbackFactory extends AbstractServiceFactory<StrategoFeedback> {
	
	@Override
	public Class<StrategoFeedback> getCreatedType() {
		return StrategoFeedback.class;
	}
	
	@Override
	public StrategoFeedback create(Descriptor descriptor) throws BadDescriptorException {
		// TODO: Sharing of FeedBack instances??
		String observerFunction = descriptor.getProperty("SemanticObserver", null);
		
		return new StrategoFeedback(descriptor, observerFunction);
	}

}
