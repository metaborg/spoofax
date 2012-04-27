package org.strategoxt.imp.runtime.dynamicloading;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.findTerm;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.ILabelProvider;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.LabelProvider;
import org.strategoxt.imp.runtime.services.StrategoObserver;


/**
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Guido Wachsmuth <G.H.Wachsmuth add tudelft.nl>
 */
public class LabelProviderFactory extends AbstractServiceFactory<ILabelProvider> {

	public LabelProviderFactory() {
		super(ILabelProvider.class, true);
	}
	
	@Override
	public ILabelProvider create(Descriptor descriptor, SGLRParseController controller)
			throws BadDescriptorException {
		
		// try to find label providing strategy in editor description
		IStrategoAppl provider = findTerm(descriptor.getDocument(), "LabelProvider");
		String function = null;
		if (provider != null)
			function = TermReader.termContents(termAt(provider, 0));
		StrategoObserver feedback = descriptor.createService(StrategoObserver.class, controller);
		return new LabelProvider(feedback, function);
	}

	public static void eagerInit(Descriptor descriptor, IParseController controller, EditorState editor) {
		
		try {
			if (editor != null && controller instanceof SGLRParseController) {
				org.eclipse.jface.viewers.ILabelProvider provider = editor.getEditor().getLanguageServiceManager().getLabelProvider();
				if (provider instanceof DynamicLabelProvider) {
					DynamicLabelProvider dynProvider = (DynamicLabelProvider) provider;
					dynProvider.initialize(controller);
				}
			}
		} catch (RuntimeException e) {
			Environment.logException("Could not eagerly intiialize the label provider service", e);
		}
	}

}
