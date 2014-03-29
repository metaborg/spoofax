package org.strategoxt.imp.runtime.services.menus;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.services.menus.model.IBuilder;
import org.strategoxt.imp.runtime.services.menus.model.IMenuContribution;
import org.strategoxt.imp.runtime.services.menus.model.Menu;

/**
 * @author Oskar van Rest
 */
public class ToolbarBaseCommandHandler implements IHandler, IElementUpdater {

	private static Map<String, List<String>> lastActions = new WeakHashMap<String, List<String>>(MenusServiceConstants.NO_OF_TOOLBAR_MENUS);

	public static void setLastAction(String menuCaption, List<String> pathOfLastAction) {
		lastActions.put(menuCaption, pathOfLastAction);
	}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		int menuIndex = Integer.parseInt((String) event.getParameter(MenusServiceConstants.MENU_ID_PARAM));
		MenuList menus = MenusServiceUtil.getMenus();

		IBuilder builder = null;

		if (menus.getAll().size() > menuIndex) {
			Menu menu = menus.getAll().get(menuIndex);
			List<String> lastAction = lastActions.get(menu.getCaption());
			if (lastAction != null) {
				builder = menus.getBuilder(lastActions.get(menu.getCaption()));
			}
		}

		if (builder == null) {
			builder = getSomeAction(menus.getAll().get(menuIndex));
		}

		if (builder != null) {
			builder.scheduleExecute(EditorState.getActiveEditor(), null, null, false);
		}

		return null;
	}

	private IBuilder getSomeAction(Menu menu) {
		for (IMenuContribution contrib : menu.getMenuContributions()) {
			switch (contrib.getContributionType()) {
			case IMenuContribution.BUILDER:
				return (IBuilder) contrib;

			case IMenuContribution.MENU:
				IBuilder action = getSomeAction((Menu) contrib);
				if (action != null) {
					return action;
				} else {
					break;
				}
			default:
				break;
			}
		}

		return null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void updateElement(final UIElement element, @SuppressWarnings("rawtypes") final Map parameters) {

		final int menuIndex = Integer.parseInt((String) parameters.get(MenusServiceConstants.MENU_ID_PARAM));
		final MenuList menus = MenusServiceUtil.getMenus();
		
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {

				if (menus.getAll().size() > menuIndex) {
					Menu menu = menus.getAll().get(menuIndex);
					String caption = menu.getCaption();

					element.setText(caption);
					element.setTooltip("");
					element.setIcon(menu.getIcon());

					MenusServiceUtil.refreshToolbarMenus();
				}
			}
		});
	}
}
