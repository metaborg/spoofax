package org.strategoxt.imp.runtime.services.menus;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.strategoxt.imp.runtime.editor.SpoofaxEditor;

public class MenuEnabledTester extends PropertyTester {

	private IWorkbenchPart activePart;
	private double time;

	@Override
	public synchronized boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

		boolean result = false;
		
		if (property.equals("menuEnabled")) {

			IWorkbenchPart lastActivePart = activePart;
			activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
			
			double newTime = System.currentTimeMillis();
			if (newTime - time > 100) {
				time = newTime;
				if (lastActivePart instanceof SpoofaxEditor) {
					return false; // refresh
				}
			}
			
			return activePart instanceof SpoofaxEditor;			
		}
		
		return result;
	}
}
