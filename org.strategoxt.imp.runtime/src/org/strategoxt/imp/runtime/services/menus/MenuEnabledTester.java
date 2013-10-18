package org.strategoxt.imp.runtime.services.menus;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.strategoxt.imp.runtime.editor.SpoofaxEditor;

public class MenuEnabledTester extends PropertyTester {

	private IWorkbenchPart activePart;
	private double time;
	private boolean result;
	private int menuIndex = -1;

	@Override
	public synchronized boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		
		if (property.equals("menuEnabled")) {

			menuIndex = (menuIndex + 1) % MenusServiceConstants.NO_OF_TOOLBAR_MENUS;
			if (menuIndex == 0) {
				result = false;
			}
			else {
				return result;
			}
			
			IWorkbenchPart lastActivePart = activePart;
			activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
			
			double newTime = System.currentTimeMillis();
			if (newTime - time > 50) { // hack: hide and show menu to trigger refresh of size and label 
				time = newTime;
				if (lastActivePart instanceof SpoofaxEditor) {
					return false; // refresh
				}
			}
			
			result = activePart instanceof SpoofaxEditor;
			return result;
		}
		
		return false;
	}
}
