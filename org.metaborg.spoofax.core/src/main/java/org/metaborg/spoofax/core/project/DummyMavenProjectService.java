package org.metaborg.spoofax.core.project;

import org.apache.maven.project.MavenProject;
import org.metaborg.core.project.IProject;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

/**
 * Dummy Maven project service that gets bound by default, to display sane warnings when nothing else is bound to
 * {@link IMavenProjectService}. Replace with working implementation or bind {@link NullMavenProjectService} to disable
 * the warning.
 */
@Deprecated
public class DummyMavenProjectService implements IMavenProjectService {
    private static final ILogger logger = LoggerUtils.logger(DummyMavenProjectService.class);


    @Override public MavenProject get(IProject project) {
        logger.warn("Using dummy Maven project service which always returns null. "
            + "Bind an actual implementation of IMavenProjectService in your Guice module.");
        return null;
    }
}
