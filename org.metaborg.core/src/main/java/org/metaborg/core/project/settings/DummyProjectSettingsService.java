package org.metaborg.core.project.settings;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyProjectSettingsService implements IProjectSettingsService {
    private static final Logger logger = LoggerFactory.getLogger(DummyProjectSettingsService.class);


    @Override public IProjectSettings get(IProject project) {
        logger.warn("Using dummy project settings service which always returns null. "
            + "Bind an actual implementation of IProjectSettingsService in your Guice module.");
        return null;
    }

    @Override public IProjectSettings get(FileObject location) {
        logger.warn("Using dummy project settings service which always returns null. "
            + "Bind an actual implementation of IProjectSettingsService in your Guice module.");
        return null;
    }
}
