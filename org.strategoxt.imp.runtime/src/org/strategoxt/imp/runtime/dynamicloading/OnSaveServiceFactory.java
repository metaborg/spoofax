/**
 * 
 */
package org.strategoxt.imp.runtime.dynamicloading;

import static java.util.Collections.synchronizedMap;
import static org.spoofax.interpreter.core.Tools.termAt;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.findTerm;

import java.util.Map;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.WeakWeakMap;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.OnSaveService;
import org.strategoxt.imp.runtime.services.StrategoObserver;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class OnSaveServiceFactory extends AbstractServiceFactory<OnSaveService> {
	
	private static final Map<UniversalEditor, OnSaveService> registeredServices =
		synchronizedMap(new WeakWeakMap<UniversalEditor, OnSaveService>());

	public OnSaveServiceFactory() {
		super(OnSaveService.class, false);
	}

	@Override
	public OnSaveService create(Descriptor descriptor, SGLRParseController controller)
			throws BadDescriptorException {
		
		IStrategoAppl onsave = findTerm(descriptor.getDocument(), "OnSave");
		String function = null;
		if (onsave != null)
			function = TermReader.termContents(termAt(onsave, 0));
		StrategoObserver feedback = descriptor.createService(StrategoObserver.class, controller);
		return new OnSaveService(feedback, function);
	}

	public static synchronized void eagerInit(Descriptor descriptor, IParseController controller,
			EditorState editor) {
		
		try {
			if (editor != null && controller instanceof SGLRParseController) {
				OnSaveService oldService = registeredServices.get(editor.getEditor());
				editor.getEditor().removeOnSaveListener(oldService);
				OnSaveService newService = descriptor.createService(OnSaveService.class, (SGLRParseController) controller);
				newService.initialize(editor);
				registeredServices.put(editor.getEditor(), newService);
				editor.getEditor().addOnSaveListener(newService);
			}
		} catch (BadDescriptorException e) {
			Environment.logException("Could not eagerly intiialize the on save service", e);
		} catch (RuntimeException e) {
			Environment.logException("Could not eagerly intiialize the on save service", e);
		}
	}
}
