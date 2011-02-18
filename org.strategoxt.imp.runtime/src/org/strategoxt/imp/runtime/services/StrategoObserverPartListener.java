package org.strategoxt.imp.runtime.services;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.imp.ui.DefaultPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;

/**
 * Activates the observer when an editor gets focus.
 * Basically, a kludge.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoObserverPartListener extends DefaultPartListener {
	
	private static Map<IWorkbenchPage, StrategoObserverPartListener> asyncListeners =
		new WeakHashMap<IWorkbenchPage, StrategoObserverPartListener>();
	
	private final Descriptor descriptor;

	private final EditorState editor;
	
	private StrategoObserver observer;
	
	private StrategoObserverPartListener(Descriptor descriptor, EditorState editor) {
		this.descriptor = descriptor;
		this.editor = editor;
	}
	
	public static synchronized void register(Descriptor descriptor, EditorState editor) {
		if (editor == null) return;
		
		IWorkbenchPage page = editor.getEditor().getSite().getPage();

		StrategoObserverPartListener oldListener = asyncListeners.get(page);
		if (oldListener != null) {
			page.removePartListener(oldListener);
		}
		
		StrategoObserverPartListener newListener = new StrategoObserverPartListener(descriptor, editor);
		page.addPartListener(newListener);
		asyncListeners.put(page, newListener);
	}
	
	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		if (part != editor.getEditor())
			return;
		
		try {
			if (observer == null)
				observer = descriptor.createService(StrategoObserver.class, editor.getParseController());
		} catch (BadDescriptorException e) {
			Environment.logWarning("Could not activate observer on focus", e);
		}
		
		observer.scheduleUpdate(editor.getParseController());
	}
}
