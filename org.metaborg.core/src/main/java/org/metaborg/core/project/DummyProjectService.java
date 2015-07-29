package org.metaborg.core.project;

import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyProjectService implements IProjectService, IProjectSettingsService {
    private static final Logger logger = LoggerFactory.getLogger(DummyProjectService.class);


    @Override public IProject get(FileObject resource) {
        logger.warn("Using dummy project service which always returns null. "
            + "Bind an actual implementation of IProjectService in your Guice module.");
        return null;
    }

    @Override public IProjectSettings get(IProject project) {
        logger.warn("Using dummy project settings service which always returns null. "
            + "Bind an actual implementation of IProjectSettingsService in your Guice module.");
        return null;
    }
}
