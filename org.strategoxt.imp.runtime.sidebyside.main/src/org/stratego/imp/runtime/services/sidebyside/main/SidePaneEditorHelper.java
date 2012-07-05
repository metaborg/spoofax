package org.stratego.imp.runtime.services.sidebyside.main;

import org.eclipse.ui.IEditorPart;

/**
 * 
 * @author vladvergu
 */
public abstract class SidePaneEditorHelper {
	
	abstract public void internalOpenSidePane() throws Throwable;
	
	abstract public void restoreFocus() throws Throwable;
	
	abstract public void setOpenedEditor(IEditorPart editor);
	
	abstract public void undoOpenSidePane() throws Throwable;
	
}
