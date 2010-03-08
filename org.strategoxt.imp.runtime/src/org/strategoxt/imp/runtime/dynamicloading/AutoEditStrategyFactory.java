/**
 * 
 */
package org.strategoxt.imp.runtime.dynamicloading;

import java.util.Set;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IAutoEditStrategy;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.AutoEditStrategy;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AutoEditStrategyFactory extends AbstractServiceFactory<IAutoEditStrategy> {

	public AutoEditStrategyFactory() {
		super(IAutoEditStrategy.class, true);
	}

	@Override
	public IAutoEditStrategy create(Descriptor descriptor, SGLRParseController controller)
			throws BadDescriptorException {
		
		ILanguageSyntaxProperties syntax = descriptor.createService(ILanguageSyntaxProperties.class, controller);
		return new AutoEditStrategy(syntax);
	}
	
	/**
	 * Eagerly initializes the auto edit strategy for an editor.
	 */
	public static void eagerInit(Descriptor descriptor, IParseController controller, EditorState editor) {
		if (editor != null && controller instanceof SGLRParseController) {
			Set<IAutoEditStrategy> autoEdits = editor.getEditor().getLanguageServiceManager().getAutoEditStrategies();
			for (IAutoEditStrategy autoEdit : autoEdits) {
				if (autoEdit instanceof DynamicAutoEditStrategy) {
					DynamicAutoEditStrategy dynAutoEdit = (DynamicAutoEditStrategy) autoEdit;
					dynAutoEdit.initialize(controller);
					ISourceViewer viewer = editor.getEditor().getServiceControllerManager().getSourceViewer();
					if (viewer instanceof ITextViewerExtension) {
						((ITextViewerExtension) viewer).prependVerifyKeyListener(dynAutoEdit);
					} else {
						Environment.logException("Text viewer does not implement ITextViewerExtension interface; cannot enable auto editing");
					}
				}
			}
		}
	}

}
