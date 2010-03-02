/**
 * 
 */
package org.strategoxt.imp.runtime.dynamicloading;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.strategoxt.imp.runtime.services.AutoEditStrategy;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class DynamicAutoEditStrategy extends AbstractService<IAutoEditStrategy>
		implements IAutoEditStrategy, VerifyKeyListener {

	public DynamicAutoEditStrategy() {
		super(IAutoEditStrategy.class);
	}

	public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
		getWrapped().customizeDocumentCommand(document, command);
	}

	public void verifyKey(VerifyEvent event) {
		IAutoEditStrategy wrapped = getWrapped();
		if (wrapped instanceof VerifyKeyListener)
			((VerifyKeyListener) wrapped).verifyKey(event);
	}
	
	@Override
	public void initialize(IParseController controller) {
		super.initialize(controller);
		IAutoEditStrategy wrapped = getWrapped();
		if (wrapped instanceof AutoEditStrategy)
			((AutoEditStrategy) wrapped).initialize(controller);
	}

}
