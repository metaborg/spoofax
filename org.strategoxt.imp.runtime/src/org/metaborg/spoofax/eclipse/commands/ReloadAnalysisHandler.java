package org.metaborg.spoofax.eclipse.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.metaborg.runtime.task.engine.TaskManager;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.index.IndexManager;
import org.spoofax.interpreter.library.index.notification.NotificationCenter;

public class ReloadAnalysisHandler extends AbstractHandler {
    @Override public Object execute(ExecutionEvent event) throws ExecutionException {
        final IProject project = AbstractHandlerUtils.getProjectFromSelected(event);
        if(project == null)
            return null;
        final String path = project.getLocation().toPortableString();
        final IOAgent agent = new IOAgent();

        final IndexManager indexManager = IndexManager.getInstance();
        indexManager.resetIndex(path, agent);
        TaskManager.getInstance().resetTaskEngine(path, agent);
        NotificationCenter.notifyNewProject(indexManager.getProjectURI(path, agent));

        return null;
    }
}
