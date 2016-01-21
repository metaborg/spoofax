package org.metaborg.core.project.settings;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

@Deprecated
public class DummyLegacyProjectSettingsService implements ILegacyProjectSettingsService {
    private static final ILogger logger = LoggerUtils.logger(DummyLegacyProjectSettingsService.class);


    @Override public ILegacyProjectSettings get(IProject project) {
        logger.warn("Using dummy project settings service which always returns null. "
            + "Bind an actual implementation of IProjectSettingsService in your Guice module.");
        return null;
    }

    @Override public ILegacyProjectSettings get(FileObject location) {
        logger.warn("Using dummy project settings service which always returns null. "
            + "Bind an actual implementation of IProjectSettingsService in your Guice module.");
        return null;
    }
}
