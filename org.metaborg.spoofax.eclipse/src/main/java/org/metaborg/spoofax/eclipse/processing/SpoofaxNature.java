package org.metaborg.spoofax.eclipse.processing;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class SpoofaxNature implements IProjectNature {
    public static final String qualifiedId = "org.metaborg.spoofax.eclipse.nature";

    private IProject project;


    public static void addTo(IProject project) throws CoreException {
        final IProjectDescription description = project.getDescription();
        final String[] natures = description.getNatureIds();
        if(natureIndex(natures) == -1) {
            final String[] newNatures = ArrayUtils.add(natures, 0, SpoofaxNature.qualifiedId);
            description.setNatureIds(newNatures);
            project.setDescription(description, null);
        }
    }

    public static void removeFrom(IProject project) throws CoreException {
        final IProjectDescription description = project.getDescription();
        final String[] natures = description.getNatureIds();
        final int natureIndex = natureIndex(natures);
        if(natureIndex != -1) {
            final String[] newNatures = ArrayUtils.remove(natures, natureIndex);
            description.setNatureIds(newNatures);
            project.setDescription(description, null);
        }
    }

    private static int natureIndex(String[] natures) throws CoreException {
        for(int i = 0; i < natures.length; ++i) {
            final String nature = natures[i];
            if(nature.equals(qualifiedId)) {
                return i;
            }
        }
        return -1;
    }


    @Override public void configure() throws CoreException {
        SpoofaxProjectBuilder.addTo(project);
    }

    @Override public void deconfigure() throws CoreException {
        SpoofaxProjectBuilder.removeFrom(project);
    }

    @Override public IProject getProject() {
        return project;
    }

    @Override public void setProject(IProject project) {
        this.project = project;
    }
}
