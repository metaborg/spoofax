package org.metaborg.core.project.settings;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

@Deprecated
public class DummyProjectSettingsService implements IProjectSettingsService {
    private static final ILogger logger = LoggerUtils.logger(DummyProjectSettingsService.class);


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
