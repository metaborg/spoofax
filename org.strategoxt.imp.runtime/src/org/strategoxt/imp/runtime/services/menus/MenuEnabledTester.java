package org.strategoxt.imp.runtime.services.menus;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.swt.widgets.Display;

/**
 * @author Oskar van Rest
 */
public class MenuEnabledTester extends PropertyTester {

	private int menuIndex = -1;

	@Override
	public synchronized boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		menuIndex = (menuIndex + 1) % MenusServiceConstants.NO_OF_TOOLBAR_MENUS;

		// Refresh toolbar menus after switching between two Spoofax editors.
		if (menuIndex == MenusServiceConstants.NO_OF_TOOLBAR_MENUS - 1) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					MenusServiceUtil.refreshToolbarMenuCommands();
				}
			});
		}
		
		MenuList menus = MenusServiceUtil.getMenus();
		return menus != null && menus.getAll().size() > menuIndex;
	}
}
