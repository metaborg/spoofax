package org.metaborg.core.project;

import org.apache.commons.vfs2.FileObject;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyProjectService implements IProjectService, IMavenProjectService {
    private static final long serialVersionUID = 1835155456881028321L;
    private static final Logger logger = LoggerFactory.getLogger(DummyProjectService.class);


    @Override public IProject get(FileObject resource) {
        logger.error("Using dummy project service which always returns null. "
            + "Bind an actual implementation of IProjectService in your Guice module.");
        return null;
    }

    @Override public MavenProject get(IProject project) {
        logger.error("Using dummy Maven project service which always returns null. "
            + "Bind an actual implementation of IMavenProjectService in your Guice module.");
        return null;
    }
}
