package org.strategoxt.imp.runtime.services;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.imp.ui.DefaultPartListener;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.strategoxt.imp.runtime.EditorState;

/**
 * Activates the observer when an editor gets focus and changes in another editor have been made.
 * Basically, a kludge.
 * 
 * Tries to install one listener per part.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoObserverPartListener extends DefaultPartListener implements IDocumentListener {
	
	private static Map<IWorkbenchPage, StrategoObserverPartListener> asyncListeners =
		new WeakHashMap<IWorkbenchPage, StrategoObserverPartListener>();
	private static Map<IWorkbenchPart, Boolean> changed = new WeakHashMap<IWorkbenchPart, Boolean>();
	
	private StrategoObserverPartListener() {
		// Private constructor
	}
	
	public static synchronized void register(EditorState editor) {
		if (editor == null) return;
		
		IWorkbenchPage page = editor.getEditor().getSite().getPage();

		StrategoObserverPartListener oldListener = asyncListeners.get(page);
		if (oldListener != null) {
			page.removePartListener(oldListener);
			editor.getDocument().removeDocumentListener(oldListener);
		}
		
		StrategoObserverPartListener newListener = new StrategoObserverPartListener();
		page.addPartListener(newListener);
		editor.getDocument().addDocumentListener(newListener);
		asyncListeners.put(page, newListener);
	}
	
	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		EditorState editor = EditorState.getEditorFor(part);
		if (editor != null) {
			Boolean change = changed.get(part);
			if(change == null || change == true) {
				editor.scheduleAnalysis();
				changed.put(part, false);
			}
		}
	}

	public void documentChanged(DocumentEvent evt) {
		for(IWorkbenchPart part : changed.keySet()) {
			changed.put(part, true);
		}
	}
	
	public void documentAboutToBeChanged(DocumentEvent evt) {
		// Not required.
	}
}
