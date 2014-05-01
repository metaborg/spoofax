package org.strategoxt.imp.runtime.services.menus;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.services.menus.model.IBuilder;

/**
 * @author Oskar van Rest
 */
public class ActionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		EditorState editor = EditorState.getActiveEditor();
		List<String> path = deserializePath(event.getParameter(MenusServiceConstants.PATH_PARAM));

		IBuilder builder = MenusServiceUtil.getMenus().getBuilder(path);
		builder.scheduleExecute(editor, null, null, false);

		ToolbarBaseCommandHandler.setLastAction(path.get(0), path);
		
		return null;
	}

	private List<String> deserializePath(String path) {
		String[] result = path.split("\\+\\+");
		for (int i=0; i<result.length; i++) {
			result[i] = result[i].replace("+\\", "+");
		}
		return Arrays.asList(result);
	}
}
