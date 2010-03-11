package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicStrategoFeedback extends AbstractService<StrategoObserver> implements IModelListener {

	public DynamicStrategoFeedback() {
		super(StrategoObserver.class);
	}

	public AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.SYNTACTIC_ANALYSIS;
	}

	public void update(IParseController parseController, IProgressMonitor monitor) {
		initialize(parseController);
		getWrapped().update(parseController, monitor);
	}
	
	@Override
	public void reinitialize(Descriptor newDescriptor) throws BadDescriptorException {
		if (isInitialized())
			getWrapped().uninitialize();
		super.reinitialize(newDescriptor);
	}

}
