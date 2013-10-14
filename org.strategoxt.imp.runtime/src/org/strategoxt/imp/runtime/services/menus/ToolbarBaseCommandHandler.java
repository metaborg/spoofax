package org.strategoxt.imp.runtime.services.menus;

import java.util.Map;
import java.util.Random;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

/**
 * @author Oskar van Rest
 */
public class ToolbarBaseCommandHandler implements IHandler, IElementUpdater {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String menuID = event.getParameter(MenusServiceConstants.MENU_ID_PARAM_ID);
		System.out.println(menuID);
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
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		String menuID = (String) parameters.get(MenusServiceConstants.MENU_ID_PARAM_ID);
		element.setText(new Random().nextInt(1000) + menuID);
		element.setTooltip("");
	}

}
