package org.metaborg.core.project;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleProjectService implements ISingleProjectService {
    private static final Logger logger = LoggerFactory.getLogger(SingleProjectService.class);

    private @Nullable IProject project;

    @Override public @Nullable IProject get(FileObject resource) {
        if(project == null) {
            return null;
        }

        final FileName resourceName = resource.getName();
        final FileName projectName = project.location().getName();
        if(!(projectName.equals(resourceName) || projectName.isDescendent(resourceName))) {
            logger.error("Resource {} outside project {}, returning null", resourceName, projectName);
            return null;
        }

        return project;
    }

    @Override public @Nullable IProject get() {
        return project;
    }

    @Override public void set(IProject project) {
        if(this.project != null) {
            final String message =
                String.format("Trying to set project while it has already been set to %s", this.project);
            throw new MetaborgRuntimeException(message);
        }
        this.project = project;
    }
}
