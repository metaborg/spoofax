package org.strategoxt.imp.runtime.services;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.imp.ui.DefaultPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.strategoxt.imp.runtime.EditorState;

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
		EditorState editor = EditorState.getEditorFor(part);
		if (editor != null) editor.scheduleAnalysis();
	}
}
