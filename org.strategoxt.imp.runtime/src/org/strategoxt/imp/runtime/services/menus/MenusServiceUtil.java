package org.strategoxt.imp.runtime.services.menus;

import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * @author Oskar van Rest
 */
@SuppressWarnings("restriction")
public class MenusServiceUtil {

	/**
	 * Refresh label and size of toolbar menus.
	 */
	public static void refreshToolbarMenus() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow window = wb.getActiveWorkbenchWindow();

		if (window instanceof WorkbenchWindow && ((WorkbenchWindow) window).getCoolBarVisible()) {
			ICoolBarManager coolBarManager = ((WorkbenchWindow) window).getCoolBarManager2();
			ToolBarContributionItem menu = (ToolBarContributionItem) coolBarManager.find(MenusServiceConstants.TOOLBAR_ID);
			
			if (menu != null) {
				ICommandService commandService = (ICommandService) wb.getService(ICommandService.class);
				for (int i = 1; i <= MenusServiceConstants.NO_OF_TOOLBAR_MENUS; i++) {
					commandService.refreshElements(MenusServiceConstants.TOOLBAR_BASECOMMAND_ID_PREFIX + i, null);
				}
				
				menu.getToolBarManager().update(true);
				menu.setVisible(false);
				menu.setVisible(true);
			}
		}
	}
}
