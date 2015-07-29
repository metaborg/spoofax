package org.metaborg.spoofax.core.project;

import javax.annotation.Nullable;

import org.apache.maven.project.MavenProject;
import org.metaborg.core.project.IProject;

public interface IMavenProjectService {
    public abstract @Nullable MavenProject get(IProject project);
}
