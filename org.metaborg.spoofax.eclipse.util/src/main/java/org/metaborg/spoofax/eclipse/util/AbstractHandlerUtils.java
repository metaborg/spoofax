package org.metaborg.spoofax.eclipse.util;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.IAdaptable;
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
        return getProjectFromElement(selected);
    }

    /**
     * Attempts to retrieve the project from a selection element.
     * 
     * @param selected
     *            Selected element.
     * @return Project from selected element, or null if no project could be retrieved.
     */
    public static IProject getProjectFromElement(Object selected) {
        if(selected instanceof IProjectNature) {
            // Test for project nature as well, in the Package explorer, Java projects of class JavaProject,
            // which implements IProjectNature.
            final IProjectNature nature = (IProjectNature) selected;
            return nature.getProject();
        } else if(selected instanceof IProject) {
            return (IProject) selected;
        } else if(selected instanceof IAdaptable) {
            final IAdaptable adaptable = (IAdaptable) selected;
            return (IProject) adaptable.getAdapter(IProject.class);
        } else {
            return null;
        }
    }
}
