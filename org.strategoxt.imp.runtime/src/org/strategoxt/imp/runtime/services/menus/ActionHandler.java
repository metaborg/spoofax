package org.strategoxt.imp.runtime.services.menus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.RuntimeActivator;
import org.strategoxt.imp.runtime.parser.ParseErrorHandler;
import org.strategoxt.imp.runtime.services.StrategoObserver;
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
		
		boolean runBuilderWithErrors = false;
		
		if (runBuilderWithErrors){
			builder.scheduleExecute(editor, null, null, false);
			ToolbarBaseCommandHandler.setLastAction(path.get(0), path);
		}
		else{
			//check for errors in the editor and show them as a pop-up before running the builder
			List<String> messages = getErrorMessages(editor);
			if (messages.isEmpty()){
				builder.scheduleExecute(editor, null, null, false);
				ToolbarBaseCommandHandler.setLastAction(path.get(0), path);
			}
			else{
				//show a pop-up
				MultiStatus info = new MultiStatus(RuntimeActivator.PLUGIN_ID, IStatus.ERROR, "There are error markers in the file.", null);
				for (String m : messages){
					info.add(new Status(IStatus.ERROR, RuntimeActivator.PLUGIN_ID, 1, m, null));
				}
				
				ErrorDialog ed = new ErrorDialog(null, "Problem Occurred", null, info, IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
				ed.open();
				
				
			//	ErrorDialog.openError(null, "Problem Occurred", null, info);
								
				return null;
			}
		}
		
		return null;
	}
	
	private List<String> getErrorMessages(EditorState editor){
		List<String> messages = new ArrayList<String>();
		
		
		//check for (Stratego) semantic errors
		try{
			StrategoObserver observer = editor.getDescriptor().createService(StrategoObserver.class, editor.getParseController());
			for (IMarker marker : observer.getMessages().getCurrentBatch().getAsyncActiveMarkers()){
				if ((int) marker.getAttribute(IMarker.SEVERITY) == IMarker.SEVERITY_ERROR)
					messages.add("Line " + (Integer) marker.getAttribute(IMarker.LINE_NUMBER) + " : " + (String) marker.getAttribute(IMarker.MESSAGE));
			}
			
			//check for syntactic errors
			ParseErrorHandler parseHandler = editor.getParseController().getErrorHandler();
			for (IMarker marker : parseHandler.getAstMessageHandler().getCurrentBatch().getAsyncActiveMarkers()){
				if ((int) marker.getAttribute(IMarker.SEVERITY) == IMarker.SEVERITY_ERROR)
				messages.add("Line " + (Integer) marker.getAttribute(IMarker.LINE_NUMBER) + " : " + (String) marker.getAttribute(IMarker.MESSAGE));
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		
		
		return messages;
	}

	private List<String> deserializePath(String path) {
		String[] result = path.split("\\+\\+");
		for (int i=0; i<result.length; i++) {
			result[i] = result[i].replace("+\\", "+");
		}
		return Arrays.asList(result);
	}
}
