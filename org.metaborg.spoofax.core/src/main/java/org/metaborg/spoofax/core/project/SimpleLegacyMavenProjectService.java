package org.metaborg.spoofax.core.project;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.maven.project.MavenProject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.project.IProject;

import com.google.common.collect.Maps;

@Deprecated
public class SimpleLegacyMavenProjectService implements ISimpleLegacyMavenProjectService {
    private final ConcurrentMap<FileObject, MavenProject> projects = Maps.newConcurrentMap();


    @Override public @Nullable MavenProject get(FileObject project) {
        return projects.get(project);
    }

    @Override public @Nullable MavenProject get(IProject project) {
        return projects.get(project.location());
    }

    @Override public MavenProject add(IProject project, MavenProject mavenProject) throws MetaborgException {
        if(projects.putIfAbsent(project.location(), mavenProject) != null) {
            final String message = String.format("Maven project for Metaborg project %s already exists", project);
            throw new MetaborgException(message);
        }
        return mavenProject;
    }

    @Override public void remove(IProject project) throws MetaborgException {
        if(projects.remove(project.location()) == null) {
            final String message = String.format("Maven project for Metaborg project %s does not exists", project);
            throw new MetaborgException(message);
        }
    }
}
