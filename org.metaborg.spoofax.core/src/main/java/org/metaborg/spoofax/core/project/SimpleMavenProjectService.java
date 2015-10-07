package org.metaborg.spoofax.core.project;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;

import org.apache.maven.project.MavenProject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.project.IProject;

import com.google.common.collect.Maps;

public class SimpleMavenProjectService implements ISimpleMavenProjectService {
    private ConcurrentMap<IProject, MavenProject> projects = Maps.newConcurrentMap();


    @Override public @Nullable MavenProject get(IProject project) {
        return projects.get(project);
    }

    @Override public MavenProject add(IProject project, MavenProject mavenProject) throws MetaborgException {
        if(projects.putIfAbsent(project, mavenProject) != null) {
            final String message = String.format("Maven project for Metaborg project %s already exists", project);
            throw new MetaborgException(message);
        }
        return mavenProject;
    }

    @Override public void remove(IProject project) throws MetaborgException {
        if(projects.remove(project) == null) {
            final String message = String.format("Maven project for Metaborg project %s does not exists", project);
            throw new MetaborgException(message);
        }
    }
}
