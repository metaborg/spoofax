package org.metaborg.spoofax.eclipse.processing;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;

public class SpoofaxNature implements IProjectNature {
    public static final String qualifiedId = SpoofaxPlugin.id + ".nature";

    private IProject project;


    /**
     * Adds this nature to given project. Does nothing if this nature has already been added to the
     * project. Adding this nature also adds a {@link SpoofaxProjectBuilder} to the project.
     * 
     * @param project
     *            Project to add the nature to.
     * @throws CoreException
     *             when {@link IProject#getDescription} throws a CoreException.
     */
    public static void addTo(IProject project) throws CoreException {
        final IProjectDescription description = project.getDescription();
        final String[] natures = description.getNatureIds();
        if(natureIndex(natures) == -1) {
            final String[] newNatures = ArrayUtils.add(natures, 0, SpoofaxNature.qualifiedId);
            description.setNatureIds(newNatures);
            project.setDescription(description, null);
        }
    }

    /**
     * Removes this nature from given project. Does nothing if the nature has not been added to the
     * project. Removing this nature also removes the {@link SpoofaxProjectBuilder} from the
     * project.
     * 
     * @param project
     *            Project to remove the nature from.
     * @throws CoreException
     *             when {@link IProject#getDescription} or {@link IProject#setDescription} throws a
     *             CoreException.
     */
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
