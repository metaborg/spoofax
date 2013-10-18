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
		int menuIndex = Integer.parseInt((String) parameters.get(MenusServiceConstants.MENU_ID_PARAM_ID));
		int a = new Random().nextInt(10);
		int b = new Random().nextInt(10);
		int c = new Random().nextInt(10);
		String a2 = a > 5? "": "" + a;
		String b2 = a > 5? "": "" + b;
		String c2 = a > 5? "": "" + c;
		
		element.setText("menu " + a2 + b2 + c2);
		element.setTooltip("");
				
//		EditorState editor = EditorState.getActiveEditor();
//
//		if (editor != null) {
//			MenusService menusService = null;
//			try {
//				menusService = editor.getDescriptor().createService(MenusService.class, editor.getParseController());
//				enabled = true; // TODO
//			} catch (BadDescriptorException e) {
//				e.printStackTrace();
//			}
//		}
	}
}
