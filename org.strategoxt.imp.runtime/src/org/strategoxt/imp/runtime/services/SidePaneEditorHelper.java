package org.strategoxt.imp.runtime.services;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.EditorAreaHelper;
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
class SidePaneEditorHelper {
	
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
		assert EditorState.isUIThread();
		IWorkbenchPage page =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		EditorAreaHelper editorArea = ((WorkbenchPage) page).getEditorPresentation();
		
		// Find an existing stack next to the active editor
		oldStack = editorArea.getActiveWorkbook();
		newStack = null;
		for (Object stack : editorArea.getWorkbooks()) {
			if (stack != oldStack) { // good, other stack
				newStack = (EditorStack) stack;
				if (newStack.getParent() == oldStack.getParent())
					break; // just perfect!
			}
		}
		
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
	}
	
	public void restoreFocus() {
		try {
			oldStack.setFocus();
		} catch (Throwable t) {
			Environment.logException("Could not close side pane", t);			
		}
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
