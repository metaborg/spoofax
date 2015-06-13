package org.metaborg.spoofax.core.project;

import javax.annotation.Nullable;

import org.apache.maven.project.MavenProject;

public interface IMavenProjectService {
    public abstract @Nullable MavenProject get(IProject project);
}
