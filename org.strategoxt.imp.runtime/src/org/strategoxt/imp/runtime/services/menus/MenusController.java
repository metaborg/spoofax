package org.strategoxt.imp.runtime.services.menus;

import java.util.HashMap;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IServiceLocator;

/**
 * @author Oskar van Rest
 */
public class MenusController {

	public MenusController INSTANCE = new MenusController();

	private HashMap<String, AbstractContributionFactory> menuContributionFactories = new HashMap<String, AbstractContributionFactory>();

	private MenusController() {
	}

	public synchronized void createMenuContributionFactory(String locationURI) {
		if (!menuContributionFactories.containsKey(locationURI)) {

			AbstractContributionFactory menuContributionFactory = new AbstractContributionFactory(locationURI, null) {
				@Override
				public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
					/*
					 * TODO
					 * 
					 * editor = getActiveEditor()
					 * 
					 * if (editor != null && editor instanceof SpoofaxEditor)
					 *   SpoofaxMenusService sms = get SpoofaxMenusService for editor
					 *   for (menu : sms)
					 *     for (category : menu)
					 *       for (command : category)
					 *         additions.add(command)
					 *       additions.add(new Separator())
					 */
				}
			};

			menuContributionFactories.put(locationURI, menuContributionFactory);
			IMenuService menuService = (IMenuService) PlatformUI.getWorkbench().getService(IMenuService.class);
			menuService.addContributionFactory(menuContributionFactory);
		}
	}
}
