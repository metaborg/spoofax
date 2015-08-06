package org.metaborg.core.project;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;

import com.google.common.collect.Maps;

public class SimpleProjectService implements ISimpleProjectService {
    private ConcurrentMap<FileName, IProject> projects = Maps.newConcurrentMap();


    @Override public @Nullable IProject get(FileObject resource) {
        final FileName name = resource.getName();
        for(Entry<FileName, IProject> entry : projects.entrySet()) {
            final FileName projectName = entry.getKey();
            if(name.equals(projectName) || name.isAncestor(projectName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override public IProject create(FileObject location) throws MetaborgException {
        final FileName name = location.getName();
        for(FileName projectName : projects.keySet()) {
            if(name.equals(projectName) || name.isAncestor(projectName)) {
                final String message =
                    String.format("Location %s is equal to or nested in project %s", name, projectName);
                throw new MetaborgException(message);
            }
        }

        final IProject project = new Project(location);
        if(projects.putIfAbsent(name, project) != null) {
            final String message = String.format("Project with location %s already exists", name);
            throw new MetaborgException(message);
        }
        return project;
    }

    @Override public void remove(IProject project) throws MetaborgException {
        final FileName name = project.location().getName();
        if(projects.remove(name) == null) {
            final String message = String.format("Project with location %s does not exists", name);
            throw new MetaborgException(message);
        }
    }
}
