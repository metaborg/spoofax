package org.metaborg.spoofax.eclipse.util;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public final class AbstractHandlerUtils {
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
