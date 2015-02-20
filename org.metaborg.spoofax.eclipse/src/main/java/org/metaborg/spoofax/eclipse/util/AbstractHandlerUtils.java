package org.metaborg.spoofax.eclipse.util;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Utility functions for {@link AbstractHandler}.
 */
public final class AbstractHandlerUtils {
    /**
     * Attempts to retrieve the project from the selection in given execution event.
     * 
     * @param event
     *            Execution event.
     * @return Selected project, or null if it could not be retrieved.
     */
    public static IProject getProjectFromSelected(ExecutionEvent event) {
        final IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
        final Object selected = selection.getFirstElement();
        if(selected instanceof IProjectNature) {
            final IProjectNature nature = (IProjectNature) selected;
            return nature.getProject();
        } else if(selected instanceof IProject) {
            return (IProject) selected;
        } else {
            return null;
        }
    }
}
