package org.strategoxt.imp.runtime.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * @author Oskar van Rest
 */
public class SelectionProvider implements ISelectionProvider {

	ISelection selection;
	List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	public ISelection getSelection() {
		return selection;
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	public void setSelection(ISelection selection) {
		this.selection = selection;
		SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
		for (ISelectionChangedListener listener : listeners) {
			listener.selectionChanged(e);
		}
	}
}
