package org.metaborg.spoofax.eclipse.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.build.SpoofaxProjectBuilder;
import org.metaborg.spoofax.eclipse.util.BuilderUtils;

public class SpoofaxNature implements IProjectNature {
    public static final String id = SpoofaxPlugin.id + ".nature";

    private IProject project;


    @Override public void configure() throws CoreException {
        BuilderUtils.addTo(SpoofaxProjectBuilder.id, project);
    }

    @Override public void deconfigure() throws CoreException {
        BuilderUtils.removeFrom(SpoofaxProjectBuilder.id, project);
    }

    @Override public IProject getProject() {
        return project;
    }

    @Override public void setProject(IProject project) {
        this.project = project;
    }
}
