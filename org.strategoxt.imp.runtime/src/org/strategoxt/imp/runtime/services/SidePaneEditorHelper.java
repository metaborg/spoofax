package org.strategoxt.imp.runtime.services;

import java.lang.ref.WeakReference;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.EditorAreaHelper;
import org.eclipse.ui.internal.EditorPane;
import org.eclipse.ui.internal.EditorSashContainer;
import org.eclipse.ui.internal.EditorStack;
import org.eclipse.ui.internal.WorkbenchPage;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;

/**
 * A helper class for opening editors side-by-side.
 * 
 * Uses the org.eclipse.ui.internal.* API, so the class should be used defensively.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
@SuppressWarnings("restriction")
public class SidePaneEditorHelper {
	
	private static EditorStack previousNewStack;
	
	private static WeakReference<IEditorPart> previousNewEditor;
	
	private EditorStack oldStack;
	
	private EditorStack newStack;
	
	private EditorSashContainer layoutPart;
	
	private boolean isEmptyPane;
	
	private SidePaneEditorHelper() {
		// Private
	}
	
	public static SidePaneEditorHelper openSidePane() throws Throwable {
		SidePaneEditorHelper cookie = new SidePaneEditorHelper();
		cookie.internalOpenSidePane();
		return cookie;
	}
	
	public void internalOpenSidePane() throws Throwable {
		if (!EditorState.isUIThread())
			throw new IllegalStateException("Must be called from the UI thread");
		
		IWorkbenchPage page =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		EditorAreaHelper editorArea = ((WorkbenchPage) page).getEditorPresentation();
		
		// Find an existing stack next to the active editor
		oldStack = editorArea.getActiveWorkbook();
		newStack = findReusableStack(editorArea);
		
		if (newStack == null) {
			// Create a new stack
			layoutPart = (EditorSashContainer) editorArea.getLayoutPart();
			newStack = EditorStack.newEditorWorkbook(layoutPart, (WorkbenchPage) page);
			oldStack.copyAppearanceProperties(newStack);
			layoutPart.add(newStack);
			isEmptyPane = true;
		} else {
			isEmptyPane = false;
		}
		
		newStack.setFocus();
		previousNewEditor = null;
	}

	private EditorStack findReusableStack(EditorAreaHelper editorArea) {
		EditorStack result = null;
		boolean foundGoodMatch = false;
		IEditorPart previousNewEditor =
			SidePaneEditorHelper.previousNewEditor == null
					? null
					: SidePaneEditorHelper.previousNewEditor.get();
		
		for (Object stackObject : editorArea.getWorkbooks()) {
			if (!(stackObject instanceof EditorStack))
				continue;
			EditorStack stack = (EditorStack) stackObject;
			if (stack != oldStack) { // good, other stack
				if (previousNewEditor != null && contains(stack.getEditors(), previousNewEditor)) {
					return stack;
				} else if (stack == previousNewStack) {
					result = stack;
					foundGoodMatch = true;
				} else if (!foundGoodMatch && stack.getParent() == oldStack.getParent()) {
					result = stack;
					foundGoodMatch = true;
				} else if (!foundGoodMatch) {
					result = stack;
				}
			}
		}
		
		return result;
	}
	
	private static boolean contains(EditorPane[] array, IEditorPart member) {
		for (int i = 0; i < array.length; i++) {
			IEditorReference ref = array[i].getEditorReference();
			if (ref.getEditor(false) == member) return true;
		}
		return false;
	}
	
	public void restoreFocus() {
		try {
			previousNewStack = newStack;
			oldStack.setFocus();
		} catch (Throwable t) {
			Environment.logException("Could not restore focus from side pane", t);			
		}
	}
	
	public void setOpenedEditor(IEditorPart editor) {
		previousNewEditor = editor == null ? null : new WeakReference<IEditorPart>(editor);
	}
	
	public void undoOpenSidePane() {
		try {
			if (isEmptyPane)
				layoutPart.remove(newStack);
		} catch (Throwable t) {
			Environment.logException("Could not close side pane", t);			
		}
	}
}
