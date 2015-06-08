package org.metaborg.spoofax.meta.core;

import javax.annotation.Nullable;
import org.apache.maven.project.MavenProject;
import org.metaborg.spoofax.core.project.IProject;

public interface IMavenProjectService {
    public abstract @Nullable MavenProject get(IProject project);
}
