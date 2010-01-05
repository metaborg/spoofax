package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.services.ILabelProvider;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.LabelProvider;


/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class LabelProviderFactory extends AbstractServiceFactory<ILabelProvider> {

	public LabelProviderFactory() {
		super(ILabelProvider.class);
	}
	
	@Override
	public ILabelProvider create(Descriptor descriptor, SGLRParseController controller)
			throws BadDescriptorException {
		
		return new LabelProvider();
	}


}
