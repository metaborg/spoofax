package org.metaborg.core.project;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

public class DummyProjectService implements IProjectService {
    private static final ILogger logger = LoggerUtils.logger(DummyProjectService.class);

    
    @Override public IProject get(FileObject resource) {
        logger.warn("Using dummy project service which always returns null. "
            + "Bind an actual implementation of IProjectService in your Guice module.");
        return null;
    }
}
