package org.metaborg.spoofax.eclipse.processing;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.metaborg.spoofax.eclipse.util.AbstractHandlerUtils;

public class AddNatureHandler extends AbstractHandler {
    @Override public Object execute(ExecutionEvent event) throws ExecutionException {
        final IProject project = AbstractHandlerUtils.getProjectFromSelected(event);
        if(project == null)
            return null;

        try {
            SpoofaxNature.addTo(project);
        } catch(CoreException e) {
            throw new ExecutionException("Cannot add Spoofax nature", e);
        }

        return null;
    }
}
