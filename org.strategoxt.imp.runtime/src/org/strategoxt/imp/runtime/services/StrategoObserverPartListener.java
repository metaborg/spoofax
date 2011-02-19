package org.strategoxt.imp.runtime.services;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.ui.DefaultPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.dynamicloading.DynamicParseController;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * Activates the observer when an editor gets focus.
 * Basically, a kludge.
 * 
 * Tries to install one listener per part.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoObserverPartListener extends DefaultPartListener {
	
	private static Map<IWorkbenchPage, StrategoObserverPartListener> asyncListeners =
		new WeakHashMap<IWorkbenchPage, StrategoObserverPartListener>();
	
	private StrategoObserverPartListener() {
		// Private constructor
	}
	
	public static synchronized void register(EditorState editor) {
		if (editor == null) return;
		
		IWorkbenchPage page = editor.getEditor().getSite().getPage();

		StrategoObserverPartListener oldListener = asyncListeners.get(page);
		if (oldListener != null) {
			page.removePartListener(oldListener);
		}
		
		StrategoObserverPartListener newListener = new StrategoObserverPartListener();
		page.addPartListener(newListener);
		asyncListeners.put(page, newListener);
	}
	
	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		if (part instanceof UniversalEditor) {
			try {
				UniversalEditor editor = (UniversalEditor) part;
				IParseController controller = editor.getParseController();
				if (controller instanceof DynamicParseController) {
					SGLRParseController sglr = (SGLRParseController) ((DynamicParseController) controller).getWrapped();
					Descriptor descriptor = Environment.getDescriptor(editor.fLanguage);
					StrategoObserver observer = descriptor.createService(StrategoObserver.class, sglr);
					observer.scheduleUpdate(sglr);
				}
			} catch (BadDescriptorException e) {
				Environment.logWarning("Could not activate observer on focus", e);
			} catch (RuntimeException e) {
				Environment.logWarning("Could not activate observer on focus", e);
			}
		}
	}
}
