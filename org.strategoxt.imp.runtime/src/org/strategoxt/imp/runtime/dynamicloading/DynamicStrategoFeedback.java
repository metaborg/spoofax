package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.strategoxt.imp.runtime.services.StrategoFeedback;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicStrategoFeedback extends DynamicService<StrategoFeedback> implements IModelListener {

	public DynamicStrategoFeedback() {
		super(StrategoFeedback.class);
	}

	public AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.SYNTACTIC_ANALYSIS;
	}

	public void update(IParseController parseController, IProgressMonitor monitor) {
		initialize(parseController.getLanguage());
		getWrapped().update(parseController, monitor);
	}

}
