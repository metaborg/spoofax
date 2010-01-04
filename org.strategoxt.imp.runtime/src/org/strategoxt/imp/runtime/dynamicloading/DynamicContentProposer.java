package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IContentProposer;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicContentProposer extends AbstractService<IContentProposer> implements IContentProposer {

	public DynamicContentProposer() {
		super(IContentProposer.class);
	}

	public ICompletionProposal[] getContentProposals(IParseController controller, int offset, ITextViewer viewer) {
		initialize(controller);
		return getWrapped().getContentProposals(controller, offset, viewer);
	}

}
