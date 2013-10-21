package org.strategoxt.imp.runtime.services.menus;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.services.menus.contribs.IBuilder;

/**
 * @author Oskar van Rest
 */
public class ActionHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		EditorState editor = EditorState.getActiveEditor();
		List<String> path = toPath(event.getParameter(MenusServiceConstants.PATH_PARAM));

		IBuilder builder = MenusServiceUtil.getMenus().getBuilder(path);
		builder.scheduleExecute(editor, null, null, false);
		
		return null;
	}

	private List<String> toPath(String path) {
		List<String> result = new LinkedList<>();
		StringTokenizer tokenizer = new StringTokenizer(path.substring(1, path.length() - 1), ",");
		while (tokenizer.hasMoreTokens()) {
			result.add(tokenizer.nextToken().trim());
		}
		return result;
	}
}
