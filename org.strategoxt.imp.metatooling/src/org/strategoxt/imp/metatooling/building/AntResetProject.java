package org.strategoxt.imp.metatooling.building;

import org.metaborg.runtime.task.TaskEngine;
import org.metaborg.runtime.task.TaskManager;
import org.spoofax.interpreter.library.index.IIndex;
import org.spoofax.interpreter.library.index.IndexManager;
import org.spoofax.interpreter.library.index.NotificationCenter;

public class AntResetProject {
	public static void main(String[] args) {
		final String projectPath = args[0];

		final TaskManager taskManager = TaskManager.getInstance();
		final TaskEngine taskEngine = taskManager.getTaskEngine(projectPath);
		if(taskEngine != null)
			taskEngine.reset();

		final IndexManager indexManager = IndexManager.getInstance();
		final IIndex index = indexManager.getIndex(args[0]);
		if(index != null)
			index.clearAll();

		NotificationCenter.notifyNewProject(indexManager.getProjectURIFromAbsolute(projectPath));
	}
}
